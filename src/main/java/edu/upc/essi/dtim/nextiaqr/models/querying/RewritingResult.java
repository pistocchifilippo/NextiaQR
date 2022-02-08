package edu.upc.essi.dtim.nextiaqr.models.querying;

import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Map;
import java.util.Set;

public class RewritingResult {

    Set<ConjunctiveQuery> CQs;
    Map<String,Integer> projectionOrder;
    Map<String,String> featuresPerAttribute;

    public RewritingResult() {
        CQs = Sets.newHashSet();
        projectionOrder = Maps.newHashMap();
        featuresPerAttribute = Maps.newHashMap();
    }

    public Set<ConjunctiveQuery> getCQs() {
        return CQs;
    }

    public void setCQs(Set<ConjunctiveQuery> CQs) {
        this.CQs = CQs;
    }

    public Map<String, Integer> getProjectionOrder() {
        return projectionOrder;
    }

    public void setProjectionOrder(Map<String, Integer> projectionOrder) {
        this.projectionOrder = projectionOrder;
    }

    public Map<String, String> getFeaturesPerAttribute() {
        return featuresPerAttribute;
    }

    public void setFeaturesPerAttribute(Map<String, String> featuresPerAttribute) {
        this.featuresPerAttribute = featuresPerAttribute;
    }
}
