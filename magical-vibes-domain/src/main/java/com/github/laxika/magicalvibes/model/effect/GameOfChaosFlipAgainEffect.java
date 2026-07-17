package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Continuation state for Game of Chaos's "…decide whether to flip again" loop. Carried inside a
 * {@code PendingMayAbility} offered to the deciding player; on accept the next coin flip is
 * performed at the (already doubled) life {@code stake}. This is not a targeted or stack-resolved
 * effect — it exists solely to route the may-ability answer to the Game of Chaos flip handler.
 */
public record GameOfChaosFlipAgainEffect(UUID spellControllerId, UUID opponentId, int stake)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
