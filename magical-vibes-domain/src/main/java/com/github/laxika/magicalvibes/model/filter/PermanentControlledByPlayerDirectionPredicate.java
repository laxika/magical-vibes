package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PlayerDirection;

/** Matches permanents controlled by the player in the given direction from the source controller. */
public record PermanentControlledByPlayerDirectionPredicate(PlayerDirection direction)
        implements PermanentPredicate {
}
