package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target land becomes a Forest until this creature leaves the battlefield." (Gaea's Liege.)
 *
 * <p>On resolution the target land's id is recorded on the source permanent's
 * {@code forestedLandIds}. A companion {@link TrackedLandsBecomeForestEffect} in the source's
 * STATIC slot then continuously makes every recorded land a Forest (a CR 305.7 basic-land-type
 * replacement, layer 4). Because the grant lives on the source permanent, it lasts exactly as long
 * as that permanent is on the battlefield — when the source leaves, the static effect stops applying
 * and the lands revert automatically, with no cleanup pass.
 */
public record TargetLandBecomesForestUntilSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.LAND);
    }
}
