package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfRaidEffect;

@CardRegistration(set = "XLN", collectorNumber = "157")
public class RiggingRunner extends Card {

    public RiggingRunner() {
        // First strike is loaded from Scryfall.
        // Raid — This creature enters with a +1/+1 counter on it if you attacked this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersIfRaidEffect(1));
    }
}
