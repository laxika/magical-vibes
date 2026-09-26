package com.github.laxika.magicalvibes.model.effect;

/** Queues one full beginning phase after the current combat phase. */
public record AdditionalBeginningPhaseEffect(boolean afterPostcombatMain) implements CardEffect {

    public AdditionalBeginningPhaseEffect() {
        this(false);
    }
}
