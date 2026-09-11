package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the player dealt damage by the source lose the game when the source has dealt at least the
 * configured amount of damage to that player this turn.
 *
 * <p>The damage threshold is checked while the trigger is collected. The effect is then placed on
 * the stack with the damaged player and source permanent bound to its context.</p>
 */
public record LoseGameIfSourceDealtDamageToPlayerThisTurnEffect(int minimumDamage)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
