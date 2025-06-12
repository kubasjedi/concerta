package org.example;

import com.codeborne.selenide.SelenideElement;
import lombok.Data;

@Data
public class Pharmacy {
    private String distance;
    private String title;
    private String address;
    private String openingHours;
    private String remainingStock;
    private String updatedAt;

    public Pharmacy(SelenideElement elementCard) {
        distance = elementCard.$x(".//p[1]").getText();
        title = elementCard.$x(".//a[1]").getText();
        address = elementCard.$x(".//p[2]").getText();
        openingHours = elementCard.$("div.MuiTypography-body1").getText();
        remainingStock = elementCard.$("p.MuiTypography-body1 span").getText();
        updatedAt = elementCard.$("p.MuiTypography-body2:nth-child(2)").getText();
    }
}
