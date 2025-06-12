package org.example;

import com.codeborne.selenide.ElementsCollection;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$$;

@Getter
public class GdziePoLekPage {

    private ElementsCollection pharmacies = $$("ul div.top");

}
