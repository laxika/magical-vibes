package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may play target [filter] card from your graveyard without paying its mana cost."
 * Targets a single card matching {@code filter} in the controller's own graveyard. On resolution
 * the controller may play it for free (a land is put onto the battlefield; any other card is cast
 * without paying its mana cost). Used by Horde of Notions (Elemental cards).
 */
public record PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(CardPredicate filter) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
    @Override public boolean targetsControllersGraveyardOnly() { return true; }
}
