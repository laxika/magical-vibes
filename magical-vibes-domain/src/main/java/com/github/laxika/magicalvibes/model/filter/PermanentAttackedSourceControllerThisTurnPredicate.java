package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures that were declared as attackers against the source's controller this turn
 * ("target creature that attacked you this turn", Jabari's Influence).
 *
 * <p>Evaluated against {@code GameData.playersAttackedThisTurn}, keyed by the candidate permanent,
 * checking whether the source controller appears among the players it attacked. A creature that
 * attacked only a planeswalker the source controller controls does not match — it attacked the
 * planeswalker, not the player.
 */
public record PermanentAttackedSourceControllerThisTurnPredicate() implements PermanentPredicate {
}
