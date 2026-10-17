package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "PIP", collectorNumber = "109")
@CardRegistration(set = "PIP", collectorNumber = "418")
@CardRegistration(set = "PIP", collectorNumber = "637")
@CardRegistration(set = "PIP", collectorNumber = "946")
public class MarcusMutantMayor extends Card {

    public MarcusMutantMayor() {
        SourceCounterThreshold hasCounter = new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE);
        // One trigger; the dealing creature's counters are checked as it resolves ("draw a card if
        // that creature has a +1/+1 counter on it. If it doesn't, put a +1/+1 counter on it").
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, SequenceEffect.of(
                        ConditionalEffect.unless(hasCounter, new DrawCardEffect(1)),
                        ConditionalEffect.unless(new NotCondition(hasCounter),
                                new PutCountersOnSourceEffect(1, 1, 1))), true));
    }
}
