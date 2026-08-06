package com.github.laxika.magicalvibes.model.effect;

/**
 * Queues one skipped turn, step or phase on a player. A single record covers the whole one-shot
 * "skips their next …" family: {@link SkipKind} picks which of the four per-player queues on
 * {@code GameData} is incremented and {@link SkipRecipient} picks whose. Skipping is a replacement
 * effect — proceeding past the turn/step/phase as though it didn't exist (CR 500.11 / 614.10) — and
 * anything scheduled for the skipped occurrence waits for the first one that isn't skipped, so two
 * skips queued on the same player make them skip the next two (CR 614.10a).
 *
 * @param kind      what is skipped, and therefore which queue the skip lands on
 * @param recipient whose occurrence is skipped
 */
public record SkipNextEffect(SkipKind kind, SkipRecipient recipient)
        implements CombatDamageTriggerContextEffect {

    /** "You skip your next …" — queued on the resolving controller, targeting nothing. */
    public SkipNextEffect(SkipKind kind) {
        this(kind, SkipRecipient.CONTROLLER);
    }

    /**
     * Only the targeted form declares a spec. The spec is benign for every kind: {@code harmful}
     * means "protection from the source must be honoured", which covers damage / destruction /
     * exile / sacrifice / fight, and a skipped step is none of those — nor could the flag ever be
     * read here, since {@code TargetValidationService} only runs its protection check against a
     * permanent target.
     */
    @Override
    public TargetSpec targetSpec() {
        return recipient == SkipRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    /**
     * Only {@link SkipRecipient#DAMAGED_PLAYER} needs a combat-damage trigger context — it reads the
     * damaged player out of the stack entry's {@code targetId}. The controller and targeted forms
     * take the plain entry.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return recipient == SkipRecipient.DAMAGED_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
