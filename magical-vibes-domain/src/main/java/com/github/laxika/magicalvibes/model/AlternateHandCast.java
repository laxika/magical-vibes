package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;

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
 *
 * <p>{@code availabilityCondition}, when non-null, gates the alternate cost on an arbitrary
 * game-state condition (e.g. Qasali Ambusher's "if a creature is attacking you and you control a
 * Forest and a Plains"). {@code grantsFlash} additionally lets the alternate cast be made any time
 * the caster has priority ("as though it had flash"). An empty {@code costs} list models a free
 * cast ("without paying its mana cost").
 */
public record AlternateHandCast(List<CastingCost> costs, Set<CardSubtype> prowlDamageSubtypes,
                                Condition availabilityCondition, boolean grantsFlash) implements CastingOption {

    public AlternateHandCast(List<CastingCost> costs) {
        this(costs, Set.of(), null, false);
    }

    public AlternateHandCast(List<CastingCost> costs, Set<CardSubtype> prowlDamageSubtypes) {
        this(costs, prowlDamageSubtypes, null, false);
    }

    /** Convenience constructor for prowl with a single qualifying creature subtype. */
    public AlternateHandCast(List<CastingCost> costs, CardSubtype prowlDamageSubtype) {
        this(costs, Set.of(prowlDamageSubtype), null, false);
    }

    /**
     * Convenience constructor for a condition-gated alternate cast, optionally granting flash
     * timing (e.g. Qasali Ambusher's free "as though it had flash" cast).
     */
    public AlternateHandCast(List<CastingCost> costs, Condition availabilityCondition, boolean grantsFlash) {
        this(costs, Set.of(), availabilityCondition, grantsFlash);
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }
}
