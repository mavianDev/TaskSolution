package org.learn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.learn.Solution.CrptApi;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        ObjectNode document = new ObjectMapper().createObjectNode();
        document.put("doc_id", "12345");

        String signature = "signature";

        boolean success = api.createDocument(document, signature);
        System.out.println("Создание документа: " + (success ? "успешно!" : "провалено!"));
    }
}