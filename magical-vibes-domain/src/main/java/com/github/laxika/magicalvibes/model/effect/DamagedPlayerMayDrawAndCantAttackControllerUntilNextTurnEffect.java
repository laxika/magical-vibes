package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Offers the damaged player a draw that, if accepted, restricts their attacks until next turn. */
public record DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect(
        UUID damagedPlayerId,
        UUID protectedPlayerId) implements CardEffect, CombatDamageTriggerContextEffect {

    public DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect() {
        this(null, null);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
