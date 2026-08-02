package com.github.laxika.magicalvibes.model.effect;

/**
 * "For each planeswalker you control, you may activate one of its loyalty abilities once this turn as
 * though none of its loyalty abilities have been activated this turn." (The Chain Veil)
 *
 * <p>Grants each planeswalker the controller controls one extra loyalty activation for the rest of
 * the turn, on top of the normal allowance. Stacks with itself and with
 * {@link AllowExtraLoyaltyActivationEffect}.</p>
 */
public record GrantExtraLoyaltyActivationToPlaneswalkersEffect() implements CardEffect {
}
