package com.github.laxika.magicalvibes.model.effect;

/**
 * Natural Balance: each player who controls six or more lands chooses five lands they control and
 * sacrifices the rest, and each player who controls four or fewer lands may search their library
 * for up to X basic land cards and put them onto the battlefield, where X is five minus the number
 * of lands they control; each player who searched then shuffles.
 *
 * <p>Both player sets are computed from the land counts as the spell begins resolving, so they are
 * disjoint and neither half can change the other's count. The engine therefore runs the (optional,
 * interactive) searches first and the forced sacrifices afterwards — the resulting game state is
 * the same, and it lets both interactive queues chain through the library-search follow-up.
 */
public record NaturalBalanceEffect() implements CardEffect {
}
