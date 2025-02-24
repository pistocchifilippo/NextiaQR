package edu.upc.essi.dtim;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.upc.essi.dtim.nextiaqr.functions.QueryRewriting;
import edu.upc.essi.dtim.nextiaqr.jena.GraphOperations;
import edu.upc.essi.dtim.nextiaqr.models.querying.ConjunctiveQuery;
import edu.upc.essi.dtim.nextiaqr.models.querying.RewritingResult;
import edu.upc.essi.dtim.nextiaqr.models.querying.Wrapper;
import edu.upc.essi.dtim.nextiaqr.models.querying.wrapper_impl.CSV_Wrapper;
import edu.upc.essi.dtim.nextiaqr.utils.SQLiteUtils;
import org.apache.jena.query.Dataset;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NextiaQR {

    public static RewritingResult rewriteToUnionOfConjunctiveQueries(String SPARQL, Dataset T, String SPARQL_REF) {
        RewritingResult res = new RewritingResult();
        res.setCQs(QueryRewriting.rewriteToUnionOfConjunctiveQueries(SPARQL,T,SPARQL_REF));
        res.setFeaturesPerAttribute(Maps.newHashMap(QueryRewriting.featuresPerAttribute));
        res.setProjectionOrder(Maps.newHashMap(QueryRewriting.projectionOrder));
        return res;
    }

    public static String toSQLAsRel (RewritingResult rewritingResult, Map<String,String> namesLut) {
        if (rewritingResult.getCQs().isEmpty()) return null;
        StringBuilder SQL = new StringBuilder();
        rewritingResult.getCQs().forEach(q -> {
            StringBuilder select = new StringBuilder("SELECT ");
            StringBuilder from = new StringBuilder(" FROM ");
            StringBuilder where = new StringBuilder(" WHERE ");
            //Sort the projections as they are indicated in the interface
            //First remove duplicates based on the features
            List<String> seenFeatures = Lists.newArrayList();
            List<String> withoutDuplicates = Lists.newArrayList();
            q.getProjections().forEach(proj -> {
                if (!seenFeatures.contains(rewritingResult.getFeaturesPerAttribute().get(proj))) {
                    withoutDuplicates.add(proj);
                    seenFeatures.add(rewritingResult.getFeaturesPerAttribute().get(proj));
                }
            });
            //Now do the sorting
            List<String> projections = Lists.newArrayList(withoutDuplicates)
                    .stream().filter(p -> rewritingResult.getProjectionOrder().containsKey(rewritingResult.getFeaturesPerAttribute().get(p))).collect(Collectors.toList());//Lists.newArrayList(q.getProjections());

            //projections.sort(Comparator.comparingInt(s -> listOfFeatures.indexOf(QueryRewriting.featuresPerAttribute.get(s))));
            projections.sort(Comparator.comparingInt(s -> rewritingResult.getProjectionOrder().get(rewritingResult.getFeaturesPerAttribute().get(s))));
            if (namesLut != null) {
                projections.forEach(proj -> select.append(""+GraphOperations.nn(proj).split("/")[GraphOperations.nn(proj).split("/").length-1]+""+" as " + namesLut.get(GraphOperations.nn(proj)) + ","));
            } else {
                projections.forEach(proj -> select.append("\""+GraphOperations.nn(proj).split("/")[GraphOperations.nn(proj).split("/").length-1]+"\""+","));
            }            //q.getWrappers().forEach(w -> from.append(wrapperIriToID.get(w.getWrapper())+","));
            q.getWrappers().forEach(w -> from.append(GraphOperations.nn(w.getWrapper())+","));
            q.getJoinConditions().forEach(j -> where.append(
                    "\""+GraphOperations.nn(j.getLeft_attribute()).split("/")[GraphOperations.nn(j.getLeft_attribute()).split("/").length-1]+"\""+
                            " = "+
                            "\""+GraphOperations.nn(j.getRight_attribute()).split("/")[GraphOperations.nn(j.getRight_attribute()).split("/").length-1]+"\""+
                            " AND "));
            SQL.append(select.substring(0,select.length()-1));
            SQL.append(from.substring(0,from.length()-1));
            if (!where.toString().equals(" WHERE ")) {
                SQL.append(where.substring(0, where.length() - " AND ".length()));
            }
            SQL.append(" UNION ");
        });
        String SQLstr = SQL.substring(0,SQL.length()-" UNION ".length())+""; //String SQLstr = SQL.substring(0,SQL.length()-" UNION ".length())+";";
        return SQLstr;
    }

    public static String toSQL (RewritingResult rewritingResult) {
        if (rewritingResult.getCQs().isEmpty()) return null;
        StringBuilder SQL = new StringBuilder();
        rewritingResult.getCQs().forEach(q -> {
            StringBuilder select = new StringBuilder("SELECT ");
            StringBuilder from = new StringBuilder(" FROM ");
            StringBuilder where = new StringBuilder(" WHERE ");
            //Sort the projections as they are indicated in the interface
            //First remove duplicates based on the features
            List<String> seenFeatures = Lists.newArrayList();
            List<String> withoutDuplicates = Lists.newArrayList();
            q.getProjections().forEach(proj -> {
                if (!seenFeatures.contains(rewritingResult.getFeaturesPerAttribute().get(proj))) {
                    withoutDuplicates.add(proj);
                    seenFeatures.add(rewritingResult.getFeaturesPerAttribute().get(proj));
                }
            });
            //Now do the sorting
            List<String> projections = Lists.newArrayList(withoutDuplicates)
                    .stream().filter(p -> rewritingResult.getProjectionOrder().containsKey(rewritingResult.getFeaturesPerAttribute().get(p))).collect(Collectors.toList());//Lists.newArrayList(q.getProjections());

            //projections.sort(Comparator.comparingInt(s -> listOfFeatures.indexOf(QueryRewriting.featuresPerAttribute.get(s))));
            projections.sort(Comparator.comparingInt(s -> rewritingResult.getProjectionOrder().get(rewritingResult.getFeaturesPerAttribute().get(s))));
            projections.forEach(proj -> select.append("\""+GraphOperations.nn(proj).split("/")[GraphOperations.nn(proj).split("/").length-1]+"\""+","));
            //q.getWrappers().forEach(w -> from.append(wrapperIriToID.get(w.getWrapper())+","));
            q.getWrappers().forEach(w -> from.append(GraphOperations.nn(w.getWrapper())+","));
            q.getJoinConditions().forEach(j -> where.append(
                    "\""+GraphOperations.nn(j.getLeft_attribute()).split("/")[GraphOperations.nn(j.getLeft_attribute()).split("/").length-1]+"\""+
                            " = "+
                            "\""+GraphOperations.nn(j.getRight_attribute()).split("/")[GraphOperations.nn(j.getRight_attribute()).split("/").length-1]+"\""+
                            " AND "));
            SQL.append(select.substring(0,select.length()-1));
            SQL.append(from.substring(0,from.length()-1));
            if (!where.toString().equals(" WHERE ")) {
                SQL.append(where.substring(0, where.length() - " AND ".length()));
            }
            SQL.append(" UNION ");
        });
        String SQLstr = SQL.substring(0,SQL.length()-" UNION ".length())+";";
        return SQLstr;
    }

    public static void executeSQL(Set<ConjunctiveQuery> UCQs, String SQL) {
        if (UCQs.isEmpty() || SQL == null) {
            System.out.println("The UCQ is empty, no output is generated");
        } else {
            Set<Wrapper> wrappersInUCQs = UCQs.stream().map(cq -> cq.getWrappers()).flatMap(wrappers -> wrappers.stream()).collect(Collectors.toSet());

            Set<CSV_Wrapper> CSVWrappers = wrappersInUCQs.stream().map(w -> (CSV_Wrapper) w).collect(Collectors.toSet());

            CSVWrappers.forEach(w -> {


            try {
                    w.inferSchema();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            CSVWrappers.forEach(w -> {
                SQLiteUtils.createTable(w);
                try {
                    w.populate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            System.out.println(SQL);
            System.out.println(SQLiteUtils.executeSelect(SQL));
        }
        //SQLiteUtils.executeSelect(SQL).forEach(d -> data.add(d));

        /*

        wrappersInUCQs.forEach(w -> {
            SQLiteUtils.createTable(w._2(),w._3());
            try {
                w._1().populate(w._2(),w._3());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println(SQL);
        SQLiteUtils.executeSelect(SQL,features).forEach(d -> data.add(d));*/


    }

}
