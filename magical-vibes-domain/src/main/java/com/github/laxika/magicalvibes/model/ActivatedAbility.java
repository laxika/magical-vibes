package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
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
    /** Maximum number of cards the controller may have in hand to activate, or null for no restriction (e.g. Dread Wanderer's "one or fewer cards in hand" = 1). Set via {@link #withMaxCardsInHand(int)}. */
    private Integer maxCardsInHandToActivate;
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
    /** Cross-target restriction on the whole chosen set for a multi-target ability (CR 601.2c), beyond the per-position filters (e.g. Gauntlets of Chaos). Null = no such restriction. Set via {@link #withMultiTargetConstraint}. */
    private MultiTargetConstraint multiTargetConstraint;
    /** Counter type the source permanent must carry at least {@link #requiredSourceCounterCount} of to activate (e.g. Edifice of Authority's "three or more brick counters on this artifact"). Null = no such restriction. Set via {@link #withRequiredSourceCounters}. */
    private CounterType requiredSourceCounterType;
    /** Minimum number of {@link #requiredSourceCounterType} counters the source permanent must have to activate. */
    private int requiredSourceCounterCount;
    /** Predicate a non-token card in the controller's graveyard must match to count toward {@link #requiredGraveyardCardCount} (e.g. Gate to the Afterlife's "six or more creature cards in your graveyard"). Null = no such restriction. Set via {@link #withRequiredGraveyardCards}. */
    private CardPredicate requiredGraveyardCardPredicate;
    /** Minimum number of matching cards in the controller's graveyard required to activate. */
    private int requiredGraveyardCardCount;
    /** Human-readable description of the graveyard-count restriction, used in the activation error message. */
    private String requiredGraveyardCardDescription;
    /**
     * Arbitrary activation gate evaluated via {@code ConditionEvaluationService} (e.g. Desert's
     * "control a Desert or Desert in graveyard" OR). Null = no such restriction. Set via
     * {@link #withActivationCondition}. Prefer the typed helpers ({@link #withRequiredControlledPermanents},
     * {@link #withRequiredGraveyardCards}, timing enums) when they cover the oracle text; use this for
     * compound conditions those helpers cannot express alone.
     */
    private Condition activationCondition;
    /** Human-readable activation-condition failure message (full sentence shown to the player). */
    private String activationConditionDescription;

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
        copy.maxCardsInHandToActivate = this.maxCardsInHandToActivate;
        copy.activatableByAnyPlayer = this.activatableByAnyPlayer;
        copy.requiresUntap = this.requiresUntap;
        copy.requiredControlledPermanentPredicate = this.requiredControlledPermanentPredicate;
        copy.requiredControlledPermanentCount = this.requiredControlledPermanentCount;
        copy.requiredControlledPermanentDescription = this.requiredControlledPermanentDescription;
        copy.multiTargetConstraint = this.multiTargetConstraint;
        copy.requiredSourceCounterType = this.requiredSourceCounterType;
        copy.requiredSourceCounterCount = this.requiredSourceCounterCount;
        copy.requiredGraveyardCardPredicate = this.requiredGraveyardCardPredicate;
        copy.requiredGraveyardCardCount = this.requiredGraveyardCardCount;
        copy.requiredGraveyardCardDescription = this.requiredGraveyardCardDescription;
        copy.activationCondition = this.activationCondition;
        copy.activationConditionDescription = this.activationConditionDescription;
        return copy;
    }

    /**
     * Fluent setter for an "Activate only if there are N or more [type] counters on this permanent"
     * restriction (e.g. Edifice of Authority's "three or more brick counters on this artifact"). The
     * count is checked against the source permanent itself. Returns this ability for chaining.
     */
    public ActivatedAbility withRequiredSourceCounters(CounterType counterType, int count) {
        this.requiredSourceCounterType = counterType;
        this.requiredSourceCounterCount = count;
        return this;
    }

    /**
     * Fluent setter for a cross-target restriction imposed on the whole set of chosen targets of a
     * multi-target ability (CR 601.2c), beyond the per-position filters (e.g. Gauntlets of Chaos'
     * "shares one of those types with it"). Returns this ability for chaining in card constructors.
     */
    public ActivatedAbility withMultiTargetConstraint(MultiTargetConstraint constraint) {
        this.multiTargetConstraint = constraint;
        return this;
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
     * Fluent setter for an "Activate only if there are N or more [matching] cards in your graveyard"
     * restriction (e.g. Gate to the Afterlife's "six or more creature cards in your graveyard").
     * {@code description} is the noun phrase spliced into the activation error message. Counts only
     * non-token cards in the controller's own graveyard. Returns this ability for chaining.
     */
    public ActivatedAbility withRequiredGraveyardCards(CardPredicate predicate, int count, String description) {
        this.requiredGraveyardCardPredicate = predicate;
        this.requiredGraveyardCardCount = count;
        this.requiredGraveyardCardDescription = description;
        return this;
    }

    /**
     * Fluent setter for a compound "Activate only if …" restriction expressed as a {@link Condition}
     * (e.g. Wall of Forgotten Pharaohs' "you control a Desert or there is a Desert card in your
     * graveyard"). {@code description} is the full error message shown when the condition is not met.
     * Returns this ability for chaining.
     */
    public ActivatedAbility withActivationCondition(Condition condition, String description) {
        this.activationCondition = condition;
        this.activationConditionDescription = description;
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
     * Fluent setter for an "activate only if you have N or fewer cards in your hand" restriction
     * (e.g. Dread Wanderer's "one or fewer cards in hand"). Returns this ability for chaining in
     * card constructors.
     */
    public ActivatedAbility withMaxCardsInHand(int maxCards) {
        this.maxCardsInHandToActivate = maxCards;
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

    /**
     * Whether this is an embalm or eternalize ability. Both keywords are modelled as a
     * graveyard-activated ability that creates a token copy of its source
     * ({@link CreateTokenCopyOfSourceEffect}), so the presence of that effect is the structural
     * marker shared by both. Read by "creature card with eternalize or embalm" searches
     * ({@code CardHasEmbalmOrEternalizePredicate}) and by the "whenever you activate an eternalize
     * or embalm ability" trigger (Vizier of the Anointed).
     */
    public boolean isEmbalmOrEternalize() {
        return effects.stream().anyMatch(CreateTokenCopyOfSourceEffect.class::isInstance);
    }

    /**
     * Whether this is a cycling ability (or typecycling / landcycling variant). Detected from the
     * ability description's name segment ending in {@code "cycling"} (engine convention —
     * {@code "Cycling {2} …"}, {@code "Islandcycling {2} …"}, {@code "Basic landcycling {2} …"}).
     */
    public boolean isCyclingAbility() {
        if (description == null) {
            return false;
        }
        int brace = description.indexOf('{');
        String namePart = (brace >= 0 ? description.substring(0, brace) : description).trim();
        return namePart.toLowerCase().endsWith("cycling");
    }
}
