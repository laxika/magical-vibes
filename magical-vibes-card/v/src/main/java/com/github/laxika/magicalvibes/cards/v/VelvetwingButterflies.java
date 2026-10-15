package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GazeInWonder;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "30")
public class VelvetwingButterflies extends Card {

    public VelvetwingButterflies() {
        setBackFaceCard(new GazeInWonder());
        addCastingOption(new AdventureCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GazeInWonder";
    }
}
