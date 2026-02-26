package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "Whenever you cast a [matching] spell, you may pay {X}.
 * If you do, create a creature token."
 * <p>
 * Used in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into the embedded {@link CreateCreatureTokenEffect}.
 * Only triggers for spells cast by the permanent's controller.
 */
public record CreateTokenOnOwnSpellCastWithCostEffect(
        CardPredicate spellFilter,
        int manaCost,
        CreateCreatureTokenEffect tokenEffect
) implements CardEffect {
}
