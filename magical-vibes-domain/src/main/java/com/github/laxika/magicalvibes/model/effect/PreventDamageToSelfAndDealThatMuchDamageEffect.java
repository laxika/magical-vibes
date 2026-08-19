package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevent damage that would be dealt to this creature, then queue a reflexive
 * ability for it to deal that much damage to another target (Phyrexian Vindicator).
 */
public record PreventDamageToSelfAndDealThatMuchDamageEffect() implements CardEffect {
}
