package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import java.util.List;
import java.util.UUID;

/**
 * "Destroy target permanent that player controls" as Dokuchi Silencer's reflexive "when you do" ability, where "that
 * player" is the player its combat damage trigger was about; {@link #forDamagedPlayer(UUID)} creates the targeted
 * effect the reflexive ability goes on the stack with. A triggered ability's own "destroy target permanent that player
 * controls" is {@link DestroyTargetPermanentEffect} with a
 * {@code PermanentControlledByTriggeringPlayerPredicate}.
 *
 * @param predicate filter restricting valid targets (e.g. creatures or planeswalkers)
 */
public record DestroyPermanentDamagedPlayerControlsEffect(PermanentPredicate predicate)
        implements DamagedPlayerControlsTargetEffect {

    /** Binds the damaged player before the reflexive ability's target is chosen. */
    public DestroyTargetPermanentEffect forDamagedPlayer(UUID playerId) {
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(playerId);
        return new DestroyTargetPermanentEffect(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller)));
    }
}
