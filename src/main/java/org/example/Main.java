package org.example;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Selenide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j; // Corrected: Removed extra semicolon

import java.util.List;

@Slf4j
public class Main {

    private static final String URL = "https://www.gdziepolek.pl/produkty/19569/concerta-tabletki-o-przedluzonym-uwalnianiu/apteki/w-dolnoslaskim";
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1382627345529176136/zg5B8jjGG6WE9Eu8Bv7U_YTb7GPmAFvSkq3WoDJ3mYASEniwxRTDC5WvSrwEIcybMJwv";

    // Define colors as constants for readability and easy modification
    private static final int COLOR_GREEN = 65280; // 0x00FF00
    private static final int COLOR_RED = 16711680;  // 0xFF0000
    private static final int COLOR_BLUE = 3447003;  // 0x3498DB (Default/Informational)

    public static void main(String[] args) {
        SelenideConfig.setup();

        GdziePoLekPage gdziePoLekPage = new GdziePoLekPage();
        Selenide.open(URL);

        Selenide.sleep(5000); // Consider if this sleep is truly necessary or if explicit waits are better
        gdziePoLekPage.getPharmacies().shouldHave(CollectionCondition.sizeGreaterThan(0));
        log.info("Found " + gdziePoLekPage.getPharmacies().size() + " pharmacies.");

        List<Pharmacy> pharmacies = gdziePoLekPage.getPharmacies().asFixedIterable()
                .stream()
                .map(Pharmacy::new)
                .peek(pharmacy -> {
                    log.info(pharmacy.toString());
                })
                .toList();
        Selenide.closeWebDriver();

        // --- Discord Webhook Post Request ---

        if (pharmacies.isEmpty()) {
            log.warn("No pharmacies found. Not sending a Discord notification.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mainPayload = mapper.createObjectNode();
        ArrayNode embedsArray = mapper.createArrayNode();

        // Create a primary embed for the overview
        ObjectNode overviewEmbed = mapper.createObjectNode();
        overviewEmbed.put("title", "Concerta Pharmacy Stock Update");
        overviewEmbed.put("description", String.format("Found **%d** pharmacies in Dolnośląskie with Concerta stock.", pharmacies.size()));
        overviewEmbed.put("color", COLOR_BLUE); // Default overview color, or use green if always positive
        overviewEmbed.put("url", URL); // Link back to the source page

        embedsArray.add(overviewEmbed);

        // Add each pharmacy as a field within another embed or as separate embeds for more details
        for (int i = 0; i < Math.min(pharmacies.size(), 10); i++) { // Limit to 10 embeds for Discord's limit
            Pharmacy pharmacy = pharmacies.get(i);
            ObjectNode pharmacyEmbed = mapper.createObjectNode();

            // Determine the color based on remainingStock
            int embedColor;
            String remainingStock = pharmacy.getRemainingStock();
            if (remainingStock != null) {
                String lowerCaseStock = remainingStock.toLowerCase();
                if (lowerCaseStock.contains("ostatnia sztuka")) {
                    embedColor = COLOR_RED;
                } else if (lowerCaseStock.contains("kilka sztuk")) {
                    embedColor = COLOR_GREEN;
                } else {
                    embedColor = COLOR_BLUE; // Default color if neither matches
                }
            } else {
                embedColor = COLOR_BLUE; // Default if stock info is null
            }

            pharmacyEmbed.put("title", pharmacy.getTitle());
            pharmacyEmbed.put("description", pharmacy.getAddress());
            pharmacyEmbed.put("color", embedColor); // Set the determined color

            ArrayNode fields = mapper.createArrayNode();
            fields.add(mapper.createObjectNode().put("name", "Distance").put("value", pharmacy.getDistance()).put("inline", true));
            fields.add(mapper.createObjectNode().put("name", "Opening Hours").put("value", pharmacy.getOpeningHours()).put("inline", true));
            fields.add(mapper.createObjectNode().put("name", "Remaining Stock").put("value", pharmacy.getRemainingStock()).put("inline", true));
            fields.add(mapper.createObjectNode().put("name", "Last Updated").put("value", pharmacy.getUpdatedAt()).put("inline", false)); // Full width

            pharmacyEmbed.set("fields", fields);
            embedsArray.add(pharmacyEmbed);
        }

        mainPayload.set("embeds", embedsArray);

        // Optionally, add a simple content field if you want text above the embeds
        mainPayload.put("content", "New Concerta Pharmacy Stock Update:");


        String jsonBody = null;
        try {
            jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mainPayload);
            log.info("Sending Discord message:\n{}", jsonBody);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error serializing JSON payload", e);
            return;
        }

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(WEBHOOK_URL)
                .then()
                .statusCode(204)
                .log().all();

        log.info("Discord webhook request sent.");
    }
}