package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Trigger descriptor for "whenever you or an opponent copy a spell" abilities.
 *
 * @param spellFilter    what copied spells trigger this (null = any copied spell)
 * @param resolvedEffects effects to put on the stack when this triggers
 * @param targetFilter   optional filter for the target chosen as the trigger is put on the stack
 * @param copiedSpellCondition optional condition on the copied spell's stack entry
 * @param allSpellTypes  whether copies of permanent spells also trigger this effect
 */
public record SpellCopyTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        TargetFilter targetFilter,
        StackEntryPredicate copiedSpellCondition,
        boolean allSpellTypes
) implements CardEffect {

    public SpellCopyTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null, null, false);
    }

    public SpellCopyTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  TargetFilter targetFilter) {
        this(spellFilter, resolvedEffects, targetFilter, null, false);
    }

    public SpellCopyTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  StackEntryPredicate copiedSpellCondition, boolean allSpellTypes) {
        this(spellFilter, resolvedEffects, null, copiedSpellCondition, allSpellTypes);
    }
}
