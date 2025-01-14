package com.bridle.component.collector;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.bridle.component.collector.CollectorTestUtils.MSG_ID_KEY;
import static com.bridle.component.collector.CollectorTestUtils.RQUID_VALUE;
import static com.bridle.component.collector.CollectorTestUtils.RQ_UID_KEY;
import static com.bridle.component.collector.CollectorTestUtils.SYS_ID_KEY;
import static com.bridle.component.collector.CollectorTestUtils.SYS_ID_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpathXmlValuesCollectorTest {

    private static final String BAD_XML = "<CIT_REQUEST>" + "  <SYSTEM>" + "</SYSTEM>";

    private static final String CORRECT_XML =
            "<CIT_REQUEST>" + "  <SYSTEM>" + "<" + RQ_UID_KEY + " Value=\"" + RQUID_VALUE + "\"/>" + "<" + SYS_ID_KEY +
                    " Value=\"" + SYS_ID_VALUE + "\"/>" + "  </SYSTEM>" + "</CIT_REQUEST>";

    @Test
    void collectValuesThrowsExceptionWhenBodyIsNull() {
        XpathXmlValuesCollector collector = new XpathXmlValuesCollector();
        HashMap<String, String> expressionsByName = new HashMap<>();
        assertThrows(XPathCollectorException.class, () -> collector.collectValues(null, expressionsByName));
    }

    @Test
    void collectValuesThrowsExceptionWhenBodyIsNotCorrectXml() {
        XpathXmlValuesCollector collector = new XpathXmlValuesCollector();
        Map<String, String> correctXpathExpressionsByName = createCorrectXpathExpressionsByName();
        assertThrows(XPathCollectorException.class,
                     () -> collector.collectValues(BAD_XML, correctXpathExpressionsByName));
    }

    @Test
    void collectValuesReturnsEmptyOptionalWhenExpressionMapIsEmpty() {
        XpathXmlValuesCollector collector = new XpathXmlValuesCollector();
        Optional<Map<String, Object>> result = collector.collectValues(CORRECT_XML, new HashMap<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void collectValuesReturnsMapOfValues() {
        XpathXmlValuesCollector collector = new XpathXmlValuesCollector();
        Optional<Map<String, Object>> result =
                collector.collectValues(CORRECT_XML, createCorrectXpathExpressionsByName());
        assertTrue(result.isPresent());
        assertEquals(RQUID_VALUE, result.get().get(RQ_UID_KEY));
        assertEquals(SYS_ID_VALUE, result.get().get(SYS_ID_KEY));
        assertNull(result.get().get(MSG_ID_KEY));
    }

    private Map<String, String> createCorrectXpathExpressionsByName() {
        Map<String, String> expressionsByName = new HashMap<>();
        expressionsByName.put(RQ_UID_KEY, "/CIT_REQUEST/SYSTEM/" + RQ_UID_KEY + "/@Value ");
        expressionsByName.put(SYS_ID_KEY, "/CIT_REQUEST/SYSTEM/" + SYS_ID_KEY + "/@Value");
        expressionsByName.put(MSG_ID_KEY, "/CIT_REQUEST/SYSTEM/" + MSG_ID_KEY + "/@Value");
        return expressionsByName;
    }
}