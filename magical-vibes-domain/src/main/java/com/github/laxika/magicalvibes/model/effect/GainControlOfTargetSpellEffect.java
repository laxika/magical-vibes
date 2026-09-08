package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

/** Gains control of a target noncreature spell while it remains on the stack. */
public record GainControlOfTargetSpellEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spells(new StackEntryNotPredicate(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)))));
    }
}
