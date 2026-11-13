package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creature attached to the source Equipment a copy of a creature card exiled with it.
 *
 * <p>As the effect of a "whenever this Equipment becomes attached" triggered ability it uses the stack (Assimilation
 * Aegis); wrapped in {@link AsThisBecomesAttachedEffect} it is the "as this Equipment becomes attached to a creature"
 * static ability, applied during the attachment (Dinosaur Headdress).
 */
public record AttachedCreatureBecomesCopyOfExiledCreatureEffect() implements CardEffect {
}
