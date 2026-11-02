package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "122")
@CardRegistration(set = "PIP", collectorNumber = "650")
public class Vault87ForcedEvolution extends Card {

    public Vault87ForcedEvolution() {
        PermanentAllOfPredicate nonMutantCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MUTANT)))
        ));
        addTrigger(Triggers.chapter(1),
                GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD, nonMutantCreature));
        setSagaChapterTargetFilter(1, Set.of(TargetFilters.creature()));

        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addTrigger(Triggers.chapter(2),
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.MUTANT));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));

        addTrigger(Triggers.chapter(3), new DrawCardEffect(new GreatestPowerAmongControlled(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MUTANT))
        )));
    }
}
