package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * A combat-damage trigger whose target is chosen by the damaged player from permanents controlled
 * by an opponent of the ability's controller.
 */
public record DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect(PermanentPredicate predicate)
        implements DamagedPlayerControlsTargetEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate());
    }

    @Override
    public UUID targetChooserId(UUID damagedPlayerId, UUID sourceControllerId) {
        return damagedPlayerId;
    }

    @Override
    public DestroyTargetPermanentEffect forDamagedPlayer(UUID playerId) {
        return new DestroyTargetPermanentEffect(targetPredicate());
    }

    private PermanentPredicate targetPredicate() {
        PermanentPredicate opponentControls = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        return new PermanentAllOfPredicate(List.of(predicate, opponentControls));
    }
}
