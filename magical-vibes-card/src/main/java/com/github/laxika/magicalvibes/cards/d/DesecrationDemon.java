package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect;

@CardRegistration(set = "RTR", collectorNumber = "63")
public class DesecrationDemon extends Card {

    public DesecrationDemon() {
        // At the beginning of each combat, any opponent may sacrifice a creature of their choice.
        // If a player does, tap this creature and put a +1/+1 counter on it.
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect());
    }
}
