package com.github.laxika.magicalvibes.model.amount;

/**
 * The source permanent's effective toughness at evaluation time. Evaluation uses the live source permanent when it is
 * still on the battlefield, else its last-known snapshot (CR 608.2h last-known information). Evaluates to 0 when no
 * source is known at all. A negative toughness counts as 0 unless {@code allowNegative}: an effect that sets a
 * creature's toughness to it keeps the negative value (CR 107.1b).
 *
 * @param allowNegative whether a negative toughness is kept rather than counted as 0
 */
public record SourceToughness(boolean allowNegative) implements DynamicAmount {

    public SourceToughness() {
        this(false);
    }
}
