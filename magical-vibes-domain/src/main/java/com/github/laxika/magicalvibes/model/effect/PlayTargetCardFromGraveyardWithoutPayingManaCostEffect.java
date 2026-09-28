package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may play target [filter] card from your graveyard without paying its mana cost."
 * Targets a single card matching {@code filter} in the controller's own graveyard. On resolution
 * the controller may play it for free (a land is put onto the battlefield; any other card is cast
 * without paying its mana cost). {@code exileInsteadOfGraveyard} replaces the resulting spell's
 * graveyard destination. Used by Horde of Notions (Elemental cards) and Victor Timely, Wily Tycoon.
 */
public record PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(
        CardPredicate filter,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    public PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(CardPredicate filter) {
        this(filter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
