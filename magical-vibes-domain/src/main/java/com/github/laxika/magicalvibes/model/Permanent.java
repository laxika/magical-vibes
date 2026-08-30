package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class Permanent {

    private final UUID id;
    private Card card;
    private final Card originalCard;
    @Setter private boolean bestow;
    /** The graveyard card currently supplying this permanent's dynamic full-text copy, if any. */
    @Setter private Card fullTextCopySourceCard;
    private boolean tapped;
    /** Whether this permanent was untapped before its controller's most recent untap step. */
    @Setter private boolean untappedAtTurnStart;
    private int untapSequence;
    private int controlChangeSequence;
    /** True once the "sacrifice a [permanent] instead of entering" replacement (Balduvian Trading
     *  Post) has been paid for this permanent, so the re-entry after the choice isn't replaced again. */
    @Setter private boolean entryCostPaid;
    private boolean attacking;
    /** The UUID of the player or planeswalker this creature is attacking. Null when not attacking. */
    @Setter private UUID attackTarget;
    private boolean attackedThisTurn;
    private boolean attackedThisCombat;
    /** Creatures that were tapped to pay this Vehicle's crew cost during the current turn. */
    private final Set<UUID> creaturesThatCrewedThisTurn = new HashSet<>();
    /** Set when this creature is declared as an attacker; unlike {@link #attackedThisTurn} it survives
     *  the intervening opponent turns and is rolled into {@link #attackedDuringControllersLastTurn} at
     *  the start of its controller's next turn by {@link #rollOverAttackRecord()}. */
    @Setter private boolean attackedDuringControllersCurrentTurn;
    /** True when this creature attacked during its controller's previous turn (Halls of Mist). Read by
     *  {@code PermanentAttackedDuringControllersLastTurnPredicate}. */
    @Setter private boolean attackedDuringControllersLastTurn;
    /** When true, this creature can't be declared as an attacker during its controller's next turn
     *  (Wall of Dust's block trigger). Promoted into {@link #cantAttackThisTurn} at the start of the
     *  controller's next turn by {@link #promoteCantAttackNextTurn()}. */
    @Setter private boolean cantAttackNextTurn;
    /** When true, this creature can't be declared as an attacker this turn. Armed from
     *  {@link #cantAttackNextTurn} at the controller's turn start and self-clears the following
     *  controller turn, so the restriction lasts exactly one turn. */
    @Setter private boolean cantAttackThisTurn;
    private boolean blocking;
    /** Set when this creature is declared as a blocker; persists after combat ends (mirroring
     *  {@link #attackedThisTurn}) and is reset at each turn start. Read by
     *  {@code PermanentAttackedOrBlockedThisTurnPredicate} (Vizier of Deferment). */
    private boolean blockedThisTurn;
    private boolean blockedThisCombat;
    /** Set when this creature blocks or becomes blocked. Unlike {@link #blockedThisTurn} it is NOT
     *  cleared at turn start — it stays set until an effect that reads a "since your last upkeep"
     *  window consumes it (Wiitigo's upkeep trigger clears it after resolving). */
    @Setter private boolean blockedOrWasBlockedSinceLastUpkeep;
    /** Set when this permanent attacks, blocks, or becomes blocked and consumed by a
     *  "since your last upkeep" effect. */
    @Setter private boolean attackedOrBlockedSinceLastUpkeep;
    /** Set when this permanent's "whenever this becomes the target of a spell or ability for the first
     *  time each turn, counter that spell or ability" trigger (Glyph Keeper) has already fired this
     *  turn, so it won't trigger again until the flag is reset at the next turn start. */
    @Setter private boolean becomeTargetCounterUsedThisTurn;
    /** Set when this permanent's backup-ability copy trigger has already fired this turn. */
    @Setter private boolean backupAbilityCopyUsedThisTurn;
    private final List<Integer> blockingTargets = new ArrayList<>();
    private final List<UUID> blockingTargetIds = new ArrayList<>();
    /** Identifies the attacking band (CR 702.22) this creature was declared in, or null if it is not
     *  in a band. Shared by every member of the same band. Set at declare-attackers time and cleared
     *  by {@link #clearCombatState()} when combat ends; persists for the rest of combat even if banding
     *  is later removed (CR 702.22e). */
    @Setter private UUID bandId;
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;
    @Setter private int damagePreventionShield;
    /** Sacred Boon-style shield: damage prevented by this shield is converted into +0/+1 counters on
     *  this permanent at the beginning of the next end step. Consumed alongside {@link #damagePreventionShield}
     *  in {@code DamagePreventionService.applyCreaturePreventionShield}; reset at turn cleanup. */
    @Setter private int damageToCounterPreventionShield;
    /** Temper-style shield: damage prevented by this shield is converted into +1/+1 counters on this
     *  permanent as the damage is prevented. Reset at turn cleanup. */
    @Setter private int damageToPlusOnePlusOneCounterPreventionShield;
    /** Brace for Impact-style shield: prevent all damage to this permanent and convert it into +1/+1
     *  counters as the damage is prevented. Reset at turn cleanup. */
    @Setter private boolean allDamageToPlusOnePlusOneCounterPreventionShield;
    /** Kill-Suit Cultist-style shield: destroy this creature instead of dealing the next damage to it.
     *  Reset at turn cleanup. */
    @Setter private int damageDestructionShield;
    @Setter private int regenerationShield;
    /** How many of this permanent's {@link #regenerationShield}s carry Soldevi Sentry's rider — when
     *  such a shield is actually used, the controller's opponent may draw a card. Plain shields are
     *  consumed first, so a rider shield is only spent once it is all that is left. Reset at turn
     *  cleanup alongside {@link #regenerationShield}. */
    @Setter private int opponentDrawRegenerationShield;
    /** How many of this permanent's {@link #regenerationShield}s carry Matopi Golem's rider — when
     *  such a shield is actually used, put a -1/-1 counter on this permanent. Plain shields are
     *  consumed first. Reset at turn cleanup alongside {@link #regenerationShield}. */
    @Setter private int minusOneCounterRegenerationShield;
    /** How many of this permanent's {@link #regenerationShield}s carry Skeleton Scavengers' rider —
     *  when such a shield is actually used, put a +1/+1 counter on this permanent. Plain shields are
     *  consumed first. Reset at turn cleanup alongside {@link #regenerationShield}. */
    @Setter private int plusOnePlusOneCounterRegenerationShield;
    /** Controller ids of this permanent's {@link #regenerationShield}s that carry Debt of Loyalty's
     *  rider — when such a shield is actually used, that player gains control of this permanent.
     *  Plain shields are consumed first, so a rider shield is only spent once it is all that is left.
     *  Cleared at turn cleanup alongside {@link #regenerationShield}. */
    private final List<UUID> gainControlRegenerationShields = new ArrayList<>();
    /** How many times this permanent has regenerated this turn (CR 701.15). Incremented every time a
     *  regeneration shield is actually applied; reset at turn cleanup. Read by
     *  {@code TimesSourceRegeneratedThisTurn} for Spiny Starfish. */
    @Setter private int timesRegeneratedThisTurn;
    private UUID attachedTo;
    /**
     * The last permanent this one was attached to, kept after {@link #attachedTo} is cleared.
     * Triggers that fire once the host has already left the battlefield (Kusari-Gama's "whenever
     * equipped creature deals damage …", where the host may have died to the same combat damage)
     * read it as last-known information.
     */
    private UUID lastAttachedTo;
    /**
     * Soulbond pairing (CR 702.94): id of the other creature this permanent is paired with, or
     * {@code null} when unpaired. Cleared when either leaves the battlefield, changes controller,
     * or ceases to be a creature.
     */
    @Setter private UUID pairedWithId;
    private CardColor chosenColor;
    /**
     * Every color chosen for this permanent. Cards that choose a single color keep this in sync with
     * {@link #chosenColor} (see {@link #setChosenColor(CardColor)}); cards that choose several
     * distinct colors (Tablet of the Guilds) write here directly and leave {@code chosenColor} null.
     */
    private final Set<CardColor> chosenColors = EnumSet.noneOf(CardColor.class);
    @Setter private String chosenName;
    /** Second card name chosen "as this enters" when two players each name a card
     *  (Null Chamber: the controller's pick → {@link #chosenName}, the opponent's → here). */
    @Setter private String secondChosenName;
    @Setter private CardSubtype chosenSubtype;
    @Setter private CardType chosenCardType;
    /** Second basic land type chosen "as this enters" when the card chooses two types
     *  (Illusionary Terrain: first type → {@link #chosenSubtype}, second → here). */
    @Setter private CardSubtype secondChosenSubtype;
    @Setter private String chosenMode;
    /** The number last chosen for this permanent by a "choose a number between X and Y" effect
     *  (e.g. Shapeshifter). Read by {@link com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource}
     *  to drive a characteristic-defining P/T. Defaults to 0 until a number is chosen. */
    @Setter private int chosenNumber;
    /**
     * Labels of the modes this permanent has already had chosen for a "choose one that hasn't been
     * chosen" modal trigger (Demonic Pact). Consumed modes are never offered again while this object
     * stays on the battlefield.
     */
    private final Set<String> chosenModeLabels = new HashSet<>();
    /** Labels of modes chosen this turn for a turn-scoped modal trigger. */
    private final Set<String> chosenModeLabelsThisTurn = new HashSet<>();
    @Setter private ManaValueParity chosenManaValueParity;
    @Setter private UUID chosenPermanentId;
    /** Player targeted by a linked enter-the-battlefield ability. */
    @Setter private UUID rememberedTargetPlayerId;
    /** Life lost by the controller when this permanent's Soulgorger Orgg-style ETB resolved. */
    @Setter private int lifeLostWhenEntered;
    /** Player who lost the recorded life when this permanent's Soulgorger Orgg-style ETB resolved. */
    @Setter private UUID lifeLostWhenEnteredControllerId;
    /** Permanents tapped this turn to pay for activated abilities whose source tracks that payment. */
    private final List<UUID> tappedPermanentsForAbilityThisTurn = new ArrayList<>();
    /**
     * Card of the permanent last chosen/sacrificed as payment for an ability that needs its printed
     * characteristics after that permanent has left the battlefield (Squandered Resources: the land
     * sacrificed for "add one mana of any type that land could produce").
     */
    @Setter private Card chosenCard;
    /** The creature card exiled with this permanent most recently chosen for ability copying. */
    @Setter private Card lastChosenExiledCard;
    /** Last-known snapshot of a permanent sacrificed as payment for an ability that needs it at resolution. */
    @Setter private Permanent chosenSacrificedPermanentSnapshot;
    @Setter private boolean cantBeBlocked;
    @Setter private boolean cantBlockThisTurn;
    @Setter private boolean cantBlockThisCombat;
    /** True while this permanent has the suspected designation. Cleared only by a leave-and-return
     *  event or an effect that specifically makes it no longer suspected. */
    @Setter private boolean suspected;
    /** Extra creatures this permanent may block this turn beyond the base one, granted by a one-shot
     *  effect (e.g. Act of Heroism). Stacks on top of any static "can block an additional creature"
     *  grants counted in {@code CombatBlockService}. Cleared at end of turn by {@link #resetModifiers()}. */
    @Setter private int additionalBlocksUntilEndOfTurn;
    /** When true, this creature must be declared as a blocker this turn if it can block any attacker
     *  (general "blocks this turn if able", e.g. Nacatl Hunt-Pride). Cleared at end of turn. */
    @Setter private boolean mustBlockThisTurnIfAble;
    /** When true, this creature must attack during the current combat if able. Cleared when combat ends. */
    @Setter private boolean mustAttackThisCombat;
    @Setter private boolean mustAttackThisTurn;
    /** When non-null, this creature must attack this specific player (not their planeswalkers). Cleared at end of turn. */
    @Setter private UUID mustAttackTargetId;
    /** When true, at least one creature must block this creature this turn if able (e.g. Emergent Growth). Cleared at end of turn. */
    @Setter private boolean mustBeBlockedThisTurn;
    /** When true, all creatures able to block this creature this turn do so (Lure-style, one-shot, e.g. Alluring Scent). Cleared at end of turn. */
    @Setter private boolean mustBeBlockedByAllThisTurn;
    /** When true, this attacking creature is blocked even though no creature is blocking it
     *  (Dazzling Beauty). CR 509.1h — it deals no combat damage, since a blocked creature with no
     *  blockers to assign damage to assigns none. Cleared at end of turn. */
    @Setter private boolean blockedWithoutBlockers;
    /** When true, this Aura's static effects are ignored until end of turn — the enchanted
     *  permanent's controller paid the Aura's "ignore this effect" cost (Volrath's Curse,
     *  {@code IgnoreSourceAuraEffectsUntilEndOfTurnEffect}). Read by
     *  {@code GameQueryService.hasAuraWithEffect}. Cleared at end of turn. */
    @Setter private boolean auraEffectsIgnoredThisTurn;
    /** When true, this Damping Engine's restriction is ignored until end of turn. */
    @Setter private boolean dampingEngineEffectIgnoredThisTurn;
    @Setter private boolean cantRegenerateThisTurn;
    /** If true, damage that would be dealt to this creature can't be prevented or redirected this turn. */
    @Setter private boolean damageCantBePreventedOrRedirectedThisTurn;
    /** When true, creatures this permanent dealt damage to this turn can't be regenerated this turn
     *  (Bone Shaman's activated ability). Cleared at end of turn. */
    @Setter private boolean damagedCreaturesCantRegenerateThisTurn;
    /** When true, creatures dealt damage by this permanent this turn are exiled instead of dying.
     *  Cleared at end of turn. */
    @Setter private boolean exileDamagedCreaturesInsteadOfDyingThisTurn;
    /** If true, this creature is exiled instead of dying this turn (e.g. Red Sun's Zenith). Cleared at end of turn. */
    @Setter private boolean exileInsteadOfDieThisTurn;
    /** If true, this permanent's controller sacrifices it at the beginning of the next cleanup step —
     *  the Mirage flash clause ({@code FlashCastWithCleanupSacrificeEffect}) when the spell was cast
     *  any time a sorcery couldn't have been cast. The cleanup sweep sacrifices it before
     *  {@link #resetModifiers()} runs, so the flag deliberately survives that reset. */
    @Setter private boolean sacrificeAtNextCleanup;
    /** If true, this permanent is returned to its owner's hand at the beginning of the next cleanup step
     *  ({@code ReturnSourceToHandAtNextCleanupEffect}, Thawing Glaciers). Like
     *  {@link #sacrificeAtNextCleanup} the sweep runs before {@link #resetModifiers()}, so the flag
     *  deliberately survives that reset. */
    @Setter private boolean returnToHandAtNextCleanup;
    /** If true, this permanent is returned to its owner's hand during its controller's next untap step
     *  as that player untaps their permanents ({@code ReturnSourceToHandAtNextUntapEffect},
     *  Undiscovered Paradise). Swept by {@code UntapStepService} (not during a skipped untap step).
     *  Survives {@link #resetModifiers()}. */
    @Setter private boolean returnToHandAtNextUntap;
    /** Secrets of Strixhaven "Prepared": true while this permanent is prepared (CR-style designation). While
     *  prepared, a copy of its prepare spell sits in exile (see {@link #preparedSpellCardId}) and the controller
     *  may cast it; casting it unprepares this permanent. */
    @Setter private boolean prepared;
    /** Phasing (CR 702.26g): true while this permanent is phased out only because the permanent it is attached
     *  to phased out ("indirectly"). Such an Aura/Equipment never phases in by itself — it phases in together
     *  with its host, so the untap-step phasing pass skips it when picking permanents to phase in. Phased-out
     *  permanents live in {@code GameData.phasedOutPermanents} rather than on a battlefield, and the flag
     *  deliberately survives {@link #resetModifiers()} (phased-out status persists across turns). */
    @Setter private boolean phasedOutIndirectly;
    /** The id of the exiled prepare-spell copy linked to this permanent while it is prepared (null otherwise). */
    @Setter private UUID preparedSpellCardId;
    /** If true, this creature has "Whenever this creature deals damage to an opponent, you may return target creature
     *  that player controls to its owner's hand" until end of turn (e.g. Arm with Aether). Cleared at end of turn. */
    @Setter private boolean hasDamageToOpponentCreatureBounce;
    /** Soul Echo: set when the targeted opponent chose that, until this permanent's controller's next
     *  upkeep, each 1 damage that would be dealt to that controller instead removes an echo counter
     *  from this permanent. Survives {@link #resetModifiers()} — the duration ends at the next upkeep,
     *  where {@code SoulEchoUpkeepEffectHandler} clears it before offering the choice again. */
    @Setter private boolean echoDamageRedirectionActive;
    /** Spatial Binding: the id of the player whose next upkeep ends this permanent's "can't phase out"
     *  restriction, or null while it may phase out normally. Phasing is a turn-based action of the untap
     *  step (CR 502.1), which precedes the upkeep, so the permanent is still protected during that
     *  player's own untap step; {@code StepTriggerService.handleUpkeepTriggers} clears the field when
     *  that player becomes the active player. Survives {@link #resetModifiers()} — the duration spans
     *  turns. */
    @Setter private UUID cantPhaseOutUntilUpkeepOf;
    /** Triggered effects temporarily granted by one-shot effects until end of turn
     *  (e.g. Verdant Rebirth granting ON_DEATH → ReturnSourceCardFromGraveyardToOwnerHandEffect).
     *  Keyed by EffectSlot so the trigger collection system can look up effects for the relevant slot.
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final Map<EffectSlot, List<CardEffect>> temporaryTriggeredEffects = new EnumMap<>(EffectSlot.class);
    /** Triggered effects granted for the rest of the current combat. Cleared by {@link #clearCombatState()}. */
    private final Map<EffectSlot, List<CardEffect>> combatTriggeredEffects = new EnumMap<>(EffectSlot.class);
    /** Triggered effects granted indefinitely by one-shot effects (e.g. Balduvian Shaman granting
     *  {@code UPKEEP_TRIGGERED} + {@code CumulativeUpkeepEffect}). Survives {@link #resetModifiers()}. */
    private final Map<EffectSlot, List<CardEffect>> persistentTriggeredEffects = new EnumMap<>(EffectSlot.class);
    /** Triggered effects granted until the next upkeep of the player whose effect created them.
     *  The expiry player is stored separately from the permanent's current controller because the
     *  duration belongs to the resolving ability's controller. */
    private final Map<EffectSlot, Map<UUID, List<CardEffect>>> untilNextUpkeepTriggeredEffects =
            new EnumMap<>(EffectSlot.class);
    @Setter private boolean basePowerToughnessOverriddenUntilEndOfTurn;
    @Setter private int basePowerOverride;
    @Setter private int baseToughnessOverride;
    private boolean faceDown;
    private boolean cloaked;
    private int faceDownPower;
    private int faceDownToughness;
    private final Set<CardType> faceDownCardTypes = EnumSet.noneOf(CardType.class);
    /** Whether this face-down permanent was put onto the battlefield by manifest. */
    @Setter private boolean manifested;
    @Setter private boolean animatedUntilEndOfTurn;
    /** When {@code true}, this permanent is animated as a creature until the combat phase ends
     *  (e.g. Jade Statue). Uses the same {@link #animatedPower}/{@link #animatedToughness}/
     *  {@link #animatedColor}/{@link #transientSubtypes} storage as {@link #animatedUntilEndOfTurn}.
     *  Cleared by {@link #clearCombatState()} when combat ends. */
    @Setter private boolean animatedUntilEndOfCombat;
    @Setter private int animatedPower;
    @Setter private int animatedToughness;
    @Setter private CardColor animatedColor;
    @Setter private boolean permanentlyAnimated;
    @Setter private int permanentAnimatedPower;
    @Setter private int permanentAnimatedToughness;
    /** All counters on this permanent, keyed by {@link CounterType}. Absent keys mean zero counters.
     *  New counter kinds require only a new {@link CounterType} value — never a new field here.
     *  Read/write via {@link #getCounterCount(CounterType)} / {@link #setCounterCount(CounterType, int)}. */
    private final Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
    private int loyaltyCountersRemovedSinceTriggerCheck;
    /** Latest placement timestamp for each counter kind. Keyword counters use this to participate
     *  in layer ordering; removing counters does not remove their timestamp. */
    private final Map<CounterType, Long> counterTimestamps = new EnumMap<>(CounterType.class);
    /** Counters this permanent must shed at the beginning of the next cleanup step, keyed by type.
     *  Populated by the "for each counter you put on a creature this way, remove a counter from that
     *  creature at the beginning of the next cleanup step" rider (Bounty of the Hunt) and swept by
     *  {@code TurnCleanupService}. Absent keys mean nothing pending. */
    private final Map<CounterType, Integer> countersToRemoveAtNextCleanup = new EnumMap<>(CounterType.class);
    @Setter private int loyaltyActivationsThisTurn;
    /** Extra loyalty activations granted to this planeswalker this turn on top of the normal
     *  allowance ("you may activate one of its loyalty abilities once this turn as though none of its
     *  loyalty abilities have been activated this turn" — The Chain Veil). Reset in the untap step
     *  alongside {@link #loyaltyActivationsThisTurn}. */
    @Setter private int extraLoyaltyActivationsThisTurn;
    /**
     * Siege battles: the opponent chosen to protect this battle as it entered. Only that player
     * may block attackers attacking this battle; everyone else (including the controller) may attack it.
     */
    @Setter private UUID protectorPlayerId;
    private final Set<Keyword> grantedKeywords = new HashSet<>();
    /** Keywords granted permanently by a one-shot effect that locks in a characteristic for the
     *  life of the permanent (e.g. Primal Clay "becomes ... with flying/defender"). Unlike
     *  {@link #grantedKeywords} these survive {@link #resetModifiers()} — the layered pass seeds
     *  them just like {@link #grantedSubtypes}/{@link #grantedColors} do for durable type/color grants. */
    private final Set<Keyword> persistentGrantedKeywords = new HashSet<>();
    /** Keywords temporarily removed by one-shot effects (e.g. activated abilities).
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final Set<Keyword> removedKeywords = new HashSet<>();
    /** Transient colors granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent color grants from one-shot effects, see {@link #grantedColors}. */
    private final Set<CardColor> transientColors = EnumSet.noneOf(CardColor.class);
    /** When {@code true}, {@link #transientColors} completely replaces the permanent's natural color
     *  (e.g. "target creature becomes blue"). When {@code false}, transient colors are additive. */
    @Setter private boolean colorOverridden;
    /** Transient subtypes granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent subtype grants from one-shot effects, see {@link #grantedSubtypes}. */
    private final List<CardSubtype> transientSubtypes = new ArrayList<>();
    /** When non-null, this land "becomes the basic land type of your choice" until end of turn,
     *  replacing its other land types and mana ability (e.g. Tideshaper Mystic). Distinct from
     *  {@link #transientSubtypes}, which is additive. Cleared every turn by {@link #resetModifiers()}. */
    @Setter private CardSubtype transientLandTypeOverride;
    /** When non-null, this land "becomes" the given basic land type until its controller's next
     *  untap step (e.g. Orcish Farmer), replacing its other land types and mana ability per rule
     *  305.7. Like {@link #transientLandTypeOverride} but longer-lived: NOT cleared by
     *  {@link #resetModifiers()} — it survives end-of-turn cleanup and is cleared at the beginning
     *  of the controller's next turn by {@link #clearUntilNextTurnEffects()}. */
    @Setter private CardSubtype untilNextTurnLandTypeOverride;
    /** When non-null, this land permanently becomes the given basic land type, replacing its
     *  other land types and mana ability. */
    @Setter private CardSubtype persistentLandTypeOverride;
    /** When non-null, this creature "becomes a [creature type]" until end of turn, replacing all its
     *  other creature types (e.g. Boldwyr Intimidator: "target creature becomes a Coward"). Read by the
     *  layered pass, which strips every creature subtype and adds this one. Distinct from
     *  {@link #transientSubtypes} (additive) and {@link #transientLandTypeOverride} (land types).
     *  Cleared every turn by {@link #resetModifiers()}. */
    @Setter private CardSubtype transientCreatureTypeOverride;
    /** When non-empty, this creature becomes these creature types until end of turn, replacing all
     *  its other creature types. The singular field above remains for single-type effects and
     *  backwards-compatible callers. Cleared every turn by {@link #resetModifiers()}. */
    private final List<CardSubtype> transientCreatureTypeOverrides = new ArrayList<>();
    private final Set<CardType> grantedCardTypes = EnumSet.noneOf(CardType.class);
    /** Card types permanently granted by one-shot effects (e.g. Phyrexian Scriptures "becomes an artifact").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient card type grants from static/activated effects, see {@link #grantedCardTypes}. */
    private final Set<CardType> persistentGrantedCardTypes = EnumSet.noneOf(CardType.class);
    /** Supertypes granted to this permanent for as long as it stays on the battlefield by a one-shot
     *  effect ({@code SetTargetPermanentSupertypeEffect}, e.g. Arcum's Weathervane "target nonsnow
     *  basic land becomes snow"). Read via {@code GameQueryService.hasEffectiveSupertype}, never by
     *  inspecting the printed supertypes. NOT cleared by {@link #resetModifiers()}. */
    private final Set<CardSupertype> persistentGrantedSupertypes = EnumSet.noneOf(CardSupertype.class);
    /** Supertypes removed from this permanent for as long as it stays on the battlefield by a one-shot
     *  effect (Arcum's Weathervane "target snow land is no longer snow"). Mutually exclusive with
     *  {@link #persistentGrantedSupertypes} — the later activation wins. NOT cleared by
     *  {@link #resetModifiers()}. */
    private final Set<CardSupertype> persistentRemovedSupertypes = EnumSet.noneOf(CardSupertype.class);
    /** Name assigned by a one-shot effect for as long as this permanent stays on the battlefield. */
    @Setter private String persistentName;
    /** Word substitutions applied by text-changing effects (CR 612). Entries flagged
     *  {@link TextReplacement#untilEndOfTurn()} (Whim of Volrath) are dropped by
     *  {@link #resetModifiers()}; the rest survive turn resets (Mind Bend, Magical Hack). */
    private final List<TextReplacement> textReplacements = new ArrayList<>();
    private final Set<CardType> protectionFromCardTypes = EnumSet.noneOf(CardType.class);
    private final Set<CardColor> protectionFromColorsUntilEndOfTurn = EnumSet.noneOf(CardColor.class);
    /** Whether this permanent has protection from colorless until end of turn. */
    @Setter private boolean protectionFromColorlessUntilEndOfTurn;
    /** Whether this permanent has durable protection from each opponent of its controller. */
    @Setter private boolean protectionFromOpponentsPermanently;
    /** Players from whom this permanent has durable protection, captured when the effect resolved. */
    private final Set<UUID> protectionFromPlayerIdsPermanently = new HashSet<>();
    /** Subtypes for "protection from non-[subtype] creatures" granted until end of turn.
     *  If this set contains HUMAN, the permanent has "protection from non-Human creatures."
     *  Cleared by {@link #resetModifiers()}. */
    private final Set<CardSubtype> protectionFromNonSubtypeCreaturesUntilEndOfTurn = EnumSet.noneOf(CardSubtype.class);
    /** Whether this permanent has protection from creatures controlled by its opponents until end of turn. */
    @Setter private boolean protectionFromOpponentCreaturesUntilEndOfTurn;
    /** Blocking restrictions granted until end of turn by one-shot effects (e.g. Dread Charge:
     *  "black creatures you control can't be blocked this turn except by black creatures").
     *  Each entry means this creature can be blocked only by blockers matching the restriction's
     *  filter. Consumed by {@code GameQueryService.getBlockRestriction}; cleared by {@link #resetModifiers()}. */
    private final List<CanBeBlockedOnlyByFilterEffect> blockRestrictionsUntilEndOfTurn = new ArrayList<>();
    /** Defender-condition evasion granted until end of turn by one-shot effects (e.g. Barbarian
     *  Guides granting snow landwalk of a chosen type). This creature can't be blocked as long as
     *  the defending player controls a permanent matching any of these predicates — the transient
     *  counterpart of a printed
     *  {@link com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect}.
     *  Consumed by {@code BlockLegalityService}/{@code CombatHelper}; cleared by {@link #resetModifiers()}. */
    private final List<PermanentPredicate> unblockableIfDefenderControlsUntilEndOfTurn = new ArrayList<>();
    /** Players who may target this permanent this turn as though it didn't have shroud (Autumn
     *  Willow). Read by the shroud checks in {@code TargetLegalityService}/{@code ValidTargetService};
     *  cleared by {@link #resetModifiers()}. */
    private final Set<UUID> shroudIgnoredByPlayersUntilEndOfTurn = new HashSet<>();
    private final Set<UUID> cantBlockIds = new HashSet<>();
    private final Set<UUID> mustBlockIds = new HashSet<>();
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield (CR 614.6). */
    @Setter private boolean exileIfLeavesBattlefield;
    /** If true, this permanent is exiled instead of being put into a graveyard from the battlefield. */
    @Setter private boolean exileIfDying;
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield this turn. */
    @Setter private boolean exileIfLeavesBattlefieldUntilEndOfTurn;
    @Setter private boolean enteredViaUnearth;
    /** When this permanent entered the battlefield from a graveyard, the ID of the player whose graveyard
     *  it came from; {@code null} otherwise. Read during the entering creature's ETB processing to fire
     *  "whenever a creature enters from your graveyard" triggers (e.g. Flayer of the Hatebound). */
    @Setter private UUID enteredFromGraveyardOwnerId;
    /** True when this permanent entered the battlefield from exile via a delayed return (flicker,
     *  Obzedat, Ghost Council). Like a token copy or a reanimated creature it never went through
     *  cast-time target selection, so a mandatory targeted ETB must choose its target as the
     *  triggered ability is put on the stack (CR 603.3b). */
    @Setter private boolean enteredFromExile;
    /** Source permanent IDs that prevent this permanent from untapping during its controller's untap step.
     *  Each entry means: "this permanent doesn't untap for as long as that source permanent remains tapped." */
    private final Set<UUID> untapPreventedByPermanentIds = new HashSet<>();
    /** Source permanent IDs that prevent this permanent from untapping during its controller's untap step.
     *  Each entry means: "this permanent doesn't untap for as long as that source permanent is on the battlefield."
     *  Unlike {@link #untapPreventedByPermanentIds}, the tapped state of the source does not matter. */
    private final Set<UUID> untapPreventedWhileSourceOnBattlefieldIds = new HashSet<>();
    /** Basic land types applied to lands by this permanent until it leaves the battlefield. The
     *  layered static effect reads this map, so the grants survive turn resets but end when this
     *  permanent's static effects stop being collected. */
    private final Map<UUID, CardSubtype> landTypesUntilSourceLeaves = new HashMap<>();
    /** Number of untap steps this permanent should skip. Decremented each untap step.
     *  Multiple triggers (e.g. land tapped twice while Vorinclex is out) stack independently.
     *  Used by Vorinclex, Voice of Hunger's opponent-land lock. */
    @Setter private int skipUntapCount;
    /** Accumulated damage marked on this creature (CR 704.5g). Reset during cleanup step. */
    private int markedDamage;
    /**
     * Damage marked on this creature broken down by damage-source object id (permanent id, or the
     * dealing spell/ability card id when there is no permanent source). Used by
     * {@link com.github.laxika.magicalvibes.model.effect.CantBeDestroyedByLethalDamageUnlessSingleSourceEffect}
     * so lethal damage from multiple sources cannot be combined. Cleared with {@link #markedDamage}.
     */
    private final Map<UUID, Integer> markedDamageBySource = new HashMap<>();
    /** Dealt damage by a source with deathtouch since the last state-based action check (CR 704.5h).
     *  Consumed by each SBA check; also cleared by regeneration and during the cleanup step. */
    @Setter private boolean damagedByDeathtouch;
    /** Colors permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other colors").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient color grants from static effects, see {@link #transientColors}. */
    private final Set<CardColor> grantedColors = EnumSet.noneOf(CardColor.class);
    /** Subtypes permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other types").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient subtype grants from static effects, see {@link #transientSubtypes}. */
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    /** When true, the creature's base power has been permanently overridden (e.g. by an exchange effect
     *  like Evra, Halcyon Witness). NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  Participates in CR 613.7 layer-7b ordering via {@link #permanentBasePowerOverrideTimestamp}. */
    @Setter private boolean basePowerOverriddenPermanently;
    @Setter private int permanentBasePowerOverride;
    /** CR 613.7 timestamp of the exchange that set {@link #basePowerOverriddenPermanently} — the
     *  layered 7b pass orders the override against other base-P/T setters with it. */
    @Setter private long permanentBasePowerOverrideTimestamp;
    /** When true, the creature's base toughness has been permanently overridden (e.g. by an exchange effect
     *  like Tree of Redemption). NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  Participates in CR 613.7 layer-7b ordering via {@link #permanentBaseToughnessOverrideTimestamp}. */
    @Setter private boolean baseToughnessOverriddenPermanently;
    @Setter private int permanentBaseToughnessOverride;
    /** CR 613.7 timestamp of the exchange that set {@link #baseToughnessOverriddenPermanently}. */
    @Setter private long permanentBaseToughnessOverrideTimestamp;
    @Setter private boolean transformed;
    /** When true, this permanent has lost all abilities until end of turn (e.g. Merfolk Trickster).
     *  Keywords, activated abilities, and triggered abilities are suppressed.
     *  Cleared by {@link #resetModifiers()}. */
    @Setter private boolean losesAllAbilitiesUntilEndOfTurn;
    /** Controllers whose next turn ends an active ability-loss effect on this permanent. */
    private final Set<UUID> losesAllAbilitiesUntilNextTurnControllers = new HashSet<>();
    /** Concrete printed static effect types suppressed on this permanent until end of turn. */
    private final Set<Class<? extends CardEffect>> suppressedStaticEffectsUntilEndOfTurn = new HashSet<>();
    /** When true, this permanent has lost all abilities indefinitely (e.g. Retched Wretch). */
    @Setter private boolean losesAllAbilitiesPermanently;
    /** When true, this permanent has lost all creature types until end of turn (e.g. Amoeboid Changeling).
     *  All creature subtypes (base, transient, granted) are treated as absent, and the Changeling keyword
     *  no longer grants any creature types. Cleared by {@link #resetModifiers()}. */
    @Setter private boolean losesAllCreatureTypesUntilEndOfTurn;
    /** Subtypes this permanent has lost until end of turn (e.g. Haunted Plate Mail "no longer an
     *  Equipment"). Honoured by every subtype query and the layered pass; cleared by
     *  {@link #resetModifiers()}. */
    private final Set<CardSubtype> transientRemovedSubtypes = EnumSet.noneOf(CardSubtype.class);
    /** Whether this permanent was kicked when cast (tracked for "if wasn't kicked" triggers). */
    @Setter private boolean kicked;
    /** Whether this permanent was cast for its evoke cost (gates the evoke sacrifice ETB trigger). */
    @Setter private boolean evoked;
    /** Whether this permanent was cast using an escape permission. */
    @Setter private boolean escaped;
    /** Whether this permanent was cast for its prowl cost (gates "if its prowl cost was paid" ETB triggers). */
    @Setter private boolean prowl;
    /** Whether this permanent was cast by paying an alternate cost. */
    @Setter private boolean alternateCost;
    /** Mana value of the creature returned to pay this permanent's web-slinging cost, when applicable. */
    @Setter private Integer webSlingingReturnedCreatureManaValue;
    /** Whether this permanent was cast for its spectacle cost. */
    @Setter private boolean spectacle;
    /** Whether this permanent's optional collect-evidence additional cost was paid. */
    @Setter private boolean collectEvidenceCostPaid;
    /** Repeatable additional mana payments made to cast this permanent's spell. */
    private List<String> repeatedAdditionalCosts = List.of();
    /** Whether the required tribute counters were placed on this permanent as it entered. */
    @Setter private boolean tributePaid;
    /** Whether this permanent is renowned (CR 702.111): set the first time a renown trigger resolves,
     *  and read by "if it's renowned" abilities. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean renowned;
    /** Whether this permanent has become monstrous. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean monstrous;
    /** Whether this permanent's Case has been solved. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean solved;
    /** Whether this permanent is harnessed. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean harnessed;
    /** Whether this permanent is saddled until end of turn. */
    @Setter private boolean saddled;
    /** Zone the spell that produced this permanent was cast from, when known (gates "if cast from a
     *  graveyard, it enters with … counters" as-enters replacements — e.g. Worldheart Phoenix). */
    @Setter private Zone castFromZone;
    /** Zone this permanent entered the battlefield from, when known. */
    @Setter private Zone enteredFromZone;
    /** Whether this permanent entered the battlefield by resolving as a spell its controller cast
     *  (as opposed to being put onto the battlefield), gating "if you cast it" abilities.
     *  NOT cleared by {@link #resetModifiers()}. */
    @Setter private boolean cast;
    /** Mana spent to cast the spell that produced this permanent, for as-enters conditions. */
    @Setter private int manaSpentToCast;
    /** Total bloodthirst granted to the spell that produced this permanent while it was on the stack
     *  (Bloodlord of Vaasgoth). Read as an as-enters replacement alongside the card's printed
     *  bloodthirst; per CR 702.54c each instance applies separately, so grants simply add up. */
    @Setter private int grantedBloodthirst;
    /** Cards of the creatures sacrificed to this permanent's devour ability as it entered (CR 702.82).
     *  Read by {@code CreaturesDevoured} ("for each creature it devoured" — Tar Fiend) via its size and by
     *  {@code DevouredCreaturesOfSubtype} ("twice the number of Goblins it devoured" — Voracious Dragon). */
    private final List<Card> devouredCreatures = new ArrayList<>();
    /**
     * Physical cards that represent a melded permanent (CR 701.37). Empty when not melded.
     * When this permanent leaves the battlefield, these cards (not {@link #originalCard}) move
     * to the destination zone.
     */
    private final List<Card> meldComponentCards = new ArrayList<>();
    /** Activated abilities temporarily granted by one-shot effects until end of turn
     *  (e.g. Navigator's Compass adding a basic land mana ability to a land).
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final List<ActivatedAbility> temporaryActivatedAbilities = new ArrayList<>();
    /** One-shot permissions for this permanent's next Adapt to ignore existing counters this turn. */
    private int adaptOverridesUntilEndOfTurn;
    /** Activated abilities granted for as long as this permanent remains on the battlefield
     *  (e.g. Aquitect's Will making a land an Island in addition to its other types — the
     *  granted "{T}: Add {U}" has no duration). Stored on the permanent rather than mutating
     *  the {@link Card}: Card instances are shared with AI simulation copies (see the copy
     *  constructor) and must stay immutable after construction.
     *  NOT cleared by {@link #resetModifiers()}. */
    private final List<ActivatedAbility> persistentGrantedActivatedAbilities = new ArrayList<>();
    /** When true, this permanent is a temporary copy (until end of turn) of another creature.
     *  At the cleanup step, the copy's floating layer-1 effect expires and the card reverts to
     *  {@link #preCopyCard} via {@link #revertEndOfTurnCopy()}.
     *  Used by Tilonalli's Skinshifter and similar shapeshifters. */
    @Setter private boolean copyUntilEndOfTurn;
    /** The card to revert to when the temporary copy effect ends.
     *  Only non-null when {@link #copyUntilEndOfTurn} is true. */
    @Setter private Card preCopyCard;
    /** When true, this permanent is a copy of another creature for as long as the Aura that made it
     *  one stays attached to it (Metamorphic Alteration). NOT cleared by {@link #resetModifiers()}.
     *  Reverts to {@link #whileAttachedPreCopyCard} via {@link #revertWhileAttachedCopy()} when that
     *  Aura's {@code WHILE_ATTACHED} floating layer-1 effect expires. */
    @Setter private boolean copyWhileAttached;
    /** The card to revert to when a "while attached" copy ends.
     *  Only non-null when {@link #copyWhileAttached} is true. */
    @Setter private Card whileAttachedPreCopyCard;
    /** Activated abilities granted until the controller's next turn begins
     *  (e.g. Song of Freyalise "{T}: Add one mana of any color.").
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by
     *  {@link com.github.laxika.magicalvibes.model.Permanent#clearUntilNextTurnEffects()}. */
    private final List<ActivatedAbility> untilNextTurnActivatedAbilities = new ArrayList<>();
    /** When true, this land permanent is animated as a creature until the controller's next turn.
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by
     *  {@link #clearUntilNextTurnEffects()}. */
    @Setter private boolean animatedUntilNextTurn;
    @Setter private int untilNextTurnAnimatedPower;
    @Setter private int untilNextTurnAnimatedToughness;
    /** Subtypes granted by "until your next turn" animation effects (e.g. Elemental from Sylvan Awakening).
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by {@link #clearUntilNextTurnEffects()}. */
    private final List<CardSubtype> untilNextTurnSubtypes = new ArrayList<>();
    /** Keywords granted by "until your next turn" animation effects (e.g. reach, indestructible, haste from Sylvan Awakening).
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by {@link #clearUntilNextTurnEffects()}. */
    private final Set<Keyword> untilNextTurnKeywords = new HashSet<>();
    /** When true, this permanent is a copy of another creature until {@link #copyUntilNextTurnControllerId}'s
     *  next turn (e.g. Shapesharer). NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Reverts to {@link #untilNextTurnPreCopyCard} via {@link #revertUntilNextTurnCopy()} at the beginning
     *  of that player's turn. */
    @Setter private boolean copyUntilControllerNextTurn;
    /** The card to revert to when an "until your next turn" copy ends.
     *  Only non-null when {@link #copyUntilControllerNextTurn} is true. */
    @Setter private Card untilNextTurnPreCopyCard;
    /** The player whose next turn ends an "until your next turn" copy (the ability's controller). */
    @Setter private UUID copyUntilNextTurnControllerId;
    /** CR 613.7 timestamp: stamped from {@link GameData#nextTimestamp()} when this permanent
     *  enters a battlefield, and re-stamped each time it becomes attached (CR 613.7e — Auras and
     *  Equipment). Control changes do NOT re-stamp (CR 613.7c). Stays 0 for permanents added to
     *  a battlefield directly in tests; the layered engine falls back to battlefield position
     *  order for equal timestamps (see {@code agent-docs/LAYER_SYSTEM.md}). */
    @Setter private long timestamp;

    public Permanent(Card card) {
        this.id = UUID.randomUUID();
        // A card wrapped in a Permanent is live game state shared with AI simulation copies —
        // freeze it so any later mutation of the Card object fails fast instead of leaking.
        card.freeze();
        this.card = card;
        this.originalCard = card;
        this.bestow = false;
        this.tapped = false;
        this.untappedAtTurnStart = true;
        this.attackedThisTurn = false;
        this.attackedThisCombat = false;
        this.summoningSick = true;
    }

    public Permanent(Card card, Zone enteredFromZone) {
        this(card);
        this.enteredFromZone = enteredFromZone;
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     * Preserves the same ID so that target references remain valid.
     * Card references are shared (immutable after construction).
     */
    public Permanent(Permanent source) {
        this.id = source.id;
        this.card = source.card;
        this.originalCard = source.originalCard;
        this.bestow = source.bestow;
        this.fullTextCopySourceCard = source.fullTextCopySourceCard;
        this.tapped = source.tapped;
        this.untappedAtTurnStart = source.untappedAtTurnStart;
        this.untapSequence = source.untapSequence;
        this.controlChangeSequence = source.controlChangeSequence;
        this.attacking = source.attacking;
        this.attackTarget = source.attackTarget;
        this.attackedThisTurn = source.attackedThisTurn;
        this.attackedThisCombat = source.attackedThisCombat;
        this.creaturesThatCrewedThisTurn.addAll(source.creaturesThatCrewedThisTurn);
        this.attackedDuringControllersCurrentTurn = source.attackedDuringControllersCurrentTurn;
        this.attackedDuringControllersLastTurn = source.attackedDuringControllersLastTurn;
        this.cantAttackNextTurn = source.cantAttackNextTurn;
        this.cantAttackThisTurn = source.cantAttackThisTurn;
        this.blocking = source.blocking;
        this.blockedThisTurn = source.blockedThisTurn;
        this.blockedThisCombat = source.blockedThisCombat;
        this.blockedOrWasBlockedSinceLastUpkeep = source.blockedOrWasBlockedSinceLastUpkeep;
        this.attackedOrBlockedSinceLastUpkeep = source.attackedOrBlockedSinceLastUpkeep;
        this.becomeTargetCounterUsedThisTurn = source.becomeTargetCounterUsedThisTurn;
        this.backupAbilityCopyUsedThisTurn = source.backupAbilityCopyUsedThisTurn;
        this.blockingTargets.addAll(source.blockingTargets);
        this.blockingTargetIds.addAll(source.blockingTargetIds);
        this.bandId = source.bandId;
        this.summoningSick = source.summoningSick;
        this.powerModifier = source.powerModifier;
        this.toughnessModifier = source.toughnessModifier;
        this.damagePreventionShield = source.damagePreventionShield;
        this.damageToCounterPreventionShield = source.damageToCounterPreventionShield;
        this.damageToPlusOnePlusOneCounterPreventionShield = source.damageToPlusOnePlusOneCounterPreventionShield;
        this.allDamageToPlusOnePlusOneCounterPreventionShield = source.allDamageToPlusOnePlusOneCounterPreventionShield;
        this.damageDestructionShield = source.damageDestructionShield;
        this.regenerationShield = source.regenerationShield;
        this.opponentDrawRegenerationShield = source.opponentDrawRegenerationShield;
        this.minusOneCounterRegenerationShield = source.minusOneCounterRegenerationShield;
        this.plusOnePlusOneCounterRegenerationShield = source.plusOnePlusOneCounterRegenerationShield;
        this.gainControlRegenerationShields.addAll(source.gainControlRegenerationShields);
        this.timesRegeneratedThisTurn = source.timesRegeneratedThisTurn;
        this.attachedTo = source.attachedTo;
        this.lastAttachedTo = source.lastAttachedTo;
        this.pairedWithId = source.pairedWithId;
        this.chosenColor = source.chosenColor;
        this.chosenColors.addAll(source.chosenColors);
        this.chosenName = source.chosenName;
        this.secondChosenName = source.secondChosenName;
        this.chosenSubtype = source.chosenSubtype;
        this.chosenCardType = source.chosenCardType;
        this.secondChosenSubtype = source.secondChosenSubtype;
        this.chosenMode = source.chosenMode;
        this.chosenNumber = source.chosenNumber;
        this.chosenModeLabels.addAll(source.chosenModeLabels);
        this.chosenModeLabelsThisTurn.addAll(source.chosenModeLabelsThisTurn);
        this.chosenManaValueParity = source.chosenManaValueParity;
        this.chosenPermanentId = source.chosenPermanentId;
        this.rememberedTargetPlayerId = source.rememberedTargetPlayerId;
        this.lifeLostWhenEntered = source.lifeLostWhenEntered;
        this.lifeLostWhenEnteredControllerId = source.lifeLostWhenEnteredControllerId;
        this.tappedPermanentsForAbilityThisTurn.addAll(source.tappedPermanentsForAbilityThisTurn);
        this.chosenCard = source.chosenCard;
        this.lastChosenExiledCard = source.lastChosenExiledCard;
        this.chosenSacrificedPermanentSnapshot = source.chosenSacrificedPermanentSnapshot == null
                ? null : new Permanent(source.chosenSacrificedPermanentSnapshot);
        this.cantBeBlocked = source.cantBeBlocked;
        this.cantBlockThisTurn = source.cantBlockThisTurn;
        this.cantBlockThisCombat = source.cantBlockThisCombat;
        this.suspected = source.suspected;
        this.additionalBlocksUntilEndOfTurn = source.additionalBlocksUntilEndOfTurn;
        this.mustBlockThisTurnIfAble = source.mustBlockThisTurnIfAble;
        this.mustAttackThisCombat = source.mustAttackThisCombat;
        this.mustAttackThisTurn = source.mustAttackThisTurn;
        this.mustAttackTargetId = source.mustAttackTargetId;
        this.mustBeBlockedThisTurn = source.mustBeBlockedThisTurn;
        this.mustBeBlockedByAllThisTurn = source.mustBeBlockedByAllThisTurn;
        this.blockedWithoutBlockers = source.blockedWithoutBlockers;
        this.dampingEngineEffectIgnoredThisTurn = source.dampingEngineEffectIgnoredThisTurn;
        this.cantRegenerateThisTurn = source.cantRegenerateThisTurn;
        this.damageCantBePreventedOrRedirectedThisTurn = source.damageCantBePreventedOrRedirectedThisTurn;
        this.damagedCreaturesCantRegenerateThisTurn = source.damagedCreaturesCantRegenerateThisTurn;
        this.exileDamagedCreaturesInsteadOfDyingThisTurn = source.exileDamagedCreaturesInsteadOfDyingThisTurn;
        this.exileInsteadOfDieThisTurn = source.exileInsteadOfDieThisTurn;
        this.prepared = source.prepared;
        this.phasedOutIndirectly = source.phasedOutIndirectly;
        this.preparedSpellCardId = source.preparedSpellCardId;
        this.hasDamageToOpponentCreatureBounce = source.hasDamageToOpponentCreatureBounce;
        source.temporaryTriggeredEffects.forEach((slot, effects) ->
                this.temporaryTriggeredEffects.put(slot, new ArrayList<>(effects)));
        source.combatTriggeredEffects.forEach((slot, effects) ->
                this.combatTriggeredEffects.put(slot, new ArrayList<>(effects)));
        source.persistentTriggeredEffects.forEach((slot, effects) ->
                this.persistentTriggeredEffects.put(slot, new ArrayList<>(effects)));
        source.untilNextUpkeepTriggeredEffects.forEach((slot, effectsByPlayer) -> {
            Map<UUID, List<CardEffect>> copiedEffectsByPlayer = new HashMap<>();
            effectsByPlayer.forEach((playerId, effects) ->
                    copiedEffectsByPlayer.put(playerId, new ArrayList<>(effects)));
            this.untilNextUpkeepTriggeredEffects.put(slot, copiedEffectsByPlayer);
        });
        this.sacrificeAtNextCleanup = source.sacrificeAtNextCleanup;
        this.returnToHandAtNextCleanup = source.returnToHandAtNextCleanup;
        this.returnToHandAtNextUntap = source.returnToHandAtNextUntap;
        this.basePowerToughnessOverriddenUntilEndOfTurn = source.basePowerToughnessOverriddenUntilEndOfTurn;
        this.basePowerOverride = source.basePowerOverride;
        this.baseToughnessOverride = source.baseToughnessOverride;
        this.faceDown = source.faceDown;
        this.cloaked = source.cloaked;
        this.faceDownPower = source.faceDownPower;
        this.faceDownToughness = source.faceDownToughness;
        this.faceDownCardTypes.addAll(source.faceDownCardTypes);
        this.manifested = source.manifested;
        this.animatedUntilEndOfTurn = source.animatedUntilEndOfTurn;
        this.animatedUntilEndOfCombat = source.animatedUntilEndOfCombat;
        this.animatedPower = source.animatedPower;
        this.animatedToughness = source.animatedToughness;
        this.animatedColor = source.animatedColor;
        this.permanentlyAnimated = source.permanentlyAnimated;
        this.permanentAnimatedPower = source.permanentAnimatedPower;
        this.permanentAnimatedToughness = source.permanentAnimatedToughness;
        this.counters.putAll(source.counters);
        this.loyaltyCountersRemovedSinceTriggerCheck = source.loyaltyCountersRemovedSinceTriggerCheck;
        this.counterTimestamps.putAll(source.counterTimestamps);
        this.countersToRemoveAtNextCleanup.putAll(source.countersToRemoveAtNextCleanup);
        this.loyaltyActivationsThisTurn = source.loyaltyActivationsThisTurn;
        this.extraLoyaltyActivationsThisTurn = source.extraLoyaltyActivationsThisTurn;
        this.protectorPlayerId = source.protectorPlayerId;
        this.enteredFromGraveyardOwnerId = source.enteredFromGraveyardOwnerId;
        this.enteredFromExile = source.enteredFromExile;
        this.grantedKeywords.addAll(source.grantedKeywords);
        this.persistentGrantedKeywords.addAll(source.persistentGrantedKeywords);
        this.removedKeywords.addAll(source.removedKeywords);
        this.transientColors.addAll(source.transientColors);
        this.colorOverridden = source.colorOverridden;
        this.transientSubtypes.addAll(source.transientSubtypes);
        this.transientCreatureTypeOverride = source.transientCreatureTypeOverride;
        this.transientCreatureTypeOverrides.addAll(source.transientCreatureTypeOverrides);
        this.grantedCardTypes.addAll(source.grantedCardTypes);
        this.persistentGrantedCardTypes.addAll(source.persistentGrantedCardTypes);
        this.persistentGrantedSupertypes.addAll(source.persistentGrantedSupertypes);
        this.persistentRemovedSupertypes.addAll(source.persistentRemovedSupertypes);
        this.persistentName = source.persistentName;
        this.textReplacements.addAll(source.textReplacements);
        this.protectionFromCardTypes.addAll(source.protectionFromCardTypes);
        this.protectionFromColorsUntilEndOfTurn.addAll(source.protectionFromColorsUntilEndOfTurn);
        this.protectionFromColorlessUntilEndOfTurn = source.protectionFromColorlessUntilEndOfTurn;
        this.protectionFromOpponentsPermanently = source.protectionFromOpponentsPermanently;
        this.protectionFromPlayerIdsPermanently.addAll(source.protectionFromPlayerIdsPermanently);
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.addAll(source.protectionFromNonSubtypeCreaturesUntilEndOfTurn);
        this.protectionFromOpponentCreaturesUntilEndOfTurn = source.protectionFromOpponentCreaturesUntilEndOfTurn;
        this.blockRestrictionsUntilEndOfTurn.addAll(source.blockRestrictionsUntilEndOfTurn);
        this.unblockableIfDefenderControlsUntilEndOfTurn.addAll(source.unblockableIfDefenderControlsUntilEndOfTurn);
        this.exileIfLeavesBattlefield = source.exileIfLeavesBattlefield;
        this.exileIfDying = source.exileIfDying;
        this.exileIfLeavesBattlefieldUntilEndOfTurn = source.exileIfLeavesBattlefieldUntilEndOfTurn;
        this.enteredViaUnearth = source.enteredViaUnearth;
        this.shroudIgnoredByPlayersUntilEndOfTurn.addAll(source.shroudIgnoredByPlayersUntilEndOfTurn);
        this.cantBlockIds.addAll(source.cantBlockIds);
        this.mustBlockIds.addAll(source.mustBlockIds);
        this.untapPreventedByPermanentIds.addAll(source.untapPreventedByPermanentIds);
        this.untapPreventedWhileSourceOnBattlefieldIds.addAll(source.untapPreventedWhileSourceOnBattlefieldIds);
        this.landTypesUntilSourceLeaves.putAll(source.landTypesUntilSourceLeaves);
        this.skipUntapCount = source.skipUntapCount;
        this.markedDamage = source.markedDamage;
        this.markedDamageBySource.putAll(source.markedDamageBySource);
        this.damagedByDeathtouch = source.damagedByDeathtouch;
        this.grantedColors.addAll(source.grantedColors);
        this.grantedSubtypes.addAll(source.grantedSubtypes);
        this.basePowerOverriddenPermanently = source.basePowerOverriddenPermanently;
        this.permanentBasePowerOverride = source.permanentBasePowerOverride;
        this.permanentBasePowerOverrideTimestamp = source.permanentBasePowerOverrideTimestamp;
        this.baseToughnessOverriddenPermanently = source.baseToughnessOverriddenPermanently;
        this.permanentBaseToughnessOverride = source.permanentBaseToughnessOverride;
        this.permanentBaseToughnessOverrideTimestamp = source.permanentBaseToughnessOverrideTimestamp;
        this.transformed = source.transformed;
        this.losesAllAbilitiesUntilEndOfTurn = source.losesAllAbilitiesUntilEndOfTurn;
        this.losesAllAbilitiesUntilNextTurnControllers.addAll(
                source.losesAllAbilitiesUntilNextTurnControllers);
        this.suppressedStaticEffectsUntilEndOfTurn.addAll(source.suppressedStaticEffectsUntilEndOfTurn);
        this.losesAllAbilitiesPermanently = source.losesAllAbilitiesPermanently;
        this.losesAllCreatureTypesUntilEndOfTurn = source.losesAllCreatureTypesUntilEndOfTurn;
        this.transientRemovedSubtypes.addAll(source.transientRemovedSubtypes);
        this.kicked = source.kicked;
        this.evoked = source.evoked;
        this.escaped = source.escaped;
        this.prowl = source.prowl;
        this.alternateCost = source.alternateCost;
        this.webSlingingReturnedCreatureManaValue = source.webSlingingReturnedCreatureManaValue;
        this.spectacle = source.spectacle;
        this.collectEvidenceCostPaid = source.collectEvidenceCostPaid;
        this.repeatedAdditionalCosts = source.repeatedAdditionalCosts;
        this.tributePaid = source.tributePaid;
        this.castFromZone = source.castFromZone;
        this.enteredFromZone = source.enteredFromZone;
        this.cast = source.cast;
        this.manaSpentToCast = source.manaSpentToCast;
        this.monstrous = source.monstrous;
        this.solved = source.solved;
        this.harnessed = source.harnessed;
        this.saddled = source.saddled;
        this.grantedBloodthirst = source.grantedBloodthirst;
        this.devouredCreatures.addAll(source.devouredCreatures);
        this.meldComponentCards.addAll(source.meldComponentCards);
        this.temporaryActivatedAbilities.addAll(source.temporaryActivatedAbilities);
        this.adaptOverridesUntilEndOfTurn = source.adaptOverridesUntilEndOfTurn;
        this.persistentGrantedActivatedAbilities.addAll(source.persistentGrantedActivatedAbilities);
        this.copyUntilEndOfTurn = source.copyUntilEndOfTurn;
        this.preCopyCard = source.preCopyCard;
        this.copyWhileAttached = source.copyWhileAttached;
        this.whileAttachedPreCopyCard = source.whileAttachedPreCopyCard;
        this.untilNextTurnActivatedAbilities.addAll(source.untilNextTurnActivatedAbilities);
        this.animatedUntilNextTurn = source.animatedUntilNextTurn;
        this.untilNextTurnAnimatedPower = source.untilNextTurnAnimatedPower;
        this.untilNextTurnAnimatedToughness = source.untilNextTurnAnimatedToughness;
        this.untilNextTurnSubtypes.addAll(source.untilNextTurnSubtypes);
        this.untilNextTurnKeywords.addAll(source.untilNextTurnKeywords);
        this.untilNextTurnLandTypeOverride = source.untilNextTurnLandTypeOverride;
        this.persistentLandTypeOverride = source.persistentLandTypeOverride;
        this.copyUntilControllerNextTurn = source.copyUntilControllerNextTurn;
        this.untilNextTurnPreCopyCard = source.untilNextTurnPreCopyCard;
        this.copyUntilNextTurnControllerId = source.copyUntilNextTurnControllerId;
        this.timestamp = source.timestamp;
    }

    public Card getOriginalCard() {
        return originalCard;
    }

    public void setFaceDown(int power, int toughness, Set<CardType> cardTypes) {
        this.faceDown = true;
        this.cloaked = false;
        this.faceDownPower = power;
        this.faceDownToughness = toughness;
        this.faceDownCardTypes.clear();
        this.faceDownCardTypes.addAll(cardTypes);
    }

    public void setFaceDownAsCloaked() {
        setFaceDown(2, 2, Set.of(CardType.CREATURE));
        this.cloaked = true;
    }

    public void turnFaceUp() {
        this.faceDown = false;
        this.cloaked = false;
        this.faceDownPower = 0;
        this.faceDownToughness = 0;
        this.faceDownCardTypes.clear();
        this.manifested = false;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    /**
     * Sets total marked damage. Setting to 0 clears per-source tracking (cleanup / regeneration).
     * Non-zero assignments used by tests leave {@link #markedDamageBySource} empty — fine for
     * creatures without single-source lethal restrictions.
     */
    public void setMarkedDamage(int markedDamage) {
        this.markedDamage = markedDamage;
        if (markedDamage == 0) {
            this.markedDamageBySource.clear();
        }
    }

    /**
     * Records damage dealt by a specific source object and updates the total. {@code sourceId} may
     * be null when the source is unknown (total still increases; per-source map is unchanged).
     */
    public void addMarkedDamage(UUID sourceId, int amount) {
        if (amount <= 0) {
            return;
        }
        this.markedDamage += amount;
        if (sourceId != null) {
            this.markedDamageBySource.merge(sourceId, amount, Integer::sum);
        }
    }

    /** True when at least one source has marked damage greater than or equal to {@code toughness}. */
    public boolean hasLethalDamageFromSingleSource(int toughness) {
        if (toughness <= 0) {
            return false;
        }
        for (int amount : markedDamageBySource.values()) {
            if (amount >= toughness) {
                return true;
            }
        }
        return false;
    }

    public void tap() {
        this.tapped = true;
    }

    /** Sets the permanent's status for an entry replacement without applying untap effects. */
    public void enterUntapped() {
        this.tapped = false;
    }

    public void untap() {
        // Stun counters (CR 122.1c / 701.x): if a tapped permanent would become untapped,
        // remove a stun counter from it instead. This is the single funnel point for all
        // untap sources (untap step, Seedborn Muse, "untap target", etc.).
        if (this.tapped && getCounterCount(CounterType.STUN) > 0) {
            setCounterCount(CounterType.STUN, getCounterCount(CounterType.STUN) - 1);
            return;
        }
        if (this.tapped) {
            this.untapSequence++;
        }
        this.tapped = false;
    }

    public void recordControlChange() {
        this.controlChangeSequence++;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        if (attacking) {
            this.attackedThisTurn = true;
            this.attackedThisCombat = true;
            this.attackedDuringControllersCurrentTurn = true;
        }
    }

    public void recordCreatureThatCrewedThisTurn(UUID creatureId) {
        if (creatureId != null) {
            creaturesThatCrewedThisTurn.add(creatureId);
        }
    }

    /**
     * Shifts the "attacked on my controller's turn" record one controller-turn back. Called at the
     * start of a turn for the permanents the new active player controls, so that during that turn
     * {@link #attackedDuringControllersLastTurn} means "attacked during their previous turn".
     */
    public void rollOverAttackRecord() {
        this.attackedDuringControllersLastTurn = this.attackedDuringControllersCurrentTurn;
        this.attackedDuringControllersCurrentTurn = false;
    }

    /** Remembers the host in {@link #lastAttachedTo} so it survives the detach. */
    public void setAttachedTo(UUID attachedTo) {
        if (attachedTo != null) {
            this.lastAttachedTo = attachedTo;
        }
        this.attachedTo = attachedTo;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
        if (blocking) {
            this.blockedThisTurn = true;
            this.blockedThisCombat = true;
        }
    }

    public void addBlockingTarget(int blockingTarget) {
        this.blockingTargets.add(blockingTarget);
    }

    public void addBlockingTargetId(UUID permanentId) {
        this.blockingTargetIds.add(permanentId);
    }

    public void setSummoningSick(boolean summoningSick) {
        this.summoningSick = summoningSick;
    }

    public boolean isAttached() {
        return attachedTo != null;
    }

    public void clearCombatState() {
        this.attacking = false;
        this.attackTarget = null;
        this.blocking = false;
        this.blockingTargets.clear();
        this.blockingTargetIds.clear();
        this.combatTriggeredEffects.clear();
        this.bandId = null;
        this.cantBlockThisCombat = false;
        this.mustAttackThisCombat = false;
        clearUntilEndOfCombatAnimation();
    }

    /**
     * Reverts an "until end of combat" animation (e.g. Jade Statue) when the combat phase ends.
     * Clears the shared animation storage that {@link #animatedUntilEndOfCombat} uses. No-op when
     * the permanent is not animated until end of combat, so unrelated transient grants are preserved.
     */
    public void clearUntilEndOfCombatAnimation() {
        if (!animatedUntilEndOfCombat) {
            return;
        }
        this.animatedUntilEndOfCombat = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.animatedColor = null;
        this.grantedKeywords.clear();
        this.transientSubtypes.clear();
        this.grantedCardTypes.clear();
    }

    /**
     * Lets the given player target this permanent for the rest of the turn as though it didn't have
     * shroud (Autumn Willow). Other players are unaffected.
     */
    public void allowShroudIgnoredBy(UUID playerId) {
        if (playerId != null) {
            this.shroudIgnoredByPlayersUntilEndOfTurn.add(playerId);
        }
    }

    /** Whether the given player may currently target this permanent as though it had no shroud. */
    public boolean ignoresShroudFor(UUID playerId) {
        return playerId != null && shroudIgnoredByPlayersUntilEndOfTurn.contains(playerId);
    }

    public void setAttackedThisTurn(boolean attackedThisTurn) {
        this.attackedThisTurn = attackedThisTurn;
    }

    public void setAttackedThisCombat(boolean attackedThisCombat) {
        this.attackedThisCombat = attackedThisCombat;
    }

    /** Records a creature devoured by this permanent's devour ability as it entered (CR 702.82). */
    public void recordDevouredCreature(Card devoured) {
        devouredCreatures.add(devoured);
    }

    /**
     * Cards that move when this permanent leaves the battlefield. Melded permanents are
     * represented by their component cards (CR 701.37); otherwise the original card.
     */
    public List<Card> cardsLeavingBattlefield() {
        if (!meldComponentCards.isEmpty()) {
            return List.copyOf(meldComponentCards);
        }
        return List.of(originalCard);
    }

    public void setBlockedThisTurn(boolean blockedThisTurn) {
        this.blockedThisTurn = blockedThisTurn;
    }

    public void setBlockedThisCombat(boolean blockedThisCombat) {
        this.blockedThisCombat = blockedThisCombat;
    }

    /**
     * Returns the number of counters of the given concrete type on this permanent.
     * {@code ANY} and {@code SILVER} are category/wildcard types, not concrete counters
     * stored on a permanent, and are rejected.
     */
    public int getCounterCount(CounterType counterType) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        return counters.getOrDefault(counterType, 0);
    }

    public int getPlusOnePlusOneCounters() {
        return getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
    }

    /**
     * Sets the number of counters of the given concrete type on this permanent. A count of
     * zero (or less) removes the entry so the backing map only holds present counters.
     * See {@link #getCounterCount(CounterType)} for the supported types.
     */
    public void setCounterCount(CounterType counterType, int count) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        int previousCount = counters.getOrDefault(counterType, 0);
        int newCount = Math.max(0, count);
        if (counterType == CounterType.LOYALTY && newCount < previousCount) {
            loyaltyCountersRemovedSinceTriggerCheck += previousCount - newCount;
        }
        if (count <= 0) {
            counters.remove(counterType);
        } else {
            counters.put(counterType, count);
        }
    }

    public int drainLoyaltyCountersRemovedSinceTriggerCheck() {
        int removed = loyaltyCountersRemovedSinceTriggerCheck;
        loyaltyCountersRemovedSinceTriggerCheck = 0;
        return removed;
    }

    public long getCounterTimestamp(CounterType counterType) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        return counterTimestamps.getOrDefault(counterType, 0L);
    }

    public void setCounterTimestamp(CounterType counterType, long timestamp) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        counterTimestamps.put(counterType, timestamp);
    }

    /**
     * Returns only the modifier portion of power (counters + temporary modifiers),
     * without the base power. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getPowerModifiers() {
        return powerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)
                - getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO);
    }

    /**
     * Returns only the modifier portion of toughness (counters + temporary modifiers),
     * without the base toughness. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getToughnessModifiers() {
        return toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + 2 * getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)
                - getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO)
                - getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO);
    }

    /**
     * This permanent's power from its own stored state (base + modifiers + counters), WITHOUT
     * layer-7d switches: P/T switching lives in floating continuous effects applied by the
     * layered pass ({@code GameQueryService.getEffectivePower} swaps the finished values per
     * CR 613.4d). This accessor is the legacy pre-switch fallback for direct {@code Permanent}
     * readers (views' raw term, last-known-information reads, predicate leaves).
     */
    public int getEffectivePower() {
        return getBasePower() + powerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)
                - getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO);
    }

    /** The toughness counterpart of {@link #getEffectivePower()} — same pre-switch caveat. */
    public int getEffectiveToughness() {
        return getBaseToughness() + toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + 2 * getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)
                - getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO)
                - getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO);
    }

    /**
     * The base power of this permanent from its own stored state, WITHOUT modifiers or counters.
     * This is NOT the CR 613 layer-7b precedence decision — that lives in the layered pass
     * ({@code LayerSystemService}), which orders every base-P/T setter (static, one-shot,
     * animation, exchange) by timestamp and surfaces the winner through
     * {@code GameQueryService.getEffectivePower}. This accessor is the legacy fallback used by
     * direct {@code Permanent} readers (views, last-known-information reads, predicate leaves)
     * when no layered 7b entry applies; when only one of these fields is set, the two agree.
     */
    public int getBasePower() {
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            return basePowerOverride;
        }
        if (basePowerOverriddenPermanently) {
            return permanentBasePowerOverride;
        }
        if (animatedUntilEndOfTurn || animatedUntilEndOfCombat) {
            return animatedPower;
        }
        if (animatedUntilNextTurn) {
            return untilNextTurnAnimatedPower;
        }
        if (permanentlyAnimated) {
            return permanentAnimatedPower;
        }
        if (faceDown) {
            return faceDownPower;
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0 && !card.hasType(CardType.CREATURE)) {
            return 8;
        }
        return card.getPower() != null ? card.getPower() : 0;
    }

    /** The base toughness counterpart of {@link #getBasePower()} — same fallback caveats. */
    public int getBaseToughness() {
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            return baseToughnessOverride;
        }
        if (baseToughnessOverriddenPermanently) {
            return permanentBaseToughnessOverride;
        }
        if (animatedUntilEndOfTurn || animatedUntilEndOfCombat) {
            return animatedToughness;
        }
        if (animatedUntilNextTurn) {
            return untilNextTurnAnimatedToughness;
        }
        if (permanentlyAnimated) {
            return permanentAnimatedToughness;
        }
        if (faceDown) {
            return faceDownToughness;
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0 && !card.hasType(CardType.CREATURE)) {
            return 8;
        }
        return card.getToughness() != null ? card.getToughness() : 0;
    }

    public CardColor getEffectiveColor() {
        if (colorOverridden && !transientColors.isEmpty()) {
            return transientColors.iterator().next();
        }
        if ((animatedUntilEndOfTurn || animatedUntilEndOfCombat) && animatedColor != null) {
            return animatedColor;
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
            return CardColor.GREEN;
        }
        if (faceDown) {
            return null;
        }
        return card.getColor();
    }

    /**
     * The permanent's full intrinsic color set, respecting the same legacy overrides as
     * {@link #getEffectiveColor()} but preserving every color of a multicolored card (a
     * {@code {W/U}} hybrid is both white and blue). Prefer this over {@link #getEffectiveColor()}
     * for color-matters checks (lords, protection, damage prevention). Does not include
     * static-effect grants — {@link com.github.laxika.magicalvibes.service.battlefield.GameQueryService#getEffectiveColors}
     * layers those on top.
     */
    public List<CardColor> getEffectiveColors() {
        if (colorOverridden && !transientColors.isEmpty()) {
            return List.copyOf(transientColors);
        }
        if ((animatedUntilEndOfTurn || animatedUntilEndOfCombat) && animatedColor != null) {
            return List.of(animatedColor);
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
            return List.of(CardColor.GREEN);
        }
        if (faceDown) {
            return List.of();
        }
        List<CardColor> intrinsic = card.getColors();
        if (intrinsic != null && !intrinsic.isEmpty()) {
            return intrinsic;
        }
        CardColor single = card.getColor();
        return single != null ? List.of(single) : List.of();
    }

    public boolean hasKeyword(Keyword keyword) {
        if (losesAllAbilitiesUntilEndOfTurn || !losesAllAbilitiesUntilNextTurnControllers.isEmpty()
                || losesAllAbilitiesPermanently) return false;
        // Changeling grants all creature types; losing all creature types nullifies that grant.
        if (keyword == Keyword.CHANGELING && losesAllCreatureTypesUntilEndOfTurn) return false;
        if (removedKeywords.contains(keyword)) return false;
        CounterType keywordCounter = switch (keyword) {
            case FLYING -> CounterType.FLYING;
            case FIRST_STRIKE -> CounterType.FIRST_STRIKE;
            case DOUBLE_STRIKE -> CounterType.DOUBLE_STRIKE;
            case DEATHTOUCH -> CounterType.DEATHTOUCH;
            case DECAYED -> CounterType.DECAYED;
            case LIFELINK -> CounterType.LIFELINK;
            case REACH -> CounterType.REACH;
            case TRAMPLE -> CounterType.TRAMPLE;
            case HEXPROOF -> CounterType.HEXPROOF;
            case INDESTRUCTIBLE -> CounterType.INDESTRUCTIBLE;
            default -> null;
        };
        return (keyword == Keyword.MENACE && suspected)
                || (!faceDown && card.hasKeyword(keyword)) || grantedKeywords.contains(keyword)
                || persistentGrantedKeywords.contains(keyword)
                || untilNextTurnKeywords.contains(keyword)
                || (keywordCounter != null && getCounterCount(keywordCounter) > 0);
    }

    public void setRepeatedAdditionalCosts(List<String> repeatedAdditionalCosts) {
        this.repeatedAdditionalCosts = repeatedAdditionalCosts == null || repeatedAdditionalCosts.isEmpty()
                ? List.of() : List.copyOf(repeatedAdditionalCosts);
    }

    public void addTemporaryTriggeredEffect(EffectSlot slot, CardEffect effect) {
        temporaryTriggeredEffects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public List<CardEffect> getTemporaryTriggeredEffects(EffectSlot slot) {
        List<CardEffect> effects = temporaryTriggeredEffects.getOrDefault(slot, List.of());
        List<CardEffect> untilEndOfCombat = combatTriggeredEffects.getOrDefault(slot, List.of());
        List<CardEffect> untilNextUpkeep = getUntilNextUpkeepTriggeredEffects(slot);
        if (untilEndOfCombat.isEmpty() && untilNextUpkeep.isEmpty()) {
            return effects;
        }
        List<CardEffect> combined = new ArrayList<>(effects.size()
                + untilEndOfCombat.size() + untilNextUpkeep.size());
        combined.addAll(effects);
        combined.addAll(untilEndOfCombat);
        combined.addAll(untilNextUpkeep);
        return combined;
    }

    public void addCombatTriggeredEffect(EffectSlot slot, CardEffect effect) {
        combatTriggeredEffects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public void addPersistentTriggeredEffect(EffectSlot slot, CardEffect effect) {
        persistentTriggeredEffects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public List<CardEffect> getPersistentTriggeredEffects(EffectSlot slot) {
        return persistentTriggeredEffects.getOrDefault(slot, List.of());
    }

    public void addUntilNextUpkeepTriggeredEffect(UUID expiryPlayerId, EffectSlot slot, CardEffect effect) {
        untilNextUpkeepTriggeredEffects
                .computeIfAbsent(slot, ignored -> new HashMap<>())
                .computeIfAbsent(expiryPlayerId, ignored -> new ArrayList<>())
                .add(effect);
    }

    private List<CardEffect> getUntilNextUpkeepTriggeredEffects(EffectSlot slot) {
        Map<UUID, List<CardEffect>> effectsByPlayer = untilNextUpkeepTriggeredEffects.get(slot);
        if (effectsByPlayer == null || effectsByPlayer.isEmpty()) {
            return List.of();
        }
        List<CardEffect> effects = new ArrayList<>();
        effectsByPlayer.values().forEach(effects::addAll);
        return effects;
    }

    public void clearUntilNextUpkeepTriggeredEffects(UUID expiryPlayerId) {
        untilNextUpkeepTriggeredEffects.values().forEach(effectsByPlayer -> effectsByPlayer.remove(expiryPlayerId));
        untilNextUpkeepTriggeredEffects.values().removeIf(Map::isEmpty);
    }

    /**
     * Whether this permanent has cumulative upkeep — printed, temporarily granted, or persistently
     * granted (Balduvian Shaman). Used for targeting restrictions like "that doesn't have
     * cumulative upkeep".
     */
    public boolean hasCumulativeUpkeep() {
        return hasCumulativeUpkeepIn(card.getEffects(EffectSlot.UPKEEP_TRIGGERED))
                || hasCumulativeUpkeepIn(getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED))
                || hasCumulativeUpkeepIn(getTemporaryTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED));
    }

    private static boolean hasCumulativeUpkeepIn(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof CumulativeUpkeepEffect) {
                return true;
            }
        }
        return false;
    }

    public boolean isLosesAllAbilitiesUntilEndOfTurn() {
        return losesAllAbilitiesUntilEndOfTurn || !losesAllAbilitiesUntilNextTurnControllers.isEmpty()
                || losesAllAbilitiesPermanently;
    }

    public void addLosesAllAbilitiesUntilNextTurnController(UUID controllerId) {
        losesAllAbilitiesUntilNextTurnControllers.add(controllerId);
    }

    public void clearLosesAllAbilitiesUntilNextTurnController(UUID controllerId) {
        losesAllAbilitiesUntilNextTurnControllers.remove(controllerId);
    }

    public void recordTappedPermanentForAbility(UUID permanentId) {
        if (permanentId != null && !tappedPermanentsForAbilityThisTurn.contains(permanentId)) {
            tappedPermanentsForAbilityThisTurn.add(permanentId);
        }
    }

    public void clearTappedPermanentsForAbilityThisTurn() {
        tappedPermanentsForAbilityThisTurn.clear();
    }

    public void suppressStaticEffectUntilEndOfTurn(Class<? extends CardEffect> effectType) {
        suppressedStaticEffectsUntilEndOfTurn.add(effectType);
    }

    public boolean isStaticEffectSuppressed(Class<? extends CardEffect> effectType) {
        return suppressedStaticEffectsUntilEndOfTurn.contains(effectType);
    }

    public void addAdaptOverrideUntilEndOfTurn() {
        adaptOverridesUntilEndOfTurn++;
    }

    public boolean consumeAdaptOverrideUntilEndOfTurn() {
        if (adaptOverridesUntilEndOfTurn == 0) {
            return false;
        }
        adaptOverridesUntilEndOfTurn--;
        return true;
    }

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
        this.basePowerToughnessOverriddenUntilEndOfTurn = false;
        this.basePowerOverride = 0;
        this.baseToughnessOverride = 0;
        this.cantBeBlocked = false;
        this.cantAttackThisTurn = false;
        this.cantBlockThisTurn = false;
        this.additionalBlocksUntilEndOfTurn = 0;
        this.mustBlockThisTurnIfAble = false;
        this.mustAttackThisCombat = false;
        this.mustAttackThisTurn = false;
        this.mustAttackTargetId = null;
        this.mustBeBlockedThisTurn = false;
        this.mustBeBlockedByAllThisTurn = false;
        this.blockedWithoutBlockers = false;
        this.creaturesThatCrewedThisTurn.clear();
        this.auraEffectsIgnoredThisTurn = false;
        this.dampingEngineEffectIgnoredThisTurn = false;
        this.cantRegenerateThisTurn = false;
        this.damageCantBePreventedOrRedirectedThisTurn = false;
        this.damagedCreaturesCantRegenerateThisTurn = false;
        this.exileDamagedCreaturesInsteadOfDyingThisTurn = false;
        this.exileInsteadOfDieThisTurn = false;
        this.hasDamageToOpponentCreatureBounce = false;
        this.temporaryTriggeredEffects.clear();
        this.saddled = false;
        this.animatedUntilEndOfTurn = false;
        this.animatedUntilEndOfCombat = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.animatedColor = null;
        this.grantedKeywords.clear();
        this.removedKeywords.clear();
        this.transientColors.clear();
        this.colorOverridden = false;
        this.transientSubtypes.clear();
        this.transientLandTypeOverride = null;
        this.transientCreatureTypeOverride = null;
        this.transientCreatureTypeOverrides.clear();
        this.grantedCardTypes.clear();
        this.protectionFromCardTypes.clear();
        this.protectionFromColorsUntilEndOfTurn.clear();
        this.protectionFromColorlessUntilEndOfTurn = false;
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.clear();
        this.protectionFromOpponentCreaturesUntilEndOfTurn = false;
        this.blockRestrictionsUntilEndOfTurn.clear();
        this.unblockableIfDefenderControlsUntilEndOfTurn.clear();
        this.exileIfLeavesBattlefieldUntilEndOfTurn = false;
        this.shroudIgnoredByPlayersUntilEndOfTurn.clear();
        this.cantBlockIds.clear();
        this.mustBlockIds.clear();
        this.losesAllAbilitiesUntilEndOfTurn = false;
        this.suppressedStaticEffectsUntilEndOfTurn.clear();
        this.losesAllCreatureTypesUntilEndOfTurn = false;
        this.transientRemovedSubtypes.clear();
        this.temporaryActivatedAbilities.clear();
        this.adaptOverridesUntilEndOfTurn = 0;
        expireTemporaryTextReplacements();
    }

    /**
     * Drops the "until end of turn" word substitutions (CR 612) and undoes any chosen-color swap they
     * made, so a permanent whose chosen color was renamed by Whim of Volrath goes back to the color it
     * was chosen as.
     */
    private void expireTemporaryTextReplacements() {
        this.textReplacements.removeIf(replacement -> {
            if (!replacement.untilEndOfTurn()) {
                return false;
            }
            CardColor renamedTo = textChangeWordAsColor(replacement.toWord());
            if (renamedTo != null && renamedTo == this.chosenColor) {
                setChosenColor(textChangeWordAsColor(replacement.fromWord()));
            }
            return true;
        });
    }

    /**
     * Records the single color chosen for this permanent, keeping {@link #getChosenColors()} in sync so
     * effects that read the whole chosen-color set (a chosen-color spell-cast watcher, for instance)
     * see it too.
     */
    public void setChosenColor(CardColor chosenColor) {
        this.chosenColor = chosenColor;
        this.chosenColors.clear();
        if (chosenColor != null) {
            this.chosenColors.add(chosenColor);
        }
    }

    private static CardColor textChangeWordAsColor(String word) {
        try {
            return CardColor.valueOf(word.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Reverts an "until end of turn" copy (e.g. Tilonalli's Skinshifter) back to the permanent's
     * pre-copy card. Driven by the expiry of the copy's floating layer-1 effect at the cleanup
     * step (CR 613 layer engine), not by {@link #resetModifiers()}. Safe to call more than once
     * per turn — a second expired copy effect on the same permanent finds the flag cleared.
     */
    public void revertEndOfTurnCopy() {
        if (this.copyUntilEndOfTurn && this.preCopyCard != null) {
            this.card = this.preCopyCard;
        }
        this.copyUntilEndOfTurn = false;
        this.preCopyCard = null;
    }

    /**
     * Reverts a "while the Aura is attached" copy (Metamorphic Alteration) back to the permanent's
     * pre-copy card. Driven by the expiry of the Aura's {@code WHILE_ATTACHED} floating layer-1
     * effect when the Aura leaves the battlefield or becomes unattached. Safe to call more than
     * once — a second expiry finds the flag already cleared.
     */
    public void revertWhileAttachedCopy() {
        if (this.copyWhileAttached && this.whileAttachedPreCopyCard != null) {
            this.card = this.whileAttachedPreCopyCard;
        }
        this.copyWhileAttached = false;
        this.whileAttachedPreCopyCard = null;
    }

    /**
     * Clears all "until your next turn" effects: activated abilities (e.g. Song of Freyalise)
     * and land animation (e.g. Sylvan Awakening). Called at the beginning of the controller's
     * next turn, not at end of turn.
     */
    public void clearUntilNextTurnEffects() {
        this.untilNextTurnActivatedAbilities.clear();
        this.animatedUntilNextTurn = false;
        this.untilNextTurnAnimatedPower = 0;
        this.untilNextTurnAnimatedToughness = 0;
        this.untilNextTurnSubtypes.clear();
        this.untilNextTurnKeywords.clear();
        this.untilNextTurnLandTypeOverride = null;
    }

    /**
     * Promotes a pending Wall of Dust "can't attack next turn" restriction into an active this-turn
     * restriction and clears any restriction that has already expired. Because it assigns
     * {@code cantAttackThisTurn = cantAttackNextTurn}, calling it at the start of every one of the
     * controller's turns makes the restriction hold for exactly one turn. Must be called only for the
     * active player's permanents at turn start (so a creature's restriction arms on ITS controller's
     * turn, never an opponent's).
     */
    public void promoteCantAttackNextTurn() {
        this.cantAttackThisTurn = this.cantAttackNextTurn;
        this.cantAttackNextTurn = false;
    }

    /**
     * The active "becomes a basic land type" replacement override (rule 305.7), if any. Prefers the
     * until-end-of-turn {@link #transientLandTypeOverride} (Tideshaper Mystic) over the longer-lived
     * {@link #untilNextTurnLandTypeOverride} (Orcish Farmer); either wins over the land's printed types.
     */
    public CardSubtype getEffectiveLandTypeOverride() {
        if (transientLandTypeOverride != null) {
            return transientLandTypeOverride;
        }
        if (untilNextTurnLandTypeOverride != null) {
            return untilNextTurnLandTypeOverride;
        }
        return persistentLandTypeOverride;
    }

    /**
     * Reverts an "until your next turn" copy (e.g. Shapesharer) back to the permanent's
     * pre-copy card. Called at the beginning of the ability controller's next turn.
     */
    public void revertUntilNextTurnCopy() {
        if (this.copyUntilControllerNextTurn && this.untilNextTurnPreCopyCard != null) {
            this.card = this.untilNextTurnPreCopyCard;
        }
        this.copyUntilControllerNextTurn = false;
        this.untilNextTurnPreCopyCard = null;
        this.copyUntilNextTurnControllerId = null;
    }
}
