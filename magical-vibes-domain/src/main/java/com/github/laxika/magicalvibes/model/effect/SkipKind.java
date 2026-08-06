package com.github.laxika.magicalvibes.model.effect;

/**
 * Which turn structure a {@link SkipNextEffect} makes a player skip. Each constant names one of the
 * four sibling per-player queues on {@code GameData} that the turn engine drains; skipping means
 * proceeding past the step, phase or turn as though it didn't exist (CR 500.11 / 614.10), and two
 * skips of the same kind stack rather than collapsing (CR 614.10a).
 *
 * <ul>
 *   <li>{@link #TURN} — {@code skipNextTurnCount}, consumed by
 *       {@code TurnProgressionService.advanceTurn} when that player's turn would begin. Pending
 *       player-controlling effects wait until a turn is actually taken (CR 723.1b).</li>
 *   <li>{@link #UNTAP_STEP} — {@code skipNextUntapStepCount}, consumed by
 *       {@code TurnProgressionService.advanceTurn} when that player next becomes active. The whole
 *       step is skipped, so the phasing event doesn't happen either (CR 702.26m) and no
 *       untap-restriction choice (Storage Matrix, Static Orb) is offered — the same path Sands of
 *       Time's {@link PlayersSkipUntapStepEffect} uses.</li>
 *   <li>{@link #DRAW_STEP} — {@code skipNextDrawStepCount}, consumed by
 *       {@code StepTriggerService.handleDrawStep}. The whole step goes: no turn-based draw and no
 *       draw-step triggered abilities.</li>
 *   <li>{@link #COMBAT_PHASE} — {@code skipNextCombatPhaseCount}, consumed by
 *       {@code TurnProgressionService.advanceStep} when that player would leave their precombat
 *       main phase, jumping straight to postcombat main.</li>
 * </ul>
 *
 * <p>These are the one-shot "next occurrence" forms. Their static counterparts are separate effects
 * with a different lifetime and are deliberately not part of this family: {@link SkipDrawStepEffect}
 * (skipped for as long as its source is on the battlefield) and {@link PlayersSkipUntapStepEffect}.
 * {@link SkipNextUntapEffect} is different again — it marks individual permanents so they don't
 * untap while the untap step itself still happens.
 */
public enum SkipKind {
    TURN,
    UNTAP_STEP,
    DRAW_STEP,
    COMBAT_PHASE
}
