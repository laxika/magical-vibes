package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a regeneration shield (CR 701.15).
 * <p>
 * {@code opponentMayDrawOnRegenerate} is Soldevi Sentry's rider: "When it regenerates this way, that
 * player may draw a card." The draw is owed only when the shield is actually spent, so it rides on
 * the shield ({@code Permanent.opponentDrawRegenerationShield}) rather than resolving with the
 * ability — a sibling {@code TargetOpponentMayDrawCardEffect} would hand over the card even if the
 * creature never regenerated.
 * <p>
 * {@code putMinusOneCounterOnRegenerate} is Matopi Golem's rider: "When it regenerates this way,
 * put a -1/-1 counter on it." Same shield-tagging pattern
 * ({@code Permanent.minusOneCounterRegenerationShield}).
 */
public record RegenerateEffect(
        boolean targetsPermanent,
        boolean opponentMayDrawOnRegenerate,
        boolean putMinusOneCounterOnRegenerate
) implements RegenerationEffect {

    public RegenerateEffect() {
        this(false, false, false);
    }

    public RegenerateEffect(boolean targetsPermanent) {
        this(targetsPermanent, false, false);
    }

    public RegenerateEffect(boolean targetsPermanent, boolean opponentMayDrawOnRegenerate) {
        this(targetsPermanent, opponentMayDrawOnRegenerate, false);
    }

    /** Matopi Golem: regenerate self; put a -1/-1 counter only when the shield is spent. */
    public static RegenerateEffect withMinusOneCounterOnRegenerate() {
        return new RegenerateEffect(false, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPermanent
                ? TargetSpec.benign(TargetCategory.PERMANENT)
                : new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
