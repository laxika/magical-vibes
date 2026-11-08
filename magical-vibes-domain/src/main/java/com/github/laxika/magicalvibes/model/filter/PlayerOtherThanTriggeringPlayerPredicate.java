package com.github.laxika.magicalvibes.model.filter;

/**
 * A player other than the one the triggered ability's event was about ("choose another target player" of "whenever a
 * player casts …": The Lord of Pain): read from the target choice's triggering player as the ability goes on the stack
 * and from the stack entry's ({@code StackEntry.getTriggeringPlayerId()}) as it resolves. Matches no player when no
 * triggering player is known.
 */
public record PlayerOtherThanTriggeringPlayerPredicate() implements PlayerPredicate {
}
