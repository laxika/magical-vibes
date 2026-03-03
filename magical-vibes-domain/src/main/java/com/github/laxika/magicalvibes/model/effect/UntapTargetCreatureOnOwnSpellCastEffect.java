package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "Whenever you cast a [matching] spell, you may untap target creature."
 * <p>
 * Used in the {@code ON_CONTROLLER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link UntapTargetPermanentEffect}.
 * Only triggers for spells cast by the permanent's controller that match the filter.
 */
public record UntapTargetCreatureOnOwnSpellCastEffect(CardPredicate spellFilter) implements CardEffect {
}
