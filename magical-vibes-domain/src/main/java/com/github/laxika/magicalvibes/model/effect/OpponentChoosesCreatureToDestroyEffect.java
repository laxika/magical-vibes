package com.github.laxika.magicalvibes.model.effect;

/**
 * "Destroy target creature of an opponent's choice." An opponent of the ability's controller
 * chooses any creature on the battlefield and it is destroyed (regeneration/indestructible apply).
 * With 0 creatures nothing happens; with exactly 1 it is destroyed automatically; with 2+ the
 * opponent picks which to destroy. Used as the second half of Diaochan, Artful Beauty (paired after
 * a {@link DestroyTargetPermanentEffect} for the controller's own choice).
 */
public record OpponentChoosesCreatureToDestroyEffect() implements CardEffect {
}
