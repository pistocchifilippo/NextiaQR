package edu.upc.essi.dtim;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.upc.essi.dtim.nextiaqr.functions.QueryRewriting;
import edu.upc.essi.dtim.nextiaqr.functions.QueryRewritingRDFS;
import edu.upc.essi.dtim.nextiaqr.jena.GraphOperations;
import edu.upc.essi.dtim.nextiaqr.models.querying.ConjunctiveQuery;
import edu.upc.essi.dtim.nextiaqr.models.querying.Wrapper;
import edu.upc.essi.dtim.nextiaqr.models.querying.wrapper_impl.CSV_Wrapper;
import edu.upc.essi.dtim.nextiaqr.utils.SQLiteUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NextiaQR_RDFS {

    public static Set<ConjunctiveQuery> rewriteToUnionOfConjunctiveQueries(Map<String, Model> sourceGraphs, Model minimal,
                                                                           Map<String, Model> subgraphs, String query) {
        QueryRewritingRDFS.parseSPARQL(query);
        return QueryRewriting.rewriteToUnionOfConjunctiveQueries(null,null);
    }


    public static void main(String[] args) {
        Map<String, Model> sourceGraphs = Maps.newHashMap();
        sourceGraphs.put("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/824f259815094f79bb0a5cac03ae8348",
                RDFDataMgr.loadModel("src/test/resources/qr_rdfs/source-graph6289942169144221467.g", Lang.TTL));
        sourceGraphs.put("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/28cd712c43eb4fddb0e4ea4e6e302737",
                RDFDataMgr.loadModel("src/test/resources/qr_rdfs/source-graph844563518469037388.g", Lang.TTL));

        Model minimal = RDFDataMgr.loadModel("src/test/resources/qr_rdfs/minimal-graph5681704536684986734.g", Lang.TTL);

        Map<String, Model> subgraphs = Maps.newHashMap();
        subgraphs.put("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/824f259815094f79bb0a5cac03ae8348",
                RDFDataMgr.loadModel("src/test/resources/qr_rdfs/subgraphs2123728943094953028.g", Lang.TTL));
        subgraphs.put("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/28cd712c43eb4fddb0e4ea4e6e302737",
                RDFDataMgr.loadModel("src/test/resources/qr_rdfs/subgraphs11519542500731068756.g", Lang.TTL));

        String query = "PREFIX nextiaDI: <http://www.essi.upc.edu/DTIM/NextiaDI/> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?id ?title " +
                "WHERE { " +
                " VALUES (?id ?title) { ( nextiaDI:identifier_idObject nextiaDI:title_title ) } " +
                " nextiaDI:identifier_idObject  rdfs:domain nextiaDI:artworks_collections . " +
                " nextiaDI:title_title rdfs:domain nextiaDI:artworks_collections " +
                "}";


        rewriteToUnionOfConjunctiveQueries(sourceGraphs,minimal,subgraphs,query);

        System.out.println(query);
    }

}
