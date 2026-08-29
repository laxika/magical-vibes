package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each creature the damaged player controls (stored in targetId on the stack
 * entry). With no fixed amount, the damage equals the combat damage dealt, stored in xValue.
 * Used by Balefire Dragon and Shockmaw Dragon.
 */
public record DealDamageToEachCreatureDamagedPlayerControlsEffect(Integer fixedDamage)
        implements CombatDamageTriggerContextEffect {

    public DealDamageToEachCreatureDamagedPlayerControlsEffect() {
        this(null);
    }

    public DealDamageToEachCreatureDamagedPlayerControlsEffect(int fixedDamage) {
        this(Integer.valueOf(fixedDamage));
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT;
    }
}
