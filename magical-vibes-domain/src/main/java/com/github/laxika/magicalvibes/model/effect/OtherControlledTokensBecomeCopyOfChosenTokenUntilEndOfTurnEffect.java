package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a token they control during resolution; each other token they control
 * becomes a copy of the chosen token until end of turn. The choice is not a target.
 */
public record OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffect() implements CardEffect {
}
