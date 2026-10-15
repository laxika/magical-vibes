package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MesmericGlare;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "49")
@CardRegistration(set = "ELD", collectorNumber = "283")
@CardRegistration(set = "FIC", collectorNumber = "264")
@CardRegistration(set = "WOC", collectorNumber = "96")
public class HypnoticSprite extends Card {

    public HypnoticSprite() {
        setBackFaceCard(new MesmericGlare());
        addCastingOption(new AdventureCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "MesmericGlare";
    }
}
