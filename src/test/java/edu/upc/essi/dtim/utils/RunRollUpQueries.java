package edu.upc.essi.dtim.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.upc.essi.dtim.NextiaQR;
import edu.upc.essi.dtim.nextiaqr.models.querying.ConjunctiveQuery;
import edu.upc.essi.dtim.nextiaqr.models.querying.Wrapper;
import edu.upc.essi.dtim.nextiaqr.models.querying.wrapper_impl.CSV_Wrapper;
import edu.upc.essi.dtim.nextiaqr.utils.Tuple2;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunRollUpQueries {
    private static String basePath = "/Users/filippo/Desktop/NextiaQR/";

    public static void main(String[] args) throws Exception {
        String scenario = "GeographicCiReCoCo"; // fix with the scenario
        String baseURI = "http://www.essi.upc.edu/~snadal/"+scenario;
        String scenarioPath = basePath + "src/test/resources/scenarios/"+scenario+"/";

        String jenaPath = "TestScenarioRunnerDataset";

        //Remove old Jena
        FileUtils.deleteDirectory(new File("TestScenarioRunnerDataset"));

        Map<String, String> prefixes = TestUtils.populatePrefixes(scenarioPath + "prefixes.txt");
        TestUtils.populateTriples(jenaPath, baseURI, scenarioPath + "metamodel.txt", prefixes);
        TestUtils.populateTriples(jenaPath, baseURI, scenarioPath + "global_graph.txt", prefixes);
        TestUtils.populateTriples(jenaPath, baseURI,scenarioPath+"source_graph.txt", prefixes);
        TestUtils.populateMappings(jenaPath,scenarioPath+"mappings.txt",
                scenarioPath + "global_graph.txt", prefixes);

        List<Tuple2<String, String>> queries = TestUtils.getQueries(scenarioPath + "queries.txt", prefixes);

        Dataset T = TDBFactory.createDataset("TestScenarioRunnerDataset");
        T.begin(ReadWrite.READ);

        final Tuple2<String, String> minimal = queries.remove(0);
        final List<Set<ConjunctiveQuery>> CQs = new LinkedList<>();
        final List<String> SQLs = new LinkedList<>();
        for (Tuple2<String, String> query : queries) {
            System.out.println(query._2());
            System.out.println(minimal._2());
            //1 -- Rewrite SPARQL to UCQs
            Set<ConjunctiveQuery> CQ = NextiaQR.rewriteToUnionOfConjunctiveQueries(query._2,T,minimal._2);
            System.out.println(CQ);
            System.out.println();


            Map<String,String> iriToCSVPath = Maps.newHashMap();
            Files.readAllLines(new File(scenarioPath+"wrappers_files.txt").toPath()).stream().forEach(s->{
                iriToCSVPath.put(s.split(",")[0],s.split(",")[1]);
            });
            CQ.forEach(cq -> {
                Set<Wrapper> CSV_Wrappers = Sets.newHashSet();
                cq.getWrappers().forEach(w -> {
                    CSV_Wrapper csv = new CSV_Wrapper(w.getWrapper());
                    csv.setPath(iriToCSVPath.get(w.getWrapper()));
                    csv.setHeaderInFirstRow(true);
                    csv.setColumnDelimiter(",");
                    csv.setRowDelimiter("\n");
                    CSV_Wrappers.add(csv);
                });
                cq.setWrappers(CSV_Wrappers);
            });
            String SQL = NextiaQR.toSQL(CQ,null);
            System.out.println(SQL);


            CQs.add(CQ);

        }



        T.end();
        T.close();

        //Remove Jena
        FileUtils.deleteDirectory(new File("TestScenarioRunnerDataset"));
    }
}
