package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Skip your draw step" (Colfenor's Plans, Necropotence), "players skip their upkeep steps" (Eon Hub), "players skip
 * their untap steps" (Stasis, Sands of Time, the plane The Eon Fog): each player
 * {@code players} names skips each {@code kind} step or phase of their turns. Skipping is a replacement effect
 * ("instead of doing it, do nothing"), and a step that has started can't be skipped (CR 614.10).
 *
 * <p>As a static ability its players are judged against the source and its controller
 * ({@code SubjectResolutionService.namesPlayer}; a condition such as hellbent wraps it in a {@link ConditionalEffect}),
 * read by {@code StepTriggerService.skipsDrawStepByStaticAbility} / {@code playersSkipUpkeepStepApplies} and
 * {@code UntapStepService.playersSkipUntapStepApplies} (which also reads a face-up plane's, judged against the planar
 * controller). Floated on a
 * player ({@code GrantStaticEffectToPlayerEffect}, Fatespinner; {@code SkipNextEffect}'s combat form) it applies to
 * the player it is on for its duration, {@code players} being unread, and is read by
 * {@code GameData.skipsStepOrPhase}.
 *
 * @param kind    the step or phase skipped
 * @param players who skips it; the controller by default
 */
public record SkipDrawStepEffect(SkipStepOrPhaseKind kind, PlayerSubject players) implements CardEffect {

    public SkipDrawStepEffect() {
        this(SkipStepOrPhaseKind.DRAW_STEP, Subjects.you());
    }

    public SkipDrawStepEffect(SkipStepOrPhaseKind kind) {
        this(kind, Subjects.you());
    }

    public SkipDrawStepEffect {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(players, "players");
    }
}
