package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ConcertedCare;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "6")
public class BofurReliableGuardian extends Card {

    public BofurReliableGuardian() {
        setBackFaceCard(new ConcertedCare());
        addCastingOption(new AdventureCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ConcertedCare";
    }
}
