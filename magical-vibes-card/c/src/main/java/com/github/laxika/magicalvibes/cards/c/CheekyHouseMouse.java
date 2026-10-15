package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SqueakBy;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "7")
public class CheekyHouseMouse extends Card {

    public CheekyHouseMouse() {
        setBackFaceCard(new SqueakBy());
        addCastingOption(new AdventureCast("{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SqueakBy";
    }
}
