package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who controls the most lands, a player tied for the most included: no other player controls more
 * lands than they do (Tectonic Hellion, "each player who controls the most lands").
 */
public record PlayerControlsMostLandsPredicate() implements PlayerPredicate {
}
