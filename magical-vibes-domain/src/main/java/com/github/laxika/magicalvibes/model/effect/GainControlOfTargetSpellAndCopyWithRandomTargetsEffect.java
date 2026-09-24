package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Gains control of a target spell, copies it, and randomly reselects every target occurrence on
 * both stack objects from targets legal for the now-controlled spell, excluding the effect
 * controller and permanents that player controls.
 */
public record GainControlOfTargetSpellAndCopyWithRandomTargetsEffect(StackEntryPredicate spellFilter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spells(spellFilter));
    }
}
