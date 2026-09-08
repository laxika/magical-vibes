package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DizzyingSwoop;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "5")
public class ArdenvaleTacticianDizzyingSwoop extends Card {

    public ArdenvaleTacticianDizzyingSwoop() {
        setBackFaceCard(new DizzyingSwoop());
        addCastingOption(new AdventureCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DizzyingSwoop";
    }
}
