package com.github.laxika.magicalvibes.model.effect;

/**
 * Bounty of the Luxa's beginning-of-first-main-phase trigger, as one atomic effect (trigger
 * collectors push one stack entry per slot effect, so the counter toggle and its two mutually
 * exclusive outcomes must live together): remove all flood counters from the source enchantment;
 * if none were removed, put a flood counter on it and draw a card; otherwise add {C}{G}{U}.
 *
 * <p>Non-targeting. Not a {@link ManaProducingEffect}: the ability triggers from the beginning of a
 * step rather than from a mana source, so it uses the stack (CR 605.1b) and is not a mana ability.
 */
public record BountyOfTheLuxaEffect() implements CardEffect {
}
