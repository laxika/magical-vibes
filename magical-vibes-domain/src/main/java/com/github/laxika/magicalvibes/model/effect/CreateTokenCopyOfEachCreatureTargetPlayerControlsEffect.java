package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token copy of each creature controlled by the targeted player.
 * The target player is read from the stack entry's target id, and all copies are created under the
 * resolving effect's controller.
 */
public record CreateTokenCopyOfEachCreatureTargetPlayerControlsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
