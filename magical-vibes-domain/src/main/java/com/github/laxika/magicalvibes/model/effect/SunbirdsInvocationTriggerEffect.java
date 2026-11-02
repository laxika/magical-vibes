package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker trigger effect for Sunbird's Invocation's Triggers.youCastSpell() trigger.
 * "Whenever you cast a spell from your hand, reveal the top X cards of your library,
 * where X is that spell's mana value. You may cast a spell with mana value X or less
 * from among cards revealed this way without paying its mana cost. Put the rest on the
 * bottom of your library in a random order."
 */
public record SunbirdsInvocationTriggerEffect() implements CardEffect {
}
