package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "INR", collectorNumber = "47")
@CardRegistration(set = "INR", collectorNumber = "452")
public class TwinbladeGeist extends Card {

    public TwinbladeGeist() {
        setBackFaceCard(new TwinbladeInvocation());

        // Double strike is auto-loaded from Scryfall keywords.

        // Disturb {2}{W}
        addCastingOption(new DisturbCast("{2}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TwinbladeInvocation";
    }
}
