package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect;

@CardRegistration(set = "XLN", collectorNumber = "93")
public class BloodcrazedPaladin extends Card {

    public BloodcrazedPaladin() {
        // Flash is loaded from Scryfall as a keyword.
        // This creature enters with a +1/+1 counter on it for each creature that died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect());
    }
}
