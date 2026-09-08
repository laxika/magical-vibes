package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CurryFavor;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "105")
public class SmittenSwordmaster extends Card {

    public SmittenSwordmaster() {
        setBackFaceCard(new CurryFavor());
        addCastingOption(new AdventureCast("{B}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CurryFavor";
    }
}
