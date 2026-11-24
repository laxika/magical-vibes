package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "[This creature] must be blocked [by a {@code blockerFilter} creature] if able": a blocking requirement on the
 * attacker (CR 509.1c), printed on it, on an Aura or Equipment attached to it ("equipped creature must be blocked by an
 * Eldrazi if able", Slayer's Cleaver), or the payload of a resolution's floating effect on it. It is obeyed when at
 * least one blocker matching {@code blockerFilter} blocks it; no cost has to be paid to obey it.
 * {@code GameQueryService.mustBeBlockedRequirements} is its one reader.
 *
 * @param blockerFilter the creatures whose block obeys it, or {@code null} for any creature
 */
public record MustBeBlockedIfAbleEffect(PermanentPredicate blockerFilter) implements CardEffect {

    /** "Must be blocked if able", by any creature. */
    public MustBeBlockedIfAbleEffect() {
        this(null);
    }
}
