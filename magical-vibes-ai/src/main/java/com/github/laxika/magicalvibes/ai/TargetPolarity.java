package com.github.laxika.magicalvibes.ai;

/**
 * The AI's prior on which board a permanent-targeting effect should aim at: who benefits
 * from being this effect's target. This is heuristic metadata, not rules semantics — it
 * lives in the AI module (never on the domain records) and states the *typical* direction;
 * search layers (MCTS) may override it in specific board states.
 *
 * <p>The three harmful values differ only in which chooser ranks candidates once the
 * direction is settled: removal prefers creatures it can kill outright, damage adds an
 * opponent-face fallback, and generic harm ranks by threat.
 */
public enum TargetPolarity {

    /** The target leaves the battlefield (destroy, exile, bounce) — kill-preference chooser. */
    HARMFUL_REMOVAL,

    /** The target takes damage — threat-ranked chooser with an opponent-face fallback. */
    HARMFUL_DAMAGE,

    /** Other harm (tap-down, debuff, steal, forced attack, …) — threat-ranked chooser. */
    HARMFUL,

    /** The effect helps its target (pump, untap, regeneration, …) — own board first. */
    BENEFICIAL,

    /** Deliberately no direction preference (e.g. Twiddle's tap-or-untap). */
    NEUTRAL;

    public boolean isHarmful() {
        return this == HARMFUL_REMOVAL || this == HARMFUL_DAMAGE || this == HARMFUL;
    }
}
