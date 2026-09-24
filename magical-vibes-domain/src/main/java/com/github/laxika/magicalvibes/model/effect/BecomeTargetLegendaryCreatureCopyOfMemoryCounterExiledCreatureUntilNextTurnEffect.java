package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/** Makes the targeted legendary creature a copy of a memory-counter exiled creature. */
public record BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect()
        implements CardEffect {

    private static PermanentAllOfPredicate legendaryCreature() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(legendaryCreature()),
                TargetPredicates.exiledCards(new CardTypePredicate(CardType.CREATURE))));
    }

    @Override
    public boolean targetsAllExiledCardsInAbility() {
        return true;
    }
}
