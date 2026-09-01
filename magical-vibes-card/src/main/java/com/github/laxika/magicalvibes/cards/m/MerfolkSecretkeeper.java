package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VentureDeeper;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "53")
public class MerfolkSecretkeeper extends Card {

    public MerfolkSecretkeeper() {
        setBackFaceCard(new VentureDeeper());
        addCastingOption(new AdventureCast("{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "VentureDeeper";
    }
}
