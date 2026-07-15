package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Search target opponent's library for a card matching {@code filter} and put that card onto the
 * battlefield under the searching player's control, then that player shuffles (e.g. Bribery searches
 * for a creature card). Targets a player.
 */
public record SearchTargetLibraryForCardToBattlefieldUnderControlEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
