package com.github.laxika.magicalvibes.model.amount;

/**
 * The wrapped amount during the source controller's turn; {@code 0} on every other turn.
 * Models "during your turn, [count]" characteristic-defining wordings (Angry Mob: "2 plus
 * the number of Swamps your opponents control" on your turn, a flat 2 otherwise — expressed
 * as {@code Sum(Fixed(2), DuringControllerTurn(...))}).
 */
public record DuringControllerTurn(DynamicAmount amount) implements DynamicAmount {
}
