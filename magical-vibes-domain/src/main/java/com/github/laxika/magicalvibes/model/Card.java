package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.NinjutsuEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Card {

    private static final Map<String, OracleData> oracleRegistry = new ConcurrentHashMap<>();
    private static volatile OracleDataResolver oracleDataResolver;

    /**
     * Resolves oracle data for a card class whose data has not been registered yet.
     *
     * <p>Production startup eagerly registers every card and leaves this unset. The shared card-test
     * context temporarily retains an on-demand resolver as a compatibility path for tests that do
     * not yet preload their declared cards through {@code @CardUsed}.
     */
    @FunctionalInterface
    public interface OracleDataResolver {
        void resolve(Class<? extends Card> cardClass);
    }

    public static void registerOracle(String className, OracleData data) {
        oracleRegistry.put(className, data);
    }

    /**
     * Registers oracle data only if the class has none yet. Used for back-face registrations: a
     * back face may name a standalone card class (prepare-spell cards reuse the real spell's
     * class), and the face-level data must not clobber the richer data registered from that
     * card's own printing — Scryfall face nodes can lack fields such as colors.
     */
    public static void registerOracleIfAbsent(String className, OracleData data) {
        oracleRegistry.putIfAbsent(className, data);
    }

    public static void clearOracleRegistry() {
        oracleRegistry.clear();
    }

    public static void installOracleDataResolver(OracleDataResolver resolver) {
        oracleDataResolver = resolver;
    }

    public static void uninstallOracleDataResolver(OracleDataResolver resolver) {
        if (oracleDataResolver == resolver) {
            oracleDataResolver = null;
        }
    }

    private final UUID id;
    /**
     * When true, this card is live in a game (part of a deck, on the stack, or wrapped in a
     * {@link Permanent}) and must no longer be mutated: live Card instances are shared with AI
     * simulation copies ({@code GameData.simulationCopy()}), so any mutation would leak between
     * the real game and simulated games. Runtime state belongs on the {@code Permanent}, the
     * {@code StackEntry}, or {@code GameData} — or mutate a {@link #createRuntimeCopy()} instead.
     */
    @Getter(AccessLevel.NONE)
    private boolean frozen;
    /**
     * The player who owns this card — the player whose deck it started the game in. Stamped once
     * at game setup ({@code GameSetupService}) and preserved across zone changes. Cards created by
     * the engine (tokens, copies) leave this {@code null}. Distinct from control: used to evaluate
     * "a spell you don't own" (e.g. Nita, Forum Conciliator).
     */
    private UUID ownerId;
    private String name;
    private CardType type;
    private String manaCost;
    /** Cached parsed ManaCost, invalidated on setManaCost. */
    @Getter(AccessLevel.NONE)
    private ManaCost parsedManaCost;
    private CardColor color;
    private List<CardColor> colors = List.of();
    /**
     * Display-only colour identity, used to tint a land's frame. Never a rules characteristic: no
     * colour predicate may read it, because a land is colourless under CR 202.2.
     */
    private List<CardColor> colorIdentity = List.of();

    private Set<CardType> additionalTypes = Set.of();
    private Set<CardSupertype> supertypes = Set.of();
    private List<CardSubtype> subtypes = List.of();
    private String cardText;
    private Integer power;
    private Integer toughness;
    private Set<Keyword> keywords = Set.of();
    private Integer loyalty;
    /** Printed defense for Battle permanents (enters with that many defense counters). */
    private Integer defense;
    /**
     * When non-null/non-empty, mana spent on X must come only from these colors
     * (e.g. Consume Spirit = {BLACK}; Soul Burn = {BLACK, RED}).
     */
    private Set<ManaColor> xColorRestrictions;
    /**
     * Optional cast-time ceiling on announced X ("X can't be greater than …"). Evaluated against
     * the casting player when the spell is cast; null means mana alone caps X.
     */
    private DynamicAmount xValueCap;
    private String setCode;
    private String collectorNumber;

    private boolean token;
    /** "This spell can't be copied." Honored by the copy effect handlers. */
    private boolean cantBeCopied;
    /**
     * When true, the permanent this card becomes is registered for sacrifice at the beginning of
     * the next end step (e.g. the token created by copying a creature spell with Choreographed Sparks).
     */
    private boolean sacrificeAtEndStep;
    private boolean requiresCreatureMana;
    /**
     * When true, this Aura enchants a player even though it isn't a Curse (e.g. Wheel of Sun and
     * Moon). Curses are recognized automatically; non-Curse "Enchant player" auras must set this.
     */
    private boolean enchantPlayer;
    private int additionalCostPerExtraTarget;
    /** Additional mana symbols required for each target beyond the first. */
    private String additionalManaCostPerExtraTarget;
    /**
     * "This spell costs N life more to cast for each target" (Phyrexian Purge). Unlike
     * {@link #additionalCostPerExtraTarget} this applies to every chosen target, including the
     * first, and is paid in life rather than mana.
     */
    private int additionalLifeCostPerTarget;
    /**
     * When true, the same permanent may be chosen for different target groups (CR 114.6c).
     * By default, targets across groups must be distinct — matching the common MTG pattern
     * where separate "target" instances imply "another". Set this for cards whose oracle text
     * does NOT use "another" and whose target filters can overlap (e.g. "target creature" +
     * "target Merfolk", where a Merfolk satisfies both).
     */
    private boolean allowSharedTargets;
    /** Whether this card's targeted attack trigger is chosen by the defending player. */
    private boolean attackTriggerTargetChosenByDefendingPlayer;
    /**
     * Optional cross-target restriction on the whole set of chosen targets (e.g. Rivals' Duel's
     * "two target creatures that share no creature types"), checked at announcement in addition
     * to the per-position filters. Null for spells with no such restriction.
     */
    private MultiTargetConstraint multiTargetConstraint;

    // Target-first targeting system: each target() call adds a SpellTarget
    @Getter(AccessLevel.NONE)
    private final List<SpellTarget> spellTargets = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final Map<CardEffect, Integer> effectTargetIndexMap = new IdentityHashMap<>();
    // Runtime override set by modal spells (ChooseOneEffect) at cast time — only ever written on
    // an unfrozen runtime copy (see SpellCastingService's modal copy-on-cast)
    private TargetFilter castTimeTargetFilter;
    private String watermark;
    private Card backFaceCard;
    /** True when this card's face is chosen while it is played from a zone, rather than transformed. */
    private boolean modalDoubleFaced;
    private List<CastingOption> castingOptions = new ArrayList<>();
    /** Morph's face-up cost; the face-down cast uses the standard {3} alternate cost. */
    private String morphCost;
    /** Optional card-reveal component of a morph face-up cost. */
    private RevealCardsFromHandCastingCost morphRevealCost;
    /** Card-specific "cast this spell only when …" restriction, or null for normal timing. Defiant Stand. */
    private SpellCastTimingRestriction spellCastTimingRestriction;
    /**
     * Card-specific "cast this spell only if …" condition, evaluated for the caster when computing
     * playability, or null for no such restriction. Talara's Battalion ("only if you've cast another
     * green spell this turn").
     */
    private Condition castCondition;
    /**
     * Card-specific "you may cast this spell as though it had flash" condition, evaluated for the
     * caster when computing timing, or null for normal timing. The spell is still cast for its
     * normal cost — only the timing permission changes. Swift Reckoning (spell mastery).
     */
    private Condition flashCastCondition;

    /**
     * Card-specific "this Equipment can be attached only to …" restriction (Konda's Banner), or
     * null when any creature may be equipped. Unlike the equip ability's target filter this is a
     * continuous requirement: an Equipment attached to a permanent that stops matching becomes
     * unattached as a state-based action (CR 704.5n).
     */
    private PermanentPredicate attachRestriction;

    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, List<EffectRegistration>> effectRegistrations = new EnumMap<>(EffectSlot.class);
    /** Cached effect-only lists, invalidated on addEffect. */
    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, List<CardEffect>> effectCache = new EnumMap<>(EffectSlot.class);
    /** Per-chapter target filters for Saga cards (e.g. "target creature an opponent controls"). */
    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, Set<TargetFilter>> sagaChapterTargetFilters = new EnumMap<>(EffectSlot.class);
    private List<ActivatedAbility> activatedAbilities = new ArrayList<>();
    private List<ActivatedAbility> graveyardActivatedAbilities = new ArrayList<>();
    /** Abilities activatable while this card is in its owner's hand (e.g. Reinforce). */
    private List<ActivatedAbility> handActivatedAbilities = new ArrayList<>();

    public Card() {
        this.id = UUID.randomUUID();
        Class<? extends Card> cardClass = getClass().asSubclass(Card.class);
        String className = cardClass.getSimpleName();
        OracleData oracle = oracleRegistry.get(className);
        OracleDataResolver resolver = oracleDataResolver;
        if (oracle == null && resolver != null && cardClass != Card.class) {
            resolver.resolve(cardClass);
            oracle = oracleRegistry.get(className);
        }
        if (oracle != null) {
            this.name = oracle.name();
            this.type = oracle.type();
            this.additionalTypes = oracle.additionalTypes();
            this.manaCost = oracle.manaCost();
            this.color = oracle.color();
            this.colors = oracle.colors();
            this.colorIdentity = oracle.colorIdentity();
            this.supertypes = oracle.supertypes();
            this.subtypes = oracle.subtypes();
            this.cardText = oracle.cardText();
            this.power = oracle.power();
            this.toughness = oracle.toughness();
            this.keywords = oracle.keywords();
            this.loyalty = oracle.loyalty();
            this.defense = oracle.defense();
            this.watermark = oracle.watermark();
        }
    }

    /**
     * Copy constructor backing {@link #createRuntimeCopy()}. Copies every field, including the
     * id, but not {@link #frozen} — the copy starts mutable. Collection fields are copied into
     * fresh containers (elements shared: effects and abilities are immutable). The copied
     * {@link SpellTarget}s keep their back-reference to the source card; that reference is only
     * used by the construction-time builder API, never at runtime. Note the copy is a plain
     * {@code Card} — subclass identity (only used for oracle-registry lookup at construction
     * and {@code getBackFaceClassName()} at set load) is not preserved.
     *
     * <p>MAINTENANCE: when adding a field to Card, copy it here. {@code CardRuntimeCopyTest}
     * fails on any newly declared field to force this update.
     */
    protected Card(Card source) {
        this.id = source.id;
        this.ownerId = source.ownerId;
        this.name = source.name;
        this.type = source.type;
        this.manaCost = source.manaCost;
        this.parsedManaCost = source.parsedManaCost;
        this.color = source.color;
        this.colors = source.colors;
        this.colorIdentity = source.colorIdentity;
        this.additionalTypes = source.additionalTypes;
        this.supertypes = source.supertypes;
        this.subtypes = source.subtypes;
        this.cardText = source.cardText;
        this.power = source.power;
        this.toughness = source.toughness;
        this.keywords = source.keywords;
        this.loyalty = source.loyalty;
        this.defense = source.defense;
        this.xColorRestrictions = source.xColorRestrictions == null
                ? null
                : EnumSet.copyOf(source.xColorRestrictions);
        this.xValueCap = source.xValueCap;
        this.setCode = source.setCode;
        this.collectorNumber = source.collectorNumber;
        this.token = source.token;
        this.cantBeCopied = source.cantBeCopied;
        this.sacrificeAtEndStep = source.sacrificeAtEndStep;
        this.requiresCreatureMana = source.requiresCreatureMana;
        this.enchantPlayer = source.enchantPlayer;
        this.additionalCostPerExtraTarget = source.additionalCostPerExtraTarget;
        this.additionalManaCostPerExtraTarget = source.additionalManaCostPerExtraTarget;
        this.additionalLifeCostPerTarget = source.additionalLifeCostPerTarget;
        this.allowSharedTargets = source.allowSharedTargets;
        this.attackTriggerTargetChosenByDefendingPlayer = source.attackTriggerTargetChosenByDefendingPlayer;
        this.multiTargetConstraint = source.multiTargetConstraint;
        this.spellTargets.addAll(source.spellTargets);
        this.effectTargetIndexMap.putAll(source.effectTargetIndexMap);
        this.castTimeTargetFilter = source.castTimeTargetFilter;
        this.watermark = source.watermark;
        this.backFaceCard = source.backFaceCard;
        this.modalDoubleFaced = source.modalDoubleFaced;
        this.castingOptions = new ArrayList<>(source.castingOptions);
        this.morphCost = source.morphCost;
        this.morphRevealCost = source.morphRevealCost;
        this.spellCastTimingRestriction = source.spellCastTimingRestriction;
        this.castCondition = source.castCondition;
        this.flashCastCondition = source.flashCastCondition;
        this.attachRestriction = source.attachRestriction;
        source.effectRegistrations.forEach((slot, regs) ->
                this.effectRegistrations.put(slot, new ArrayList<>(regs)));
        // effectCache intentionally left empty — rebuilt lazily by getEffects()
        this.sagaChapterTargetFilters.putAll(source.sagaChapterTargetFilters);
        this.activatedAbilities = new ArrayList<>(source.activatedAbilities);
        this.graveyardActivatedAbilities = new ArrayList<>(source.graveyardActivatedAbilities);
        this.handActivatedAbilities = new ArrayList<>(source.handActivatedAbilities);
    }

    /**
     * Creates an unfrozen copy of this card with the same id, for flows that must write
     * cast-time state onto a card (modal spells choosing a mode, AI mode evaluation). The
     * copy replaces the original in the zone it is cast from and travels on from there;
     * the shared original is never mutated.
     */
    public Card createRuntimeCopy() {
        return new Card(this);
    }

    // ── Freeze guard ─────────────────────────────────────────────────

    /**
     * Marks this card as live: from now on every mutator throws. Called when the card joins
     * live game structures (deck stamping in {@code GameSetupService}, {@code Permanent} and
     * {@code StackEntry} construction). Idempotent.
     */
    public void freeze() {
        this.frozen = true;
    }

    private void assertMutable() {
        if (frozen) {
            throw new IllegalStateException("Card '" + name + "' (" + id + ") is frozen. Live cards are shared"
                    + " with AI simulation copies and must not be mutated — store runtime state on the Permanent,"
                    + " the StackEntry, or GameData, or mutate a createRuntimeCopy() instead.");
        }
    }

    // ── Guarded setters (hand-written instead of @Setter so assertMutable() runs) ──

    public void setOwnerId(UUID ownerId) { assertMutable(); this.ownerId = ownerId; }
    public void setName(String name) { assertMutable(); this.name = name; }
    public void setType(CardType type) { assertMutable(); this.type = type; }
    public void setColor(CardColor color) { assertMutable(); this.color = color; }
    public void setColors(List<CardColor> colors) { assertMutable(); this.colors = colors; }
    public void setAdditionalTypes(Set<CardType> additionalTypes) { assertMutable(); this.additionalTypes = additionalTypes; }
    public void setSupertypes(Set<CardSupertype> supertypes) { assertMutable(); this.supertypes = supertypes; }
    public void setSubtypes(List<CardSubtype> subtypes) { assertMutable(); this.subtypes = subtypes; }
    public void setCardText(String cardText) { assertMutable(); this.cardText = cardText; }
    public void setPower(Integer power) { assertMutable(); this.power = power; }
    public void setToughness(Integer toughness) { assertMutable(); this.toughness = toughness; }
    public void setKeywords(Set<Keyword> keywords) { assertMutable(); this.keywords = keywords; }
    public void setLoyalty(Integer loyalty) { assertMutable(); this.loyalty = loyalty; }
    public void setDefense(Integer defense) { assertMutable(); this.defense = defense; }
    /** Restrict X to a single color (Consume Spirit). */
    public void setXColorRestriction(ManaColor xColorRestriction) {
        assertMutable();
        this.xColorRestrictions = xColorRestriction == null ? null : EnumSet.of(xColorRestriction);
    }

    /** Restrict X to one or more colors (Soul Burn: black and/or red). */
    public void setXColorRestrictions(ManaColor first, ManaColor... rest) {
        assertMutable();
        EnumSet<ManaColor> colors = EnumSet.of(first);
        if (rest != null) {
            Collections.addAll(colors, rest);
        }
        this.xColorRestrictions = colors;
    }

    /** Copy/replace the full X-color restriction set (null clears). */
    public void setXColorRestrictions(Set<ManaColor> xColorRestrictions) {
        assertMutable();
        this.xColorRestrictions = xColorRestrictions == null || xColorRestrictions.isEmpty()
                ? null
                : EnumSet.copyOf(xColorRestrictions);
    }

    public boolean hasXColorRestriction() {
        return xColorRestrictions != null && !xColorRestrictions.isEmpty();
    }
    public void setXValueCap(DynamicAmount xValueCap) { assertMutable(); this.xValueCap = xValueCap; }
    public void setSetCode(String setCode) { assertMutable(); this.setCode = setCode; }
    public void setCollectorNumber(String collectorNumber) { assertMutable(); this.collectorNumber = collectorNumber; }
    public void setToken(boolean token) { assertMutable(); this.token = token; }
    public void setCantBeCopied(boolean cantBeCopied) { assertMutable(); this.cantBeCopied = cantBeCopied; }
    public void setSacrificeAtEndStep(boolean sacrificeAtEndStep) { assertMutable(); this.sacrificeAtEndStep = sacrificeAtEndStep; }
    public void setRequiresCreatureMana(boolean requiresCreatureMana) { assertMutable(); this.requiresCreatureMana = requiresCreatureMana; }
    public void setEnchantPlayer(boolean enchantPlayer) { assertMutable(); this.enchantPlayer = enchantPlayer; }
    public void setAdditionalCostPerExtraTarget(int additionalCostPerExtraTarget) { assertMutable(); this.additionalCostPerExtraTarget = additionalCostPerExtraTarget; }
    public void setAdditionalManaCostPerExtraTarget(String additionalManaCostPerExtraTarget) { assertMutable(); this.additionalManaCostPerExtraTarget = additionalManaCostPerExtraTarget; }
    public void setAdditionalLifeCostPerTarget(int additionalLifeCostPerTarget) { assertMutable(); this.additionalLifeCostPerTarget = additionalLifeCostPerTarget; }
    public void setAllowSharedTargets(boolean allowSharedTargets) { assertMutable(); this.allowSharedTargets = allowSharedTargets; }
    public void setAttackTriggerTargetChosenByDefendingPlayer(boolean chosenByDefendingPlayer) {
        assertMutable();
        this.attackTriggerTargetChosenByDefendingPlayer = chosenByDefendingPlayer;
    }
    public void setMultiTargetConstraint(MultiTargetConstraint multiTargetConstraint) { assertMutable(); this.multiTargetConstraint = multiTargetConstraint; }
    public void setCastTimeTargetFilter(TargetFilter castTimeTargetFilter) { assertMutable(); this.castTimeTargetFilter = castTimeTargetFilter; }
    public void setSpellCastTimingRestriction(SpellCastTimingRestriction spellCastTimingRestriction) { assertMutable(); this.spellCastTimingRestriction = spellCastTimingRestriction; }
    public void setCastCondition(Condition castCondition) { assertMutable(); this.castCondition = castCondition; }
    public void setFlashCastCondition(Condition flashCastCondition) { assertMutable(); this.flashCastCondition = flashCastCondition; }
    public void setAttachRestriction(PermanentPredicate attachRestriction) { assertMutable(); this.attachRestriction = attachRestriction; }
    public void setWatermark(String watermark) { assertMutable(); this.watermark = watermark; }
    public void setBackFaceCard(Card backFaceCard) { assertMutable(); this.backFaceCard = backFaceCard; }
    public void setModalDoubleFaced(boolean modalDoubleFaced) { assertMutable(); this.modalDoubleFaced = modalDoubleFaced; }

    // ── Target-first builder API ──────────────────────────────────────

    /**
     * Declares a required target (min=1, max=1) and returns a builder
     * whose {@code addEffect()} associates effects with this target.
     */
    public SpellTarget target(TargetFilter filter) {
        return target(filter, 1, 1);
    }

    /**
     * Declares an unfiltered target with custom min/max counts.
     */
    public SpellTarget target(int minTargets, int maxTargets) {
        return target(null, minTargets, maxTargets);
    }

    /**
     * Declares a target with custom min/max counts and returns a builder
     * whose {@code addEffect()} associates effects with this target.
     */
    public SpellTarget target(TargetFilter filter, int minTargets, int maxTargets) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, minTargets, maxTargets, spellTargets.size());
        spellTargets.add(st);
        return st;
    }

    /**
     * Declares a target group whose bounds change when the spell is kicked.
     * The ordinary bounds apply when the spell is not kicked; the kicker bounds apply when it is.
     */
    public SpellTarget targetWhenKicked(TargetFilter filter, int minTargets, int maxTargets,
                                        int kickedMinTargets, int kickedMaxTargets) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, minTargets, maxTargets,
                kickedMinTargets, kickedMaxTargets, spellTargets.size(), false, null);
        spellTargets.add(st);
        return st;
    }

    /**
     * Declares a target group whose target count scales with the spell's X value
     * ("Destroy X target nonblack creatures" — Dregs of Sorrow). The effective number of
     * targets is bounded by X at cast time; {@code cap} is only a sanity ceiling.
     */
    public SpellTarget targetX(TargetFilter filter, int cap) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, 0, cap, spellTargets.size(), true);
        spellTargets.add(st);
        return st;
    }

    /**
     * Declares exactly X targets for a spell's X value. The cap is a sanity ceiling for target
     * position handling and should be at least as large as any practical X value.
     */
    public SpellTarget targetExactlyX(TargetFilter filter, int cap) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, cap, cap, spellTargets.size(), true);
        spellTargets.add(st);
        return st;
    }

    /**
     * Declares an optional target group whose maximum is evaluated when an ETB trigger is put on
     * the stack. The fixed cap is only a sanity ceiling for target-position handling.
     */
    public SpellTarget targetUpTo(DynamicAmount dynamicMaxTargets, TargetFilter filter, int cap) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, 0, cap, spellTargets.size(), false, dynamicMaxTargets);
        spellTargets.add(st);
        return st;
    }

    /**
     * Called by {@link SpellTarget#addEffect} to map an effect instance to its target index.
     * Wrapper effects (conditional, may) register their inner effects under the same index,
     * because resolution unwraps them before dispatching to the handler — the handler must be
     * able to look up the group by the effect instance it actually receives.
     */
    public void registerEffectTargetIndex(CardEffect effect, int targetIndex) {
        if (effect == null) {
            return;
        }
        assertMutable();
        effectTargetIndexMap.put(effect, targetIndex);
        switch (effect) {
            case ConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case ConditionalReplacementEffect e -> {
                if (e.baseEffect() != null) registerEffectTargetIndex(e.baseEffect(), targetIndex);
                registerEffectTargetIndex(e.upgradedEffect(), targetIndex);
            }
            case MayEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case MayPayManaEffect e -> {
                if (e.wrapped() != null) registerEffectTargetIndex(e.wrapped(), targetIndex);
                if (e.elseEffect() != null) registerEffectTargetIndex(e.elseEffect(), targetIndex);
            }
            case MayPayTapPermanentsEffect e -> {
                if (e.wrapped() != null) registerEffectTargetIndex(e.wrapped(), targetIndex);
                if (e.elseEffect() != null) registerEffectTargetIndex(e.elseEffect(), targetIndex);
            }
            // SequenceEffect splices its steps into the resolution list; each step must keep the
            // sequence's target group (fuse halves that bundle multi-step one-target instructions).
            case SequenceEffect e -> {
                for (CardEffect step : e.steps()) {
                    registerEffectTargetIndex(step, targetIndex);
                }
            }
            case SpellCastTriggerEffect e -> {
                for (CardEffect resolvedEffect : e.resolvedEffects()) {
                    registerEffectTargetIndex(resolvedEffect, targetIndex);
                }
            }
            // Triggering conditionals (e.g. Diregraf Captain's "whenever another Zombie you control
            // dies") are unwrapped to their inner effect when the trigger is serviced, so the inner
            // effect must resolve to the same declared target group — otherwise the card-level target
            // filter (e.g. opponent-only) is lost after unwrapping.
            case TriggeringCardConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case TriggeringPermanentConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case TriggeringArtifactControllerConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case TriggeringPermanentControllerConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            default -> { }
        }
    }

    /**
     * Appends another spell's target declarations and effect bindings to this card.
     * Splice adds the other spell's effects to the host spell, so its target groups must be added
     * to the host's cast-time target layout as well.
     */
    public void appendSpellTargetingFrom(Card source) {
        assertMutable();
        int targetIndexOffset = spellTargets.size();
        for (SpellTarget sourceTarget : source.spellTargets) {
            SpellTarget target = new SpellTarget(
                    this,
                    sourceTarget.getFilter(),
                    sourceTarget.getMinTargets(),
                    sourceTarget.getMaxTargets(),
                    sourceTarget.getKickedMinTargets(),
                    sourceTarget.getKickedMaxTargets(),
                    targetIndexOffset + sourceTarget.getIndex(),
                    sourceTarget.isXScaled(),
                    sourceTarget.getDynamicMaxTargets());
            spellTargets.add(target);
        }
        source.effectTargetIndexMap.forEach((effect, targetIndex) ->
                registerEffectTargetIndex(effect, targetIndexOffset + targetIndex));
    }

    /**
     * Clears runtime target-first declarations. Used by modal spells (ChooseOneEffect) whose chosen
     * mode declares its own {@code target()} slots at cast time, so re-casting the same card instance
     * does not accumulate stale target declarations.
     */
    public void clearRuntimeSpellTargets() {
        assertMutable();
        spellTargets.clear();
        effectTargetIndexMap.clear();
    }

    // ── Derived targeting getters (replace old stored fields) ────────

    /**
     * Returns the target filter for single-target spells, or the first target's
     * filter for multi-target spells. For modal spells, returns the cast-time override.
     */
    public TargetFilter getTargetFilter() {
        if (castTimeTargetFilter != null) return castTimeTargetFilter;
        return getDeclaredTargetFilter();
    }

    /**
     * Returns the target filter declared by the card's target group, ignoring any cast-time
     * override used by a triggered ability or a modal choice. Aura attachment legality uses this
     * filter because a cast-time target restriction is not an ongoing enchant restriction.
     */
    public TargetFilter getDeclaredTargetFilter() {
        if (spellTargets.isEmpty()) return null;
        return spellTargets.getFirst().getFilter();
    }

    /**
     * Returns per-position target filters for multi-target spells.
     * Each target group's filter is repeated for its maxTargets count,
     * so that position-based validation matches the correct filter
     * when a group allows multiple targets (e.g. "up to 2 target creatures").
     */
    public List<TargetFilter> getMultiTargetFilters() {
        List<TargetFilter> expanded = new ArrayList<>();
        for (SpellTarget st : spellTargets) {
            for (int i = 0; i < Math.max(st.getMaxTargets(), st.getKickedMaxTargets()); i++) {
                expanded.add(st.getFilter());
            }
        }
        return expanded;
    }

    /**
     * Returns the minimum total number of targets required.
     */
    public int getMinTargets() {
        return spellTargets.stream().mapToInt(SpellTarget::getMinTargets).sum();
    }

    /**
     * Returns the maximum total number of targets allowed.
     */
    public int getMaxTargets() {
        return spellTargets.stream()
                .mapToInt(st -> Math.max(st.getMaxTargets(), st.getKickedMaxTargets()))
                .sum();
    }

    /**
     * Returns true if any target group scales its target count with the spell's X value.
     */
    public boolean hasXScaledTargets() {
        return spellTargets.stream().anyMatch(SpellTarget::isXScaled);
    }

    public boolean hasDynamicTargetCount() {
        return spellTargets.stream().anyMatch(st -> st.getDynamicMaxTargets() != null);
    }

    /**
     * Returns the minimum total number of targets required for the given X value.
     * X-scaled groups contribute {@code min(xValue, minTargets)}; others contribute their static minimum.
     */
    public int getEffectiveMinTargets(int xValue) {
        return getEffectiveMinTargets(xValue, false);
    }

    /** Returns the minimum total number of targets for the given X value and kicker state. */
    public int getEffectiveMinTargets(int xValue, boolean kicked) {
        return spellTargets.stream()
                .mapToInt(st -> {
                    int min = kicked ? st.getKickedMinTargets() : st.getMinTargets();
                    return st.isXScaled() ? Math.min(xValue, min) : min;
                })
                .sum();
    }

    /**
     * Returns the maximum total number of targets allowed for the given X value.
     * X-scaled groups contribute {@code min(xValue, maxTargets)}; others contribute their static maximum.
     */
    public int getEffectiveMaxTargets(int xValue) {
        return getEffectiveMaxTargets(xValue, false);
    }

    /** Returns the maximum total number of targets for the given X value and kicker state. */
    public int getEffectiveMaxTargets(int xValue, boolean kicked) {
        return spellTargets.stream()
                .mapToInt(st -> {
                    int max = kicked ? st.getKickedMaxTargets() : st.getMaxTargets();
                    return st.isXScaled() ? Math.min(xValue, max) : max;
                })
                .sum();
    }

    /**
     * Returns the spell target declarations.
     */
    public List<SpellTarget> getSpellTargets() {
        return spellTargets;
    }

    /**
     * Returns the target index for the given effect instance, or -1 if not mapped.
     */
    public int getEffectTargetIndex(CardEffect effect) {
        return effectTargetIndexMap.getOrDefault(effect, -1);
    }

    /**
     * True if any spell effect is bound (via {@code target(...).addEffect(...)}) to the given target
     * group. A group with no bound effect is a bare positional target that another effect reads by
     * index (e.g. Blood Feud's first fight target, read by the {@code FightTargetsEffect} bound to the
     * second group); such a group is never a gated-out trigger group.
     */
    public boolean bindsEffectToTargetGroup(int groupIndex) {
        return effectTargetIndexMap.containsValue(groupIndex);
    }

    /**
     * Returns true if the target group at the given expanded position allows player targets.
     * Used by the valid target service to determine per-position player targeting in multi-target spells.
     *
     * <p>Bound effects win when their {@code targetSpec()} includes players. Bare positional groups
     * (no bound effect — e.g. Injury's creature + player/planeswalker slots feeding
     * {@code DealDamageToEachTargetEffect}) fall back to the group's declared filter.
     */
    public boolean doesPositionAllowPlayerTargets(int expandedPosition) {
        if (spellTargets.isEmpty()) return false;
        int cumulative = 0;
        for (SpellTarget st : spellTargets) {
            cumulative += st.getMaxTargets();
            if (expandedPosition < cumulative) {
                int groupIndex = st.getIndex();
                for (Map.Entry<CardEffect, Integer> entry : effectTargetIndexMap.entrySet()) {
                    if (entry.getValue() == groupIndex && entry.getKey().targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                        return true;
                    }
                }
                TargetFilter filter = st.getFilter();
                return filter instanceof PlayerPredicateTargetFilter
                        || filter instanceof AnyTargetPredicateTargetFilter;
            }
        }
        return false;
    }

    /**
     * Copies targeting configuration from another card (used by spell copy effects).
     */
    public void copyTargetingFrom(Card original) {
        assertMutable();
        for (SpellTarget st : original.spellTargets) {
            spellTargets.add(new SpellTarget(this, st.getFilter(), st.getMinTargets(), st.getMaxTargets(), st.getIndex(),
                    st.isXScaled(), st.getDynamicMaxTargets()));
        }
        effectTargetIndexMap.putAll(original.effectTargetIndexMap);
        castTimeTargetFilter = original.castTimeTargetFilter;
    }

    // ── Effect management ───────────────────────────────────────────

    public List<CardEffect> getEffects(EffectSlot slot) {
        List<CardEffect> cached = effectCache.get(slot);
        if (cached != null) return cached;
        List<EffectRegistration> regs = effectRegistrations.get(slot);
        if (regs == null) return List.of();
        List<CardEffect> effects = new ArrayList<>(regs.size());
        for (EffectRegistration reg : regs) {
            effects.add(reg.effect());
        }
        List<CardEffect> unmodifiable = Collections.unmodifiableList(effects);
        effectCache.put(slot, unmodifiable);
        return unmodifiable;
    }

    public List<EffectRegistration> getEffectRegistrations(EffectSlot slot) {
        return effectRegistrations.getOrDefault(slot, List.of());
    }

    public void removeKeyword(Keyword keyword) {
        assertMutable();
        if (keywords.contains(keyword)) {
            var mutable = EnumSet.copyOf(keywords);
            mutable.remove(keyword);
            this.keywords = mutable;
        }
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        assertMutable();
        validateEffectSlotType(slot, effect);
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect));
        effectCache.remove(slot);
    }

    public void addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        assertMutable();
        validateEffectSlotType(slot, effect);
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect, triggerMode));
        effectCache.remove(slot);
    }

    private void validateEffectSlotType(EffectSlot slot, CardEffect effect) {
        if (slot == EffectSlot.STATE_TRIGGERED && !(effect instanceof StateTriggerEffect)) {
            throw new IllegalArgumentException(
                    "STATE_TRIGGERED slot requires StateTriggerEffect, got " + effect.getClass().getSimpleName());
        }
    }

    public void addCastingOption(CastingOption option) {
        assertMutable();
        castingOptions.add(option);
    }

    /** Adds a prototype alternate cast with its alternate color and base power/toughness. */
    public void addPrototype(String manaCost, CardColor color, int power, int toughness) {
        assertMutable();
        addCastingOption(AlternateHandCast.prototype(manaCost, color, power, toughness));
    }

    /** Adds morph and its standard face-down alternate casting cost. */
    public void addMorph(String morphCost) {
        assertMutable();
        this.morphCost = morphCost;
        this.morphRevealCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph with a card that must be revealed from hand as part of the face-down cost. */
    public void addMorph(String morphCost, CardPredicate revealPredicate, String revealLabel) {
        assertMutable();
        this.morphCost = morphCost;
        this.morphRevealCost = null;
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{3}"),
                new RevealCardsFromHandCastingCost(revealPredicate, revealLabel))));
    }

    /** Adds morph whose face-up cost is revealing a matching card from hand. */
    public void addMorphWithRevealCost(CardPredicate revealPredicate, String revealLabel) {
        assertMutable();
        this.morphCost = "{0}";
        this.morphRevealCost = new RevealCardsFromHandCastingCost(revealPredicate, revealLabel);
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    public <T extends CastingOption> Optional<T> getCastingOption(Class<T> type) {
        return castingOptions.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    /**
     * Flashback casting option on this card, or on its back face when only the back half is
     * castable from the graveyard (aftermath-style split cards such as Farm // Market).
     */
    public Optional<FlashbackCast> effectiveFlashbackCast() {
        Optional<FlashbackCast> own = getCastingOption(FlashbackCast.class);
        if (own.isPresent()) {
            return own;
        }
        return backFaceCard != null
                ? backFaceCard.getCastingOption(FlashbackCast.class)
                : Optional.empty();
    }

    /**
     * Half whose SPELL effects, type line, and targeting apply when casting via
     * {@link #effectiveFlashbackCast()}. For aftermath splits this is the back face; otherwise
     * this card. The physical card object in the graveyard / on the stack remains {@code this}
     * so exile disposition still moves the parent split card.
     */
    public Card graveyardCastHalf() {
        if (getCastingOption(FlashbackCast.class).isPresent()) {
            return this;
        }
        if (backFaceCard != null && backFaceCard.getCastingOption(FlashbackCast.class).isPresent()) {
            return backFaceCard;
        }
        return this;
    }

    public void setSagaChapterTargetFilter(EffectSlot slot, Set<TargetFilter> filters) {
        assertMutable();
        sagaChapterTargetFilters.put(slot, filters);
    }

    public Set<TargetFilter> getSagaChapterTargetFilters(EffectSlot slot) {
        return sagaChapterTargetFilters.getOrDefault(slot, Set.of());
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        activatedAbilities.add(ability);
    }

    public void addGraveyardActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        graveyardActivatedAbilities.add(ability);
    }

    public void addHandActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        handActivatedAbilities.add(ability);
    }

    /**
     * Adds plain cycling for {@code cost} — "{@code Cost, Discard this card: Draw a card.}"
     *
     * <p>Both halves of this matter to the engine, which is why it is one call: cycling is only
     * activatable from hand, and {@link ActivatedAbility#isCyclingAbility()} recognises it by the
     * description beginning with "Cycling", so the reminder text is built from the cost here
     * rather than retyped per card.
     *
     * <p>Cycling that does something extra as it resolves (the Sojourners and Resounding cycles,
     * Deem Worthy) is a different ability — build those with
     * {@link #addHandActivatedAbility(ActivatedAbility)} and list the extra effect ahead of the
     * draw. Typecycling and landcycling likewise search rather than draw.
     */
    public void addCycling(String cost) {
        addHandActivatedAbility(new ActivatedAbility(false, cost,
                List.of(new DrawCardEffect(1)),
                "Cycling " + cost + " (" + cost + ", Discard this card: Draw a card.)"));
    }

    /**
     * Adds ninjutsu for {@code cost} — "{@code Cost, Reveal this card from your hand, Return an
     * unblocked attacking creature you control to its owner's hand: Put this card onto the
     * battlefield from your hand tapped and attacking.}" (CR 702.49a).
     *
     * <p>Like cycling this is one call because both halves matter to the engine: ninjutsu functions
     * only from hand, and {@link ActivatedAbility#isNinjutsuAbility()} is what makes the activation
     * path pay the return-an-attacker cost, leave the source in hand, and enter it attacking the
     * same defender the returned creature was attacking (CR 702.49c).
     */
    public void addNinjutsu(String cost) {
        addHandActivatedAbility(new ActivatedAbility(false, cost,
                List.of(new NinjutsuEffect()),
                "Ninjutsu " + cost + " (" + cost + ", Return an unblocked attacker you control to hand: "
                        + "Put this card onto the battlefield from your hand tapped and attacking.)")
                .withNinjutsu());
    }

    /**
     * Adds unearth for {@code cost}: return this card from the graveyard to the battlefield with
     * haste, exiled at the beginning of the next end step, sorcery speed only.
     *
     * <p>Only for a card that has unearth itself. Sedris, the Traitor King grants unearth to
     * <em>other</em> creatures and is a different effect entirely.
     */
    public void addUnearth(String cost) {
        addGraveyardActivatedAbility(new ActivatedAbility(false, cost,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .exileIfLeavesBattlefield(true)
                        .unearth(true)
                        .build()),
                "Unearth " + cost,
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /**
     * Adds embalm for {@code cost}: exile this card from the graveyard to create a token copy that
     * is a white Zombie with no mana cost, sorcery speed only.
     *
     * @param creatureTypes the card's own creature types as they read in the reminder text
     *                      ("Human Cleric", "Bird Warrior"). They cannot be derived here — the
     *                      subtypes are loaded from Scryfall after the constructor runs.
     */
    public void addEmbalm(String cost, String creatureTypes) {
        addGraveyardActivatedAbility(new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)),
                "Embalm " + cost + " (" + cost + ", Exile this card from your graveyard: Create a token "
                        + "that's a copy of it, except it's a white Zombie " + creatureTypes
                        + " with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /**
     * Builds an embalm graveyard-activated ability for {@code cost}. This form is used when a
     * permanent grants embalm to a card whose own creature types are not available in the granting
     * permanent's constructor.
     */
    public static ActivatedAbility embalmAbility(String cost) {
        return new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)),
                "Embalm " + cost + " (" + cost + ", Exile this card from your graveyard: Create a token "
                        + "that's a copy of it, except it's a white Zombie with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED);
    }

    /**
     * Adds eternalize for {@code cost}: like {@link #addEmbalm}, except the token is a 4/4 black
     * Zombie.
     *
     * @param creatureTypes the card's own creature types as they read in the reminder text
     */
    public void addEternalize(String cost, String creatureTypes) {
        addGraveyardActivatedAbility(new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)),
                "Eternalize " + cost + " (" + cost + ", Exile this card from your graveyard: Create a token "
                        + "that's a copy of it, except it's a 4/4 black Zombie " + creatureTypes
                        + " with no mana cost. Eternalize only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /**
     * Adds scavenge for {@code cost} (CR 702.97a): "{@code Cost}, Exile this card from your
     * graveyard: Put a number of +1/+1 counters equal to the power of the card you exiled on target
     * creature. Activate only as a sorcery."
     *
     * <p>The count is {@link SourceCardPower} rather than {@code SourcePower} because the scavenged
     * card is exiled as an activation cost and never was a permanent — the number comes from the
     * card itself.
     */
    public void addScavenge(String cost) {
        addGraveyardActivatedAbility(scavengeAbility(cost));
    }

    /**
     * Builds the scavenge graveyard-activated ability for {@code cost}. Shared by {@link
     * #addScavenge(String)} and by static effects that grant scavenge to cards in a graveyard
     * (Varolz, the Scar-Striped grants it with a cost equal to each card's mana cost).
     */
    public static ActivatedAbility scavengeAbility(String cost) {
        return new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new SourceCardPower())),
                "Scavenge " + cost + " (" + cost + ", Exile this card from your graveyard: Put a number of "
                        + "+1/+1 counters equal to this card's power on target creature. Scavenge only as a sorcery.)",
                TargetFilters.creature(), null, null, ActivationTimingRestriction.SORCERY_SPEED);
    }

    public String getBackFaceClassName() {
        return null;
    }

    public void setManaCost(String manaCost) {
        assertMutable();
        this.manaCost = manaCost;
        this.parsedManaCost = null;
    }

    /**
     * Returns a cached parsed {@link ManaCost} for this card's mana cost string.
     * Returns {@code null} if this card has no mana cost.
     */
    public ManaCost getParsedManaCost() {
        if (manaCost == null) return null;
        ManaCost cached = parsedManaCost;
        if (cached == null) {
            cached = new ManaCost(manaCost);
            parsedManaCost = cached;
        }
        return cached;
    }

    public int getManaValue() {
        ManaCost cost = getParsedManaCost();
        return cost != null ? cost.getManaValue() : 0;
    }

    public boolean hasType(CardType cardType) {
        return type == cardType || additionalTypes.contains(cardType);
    }

    public boolean isAura() {
        return subtypes.contains(CardSubtype.AURA);
    }

    public boolean isSaga() {
        return subtypes.contains(CardSubtype.SAGA);
    }

    /**
     * Returns the final chapter number for a Saga card (e.g. 3 for a three-chapter Saga).
     * Returns 0 if the card has no chapter abilities.
     */
    public int getSagaFinalChapter() {
        if (!getEffects(EffectSlot.SAGA_CHAPTER_III).isEmpty()) return 3;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_II).isEmpty()) return 2;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_I).isEmpty()) return 1;
        return 0;
    }

    public boolean isEnchantPlayer() {
        return isAura() && (subtypes.contains(CardSubtype.CURSE) || enchantPlayer);
    }

}
