package com.github.laxika.magicalvibes.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class Permanent {

    /** What a face-down permanent, which has no name (CR 708.2), is called wherever a name would be shown. */
    public static final String FACE_DOWN_PERMANENT = "a face-down permanent";
    /** What a face-up token with no name, such as a copy of a face-down permanent, is called in text (CR 201.2a). */
    public static final String NAMELESS_TOKEN = "a nameless token";
    /** What a face-up nontoken object with no name, such as a Clone of a face-down creature, is called in text. */
    public static final String NAMELESS_PERMANENT = "a nameless permanent";

    /**
     * {@code card}'s name for text, or a description when it has none: the card keeps no name, so rules comparing
     * names still see none (CR 201.2a).
     */
    public static String displayName(Card card) {
        String name = card.getName();
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return card.isToken() ? NAMELESS_TOKEN : NAMELESS_PERMANENT;
    }

    private final UUID id;
    private Card card;
    private Card originalCard;
    @Setter private boolean bestow;
    /** The graveyard card currently supplying this permanent's dynamic full-text copy, if any. */
    @Setter private Card fullTextCopySourceCard;
    /** Copiable characteristics before a layer-3 graveyard text change, including Clone effects. */
    @Setter private Card fullTextCopyBaseCard;
    /**
     * The card this permanent showed before a one-shot "becomes a creature" effect replaced it. That
     * effect is not a copiable value (CR 707.2), so a copy of this permanent sees this card.
     */
    @Setter private Card becomeCreatureBaseCard;
    /** The card that effect produced; once something else replaces it, {@link #becomeCreatureBaseCard} no longer applies. */
    @Setter private Card becomeCreatureResultCard;
    /** The creature card exiled by Werewhat and linked to its dynamic back face. */
    @Setter private Card werewhatCompanionCard;
    private boolean tapped;
    /** Floating restrictions that prevent this permanent from becoming tapped except while attacking. */
    private final Set<UUID> tapRestrictionEffectIds = new HashSet<>();
    /** Whether this permanent was untapped before its controller's most recent untap step. */
    @Setter private boolean untappedAtTurnStart;
    /** Set while an ability's untap cost chose this untapped source: it is tapped for {T} first, then untapped. */
    @Setter private boolean untapAfterTapCost;
    private int untapSequence;
    private int controlChangeSequence;
    /** How many of its controller's upkeeps have begun since it came under that player's control (entering or a
     *  control change), for echo's "if this permanent came under your control since the beginning of your last
     *  upkeep" (CR 702.30a): at most one during that player's upkeep means it did. */
    private int controllerUpkeepsSinceControlled;
    /** True once the "sacrifice a [permanent] instead of entering" replacement (Balduvian Trading
     *  Post) has been paid for this permanent, so the re-entry after the choice isn't replaced again. */
    @Setter private boolean entryCostPaid;
    /** True once an optional land-casualty entry choice has been completed, whether or not it was paid. */
    @Setter private boolean entryCostResolved;
    /** Null until the controller has chosen the cards to reveal for Amplify. */
    @Setter private Integer amplifyRevealedCards;
    @Setter private Boolean chosenTappedEntryState;
    /** The enter-as-a-copy source the controller chose to apply last for this entry; null until chosen. */
    @Setter private UUID chosenEnterAsCopySourceId;
    /** Chosen slot among controlled creatures for the current battlefield entry or control change. */
    @Setter private Integer chosenCreaturePosition;
    /** Whether a control change still requires the controller to position this creature. */
    @Setter private boolean creaturePositionPending;
    /** Whether the native simultaneous-entry devour choice has already finished. */
    @Setter private boolean nativeDevourResolved;
    /** Devour instances (printed, then granted) already answered while this permanent was entering (CR 702.82a). */
    @Getter @Setter private int devourInstancesResolved;
    /** Whether this entry's "as enters, discard cards" replacement choice has already been answered. */
    @Setter private boolean asEntersDiscardResolved;
    /** Whether this entry's "enter as a copy" replacement choice has already been offered and answered. */
    @Setter private boolean copyEntryResolved;
    /** Counter placements on this entering permanent whose replacement order its controller must choose (CR 616.1). */
    private final List<ChoiceContext.EntryCounterPlacement> pendingEntryCounterPlacements = new ArrayList<>();
    private boolean attacking;
    /** The UUID of the player or planeswalker this creature is attacking. Null when not attacking. */
    private UUID attackTarget;
    private boolean attackedThisTurn;
    /** Number of times this permanent has been declared as an attacker this turn. */
    @Setter private int attacksThisTurn;
    /** Number of times this permanent has attacked during this game. */
    @Setter private int attacksThisGame;
    private boolean attackedThisCombat;
    /**
     * Whether another effect reselected which player or planeswalker this creature is attacking. It is
     * still considered to have attacked what it was declared to attack, not the new defender.
     */
    @Setter private boolean attackTargetReselected;
    /** Player or permanent IDs this creature attacked during the current combat. */
    private final Set<UUID> playersAttackedThisCombat = new HashSet<>();
    /** Player or permanent IDs this creature attacked during its immediately preceding combat. */
    private final Set<UUID> playersAttackedLastCombat = new HashSet<>();
    /** Creatures that were tapped to pay this Vehicle's crew cost during the current turn. */
    private final Set<UUID> creaturesThatCrewedThisTurn = new HashSet<>();
    /** Players during whose most recent turn this creature attacked, updated as each turn ends by
     *  {@link #recordTurnEnded(UUID)}. Keyed by player rather than by controller so that "attacked
     *  during its controller's last turn" (Halls of Mist, Giant Turtle, Tangle Kelp) follows the
     *  current controller after a control change. */
    private final Set<UUID> playersDuringWhoseLastTurnThisAttacked = new HashSet<>();
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
    /** Set when this permanent attacks, blocks, or becomes blocked. A "since your last upkeep" effect
     *  reads it together with {@link #upkeepsStartedSinceAttackOrBlock}. */
    private boolean attackedOrBlockedSinceLastUpkeep;
    /** How many upkeeps each player has begun since this permanent last attacked or blocked, so that
     *  every "since your last upkeep" effect measures the window from its own controller's upkeep. */
    private final Map<UUID, Integer> upkeepsStartedSinceAttackOrBlock = new HashMap<>();
    /** Set when this permanent's "whenever this becomes the target of a spell or ability for the first
     *  time each turn, counter that spell or ability" trigger (Glyph Keeper) has already fired this
     *  turn, so it won't trigger again until the flag is reset at the next turn start. */
    @Setter private boolean becomeTargetCounterUsedThisTurn;
    private final List<Integer> blockingTargets = new ArrayList<>();
    private final List<UUID> blockingTargetIds = new ArrayList<>();
    /** Creatures this permanent blocked during the current combat, retained when it leaves combat. */
    private final List<UUID> blockedAttackerIdsThisCombat = new ArrayList<>();
    /** Permanents that dealt damage to this permanent this turn, kept so a stack entry's source
     *  snapshot carries the damage history as last-known information (CR 608.2h). Cleared at the
     *  start of each turn. */
    private final Set<UUID> permanentIdsThatDealtDamageThisTurn = new HashSet<>();
    /** Identifies the attacking band (CR 702.22) this creature was declared in, or null if it is not
     *  in a band. Shared by every member of the same band. Set at declare-attackers time and cleared
     *  by {@link #clearCombatState()} when combat ends; persists for the rest of combat even if banding
     *  is later removed (CR 702.22e). */
    @Setter private UUID bandId;
    private boolean summoningSick;
    /** The turn number on which this permanent object was put onto the battlefield, or 0 when unrecorded.
     *  Distinguishes this object from a later object of the same card after it re-enters (CR 400.7). */
    @Setter private int enteredBattlefieldTurnNumber;
    /** Perpetual power modifier attached to this card identity; survives turn cleanup and zone changes. */
    @Setter private int persistentPowerModifier;
    /** Class progression is permanent state and is independent of counters. */
    @Setter private int classLevel = 1;
    /** How many times this permanent has regenerated this turn (CR 701.19). Incremented every time a
     *  regeneration shield is actually applied; reset at turn cleanup. Read by
     *  {@code TimesSourceRegeneratedThisTurn} for Spiny Starfish. */
    @Setter private int timesRegeneratedThisTurn;
    private UUID attachedTo;
    /** Realms currently hosting this permanent. */
    private final Set<UUID> hostedByRealmIds = new HashSet<>();
    /**
     * The last permanent this one was attached to, kept after {@link #attachedTo} is cleared.
     * Triggers that fire once the host has already left the battlefield (Kusari-Gama's "whenever
     * equipped creature deals damage …", where the host may have died to the same combat damage)
     * read it as last-known information.
     */
    private UUID lastAttachedTo;
    /**
     * Soulbond pairing (CR 702.95): id of the other creature this permanent is paired with, or
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
    /** Whether an as-enters color choice has been completed, including an optional empty choice. */
    @Setter private boolean chosenColorChoiceMade;

    /** CR 613.7 timestamp of the latest color choice for this permanent's "becomes the chosen color" effect
     *  (Puca's Eye): the effect is created when the choice is made, not when the permanent entered. */
    @Setter private long chosenColorTimestamp;
    @Setter private String chosenName;
    /** Names chosen by resolving abilities of this permanent during its current battlefield visit. */
    private final Set<String> chosenNamesAtResolution = new HashSet<>();
    /** Second card name chosen "as this enters" when two players each name a card
     *  (Null Chamber: the controller's pick → {@link #chosenName}, the opponent's → here). */
    @Setter private String secondChosenName;
    @Setter private CardSubtype chosenSubtype;
    /** Whether this permanent has completed its custom buddy-list choice as it entered. */
    @Setter private boolean buddyListChoiceMade;
    @Setter private CardType chosenCardType;
    /** Card types checked on an associated tracker list, separate from permanent counters. */
    private final Set<CardType> trackedSpellCardTypes = EnumSet.noneOf(CardType.class);
    /** Identifies the characteristics supplying the current tracker, so new copy lists start empty. */
    @Setter private UUID trackedSpellCardTypesSourceCardId;
    /** Second basic land type chosen "as this enters" when the card chooses two types
     *  (Illusionary Terrain: first type → {@link #chosenSubtype}, second → here). */
    @Setter private CardSubtype secondChosenSubtype;
    @Setter private String chosenMode;
    @Setter private AttackDirection chosenAttackDirection;
    /** Mode chosen by each player for an entering permanent whose ability says each player chooses. */
    private final Map<UUID, String> chosenModeByPlayer = new HashMap<>();
    /** The number last chosen for this permanent by a "choose a number between X and Y" effect
     *  (e.g. Shapeshifter). Read by {@link com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource}
     *  to drive a characteristic-defining P/T. Defaults to 0 until a number is chosen. */
    @Setter private int chosenNumber;
    /** Numbers already chosen for The Toymaker's Trap while this permanent remains on the battlefield. */
    private final Set<Integer> toymakersTrapChosenNumbers = new HashSet<>();
    /** The ten digits written down by Duelists' Convocation International. */
    @Setter private List<Integer> chosenNumberDigits = List.of();
    /** The positions of digits crossed out by Duelists' Convocation International. */
    private final Set<Integer> crossedNumberDigitPositions = new HashSet<>();
    /**
     * Labels of the modes this permanent has already had chosen for a "choose one that hasn't been
     * chosen" modal trigger (Demonic Pact). Consumed modes are never offered again while this object
     * stays on the battlefield.
     */
    private final Set<String> chosenModeLabels = new HashSet<>();
    /**
     * The "as this enters, flip a coin" flip (Molten Sentry) whose Krark's Thumb results disagreed, while its
     * flipping player has not yet chosen the kept result; {@code null} otherwise. The result is chosen before this
     * permanent's enter abilities are processed.
     */
    @Setter private RecordedCoinFlip entryCoinFlipAwaitingChoice;
    /** Labels of modes chosen this turn for a turn-scoped modal trigger. */
    private final Set<String> chosenModeLabelsThisTurn = new HashSet<>();
    /** Room doors unlocked on this permanent, in door order. This state survives turn resets. */
    @Getter(AccessLevel.NONE)
    private final Set<Integer> unlockedRoomDoors = new HashSet<>();
    @Setter private ManaValueParity chosenManaValueParity;
    @Setter private UUID chosenPermanentId;
    /** Player targeted by a linked enter-the-battlefield ability. */
    @Setter private UUID rememberedTargetPlayerId;
    /** Players chosen by an as-enters ability that remembers more than one player. */
    @Getter private final List<UUID> rememberedTargetPlayerIds = new ArrayList<>();
    /** Players chosen by an as-enters effect such as Bitter Feud. */
    private final List<UUID> chosenPlayerIds = new ArrayList<>();
    /**
     * The player who secretly chose a player as this permanent entered (Guardian Archon). Only that
     * player can reveal the choice, so a later controller can't pay a reveal cost.
     */
    @Setter private UUID asEntersPlayerChooserId;
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
    /** The card most recently exiled as an activated-ability cost when the resolving ability needs it. */
    @Setter private Card chosenExiledCard;
    /** Last-known snapshot of a permanent sacrificed as payment for an ability that needs it at resolution. */
    @Setter private Permanent chosenSacrificedPermanentSnapshot;
    /** Effective power of the permanent sacrificed to pay an ability cost, read while it was still on the battlefield. */
    @Setter private int chosenSacrificedPower;
    /** Effective toughness of the permanent sacrificed to pay an ability cost, read while it was still on the battlefield. */
    @Setter private int chosenSacrificedToughness;
    /** Effective color count of the permanent sacrificed to pay an ability cost, read while it was still on the battlefield. */
    @Setter private int chosenSacrificedColorCount;
    /** True while this permanent has the suspected designation. Cleared only by a leave-and-return
     *  event or an effect that specifically makes it no longer suspected. */
    @Setter private boolean suspected;
    /** True while this permanent represents a commander. */
    @Setter private boolean commander;
    /** When true, this attacking creature is blocked even though no creature is blocking it
     *  (Dazzling Beauty). CR 509.1h — it deals no combat damage, since a blocked creature with no
     *  blockers to assign damage to assigns none. Cleared at end of turn. */
    @Setter private boolean blockedWithoutBlockers;
    /**
     * The position of the first digit Duelists' Convocation International wrote down that equals {@code digit} and
     * isn't crossed out, or -1 when there is none.
     */
    public int uncrossedChosenNumberDigitPosition(int digit) {
        for (int position = 0; position < chosenNumberDigits.size(); position++) {
            if (!crossedNumberDigitPositions.contains(position) && chosenNumberDigits.get(position) == digit) {
                return position;
            }
        }
        return -1;
    }

    /** If true, this permanent's controller sacrifices it at the beginning of the next cleanup step —
     *  the Mirage flash clause ({@code FlashCastWithCleanupSacrificeEffect}) when the spell was cast
     *  any time a sorcery couldn't have been cast. The cleanup sweep sacrifices it before
     *  {@link #resetModifiers()} runs, so the flag deliberately survives that reset. */
    @Setter private boolean sacrificeAtNextCleanup;
    /** The player whose next untap step returns this permanent to its owner's hand
     *  ({@code ReturnSourceToHandAtNextUntapEffect}, Undiscovered Paradise), or null. This is the player
     *  who set it up, not whoever controls the permanent at that untap step. Swept by
     *  {@code UntapStepService} (not during a skipped untap step). Survives {@link #resetModifiers()}. */
    @Setter private UUID returnToHandAtNextUntapPlayerId;
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
    /** Current base toughness used as loyalty by an attached Planeswalkerificate-style Aura. */
    @Setter private Integer toughnessAsLoyalty;
    private boolean faceDown;
    /** An automatic Illusionary Mask turn-up whose engine triggers still need to be collected. */
    @Setter private boolean pendingAutomaticTurnFaceUp;
    /**
     * A token whose creation event already applied the token replacements that change what is created (Divine
     * Visitation, Fisher's Talent) in the order its creator chose (CR 616.1), so entering doesn't apply them again.
     */
    @Setter private boolean tokenKindReplacementsApplied;
    /** Illusionary Mask: this face-down permanent turns face up before it deals or is dealt damage, or becomes tapped. */
    @Setter private boolean turnsFaceUpOnDamageOrTap;
    private boolean cloaked;
    /** Ward supplied by casting this permanent face down using disguise. */
    private boolean faceDownWard;
    private int faceDownPower;
    private int faceDownToughness;
    private final Set<CardType> faceDownCardTypes = EnumSet.noneOf(CardType.class);
    private final Set<CardSubtype> faceDownSubtypes = EnumSet.noneOf(CardSubtype.class);
    /** Whether this face-down permanent was put onto the battlefield by manifest. */
    @Setter private boolean manifested;
    /** All counters on this permanent, keyed by {@link CounterType}. Absent keys mean zero counters.
     *  New counter kinds require only a new {@link CounterType} value — never a new field here.
     *  Read/write via {@link #getCounterCount(CounterType)} / {@link #setCounterCount(CounterType, int)}. */
    private final Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
    private final Map<CounterType, Long> lastCounterRemovalVersions = new EnumMap<>(CounterType.class);
    private int countersRemovedSinceTriggerCheck;
    private int loyaltyCountersRemovedSinceTriggerCheck;
    private int timeCountersRemovedSinceTriggerCheck;
    private int plusOnePlusOneCountersRemovedSinceTriggerCheck;
    /** Latest placement timestamp for each counter kind. Keyword counters use this to participate
     *  in layer ordering; removing counters does not remove their timestamp. */
    private final Map<CounterType, Long> counterTimestamps = new EnumMap<>(CounterType.class);
    @Setter private int loyaltyActivationsThisTurn;
    /**
     * Siege battles: the opponent chosen to protect this battle as it entered. Only that player
     * may block attackers attacking this battle; everyone else (including the controller) may attack it.
     */
    @Setter private UUID protectorPlayerId;
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield (CR 614.6). */
    @Setter private boolean exileIfLeavesBattlefield;
    /** If true, this permanent is exiled instead of being put into a graveyard from the battlefield. */
    @Setter private boolean exileIfDying;
    @Setter private long exileIfDyingTimestamp;
    @Setter private Integer lastKnownAuraCount;
    @Setter private Integer lastKnownEquipmentCount;
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
    /** The permanent whose leaving the battlefield returned this one from the exile it was in "until" that permanent
     *  left (Lagrella, the Magpie: "when an exiled card enters under your control this way"), or {@code null}. */
    @Setter private UUID returnedWhenSourceLeftId;
    /** Land permanent ids that this Cyclopean Tomb put mire counters onto while it was on the battlefield. */
    private final Set<UUID> mireCounterLandIds = new HashSet<>();
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
    private boolean transformed;
    /** Number of face changes, used to prevent an older ability from transforming this object again. */
    @Setter private int transformationSequence;
    /** Effective toughness recorded immediately before this object leaves the battlefield. */
    @Setter private Integer lastKnownToughness;
    /** Effective power recorded immediately before this permanent leaves the battlefield. */
    @Setter private Integer lastKnownPower;
    /** Colors immediately before this permanent left the battlefield. */
    @Setter private Set<CardColor> lastKnownColors;
    /** Effective subtypes immediately before this permanent left the battlefield. */
    @Setter private Set<CardSubtype> lastKnownSubtypes;
    /** Effective supertypes immediately before this permanent left the battlefield (layer-granted ones included). */
    @Setter private Set<CardSupertype> lastKnownSupertypes;
    /** The card as this permanent last existed on the battlefield, every continuous effect applied (its types, subtypes,
     *  supertypes, colors and keywords), recorded on its departure snapshot: "if it was a Soldier" reads it once the
     *  permanent is gone (CR 608.2h). {@code null} on a permanent that hasn't left. */
    @Setter private Card lastKnownCard;
    /** Keywords this permanent had from continuous effects that end as it leaves the battlefield (an Aura's or another
     *  permanent's static grant), kept so a last-known-information copy still has them (CR 608.2h). */
    @Setter private Set<Keyword> lastKnownKeywords;
    /** Whether this permanent had lost its printed abilities as it left the battlefield (a "loses all abilities"
     *  effect, or a land type change, CR 305.7), recorded on its last-known-information copy (CR 603.10a). */
    @Setter private boolean lastKnownLostPrintedAbilities;
    /** Whether this permanent was kicked when cast (tracked for "if wasn't kicked" triggers). */
    @Setter private boolean kicked;
    /** Whether this permanent was cast for its evoke cost (gates the evoke sacrifice ETB trigger). */
    @Setter private boolean evoked;
    /** Whether this permanent was cast using an escape permission. */
    @Setter private boolean escaped;
    /** Whether this permanent was cast for its prowl cost (gates "if its prowl cost was paid" ETB triggers). */
    @Setter private boolean prowl;
    /** Whether this permanent was cast for its Warp cost. */
    @Setter private boolean castWithWarp;
    /** Whether this permanent was cast for its madness cost. */
    @Setter private boolean madness;
    /** The alternative cost this permanent was cast for; {@code null} when none was (CR 118.9). */
    @Setter private AlternativeCostKind alternativeCost;
    /** Effective toughness of the permanent sacrificed to pay this permanent's alternate cost. */
    @Setter private int alternateCostSacrificedToughness;
    /** Mana value of the creature returned to pay this permanent's web-slinging cost, when applicable. */
    @Setter private Integer webSlingingReturnedCreatureManaValue;
    /** Whether this permanent was cast for its spectacle cost. */
    @Setter private boolean spectacle;
    /** Whether this permanent's optional collect-evidence additional cost was paid. */
    @Setter private boolean collectEvidenceCostPaid;
    /**
     * Card of the permanent sacrificed to pay this permanent's spell's sacrifice cost, so an enters ability's
     * "if the sacrificed creature was …" is checked as the ability triggers (Graven Archfiend).
     */
    @Setter private Card castSacrificedCard;
    /** Whether this permanent's spell had its waterbend additional cost paid. */
    @Setter private boolean waterbendCostPaid;
    /** Whether the spell's optional reveal-a-card-from-hand additional cost was paid. */
    @Setter private boolean revealCardFromHandCostPaid;
    /** Whether the spell's controller controlled a Dragon when the spell was finished being cast. */
    @Setter private boolean controlledDragonAsCast;
    /** Whether the spell's Gift was promised as it was cast. */
    @Setter private boolean giftPromised;
    /** Cards exiled to pay the delve cost of the spell this permanent resolved from, kept after they leave exile. */
    @Setter private List<Card> delvedCards = List.of();
    /** Repeatable additional mana payments made to cast this permanent's spell. */
    private List<String> repeatedAdditionalCosts = List.of();
    /** Whether the required tribute counters were placed on this permanent as it entered. */
    @Setter private boolean tributePaid;
    /** Whether this permanent is renowned (CR 702.111): set the first time a renown trigger resolves,
     *  and read by "if it's renowned" abilities. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean renowned;
    /** Makeshift Mannequin's "for as long as this creature has a mannequin counter on it" grant (CR 611.2b).
     *  Set when the creature enters with its mannequin counter, cleared when its last mannequin counter is
     *  removed, and never restarted by a later counter. */
    @Setter private boolean mannequinGrantActive;
    /** Whether this permanent has become monstrous. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean monstrous;
    /** The player who paid this permanent's motivate cost, or null if it has not been motivated. Motivation belongs to that
     *  player: after control changes it no longer applies to the new controller. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private UUID motivatedByPlayerId;

    public boolean isMotivated() {
        return motivatedByPlayerId != null;
    }
    /** Whether this permanent's Case has been solved. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean solved;
    /** Whether this permanent is harnessed. Permanent state; never cleared by {@link #resetModifiers()}. */
    @Setter private boolean harnessed;
    /** Number of times this permanent has mutated. Permanent state; never cleared by {@link #resetModifiers()}. */
    private int timesMutated;
    /** Whether this permanent is saddled until end of turn. */
    @Setter private boolean saddled;
    /** Zone the spell that produced this permanent was cast from, when known (gates "if cast from a
     *  graveyard, it enters with … counters" as-enters replacements — e.g. Worldheart Phoenix). */
    @Setter private Zone castFromZone;
    /** Owner of the graveyard the spell that produced this permanent was cast from, when cast from a graveyard. */
    @Setter private UUID castFromGraveyardOwnerId;
    /** Zone this permanent entered the battlefield from, when known. */
    @Setter private Zone enteredFromZone;
    /** Source permanent whose ability put this permanent onto the battlefield, when applicable. */
    @Setter private UUID putOntoBattlefieldWithAbilitySourcePermanentId;
    /** Whether this permanent entered the battlefield by resolving as a spell its controller cast
     *  (as opposed to being put onto the battlefield), gating "if you cast it" abilities.
     *  NOT cleared by {@link #resetModifiers()}. */
    @Setter private boolean cast;
    /** Controller who cast the spell that became this permanent, retained through control changes. */
    @Setter private UUID castControllerId;
    /** Mana spent to cast the spell that produced this permanent, for as-enters conditions. */
    @Setter private int manaSpentToCast;
    /**
     * Mana of each color spent to cast the spell that produced this permanent, recorded as it resolves into this
     * permanent; empty when it wasn't cast. Read by {@code ColorManaPairsSpentToCast} as each of its own enters
     * abilities resolves, so one ability's resolution doesn't take the payment from the others (Bladecoil Serpent).
     */
    private Map<ManaColor, Integer> manaSpentByColorToCast = Map.of();
    /** Total bloodthirst granted to the spell that produced this permanent while it was on the stack
     *  (Bloodlord of Vaasgoth). Read as an as-enters replacement alongside the card's printed
     *  bloodthirst; per CR 702.54c each instance applies separately, so grants simply add up. */
    @Setter private int grantedBloodthirst;
    /** Numeric devour grant carried from the spell that produced this permanent. */
    @Setter private int grantedDevour;
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
    /**
     * Physical components of a creature merged by mutate, ordered from top to bottom. When the permanent leaves the
     * battlefield one permanent leaves and each component moves to its own destination (CR 731.3,
     * {@link #cardsLeavingBattlefield()}). The copy constructor keeps the
     * list, while an object that copies this creature's characteristics is a single object without components.
     */
    private final List<Card> mutatedComponentCards = new ArrayList<>();
    /** CR 613.7 timestamp: stamped from {@link GameData#nextTimestamp()} when this permanent
     *  enters a battlefield, and re-stamped each time it becomes attached (CR 613.7e — Auras and
     *  Equipment). Control changes do NOT re-stamp (CR 613.7c). Stays 0 for permanents added to
     *  a battlefield directly in tests; the layered engine falls back to battlefield position
     *  order for equal timestamps (see {@code agent-docs/LAYER_SYSTEM.md}). */
    @Setter private long timestamp;

    public void setAttackedOrBlockedSinceLastUpkeep(boolean attackedOrBlocked) {
        this.attackedOrBlockedSinceLastUpkeep = attackedOrBlocked;
        if (attackedOrBlocked) {
            upkeepsStartedSinceAttackOrBlock.clear();
        }
    }

    /** Records that the given player's upkeep step began, for "since your last upkeep" windows. */
    public void markUpkeepStarted(UUID playerId) {
        upkeepsStartedSinceAttackOrBlock.merge(playerId, 1, Integer::sum);
    }

    /**
     * Whether this permanent attacked, blocked, or became blocked since the given player's last
     * upkeep, read during that player's current upkeep: only the current upkeep may have begun since.
     */
    public boolean attackedOrBlockedSinceUpkeepOf(UUID playerId) {
        return attackedOrBlockedSinceLastUpkeep
                && upkeepsStartedSinceAttackOrBlock.getOrDefault(playerId, 0) <= 1;
    }

    public Permanent(Card card) {
        this(card, UUID.randomUUID());
    }

    private Permanent(Card card, UUID id) {
        this.id = id;
        // A card wrapped in a Permanent is live game state shared with AI simulation copies —
        // freeze it so any later mutation of the Card object fails fast instead of leaking.
        card.freeze();
        this.card = card;
        this.originalCard = card;
        this.bestow = false;
        this.tapped = false;
        this.untappedAtTurnStart = true;
        this.attackedThisTurn = false;
        this.attacksThisTurn = 0;
        this.attacksThisGame = 0;
        this.attackedThisCombat = false;
        this.summoningSick = true;
    }

    /**
     * A stand-in for an instant or sorcery spell whose abilities function on the stack (CR 113.6),
     * identified as that stack object, so an ability it grants names the spell itself (a copy
     * names the copy, not the original) as its granting source.
     */
    public static Permanent forSpellOnStack(StackEntry spell) {
        return new Permanent(spell.getCard(), spell.getTargetableId());
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
        this.fullTextCopyBaseCard = source.fullTextCopyBaseCard;
        this.becomeCreatureBaseCard = source.becomeCreatureBaseCard;
        this.becomeCreatureResultCard = source.becomeCreatureResultCard;
        this.werewhatCompanionCard = source.werewhatCompanionCard;
        this.tapped = source.tapped;
        this.tapRestrictionEffectIds.addAll(source.tapRestrictionEffectIds);
        this.untappedAtTurnStart = source.untappedAtTurnStart;
        this.untapSequence = source.untapSequence;
        this.controlChangeSequence = source.controlChangeSequence;
        this.controllerUpkeepsSinceControlled = source.controllerUpkeepsSinceControlled;
        this.entryCostPaid = source.entryCostPaid;
        this.entryCostResolved = source.entryCostResolved;
        this.amplifyRevealedCards = source.amplifyRevealedCards;
        this.chosenTappedEntryState = source.chosenTappedEntryState;
        this.chosenEnterAsCopySourceId = source.chosenEnterAsCopySourceId;
        this.chosenCreaturePosition = source.chosenCreaturePosition;
        this.creaturePositionPending = source.creaturePositionPending;
        this.nativeDevourResolved = source.nativeDevourResolved;
        this.devourInstancesResolved = source.devourInstancesResolved;
        this.asEntersDiscardResolved = source.asEntersDiscardResolved;
        this.copyEntryResolved = source.copyEntryResolved;
        this.pendingEntryCounterPlacements.addAll(source.pendingEntryCounterPlacements);
        this.attacking = source.attacking;
        this.attackTarget = source.attackTarget;
        this.attackedThisTurn = source.attackedThisTurn;
        this.attacksThisTurn = source.attacksThisTurn;
        this.attacksThisGame = source.attacksThisGame;
        this.attackedThisCombat = source.attackedThisCombat;
        this.attackTargetReselected = source.attackTargetReselected;
        this.playersAttackedThisCombat.addAll(source.playersAttackedThisCombat);
        this.playersAttackedLastCombat.addAll(source.playersAttackedLastCombat);
        this.creaturesThatCrewedThisTurn.addAll(source.creaturesThatCrewedThisTurn);
        this.playersDuringWhoseLastTurnThisAttacked.addAll(source.playersDuringWhoseLastTurnThisAttacked);
        this.blocking = source.blocking;
        this.blockedThisTurn = source.blockedThisTurn;
        this.blockedThisCombat = source.blockedThisCombat;
        this.blockedOrWasBlockedSinceLastUpkeep = source.blockedOrWasBlockedSinceLastUpkeep;
        this.attackedOrBlockedSinceLastUpkeep = source.attackedOrBlockedSinceLastUpkeep;
        this.upkeepsStartedSinceAttackOrBlock.putAll(source.upkeepsStartedSinceAttackOrBlock);
        this.becomeTargetCounterUsedThisTurn = source.becomeTargetCounterUsedThisTurn;
        this.blockingTargets.addAll(source.blockingTargets);
        this.blockingTargetIds.addAll(source.blockingTargetIds);
        this.blockedAttackerIdsThisCombat.addAll(source.blockedAttackerIdsThisCombat);
        this.permanentIdsThatDealtDamageThisTurn.addAll(source.permanentIdsThatDealtDamageThisTurn);
        this.bandId = source.bandId;
        this.summoningSick = source.summoningSick;
        this.enteredBattlefieldTurnNumber = source.enteredBattlefieldTurnNumber;
        this.persistentPowerModifier = source.persistentPowerModifier;
        this.classLevel = source.classLevel;
        this.timesRegeneratedThisTurn = source.timesRegeneratedThisTurn;
        this.attachedTo = source.attachedTo;
        this.lastAttachedTo = source.lastAttachedTo;
        this.hostedByRealmIds.addAll(source.hostedByRealmIds);
        this.pairedWithId = source.pairedWithId;
        this.chosenColor = source.chosenColor;
        this.chosenColors.addAll(source.chosenColors);
        this.chosenColorChoiceMade = source.chosenColorChoiceMade;
        this.chosenColorTimestamp = source.chosenColorTimestamp;
        this.chosenName = source.chosenName;
        this.chosenNamesAtResolution.addAll(source.chosenNamesAtResolution);
        this.secondChosenName = source.secondChosenName;
        this.chosenSubtype = source.chosenSubtype;
        this.buddyListChoiceMade = source.buddyListChoiceMade;
        this.chosenCardType = source.chosenCardType;
        this.trackedSpellCardTypes.addAll(source.trackedSpellCardTypes);
        this.trackedSpellCardTypesSourceCardId = source.trackedSpellCardTypesSourceCardId;
        this.secondChosenSubtype = source.secondChosenSubtype;
        this.chosenMode = source.chosenMode;
        this.chosenAttackDirection = source.chosenAttackDirection;
        this.chosenModeByPlayer.putAll(source.chosenModeByPlayer);
        this.chosenNumber = source.chosenNumber;
        this.toymakersTrapChosenNumbers.addAll(source.toymakersTrapChosenNumbers);
        this.chosenNumberDigits = List.copyOf(source.chosenNumberDigits);
        this.crossedNumberDigitPositions.addAll(source.crossedNumberDigitPositions);
        this.entryCoinFlipAwaitingChoice = source.entryCoinFlipAwaitingChoice;
        this.chosenModeLabels.addAll(source.chosenModeLabels);
        this.chosenModeLabelsThisTurn.addAll(source.chosenModeLabelsThisTurn);
        this.unlockedRoomDoors.addAll(source.unlockedRoomDoors);
        this.chosenManaValueParity = source.chosenManaValueParity;
        this.chosenPermanentId = source.chosenPermanentId;
        this.rememberedTargetPlayerId = source.rememberedTargetPlayerId;
        this.rememberedTargetPlayerIds.addAll(source.rememberedTargetPlayerIds);
        this.chosenPlayerIds.addAll(source.chosenPlayerIds);
        this.asEntersPlayerChooserId = source.asEntersPlayerChooserId;
        this.lifeLostWhenEntered = source.lifeLostWhenEntered;
        this.lifeLostWhenEnteredControllerId = source.lifeLostWhenEnteredControllerId;
        this.tappedPermanentsForAbilityThisTurn.addAll(source.tappedPermanentsForAbilityThisTurn);
        this.chosenCard = source.chosenCard;
        this.lastChosenExiledCard = source.lastChosenExiledCard;
        this.chosenExiledCard = source.chosenExiledCard;
        this.chosenSacrificedPermanentSnapshot = source.chosenSacrificedPermanentSnapshot == null
                ? null : new Permanent(source.chosenSacrificedPermanentSnapshot);
        this.chosenSacrificedPower = source.chosenSacrificedPower;
        this.chosenSacrificedToughness = source.chosenSacrificedToughness;
        this.chosenSacrificedColorCount = source.chosenSacrificedColorCount;
        this.suspected = source.suspected;
        this.commander = source.commander;
        this.blockedWithoutBlockers = source.blockedWithoutBlockers;
        this.prepared = source.prepared;
        this.phasedOutIndirectly = source.phasedOutIndirectly;
        this.preparedSpellCardId = source.preparedSpellCardId;
        this.sacrificeAtNextCleanup = source.sacrificeAtNextCleanup;
        this.returnToHandAtNextUntapPlayerId = source.returnToHandAtNextUntapPlayerId;
        this.toughnessAsLoyalty = source.toughnessAsLoyalty;
        this.faceDown = source.faceDown;
        this.pendingAutomaticTurnFaceUp = source.pendingAutomaticTurnFaceUp;
        this.tokenKindReplacementsApplied = source.tokenKindReplacementsApplied;
        this.turnsFaceUpOnDamageOrTap = source.turnsFaceUpOnDamageOrTap;
        this.cloaked = source.cloaked;
        this.faceDownWard = source.faceDownWard;
        this.faceDownPower = source.faceDownPower;
        this.faceDownToughness = source.faceDownToughness;
        this.faceDownCardTypes.addAll(source.faceDownCardTypes);
        this.faceDownSubtypes.addAll(source.faceDownSubtypes);
        this.manifested = source.manifested;
        this.untapAfterTapCost = source.untapAfterTapCost;
        this.counters.putAll(source.counters);
        this.lastCounterRemovalVersions.putAll(source.lastCounterRemovalVersions);
        this.countersRemovedSinceTriggerCheck = source.countersRemovedSinceTriggerCheck;
        this.loyaltyCountersRemovedSinceTriggerCheck = source.loyaltyCountersRemovedSinceTriggerCheck;
        this.timeCountersRemovedSinceTriggerCheck = source.timeCountersRemovedSinceTriggerCheck;
        this.plusOnePlusOneCountersRemovedSinceTriggerCheck = source.plusOnePlusOneCountersRemovedSinceTriggerCheck;
        this.counterTimestamps.putAll(source.counterTimestamps);
        this.loyaltyActivationsThisTurn = source.loyaltyActivationsThisTurn;
        this.protectorPlayerId = source.protectorPlayerId;
        this.enteredFromGraveyardOwnerId = source.enteredFromGraveyardOwnerId;
        this.enteredFromExile = source.enteredFromExile;
        this.returnedWhenSourceLeftId = source.returnedWhenSourceLeftId;
        this.exileIfLeavesBattlefield = source.exileIfLeavesBattlefield;
        this.exileIfDying = source.exileIfDying;
        this.exileIfDyingTimestamp = source.exileIfDyingTimestamp;
        this.lastKnownAuraCount = source.lastKnownAuraCount;
        this.lastKnownEquipmentCount = source.lastKnownEquipmentCount;
        this.enteredViaUnearth = source.enteredViaUnearth;
        this.mireCounterLandIds.addAll(source.mireCounterLandIds);
        this.markedDamage = source.markedDamage;
        this.markedDamageBySource.putAll(source.markedDamageBySource);
        this.damagedByDeathtouch = source.damagedByDeathtouch;
        this.transformed = source.transformed;
        this.transformationSequence = source.transformationSequence;
        this.lastKnownToughness = source.lastKnownToughness;
        this.lastKnownPower = source.lastKnownPower;
        this.lastKnownColors = source.lastKnownColors == null ? null : Set.copyOf(source.lastKnownColors);
        this.lastKnownSubtypes = source.lastKnownSubtypes == null ? null : Set.copyOf(source.lastKnownSubtypes);
        this.lastKnownSupertypes = source.lastKnownSupertypes == null ? null : Set.copyOf(source.lastKnownSupertypes);
        this.lastKnownCard = source.lastKnownCard;
        this.lastKnownKeywords = source.lastKnownKeywords == null ? null : Set.copyOf(source.lastKnownKeywords);
        this.lastKnownLostPrintedAbilities = source.lastKnownLostPrintedAbilities;
        this.kicked = source.kicked;
        this.evoked = source.evoked;
        this.escaped = source.escaped;
        this.prowl = source.prowl;
        this.castWithWarp = source.castWithWarp;
        this.madness = source.madness;
        this.alternativeCost = source.alternativeCost;
        this.alternateCostSacrificedToughness = source.alternateCostSacrificedToughness;
        this.webSlingingReturnedCreatureManaValue = source.webSlingingReturnedCreatureManaValue;
        this.spectacle = source.spectacle;
        this.collectEvidenceCostPaid = source.collectEvidenceCostPaid;
        this.castSacrificedCard = source.castSacrificedCard;
        this.waterbendCostPaid = source.waterbendCostPaid;
        this.revealCardFromHandCostPaid = source.revealCardFromHandCostPaid;
        this.controlledDragonAsCast = source.controlledDragonAsCast;
        this.giftPromised = source.giftPromised;
        this.delvedCards = source.delvedCards;
        this.repeatedAdditionalCosts = source.repeatedAdditionalCosts;
        this.tributePaid = source.tributePaid;
        this.renowned = source.renowned;
        this.mannequinGrantActive = source.mannequinGrantActive;
        this.castFromZone = source.castFromZone;
        this.castFromGraveyardOwnerId = source.castFromGraveyardOwnerId;
        this.enteredFromZone = source.enteredFromZone;
        this.putOntoBattlefieldWithAbilitySourcePermanentId = source.putOntoBattlefieldWithAbilitySourcePermanentId;
        this.cast = source.cast;
        this.castControllerId = source.castControllerId;
        this.manaSpentToCast = source.manaSpentToCast;
        this.manaSpentByColorToCast = source.manaSpentByColorToCast;
        this.monstrous = source.monstrous;
        this.motivatedByPlayerId = source.motivatedByPlayerId;
        this.solved = source.solved;
        this.harnessed = source.harnessed;
        this.timesMutated = source.timesMutated;
        this.saddled = source.saddled;
        this.grantedBloodthirst = source.grantedBloodthirst;
        this.grantedDevour = source.grantedDevour;
        this.devouredCreatures.addAll(source.devouredCreatures);
        this.meldComponentCards.addAll(source.meldComponentCards);
        this.mutatedComponentCards.addAll(source.mutatedComponentCards);
        this.timestamp = source.timestamp;
    }

    public Card getOriginalCard() {
        return originalCard;
    }

    public void recordMutation() {
        timesMutated++;
    }

    /** This permanent's name as everybody may read it while it's on the battlefield (CR 708.2, 708.5). */
    public String getPublicName() {
        return faceDown ? FACE_DOWN_PERMANENT : displayName(getCard());
    }

    public void setFaceDown(int power, int toughness, Set<CardType> cardTypes) {
        setFaceDown(power, toughness, cardTypes, Set.of());
    }

    public void setFaceDown(int power, int toughness, Set<CardType> cardTypes,
                            Set<CardSubtype> subtypes) {
        this.faceDown = true;
        this.cloaked = false;
        this.faceDownWard = false;
        this.faceDownPower = power;
        this.faceDownToughness = toughness;
        this.faceDownCardTypes.clear();
        this.faceDownCardTypes.addAll(cardTypes);
        this.faceDownSubtypes.clear();
        this.faceDownSubtypes.addAll(subtypes);
    }

    public void setFaceDownAsCloaked() {
        setFaceDown(2, 2, Set.of(CardType.CREATURE));
        this.cloaked = true;
    }

    public void setFaceDownAsDisguised() {
        setFaceDown(2, 2, Set.of(CardType.CREATURE));
        this.faceDownWard = true;
    }

    public void turnFaceUp() {
        this.faceDown = false;
        this.cloaked = false;
        this.faceDownWard = false;
        this.faceDownPower = 0;
        this.faceDownToughness = 0;
        this.faceDownCardTypes.clear();
        this.faceDownSubtypes.clear();
        this.manifested = false;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    /** Replaces the card represented by this permanent while preserving the permanent object and its state. */
    public void exchangeCard(Card card) {
        card.freeze();
        this.card = card;
        this.originalCard = card;
    }

    /**
     * Makes this permanent, which a resolving copy of a permanent spell became, a token (CR 111.13):
     * both the face it shows and its original card, so it stays a token wherever it later goes.
     */
    public void becomeTokenFromCopiedSpell() {
        boolean showingOriginal = card == originalCard;
        originalCard = tokenVersionOf(originalCard);
        card = showingOriginal ? originalCard : tokenVersionOf(card);
    }

    private static Card tokenVersionOf(Card card) {
        if (card.isToken()) return card;
        Card token = card.createRuntimeCopy();
        token.setToken(true);
        token.freeze();
        return token;
    }

    public void restoreBombardmentCard(Card card) {
        this.card = card;
        this.originalCard = card;
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

    /** Removes all damage currently marked on this permanent and clears its deathtouch damage memory. */
    public void healDamage() {
        setMarkedDamage(0);
        this.damagedByDeathtouch = false;
    }

    /**
     * Records damage dealt by a specific source object and updates the total. {@code sourceId} may
     * be null when the source is unknown (total still increases; per-source map is unchanged).
     */
    public void addMarkedDamage(UUID sourceId, int amount) {
        if (amount <= 0) {
            return;
        }
        if (faceDown && turnsFaceUpOnDamageOrTap) {
            turnFaceUp();
            pendingAutomaticTurnFaceUp = true;
        }
        this.markedDamage = SaturatingMath.add(this.markedDamage, amount);
        if (sourceId != null) {
            this.markedDamageBySource.merge(sourceId, amount, SaturatingMath::add);
        }
    }

    /** True when at least one source has marked damage greater than or equal to the lethal threshold. */
    public boolean hasLethalDamageFromSingleSource(int lethalDamageThreshold) {
        if (lethalDamageThreshold <= 0) {
            return false;
        }
        for (int amount : markedDamageBySource.values()) {
            if (amount >= lethalDamageThreshold) {
                return true;
            }
        }
        return false;
    }

    public boolean tap() {
        if (!tapped && !attacking && !tapRestrictionEffectIds.isEmpty()) {
            return false;
        }
        if (faceDown && turnsFaceUpOnDamageOrTap) {
            turnFaceUp();
            pendingAutomaticTurnFaceUp = true;
        }
        boolean becameTapped = !this.tapped;
        this.tapped = true;
        return becameTapped;
    }

    /** Adds one floating tap restriction to this permanent. */
    public void addTapRestriction(UUID effectId) {
        if (effectId != null) {
            tapRestrictionEffectIds.add(effectId);
        }
    }

    /** Removes one expired floating tap restriction from this permanent. */
    public void removeTapRestriction(UUID effectId) {
        if (effectId != null) {
            tapRestrictionEffectIds.remove(effectId);
        }
    }

    /** Whether this permanent is currently prevented from becoming tapped except while attacking. */
    public boolean isTapRestrictedUnlessAttacking() {
        return !tapRestrictionEffectIds.isEmpty();
    }

    /** Sets the permanent's status for an entry replacement without applying untap effects. */
    public void enterUntapped() {
        this.tapped = false;
    }

    public boolean isReturnToHandAtNextUntap() {
        return returnToHandAtNextUntapPlayerId != null;
    }

    public void untap() {
        // Stun counters (CR 122.1d): if a tapped permanent would become untapped,
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
        this.controllerUpkeepsSinceControlled = 0;
    }

    /** Records that its controller's upkeep began, for {@link #controllerUpkeepsSinceControlled}. */
    public void markControllerUpkeepStarted() {
        this.controllerUpkeepsSinceControlled++;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        if (attacking) {
            this.attackedThisTurn = true;
            this.attacksThisTurn++;
            this.attacksThisGame++;
            this.attackedThisCombat = true;
            if (attackTarget != null) {
                playersAttackedThisCombat.add(attackTarget);
            }
        }
    }

    /** Puts this permanent into combat attacking without declaring it as an attacker. */
    public void enterAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public void setAttackTarget(UUID attackTarget) {
        this.attackTarget = attackTarget;
        if (attacking && attackTarget != null) {
            playersAttackedThisCombat.add(attackTarget);
        }
    }

    public void recordCreatureThatCrewedThisTurn(UUID creatureId) {
        if (creatureId != null) {
            creaturesThatCrewedThisTurn.add(creatureId);
        }
    }

    /**
     * Records whether this creature attacked during the turn of {@code endingActivePlayerId}, which is
     * ending. Only the active player's creatures can attack, so the turn's attack flag is that
     * player's record until their next turn ends.
     */
    public void recordTurnEnded(UUID endingActivePlayerId) {
        if (endingActivePlayerId == null) return;
        if (attackedThisTurn) {
            playersDuringWhoseLastTurnThisAttacked.add(endingActivePlayerId);
        } else {
            playersDuringWhoseLastTurnThisAttacked.remove(endingActivePlayerId);
        }
    }

    /** Whether this creature attacked during {@code playerId}'s most recent completed turn. */
    public boolean attackedDuringLastTurnOf(UUID playerId) {
        return playerId != null && playersDuringWhoseLastTurnThisAttacked.contains(playerId);
    }

    /** Remembers the host in {@link #lastAttachedTo} so it survives the detach. */
    public void setAttachedTo(UUID attachedTo) {
        if (attachedTo != null) {
            this.lastAttachedTo = attachedTo;
        }
        this.attachedTo = attachedTo;
    }

    public void addHostedByRealm(UUID realmId) {
        if (realmId != null) {
            hostedByRealmIds.add(realmId);
        }
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
        if (!blockedAttackerIdsThisCombat.contains(permanentId)) {
            blockedAttackerIdsThisCombat.add(permanentId);
        }
    }

    /** Records that the permanent {@code sourcePermanentId} dealt damage to this permanent this turn. */
    public void recordDamageDealtByPermanent(UUID sourcePermanentId) {
        this.permanentIdsThatDealtDamageThisTurn.add(sourcePermanentId);
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
        this.bandId = null;
    }

    /** Finalizes the set of players or permanents this creature attacked during the combat. */
    public void rollOverCombatAttackRecord() {
        playersAttackedLastCombat.clear();
        playersAttackedLastCombat.addAll(playersAttackedThisCombat);
        playersAttackedThisCombat.clear();
        attackTargetReselected = false;
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

    /** Whether this permanent consists of physical components merged by mutate. */
    public boolean isMergedByMutation() {
        return !mutatedComponentCards.isEmpty();
    }

    /** Cards and tokens which move together when this permanent leaves the battlefield. */
    public List<Card> cardsLeavingBattlefield() {
        if (isMergedByMutation()) {
            return List.copyOf(mutatedComponentCards);
        }
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
        if (!blockedThisCombat) {
            blockedAttackerIdsThisCombat.clear();
        }
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

    /** Returns the total number of counters on this permanent, regardless of counter type. */
    public int getTotalCounterCount() {
        return counters.values().stream().mapToInt(Integer::intValue).sum();
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
        if (counterType == CounterType.MANNEQUIN && newCount == 0) {
            mannequinGrantActive = false;
        }
        if (previousCount > 0 && newCount == 0) {
            lastCounterRemovalVersions.merge(counterType, 1L, Long::sum);
        }
        if (newCount < previousCount) {
            countersRemovedSinceTriggerCheck += previousCount - newCount;
        }
        if (counterType == CounterType.LOYALTY && newCount < previousCount) {
            loyaltyCountersRemovedSinceTriggerCheck += previousCount - newCount;
        }
        if (counterType == CounterType.TIME && newCount < previousCount) {
            timeCountersRemovedSinceTriggerCheck += previousCount - newCount;
        }
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE && newCount < previousCount) {
            plusOnePlusOneCountersRemovedSinceTriggerCheck += previousCount - newCount;
        }
        if (count <= 0) {
            counters.remove(counterType);
        } else {
            counters.put(counterType, count);
        }
    }

    public int drainCountersRemovedSinceTriggerCheck() {
        int removed = countersRemovedSinceTriggerCheck;
        countersRemovedSinceTriggerCheck = 0;
        return removed;
    }

    public int drainLoyaltyCountersRemovedSinceTriggerCheck() {
        int removed = loyaltyCountersRemovedSinceTriggerCheck;
        loyaltyCountersRemovedSinceTriggerCheck = 0;
        return removed;
    }

    public int drainPlusOnePlusOneCountersRemovedSinceTriggerCheck() {
        int removed = plusOnePlusOneCountersRemovedSinceTriggerCheck;
        plusOnePlusOneCountersRemovedSinceTriggerCheck = 0;
        return removed;
    }

    public int drainTimeCountersRemovedSinceTriggerCheck() {
        int removed = timeCountersRemovedSinceTriggerCheck;
        timeCountersRemovedSinceTriggerCheck = 0;
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
     * Returns only the modifier portion of power this permanent itself holds (perpetual and counter
     * modifiers), without the base power. Effects that modify power ("gets +2/+0 until end of turn") are
     * floating effects, which the layered pass adds (CR 613.4c). Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getPowerModifiers() {
        return SaturatingMath.clamp((long) persistentPowerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)
                - getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO));
    }

    /**
     * Returns only the modifier portion of toughness this permanent itself holds (counters), without the
     * base toughness. Effects that modify toughness are floating effects, which the layered pass adds. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getToughnessModifiers() {
        return SaturatingMath.clamp((long) getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + 2 * getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)
                - getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO)
                - getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO));
    }

    /**
     * This permanent's power from its own stored state (intrinsic base + perpetual modifier + counters).
     * Effects that set, modify or switch power (CR 613.4b-d) are floating effects the layered pass applies
     * ({@code GameQueryService.getEffectivePower}); this accessor sees none of them. It is the term views add
     * the layered difference to; last-known reads of a departed permanent ask {@code GameQueryService.ownPower}.
     */
    public int getEffectivePower() {
        return SaturatingMath.clamp((long) getBasePower() + persistentPowerModifier
                + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)
                - getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO));
    }

    /** The toughness counterpart of {@link #getEffectivePower()} — same pre-switch caveat. */
    public int getEffectiveToughness() {
        return SaturatingMath.clamp((long) getBaseToughness() + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                + 2 * getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)
                + getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)
                - getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)
                + 2 * getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO)
                - getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)
                - 2 * getCounterCount(CounterType.MINUS_TWO_MINUS_TWO));
    }

    /**
     * The intrinsic base power of this permanent (printed, face-down or awakened), WITHOUT modifiers or
     * counters. Every effect that sets base power (static, one-shot, animation, exchange) is a layer-7b entry the
     * layered pass ({@code LayerSystemService}) orders by timestamp (CR 613.4b, 613.7); this accessor is what
     * applies when no 7b entry does.
     */
    public int getBasePower() {
        if (faceDown) {
            return faceDownPower;
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
            return 8;
        }
        return card.getPower() != null ? card.getPower() : 0;
    }

    /** The base toughness counterpart of {@link #getBasePower()} — same fallback caveats. */
    public int getBaseToughness() {
        if (faceDown) {
            return faceDownToughness;
        }
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
            return 8;
        }
        return card.getToughness() != null ? card.getToughness() : 0;
    }

    public CardColor getEffectiveColor() {
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
            return CardColor.GREEN;
        }
        if (faceDown) {
            return null;
        }
        return card.getColor();
    }

    /**
     * The permanent's full intrinsic color set, the same intrinsic answer as
     * {@link #getEffectiveColor()} (face-down, awakening, card colors) but preserving every color of a multicolored card (a
     * {@code {W/U}} hybrid is both white and blue). Prefer this over {@link #getEffectiveColor()}
     * for color-matters checks (lords, protection, damage prevention). Does not include
     * static-effect grants — {@link com.github.laxika.magicalvibes.service.battlefield.GameQueryService#getEffectiveColors}
     * layers those on top.
     */
    public List<CardColor> getEffectiveColors() {
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
        CounterType keywordCounter = switch (keyword) {
            case HASTE -> CounterType.HASTE;
            case FLYING -> CounterType.FLYING;
            case FIRST_STRIKE -> CounterType.FIRST_STRIKE;
            case DOUBLE_STRIKE -> CounterType.DOUBLE_STRIKE;
            case DEATHTOUCH -> CounterType.DEATHTOUCH;
            case DECAYED -> CounterType.DECAYED;
            case LIFELINK -> CounterType.LIFELINK;
            case REACH -> CounterType.REACH;
            case TRAMPLE -> CounterType.TRAMPLE;
            case VIGILANCE -> CounterType.VIGILANCE;
            case HEXPROOF -> CounterType.HEXPROOF;
            case INDESTRUCTIBLE -> CounterType.INDESTRUCTIBLE;
            case MENACE -> CounterType.MENACE;
            case SHADOW -> CounterType.SHADOW;
            default -> null;
        };
        return (keyword == Keyword.MENACE && suspected)
                || (keyword == Keyword.HASTE && hasAlternativeCostHaste())
                || (!faceDown && card.hasKeyword(keyword))
                || (keywordCounter != null && getCounterCount(keywordCounter) > 0);
    }

    /**
     * Whether its own static ability from the alternative cost it was cast for gives it haste: "As long as this
     * permanent's dash cost was paid, it has haste" (CR 702.109a) or "As long as this permanent's blitz cost was paid,
     * it has haste" (CR 702.152a). The ability is the permanent's own, so it is gone with its abilities (removed in the
     * layered pass like any printed ability) and, for dash, with the dash keyword (a copy effect).
     */
    public boolean hasAlternativeCostHaste() {
        return !faceDown && (isBlitzCostPaid() || isDashCostPaid() && card.hasKeyword(Keyword.DASH));
    }

    public void setRepeatedAdditionalCosts(List<String> repeatedAdditionalCosts) {
        this.repeatedAdditionalCosts = repeatedAdditionalCosts == null || repeatedAdditionalCosts.isEmpty()
                ? List.of() : List.copyOf(repeatedAdditionalCosts);
    }

    public void setTransformed(boolean transformed) {
        if (this.transformed != transformed) {
            transformationSequence++;
        }
        this.transformed = transformed;
    }

    public void recordTappedPermanentForAbility(UUID permanentId) {
        if (permanentId != null && !tappedPermanentsForAbilityThisTurn.contains(permanentId)) {
            tappedPermanentsForAbilityThisTurn.add(permanentId);
        }
    }

    public void clearTappedPermanentsForAbilityThisTurn() {
        tappedPermanentsForAbilityThisTurn.clear();
    }

    public void resetModifiers() {
        this.blockedWithoutBlockers = false;
        this.creaturesThatCrewedThisTurn.clear();
        this.saddled = false;
    }

    /**
     * Captures every "until end of turn" / "this turn" store that {@link #resetModifiers()} or the
     * cleanup step clears (CR 514.2), except the combat-only ones, including the turn-history
     * records (times regenerated, permanents tapped for its abilities, creatures that crewed it).
     * Used to split that state between the players active when it was created under Runed Terror's
     * sequential phases.
     */
    public EndOfTurnStateShare captureEndOfTurnState() {
        Map<EndOfTurnStateField, Object> raw = new EnumMap<>(EndOfTurnStateField.class);
        for (EndOfTurnStateField field : EndOfTurnStateField.values()) {
            raw.put(field, endOfTurnStateValue(field));
        }
        return EndOfTurnStateShare.of(raw);
    }

    /**
     * Adds {@code share} onto this permanent's current "until end of turn" state: flags become true,
     * counts and modifiers are increased, single values are overwritten and collection elements are
     * added. The cleanup step calls this right after resetting the permanent to re-apply the state other
     * players created under Runed Terror's sequential phases.
     */
    public void addEndOfTurnState(EndOfTurnStateShare share) {
        share.values().forEach(this::addEndOfTurnStateValue);
    }

    private Object endOfTurnStateValue(EndOfTurnStateField field) {
        return switch (field) {
            case BLOCKED_WITHOUT_BLOCKERS -> blockedWithoutBlockers;
            case SADDLED -> saddled;
            case TIMES_REGENERATED -> timesRegeneratedThisTurn;
            case TAPPED_PERMANENTS_FOR_ABILITY -> List.copyOf(tappedPermanentsForAbilityThisTurn);
            case CREATURES_THAT_CREWED -> Set.copyOf(creaturesThatCrewedThisTurn);
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addEndOfTurnStateValue(EndOfTurnStateField field, Object value) {
        switch (field) {
            case BLOCKED_WITHOUT_BLOCKERS -> blockedWithoutBlockers = true;
            case SADDLED -> saddled = true;
            case TIMES_REGENERATED -> timesRegeneratedThisTurn += (Integer) value;
            case TAPPED_PERMANENTS_FOR_ABILITY -> ((List<UUID>) value).forEach(this::recordTappedPermanentForAbility);
            case CREATURES_THAT_CREWED -> creaturesThatCrewedThisTurn.addAll((Set<UUID>) value);
            default -> {
            }
        }
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

    public boolean isRoomDoorUnlocked(int doorIndex) {
        return unlockedRoomDoors.contains(doorIndex);
    }

    public void unlockRoomDoor(int doorIndex) {
        if (doorIndex < 0 || doorIndex > 1) {
            throw new IllegalArgumentException("Invalid Room door index: " + doorIndex);
        }
        unlockedRoomDoors.add(doorIndex);
    }

    public void lockRoomDoor(int doorIndex) {
        if (doorIndex < 0 || doorIndex > 1) {
            throw new IllegalArgumentException("Invalid Room door index: " + doorIndex);
        }
        unlockedRoomDoors.remove(doorIndex);
    }

    public boolean isRoomFullyUnlocked() {
        return unlockedRoomDoors.contains(0) && unlockedRoomDoors.contains(1);
    }

    /** The mana of {@code color} spent to cast the spell that produced this permanent ({@link #manaSpentByColorToCast}). */
    public int getManaSpentByColorToCast(ManaColor color) {
        return manaSpentByColorToCast.getOrDefault(color, 0);
    }

    /** Whether the mana spent to cast this permanent's spell was recorded by color ({@link #manaSpentByColorToCast}). */
    public boolean hasManaSpentByColorToCast() {
        return !manaSpentByColorToCast.isEmpty();
    }

    public void setManaSpentByColorToCast(Map<ManaColor, Integer> manaSpentByColor) {
        this.manaSpentByColorToCast = manaSpentByColor == null || manaSpentByColor.isEmpty()
                ? Map.of() : Map.copyOf(manaSpentByColor);
    }

    /** Whether this permanent was cast for an alternative cost (CR 118.9), whichever it was. */
    public boolean isAlternateCost() {
        return alternativeCost != null;
    }

    /**
     * Records that an alternative cost was paid ({@code true}) or that none was; a kind already recorded is kept
     * when {@code alternateCost} is {@code true}.
     */
    public void setAlternateCost(boolean alternateCost) {
        if (!alternateCost) {
            this.alternativeCost = null;
        } else if (this.alternativeCost == null) {
            this.alternativeCost = AlternativeCostKind.OTHER;
        }
    }

    /** Whether this permanent was cast for its dash cost (CR 702.109a), not any other alternative cost. */
    public boolean isDashCostPaid() {
        return alternativeCost == AlternativeCostKind.DASH;
    }

    /** Whether this permanent was cast for a blitz cost (CR 702.152a), not any other alternative cost. */
    public boolean isBlitzCostPaid() {
        return alternativeCost == AlternativeCostKind.BLITZ;
    }
}
