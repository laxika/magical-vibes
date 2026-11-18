package com.github.laxika.magicalvibes.model.effect;

/**
 * One skipped turn, step or phase for a player. A single record covers the whole one-shot "skips their
 * next …" family: {@link SkipKind} picks what is skipped and {@link SkipRecipient} picks whose; each
 * resolution makes one floating effect on that player, used up as it applies. Skipping is a replacement
 * effect — proceeding past the turn/step/phase as though it didn't exist (CR 500.11 / 614.10) — and
 * anything scheduled for the skipped occurrence waits for the first one that isn't skipped, so two
 * skips queued on the same player make them skip the next two (CR 614.10a).
 *
 * @param kind      what is skipped
 * @param recipient whose occurrence is skipped
 * @param allCombatPhasesOfNextTurn whether to skip every combat of the affected player's next turn
 * @param thisTurnOnly whether the skip lasts only this turn ("skip your draw step this turn", "skip your next combat
 *                     phase this turn"); it applies before a skip that waits, and ends unused at cleanup
 */
public record SkipNextEffect(SkipKind kind, SkipRecipient recipient, boolean allCombatPhasesOfNextTurn,
                             boolean thisTurnOnly)
        implements CardEffect {

    public SkipNextEffect(SkipKind kind, SkipRecipient recipient, boolean allCombatPhasesOfNextTurn) {
        this(kind, recipient, allCombatPhasesOfNextTurn, false);
    }

    public SkipNextEffect(SkipKind kind, SkipRecipient recipient) {
        this(kind, recipient, false);
    }

    public SkipNextEffect {
        if (thisTurnOnly && (kind == SkipKind.TURN || kind == SkipKind.UNTAP_STEP || allCombatPhasesOfNextTurn)) {
            throw new IllegalArgumentException("This-turn duration requires a single draw step or combat phase");
        }
        if (allCombatPhasesOfNextTurn && kind != SkipKind.COMBAT_PHASE) {
            throw new IllegalArgumentException("All-combat duration requires combat phases");
        }
    }

    /** "You skip your next …" — on the resolving controller, targeting nothing. */
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
}
