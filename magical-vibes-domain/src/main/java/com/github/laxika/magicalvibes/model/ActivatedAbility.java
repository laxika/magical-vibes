package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
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
    private final CardSubtype requiredControlledSubtype;
    private final int requiredControlledSubtypeCount;
    /** Minimum number of cards the controller must have in hand to activate (0 = no restriction). Set via {@link #withMinCardsInHand(int)}. */
    private int minCardsInHandToActivate;
    /** When true, any player (not just the source's controller) may activate this ability, e.g. Oona's Prowler. Set via {@link #withActivatableByAnyPlayer()}. */
    private boolean activatableByAnyPlayer;
    /** When true, the ability's cost includes the untap symbol {@code {Q}}: the permanent must be tapped and is untapped to pay (e.g. Order of Whiteclay). Set via {@link #withRequiresUntap()}. */
    private boolean requiresUntap;
    /** Predicate a controlled permanent must match to count toward {@link #requiredControlledPermanentCount} (e.g. Leechridden Swamp's "two or more black permanents"). Null = no such restriction. Set via {@link #withRequiredControlledPermanents}. */
    private PermanentPredicate requiredControlledPermanentPredicate;
    /** Minimum number of controlled permanents matching {@link #requiredControlledPermanentPredicate} required to activate. */
    private int requiredControlledPermanentCount;
    /** Human-readable description of the predicate-count restriction, used in the activation error message. */
    private String requiredControlledPermanentDescription;

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description) {
        this(requiresTap, manaCost, effects, description, null, null, null, null, List.of(), 1, 1, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, description, targetFilter, null, null, null, List.of(), 1, 1, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, Integer maxActivationsPerTurn) {
        this(requiresTap, manaCost, effects, description, null, null, maxActivationsPerTurn, null, List.of(), 1, 1, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, description, null, null, null, timingRestriction, List.of(), 1, 1, false, null, null, 0);
    }

    // Loyalty ability constructor
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description) {
        this(false, null, effects, description, null, loyaltyCost, null, null, List.of(), 1, 1, false, null, null, 0);
    }

    // Loyalty ability constructor with target filter
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(false, null, effects, description, targetFilter, loyaltyCost, null, null, List.of(), 1, 1, false, null, null, 0);
    }

    // Variable loyalty ability (-X) with target filter
    public static ActivatedAbility variableLoyaltyAbility(List<CardEffect> effects, String description, TargetFilter targetFilter) {
        return new ActivatedAbility(false, null, effects, description, targetFilter, 0, null, null, List.of(), 1, 1, true, null, null, 0);
    }

    // Multi-target ability constructor (e.g. Brass Squire: target Equipment + target creature)
    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets) {
        this(requiresTap, manaCost, effects, description, null, null, null, null, multiTargetFilters, minTargets, maxTargets, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction, List.of(), 1, 1, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                            ActivationTimingRestriction timingRestriction,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction, multiTargetFilters, minTargets, maxTargets, false, null, null, 0);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                            ActivationTimingRestriction timingRestriction,
                            List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets,
                            boolean variableLoyaltyCost) {
        this(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost, maxActivationsPerTurn,
                timingRestriction, multiTargetFilters, minTargets, maxTargets, variableLoyaltyCost, null, null, 0);
    }

    // Ability with subtype count restriction (e.g. "Activate only if you control five or more Vampires")
    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                            CardSubtype requiredControlledSubtype, int requiredControlledSubtypeCount) {
        this(requiresTap, manaCost, effects, description, null, null, null, null, List.of(), 1, 1, false, null, requiredControlledSubtype, requiredControlledSubtypeCount);
    }

    private ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description,
                             TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn,
                             ActivationTimingRestriction timingRestriction,
                             List<TargetFilter> multiTargetFilters, int minTargets, int maxTargets,
                             boolean variableLoyaltyCost, UUID grantSourcePermanentId,
                             CardSubtype requiredControlledSubtype, int requiredControlledSubtypeCount) {
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
        this.requiredControlledSubtype = requiredControlledSubtype;
        this.requiredControlledSubtypeCount = requiredControlledSubtypeCount;
    }

    /**
     * Returns a copy of this ability with the grant source permanent ID set.
     * Used by the static bonus system to track which permanent granted this ability.
     */
    public ActivatedAbility withGrantSource(UUID sourcePermanentId) {
        ActivatedAbility copy = new ActivatedAbility(requiresTap, manaCost, effects, description, targetFilter, loyaltyCost,
                maxActivationsPerTurn, timingRestriction, multiTargetFilters, minTargets, maxTargets,
                variableLoyaltyCost, sourcePermanentId, requiredControlledSubtype, requiredControlledSubtypeCount);
        copy.minCardsInHandToActivate = this.minCardsInHandToActivate;
        copy.activatableByAnyPlayer = this.activatableByAnyPlayer;
        copy.requiresUntap = this.requiresUntap;
        copy.requiredControlledPermanentPredicate = this.requiredControlledPermanentPredicate;
        copy.requiredControlledPermanentCount = this.requiredControlledPermanentCount;
        copy.requiredControlledPermanentDescription = this.requiredControlledPermanentDescription;
        return copy;
    }

    /**
     * Fluent setter for a "Activate only if you control N or more [matching] permanents" restriction
     * (e.g. Leechridden Swamp's "two or more black permanents"). {@code description} is the plural
     * noun phrase used in the activation error message. Returns this ability for chaining.
     */
    public ActivatedAbility withRequiredControlledPermanents(PermanentPredicate predicate, int count, String description) {
        this.requiredControlledPermanentPredicate = predicate;
        this.requiredControlledPermanentCount = count;
        this.requiredControlledPermanentDescription = description;
        return this;
    }

    /**
     * Fluent setter marking this ability's cost as including the untap symbol {@code {Q}}: the
     * source permanent must be tapped to activate, and paying the cost untaps it (e.g. Order of
     * Whiteclay). Returns this ability for chaining in card constructors.
     */
    public ActivatedAbility withRequiresUntap() {
        this.requiresUntap = true;
        return this;
    }

    /**
     * Fluent setter for a "activate only if you have N or more cards in your hand" restriction
     * (e.g. Resonating Lute). Returns this ability for chaining in card constructors.
     */
    public ActivatedAbility withMinCardsInHand(int minCards) {
        this.minCardsInHandToActivate = minCards;
        return this;
    }

    /**
     * Fluent setter marking this ability as activatable by any player, not just the source's
     * controller (e.g. Oona's Prowler's "Any player may activate this ability."). Returns this
     * ability for chaining in card constructors.
     */
    public ActivatedAbility withActivatableByAnyPlayer() {
        this.activatableByAnyPlayer = true;
        return this;
    }

    public boolean isNeedsTarget() {
        return !multiTargetFilters.isEmpty()
                || effects.stream().anyMatch(e -> {
                    TargetCategory category = e.targetSpec().category();
                    return category.includesPlayers() || category.includesPermanents() || category.isGraveyard();
                });
    }

    public boolean isMultiTarget() {
        return !multiTargetFilters.isEmpty();
    }

    public boolean isNeedsSpellTarget() {
        return effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
    }
}
