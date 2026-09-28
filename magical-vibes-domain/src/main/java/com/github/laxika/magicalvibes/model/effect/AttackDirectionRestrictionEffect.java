package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability exposed by a temporary effect that restricts combat attack targets by table
 * direction. Combat legality reads this capability without depending on a concrete effect class.
 */
public interface AttackDirectionRestrictionEffect extends CardEffect {

    RestrictAttacksToDirectionUntilNextTurnEffect.Direction direction();
}
