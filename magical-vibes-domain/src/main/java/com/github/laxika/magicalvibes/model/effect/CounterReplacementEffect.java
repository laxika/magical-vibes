package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/** Replacement behavior for counters put on permanents or players. */
public interface CounterReplacementEffect extends CardEffect {

    int replace(CounterType counterType, int count);

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return true;
    }

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                              boolean affectedPermanentIsArtifact) {
        return appliesTo(counterType, affectedPermanentIsCreature);
    }

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                              boolean affectedPermanentIsArtifact, Permanent sourcePermanent,
                              Permanent affectedPermanent) {
        return appliesTo(counterType, affectedPermanentIsCreature, affectedPermanentIsArtifact);
    }

    default boolean appliesToWhenEntering(CounterType counterType,
                                          boolean enteringPermanentIsCreature,
                                          boolean enteringPermanentIsArtifact,
                                          Permanent enteringPermanent) {
        return false;
    }

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                              boolean sourceControlsAffectedPermanent,
                              boolean sourceControllerIsPlacingPlayer,
                              boolean affectedObjectIsPlayer) {
        return sourceControlsAffectedPermanent
                && appliesTo(counterType, affectedPermanentIsCreature, false);
    }
}
