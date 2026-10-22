package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot: "You may activate the loyalty abilities of planeswalkers you control twice this turn rather
 * than only once" (Urza Assembles the Titans chapter III). Applies to every planeswalker the controller
 * controls, including ones that come under their control later in the turn. Overlapping grants do not
 * stack; the cap stays at two activations per planeswalker.
 */
public record AllowTwiceLoyaltyActivationsThisTurnEffect() implements CardEffect {
}
