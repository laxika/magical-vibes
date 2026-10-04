package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/** Damage-trigger targeting context, bound to ordinary targeted bounce before going on the stack. */
public record ReturnPermanentDamagedPlayerControlsToHandEffect(PermanentPredicate predicate)
        implements DamagedPlayerControlsTargetEffect {

    @Override
    public ReturnToHandEffect forDamagedPlayer(UUID playerId) {
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(playerId);
        return ReturnToHandEffect.target(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller)));
    }
}
