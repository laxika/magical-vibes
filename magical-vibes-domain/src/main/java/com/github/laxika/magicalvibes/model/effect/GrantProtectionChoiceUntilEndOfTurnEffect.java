package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * On resolution, prompts a player to choose a color (and optionally "colorless" or "artifacts"),
 * then grants protection from that choice until end of turn to the permanents selected by
 * {@code scope}. A single color is chosen and applied to all of them.
 * <p>
 * When {@code includeArtifacts} is {@code true}, the player may also choose "artifacts"
 * instead of a color (e.g. Apostle's Blessing).
 * When {@code includeColorless} is {@code true}, the player may also choose "colorless"
 * instead of a color (e.g. Angelic Intervention).
 * <p>
 * When {@code targetControllerChooses} is {@code true}, the choice is made by the target
 * permanent's controller rather than the ability's controller (e.g. Pale Wayfarer's
 * "protection from the color of its controller's choice").
 * <p>
 * Scope {@link GrantScope#TARGET} is the "target permanent gains …" shape. Scope
 * {@link GrantScope#SELF} is "this creature gains protection from the color of your choice"
 * (Knight of Dawn) — no target is chosen and the grant resolves against the ability's source
 * permanent. Scope {@link GrantScope#OWN_CREATURES} is the untargeted mass form (Brave the
 * Elements) — every creature the controller controls that matches {@code filter}, determined on
 * resolution. {@code filter} is only meaningful there; {@code null} means "each creature you
 * control".
 * Scope {@link GrantScope#ALL_CREATURES} is the untargeted global form; it applies the chosen
 * protection to every creature on the battlefield.
 */
public record GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts,
                                                        boolean targetControllerChooses,
                                                        GrantScope scope,
                                                        PermanentPredicate filter,
                                                        boolean includeColorless) implements CardEffect {

    public GrantProtectionChoiceUntilEndOfTurnEffect {
        if (scope != GrantScope.TARGET && scope != GrantScope.SELF && scope != GrantScope.OWN_CREATURES
                && scope != GrantScope.ALL_CREATURES) {
            throw new IllegalArgumentException(
                    "GrantProtectionChoiceUntilEndOfTurnEffect supports only TARGET, SELF, OWN_CREATURES and ALL_CREATURES, got "
                            + scope);
        }
        if (filter != null && scope != GrantScope.OWN_CREATURES) {
            throw new IllegalArgumentException("A filter is only consulted for OWN_CREATURES, not " + scope);
        }
    }

    /**
     * Color-only variant (no artifact option), chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect() {
        this(false, false, GrantScope.TARGET, null, false);
    }

    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts,
                                                     boolean targetControllerChooses,
                                                     GrantScope scope,
                                                     PermanentPredicate filter) {
        this(includeArtifacts, targetControllerChooses, scope, filter, false);
    }

    /**
     * Chosen by the ability's controller.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts) {
        this(includeArtifacts, false, GrantScope.TARGET, null);
    }

    public GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts, boolean targetControllerChooses) {
        this(includeArtifacts, targetControllerChooses, GrantScope.TARGET, null);
    }

    /**
     * Self-scoped variant: the source permanent gains the protection, no target is chosen.
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope scope) {
        this(false, false, scope, null);
    }

    /**
     * Mass variant: every creature the controller controls matching {@code filter} gains the
     * protection. Untargeted — pass {@code null} for "each creature you control".
     */
    public GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope scope, PermanentPredicate filter) {
        this(false, false, scope, filter);
    }

    /**
     * Targeted variant that offers the five colors and colorless.
     */
    public static GrantProtectionChoiceUntilEndOfTurnEffect colorOrColorless() {
        return new GrantProtectionChoiceUntilEndOfTurnEffect(false, false, GrantScope.TARGET, null, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent());
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
