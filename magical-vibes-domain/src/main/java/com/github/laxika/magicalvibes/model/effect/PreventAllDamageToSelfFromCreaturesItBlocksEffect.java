package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevents all damage that would be dealt to this creature by creatures it blocks.
 * The marker is evaluated against the current blocking assignments by the damage services.
 */
public record PreventAllDamageToSelfFromCreaturesItBlocksEffect() implements CardEffect {
}
