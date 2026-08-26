package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TreatsToShare;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "150")
public class CuriousPair extends Card {

    public CuriousPair() {
        setBackFaceCard(new TreatsToShare());
        addCastingOption(new AdventureCast("{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TreatsToShare";
    }
}
