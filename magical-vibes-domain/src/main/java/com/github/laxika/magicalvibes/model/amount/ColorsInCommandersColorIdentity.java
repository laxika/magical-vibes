package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of colors in the color identity of the controller's commanders ("pay life equal to the number of colors
 * in your commanders' color identity"). Color identity is a Commander notion, set before the game begins (CR 903.4,
 * 903.4a). For a player who has no commander the quality is undefined: the amount is 0 for an effect, and a cost that
 * refers to it can't be paid (CR 903.4f, {@code AmountEvaluationService.isUnpayableCost}).
 */
public record ColorsInCommandersColorIdentity() implements DynamicAmount {
}
