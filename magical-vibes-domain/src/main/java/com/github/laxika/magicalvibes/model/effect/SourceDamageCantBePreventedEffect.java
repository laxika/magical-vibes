package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: damage that would be dealt by <em>this</em> permanent can't be prevented
 * (e.g. Malignus). The source-scoped sibling of {@link DamageCantBePreventedEffect}, which turns
 * prevention off for every source in the game.
 *
 * <p>Honoured on the paths where the damage source permanent is known: combat damage (to players,
 * planeswalkers and creatures, including the protection-based prevention of combat damage) and
 * permanent-sourced noncombat damage to creatures (fight/bite). Prevention shields, Circles of
 * Protection and protection's damage-prevention are all bypassed for that damage.
 */
public record SourceDamageCantBePreventedEffect() implements CardEffect {
}
