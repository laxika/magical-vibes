package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creature attached to the source Equipment a copy of a creature card exiled with it.
 *
 * <p>{@code asAttaches} marks the "As this Equipment becomes attached to a creature" wording (Dinosaur
 * Headdress), a static-style effect applied during the attachment; otherwise this is the "Whenever this
 * Equipment becomes attached" triggered ability (Assimilation Aegis) that uses the stack.
 */
public record AttachedCreatureBecomesCopyOfExiledCreatureEffect(boolean asAttaches) implements CardEffect {

    public AttachedCreatureBecomesCopyOfExiledCreatureEffect() {
        this(false);
    }
}
