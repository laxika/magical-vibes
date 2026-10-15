package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect;

@CardRegistration(set = "XLN", collectorNumber = "199")
public class OldGrowthDryads extends Card {

    public OldGrowthDryads() {
        // When this creature enters, each opponent may search their library for a basic land card,
        // put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect());
    }
}
