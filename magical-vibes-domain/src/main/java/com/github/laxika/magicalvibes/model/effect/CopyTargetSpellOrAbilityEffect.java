package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Copies a target spell or ability on the stack and offers its controller new targets for the copy.
 */
public record CopyTargetSpellOrAbilityEffect(StackEntryPredicate targetPredicate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spells(targetPredicate));
    }
}
