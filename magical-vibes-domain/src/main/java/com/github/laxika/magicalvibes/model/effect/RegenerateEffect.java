package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a regeneration shield (CR 701.15).
 * <p>
 * {@code opponentMayDrawOnRegenerate} is Soldevi Sentry's rider: "When it regenerates this way, that
 * player may draw a card." The draw is owed only when the shield is actually spent, so it rides on
 * the shield ({@code Permanent.opponentDrawRegenerationShield}) rather than resolving with the
 * ability — a sibling {@code TargetOpponentMayDrawCardEffect} would hand over the card even if the
 * creature never regenerated.
 */
public record RegenerateEffect(boolean targetsPermanent, boolean opponentMayDrawOnRegenerate) implements RegenerationEffect {

    public RegenerateEffect() {
        this(false, false);
    }

    public RegenerateEffect(boolean targetsPermanent) {
        this(targetsPermanent, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPermanent
                ? TargetSpec.benign(TargetCategory.PERMANENT)
                : new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
