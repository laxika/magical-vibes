package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent a counter-removal effect takes counters off: the ability's own source permanent
 * ("remove a counter from this permanent") or the effect's target ("remove all -1/-1 counters from
 * target creature"). The distinction also drives {@code targetSpec()} — a {@link #SOURCE} form
 * declares the non-targeting source-required spec, a {@link #TARGET} form declares a benign
 * creature target.
 *
 * <p>Sibling of {@code PhaseOutSubject} and {@code PermanentReference}: the same "which permanent
 * does this effect act on" axis, kept separate because this family selects between exactly these
 * two and neither of the other enums offers both.</p>
 */
public enum CounterRemovalSubject {

    /**
     * The stack entry's source permanent, falling back to its {@code targetId} when no source is
     * bound (trigger slots that carry the affected permanent as a non-targeting target).
     */
    SOURCE,

    /** The stack entry's target permanent. */
    TARGET
}
