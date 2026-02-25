package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "Whenever you cast a [matching] spell, you may pay {X}.
 * If you do, you gain N life."
 * <p>
 * Used in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link GainLifeEffect}.
 * Only triggers for spells cast by the permanent's controller.
 */
public record GainLifeOnOwnSpellCastWithCostEffect(CardPredicate spellFilter, int manaCost, int amount) implements CardEffect {
}
