package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Set;

/**
 * Static effect: noncreature spells can't be cast from any of the listed zones.
 * Land plays are unaffected because lands aren't spells, and creature spells remain castable.
 */
public record NoncreatureSpellsCantBeCastFromZonesEffect(Set<Zone> zones) implements CardEffect {
}
