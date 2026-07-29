package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage this turn, that damage is dealt to that
 * source's controller instead." (Reflect Damage). The source is chosen on resolution (any battlefield
 * permanent, not a target). One-shot redirection (a replacement effect, not prevention): the next
 * damage event from that source — to a player, planeswalker or creature, combat or noncombat — is
 * dealt to that source's controller instead, then the shield is consumed.
 */
public record ReflectNextDamageFromChosenSourceToItsControllerEffect() implements CardEffect {
}
