package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage this turn, that damage is dealt to this
 * creature instead." The source is chosen on resolution (not a target) and the redirected damage is
 * dealt to the source permanent of this ability. Redirection (replacement), not prevention, so it
 * applies even when damage can't be prevented. Used by Opal-Eye, Konda's Yojimbo.
 */
public record RedirectNextDamageFromChosenSourceToSelfEffect() implements CardEffect {
}
