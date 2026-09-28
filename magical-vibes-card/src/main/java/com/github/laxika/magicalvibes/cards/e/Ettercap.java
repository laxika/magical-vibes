package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WebShot;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HBG", collectorNumber = "211")
public class Ettercap extends Card {

    public Ettercap() {
        setBackFaceCard(new WebShot());
        addCastingOption(new AdventureCast("{2}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "WebShot";
    }
}
