package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "121")
public class BellowingBruiser extends Card {

    public BellowingBruiser() {
        setBackFaceCard(new BeatAPath());
        addCastingOption(new AdventureCast("{2}{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BeatAPath";
    }
}
