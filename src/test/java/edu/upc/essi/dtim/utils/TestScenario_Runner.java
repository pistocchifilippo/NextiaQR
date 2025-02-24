package edu.upc.essi.dtim.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.upc.essi.dtim.NextiaQR;
import edu.upc.essi.dtim.nextiaqr.functions.QueryRewriting;
import edu.upc.essi.dtim.nextiaqr.models.querying.ConjunctiveQuery;
import edu.upc.essi.dtim.nextiaqr.models.querying.RewritingResult;
import edu.upc.essi.dtim.nextiaqr.models.querying.Wrapper;
import edu.upc.essi.dtim.nextiaqr.models.querying.wrapper_impl.CSV_Wrapper;
import edu.upc.essi.dtim.nextiaqr.utils.Tuple2;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Given an scenario name (stored in ODIN/datasets/scenarios), it will parse all the files
 * (prefixes, metamodel, global graph, queries, ...) to rewrite the different queries.
 */
public class TestScenario_Runner {

    private static String basePath = "/Users/filippo/Desktop/NextiaQR/";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new Exception("Scenario name not provided");
        String scenario = args[0];
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

        for (Tuple2<String, String> query : queries) {
            System.out.println(query._1);

            //1 -- Rewrite SPARQL to UCQs
            RewritingResult CQs = NextiaQR.rewriteToUnionOfConjunctiveQueries(query._2,T,query._2);

            //2 -- Convert UCQs to SQL
            /**
             * Here we assume all wrappers are CSV and there exists a file 'wrappers_files.txt' that
             * maps the wrapper IRI  a file path
             */
            String SQL = NextiaQR.toSQL(CQs);
            System.out.println(SQL);
//            String SQL = NextiaQR.toSQL(CQs,null);
//
//            //3 -- Convert SQL to DATA
//            NextiaQR.executeSQL(CQs,SQL);
        }
        T.end();
        T.close();

        //Remove Jena
        FileUtils.deleteDirectory(new File("TestScenarioRunnerDataset"));
    }
}
