package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * Creates tokens under the targeted player's control (not the controller's).
 * Used by cards like Dowsing Dagger that say "target opponent creates [tokens]".
 * When used as a combat-damage trigger, the damaged player is automatically used as the target.
 */
public record CreateTokenForTargetPlayerEffect(CreateTokenEffect tokenEffect,
                                               PlayerRelation targetPlayerRelation) implements CombatDamageTriggerContextEffect {

    public CreateTokenForTargetPlayerEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, PlayerRelation.ANY);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetPlayerRelation == PlayerRelation.ANY
                ? TargetPredicates.player()
                : TargetPredicates.players(new PlayerRelationPredicate(targetPlayerRelation)));
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
