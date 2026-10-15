package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "ARB", collectorNumber = "73")
public class MycoidShepherd extends Card {

    public MycoidShepherd() {
        // Whenever this creature or another creature you control with power 5 or greater dies, you may
        // gain 5 life. "This creature" is unconditional (Mycoid Shepherd is 5/4); another creature's
        // power is checked against its last-known information on the battlefield.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new GainLifeEffect(5), "Gain 5 life?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentPowerAtLeastPredicate(5),
                new MayEffect(new GainLifeEffect(5), "Gain 5 life?")));
    }
}
