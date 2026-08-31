package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

/**
 * Registers a delayed trigger that watches a target Wall for damage dealt by attacking creatures
 * until end of turn.
 */
public record RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect(List<CardEffect> effects)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentHasSubtypePredicate(CardSubtype.WALL));
    }
}
