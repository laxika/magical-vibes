package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class ActivatedAbility {

    private final boolean requiresTap;
    private final String manaCost;
    private final List<CardEffect> effects;
    private final String description;
    private final TargetFilter targetFilter;
    private final Integer loyaltyCost;
    private final Integer maxActivationsPerTurn;
    private final ActivationTimingRestriction timingRestriction;
    private final List<TargetFilter> multiTargetFilters;
    private final int minTargets;
    private final int maxTargets;
    private final boolean variableLoyaltyCost;
    private final UUID grantSourcePermanentId;

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description) {
        this(requiresTap, manaCost, effects, description, null, null, null, null, List.of(), 1, 1, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, description, targetFilter, null, null, null, List.of(), 1, 1, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, Integer maxActivationsPerTurn) {
        this(requiresTap, manaCost, effects, description, null, null, maxActivationsPerTurn, null, List.of(), 1, 1, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, description, null, null, null, timingRestriction, List.of(), 1, 1, false);
    }

    // Loyalty ability constructor
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description) {
        this(false, null, effects, description, null, loyaltyCost, null, null, List.of(), 1, 1, false);
    }

    // Loyalty ability constructor with target filter
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(false, null, effects, description, targetFilter, loyaltyCost, null, null, List.of(), 1, 1, false);
    }

    // Variable loyalty ability (-X) with target filter
    public static ActivatedAbility variableLoyaltyAbility(List<CardEffect> effects, String description, TargetFilter targetFilter) {
        return new ActivatedAbility(false, null, effects, description, targetFilter, 0, null, null, List.of(), 1, 1, true);
    }

    // Multi-target ability constructor (e.g. Brass Squire: target Equipment + target creature)
    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets) {
        this(requiresTap, manaCost, effects, description, null, null, null, null, multiTargetFilters, minTargets, maxTargets, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction, List.of(), 1, 1, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                            ActivationTimingRestriction timingRestriction,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction, multiTargetFilters, minTargets, maxTargets, false);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                            ActivationTimingRestriction timingRestriction,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets,
                            boolean variableLoyaltyCost) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn,
                timingRestriction, multiTargetFilters, minTargets, maxTargets, variableLoyaltyCost, null);
    }

    private ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                             TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                             ActivationTimingRestriction timingRestriction,
                             List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets,
                             boolean variableLoyaltyCost, UUID grantSourcePermanentId) {
        this.requiresTap = requiresTap;
        this.manaCost = manaCost;
        this.effects = effects;
        this.description = description;
        this.targetFilter = targetFilter;
        this.loyaltyCost = variableLoyaltyCost ? Integer.valueOf(0) : loyaltyCost;
        this.maxActivationsPerTurn = maxActivationsPerTurn;
        this.timingRestriction = timingRestriction;
        this.multiTargetFilters = multiTargetFilters != null ? multiTargetFilters : List.of();
        this.minTargets = minTargets;
        this.maxTargets = maxTargets;
        this.variableLoyaltyCost = variableLoyaltyCost;
        this.grantSourcePermanentId = grantSourcePermanentId;
    }

    /**
     * Returns a copy of this ability with the grant source permanent ID set.
     * Used by the static bonus system to track which permanent granted this ability.
     */
    public ActivatedAbility withGrantSource(UUID sourcePermanentId) {
        return new ActivatedAbility(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost,
                maxActivationsPerTurn, timingRestriction, multiTargetFilters, minTargets, maxTargets,
                variableLoyaltyCost, sourcePermanentId);
    }

    public boolean isNeedsTarget() {
        return !multiTargetFilters.isEmpty()
                || effects.stream().anyMatch(e -> e.canTargetPlayer() || e.canTargetPermanent() || e.canTargetGraveyard());
    }

    public boolean isMultiTarget() {
        return !multiTargetFilters.isEmpty();
    }

    public boolean isNeedsSpellTarget() {
        return effects.stream().anyMatch(CardEffect::canTargetSpell);
    }
}
