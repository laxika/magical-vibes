package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Source fights a target creature controlled by a player selected earlier in the resolution.
 * The optional excluded permanent is the "another" part of the target restriction.
 */
public record SourceFightsTargetCreatureControlledByPlayerEffect(
        UUID playerId, UUID excludedPermanentId) implements CardEffect {

    public SourceFightsTargetCreatureControlledByPlayerEffect(UUID playerId) {
        this(playerId, null);
    }

    @Override
    public TargetSpec targetSpec() {
        var controlledByPlayer = new PermanentControlledByPlayerPredicate(playerId);
        var predicate = excludedPermanentId == null
                ? controlledByPlayer
                : new PermanentAllOfPredicate(List.of(
                        controlledByPlayer,
                        new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(excludedPermanentId))));
        return TargetSpec.harmful(TargetPredicates.creature(), predicate);
    }
}
