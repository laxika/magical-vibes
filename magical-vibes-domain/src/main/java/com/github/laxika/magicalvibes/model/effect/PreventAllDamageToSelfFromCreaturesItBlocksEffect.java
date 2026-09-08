package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevent all damage that would be dealt to this creature by creatures it blocks.
 *
 * <p>The source permanent's blocking assignments are checked when the damage event occurs, so
 * the effect covers both combat and noncombat damage from those creatures.</p>
 */
public record PreventAllDamageToSelfFromCreaturesItBlocksEffect() implements CardEffect {
}
