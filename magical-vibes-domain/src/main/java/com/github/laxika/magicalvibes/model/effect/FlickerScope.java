package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent(s) a {@link FlickerEffect} exiles.
 *
 * <ul>
 *   <li>{@code TARGET} — the single targeted permanent (target chosen via the source card's filter).</li>
 *   <li>{@code SELF} — the source permanent itself (Argent Sphinx-style).</li>
 *   <li>{@code TARGET_PLAYERS_PERMANENTS} — every permanent matching the effect's filter that the
 *       targeted player controls (Sudden Disappearance-style mass flicker).</li>
 *   <li>{@code CONTROLLERS_PERMANENTS} — every permanent matching the effect's filter that the
 *       effect's own controller controls, with no target at all (Legion's Initiative). All of them
 *       return together as one delayed action, so they re-enter simultaneously.</li>
 * </ul>
 */
public enum FlickerScope {
    TARGET,
    SELF,
    TARGET_PLAYERS_PERMANENTS,
    CONTROLLERS_PERMANENTS
}
