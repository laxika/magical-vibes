package com.github.laxika.magicalvibes.model.effect;

/**
 * The effect's controller looks at the target player's hand.
 *
 * <p>As an {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger it needs the damaged player bound as the
 * stack entry's target ("look at that player's hand" — Walker of Secret Ways).
 */
public record LookAtHandEffect() implements CombatDamageTriggerContextEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
