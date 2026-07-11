package com.github.laxika.magicalvibes.model.effect;

/**
 * An additional mana-value constraint on a {@link SearchLibraryEffect}, evaluated relative to the
 * resolving spell/ability's X (the stack entry's {@code xValue}) plus a fixed {@code offset}. When
 * {@code exact} is {@code true} the search matches only cards whose mana value equals
 * {@code X + offset}; otherwise it matches cards whose mana value is at most {@code X + offset}.
 *
 * <p>Used by Citanul Flute ({@code new XManaValueBound(false, 0)} — creatures with MV ≤ X), Green
 * Sun's Zenith ({@code new XManaValueBound(false, 0)}) and Birthing Pod
 * ({@code new XManaValueBound(true, 1)} — a creature with MV exactly one more than the sacrificed
 * creature's). A {@code null} bound on the effect means no mana-value constraint.
 */
public record XManaValueBound(boolean exact, int offset) {
}
