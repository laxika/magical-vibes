package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, prompts a player to choose a color (and optionally "artifacts"),
 * then grants the target permanent protection from that choice until end of turn.
 * <p>
 * When {@code includeArtifacts} is {@code true}, the player may also choose "artifacts"
 * instead of a color (e.g. Apostle's Blessing).
 * When {@code false}, only the five colors are offered (e.g. Gods Willing).
 * <p>
 * When {@code targetControllerChooses} is {@code true}, the choice is made by the target
 * permanent's controller rather than the ability's controller (e.g. Pale Wayfarer's
 * "protection from the color of its controller's choice").
 */
public record GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts,
                                                        boolean targetControllerChooses) implements CardEffect {

    /**
     * Color-only variant (no artifact option), chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect() {
        this(false, false);
    }

    /**
     * Chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts) {
        this(includeArtifacts, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
