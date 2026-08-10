package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
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
    /** When true, only the controller of the permanent this Aura is attached to may activate this ability, e.g. Volrath's Curse. Set via {@link #withActivatableOnlyByEnchantedPermanentController()}. */
    private boolean activatableOnlyByEnchantedPermanentController;
    /** When true, only opponents of the source permanent's controller may activate this ability, e.g. Soul Ransom. Set via {@link #withActivatableOnlyByOpponents()}. */
    private boolean activatableOnlyByOpponents;
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
    /** Position in the target list chosen by the opponent of the first target. */
    private int opponentChosenTargetIndex = -1;
    /** Filter used when presenting the opponent's target choice. */
    private TargetFilter opponentChosenTargetFilter;
    /** Whether the same permanent may be selected in more than one target group. */
    private boolean allowSharedTargets;
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
    /**
     * Per-turn activation cap that is recomputed from the game state at each activation, for
     * "Activate no more times each turn than [count]" (e.g. Withering Wisps' "the number of snow
     * Swamps you control"). Null = no dynamic cap; use {@link #maxActivationsPerTurn} for a fixed
     * printed number. Set via {@link #withMaxActivationsPerTurn(DynamicAmount, String)}.
     */
    private DynamicAmount maxActivationsPerTurnAmount;
    /** Human-readable description of the dynamic cap, used in the activation error message. */
    private String maxActivationsPerTurnDescription;
    /**
     * When true the number of targets scales with the X paid for this ability's {@code {X}} mana
     * cost ("{X}, {T}, Sacrifice this artifact: X target creatures … " — Runed Arch). The ability
     * declares {@code minTargets = 0} and {@code maxTargets} as a sanity cap; the effective bounds
     * are computed from the paid X by {@link #getEffectiveMinTargets(int)} /
     * {@link #getEffectiveMaxTargets(int)}. The ability-side counterpart of {@code Card.targetX}.
     * Set via {@link #withXScaledTargets()}.
     */
    private boolean xScaledTargets;
    /** Whether activation requires a player-chosen xValue even though the cost is not mana-based. */
    private boolean requiresXValue;
    /**
     * Whether the chosen xValue is bounded by the +1/+1 counters on all creatures the activating
     * player controls rather than by those on the source permanent ("Remove one or more +1/+1
     * counters from among creatures you control" — Ooze Flux). Set via
     * {@link #withXValueFromControlledCreatureCounters()}.
     */
    private boolean xValueFromControlledCreatureCounters;
    /**
     * Whole-game activation cap for "Activate only once" (e.g. Goblin Ski Patrol). Null = no such
     * cap. Counted per permanent object in {@code GameData.activatedAbilityUsesThisGame}, so a
     * permanent that leaves and re-enters the battlefield may activate again (CR 400.7). Set via
     * {@link #withMaxActivationsPerGame(int)}.
     */
    private Integer maxActivationsPerGame;
    /**
     * When true this hand-activated ability's intrinsic cost exiles the source card instead of
     * discarding it ("Exile this card from your hand: Add {G}" — Elvish Spirit Guide). No discard
     * triggers fire. Set via {@link #withExilesSourceFromHand()}.
     */
    private boolean exilesSourceFromHand;
    /**
     * When true this hand-activated ability is a ninjutsu ability (CR 702.49a). Its intrinsic cost
     * returns an unblocked attacking creature the activating player controls to its owner's hand
     * instead of discarding the source, and the source card stays in hand — revealed — until the
     * ability resolves and puts it onto the battlefield tapped and attacking. Set via
     * {@link #withNinjutsu()}.
     */
    private boolean ninjutsuAbility;

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
        copy.activatableOnlyByEnchantedPermanentController = this.activatableOnlyByEnchantedPermanentController;
        copy.activatableOnlyByOpponents = this.activatableOnlyByOpponents;
        copy.requiresUntap = this.requiresUntap;
        copy.requiredControlledPermanentPredicate = this.requiredControlledPermanentPredicate;
        copy.requiredControlledPermanentCount = this.requiredControlledPermanentCount;
        copy.requiredControlledPermanentDescription = this.requiredControlledPermanentDescription;
        copy.multiTargetConstraint = this.multiTargetConstraint;
        copy.opponentChosenTargetIndex = this.opponentChosenTargetIndex;
        copy.opponentChosenTargetFilter = this.opponentChosenTargetFilter;
        copy.allowSharedTargets = this.allowSharedTargets;
        copy.requiredSourceCounterType = this.requiredSourceCounterType;
        copy.requiredSourceCounterCount = this.requiredSourceCounterCount;
        copy.requiredGraveyardCardPredicate = this.requiredGraveyardCardPredicate;
        copy.requiredGraveyardCardCount = this.requiredGraveyardCardCount;
        copy.requiredGraveyardCardDescription = this.requiredGraveyardCardDescription;
        copy.activationCondition = this.activationCondition;
        copy.activationConditionDescription = this.activationConditionDescription;
        copy.maxActivationsPerTurnAmount = this.maxActivationsPerTurnAmount;
        copy.maxActivationsPerTurnDescription = this.maxActivationsPerTurnDescription;
        copy.maxActivationsPerGame = this.maxActivationsPerGame;
        copy.xScaledTargets = this.xScaledTargets;
        copy.requiresXValue = this.requiresXValue;
        copy.xValueFromControlledCreatureCounters = this.xValueFromControlledCreatureCounters;
        return copy;
    }

    /**
     * Fluent setter for a per-turn activation cap computed from the game state rather than printed
     * as a fixed number ("Activate no more times each turn than the number of snow Swamps you
     * control"). The amount is evaluated at each activation with the source permanent and its
     * controller in context. Returns this ability for chaining in card constructors.
     */
    public ActivatedAbility withMaxActivationsPerTurn(DynamicAmount amount, String description) {
        this.maxActivationsPerTurnAmount = amount;
        this.maxActivationsPerTurnDescription = description;
        return this;
    }

    /**
     * Fluent setter for a whole-game activation cap ("Activate only once", Goblin Ski Patrol). The
     * count is kept per permanent object, so a permanent that leaves and re-enters the battlefield is
     * a new object and may activate again (CR 400.7). Returns this ability for chaining.
     */
    public ActivatedAbility withMaxActivationsPerGame(int maxActivations) {
        this.maxActivationsPerGame = maxActivations;
        return this;
    }

    /**
     * Fluent setter marking a hand-activated ability whose intrinsic cost exiles the source card
     * rather than discarding it ("Exile this card from your hand: Add {G}"). Returns this ability
     * for chaining.
     */
    public ActivatedAbility withExilesSourceFromHand() {
        this.exilesSourceFromHand = true;
        return this;
    }

    /**
     * Fluent setter marking this hand-activated ability as ninjutsu (CR 702.49a). Returns this
     * ability for chaining.
     */
    public ActivatedAbility withNinjutsu() {
        this.ninjutsuAbility = true;
        return this;
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

    /** Marks one target position as chosen by the opponent who controls the first target. */
    public ActivatedAbility withOpponentChosenTarget(int targetIndex, TargetFilter targetFilter) {
        this.opponentChosenTargetIndex = targetIndex;
        this.opponentChosenTargetFilter = targetFilter;
        return this;
    }

    /** Allows one permanent to be selected in more than one target group. */
    public ActivatedAbility withAllowSharedTargets() {
        this.allowSharedTargets = true;
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

    /**
     * Narrows {@link #withActivatableByAnyPlayer()} to the controller of the permanent this Aura
     * is attached to (Volrath's Curse: "That creature's controller may sacrifice a permanent…").
     * Chain both flags: the any-player flag makes the ability reachable from a battlefield the
     * activator doesn't control, this one rejects every player except the enchanted permanent's
     * controller. Returns this ability for chaining in card constructors.
     */
    public ActivatedAbility withActivatableOnlyByEnchantedPermanentController() {
        this.activatableOnlyByEnchantedPermanentController = true;
        return this;
    }

    /**
     * Narrows {@link #withActivatableByAnyPlayer()} to the opponents of the source permanent's
     * controller (Soul Ransom: "Only your opponents may activate this ability."). Chain both flags:
     * the any-player flag makes the ability reachable from a battlefield the activator doesn't
     * control, this one rejects the source's own controller. Returns this ability for chaining in
     * card constructors.
     */
    public ActivatedAbility withActivatableOnlyByOpponents() {
        this.activatableOnlyByOpponents = true;
        return this;
    }

    public boolean isNeedsTarget() {
        return !multiTargetFilters.isEmpty()
                || effects.stream().anyMatch(e -> {
                    TargetSpec spec = e.targetSpec();
                    return spec.admits(TargetPredicate.Kind.PLAYER)
                            || spec.admits(TargetPredicate.Kind.PERMANENT)
                            || spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD);
                });
    }

    public boolean isMultiTarget() {
        return !multiTargetFilters.isEmpty();
    }

    /**
     * Fluent setter marking this ability's target count as scaling with the paid X (Runed Arch's
     * "X target creatures with power 2 or less"). Pair with an {@code {X}} mana cost, a single
     * {@code targetFilter}, {@code minTargets = 0} and a sanity {@code maxTargets} cap. Returns this
     * ability for chaining in card constructors.
     */
    public ActivatedAbility withXScaledTargets() {
        this.xScaledTargets = true;
        return this;
    }

    /** Marks the ability as requiring a player-chosen xValue for a dynamic non-mana cost. */
    public ActivatedAbility withXValue() {
        this.requiresXValue = true;
        return this;
    }

    /** As {@link #withXValue()}, but X is capped by the +1/+1 counters among all creatures you control. */
    public ActivatedAbility withXValueFromControlledCreatureCounters() {
        this.requiresXValue = true;
        this.xValueFromControlledCreatureCounters = true;
        return this;
    }

    /**
     * Minimum number of targets required for the given paid X. Mirrors {@code Card.getEffectiveMinTargets}:
     * an X-scaled group is declared with {@code minTargets = 0}, so fewer than X targets stay legal
     * when not enough legal targets exist.
     */
    public int getEffectiveMinTargets(int xValue) {
        return xScaledTargets ? Math.min(xValue, minTargets) : minTargets;
    }

    /** Maximum number of targets allowed for the given paid X ({@code min(X, maxTargets)} when X-scaled). */
    public int getEffectiveMaxTargets(int xValue) {
        return xScaledTargets ? Math.min(xValue, maxTargets) : maxTargets;
    }

    public boolean isNeedsSpellTarget() {
        return effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
    }

    /**
     * Whether this activation targets a spell on the stack. An ability that can only ever target a
     * spell (Spiketail Hatchling) has a single legal zone, so an activation may leave the zone unset
     * and still mean the stack. A dual "target spell or permanent" ability (Eight-and-a-Half-Tails)
     * carries a permanent target spec alongside the spell one, so only an explicit
     * {@link Zone#STACK} tells us the spell half was chosen.
     */
    public boolean targetsSpellOnStack(Zone requestedZone) {
        return isNeedsSpellTarget() && (requestedZone == Zone.STACK || isSpellOnlyTarget());
    }

    /**
     * True when no effect offers a permanent target as an alternative to the spell target. The dual
     * effects are exactly the ones that reach {@code targetsSpellOnStack} without declaring a spell
     * leaf — their spec describes the permanent half and the spell half rides on a dedicated record
     * component.
     */
    private boolean isSpellOnlyTarget() {
        return effects.stream()
                .filter(EffectResolution::targetsSpellOnStack)
                .allMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.SPELL));
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
