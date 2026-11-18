package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creature attached to the source Equipment a copy of a creature card exiled with it.
 *
 * <p>As the effect of a "whenever this Equipment becomes attached" triggered ability it uses the stack (Assimilation
 * Aegis); wrapped in {@link AsThisBecomesAttachedEffect} it is the "as this Equipment becomes attached to a creature"
 * static ability, applied during the attachment (Dinosaur Headdress).
 *
 * @param duration how long the copy lasts: {@code WHILE_REMAINS_ATTACHED} for Assimilation Aegis's "for as long as this
 *                 Equipment remains attached to it", which phasing out ends (CR 702.26f); {@code WHILE_ATTACHED}, the
 *                 default, for the static "equipped creature is a copy of" of Dinosaur Headdress
 */
public record AttachedCreatureBecomesCopyOfExiledCreatureEffect(Duration duration) implements CardEffect {

    public AttachedCreatureBecomesCopyOfExiledCreatureEffect() {
        this(Duration.WHILE_ATTACHED);
    }
}
