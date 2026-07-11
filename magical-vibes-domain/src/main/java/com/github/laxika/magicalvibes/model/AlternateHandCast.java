package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;

/**
 * An alternate casting cost from hand that replaces the normal mana cost
 * (e.g. Demon of Death's Gate: pay 6 life and sacrifice 3 black creatures).
 *
 * <p>When {@code prowlDamageSubtypes} is non-empty this option models the prowl keyword: the
 * alternate cost may only be used if the caster dealt combat damage to a player this turn with a
 * creature of any of those subtypes (CR 702.75, e.g. Knowledge Exploitation's "Prowl {3}{U}" with
 * Rogue, or Latchkey Faerie's "Prowl {2}{U}" with Faerie or Rogue).
 */
public record AlternateHandCast(List<CastingCost> costs, Set<CardSubtype> prowlDamageSubtypes) implements CastingOption {

    public AlternateHandCast(List<CastingCost> costs) {
        this(costs, Set.of());
    }

    /** Convenience constructor for prowl with a single qualifying creature subtype. */
    public AlternateHandCast(List<CastingCost> costs, CardSubtype prowlDamageSubtype) {
        this(costs, Set.of(prowlDamageSubtype));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }
}
