package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents every player other than the resolving effect's controller from playing cards from
 * their graveyards until end of turn.
 */
public record OtherPlayersCantPlayFromGraveyardsThisTurnEffect() implements CardEffect {
}
