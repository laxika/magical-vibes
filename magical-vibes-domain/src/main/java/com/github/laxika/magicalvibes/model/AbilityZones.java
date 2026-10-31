package com.github.laxika.magicalvibes.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * The zones an ability functions from. Abilities of an instant or sorcery spell usually function
 * only on the stack, and abilities of every other object only on the battlefield (CR 113.6). An
 * ability that states its zones functions only from them (CR 113.6b), an ability whose cost or
 * effect moves its object out of a zone functions only in that zone (CR 113.6m), and a
 * characteristic-defining ability functions everywhere (CR 113.6a).
 *
 * @param zones at least one zone, held as an unmodifiable copy
 */
public record AbilityZones(Set<Zone> zones) {

    public static final AbilityZones BATTLEFIELD = of(Zone.BATTLEFIELD);
    public static final AbilityZones GRAVEYARD = of(Zone.GRAVEYARD);
    public static final AbilityZones HAND = of(Zone.HAND);
    public static final AbilityZones STACK = of(Zone.STACK);
    public static final AbilityZones EXILE = of(Zone.EXILE);
    public static final AbilityZones COMMAND = of(Zone.COMMAND);

    public AbilityZones {
        Objects.requireNonNull(zones, "zones");
        if (zones.isEmpty()) {
            throw new IllegalArgumentException("An ability functions from at least one zone");
        }
        zones = Collections.unmodifiableSet(EnumSet.copyOf(zones));
    }

    public static AbilityZones of(Zone first, Zone... rest) {
        return new AbilityZones(EnumSet.of(first, rest));
    }

    /** Whether the ability functions while its object is in {@code zone}. */
    public boolean functionsIn(Zone zone) {
        return zones.contains(zone);
    }
}
