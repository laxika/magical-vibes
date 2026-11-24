package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player with the most cards in hand or tied for it: no other player has more ("the player who has the most
 * cards in hand gains control of this creature" after "if a player has more cards in hand than each other player",
 * Sokenzan Renegade). Used with {@code Subjects.matching(eachPlayer(), ...)}; hand sizes are read as the effect applies.
 */
public record PlayerHasMostCardsInHandPredicate() implements PlayerPredicate {
}
