package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShieldsMight;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "156")
public class GarenbrigCarver extends Card {

    public GarenbrigCarver() {
        setBackFaceCard(new ShieldsMight());
        addCastingOption(new AdventureCast("{1}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ShieldsMight";
    }
}
