package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OakenBoon;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "180")
public class TuinvaleTreefolk extends Card {

    public TuinvaleTreefolk() {
        setBackFaceCard(new OakenBoon());
        addCastingOption(new AdventureCast("{3}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "OakenBoon";
    }
}
