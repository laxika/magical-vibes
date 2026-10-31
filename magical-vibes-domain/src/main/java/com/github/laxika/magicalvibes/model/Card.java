package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.AllCardNamesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.NinjutsuEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.RollDiceEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Getter
public class Card {

    private static final Map<String, OracleData> oracleRegistry = new ConcurrentHashMap<>();
    private static final Map<String, OracleData> embeddedOracleRegistry = new ConcurrentHashMap<>();
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

    /** Keeps card-specific oracle data available across test registry resets. */
    public static void registerEmbeddedOracle(String className, OracleData data) {
        embeddedOracleRegistry.put(className, data);
        oracleRegistry.putIfAbsent(className, data);
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
        oracleRegistry.putAll(embeddedOracleRegistry);
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
     * at game setup ({@code GameSetupService}) and preserved across zone changes. Engine-created
     * cards normally leave this {@code null}; token cards created for a player's library are owned
     * by that player. Distinct from control: used to evaluate "a spell you don't own" (e.g. Nita,
     * Forum Conciliator).
     */
    private UUID ownerId;
    private String name;
    private CardType type;
    private String manaCost;
    private Integer combinedManaValue;
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
    /** Each half of a split card with only its own characteristics (CR 709.3b); empty otherwise. */
    private List<OracleData> splitHalves = List.of();
    /**
     * The whole split card, with both halves' combined characteristics, that this runtime copy was
     * cast from as one half; null otherwise. Only the cast half exists on the stack (CR 709.3b), but
     * the card has its combined characteristics again in every other zone (CR 709.4).
     */
    private Card splitWholeCard;

    private Set<CardType> additionalTypes = Set.of();
    private Set<CardSupertype> supertypes = Set.of();
    private List<CardSubtype> subtypes = List.of();
    private String cardText;
    private Integer power;
    private Integer toughness;
    private Set<Keyword> keywords = Set.of();
    /** Persistent starting intensity for digital cards that use the intensity mechanic. */
    private int startingIntensity;
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
    private int minimumXValue;
    private String setCode;
    private String collectorNumber;

    private boolean token;
    /** When true, this Saga adds its lore counter at the beginning of its controller's end step. */
    private boolean bedtimeStory;
    /**
     * A token that is also a card, such as a Time Walk token card. Unlike ordinary tokens, these
     * remain in zones other than the battlefield.
     */
    private boolean tokenCard;
    /**
     * A card created by conjure (Alchemy). Conjured cards are not tokens: they count as nontoken,
     * persist in every zone like any other card, and are owned by the player who conjured them.
     */
    private boolean conjured;
    /** "This spell can't be copied." Honored by the copy effect handlers. */
    private boolean cantBeCopied;
    /**
     * When true, the permanent this card becomes is registered for sacrifice at the beginning of
     * the next end step (e.g. the token created by copying a creature spell with Choreographed Sparks).
     */
    private boolean sacrificeAtEndStep;
    private boolean requiresCreatureMana;
    private boolean requiresBasicLandMana;
    private boolean requiresNoMana;
    /**
     * When true, this Aura enchants a player even though it isn't a Curse (e.g. Wheel of Sun and
     * Moon). Curses are recognized automatically; non-Curse "Enchant player" auras must set this.
     */
    private boolean enchantPlayer;
    /** When true, this Aura enchants the shared zone rather than a player or permanent. */
    private boolean enchantZone;
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
     * When true, the same permanent may be chosen for different target groups (CR 601.2c).
     * By default, targets across groups must be distinct — matching the common MTG pattern
     * where separate "target" instances imply "another". Set this for cards whose oracle text
     * does NOT use "another" and whose target filters can overlap (e.g. "target creature" +
     * "target Merfolk", where a Merfolk satisfies both).
     */
    private boolean allowSharedTargets;
    /** Expanded target positions chosen by an opponent during this spell's announcement. */
    private List<Integer> opponentChosenSpellTargetIndices = List.of();
    /** Whether this card's targeted attack trigger is chosen by the defending player. */
    private boolean attackTriggerTargetChosenByDefendingPlayer;
    /**
     * Whether this Aura's {@code ON_ATTACK} trigger is the Aura's own ability ("Whenever enchanted creature
     * attacks, ...", controlled by the Aura's controller) rather than an ability it grants to the enchanted
     * creature ("Enchanted creature has 'Whenever this creature attacks, ...'", controlled by the creature's
     * controller).
     */
    private boolean attackTriggerOwnedByAura;
    /**
     * Whether this Equipment's {@code ON_ATTACK} trigger is an ability it grants to the equipped creature
     * ("Equipped creature has 'Whenever this creature attacks, ...'", controlled by the creature's
     * controller) rather than the Equipment's own ("Whenever equipped creature attacks, ...").
     */
    private boolean attackTriggerGrantedByEquipment;
    /**
     * Optional cross-target restriction on the whole set of chosen targets (e.g. Rivals' Duel's
     * "two target creatures that share no creature types"), checked at announcement in addition
     * to the per-position filters. Null for spells with no such restriction.
     */
    private MultiTargetConstraint multiTargetConstraint;

    /**
     * With {@link MultiTargetConstraint#AT_MOST_ONE_PER_CONTROLLER}, marks targets that are tied to a
     * specific player ("for each opponent, choose up to one target creature that player controls", e.g.
     * Decoy Gambit): each target's controller is recorded at cast and a target that changed controller
     * is illegal on resolution. Leave false for "with different controllers" wording, where a control
     * change alone doesn't break the requirement.
     */
    private boolean targetControllersFixedAtCast;

    /**
     * The target groups in the order {@code target()} declared them; see {@link #getTargetGroups()}.
     * The spell ability's groups come first and are mirrored in it ({@link SpellAbility#targets()}).
     */
    @Getter(AccessLevel.NONE)
    private final List<TargetGroup> targetGroups = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final Map<CardEffect, List<Integer>> effectTargetIndexMap = new IdentityHashMap<>();
    // Runtime override set by modal spells (ChooseOneEffect) at cast time — only ever written on
    // an unfrozen runtime copy (see SpellCastingService's modal copy-on-cast)
    private TargetFilter castTimeTargetFilter;
    private String watermark;
    private Card backFaceCard;
    /** True when this card's face is chosen while it is played from a zone, rather than transformed. */
    private boolean modalDoubleFaced;
    /** True when this card is one half of a meld pair (CR 712.4), a kind of double-faced card that can't transform. */
    private boolean meldCard;
    private List<CastingOption> castingOptions = new ArrayList<>();
    /** Mana costs of the two Room doors, in door order, when this card is a Room. */
    private List<String> roomDoorManaCosts = List.of();
    /** The Room door chosen while this card was cast, carried to its entering permanent. */
    private Integer selectedRoomDoor;
    /** Morph's face-up cost; the face-down cast uses the standard {3} alternate cost. */
    private String morphCost;
    /** Whether the morph ability casts this card as a spell from the battlefield. */
    private boolean spellmorph;
    /** Optional dynamic generic reduction applied to the morph/disguise face-up cost. */
    private DynamicAmount morphCostReduction;
    /** Optional card-reveal component of a morph face-up cost. */
    private RevealCardsFromHandCastingCost morphRevealCost;
    /** Optional permanent-return component of a morph face-up cost. */
    private ReturnPermanentsCost morphAdditionalCost;
    /** Optional permanent-sacrifice component of a morph face-up cost. */
    private SacrificePermanentsCost morphSacrificeCost;
    /** Optional discard component of a morph face-up cost. */
    private DiscardCardTypeCost morphDiscardCost;
    /** Optional life component of a morph face-up cost. */
    private LifeCastingCost morphLifeCost;
    /** Card-specific "cast this spell only when …" restriction, or null for normal timing. Defiant Stand. */
    private SpellCastTimingRestriction spellCastTimingRestriction;
    /**
     * Exact number of permanent targets the card's "when you cycle this card" trigger needs, chosen as the
     * trigger is put on the stack; the trigger is removed when fewer legal targets exist (Resounding Wave).
     * Zero for a trigger that does not use this declaration.
     */
    private int selfCycledTriggerPermanentTargets;
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
    /** Optional permanent predicate that makes this spell castable at instant speed when targeted. */
    private PermanentPredicate flashCastTargetPredicate;

    /**
     * Card-specific "this Equipment can be attached only to …" restriction (Konda's Banner), or
     * null when any creature may be equipped. Unlike the equip ability's target filter this is a
     * continuous requirement: an Equipment attached to a permanent that stops matching becomes
     * unattached as a state-based action (CR 704.5n).
     */
    private PermanentPredicate attachRestriction;

    /**
     * The spell, triggered and static abilities, in the order they were added; see
     * {@link #getAbilities()}. The elements are immutable, so a change replaces an element.
     */
    @Getter(AccessLevel.NONE)
    private List<Ability> abilities = new ArrayList<>();
    /** The per-slot view of {@link #abilities}, built on first use and dropped whenever they change. */
    @Getter(AccessLevel.NONE)
    private volatile SlotIndex slotIndex;
    /**
     * The additional costs to cast this card (CR 601.2b, 601.2f), in the order they were added; see
     * {@link #getCastingCosts()}. They are not part of its spell ability.
     */
    @Getter(AccessLevel.NONE)
    private List<CostEffect> castingCosts = new ArrayList<>();
    private List<ActivatedAbility> activatedAbilities = new ArrayList<>();
    private List<ActivatedAbility> graveyardActivatedAbilities = new ArrayList<>();
    /** Abilities activatable while this card is in its owner's hand (e.g. Reinforce). */
    private List<ActivatedAbility> handActivatedAbilities = new ArrayList<>();
    /** Abilities activatable while this card is a spell on the stack (e.g. Lightning Storm). */
    private List<ActivatedAbility> stackActivatedAbilities = new ArrayList<>();

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
            this.combinedManaValue = oracle.combinedManaValue();
            this.color = oracle.color();
            this.colors = oracle.colors();
            this.colorIdentity = oracle.colorIdentity();
            this.splitHalves = oracle.splitHalves();
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

    /** Creates an immutable synthetic card used only to identify a runtime stack entry. */
    public static Card namedRuntimePlaceholder(String name) {
        Card card = new Card();
        card.name = name;
        card.freeze();
        return card;
    }

    /**
     * Copy constructor backing {@link #createRuntimeCopy()}. Copies every field, including the
     * id, but not {@link #frozen} — the copy starts mutable. Collection fields are copied into
     * fresh containers (elements shared: effects, abilities and target groups are immutable). Note
     * the copy is a plain
     * {@code Card} — subclass identity (only used for oracle-registry lookup at construction
     * and {@code getBackFaceClassName()} at set load) is not preserved.
     *
     * <p>MAINTENANCE: when adding a field to Card, copy it here. {@code CardRuntimeCopyTest}
     * fails on any newly declared field to force this update.
     */
    protected Card(Card source) {
        this(source, source.id);
    }

    /** Creates a mutable copy with a fresh card identity, for snapshots of a card's last known state. */
    public Card createCardCopy() {
        return new Card(this, UUID.randomUUID());
    }

    private Card(Card source, UUID id) {
        this.id = id;
        this.ownerId = source.ownerId;
        this.name = source.name;
        this.type = source.type;
        this.manaCost = source.manaCost;
        this.combinedManaValue = source.combinedManaValue;
        this.parsedManaCost = source.parsedManaCost;
        this.color = source.color;
        this.colors = source.colors;
        this.colorIdentity = source.colorIdentity;
        this.splitHalves = source.splitHalves;
        // A fresh-identity copy is a different object, so it can't be restored to this physical card.
        this.splitWholeCard = id.equals(source.id) ? source.splitWholeCard : null;
        this.additionalTypes = source.additionalTypes;
        this.supertypes = source.supertypes;
        this.subtypes = source.subtypes;
        this.cardText = source.cardText;
        this.power = source.power;
        this.toughness = source.toughness;
        this.keywords = source.keywords;
        this.startingIntensity = source.startingIntensity;
        this.loyalty = source.loyalty;
        this.defense = source.defense;
        this.xColorRestrictions = source.xColorRestrictions == null
                ? null
                : EnumSet.copyOf(source.xColorRestrictions);
        this.xValueCap = source.xValueCap;
        this.minimumXValue = source.minimumXValue;
        this.setCode = source.setCode;
        this.collectorNumber = source.collectorNumber;
        this.token = source.token;
        this.bedtimeStory = source.bedtimeStory;
        this.tokenCard = source.tokenCard;
        this.conjured = source.conjured;
        this.cantBeCopied = source.cantBeCopied;
        this.sacrificeAtEndStep = source.sacrificeAtEndStep;
        this.requiresCreatureMana = source.requiresCreatureMana;
        this.requiresBasicLandMana = source.requiresBasicLandMana;
        this.requiresNoMana = source.requiresNoMana;
        this.enchantPlayer = source.enchantPlayer;
        this.enchantZone = source.enchantZone;
        this.additionalCostPerExtraTarget = source.additionalCostPerExtraTarget;
        this.additionalManaCostPerExtraTarget = source.additionalManaCostPerExtraTarget;
        this.additionalLifeCostPerTarget = source.additionalLifeCostPerTarget;
        this.allowSharedTargets = source.allowSharedTargets;
        this.opponentChosenSpellTargetIndices = source.opponentChosenSpellTargetIndices;
        this.attackTriggerTargetChosenByDefendingPlayer = source.attackTriggerTargetChosenByDefendingPlayer;
        this.attackTriggerOwnedByAura = source.attackTriggerOwnedByAura;
        this.attackTriggerGrantedByEquipment = source.attackTriggerGrantedByEquipment;
        this.multiTargetConstraint = source.multiTargetConstraint;
        this.targetControllersFixedAtCast = source.targetControllersFixedAtCast;
        this.targetGroups.addAll(source.targetGroups);
        source.effectTargetIndexMap.forEach((effect, targetIndices) ->
                this.effectTargetIndexMap.put(effect, new ArrayList<>(targetIndices)));
        this.castTimeTargetFilter = source.castTimeTargetFilter;
        this.watermark = source.watermark;
        this.backFaceCard = source.backFaceCard;
        this.modalDoubleFaced = source.modalDoubleFaced;
        this.meldCard = source.meldCard;
        this.castingOptions = new ArrayList<>(source.castingOptions);
        this.roomDoorManaCosts = List.copyOf(source.roomDoorManaCosts);
        this.selectedRoomDoor = source.selectedRoomDoor;
        this.morphCost = source.morphCost;
        this.spellmorph = source.spellmorph;
        this.morphCostReduction = source.morphCostReduction;
        this.morphRevealCost = source.morphRevealCost;
        this.morphAdditionalCost = source.morphAdditionalCost;
        this.morphSacrificeCost = source.morphSacrificeCost;
        this.morphDiscardCost = source.morphDiscardCost;
        this.morphLifeCost = source.morphLifeCost;
        this.spellCastTimingRestriction = source.spellCastTimingRestriction;
        this.selfCycledTriggerPermanentTargets = source.selfCycledTriggerPermanentTargets;
        this.castCondition = source.castCondition;
        this.flashCastCondition = source.flashCastCondition;
        this.flashCastTargetPredicate = source.flashCastTargetPredicate;
        this.attachRestriction = source.attachRestriction;
        this.abilities = new ArrayList<>(source.abilities);
        // The index holds no reference to its card, so the copy shares it until either changes.
        this.slotIndex = source.slotIndex;
        this.castingCosts = new ArrayList<>(source.castingCosts);
        this.activatedAbilities = new ArrayList<>(source.activatedAbilities);
        this.graveyardActivatedAbilities = new ArrayList<>(source.graveyardActivatedAbilities);
        this.handActivatedAbilities = new ArrayList<>(source.handActivatedAbilities);
        this.stackActivatedAbilities = new ArrayList<>(source.stackActivatedAbilities);
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

    /** Creates an unfrozen copy with a fresh identity, for snapshots that must not alias the original. */
    public Card createRuntimeCopyWithNewId() {
        return new Card(this, UUID.randomUUID());
    }

    /**
     * Creates a mutable, fresh-identity duplicate for a conjure effect. The duplicate is a conjured
     * card, never a token, even when this card is a token or a token card.
     */
    public Card createConjuredCopy() {
        Card copy = new Card(this, UUID.randomUUID());
        copy.token = false;
        copy.tokenCard = false;
        copy.conjured = true;
        return copy;
    }

    /**
     * Creates a runtime copy that keeps this card's non-text-box characteristics and uses the
     * supplied card's rules text, keywords, abilities, and targeting configuration.
     */
    public Card createRuntimeTextBoxCopy(Card textSource) {
        Card copy = new Card(this);
        copy.cardText = textSource.cardText;
        copy.bedtimeStory = textSource.bedtimeStory;
        copy.keywords = textSource.keywords.isEmpty()
                ? Set.of()
                : EnumSet.copyOf(textSource.keywords);

        copy.abilities = new ArrayList<>(textSource.abilities);
        copy.slotIndex = textSource.slotIndex;

        copy.castingCosts = new ArrayList<>(textSource.castingCosts);

        copy.targetGroups.clear();
        copy.effectTargetIndexMap.clear();
        copy.copyTargetingFrom(textSource);

        copy.activatedAbilities = new ArrayList<>(textSource.activatedAbilities);
        copy.graveyardActivatedAbilities = new ArrayList<>(textSource.graveyardActivatedAbilities);
        copy.handActivatedAbilities = new ArrayList<>(textSource.handActivatedAbilities);
        copy.stackActivatedAbilities = new ArrayList<>(textSource.stackActivatedAbilities);
        return copy;
    }

    /**
     * Creates a runtime copy with this card's identity and the supplied face's characteristics.
     * The copy is used for a modal double-faced spell after its face has been chosen; keeping the
     * id from this card lets stack targets and zone movement continue to refer to the physical card.
     */
    public Card createRuntimeCopyWithFace(Card face) {
        Card copy = new Card(this);
        copy.copyFaceCharacteristicsFrom(face);
        return copy;
    }

    private void copyFaceCharacteristicsFrom(Card face) {
        assertMutable();
        this.name = face.name;
        this.type = face.type;
        this.manaCost = face.manaCost;
        this.combinedManaValue = face.combinedManaValue;
        this.parsedManaCost = face.parsedManaCost;
        this.color = face.color;
        this.colors = face.colors;
        this.colorIdentity = face.colorIdentity;
        this.splitHalves = face.splitHalves;
        this.additionalTypes = face.additionalTypes;
        this.supertypes = face.supertypes;
        this.subtypes = face.subtypes;
        this.cardText = face.cardText;
        this.power = face.power;
        this.toughness = face.toughness;
        this.keywords = face.keywords;
        this.loyalty = face.loyalty;
        this.defense = face.defense;
        this.xColorRestrictions = face.xColorRestrictions == null
                ? null
                : EnumSet.copyOf(face.xColorRestrictions);
        this.xValueCap = face.xValueCap;
        this.minimumXValue = face.minimumXValue;
        this.token = face.token;
        this.bedtimeStory = face.bedtimeStory;
        this.tokenCard = face.tokenCard;
        this.cantBeCopied = face.cantBeCopied;
        this.sacrificeAtEndStep = face.sacrificeAtEndStep;
        this.requiresCreatureMana = face.requiresCreatureMana;
        this.requiresBasicLandMana = face.requiresBasicLandMana;
        this.requiresNoMana = face.requiresNoMana;
        this.enchantPlayer = face.enchantPlayer;
        this.enchantZone = face.enchantZone;
        this.additionalCostPerExtraTarget = face.additionalCostPerExtraTarget;
        this.additionalManaCostPerExtraTarget = face.additionalManaCostPerExtraTarget;
        this.additionalLifeCostPerTarget = face.additionalLifeCostPerTarget;
        this.allowSharedTargets = face.allowSharedTargets;
        this.opponentChosenSpellTargetIndices = face.opponentChosenSpellTargetIndices;
        this.multiTargetConstraint = face.multiTargetConstraint;
        this.targetControllersFixedAtCast = face.targetControllersFixedAtCast;
        this.targetGroups.clear();
        this.targetGroups.addAll(face.targetGroups);
        this.effectTargetIndexMap.clear();
        face.effectTargetIndexMap.forEach((effect, targetIndices) ->
                this.effectTargetIndexMap.put(effect, new ArrayList<>(targetIndices)));
        this.castTimeTargetFilter = face.castTimeTargetFilter;
        this.watermark = face.watermark;
        this.castingOptions = new ArrayList<>(face.castingOptions);
        this.roomDoorManaCosts = List.copyOf(face.roomDoorManaCosts);
        this.selectedRoomDoor = face.selectedRoomDoor;
        this.morphCost = face.morphCost;
        this.spellmorph = face.spellmorph;
        this.morphCostReduction = face.morphCostReduction;
        this.morphRevealCost = face.morphRevealCost;
        this.morphAdditionalCost = face.morphAdditionalCost;
        this.morphSacrificeCost = face.morphSacrificeCost;
        this.morphDiscardCost = face.morphDiscardCost;
        this.morphLifeCost = face.morphLifeCost;
        this.spellCastTimingRestriction = face.spellCastTimingRestriction;
        this.selfCycledTriggerPermanentTargets = face.selfCycledTriggerPermanentTargets;
        this.castCondition = face.castCondition;
        this.flashCastCondition = face.flashCastCondition;
        this.attachRestriction = face.attachRestriction;
        this.abilities = new ArrayList<>(face.abilities);
        this.slotIndex = face.slotIndex;
        this.castingCosts = new ArrayList<>(face.castingCosts);
        this.activatedAbilities = new ArrayList<>(face.activatedAbilities);
        this.graveyardActivatedAbilities = new ArrayList<>(face.graveyardActivatedAbilities);
        this.handActivatedAbilities = new ArrayList<>(face.handActivatedAbilities);
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

    /** Whether {@link #freeze()} has run, so nothing about this card can change any more. */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Remembers this card's whole split-card characteristics before one half's characteristics are
     * applied for casting, so the card can be restored when the spell leaves the stack (CR 709.4).
     * Does nothing once a half has already been applied.
     */
    public void rememberSplitWholeCard() {
        assertMutable();
        if (splitWholeCard == null) {
            Card whole = new Card(this);
            whole.freeze();
            splitWholeCard = whole;
        }
    }

    /**
     * Gives this card one split half's own characteristics for casting, remembering the whole card
     * first so it is the whole card again once the spell leaves the stack. While on the stack only
     * the characteristics of the half being cast exist (CR 709.3b).
     */
    public void applySplitHalf(OracleData half) {
        rememberSplitWholeCard();
        setName(half.name());
        setType(half.type());
        setAdditionalTypes(half.additionalTypes());
        setSupertypes(half.supertypes());
        setSubtypes(half.subtypes());
        setManaCost(half.manaCost());
        setColors(half.colors());
        setColor(half.color());
        setCardText(half.cardText());
    }

    /**
     * Whether this is a split card with aftermath: its second half is the back face, cast only from
     * a graveyard (CR 702.127a), so a cast from anywhere else casts its first half.
     */
    public boolean isAftermathSplitCard() {
        return splitHalves.size() >= 2 && graveyardCastHalf() != this;
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
    public void setColorIdentity(List<CardColor> colorIdentity) { assertMutable(); this.colorIdentity = colorIdentity; }
    public void setAdditionalTypes(Set<CardType> additionalTypes) { assertMutable(); this.additionalTypes = additionalTypes; }
    public void setSupertypes(Set<CardSupertype> supertypes) { assertMutable(); this.supertypes = supertypes; }
    public void setSubtypes(List<CardSubtype> subtypes) { assertMutable(); this.subtypes = subtypes; }
    public void setCardText(String cardText) { assertMutable(); this.cardText = cardText; }
    public void setPower(Integer power) { assertMutable(); this.power = power; }
    public void setToughness(Integer toughness) { assertMutable(); this.toughness = toughness; }
    public void setKeywords(Set<Keyword> keywords) { assertMutable(); this.keywords = keywords; }
    public void setStartingIntensity(int startingIntensity) {
        assertMutable();
        if (startingIntensity < 0) {
            throw new IllegalArgumentException("Starting intensity cannot be negative");
        }
        this.startingIntensity = startingIntensity;
    }
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
    public void setMinimumXValue(int minimumXValue) { assertMutable(); this.minimumXValue = minimumXValue; }
    public void setSetCode(String setCode) { assertMutable(); this.setCode = setCode; }
    public void setCollectorNumber(String collectorNumber) { assertMutable(); this.collectorNumber = collectorNumber; }
    public void setToken(boolean token) { assertMutable(); this.token = token; }
    public void setCombinedManaValue(Integer combinedManaValue) { assertMutable(); this.combinedManaValue = combinedManaValue; }
    public void setSplitHalves(List<OracleData> splitHalves) { assertMutable(); this.splitHalves = List.copyOf(splitHalves); }
    public void setBedtimeStory(boolean bedtimeStory) { assertMutable(); this.bedtimeStory = bedtimeStory; }
    public void setTokenCard(boolean tokenCard) { assertMutable(); this.tokenCard = tokenCard; }
    public void setConjured(boolean conjured) { assertMutable(); this.conjured = conjured; }
    public void setCantBeCopied(boolean cantBeCopied) { assertMutable(); this.cantBeCopied = cantBeCopied; }
    public void setSacrificeAtEndStep(boolean sacrificeAtEndStep) { assertMutable(); this.sacrificeAtEndStep = sacrificeAtEndStep; }
    public void setRequiresCreatureMana(boolean requiresCreatureMana) { assertMutable(); this.requiresCreatureMana = requiresCreatureMana; }
    public void setRequiresBasicLandMana(boolean requiresBasicLandMana) { assertMutable(); this.requiresBasicLandMana = requiresBasicLandMana; }
    public void setRequiresNoMana(boolean requiresNoMana) { assertMutable(); this.requiresNoMana = requiresNoMana; }
    public void setEnchantPlayer(boolean enchantPlayer) { assertMutable(); this.enchantPlayer = enchantPlayer; }
    public void setEnchantZone(boolean enchantZone) { assertMutable(); this.enchantZone = enchantZone; }
    public void setAdditionalCostPerExtraTarget(int additionalCostPerExtraTarget) { assertMutable(); this.additionalCostPerExtraTarget = additionalCostPerExtraTarget; }
    public void setAdditionalManaCostPerExtraTarget(String additionalManaCostPerExtraTarget) { assertMutable(); this.additionalManaCostPerExtraTarget = additionalManaCostPerExtraTarget; }
    public void setAdditionalLifeCostPerTarget(int additionalLifeCostPerTarget) { assertMutable(); this.additionalLifeCostPerTarget = additionalLifeCostPerTarget; }
    public void setAllowSharedTargets(boolean allowSharedTargets) { assertMutable(); this.allowSharedTargets = allowSharedTargets; }
    public void setOpponentChosenSpellTargetIndices(List<Integer> indices) {
        assertMutable();
        this.opponentChosenSpellTargetIndices = indices == null ? List.of() : List.copyOf(indices);
    }
    public void setAttackTriggerTargetChosenByDefendingPlayer(boolean chosenByDefendingPlayer) {
        assertMutable();
        this.attackTriggerTargetChosenByDefendingPlayer = chosenByDefendingPlayer;
    }
    public void setAttackTriggerOwnedByAura(boolean ownedByAura) {
        assertMutable();
        this.attackTriggerOwnedByAura = ownedByAura;
    }
    public void setAttackTriggerGrantedByEquipment(boolean grantedByEquipment) {
        assertMutable();
        this.attackTriggerGrantedByEquipment = grantedByEquipment;
    }
    public void setMultiTargetConstraint(MultiTargetConstraint multiTargetConstraint) { assertMutable(); this.multiTargetConstraint = multiTargetConstraint; }
    public void setTargetControllersFixedAtCast(boolean targetControllersFixedAtCast) { assertMutable(); this.targetControllersFixedAtCast = targetControllersFixedAtCast; }
    public void setCastTimeTargetFilter(TargetFilter castTimeTargetFilter) { assertMutable(); this.castTimeTargetFilter = castTimeTargetFilter; }
    public void setSpellCastTimingRestriction(SpellCastTimingRestriction spellCastTimingRestriction) { assertMutable(); this.spellCastTimingRestriction = spellCastTimingRestriction; }
    public void setSelfCycledTriggerPermanentTargets(int count) { assertMutable(); this.selfCycledTriggerPermanentTargets = count; }
    public void setCastCondition(Condition castCondition) { assertMutable(); this.castCondition = castCondition; }
    public void setFlashCastCondition(Condition flashCastCondition) { assertMutable(); this.flashCastCondition = flashCastCondition; }
    public void setFlashCastTargetPredicate(PermanentPredicate flashCastTargetPredicate) { assertMutable(); this.flashCastTargetPredicate = flashCastTargetPredicate; }
    public void setAttachRestriction(PermanentPredicate attachRestriction) { assertMutable(); this.attachRestriction = attachRestriction; }
    public void setWatermark(String watermark) { assertMutable(); this.watermark = watermark; }
    public void setBackFaceCard(Card backFaceCard) { assertMutable(); this.backFaceCard = backFaceCard; }
    public void setModalDoubleFaced(boolean modalDoubleFaced) { assertMutable(); this.modalDoubleFaced = modalDoubleFaced; }
    public void setMeldCard(boolean meldCard) { assertMutable(); this.meldCard = meldCard; }

    /** Replaces this unfrozen runtime copy with Bombardment's Missile characteristics. */
    public void becomeMissile() {
        assertMutable();
        setName("Missile");
        setType(CardType.SORCERY);
        setManaCost("{R}");
        setColor(CardColor.RED);
        setColors(List.of(CardColor.RED));
        setColorIdentity(List.of(CardColor.RED));
        setAdditionalTypes(Set.of());
        setSubtypes(List.of());
        setCardText("Missile deals 2 damage to any target.");
        setPower(null);
        setToughness(null);
        setLoyalty(null);
        setDefense(null);
        setKeywords(Set.of());
        setXColorRestrictions((Set<ManaColor>) null);
        setXValueCap(null);
        setMinimumXValue(0);
        setCantBeCopied(false);
        setSacrificeAtEndStep(false);
        setRequiresCreatureMana(false);
        setRequiresBasicLandMana(false);
        setEnchantPlayer(false);
        setEnchantZone(false);
        setAdditionalCostPerExtraTarget(0);
        setAdditionalManaCostPerExtraTarget(null);
        setAdditionalLifeCostPerTarget(0);
        setAllowSharedTargets(false);
        setOpponentChosenSpellTargetIndices(List.of());
        setAttackTriggerTargetChosenByDefendingPlayer(false);
        setAttackTriggerOwnedByAura(false);
        setAttackTriggerGrantedByEquipment(false);
        setMultiTargetConstraint(null);
        setTargetControllersFixedAtCast(false);
        setCastTimeTargetFilter(null);
        setSpellCastTimingRestriction(null);
        setCastCondition(null);
        setFlashCastCondition(null);
        setFlashCastTargetPredicate(null);
        setAttachRestriction(null);
        setWatermark(null);
        setBackFaceCard(null);
        setModalDoubleFaced(false);
        castingOptions.clear();
        roomDoorManaCosts = List.of();
        selectedRoomDoor = null;
        morphCost = null;
        spellmorph = false;
        morphCostReduction = null;
        morphRevealCost = null;
        morphAdditionalCost = null;
        morphSacrificeCost = null;
        morphDiscardCost = null;
        morphLifeCost = null;
        targetGroups.clear();
        effectTargetIndexMap.clear();
        abilities.clear();
        slotIndex = null;
        castingCosts.clear();
        activatedAbilities.clear();
        graveyardActivatedAbilities.clear();
        handActivatedAbilities.clear();
        stackActivatedAbilities.clear();
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target")).addSpellEffect(new DealDamageToAnyTargetEffect(2));
    }

    // ── Target-first builder API ──────────────────────────────────────

    /** Clears the rules text and ability registrations on a mutable runtime copy. */
    public void clearRulesTextAndAbilities() {
        assertMutable();
        abilities.clear();
        slotIndex = null;
        castingCosts.clear();
        targetGroups.clear();
        effectTargetIndexMap.clear();
        activatedAbilities.clear();
        graveyardActivatedAbilities.clear();
        handActivatedAbilities.clear();
        stackActivatedAbilities.clear();
    }

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
        return declareTargetGroup(filter, filter, minTargets, maxTargets, minTargets, maxTargets, false, null, null,
                minTargets);
    }

    public SpellTarget targetWhenGiftPromised(TargetFilter filter, int minTargets, int maxTargets,
                                              int giftPromisedMinTargets) {
        return declareTargetGroup(filter, filter, minTargets, maxTargets, minTargets, maxTargets, false, null, null,
                giftPromisedMinTargets);
    }

    /**
     * Declares a target group whose bounds change when the spell is kicked.
     * The ordinary bounds apply when the spell is not kicked; the kicker bounds apply when it is.
     */
    public SpellTarget targetWhenKicked(TargetFilter filter, int minTargets, int maxTargets,
                                        int kickedMinTargets, int kickedMaxTargets) {
        return targetWhenKicked(filter, filter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets);
    }

    /**
     * Declares a target group whose target filter and bounds can change when the spell is kicked.
     */
    public SpellTarget targetWhenKicked(TargetFilter filter, TargetFilter kickedFilter,
                                        int minTargets, int maxTargets,
                                        int kickedMinTargets, int kickedMaxTargets) {
        return declareTargetGroup(filter, kickedFilter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets,
                false, null, null, minTargets);
    }

    /**
     * Declares a target group whose target count scales with the spell's X value
     * ("Destroy X target nonblack creatures" — Dregs of Sorrow). The effective number of
     * targets is bounded by X at cast time; {@code cap} is only a sanity ceiling.
     */
    public SpellTarget targetX(TargetFilter filter, int cap) {
        return declareTargetGroup(filter, filter, 0, cap, 0, cap, true, null, null, 0);
    }

    /**
     * Declares exactly X targets for a spell's X value. The cap is a sanity ceiling for target
     * position handling and should be at least as large as any practical X value.
     */
    public SpellTarget targetExactlyX(TargetFilter filter, int cap) {
        return declareTargetGroup(filter, filter, cap, cap, cap, cap, true, null, null, cap);
    }

    /**
     * Declares an optional target group whose maximum is evaluated when an ETB trigger is put on
     * the stack. The fixed cap is only a sanity ceiling for target-position handling.
     */
    public SpellTarget targetUpTo(DynamicAmount dynamicMaxTargets, TargetFilter filter, int cap) {
        return declareTargetGroup(filter, filter, 0, cap, 0, cap, false, null, dynamicMaxTargets, 0);
    }

    /**
     * Declares a target group whose minimum and maximum are both the evaluated dynamic count.
     * A zero count produces no target, while a positive count requires that many targets.
     */
    public SpellTarget targetWithDynamicCount(DynamicAmount dynamicTargetCount, TargetFilter filter, int cap) {
        return declareTargetGroup(filter, filter, 0, cap, 0, cap, false, dynamicTargetCount, dynamicTargetCount, 0);
    }

    /**
     * Declares a target group whose minimum and maximum are separate evaluated dynamic counts, e.g.
     * "divided among any number of targets" with a positive X, which needs at least one target
     * (Rolling Thunder ruling) and at most X.
     */
    public SpellTarget targetWithDynamicRange(DynamicAmount dynamicMinTargets,
                                              DynamicAmount dynamicMaxTargets, TargetFilter filter, int cap) {
        return declareTargetGroup(filter, filter, 0, cap, 0, cap, false, dynamicMinTargets, dynamicMaxTargets, 0);
    }

    private SpellTarget declareTargetGroup(TargetFilter filter, TargetFilter kickedFilter, int minTargets,
                                           int maxTargets, int kickedMinTargets, int kickedMaxTargets,
                                           boolean xScaled, DynamicAmount dynamicMinTargets,
                                           DynamicAmount dynamicMaxTargets, int giftPromisedMinTargets) {
        assertMutable();
        int index = targetGroups.size();
        targetGroups.add(new TargetGroup(filter, kickedFilter, minTargets, maxTargets, kickedMinTargets,
                kickedMaxTargets, index, xScaled, dynamicMinTargets, dynamicMaxTargets, giftPromisedMinTargets));
        syncSpellTargets();
        return new SpellTarget(this, index);
    }

    /** Puts the target group at {@code index} in the mode {@code scope}; see {@link SpellTarget#inDistinctScope}. */
    void setTargetGroupDistinctScope(int index, int scope) {
        assertMutable();
        targetGroups.set(index, targetGroups.get(index).inDistinctScope(scope));
        syncSpellTargets();
    }

    /**
     * Called by {@link SpellTarget#addEffect} to map an effect instance to its target index.
     * Wrapper effects (conditional, may) register their inner effects under the same index,
     * because resolution unwraps them before dispatching to the handler — the handler must be
     * able to look up the group by the effect instance it actually receives.
     */
    public void registerEffectTargetIndex(CardEffect effect, int targetIndex) {
        bindEffectToTargetGroup(effect, targetIndex);
        syncSpellTargets();
    }

    private void bindEffectToTargetGroup(CardEffect effect, int targetIndex) {
        if (effect == null) {
            return;
        }
        assertMutable();
        effectTargetIndexMap.computeIfAbsent(effect, ignored -> new ArrayList<>()).add(targetIndex);
        switch (effect) {
            case ConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case QueueReflexiveAbilityEffect e -> bindEffectToTargetGroup(e.effect(), targetIndex);
            case ConditionalReplacementEffect e -> {
                if (e.baseEffect() != null) bindEffectToTargetGroup(e.baseEffect(), targetIndex);
                bindEffectToTargetGroup(e.upgradedEffect(), targetIndex);
            }
            case MayEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case ExileSourceCardFromGraveyardThenEffect e ->
                    bindEffectToTargetGroup(e.thenEffect(), targetIndex);
            case ExilePermanentYouControlAndTrackWithSourceEffect e ->
                    bindEffectToTargetGroup(e.thenEffect(), targetIndex);
            case SacrificePermanentThenEffect e -> bindEffectToTargetGroup(e.thenEffect(), targetIndex);
            case SacrificeSelfThenEffect e -> bindEffectToTargetGroup(e.thenEffect(), targetIndex);
            case StateTriggerEffect e -> e.effects().forEach(innerEffect ->
                    bindEffectToTargetGroup(innerEffect, targetIndex));
            case MayPayManaEffect e -> {
                if (e.wrapped() != null) bindEffectToTargetGroup(e.wrapped(), targetIndex);
                if (e.elseEffect() != null) bindEffectToTargetGroup(e.elseEffect(), targetIndex);
            }
            case MayPayTapPermanentsEffect e -> {
                if (e.wrapped() != null) bindEffectToTargetGroup(e.wrapped(), targetIndex);
                if (e.elseEffect() != null) bindEffectToTargetGroup(e.elseEffect(), targetIndex);
            }
            case MayPayTapAndSacrificePermanentEffect e ->
                    bindEffectToTargetGroup(e.thenEffect(), targetIndex);
            case ForcedCostOrElseEffect e -> {
                e.paidEffects().forEach(innerEffect -> bindEffectToTargetGroup(innerEffect, targetIndex));
                if (e.elseEffects() != null) {
                    e.elseEffects().forEach(innerEffect -> bindEffectToTargetGroup(innerEffect, targetIndex));
                }
            }
            case OncePerTurnTriggerEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case OnceOnlyTriggerEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            // Ally combat-damage triggers resolve their wrapped effect when the trigger fires;
            // preserve its target-group binding for deferred trigger-time target selection.
            case AllyCombatDamageTriggerEffect e -> bindEffectToTargetGroup(e.effect(), targetIndex);
            case RollD20Effect e -> {
                if (e.zeroOrLess() != null) bindEffectToTargetGroup(e.zeroOrLess(), targetIndex);
                if (e.oneToNine() != null) bindEffectToTargetGroup(e.oneToNine(), targetIndex);
                if (e.tenToNineteen() != null) bindEffectToTargetGroup(e.tenToNineteen(), targetIndex);
                if (e.twenty() != null) bindEffectToTargetGroup(e.twenty(), targetIndex);
            }
            case RollD6Effect e -> e.branches().forEach(branch ->
                    bindEffectToTargetGroup(branch, targetIndex));
            case RollDiceEffect e -> {
                if (e.oddResult() != null) bindEffectToTargetGroup(e.oddResult(), targetIndex);
                if (e.evenResult() != null) bindEffectToTargetGroup(e.evenResult(), targetIndex);
            }
            // SequenceEffect splices its steps into the resolution list; each step must keep the
            // sequence's target group (fuse halves that bundle multi-step one-target instructions).
            case SequenceEffect e -> {
                for (CardEffect step : e.steps()) {
                    bindEffectToTargetGroup(step, targetIndex);
                }
            }
            case SpellCastTriggerEffect e -> {
                for (CardEffect resolvedEffect : e.resolvedEffects()) {
                    bindEffectToTargetGroup(resolvedEffect, targetIndex);
                }
            }
            case ChooseOneForTargetPermanentEffect e -> {
                for (var option : e.options()) {
                    for (CardEffect optionEffect : option.effects()) {
                        bindEffectToTargetGroup(optionEffect, targetIndex);
                    }
                }
            }
            // Triggering conditionals (e.g. Diregraf Captain's "whenever another Zombie you control
            // dies") are unwrapped to their inner effect when the trigger is serviced, so the inner
            // effect must resolve to the same declared target group — otherwise the card-level target
            // filter (e.g. opponent-only) is lost after unwrapping.
            case TriggeringCardConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case TriggeringPermanentConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case ConditionalTriggeringPermanentEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case TriggeringArtifactControllerConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case TriggeringPermanentControllerConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
            case TriggeringRoomDoorConditionalEffect e -> bindEffectToTargetGroup(e.wrapped(), targetIndex);
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
        int targetIndexOffset = targetGroups.size();
        int targetPositionOffset = getMultiTargetFilters().size();
        for (TargetGroup sourceTarget : source.targetGroups) {
            targetGroups.add(sourceTarget.atIndex(targetIndexOffset + sourceTarget.getIndex()));
        }
        source.effectTargetIndexMap.forEach((effect, targetIndices) ->
                targetIndices.forEach(targetIndex ->
                        bindEffectToTargetGroup(effect, targetIndexOffset + targetIndex)));
        if (!source.opponentChosenSpellTargetIndices.isEmpty()) {
            List<Integer> appendedIndices = new ArrayList<>(opponentChosenSpellTargetIndices);
            source.opponentChosenSpellTargetIndices.stream()
                    .map(index -> index + targetPositionOffset)
                    .forEach(appendedIndices::add);
            opponentChosenSpellTargetIndices = List.copyOf(appendedIndices);
        }
        syncSpellTargets();
    }

    /**
     * Appends the target groups used by the supplied effects from another card. This is used when
     * an effect copies a permanent but adds an effect whose target declaration belongs to the card
     * creating the copy rather than to the copied permanent.
     */
    public void appendSpellTargetingForEffectsFrom(Card source, Collection<CardEffect> effects) {
        assertMutable();
        if (source == null || effects == null || effects.isEmpty()) {
            return;
        }

        Set<CardEffect> effectSet = Collections.newSetFromMap(new IdentityHashMap<>());
        effectSet.addAll(effects);
        Set<Integer> sourceTargetIndices = new java.util.HashSet<>();
        for (CardEffect effect : effectSet) {
            List<Integer> targetIndices = source.effectTargetIndexMap.get(effect);
            if (targetIndices != null) {
                sourceTargetIndices.addAll(targetIndices);
            }
        }
        if (sourceTargetIndices.isEmpty()) {
            return;
        }

        Map<Integer, Integer> targetIndexMap = new HashMap<>();
        for (TargetGroup sourceTarget : source.targetGroups) {
            if (!sourceTargetIndices.contains(sourceTarget.getIndex())) {
                continue;
            }
            int targetIndex = targetGroups.size();
            targetGroups.add(new TargetGroup(
                    sourceTarget.getFilter(),
                    sourceTarget.getKickedFilter(),
                    sourceTarget.getMinTargets(),
                    sourceTarget.getMaxTargets(),
                    sourceTarget.getKickedMinTargets(),
                    sourceTarget.getKickedMaxTargets(),
                    targetIndex,
                    sourceTarget.isXScaled(),
                    sourceTarget.getDynamicMinTargets(),
                    sourceTarget.getDynamicMaxTargets(),
                    sourceTarget.getMinTargets()));
            targetIndexMap.put(sourceTarget.getIndex(), targetIndex);
        }

        source.effectTargetIndexMap.forEach((effect, targetIndices) -> {
            if (!effectSet.contains(effect)) {
                return;
            }
            targetIndices.forEach(sourceTargetIndex -> {
                Integer targetIndex = targetIndexMap.get(sourceTargetIndex);
                if (targetIndex != null) {
                    bindEffectToTargetGroup(effect, targetIndex);
                }
            });
        });
        syncSpellTargets();
    }

    /**
     * Clears runtime target-first declarations. Used by modal spells (ChooseOneEffect) whose chosen
     * mode declares its own {@code target()} slots at cast time, so re-casting the same card instance
     * does not accumulate stale target declarations.
     */
    public void clearRuntimeSpellTargets() {
        assertMutable();
        targetGroups.clear();
        effectTargetIndexMap.clear();
        opponentChosenSpellTargetIndices = List.of();
        syncSpellTargets();
    }

    // ── Derived targeting getters (replace old stored fields) ────────

    /**
     * Returns the target filter for single-target spells, or the first target's
     * filter for multi-target spells. For modal spells, returns the cast-time override.
     */
    public TargetFilter getTargetFilter() {
        return getTargetFilter(false);
    }

    /** Returns the target filter for the selected kicker branch. */
    public TargetFilter getTargetFilter(boolean kicked) {
        if (castTimeTargetFilter != null) return castTimeTargetFilter;
        return getDeclaredTargetFilter(kicked);
    }

    /**
     * Returns the target filter declared by the card's target group, ignoring any cast-time
     * override used by a triggered ability or a modal choice. Aura attachment legality uses this
     * filter because a cast-time target restriction is not an ongoing enchant restriction.
     */
    public TargetFilter getDeclaredTargetFilter() {
        return getDeclaredTargetFilter(false);
    }

    /** Returns the declared target filter for the selected kicker branch. */
    public TargetFilter getDeclaredTargetFilter(boolean kicked) {
        if (targetGroups.isEmpty()) return null;
        return targetGroups.getFirst().getFilter(kicked);
    }

    /**
     * Returns per-position target filters for multi-target spells.
     * Each target group's filter is repeated for its maxTargets count,
     * so that position-based validation matches the correct filter
     * when a group allows multiple targets (e.g. "up to 2 target creatures").
     */
    public List<TargetFilter> getMultiTargetFilters() {
        return getMultiTargetFilters(false);
    }

    /** Returns per-position target filters for the selected kicker branch. */
    public List<TargetFilter> getMultiTargetFilters(boolean kicked) {
        int positionCount = (int) Math.min(Integer.MAX_VALUE, targetGroups.stream()
                .mapToLong(st -> Math.max(st.getMaxTargets(), st.getKickedMaxTargets())).sum());
        return new java.util.AbstractList<>() {
            @Override
            public TargetFilter get(int index) {
                java.util.Objects.checkIndex(index, positionCount);
                int remaining = index;
                for (TargetGroup st : targetGroups) {
                    int count = Math.max(st.getMaxTargets(), st.getKickedMaxTargets());
                    if (remaining < count) return st.getFilter(kicked);
                    remaining -= count;
                }
                throw new IndexOutOfBoundsException(index);
            }

            @Override
            public int size() {
                return positionCount;
            }
        };
    }

    /**
     * Returns the minimum total number of targets required.
     */
    public int getMinTargets() {
        return targetGroups.stream().mapToInt(TargetGroup::getMinTargets).sum();
    }

    /**
     * Returns an additional minimum target count imposed by an alternate cost, such as awaken.
     * Normal spell targeting is validated against the prepared spell effects. Target declarations
     * on this card may instead belong to triggered abilities or disappear when cast face down or
     * overloaded, so they must not impose a blanket cast-time minimum here.
     */
    public int getMinTargetsWhenCastForAlternateCost() {
        return 0;
    }

    /**
     * Returns the maximum total number of targets allowed.
     */
    public int getMaxTargets() {
        // Saturates so an "any number of targets" group (Integer.MAX_VALUE) beside others can't overflow.
        return (int) Math.min(Integer.MAX_VALUE, targetGroups.stream()
                .mapToLong(st -> Math.max(st.getMaxTargets(), st.getKickedMaxTargets()))
                .sum());
    }

    /**
     * Returns true if any target group scales its target count with the spell's X value.
     */
    public boolean hasXScaledTargets() {
        return targetGroups.stream().anyMatch(TargetGroup::isXScaled);
    }

    public boolean hasDynamicTargetCount() {
        return targetGroups.stream().anyMatch(st -> st.getDynamicMinTargets() != null
                || st.getDynamicMaxTargets() != null);
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
        return getEffectiveMinTargets(xValue, kicked, false);
    }

    public int getEffectiveMinTargets(int xValue, boolean kicked, boolean giftPromised) {
        return targetGroups.stream()
                .mapToInt(st -> {
                    int min = giftPromised
                            ? st.getGiftPromisedMinTargets()
                            : kicked ? st.getKickedMinTargets() : st.getMinTargets();
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
        return targetGroups.stream()
                .mapToInt(st -> {
                    int max = kicked ? st.getKickedMaxTargets() : st.getMaxTargets();
                    return st.isXScaled() ? Math.min(xValue, max) : max;
                })
                .sum();
    }

    /**
     * Every target group the card declares, in order: the spell ability's first (see
     * {@link #getSpellTargetGroups()}), then those bound to its other abilities' effects, which stay
     * here until their trigger families leave {@link EffectSlot}. A group's index is its position.
     */
    public List<TargetGroup> getTargetGroups() {
        return Collections.unmodifiableList(targetGroups);
    }

    /**
     * The target groups of the spell this card is cast as: its spell ability's
     * ({@link SpellAbility#targets()}), or, for a card without one (an Aura spell, whose enchant
     * ability defines its target, CR 303.4a), every group it declares.
     */
    public List<TargetGroup> getSpellTargetGroups() {
        SpellAbility spell = spellAbilityOrNull();
        return spell == null ? getTargetGroups() : spell.targets();
    }

    /**
     * Returns the target index for the given effect instance, or -1 if not mapped.
     */
    public int getEffectTargetIndex(CardEffect effect) {
        return getEffectTargetIndex(effect, 0);
    }

    /**
     * Returns the target index for a particular occurrence of an effect instance. Repeatable modal
     * spells can insert the same immutable effect object more than once, with a distinct target
     * group for each selection.
     */
    public int getEffectTargetIndex(CardEffect effect, int occurrence) {
        List<Integer> targetIndices = targetIndicesForEffect(effect);
        if (targetIndices == null || occurrence < 0 || occurrence >= targetIndices.size()) {
            return -1;
        }
        return targetIndices.get(occurrence);
    }

    public boolean hasEffectTargetIndex(CardEffect effect) {
        return targetIndicesForEffect(effect) != null;
    }

    public boolean isEffectBoundToTargetGroup(CardEffect effect, int groupIndex) {
        List<Integer> targetIndices = targetIndicesForEffect(effect);
        return targetIndices != null && targetIndices.contains(groupIndex);
    }

    /** Returns every effect registered against the target group, regardless of its effect slot. */
    public List<CardEffect> getEffectsBoundToTargetGroup(int groupIndex) {
        return effectTargetIndexMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(groupIndex))
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Integer> targetIndicesForEffect(CardEffect effect) {
        List<Integer> targetIndices = effectTargetIndexMap.get(effect);
        if (targetIndices != null) {
            return targetIndices;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return targetIndicesForEffect(conditional.wrapped());
        }
        if (effect instanceof SequenceEffect sequence) {
            // A sequence assembled at resolution (a reflexive trigger's counters-then-phase-out) targets
            // through whichever of its steps the card bound to a target group.
            for (CardEffect step : sequence.steps()) {
                List<Integer> stepIndices = targetIndicesForEffect(step);
                if (stepIndices != null) {
                    return stepIndices;
                }
            }
        }
        for (Map.Entry<CardEffect, List<Integer>> registered : effectTargetIndexMap.entrySet()) {
            CardEffect wrapped = registered.getKey() instanceof TriggeringPermanentConditionalEffect permanentConditional
                    ? permanentConditional.wrapped()
                    : registered.getKey() instanceof TriggeringCardConditionalEffect cardConditional
                    ? cardConditional.wrapped() : null;
            if (wrapped == effect) {
                return registered.getValue();
            }
        }
        return null;
    }

    /**
     * True if any spell effect is bound (via {@code target(...).addEffect(...)}) to the given target
     * group. A group with no bound effect is a bare positional target that another effect reads by
     * index (e.g. Blood Feud's first fight target, read by the {@code FightTargetsEffect} bound to the
     * second group); such a group is never a gated-out trigger group.
     */
    public boolean bindsEffectToTargetGroup(int groupIndex) {
        return effectTargetIndexMap.values().stream().anyMatch(indices -> indices.contains(groupIndex));
    }

    /**
     * Returns true if the target group at the given expanded position allows player targets.
     * Used by the valid target service to determine per-position player targeting in multi-target spells.
     *
     * <p>An explicit group filter defines the legal target kind. Only an unfiltered group inherits
     * player targeting from the effects bound to it.
     */
    public boolean doesPositionAllowPlayerTargets(int expandedPosition) {
        return doesPositionAllowPlayerTargets(expandedPosition, false);
    }

    /** Returns whether a target position allows players for the selected kicker branch. */
    public boolean doesPositionAllowPlayerTargets(int expandedPosition, boolean kicked) {
        if (targetGroups.isEmpty()) return false;
        int cumulative = 0;
        for (TargetGroup st : targetGroups) {
            cumulative += kicked ? st.getKickedMaxTargets() : st.getMaxTargets();
            if (expandedPosition < cumulative) {
                TargetFilter filter = st.getFilter(kicked);
                if (filter != null) {
                    return filter instanceof PlayerPredicateTargetFilter
                            || filter instanceof AnyTargetPredicateTargetFilter;
                }
                int groupIndex = st.getIndex();
                for (Map.Entry<CardEffect, List<Integer>> entry : effectTargetIndexMap.entrySet()) {
                    if (entry.getValue().contains(groupIndex)
                            && entry.getKey().targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Copies targeting configuration from another card (used by spell copy effects).
     */
    public void copyTargetingFrom(Card original) {
        assertMutable();
        for (TargetGroup st : original.targetGroups) {
            targetGroups.add(st.atIndex(st.getIndex()));
        }
        original.effectTargetIndexMap.forEach((effect, targetIndices) ->
                effectTargetIndexMap.put(effect, new ArrayList<>(targetIndices)));
        opponentChosenSpellTargetIndices = original.opponentChosenSpellTargetIndices;
        castTimeTargetFilter = original.castTimeTargetFilter;
        allowSharedTargets = original.allowSharedTargets;
        syncSpellTargets();
    }

    // ── Effect management ───────────────────────────────────────────

    /**
     * The card's spell, triggered and static abilities, in the order they were added. Activated
     * abilities are still in the per-zone lists ({@link #getActivatedAbilities()} and the others).
     *
     * <p>{@link #addSpellEffect} and {@link #addSimultaneousSpellEffects} build the one
     * {@link SpellAbility}, and {@link #addStaticEffect} adds a {@link StaticAbility} for each effect.
     * Until each other family leaves {@link EffectSlot}, the rest are built from slot registrations by
     * {@link #addEffect(EffectSlot, CardEffect, TriggerMode)} and {@link #addSimultaneousEffects}: a
     * slot's effects form {@link TriggeredAbility}s with a {@link LegacySlotTrigger}, grouped as that
     * slot's collectors group them ({@link LegacySlotGrouping}). The collectors still read the slot
     * views, so the grouping changes nothing yet.
     */
    public List<Ability> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }

    /** The card's spell ability, if it has one (CR 113.3a). */
    public Optional<SpellAbility> getSpellAbility() {
        return Optional.ofNullable(spellAbilityOrNull());
    }

    /** The effects of the card's spell ability in the order they resolve; empty when it has none. */
    public List<CardEffect> getSpellEffects() {
        SpellAbility spell = spellAbilityOrNull();
        return spell == null ? List.of() : LegacySlots.effectsOf(spell);
    }

    /** The registrations of the card's spell ability in the order they resolve; empty when it has none. */
    public List<EffectRegistration> getSpellEffectRegistrations() {
        SpellAbility spell = spellAbilityOrNull();
        return spell == null ? List.of() : spell.effects();
    }

    /**
     * The additional costs to cast this card (CR 601.2b, 601.2f), such as "as an additional cost to
     * cast this spell, sacrifice a creature", multikicker or delve. They are paid as the card is cast,
     * whatever its type, and are not part of its spell ability.
     */
    public List<CostEffect> getCastingCosts() {
        return Collections.unmodifiableList(castingCosts);
    }

    /**
     * The casting costs followed by the spell ability's effects, for the cast paths that take the
     * costs out of one list as the spell is cast ({@code AdditionalSpellCostService.extractAndRemove})
     * and put the rest on the stack.
     */
    public List<CardEffect> getCastingCostsAndSpellEffects() {
        List<CardEffect> spellEffects = getSpellEffects();
        if (castingCosts.isEmpty()) {
            return spellEffects;
        }
        List<CardEffect> all = new ArrayList<>(castingCosts.size() + spellEffects.size());
        all.addAll(castingCosts);
        all.addAll(spellEffects);
        return Collections.unmodifiableList(all);
    }

    /** Adds an additional cost to cast this card; see {@link #getCastingCosts()}. */
    public void addCastingCost(CostEffect cost) {
        assertMutable();
        castingCosts.add(java.util.Objects.requireNonNull(cost, "cost"));
    }

    /**
     * Adds the casting costs and spell effects of {@code source} that {@code keep} accepts, each spell
     * effect on its own, as the engine's copy paths copy every other slot's effects.
     */
    public void addCastingCostsAndSpellEffectsFrom(Card source, Predicate<CardEffect> keep) {
        assertMutable();
        for (CostEffect cost : source.castingCosts) {
            if (keep.test(cost)) {
                addCastingCost(cost);
            }
        }
        for (EffectRegistration registration : source.getSpellEffectRegistrations()) {
            if (keep.test(registration.effect())) {
                addSpellEffect(registration.effect());
            }
        }
    }

    /** Adds an effect to the end of the card's spell ability, creating it if it has none. */
    public void addSpellEffect(CardEffect effect) {
        assertMutable();
        addSpellRegistrations(List.of(new EffectRegistration(effect)));
    }

    /**
     * Adds spell effects that happen at the same time ("deals 4 damage to any target and 2 damage to
     * you"); see {@link #addSimultaneousEffects}.
     */
    public void addSimultaneousSpellEffects(CardEffect... effects) {
        assertMutable();
        addSpellRegistrations(simultaneousGroup(effects, TriggerMode.NORMAL, null));
    }

    /**
     * Adds a static ability made of {@code effect} (CR 113.3d). It functions from the zones the effect
     * names ({@link CardEffect#staticAbilityZones()}): the battlefield (CR 113.6), unless its rule says
     * otherwise, as for kicker.
     */
    public void addStaticEffect(CardEffect effect) {
        addStaticEffect(effect.staticAbilityZones(), effect);
    }

    /**
     * Adds a static ability made of {@code effect} that functions from {@code zones}, such as an
     * Eminence ability that functions in the command zone and on the battlefield (CR 113.6b).
     */
    public void addStaticEffect(AbilityZones zones, CardEffect effect) {
        assertMutable();
        abilities.add(new StaticAbility(List.of(effect), zones));
        slotIndex = null;
    }

    /** The card's static abilities, in the order they were added; each holds one effect. */
    public List<StaticAbility> getStaticAbilities() {
        return slotIndex().staticAbilities();
    }

    /**
     * The effects of every static ability of the card, in the order they were added, whatever zones
     * they function from. The engine knows where it looks at a card and which effects it looks for,
     * so most readers use this; {@link #getStaticEffects(Zone)} filters by zone.
     */
    public List<CardEffect> getStaticEffects() {
        return slotIndex().staticEffects();
    }

    /** The effects of the static abilities that function while the card is in {@code zone}, in order. */
    public List<CardEffect> getStaticEffects(Zone zone) {
        List<StaticAbility> statics = getStaticAbilities();
        if (statics.isEmpty()) {
            return List.of();
        }
        List<CardEffect> effects = new ArrayList<>();
        for (StaticAbility ability : statics) {
            if (ability.zones().functionsIn(zone)) {
                effects.addAll(ability.effects());
            }
        }
        return effects.isEmpty() ? List.of() : Collections.unmodifiableList(effects);
    }

    /**
     * Adds the static abilities of {@code source} whose effect {@code keep} accepts, with their zones,
     * as the engine's copy paths copy every slot's effects.
     */
    public void addStaticAbilitiesFrom(Card source, Predicate<CardEffect> keep) {
        assertMutable();
        for (StaticAbility ability : source.getStaticAbilities()) {
            for (CardEffect effect : ability.effects()) {
                if (keep.test(effect)) {
                    addStaticEffect(ability.zones(), effect);
                }
            }
        }
    }

    /** The effects of every ability in {@code slot}, in the order they were added. */
    public List<CardEffect> getEffects(EffectSlot slot) {
        SlotView view = slotIndex().find(slot);
        return view == null ? List.of() : view.effects();
    }

    /** The registrations of every ability in {@code slot}, in the order they were added. */
    public List<EffectRegistration> getEffectRegistrations(EffectSlot slot) {
        SlotView view = slotIndex().find(slot);
        return view == null ? List.of() : view.registrations();
    }

    public void removeKeyword(Keyword keyword) {
        assertMutable();
        if (keywords.contains(keyword)) {
            var mutable = EnumSet.copyOf(keywords);
            mutable.remove(keyword);
            this.keywords = mutable;
        }
    }

    /** Removes every registered effect matching {@code filter} from every slot, such as a copy exception's omissions. */
    public void removeEffectsIf(Predicate<CardEffect> filter) {
        removeSlotEffectsIf((slot, effect) -> filter.test(effect));
    }

    /**
     * Removes every registered effect for which {@code filter} holds, given the effect's slot, which is
     * {@code null} for a spell effect, a static effect or a casting cost. An ability left without effects
     * is removed; an effect that was simultaneous with a removed one becomes the first of its ability,
     * so it is no longer marked simultaneous.
     */

    public void removeSlotEffectsIf(BiPredicate<EffectSlot, CardEffect> filter) {
        removeAbilityEffectsIf((ability, effect) -> filter.test(ability == null ? null : slotOf(ability), effect));
    }

    /**
     * Removes every registered effect for which {@code filter} holds, given the ability that holds the
     * effect, which is {@code null} for a casting cost. An ability left without effects is removed, as
     * for {@link #removeSlotEffectsIf}.
     */
    public void removeAbilityEffectsIf(BiPredicate<Ability, CardEffect> filter) {
        assertMutable();
        List<Ability> kept = new ArrayList<>(abilities.size());
        for (Ability ability : abilities) {
            Ability rest = withoutEffects(ability, filter);
            if (rest != null) {
                kept.add(rest);
            }
        }
        abilities = kept;
        slotIndex = null;
        castingCosts.removeIf(cost -> filter.test(null, cost));
        syncSpellTargets();
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        addEffect(slot, effect, TriggerMode.NORMAL);
    }

    public void addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        assertMutable();
        validateEffectSlotType(slot, effect);
        addRegistrations(slot, List.of(new EffectRegistration(effect, triggerMode)));
    }

    /**
     * Adds effects that happen at the same time ("deals 4 damage to any target and 2 damage to
     * you"), so the damage they deal is one damage event: a lifelink source gains life once for all
     * of it. Every separate effect is otherwise its own event. Effects bound to different target
     * groups are attached with {@code SpellTarget.bindEffect} first and added here. Steps inside a
     * wrapper effect or an activated ability's effect list go in a
     * {@code SequenceEffect.simultaneous} instead. The effects are always one ability.
     */
    public void addSimultaneousEffects(EffectSlot slot, CardEffect... effects) {
        addSimultaneousEffects(slot, TriggerMode.NORMAL, effects);
    }

    public void addSimultaneousEffects(EffectSlot slot, TriggerMode triggerMode, CardEffect... effects) {
        assertMutable();
        addRegistrations(slot, simultaneousGroup(effects, triggerMode, slot));
    }

    private List<EffectRegistration> simultaneousGroup(CardEffect[] effects, TriggerMode triggerMode, EffectSlot slot) {
        if (effects.length < 2) {
            throw new IllegalArgumentException("Simultaneous effects need at least two effects, got " + effects.length);
        }
        List<EffectRegistration> group = new ArrayList<>(effects.length);
        for (int i = 0; i < effects.length; i++) {
            if (slot != null) {
                validateEffectSlotType(slot, effects[i]);
            }
            group.add(new EffectRegistration(effects[i], triggerMode, i > 0));
        }
        return group;
    }

    /**
     * Adds {@code ability} the way {@link #addEffect} and {@link #addSimultaneousEffects} add its
     * effects, so it joins an ability of its slot that it would have joined as registrations.
     */
    public void addAbility(Ability ability) {
        assertMutable();
        if (ability instanceof StaticAbility statik) {
            // Each effect is a static ability of its own, as addStaticEffect adds it.
            statik.effects().forEach(effect -> addStaticEffect(statik.zones(), effect));
            return;
        }
        EffectSlot slot = slotOf(ability);
        List<EffectRegistration> registrations = switch (ability) {
            // Its target groups are this card's first groups, so they follow from this card's own.
            case SpellAbility spell -> spell.effects();
            case TriggeredAbility triggered -> triggered.effects();
            case StaticAbility statik -> throw new IllegalStateException("Added above");
            case ActivatedAbility activated -> throw new IllegalArgumentException(
                    "Activated abilities are added with addActivatedAbility");
        };
        if (slot == null) {
            addSpellRegistrations(registrations);
            return;
        }
        List<EffectRegistration> group = new ArrayList<>();
        for (EffectRegistration registration : registrations) {
            if (!registration.simultaneousWithPrevious() && !group.isEmpty()) {
                addRegistrations(slot, group);
                group = new ArrayList<>();
            }
            validateEffectSlotType(slot, registration.effect());
            group.add(registration);
        }
        addRegistrations(slot, group);
    }

    /** Whether {@code effect} is registered as happening at the same time as the effect before it. */
    public boolean isSimultaneousWithPrevious(CardEffect effect) {
        for (EffectRegistration reg : getSpellEffectRegistrations()) {
            if (reg.effect() == effect) {
                return reg.simultaneousWithPrevious();
            }
        }
        for (SlotView view : slotIndex().views()) {
            for (EffectRegistration reg : view.registrations()) {
                if (reg.effect() == effect) {
                    return reg.simultaneousWithPrevious();
                }
            }
        }
        return false;
    }

    private void validateEffectSlotType(EffectSlot slot, CardEffect effect) {
        if (slot == EffectSlot.STATE_TRIGGERED && !(effect instanceof StateTriggerEffect)) {
            throw new IllegalArgumentException(
                    "STATE_TRIGGERED slot requires StateTriggerEffect, got " + effect.getClass().getSimpleName());
        }
    }

    /**
     * Adds {@code group}, one registration or a group of simultaneous ones, to the ability of
     * {@code slot} that it joins, or as a new ability. A group always stays in one ability.
     */
    private void addRegistrations(EffectSlot slot, List<EffectRegistration> group) {
        EffectRegistration first = group.getFirst();
        int index = switch (LegacySlotGrouping.of(slot)) {
            case EACH -> -1;
            case ALL -> indexOfFirstAbility(slot);
            case ALL_BUT_INDEPENDENT -> first.triggerMode() == TriggerMode.INDEPENDENT
                    ? -1
                    : indexOfSharedAbility(slot);
        };
        if (index < 0) {
            abilities.add(new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT, group));
        } else {
            TriggeredAbility ability = (TriggeredAbility) abilities.get(index);
            abilities.set(index, ability.withEffects(concat(ability.effects(), group)));
        }
        slotIndex = null;
    }

    /**
     * Adds {@code group}, one registration or a group of simultaneous ones, to the end of the spell
     * ability, which is created where the first one is added. A spell ability doesn't trigger, so its
     * effects keep no trigger mode.
     */
    private void addSpellRegistrations(List<EffectRegistration> group) {
        List<EffectRegistration> spellGroup = group.stream()
                .map(registration -> registration.triggerMode() == TriggerMode.NORMAL ? registration
                        : new EffectRegistration(registration.effect(), TriggerMode.NORMAL,
                        registration.simultaneousWithPrevious()))
                .toList();
        int index = indexOfSpellAbility();
        if (index < 0) {
            abilities.add(new SpellAbility(spellGroup));
        } else {
            SpellAbility spell = (SpellAbility) abilities.get(index);
            abilities.set(index, spell.withEffects(concat(spell.effects(), spellGroup)));
        }
        syncSpellTargets();
    }

    private int indexOfSpellAbility() {
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i) instanceof SpellAbility) {
                return i;
            }
        }
        return -1;
    }

    private SpellAbility spellAbilityOrNull() {
        for (Ability ability : abilities) {
            if (ability instanceof SpellAbility spell) {
                return spell;
            }
        }
        return null;
    }

    /**
     * Keeps the spell ability's target groups in step with the card's: they are the card's leading
     * groups that are bound to a spell effect or to no effect at all (a positional group another
     * effect reads), up to the first group bound only to other abilities' effects.
     */
    private void syncSpellTargets() {
        int index = indexOfSpellAbility();
        if (index < 0) {
            return;
        }
        SpellAbility spell = (SpellAbility) abilities.get(index);
        Set<Integer> spellBound = new java.util.HashSet<>();
        for (EffectRegistration registration : spell.effects()) {
            List<Integer> indices = effectTargetIndexMap.get(registration.effect());
            if (indices != null) {
                spellBound.addAll(indices);
            }
        }
        Set<Integer> bound = new java.util.HashSet<>();
        effectTargetIndexMap.values().forEach(bound::addAll);
        int count = 0;
        while (count < targetGroups.size() && (spellBound.contains(count) || !bound.contains(count))) {
            count++;
        }
        List<TargetGroup> targets = List.copyOf(targetGroups.subList(0, count));
        if (!targets.equals(spell.targets())) {
            abilities.set(index, spell.withTargets(targets));
        }
    }

    private int indexOfFirstAbility(EffectSlot slot) {
        for (int i = 0; i < abilities.size(); i++) {
            if (slotOf(abilities.get(i)) == slot) {
                return i;
            }
        }
        return -1;
    }

    /** The ability of an all-but-independent slot that its non-independent registrations share. */
    private int indexOfSharedAbility(EffectSlot slot) {
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i) instanceof TriggeredAbility ability && slotOf(ability) == slot
                    && ability.effects().getFirst().triggerMode() != TriggerMode.INDEPENDENT) {
                return i;
            }
        }
        return -1;
    }

    private static List<EffectRegistration> concat(List<EffectRegistration> head, List<EffectRegistration> tail) {
        List<EffectRegistration> all = new ArrayList<>(head.size() + tail.size());
        all.addAll(head);
        all.addAll(tail);
        return all;
    }

    /** {@code ability} without the effects {@code filter} removes, itself if none, or null if it has none left. */
    private static Ability withoutEffects(Ability ability, BiPredicate<Ability, CardEffect> filter) {
        return switch (ability) {
            case SpellAbility spell -> {
                List<EffectRegistration> kept = keptRegistrations(spell, spell.effects(), filter);
                yield kept == spell.effects() ? spell : kept.isEmpty() ? null : spell.withEffects(kept);
            }
            case TriggeredAbility triggered -> {
                List<EffectRegistration> kept = keptRegistrations(triggered, triggered.effects(), filter);
                yield kept == triggered.effects() ? triggered : kept.isEmpty() ? null : triggered.withEffects(kept);
            }
            case StaticAbility statik -> {
                List<CardEffect> kept = statik.effects().stream()
                        .filter(effect -> !filter.test(statik, effect))
                        .toList();
                yield kept.size() == statik.effects().size() ? statik
                        : kept.isEmpty() ? null : new StaticAbility(kept, statik.zones());
            }
            case ActivatedAbility activated -> activated;
        };
    }

    /**
     * The registrations of {@code ability} without those {@code filter} removes, the same list if it
     * removes none. The first one left is no longer marked simultaneous, since the effect before it is gone.
     */
    private static List<EffectRegistration> keptRegistrations(Ability ability,
                                                              List<EffectRegistration> registrations,
                                                              BiPredicate<Ability, CardEffect> filter) {
        List<EffectRegistration> kept = new ArrayList<>(registrations.size());
        for (EffectRegistration reg : registrations) {
            if (!filter.test(ability, reg.effect())) {
                kept.add(reg);
            }
        }
        if (kept.size() == registrations.size()) {
            return registrations;
        }
        if (!kept.isEmpty() && kept.getFirst().simultaneousWithPrevious()) {
            EffectRegistration first = kept.getFirst();
            kept.set(0, new EffectRegistration(first.effect(), first.triggerMode(), false));
        }
        return kept;
    }

    private static EffectSlot slotOf(Ability ability) {
        return LegacySlots.slotOf(ability);
    }

    private SlotIndex slotIndex() {
        SlotIndex index = slotIndex;
        if (index == null) {
            index = SlotIndex.of(abilities);
            slotIndex = index;
        }
        return index;
    }

    /**
     * One slot's view: the registrations and effects of its abilities in the order they were added,
     * and the slot's first triggered ability, which holds a Saga chapter's targets.
     */
    private record SlotView(EffectSlot slot, List<EffectRegistration> registrations, List<CardEffect> effects,
                            TriggeredAbility firstTriggeredAbility) {
    }

    /**
     * The views of every slot that holds abilities, in slot order, and the static abilities and their
     * effects in the order they were added. It is immutable and refers to no card, so frozen cards and
     * their copies can share it across threads; two threads that both build it build the same index.
     */
    private record SlotIndex(List<SlotView> views, List<StaticAbility> staticAbilities,
                             List<CardEffect> staticEffects) {

        static SlotIndex of(List<Ability> abilities) {
            Map<EffectSlot, List<TriggeredAbility>> bySlot = new TreeMap<>();
            List<StaticAbility> statics = new ArrayList<>();
            List<CardEffect> staticEffects = new ArrayList<>();
            for (Ability ability : abilities) {
                if (ability instanceof StaticAbility statik) {
                    statics.add(statik);
                    staticEffects.addAll(statik.effects());
                } else if (ability instanceof TriggeredAbility triggered) {
                    bySlot.computeIfAbsent(slotOf(triggered), slot -> new ArrayList<>()).add(triggered);
                }
            }
            List<SlotView> views = new ArrayList<>(bySlot.size());
            bySlot.forEach((slot, slotAbilities) -> {
                List<EffectRegistration> registrations = new ArrayList<>();
                for (TriggeredAbility triggered : slotAbilities) {
                    registrations.addAll(triggered.effects());
                }
                views.add(new SlotView(slot, List.copyOf(registrations),
                        registrations.stream().map(EffectRegistration::effect).toList(), slotAbilities.getFirst()));
            });
            return new SlotIndex(List.copyOf(views), List.copyOf(statics), List.copyOf(staticEffects));
        }

        SlotView find(EffectSlot slot) {
            for (SlotView view : views) {
                if (view.slot() == slot) {
                    return view;
                }
            }
            return null;
        }
    }

    public void addCastingOption(CastingOption option) {
        assertMutable();
        castingOptions.add(option);
    }

    public void setRoomDoorManaCosts(List<String> roomDoorManaCosts) {
        assertMutable();
        if (roomDoorManaCosts == null || roomDoorManaCosts.size() != 2
                || roomDoorManaCosts.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("A Room must have exactly two door mana costs");
        }
        this.roomDoorManaCosts = List.copyOf(roomDoorManaCosts);
    }

    public void setSelectedRoomDoor(int selectedRoomDoor) {
        assertMutable();
        if (selectedRoomDoor < 0 || selectedRoomDoor >= roomDoorManaCosts.size()) {
            throw new IllegalArgumentException("Invalid Room door index: " + selectedRoomDoor);
        }
        this.selectedRoomDoor = selectedRoomDoor;
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
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph/disguise with a dynamic generic reduction to its face-up cost. */
    public void addMorph(String morphCost, DynamicAmount morphCostReduction) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = morphCostReduction;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph with a card that must be revealed from hand as part of the face-down cost. */
    public void addMorph(String morphCost, CardPredicate revealPredicate, String revealLabel) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{3}"),
                new RevealCardsFromHandCastingCost(revealPredicate, revealLabel))));
    }

    /** Adds morph with a permanent-return component to its face-up cost. */
    public void addMorph(String morphCost, ReturnPermanentsCost additionalCost) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = additionalCost;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph whose face-up cost includes sacrificing matching permanents. */
    public void addMorph(String morphCost, SacrificePermanentsCost additionalCost) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = additionalCost;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph whose face-up cost is discarding a card matching the supplied cost. */
    public void addMorph(String morphCost, DiscardCardTypeCost additionalCost) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = additionalCost;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph whose face-up cost includes a life payment. */
    public void addMorph(String morphCost, LifeCastingCost additionalCost) {
        assertMutable();
        this.morphCost = morphCost;
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = additionalCost;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds morph whose face-up cost is revealing a matching card from hand. */
    public void addMorphWithRevealCost(CardPredicate revealPredicate, String revealLabel) {
        assertMutable();
        this.morphCost = "{0}";
        this.spellmorph = false;
        this.morphCostReduction = null;
        this.morphRevealCost = new RevealCardsFromHandCastingCost(revealPredicate, revealLabel);
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    /** Adds spellmorph and its standard face-down alternate casting cost. */
    public void addSpellmorph(String spellmorphCost) {
        assertMutable();
        this.morphCost = spellmorphCost;
        this.spellmorph = true;
        this.morphCostReduction = null;
        this.morphRevealCost = null;
        this.morphAdditionalCost = null;
        this.morphSacrificeCost = null;
        this.morphDiscardCost = null;
        this.morphLifeCost = null;
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }

    public <T extends CastingOption> Optional<T> getCastingOption(Class<T> type) {
        return castingOptions.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    /** Whether this card's casting permissions prohibit casting it from every zone except graveyard. */
    public boolean isCastOnlyFromGraveyard() {
        return getCastingOption(GraveyardCast.class)
                .map(GraveyardCast::onlyFromGraveyard)
                .orElse(false);
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

    /**
     * Sets the filters of the single target of the Saga chapter ability in {@code slot}. The
     * chapter's effects must be added first, since the targets are held by its ability.
     */
    public void setSagaChapterTargetFilter(EffectSlot slot, Set<TargetFilter> filters) {
        assertMutable();
        changeChapterTargets(slot, targets -> targets.withFilters(filters));
    }

    public Set<TargetFilter> getSagaChapterTargetFilters(EffectSlot slot) {
        return chapterTargets(slot).filters();
    }

    /**
     * Marks a Saga chapter's single target as mandatory: it must be chosen whenever a legal one
     * exists. The chapter's effects must be added first.
     */
    public void setSagaChapterTargetRequired(EffectSlot slot) {
        assertMutable();
        changeChapterTargets(slot, SagaChapterTargets::withRequired);
    }

    public boolean isSagaChapterTargetRequired(EffectSlot slot) {
        return chapterTargets(slot).required();
    }

    /** Sets the target groups of a Saga chapter with more than one target. The chapter's effects must be added first. */
    public void setSagaChapterTargetGroups(EffectSlot slot, List<SagaChapterTargetGroup> groups) {
        assertMutable();
        changeChapterTargets(slot, targets -> targets.withGroups(groups));
    }

    public List<SagaChapterTargetGroup> getSagaChapterTargetGroups(EffectSlot slot) {
        return chapterTargets(slot).groups();
    }

    private SagaChapterTargets chapterTargets(EffectSlot slot) {
        SlotView view = slotIndex().find(slot);
        return view == null || view.firstTriggeredAbility() == null
                ? SagaChapterTargets.NONE
                : view.firstTriggeredAbility().chapterTargets();
    }

    private void changeChapterTargets(EffectSlot slot, UnaryOperator<SagaChapterTargets> change) {
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i) instanceof TriggeredAbility ability && slotOf(ability) == slot) {
                abilities.set(i, ability.withChapterTargets(change.apply(ability.chapterTargets())));
                slotIndex = null;
                return;
            }
        }
        throw new IllegalStateException("'" + name + "' has no ability in " + slot
                + " to hold its targets; add the chapter's effects before its targets");
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

    public void addStackActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        stackActivatedAbilities.add(ability);
    }

    /**
     * Adds plain cycling for {@code cost} — "{@code Cost, Discard this card: Draw a card.}"
     *
     * <p>Both halves of this matter to the engine, which is why it is one call: cycling is only
     * activatable from hand, and {@link ActivatedAbility#isCyclingAbility()} recognises it by the
     * description beginning with "Cycling", so the reminder text is built from the cost here
     * rather than retyped per card.
     *
     * <p>Abilities triggered by cycling (such as the Sojourners and Resounding cycles) belong
     * in {@link EffectSlot#ON_SELF_CYCLED}; they trigger separately from the cycling draw ability.
     * Typecycling and landcycling instead use a hand activated ability that searches rather
     * than draws.
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
        addHandActivatedAbility(ninjutsuAbility(cost));
    }

    /** Builds a ninjutsu ability for a card in hand, including the engine's special activation flag. */
    public static ActivatedAbility ninjutsuAbility(String cost) {
        return new ActivatedAbility(false, cost,
                List.of(new NinjutsuEffect()),
                "Ninjutsu " + cost + " (" + cost + ", Return an unblocked attacker you control to hand: "
                        + "Put this card onto the battlefield from your hand tapped and attacking.)")
                .withNinjutsu();
    }

    /** Adds Sneak for {@code cost}. */
    public void addSneak(String cost) {
        addCastingOption(AlternateHandCast.sneak(cost));
    }

    /**
     * Adds unearth for {@code cost}: return this card from the graveyard to the battlefield with
     * haste, exiled at the beginning of the next end step, sorcery speed only.
     *
     * <p>Only for a card that has unearth itself. Sedris, the Traitor King grants unearth to
     * <em>other</em> creatures and is a different effect entirely.
     */
    public void addUnearth(String cost) {
        addGraveyardActivatedAbility(unearthAbility(cost));
    }

    /** Builds the unearth graveyard-activated ability for {@code cost}. */
    public static ActivatedAbility unearthAbility(String cost) {
        return new ActivatedAbility(false, cost,
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
                ActivationTimingRestriction.SORCERY_SPEED);
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
        this.combinedManaValue = null;
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
        if (combinedManaValue != null) return combinedManaValue;
        ManaCost cost = getParsedManaCost();
        return cost != null ? cost.getManaValue() : 0;
    }

    /**
     * The smallest mana value this card can have as a spell. Only the half of a split card being
     * cast is put onto the stack (CR 709.3a), so its cheaper half counts; any other card counts
     * with its own mana value.
     */
    public int getSmallestSpellManaValue() {
        return splitHalves.size() < 2 ? getManaValue() : splitHalves.stream()
                .mapToInt(half -> half.manaCost() == null ? 0 : new ManaCost(half.manaCost()).getManaValue())
                .min()
                .orElse(getManaValue());
    }

    public boolean hasType(CardType cardType) {
        return type == cardType || additionalTypes.contains(cardType);
    }

    public boolean hasKeyword(Keyword keyword) {
        return keywords.contains(keyword)
                || (keyword == Keyword.CHANGELING && getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                        && grant.scope() == GrantScope.SELF));
    }

    public boolean hasAllCardNames() {
        return getStaticEffects().stream().anyMatch(AllCardNamesEffect.class::isInstance);
    }

    /**
     * The names this card can be chosen by: each half's name for a split card, otherwise its one name
     * (CR 709.4a). The combined "A // B" name is never a name a player can choose.
     */
    public List<String> getNames() {
        if (name == null) {
            return List.of();
        }
        return name.contains(" // ") ? List.of(name.split(" // ")) : List.of(name);
    }

    /** Whether this card has the chosen name, including either half of a split card (CR 709.4a). */
    public boolean hasName(String chosenName) {
        return chosenName != null && (hasAllCardNames() || getNames().contains(chosenName)
                || chosenName.equals(name));
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
        if (!getEffects(EffectSlot.SAGA_CHAPTER_VI).isEmpty()) return 6;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_V).isEmpty()) return 5;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_IV).isEmpty()) return 4;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_III).isEmpty()) return 3;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_II).isEmpty()) return 2;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_I).isEmpty()) return 1;
        return 0;
    }

    public boolean isEnchantPlayer() {
        return isAura() && (subtypes.contains(CardSubtype.CURSE) || enchantPlayer);
    }

    /** Whether this Aura needs a player/permanent attachment target when cast or entering. */
    public boolean isAuraThatRequiresAttachment() {
        return isAura() && !enchantZone;
    }

}
