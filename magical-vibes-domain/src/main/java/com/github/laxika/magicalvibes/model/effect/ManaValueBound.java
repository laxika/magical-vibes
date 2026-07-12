package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * An additional mana-value constraint on a {@link SearchLibraryEffect}, evaluated relative to a
 * {@link DynamicAmount} base plus a fixed {@code offset}. When {@code exact} is {@code true} the
 * search matches only cards whose mana value equals {@code base + offset}; otherwise it matches
 * cards whose mana value is at most {@code base + offset}.
 *
 * <p>X-relative searches use an {@link XValue} base (the {@code (exact, offset)} convenience
 * constructor): Citanul Flute / Green Sun's Zenith ({@code new ManaValueBound(false, 0)} — MV ≤ X)
 * and Birthing Pod ({@code new ManaValueBound(true, 1)} — a creature with MV exactly one more than
 * the sacrificed creature). Other bases derive the bound from the board: Beseech the Queen uses a
 * {@code PermanentCount} of the lands the controller has (MV ≤ lands controlled). A {@code null}
 * bound on the effect means no mana-value constraint.
 */
public record ManaValueBound(DynamicAmount amount, boolean exact, int offset) {

    /** X-relative bound: base = the resolving spell/ability's X. */
    public ManaValueBound(boolean exact, int offset) {
        this(new XValue(), exact, offset);
    }
}
