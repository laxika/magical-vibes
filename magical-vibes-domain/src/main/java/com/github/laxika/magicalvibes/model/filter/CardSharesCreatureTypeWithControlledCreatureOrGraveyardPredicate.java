package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares a creature type with a creature controlled by the perspective player
 * or with a creature card in that player's graveyard.
 *
 * <p>This predicate requires game state and is intended for state-aware spell-cast filters.
 * Changeling and effective creature types in the relevant zones are honored.</p>
 */
public record CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate() implements CardPredicate {
}
