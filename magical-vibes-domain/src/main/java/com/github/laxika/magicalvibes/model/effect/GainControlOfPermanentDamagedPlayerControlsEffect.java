package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * "Gain control of target permanent that player controls", where "that player" is the player
 * dealt damage by the source creature. The damaged player's identity is bound before choosing the
 * permanent target.
 */
public record GainControlOfPermanentDamagedPlayerControlsEffect(PermanentPredicate predicate)
        implements DamagedPlayerControlsTargetEffect, ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), predicate);
    }

    @Override
    public GainControlOfTargetEffect forDamagedPlayer(UUID playerId) {
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(playerId);
        PermanentPredicate targetPredicate = predicate == null
                ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller));
        return GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT, targetPredicate);
    }
}
