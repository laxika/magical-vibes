package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks the target creature so that if it would die this turn, it is exiled instead
 * (sets the permanent's {@code exileInsteadOfDieThisTurn} flag, cleared at end of turn).
 * Place before a damage effect on the same target so lethal damage triggers the replacement.
 * Used by Wilt in the Heat.
 */
public record MarkTargetCreatureExileInsteadOfDieThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
