package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** True when exactly one player controls more matching creatures than every other player. */
public record APlayerControlsMoreCreaturesThanEachOtherPlayer(PermanentPredicate creatureFilter)
        implements Condition {

    /** Counts all creatures, preserving the original condition's behavior. */
    public APlayerControlsMoreCreaturesThanEachOtherPlayer() {
        this(new PermanentIsCreaturePredicate());
    }

    @Override
    public String conditionName() {
        return "a player controls more creatures than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player controls more creatures than each other player";
    }
}
