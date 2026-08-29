package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static restriction that limits when a player may cast spells based on the source's controller.
 */
public interface SpellCastingTimingRestrictionEffect extends CardEffect {

    boolean appliesTo(UUID sourceControllerId, UUID castingPlayerId);
}
