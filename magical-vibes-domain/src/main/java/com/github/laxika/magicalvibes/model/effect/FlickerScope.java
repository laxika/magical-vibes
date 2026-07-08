package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent(s) a {@link FlickerEffect} exiles.
 *
 * <ul>
 *   <li>{@code TARGET} — the single targeted permanent (target chosen via the source card's filter).</li>
 *   <li>{@code SELF} — the source permanent itself (Argent Sphinx-style).</li>
 *   <li>{@code TARGET_PLAYERS_PERMANENTS} — every permanent matching the effect's filter that the
 *       targeted player controls (Sudden Disappearance-style mass flicker).</li>
 * </ul>
 */
public enum FlickerScope {
    TARGET,
    SELF,
    TARGET_PLAYERS_PERMANENTS
}
