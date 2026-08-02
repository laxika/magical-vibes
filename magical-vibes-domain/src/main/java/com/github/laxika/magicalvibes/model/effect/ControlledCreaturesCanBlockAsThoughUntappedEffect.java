package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: tapped creatures you control can block as though they were untapped.
 */
public record ControlledCreaturesCanBlockAsThoughUntappedEffect() implements TappedBlockPermissionEffect {

    @Override
    public PermanentPredicate tappedBlockMatcher() {
        return new PermanentControlledBySourceControllerPredicate();
    }
}
