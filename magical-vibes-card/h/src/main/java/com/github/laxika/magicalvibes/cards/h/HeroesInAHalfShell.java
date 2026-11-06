package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "6")
@CardRegistration(set = "TMC", collectorNumber = "96")
public class HeroesInAHalfShell extends Card {

    public HeroesInAHalfShell() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.MUTANT, CardSubtype.NINJA, CardSubtype.TURTLE))),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentTruePredicate(), EachPermanentScope.TRIGGERING_PERMANENTS),
                        new DrawCardEffect(1)));
    }
}
