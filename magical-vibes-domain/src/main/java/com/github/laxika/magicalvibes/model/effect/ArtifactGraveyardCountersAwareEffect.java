package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Capability for an artifact-graveyard trigger effect that needs the counters the artifact had
 * before it left the battlefield.
 */
public interface ArtifactGraveyardCountersAwareEffect {

    /** Returns the effect with the leaving artifact's counter snapshot bound in. */
    CardEffect boundToArtifactGraveyardCounters(Map<CounterType, Integer> counters);
}
