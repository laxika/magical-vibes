package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates a tapped and attacking token copy of the triggering attacker for each other opponent. */
public record CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
        UUID opponentId, boolean mayCreate, boolean removeLegendary, boolean exileAtEndStep)
        implements CardEffect {

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect() {
        this(null, true, false, true);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(UUID opponentId) {
        this(opponentId, true, false, true);
    }

    /** Creates the mandatory, nonlegendary variant used by Shredder, Shadow Master. */
    public static CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect mandatoryNonLegendary() {
        return new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(null, false, true, false);
    }
}
