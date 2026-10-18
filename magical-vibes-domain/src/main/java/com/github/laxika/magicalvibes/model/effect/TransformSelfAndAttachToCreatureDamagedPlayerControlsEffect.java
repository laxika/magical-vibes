package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * "Transform this permanent. If you do, attach it to target creature that player controls." Used
 * inside a {@link MayEffect} wrapper for combat damage triggers where "that player" is the
 * damaged player. The damaged player is bound before the creature target is chosen.
 */
public record TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect()
        implements DamagedPlayerControlsTargetEffect {

    @Override
    public PermanentPredicate predicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public TransformSelfAndAttachToTargetCreatureEffect forDamagedPlayer(UUID playerId) {
        return new TransformSelfAndAttachToTargetCreatureEffect(new PermanentAllOfPredicate(List.of(
                predicate(), new PermanentControlledByPlayerPredicate(playerId))));
    }
}
