package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to any target this turn, prevent that
 * damage." (Sanctum Guardian). One-shot: the chosen source's very next damage event — whether to a
 * player, planeswalker, or creature, combat or noncombat — is prevented, then the shield is consumed.
 *
 * <p>Unlike {@link PreventNextDamageFromChosenSourceEffect} (Reverse Damage) and the
 * Circle of Protection cycle, which only protect the controller, this protects <em>any</em> target.
 */
public record PreventNextDamageFromChosenSourceToAnyTargetEffect() implements CardEffect {
}
