package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PriceOfBeauty;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "84")
public class ConceitedWitch extends Card {

    public ConceitedWitch() {
        setBackFaceCard(new PriceOfBeauty());
        addCastingOption(new AdventureCast("{B}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "PriceOfBeauty";
    }
}
