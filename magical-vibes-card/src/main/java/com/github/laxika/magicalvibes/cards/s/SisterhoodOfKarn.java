package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "109")
@CardRegistration(set = "WHO", collectorNumber = "399")
@CardRegistration(set = "WHO", collectorNumber = "714")
@CardRegistration(set = "WHO", collectorNumber = "990")
public class SisterhoodOfKarn extends Card {

    public SisterhoodOfKarn() {
        // This creature enters with a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        // Paradox — Whenever you cast a spell from anywhere other than your hand, double the
        // number of +1/+1 counters on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(List.of(
                        new DoublePlusOneCountersOnSourceEffect())));
    }
}
