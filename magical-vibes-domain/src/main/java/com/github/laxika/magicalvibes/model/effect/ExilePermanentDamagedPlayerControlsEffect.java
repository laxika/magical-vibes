package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;

import java.util.List;
import java.util.UUID;

/**
 * "Exile target permanent that player controls" where "that player" is the damaged player.
 * Used inside a MayEffect wrapper for combat damage triggers.
 * Context: StackEntry.targetId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 *
 * @param predicate optional filter to restrict valid targets (e.g. black or red permanents)
 */
public record ExilePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate)
        implements DamagedPlayerControlsTargetEffect {

    @Override
    public ExileTargetPermanentEffect forDamagedPlayer(UUID playerId) {
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(playerId);
        return new ExileTargetPermanentEffect(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller)));
    }
}
