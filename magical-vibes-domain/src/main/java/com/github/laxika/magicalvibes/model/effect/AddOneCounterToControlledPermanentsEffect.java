package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Doc Samson-style replacement for counters put on permanents its controller controls.
 *
 * <p>When {@code onlyWhenControllerPlaces} is true, the replacement applies only to counters placed by the
 * source's controller ("if you would put counters on a permanent you control"), so counters placed by an
 * opponent are not replaced. When false, it applies to counters on any permanent the source's controller
 * controls regardless of who places them ("if one or more counters would be put on a permanent your team
 * controls").
 */
public record AddOneCounterToControlledPermanentsEffect(boolean onlyWhenControllerPlaces)
        implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesGlobally() {
        return onlyWhenControllerPlaces;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean sourceControlsAffectedPermanent,
                             boolean sourceControllerIsPlacingPlayer,
                             boolean affectedObjectIsPlayer) {
        return !affectedObjectIsPlayer && sourceControlsAffectedPermanent && sourceControllerIsPlacingPlayer;
    }
}
