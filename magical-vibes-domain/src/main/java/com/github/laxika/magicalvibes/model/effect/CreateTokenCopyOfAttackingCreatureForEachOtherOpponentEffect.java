package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates a tapped and attacking token copy of the triggering attacker for each other opponent. */
public record CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
        UUID opponentId, boolean mayCreate, boolean removeLegendary, boolean exileAtEndStep,
        boolean exileAtEndOfCombat)
        implements CardEffect {

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect() {
        this(null, true, false, true, false);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(UUID opponentId) {
        this(opponentId, true, false, true, false);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(boolean exileAtEndOfCombat) {
        this(null, true, false, !exileAtEndOfCombat, exileAtEndOfCombat);
    }

    public CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
            UUID opponentId, boolean mayCreate, boolean removeLegendary, boolean exileAtEndStep) {
        this(opponentId, mayCreate, removeLegendary, exileAtEndStep, false);
    }

    /** Creates the optional Myriad variant, which exiles its copies at end of combat. */
    public static CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect myriad() {
        return new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
                null, true, false, false, true);
    }

    /** Creates the mandatory, nonlegendary variant used by Shredder, Shadow Master. */
    public static CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect mandatoryNonLegendary() {
        return new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(
                null, false, true, false, false);
    }
}
