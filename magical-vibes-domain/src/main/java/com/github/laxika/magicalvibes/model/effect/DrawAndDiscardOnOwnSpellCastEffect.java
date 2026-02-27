package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "Whenever you cast a [matching] spell, you may draw a card.
 * If you do, discard a card."
 * <p>
 * Used in the {@code ON_CONTROLLER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link DrawCardEffect} + {@link DiscardCardEffect}.
 * Only triggers for spells cast by the permanent's controller that match the filter.
 */
public record DrawAndDiscardOnOwnSpellCastEffect(CardPredicate spellFilter) implements CardEffect {
}
