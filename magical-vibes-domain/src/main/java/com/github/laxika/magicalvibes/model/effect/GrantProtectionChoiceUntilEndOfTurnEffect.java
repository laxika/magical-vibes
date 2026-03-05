package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, prompts the controller to choose a color (and optionally "artifacts"),
 * then grants the target permanent protection from that choice until end of turn.
 * <p>
 * When {@code includeArtifacts} is {@code true}, the player may also choose "artifacts"
 * instead of a color (e.g. Apostle's Blessing).
 * When {@code false}, only the five colors are offered (e.g. Gods Willing).
 */
public record GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts) implements CardEffect {

    /**
     * Color-only variant (no artifact option).
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
