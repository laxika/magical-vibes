package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Creates a tapped and attacking token copy of the triggering attacking creature for one other
 * opponent. A null opponent is the authored planar trigger; a non-null opponent is one accepted
 * resolution-time choice from that trigger.
 */
public record CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
        UUID opponentId,
        boolean exileAtEndOfCombat)
        implements CardEffect {

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect() {
        this(null, false);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(UUID opponentId) {
        this(opponentId, false);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(boolean exileAtEndOfCombat) {
        this(null, exileAtEndOfCombat);
    }
}
