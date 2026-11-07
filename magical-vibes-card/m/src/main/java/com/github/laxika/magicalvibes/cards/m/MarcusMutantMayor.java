package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "109")
@CardRegistration(set = "PIP", collectorNumber = "418")
@CardRegistration(set = "PIP", collectorNumber = "637")
@CardRegistration(set = "PIP", collectorNumber = "946")
public class MarcusMutantMayor extends Card {

    public MarcusMutantMayor() {
        TriggeringPermanentMatches hasCounter = new TriggeringPermanentMatches(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE));
        // One trigger; the dealing creature's counters are checked as it resolves ("draw a card if
        // that creature has a +1/+1 counter on it. If it doesn't, put a +1/+1 counter on it").
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ConditionalEffect(hasCounter, new DrawCardEffect(1)),
                        new ConditionalEffect(new NotCondition(hasCounter),
                                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                        CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
