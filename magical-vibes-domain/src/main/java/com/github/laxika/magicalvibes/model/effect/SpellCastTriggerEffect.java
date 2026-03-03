package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Generic trigger descriptor for "whenever [someone] casts a spell [matching filter]" abilities.
 * <p>
 * Works in both {@code ON_ANY_PLAYER_CASTS_SPELL} and {@code ON_CONTROLLER_CASTS_SPELL} slots —
 * the EffectSlot determines which loop fires it.
 * <p>
 * When wrapped in {@link MayEffect}, the player is prompted before the effects resolve.
 * If {@code manaCost} is non-null, a "may pay" prompt is shown and the cost must be paid.
 *
 * @param spellFilter     what spells trigger this (null = any spell)
 * @param resolvedEffects effects to put on the stack when this triggers
 * @param manaCost        optional mana cost string, e.g. "{1}" (null = free)
 */
public record SpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        String manaCost
) implements CardEffect {

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null);
    }
}
