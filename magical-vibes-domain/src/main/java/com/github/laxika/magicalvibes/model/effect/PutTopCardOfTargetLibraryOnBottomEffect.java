package com.github.laxika.magicalvibes.model.effect;

/**
 * Put the top card of the stack entry's {@code targetId} player's library on the bottom of that
 * library. Non-targeting on its own — it is pushed as the body of the may-ability created by
 * {@link LookAtTopCardsOfTargetLibraryEffect} with
 * {@link TargetLibraryAction#MAY_PUT_TOP_ON_BOTTOM} (Coral Fighters).
 */
public record PutTopCardOfTargetLibraryOnBottomEffect() implements CardEffect {

    /**
     * A player spec so the may-ability accept path carries the pre-chosen player through to the
     * stack entry's {@code targetId} (same reason {@code ShuffleLibraryEffect} declares one).
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
