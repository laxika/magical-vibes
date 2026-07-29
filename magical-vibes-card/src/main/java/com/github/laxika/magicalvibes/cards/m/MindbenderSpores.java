package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentEffect;

@CardRegistration(set = "MIR", collectorNumber = "229")
public class MindbenderSpores extends Card {

    public MindbenderSpores() {
        // Whenever this creature blocks a creature, put four fungus counters on that creature. The two
        // abilities the blocked creature gains ("doesn't untap while it has a fungus counter" and the
        // upkeep trigger removing one) ride along with the counters — they are granted wherever fungus
        // counters are placed, so they survive Mindbender Spores leaving the battlefield.
        addEffect(EffectSlot.ON_BLOCK, new PutCounterOnCombatOpponentEffect(CounterType.FUNGUS, 4));
    }
}
