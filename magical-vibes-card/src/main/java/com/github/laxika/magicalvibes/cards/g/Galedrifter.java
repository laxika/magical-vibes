package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.Waildrifter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "MID", collectorNumber = "55")
public class Galedrifter extends Card {

    public Galedrifter() {
        setBackFaceCard(new Waildrifter());

        // Flying is auto-loaded from Scryfall keywords.

        // Disturb {4}{U}
        addCastingOption(new DisturbCast("{4}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "Waildrifter";
    }
}
