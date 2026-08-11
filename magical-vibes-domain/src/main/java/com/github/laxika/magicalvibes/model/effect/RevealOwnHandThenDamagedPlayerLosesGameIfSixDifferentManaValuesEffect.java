package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the resolving ability's controller's hand, then makes the damaged player lose the game
 * if that hand contains cards with at least six different mana values. The effect can be used on
 * the all-damage-to-player trigger slot, which covers both combat and noncombat damage.
 */
public record RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
