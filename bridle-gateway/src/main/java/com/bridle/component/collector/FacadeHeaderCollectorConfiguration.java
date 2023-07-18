package com.bridle.component.collector;

import com.bridle.component.collector.enums.ExpressionFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Validated
public class FacadeHeaderCollectorConfiguration {

    private Map<String, String> queryExpressionsByHeaderName = new HashMap<>();
    @NotNull
    private ExpressionFormat expressionFormat;

    public Map<String, String> getQueryExpressionsByHeaderName() {
        return queryExpressionsByHeaderName;
    }

    public void setQueryExpressionsByHeaderName(Map<String, String> queryExpressionsByHeaderName) {
        this.queryExpressionsByHeaderName = queryExpressionsByHeaderName;
    }

    public ExpressionFormat getExpressionFormat() {
        return expressionFormat;
    }

    public void setExpressionFormat(ExpressionFormat expressionFormat) {
        this.expressionFormat = expressionFormat;
    }
}
