package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class IfritWardenOfInferno extends Card {

    public IfritWardenOfInferno() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new SourceFightsTargetCreatureEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        "Target must be another creature"), 0, 1)));

        addBrimstoneEffects(EffectSlot.SAGA_CHAPTER_II);
        addBrimstoneEffects(EffectSlot.SAGA_CHAPTER_III);
    }

    private void addBrimstoneEffects(EffectSlot slot) {
        addEffect(slot, new AwardManaEffect(ManaColor.RED, 4));
        addEffect(slot, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LORE),
                new ExileSelfAndReturnTransformedEffect()));
    }
}
