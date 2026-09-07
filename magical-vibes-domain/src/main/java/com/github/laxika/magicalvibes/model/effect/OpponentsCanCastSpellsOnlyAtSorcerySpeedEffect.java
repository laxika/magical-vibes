package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static effect: opponents of this permanent's controller can cast spells only when they could
 * cast a sorcery (Teferi, Time Raveler).
 */
public record OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect()
        implements SpellCastingTimingRestrictionEffect {

    @Override
    public boolean appliesTo(UUID sourceControllerId, UUID castingPlayerId) {
        return !sourceControllerId.equals(castingPlayerId);
    }
}
