package org.example;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Selenide;
import lombok.extern.slf4j.Slf4j;

import static com.codeborne.selenide.Selenide.sleep;

@Slf4j
public class Main {

    private static final String URL = "https://www.gdziepolek.pl/produkty/19569/concerta-tabletki-o-przedluzonym-uwalnianiu/apteki/w-dolnoslaskim";

    public static void main(String[] args) {
        SelenideConfig.setup(); // <--- ADD THIS LINE

        GdziePoLekPage gdziePoLekPage = new GdziePoLekPage();
        System.out.println("Hello world!");
        Selenide.open(URL);
        gdziePoLekPage.getPharmacies().shouldHave(CollectionCondition.sizeGreaterThan(0));
        Selenide.screenshot("test");
        System.out.println(gdziePoLekPage.getPharmacies().size());
        gdziePoLekPage.getPharmacies().asFixedIterable()
                .stream()
                .map(Pharmacy::new)
                .peek(pharmacy -> {
                    log.info(pharmacy.toString());
                })
                .toList();
        Selenide.closeWebDriver();


    }
}