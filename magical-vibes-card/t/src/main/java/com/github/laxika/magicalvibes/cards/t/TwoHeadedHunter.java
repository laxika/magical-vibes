package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "155")
public class TwoHeadedHunter extends Card {

    public TwoHeadedHunter() {
        setBackFaceCard(new TwiceTheRage());
        addCastingOption(new AdventureCast("{1}{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TwiceTheRage";
    }
}
