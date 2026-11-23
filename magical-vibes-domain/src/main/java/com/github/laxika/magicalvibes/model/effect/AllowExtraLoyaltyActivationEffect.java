package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "You may activate the loyalty abilities of planeswalkers you control twice each turn
 * rather than only once." (Oath of Teferi-style). Floated on a player by
 * {@link GrantStaticEffectToPlayerEffect#untilEndOfTurn} it is "twice this turn" (Urza Assembles the Titans); grants
 * don't stack, the cap stays at two.
 */
public record AllowExtraLoyaltyActivationEffect() implements CardEffect {
}
