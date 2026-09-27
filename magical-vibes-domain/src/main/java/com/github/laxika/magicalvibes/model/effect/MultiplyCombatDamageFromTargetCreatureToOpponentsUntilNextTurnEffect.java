package com.github.laxika.magicalvibes.model.effect;

/**
 * Until the controller's next turn, the targeted creature deals the configured multiple of its
 * combat damage to that player's opponents.
 */
public record MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect(int multiplier)
        implements CardEffect {

    public MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Damage multiplier must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
