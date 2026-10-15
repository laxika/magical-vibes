package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD, nonMutantCreature));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(TargetFilters.creature()));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.MUTANT));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(new GreatestPowerAmongControlled(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MUTANT))
        )));
    }
}
