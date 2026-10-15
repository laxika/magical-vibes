package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterEachTargetSpellEffect;

@CardRegistration(set = "ARB", collectorNumber = "87")
public class DoubleNegative extends Card {

    public DoubleNegative() {
        // "Counter up to two target spells." A single 0–2 spell-on-stack target group (null filter =
        // any spell); the each-target counter effect counters every spell chosen for that group.
        target(null, 0, 2).addEffect(EffectSlot.SPELL, new CounterEachTargetSpellEffect());
    }
}
