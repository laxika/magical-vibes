package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * The payload of a resolved "it doesn't untap during its controller's next untap step" effect on one permanent
 * (a floating effect on it, {@code Duration.PERMANENT}): exert (CR 701.43a), {@link SkipNextUntapEffect} on chosen
 * permanents, Vorinclex's opponent-land lock, "tap it at end of combat; it doesn't untap". Each resolution is its own
 * effect. During each untap step it applies to, the permanent doesn't untap and the effect is used up by one step:
 * replaced by one for a step fewer, or ended after its last (CR 701.43b: two such effects waiting for the same untap
 * step both end in it).
 *
 * @param untapSteps        the untap steps it still applies to
 * @param untapStepPlayerId the player whose next untap step it is ("your next untap step" of exert, or the target
 *                          player's), so it applies only during that player's untap step and ends with it even if
 *                          another player controls the permanent then; {@code null} for the untap step of whoever
 *                          controls the permanent
 */
public record DoesntUntapDuringNextUntapStepsEffect(int untapSteps, UUID untapStepPlayerId) implements CardEffect {

    public DoesntUntapDuringNextUntapStepsEffect {
        if (untapSteps < 1) {
            throw new IllegalArgumentException("untapSteps must be positive");
        }
    }

    /** Whether it applies during {@code activePlayerId}'s untap step. */
    public boolean appliesDuringUntapStepOf(UUID activePlayerId) {
        return untapStepPlayerId == null || untapStepPlayerId.equals(activePlayerId);
    }
}
