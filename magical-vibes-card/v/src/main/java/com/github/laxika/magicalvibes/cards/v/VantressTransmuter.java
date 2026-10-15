package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CroakingCurse;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "75")
public class VantressTransmuter extends Card {

    public VantressTransmuter() {
        setBackFaceCard(new CroakingCurse());
        addCastingOption(new AdventureCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CroakingCurse";
    }
}
