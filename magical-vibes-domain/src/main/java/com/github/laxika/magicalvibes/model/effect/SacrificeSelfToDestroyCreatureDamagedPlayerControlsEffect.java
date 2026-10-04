package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;

/**
 * "Sacrifice this creature. If you do, destroy target creature that player controls."
 * Used inside a MayEffect wrapper for combat damage / unblocked-attack triggers where "that player"
 * is the damaged / defending player.
 * Context: StackEntry.targetId = affected player ID, StackEntry.sourcePermanentId = source creature ID.
 * {@code cannotBeRegenerated} = the destroyed creature can't be regenerated (e.g. Necrite).
 */
public record SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(
        boolean cannotBeRegenerated,
        List<UUID> eligibleTargetIds) implements DamagedPlayerControlsTargetEffect {

    public SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect {
        eligibleTargetIds = List.copyOf(eligibleTargetIds);
    }

    public SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect() {
        this(false, List.of());
    }

    public SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, List.of());
    }

    @Override
    public PermanentPredicate predicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public CardEffect forDamagedPlayer(UUID playerId) {
        return new SacrificeSelfThenEffect(new DestroyTargetPermanentEffect(cannotBeRegenerated,
                null, -1, new PermanentAllOfPredicate(List.of(predicate(),
                new PermanentControlledByPlayerPredicate(playerId)))));
    }
}
