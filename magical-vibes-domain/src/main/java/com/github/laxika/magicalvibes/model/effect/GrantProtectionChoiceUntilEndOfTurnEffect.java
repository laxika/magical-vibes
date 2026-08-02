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
 * <p>
 * Scope {@link GrantScope#TARGET} is the "target permanent gains …" shape. Scope
 * {@link GrantScope#SELF} is "this creature gains protection from the color of your choice"
 * (Knight of Dawn) — no target is chosen and the grant resolves against the ability's source
 * permanent.
 */
public record GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts,
                                                        boolean targetControllerChooses,
                                                        GrantScope scope) implements CardEffect {

    /**
     * Color-only variant (no artifact option), chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect() {
        this(false, false, GrantScope.TARGET);
    }

    /**
     * Chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts) {
        this(includeArtifacts, false, GrantScope.TARGET);
    }

    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts, boolean targetControllerChooses) {
        this(includeArtifacts, targetControllerChooses, GrantScope.TARGET);
    }

    /**
     * Self-scoped variant: the source permanent gains the protection, no target is chosen.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope scope) {
        this(false, false, scope);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetCategory.PERMANENT);
            case SELF -> new TargetSpec(TargetCategory.NONE, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
