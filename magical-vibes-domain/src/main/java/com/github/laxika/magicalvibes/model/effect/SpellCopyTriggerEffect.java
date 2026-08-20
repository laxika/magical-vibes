package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Trigger descriptor for "whenever you or an opponent copy an instant or sorcery spell" abilities.
 *
 * @param spellFilter    what copied spells trigger this (null = any copied spell)
 * @param resolvedEffects effects to put on the stack when this triggers
 * @param targetFilter   optional filter for the target chosen as the trigger is put on the stack
 */
public record SpellCopyTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        TargetFilter targetFilter
) implements CardEffect {

    public SpellCopyTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null);
    }
}
