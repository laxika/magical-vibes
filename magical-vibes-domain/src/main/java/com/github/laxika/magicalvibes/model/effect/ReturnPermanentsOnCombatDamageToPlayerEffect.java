package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever this deals combat damage to a player, return up to that many target permanents that player controls to
 * their owners' hands" (Cephalid Constable): the targets are chosen as the ability is put on the stack, as many as the
 * damage it recorded (CR 603.3d), and return together.
 */
public record ReturnPermanentsOnCombatDamageToPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
