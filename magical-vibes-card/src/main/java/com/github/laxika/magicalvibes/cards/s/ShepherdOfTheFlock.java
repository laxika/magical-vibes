package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UsherToSafety;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "28")
public class ShepherdOfTheFlock extends Card {

    public ShepherdOfTheFlock() {
        setBackFaceCard(new UsherToSafety());
        addCastingOption(new AdventureCast("{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "UsherToSafety";
    }
}
