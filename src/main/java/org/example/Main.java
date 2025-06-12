package org.example;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

public class Main {

    private static final String URL = "https://www.gdziepolek.pl/produkty/19569/concerta-tabletki-o-przedluzonym-uwalnianiu/apteki/w-dolnoslaskim";

    public static void main(String[] args) {
        GdziePoLekPage gdziePoLekPage = new GdziePoLekPage();
        System.out.println("Hello world!");
        Selenide.open(URL);
        sleep(3000);
        System.out.println(gdziePoLekPage.getPharmacies().size());
        gdziePoLekPage.getPharmacies().asFixedIterable()
                .stream()
                .map(Pharmacy::new)
                .peek(System.out::println)
                .toList();


    }
}