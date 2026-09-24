package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

/** Gains control of a target spell while it remains on the stack. */
public record GainControlOfTargetSpellEffect(boolean noncreatureOnly) implements CardEffect {

    public GainControlOfTargetSpellEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        Set<StackEntryType> spellTypes = noncreatureOnly
                ? Set.of(
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL)
                : Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL);
        return TargetSpec.benign(TargetPredicates.spells(new StackEntryTypeInPredicate(spellTypes)));
    }
}
