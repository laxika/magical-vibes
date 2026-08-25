package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "MID", collectorNumber = "28")
public class MourningPatrol extends Card {

    public MourningPatrol() {
        setBackFaceCard(new MorningApparition());

        // Disturb {3}{W}
        addCastingOption(new DisturbCast("{3}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "MorningApparition";
    }
}
