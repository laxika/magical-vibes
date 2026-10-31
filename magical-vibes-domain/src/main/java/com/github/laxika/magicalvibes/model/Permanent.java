package com.github.laxika.magicalvibes.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentEntryCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
public class Permanent {

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
    /** Set when this permanent's backup-ability copy trigger has already fired this turn. */
    @Setter private boolean backupAbilityCopyUsedThisTurn;
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
    /** Gatta and Luzzu-style replacement: on top of the all-damage shield, the counters are put even when the
     *  damage can't be prevented. Reset at turn cleanup. */
    @Setter private boolean damageReplacedByPlusOnePlusOneCounters;
    /** Kill-Suit Cultist-style shield: destroy this creature instead of dealing the next damage to it.
     *  Reset at turn cleanup. */
    @Setter private int damageDestructionShield;
    /** Pyramids-style shield: remove all damage instead of destroying this permanent the next time it
     *  would be destroyed. Reset at turn cleanup. */
    @Setter private int landDestructionShield;
    @Setter private int regenerationShield;
    /** How many of this permanent's {@link #regenerationShield}s carry Soldevi Sentry's rider — when
     *  such a shield is actually used, the controller's opponent may draw a card. Plain shields are
     *  consumed first, so a rider shield is only spent once it is all that is left. Reset at turn
     *  cleanup alongside {@link #regenerationShield}. */
    @Setter private int opponentDrawRegenerationShield;
    /** The opponent chosen for each Soldevi Sentry regeneration shield, in grant order. */
    private final List<UUID> opponentDrawRegenerationShieldRecipients = new ArrayList<>();
    /** The player who activated the ability granting each Soldevi Sentry regeneration shield (the
     *  controller of the resulting draw trigger), aligned with {@link #opponentDrawRegenerationShieldRecipients}. */
    private final List<UUID> opponentDrawRegenerationShieldControllers = new ArrayList<>();
    /** How many of this permanent's {@link #regenerationShield}s carry Matopi Golem's rider — when
     *  such a shield is actually used, put a -1/-1 counter on this permanent. Plain shields are
     *  consumed first. Reset at turn cleanup alongside {@link #regenerationShield}. */
    @Setter private int minusOneCounterRegenerationShield;
    /** How many of this permanent's {@link #regenerationShield}s carry Skeleton Scavengers' rider —
     *  when such a shield is actually used, put a +1/+1 counter on this permanent. Plain shields are
     *  consumed first. Reset at turn cleanup alongside {@link #regenerationShield}. */
    @Setter private int plusOnePlusOneCounterRegenerationShield;
    /** Controllers of the abilities that granted the +1/+1-counter regeneration shields, in grant order. The
     *  rider is that ability's delayed trigger, so its controller is the one who activated it (CR 603.7d).
     *  Cleared at turn cleanup alongside {@link #plusOnePlusOneCounterRegenerationShield}. */
    private final List<UUID> plusOnePlusOneCounterRegenerationShieldControllers = new ArrayList<>();
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
     * Result of the "as this enters, flip a coin" replacement that fixed this permanent's form
     * (Molten Sentry): {@code true} for heads, {@code null} when no such flip happened.
     */
    @Setter private Boolean entryCoinFlipHeads;
    /**
     * The shape Primal Clay chose as it entered, or {@code null} when this permanent made no such choice. The
     * shape's keyword and Wall type are copiable values a copy takes, while its P/T is set by the copy's own choice
     * (CR 707.2, CR 614.1c).
     */
    @Setter private PrimalClayForm primalClayForm;
    /**
     * "As a permanent you control enters, it becomes ..." conversions applied to this permanent as it entered
     * (Displaced Dinosaurs). They are copiable values (CR 707.2), so a copy of this permanent takes them too.
     */
    private final List<ControlledPermanentEntryCharacteristicsEffect> copiableEntryCharacteristics = new ArrayList<>();
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
    @Setter private boolean cantBeBlocked;
    /** True when {@link #cantBeBlocked} comes from a granted "can't be blocked" ability (Ogre Marauder),
     *  which a later loss of all abilities removes. */
    @Setter private boolean cantBeBlockedGrantedAbility;
    @Setter private boolean cantBlockThisTurn;
    /** True when {@link #cantBlockThisTurn} comes from a granted "can't block" ability (Incite Hysteria),
     *  which a later loss of all abilities removes. */
    @Setter private boolean cantBlockGrantedAbilityThisTurn;
    @Setter private boolean cantBlockThisCombat;
    /** True while this permanent has the suspected designation. Cleared only by a leave-and-return
     *  event or an effect that specifically makes it no longer suspected. */
    @Setter private boolean suspected;
    /** True while this permanent represents a commander. */
    @Setter private boolean commander;
    /** Extra creatures this permanent may block this turn beyond the base one, granted by a one-shot
     *  effect (e.g. Act of Heroism). Stacks on top of any static "can block an additional creature"
     *  grants counted in {@code CombatBlockService}. Cleared at end of turn by {@link #resetModifiers()}. */
    @Setter private int additionalBlocksUntilEndOfTurn;
    /** When true, this creature must be declared as a blocker this turn if it can block any attacker
     *  (general "blocks this turn if able", e.g. Nacatl Hunt-Pride). Cleared at end of turn. */
    @Setter private boolean mustBlockThisTurnIfAble;
    /** When true, this creature must block every attacking creature it can legally block this turn.
     *  Cleared at end of turn. */
    @Setter private boolean mustBlockEachAttackingCreatureThisTurnIfAble;
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
    /** Players who paid this Aura's "ignore this effect" cost this turn (Volrath's Curse, Lost in Thought;
     *  {@code IgnoreSourceAuraEffectsUntilEndOfTurnEffect}). The Aura's effects are ignored only for the
     *  player who paid, so a later controller of the enchanted creature is still restricted. Read by
     *  {@code GameQueryService.hasAuraWithEffect}. Cleared at end of turn. */
    private final Set<UUID> auraEffectsIgnoredByPlayersThisTurn = new HashSet<>();
    /** Players who have paid to ignore this Damping Engine's restriction this turn. */
    private final Set<UUID> dampingEngineIgnoredByPlayersThisTurn = new HashSet<>();
    public boolean isDampingEngineEffectIgnoredThisTurn() {
        return !dampingEngineIgnoredByPlayersThisTurn.isEmpty();
    }

    public boolean isDampingEngineEffectIgnoredThisTurn(UUID playerId) {
        return dampingEngineIgnoredByPlayersThisTurn.contains(playerId);
    }

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
    /** Source permanent to associate with this turn's exile replacement, when applicable. */
    @Setter private UUID exileInsteadOfDieSourcePermanentId;
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
    /** If true, this creature has "Whenever this creature deals damage to an opponent, you may return target creature
     *  that player controls to its owner's hand" until end of turn (e.g. Arm with Aether). Cleared at end of turn. */
    @Setter private boolean hasDamageToOpponentCreatureBounce;
    /** Soul Echo: set when the targeted opponent chose that, until this permanent's controller's next
     *  upkeep, each 1 damage that would be dealt to that controller instead removes an echo counter
     *  from this permanent. Survives {@link #resetModifiers()} — the duration ends at the next upkeep,
     *  where upkeep trigger collection clears it before offering the choice again. */
    @Setter private boolean echoDamageRedirectionActive;
    /** Spatial Binding: each player whose next upkeep ends one active "can't phase out"
     *  restriction on this permanent. Multiple activations have independent durations.
     */
    private final Set<UUID> cantPhaseOutUntilUpkeepsOf = new HashSet<>();
    /** Abilities temporarily granted by one-shot effects until end of turn (e.g. Verdant Rebirth
     *  granting an {@code ON_DEATH} ability with ReturnSourceCardFromGraveyardToOwnerHandEffect), mostly
     *  triggered and sometimes static. Cleared every turn by {@link #resetModifiers()}. */
    @Getter(AccessLevel.NONE)
    private final List<Ability> temporaryAbilities = new ArrayList<>();
    /** Abilities granted for the rest of the current combat. Cleared by {@link #clearCombatState()}. */
    @Getter(AccessLevel.NONE)
    private final List<Ability> combatAbilities = new ArrayList<>();
    /** Abilities granted indefinitely by one-shot effects (e.g. Balduvian Shaman granting an
     *  {@code UPKEEP_TRIGGERED} ability with CumulativeUpkeepEffect). Survives {@link #resetModifiers()}. */
    @Getter(AccessLevel.NONE)
    private final List<Ability> persistentAbilities = new ArrayList<>();
    /** "When this creature dies" delayed triggers created by a resolving ability (CR 603.7). Unlike
     *  {@link #persistentAbilities} they are not abilities of the permanent, so losing all
     *  abilities does not stop them. */
    private final List<CardEffect> delayedDeathEffects = new ArrayList<>();
    /** Abilities granted until the next upkeep of the player whose effect created them, keyed by
     *  that player. The expiry player is stored separately from the permanent's current controller
     *  because the duration belongs to the resolving ability's controller. */
    @Getter(AccessLevel.NONE)
    private final Map<UUID, List<Ability>> untilNextUpkeepAbilities = new HashMap<>();
    @Setter private boolean basePowerToughnessOverriddenUntilEndOfTurn;
    @Setter private int basePowerOverride;
    @Setter private int baseToughnessOverride;
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
    @Setter private boolean animatedUntilEndOfTurn;
    /** When {@code true}, this permanent is animated as a creature until the combat phase ends
     *  (e.g. Jade Statue). Uses the same {@link #animatedPower}/{@link #animatedToughness}/
     *  {@link #animatedColor}/{@link #transientSubtypes} storage as {@link #animatedUntilEndOfTurn}.
     *  Cleared by {@link #clearCombatState()} when combat ends. */
    @Setter private boolean animatedUntilEndOfCombat;
    private int animatedPower;
    /** Whether the animation keeps the permanent's own printed power and toughness (Crew on a Vehicle)
     *  rather than fixing them, so a copy effect that changes the card also changes the base values.
     *  Cleared whenever {@link #setAnimatedPower(int)} fixes a value. */
    @Setter private boolean animatedPtFromCard;
    @Setter private int animatedToughness;
    @Setter private CardColor animatedColor;
    @Setter private boolean permanentlyAnimated;
    @Setter private int permanentAnimatedPower;
    @Setter private int permanentAnimatedToughness;
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
    private final Map<CardSupertype, Long> persistentSupertypeChangeTimestamps = new EnumMap<>(CardSupertype.class);
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
    /** Players from whom this permanent has protection until end of turn. */
    private final Set<UUID> protectionFromPlayerIdsUntilEndOfTurn = new HashSet<>();
    /** Players this permanent has protection from until a player's next turn, keyed by that player (Eon Frolicker). */
    private final Map<UUID, Set<UUID>> protectionFromPlayerIdsUntilNextTurnOf = new HashMap<>();
    /** Subtypes for "protection from non-[subtype] creatures" granted until end of turn.
     *  If this set contains HUMAN, the permanent has "protection from non-Human creatures."
     *  Cleared by {@link #resetModifiers()}. */
    private final Set<CardSubtype> protectionFromNonSubtypeCreaturesUntilEndOfTurn = EnumSet.noneOf(CardSubtype.class);
    /** Whether this permanent has protection from creatures controlled by its opponents until end of turn. */
    @Setter private boolean protectionFromOpponentCreaturesUntilEndOfTurn;
    /** Whether all protection abilities have been removed from this permanent until end of turn. */
    @Setter private boolean protectionRemovedUntilEndOfTurn;
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
    /** Source identities this creature must block only during the current combat. */
    private final Set<UUID> mustBlockIdsUntilEndOfCombat = new HashSet<>();
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield (CR 614.6). */
    @Setter private boolean exileIfLeavesBattlefield;
    /** If true, this permanent is exiled instead of being put into a graveyard from the battlefield. */
    @Setter private boolean exileIfDying;
    @Setter private long exileIfDyingTimestamp;
    @Setter private Integer lastKnownAuraCount;
    @Setter private Integer lastKnownEquipmentCount;
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield this turn. */
    @Setter private boolean exileIfLeavesBattlefieldUntilEndOfTurn;
    /** Timestamp of the grant of {@link #exileIfLeavesBattlefieldUntilEndOfTurn}, used to tell whether a later loss of abilities removed it. */
    @Setter private long exileIfLeavesBattlefieldUntilEndOfTurnTimestamp;
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
    /** Land permanent ids that this Cyclopean Tomb put mire counters onto while it was on the battlefield. */
    private final Set<UUID> mireCounterLandIds = new HashSet<>();
    /** Number of untap steps this permanent should skip. Decremented each untap step.
     *  Multiple triggers (e.g. land tapped twice while Vorinclex is out) stack independently.
     *  Used by Vorinclex, Voice of Hunger's opponent-land lock. */
    /** Restricts an exert-style untap skip to the player who chose it. */
    @Setter private UUID skipUntapControllerId;
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
    /** When true, this permanent has lost all abilities until end of turn (e.g. Merfolk Trickster).
     *  Keywords, activated abilities, and triggered abilities are suppressed.
     *  Cleared by {@link #resetModifiers()}. */
    @Setter private boolean losesAllAbilitiesUntilEndOfTurn;
    /** Controllers whose next turn ends an active ability-loss effect on this permanent. */
    private final Set<UUID> losesAllAbilitiesUntilNextTurnControllers = new HashSet<>();
    /** Concrete printed static effect types suppressed on this permanent until end of turn. */
    private final Set<Class<? extends CardEffect>> suppressedStaticEffectsUntilEndOfTurn = new HashSet<>();
    /** Static effect objects removed from this permanent until end of turn; an effect it gains later is a
     *  different object and is not affected (CR 611.2c). Compared by identity. */
    private final Set<CardEffect> suppressedStaticEffectInstancesUntilEndOfTurn =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
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
    /** Whether this permanent was cast for its Warp cost. */
    @Setter private boolean castWithWarp;
    /** Whether this permanent was cast for its madness cost. */
    @Setter private boolean madness;
    /** Whether this permanent was cast by paying an alternate cost. */
    @Setter private boolean alternateCost;
    /** Effective toughness of the permanent sacrificed to pay this permanent's alternate cost. */
    @Setter private int alternateCostSacrificedToughness;
    /** Mana value of the creature returned to pay this permanent's web-slinging cost, when applicable. */
    @Setter private Integer webSlingingReturnedCreatureManaValue;
    /** Whether this permanent was cast for its spectacle cost. */
    @Setter private boolean spectacle;
    /** Whether this permanent's optional collect-evidence additional cost was paid. */
    @Setter private boolean collectEvidenceCostPaid;
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
    /** Physical components of a creature merged by mutate, ordered from top to bottom. */
    private final List<Card> mutatedComponentCards = new ArrayList<>();
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
    /** Printed activated abilities this permanent has lost ("then this creature loses this ability",
     *  Great Fang Chroniclers), held by identity. Only the removed ability stays gone: if the permanent
     *  becomes a copy of something else or changes face, the new card's abilities are other objects and
     *  remain available, and the loss applies again if the permanent shows the original card once more.
     *  Card instances (and so their abilities) are shared with AI simulation copies, so identity
     *  survives the copy constructor. Never cleared by {@link #resetModifiers()}. */
    @Getter(AccessLevel.NONE)
    private final java.util.Set<ActivatedAbility> lostPrintedActivatedAbilities =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    /** When true, this permanent is a temporary copy (until end of turn) of another creature.
     *  At the cleanup step, the copy's floating layer-1 effect expires and the card reverts to
     *  {@link #preCopyCard} via {@link #revertEndOfTurnCopy()}.
     *  Used by Tilonalli's Skinshifter and similar shapeshifters. */
    @Setter private boolean copyUntilEndOfTurn;
    /** The card to revert to when the temporary copy effect ends.
     *  Only non-null when {@link #copyUntilEndOfTurn} is true. */
    @Setter private Card preCopyCard;
    /** Keywords on {@link #card} that a temporary copy effect granted separately from the copy itself
     *  ("becomes a copy ... and gains haste"), so they are not copiable values of this permanent.
     *  Cleared with the temporary copy by {@link #revertEndOfTurnCopy()}. */
    private final Set<Keyword> nonCopiableKeywords = EnumSet.noneOf(Keyword.class);
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
    /** The player whose effect granted the current "until your next turn" animation, or null when the
     *  animation is not tied to a grantor. A grantor's animation ends at the grantor's next turn rather
     *  than at the permanent's controller's next turn (Karn, the Great Creator's +1 on an opponent's artifact). */
    @Setter private UUID untilNextTurnAnimationGrantorId;
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
    /** The copies currently applied by Shapesharer-style "until your next turn" effects, oldest first.
     *  The newest one overrides the earlier ones (CR 613.7); when it expires first, the previous
     *  still-active copy applies again (CR 613.6). */
    private final List<UntilNextTurnCopyLayer> untilNextTurnCopyLayers = new ArrayList<>();

    /** A copy applied by an effect that lasts until {@code controllerId}'s next turn. */
    public record UntilNextTurnCopyLayer(UUID controllerId, Card copyCard) {}

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
        this.cantAttackNextTurn = source.cantAttackNextTurn;
        this.cantAttackThisTurn = source.cantAttackThisTurn;
        this.blocking = source.blocking;
        this.blockedThisTurn = source.blockedThisTurn;
        this.blockedThisCombat = source.blockedThisCombat;
        this.blockedOrWasBlockedSinceLastUpkeep = source.blockedOrWasBlockedSinceLastUpkeep;
        this.attackedOrBlockedSinceLastUpkeep = source.attackedOrBlockedSinceLastUpkeep;
        this.upkeepsStartedSinceAttackOrBlock.putAll(source.upkeepsStartedSinceAttackOrBlock);
        this.becomeTargetCounterUsedThisTurn = source.becomeTargetCounterUsedThisTurn;
        this.backupAbilityCopyUsedThisTurn = source.backupAbilityCopyUsedThisTurn;
        this.blockingTargets.addAll(source.blockingTargets);
        this.blockingTargetIds.addAll(source.blockingTargetIds);
        this.blockedAttackerIdsThisCombat.addAll(source.blockedAttackerIdsThisCombat);
        this.permanentIdsThatDealtDamageThisTurn.addAll(source.permanentIdsThatDealtDamageThisTurn);
        this.bandId = source.bandId;
        this.summoningSick = source.summoningSick;
        this.enteredBattlefieldTurnNumber = source.enteredBattlefieldTurnNumber;
        this.persistentPowerModifier = source.persistentPowerModifier;
        this.classLevel = source.classLevel;
        this.powerModifier = source.powerModifier;
        this.toughnessModifier = source.toughnessModifier;
        this.damagePreventionShield = source.damagePreventionShield;
        this.damageToCounterPreventionShield = source.damageToCounterPreventionShield;
        this.damageToPlusOnePlusOneCounterPreventionShield = source.damageToPlusOnePlusOneCounterPreventionShield;
        this.allDamageToPlusOnePlusOneCounterPreventionShield = source.allDamageToPlusOnePlusOneCounterPreventionShield;
        this.damageReplacedByPlusOnePlusOneCounters = source.damageReplacedByPlusOnePlusOneCounters;
        this.damageDestructionShield = source.damageDestructionShield;
        this.landDestructionShield = source.landDestructionShield;
        this.regenerationShield = source.regenerationShield;
        this.opponentDrawRegenerationShield = source.opponentDrawRegenerationShield;
        this.opponentDrawRegenerationShieldRecipients.addAll(
                source.opponentDrawRegenerationShieldRecipients);
        this.opponentDrawRegenerationShieldControllers.addAll(
                source.opponentDrawRegenerationShieldControllers);
        this.minusOneCounterRegenerationShield = source.minusOneCounterRegenerationShield;
        this.plusOnePlusOneCounterRegenerationShield = source.plusOnePlusOneCounterRegenerationShield;
        this.plusOnePlusOneCounterRegenerationShieldControllers.addAll(
                source.plusOnePlusOneCounterRegenerationShieldControllers);
        this.gainControlRegenerationShields.addAll(source.gainControlRegenerationShields);
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
        this.entryCoinFlipHeads = source.entryCoinFlipHeads;
        this.primalClayForm = source.primalClayForm;
        this.copiableEntryCharacteristics.addAll(source.copiableEntryCharacteristics);
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
        this.cantBeBlocked = source.cantBeBlocked;
        this.cantBeBlockedGrantedAbility = source.cantBeBlockedGrantedAbility;
        this.cantBlockThisTurn = source.cantBlockThisTurn;
        this.cantBlockGrantedAbilityThisTurn = source.cantBlockGrantedAbilityThisTurn;
        this.cantBlockThisCombat = source.cantBlockThisCombat;
        this.suspected = source.suspected;
        this.commander = source.commander;
        this.additionalBlocksUntilEndOfTurn = source.additionalBlocksUntilEndOfTurn;
        this.mustBlockThisTurnIfAble = source.mustBlockThisTurnIfAble;
        this.mustBlockEachAttackingCreatureThisTurnIfAble = source.mustBlockEachAttackingCreatureThisTurnIfAble;
        this.mustAttackThisCombat = source.mustAttackThisCombat;
        this.mustAttackThisTurn = source.mustAttackThisTurn;
        this.mustAttackTargetId = source.mustAttackTargetId;
        this.mustBeBlockedThisTurn = source.mustBeBlockedThisTurn;
        this.mustBeBlockedByAllThisTurn = source.mustBeBlockedByAllThisTurn;
        this.blockedWithoutBlockers = source.blockedWithoutBlockers;
        this.dampingEngineIgnoredByPlayersThisTurn.addAll(source.dampingEngineIgnoredByPlayersThisTurn);
        this.cantRegenerateThisTurn = source.cantRegenerateThisTurn;
        this.damageCantBePreventedOrRedirectedThisTurn = source.damageCantBePreventedOrRedirectedThisTurn;
        this.damagedCreaturesCantRegenerateThisTurn = source.damagedCreaturesCantRegenerateThisTurn;
        this.exileDamagedCreaturesInsteadOfDyingThisTurn = source.exileDamagedCreaturesInsteadOfDyingThisTurn;
        this.exileInsteadOfDieThisTurn = source.exileInsteadOfDieThisTurn;
        this.exileInsteadOfDieSourcePermanentId = source.exileInsteadOfDieSourcePermanentId;
        this.prepared = source.prepared;
        this.phasedOutIndirectly = source.phasedOutIndirectly;
        this.preparedSpellCardId = source.preparedSpellCardId;
        this.hasDamageToOpponentCreatureBounce = source.hasDamageToOpponentCreatureBounce;
        this.echoDamageRedirectionActive = source.echoDamageRedirectionActive;
        this.cantPhaseOutUntilUpkeepsOf.addAll(source.cantPhaseOutUntilUpkeepsOf);
        this.temporaryAbilities.addAll(source.temporaryAbilities);
        this.combatAbilities.addAll(source.combatAbilities);
        this.persistentAbilities.addAll(source.persistentAbilities);
        this.delayedDeathEffects.addAll(source.delayedDeathEffects);
        source.untilNextUpkeepAbilities.forEach((playerId, abilities) ->
                this.untilNextUpkeepAbilities.put(playerId, new ArrayList<>(abilities)));
        this.sacrificeAtNextCleanup = source.sacrificeAtNextCleanup;
        this.returnToHandAtNextCleanup = source.returnToHandAtNextCleanup;
        this.returnToHandAtNextUntapPlayerId = source.returnToHandAtNextUntapPlayerId;
        this.basePowerToughnessOverriddenUntilEndOfTurn = source.basePowerToughnessOverriddenUntilEndOfTurn;
        this.basePowerOverride = source.basePowerOverride;
        this.baseToughnessOverride = source.baseToughnessOverride;
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
        this.animatedUntilEndOfTurn = source.animatedUntilEndOfTurn;
        this.animatedUntilEndOfCombat = source.animatedUntilEndOfCombat;
        this.animatedPower = source.animatedPower;
        this.animatedPtFromCard = source.animatedPtFromCard;
        this.animatedToughness = source.animatedToughness;
        this.animatedColor = source.animatedColor;
        this.permanentlyAnimated = source.permanentlyAnimated;
        this.permanentAnimatedPower = source.permanentAnimatedPower;
        this.permanentAnimatedToughness = source.permanentAnimatedToughness;
        this.untapAfterTapCost = source.untapAfterTapCost;
        this.counters.putAll(source.counters);
        this.lastCounterRemovalVersions.putAll(source.lastCounterRemovalVersions);
        this.countersRemovedSinceTriggerCheck = source.countersRemovedSinceTriggerCheck;
        this.loyaltyCountersRemovedSinceTriggerCheck = source.loyaltyCountersRemovedSinceTriggerCheck;
        this.timeCountersRemovedSinceTriggerCheck = source.timeCountersRemovedSinceTriggerCheck;
        this.plusOnePlusOneCountersRemovedSinceTriggerCheck = source.plusOnePlusOneCountersRemovedSinceTriggerCheck;
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
        this.persistentSupertypeChangeTimestamps.putAll(source.persistentSupertypeChangeTimestamps);
        this.persistentName = source.persistentName;
        this.textReplacements.addAll(source.textReplacements);
        this.protectionFromCardTypes.addAll(source.protectionFromCardTypes);
        this.protectionFromColorsUntilEndOfTurn.addAll(source.protectionFromColorsUntilEndOfTurn);
        this.protectionFromColorlessUntilEndOfTurn = source.protectionFromColorlessUntilEndOfTurn;
        this.protectionFromOpponentsPermanently = source.protectionFromOpponentsPermanently;
        this.protectionFromPlayerIdsPermanently.addAll(source.protectionFromPlayerIdsPermanently);
        this.protectionFromPlayerIdsUntilEndOfTurn.addAll(source.protectionFromPlayerIdsUntilEndOfTurn);
        source.protectionFromPlayerIdsUntilNextTurnOf.forEach((untilTurnOfPlayerId, playerIds) ->
                this.protectionFromPlayerIdsUntilNextTurnOf.put(untilTurnOfPlayerId, new HashSet<>(playerIds)));
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.addAll(source.protectionFromNonSubtypeCreaturesUntilEndOfTurn);
        this.protectionFromOpponentCreaturesUntilEndOfTurn = source.protectionFromOpponentCreaturesUntilEndOfTurn;
        this.protectionRemovedUntilEndOfTurn = source.protectionRemovedUntilEndOfTurn;
        this.blockRestrictionsUntilEndOfTurn.addAll(source.blockRestrictionsUntilEndOfTurn);
        this.unblockableIfDefenderControlsUntilEndOfTurn.addAll(source.unblockableIfDefenderControlsUntilEndOfTurn);
        this.exileIfLeavesBattlefield = source.exileIfLeavesBattlefield;
        this.exileIfDying = source.exileIfDying;
        this.exileIfDyingTimestamp = source.exileIfDyingTimestamp;
        this.lastKnownAuraCount = source.lastKnownAuraCount;
        this.lastKnownEquipmentCount = source.lastKnownEquipmentCount;
        this.exileIfLeavesBattlefieldUntilEndOfTurn = source.exileIfLeavesBattlefieldUntilEndOfTurn;
        this.exileIfLeavesBattlefieldUntilEndOfTurnTimestamp = source.exileIfLeavesBattlefieldUntilEndOfTurnTimestamp;
        this.enteredViaUnearth = source.enteredViaUnearth;
        this.shroudIgnoredByPlayersUntilEndOfTurn.addAll(source.shroudIgnoredByPlayersUntilEndOfTurn);
        this.cantBlockIds.addAll(source.cantBlockIds);
        this.mustBlockIds.addAll(source.mustBlockIds);
        this.mustBlockIdsUntilEndOfCombat.addAll(source.mustBlockIdsUntilEndOfCombat);
        this.untapPreventedByPermanentIds.addAll(source.untapPreventedByPermanentIds);
        this.untapPreventedWhileSourceOnBattlefieldIds.addAll(source.untapPreventedWhileSourceOnBattlefieldIds);
        this.mireCounterLandIds.addAll(source.mireCounterLandIds);
        this.skipUntapCount = source.skipUntapCount;
        this.skipUntapControllerId = source.skipUntapControllerId;
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
        this.transformationSequence = source.transformationSequence;
        this.lastKnownToughness = source.lastKnownToughness;
        this.lastKnownPower = source.lastKnownPower;
        this.lastKnownColors = source.lastKnownColors == null ? null : Set.copyOf(source.lastKnownColors);
        this.lastKnownSubtypes = source.lastKnownSubtypes == null ? null : Set.copyOf(source.lastKnownSubtypes);
        this.lastKnownSupertypes = source.lastKnownSupertypes == null ? null : Set.copyOf(source.lastKnownSupertypes);
        this.losesAllAbilitiesUntilEndOfTurn = source.losesAllAbilitiesUntilEndOfTurn;
        this.losesAllAbilitiesUntilNextTurnControllers.addAll(
                source.losesAllAbilitiesUntilNextTurnControllers);
        this.suppressedStaticEffectsUntilEndOfTurn.addAll(source.suppressedStaticEffectsUntilEndOfTurn);
        this.suppressedStaticEffectInstancesUntilEndOfTurn.addAll(source.suppressedStaticEffectInstancesUntilEndOfTurn);
        this.losesAllAbilitiesPermanently = source.losesAllAbilitiesPermanently;
        this.losesAllCreatureTypesUntilEndOfTurn = source.losesAllCreatureTypesUntilEndOfTurn;
        this.transientRemovedSubtypes.addAll(source.transientRemovedSubtypes);
        this.kicked = source.kicked;
        this.evoked = source.evoked;
        this.escaped = source.escaped;
        this.prowl = source.prowl;
        this.castWithWarp = source.castWithWarp;
        this.madness = source.madness;
        this.alternateCost = source.alternateCost;
        this.alternateCostSacrificedToughness = source.alternateCostSacrificedToughness;
        this.webSlingingReturnedCreatureManaValue = source.webSlingingReturnedCreatureManaValue;
        this.spectacle = source.spectacle;
        this.collectEvidenceCostPaid = source.collectEvidenceCostPaid;
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
        this.temporaryActivatedAbilities.addAll(source.temporaryActivatedAbilities);
        this.adaptOverridesUntilEndOfTurn = source.adaptOverridesUntilEndOfTurn;
        this.persistentGrantedActivatedAbilities.addAll(source.persistentGrantedActivatedAbilities);
        this.lostPrintedActivatedAbilities.addAll(source.lostPrintedActivatedAbilities);
        this.copyUntilEndOfTurn = source.copyUntilEndOfTurn;
        this.preCopyCard = source.preCopyCard;
        this.nonCopiableKeywords.addAll(source.nonCopiableKeywords);
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
        this.untilNextTurnCopyLayers.addAll(source.untilNextTurnCopyLayers);
        this.timestamp = source.timestamp;
    }

    /** Records that this permanent has lost the given printed activated ability. */
    public void loseActivatedAbility(ActivatedAbility ability) {
        lostPrintedActivatedAbilities.add(ability);
    }

    /** Whether this permanent has lost exactly this ability object ("loses this ability"). */
    public boolean hasLostActivatedAbility(ActivatedAbility ability) {
        return lostPrintedActivatedAbilities.contains(ability);
    }

    /** Whether any printed activated ability of the card this permanent currently shows has been lost. */
    public boolean hasLostAnyCurrentActivatedAbility() {
        return !lostPrintedActivatedAbilities.isEmpty()
                && card.getActivatedAbilities().stream().anyMatch(lostPrintedActivatedAbilities::contains);
    }

    public Card getOriginalCard() {
        return originalCard;
    }

    public void recordMutation() {
        timesMutated++;
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
        this.combatAbilities.clear();
        this.mustBlockIdsUntilEndOfCombat.clear();
        this.bandId = null;
        this.cantBlockThisCombat = false;
        if (this.mustAttackThisCombat && !this.mustAttackThisTurn) {
            // A combat-long attack requirement's chosen target ends with that combat.
            this.mustAttackTargetId = null;
        }
        this.mustAttackThisCombat = false;
    }

    /** Sources this creature must block under either turn-long or combat-long requirements. */
    public Set<UUID> getRequiredBlockSourceIds() {
        Set<UUID> sources = new HashSet<>(mustBlockIds);
        sources.addAll(mustBlockIdsUntilEndOfCombat);
        return sources;
    }

    /** Finalizes the set of players or permanents this creature attacked during the combat. */
    public void rollOverCombatAttackRecord() {
        playersAttackedLastCombat.clear();
        playersAttackedLastCombat.addAll(playersAttackedThisCombat);
        playersAttackedThisCombat.clear();
        attackTargetReselected = false;
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
     * Returns only the modifier portion of power (perpetual, counter, and temporary modifiers),
     * without the base power. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getPowerModifiers() {
        return SaturatingMath.clamp((long) persistentPowerModifier + powerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
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
     * Returns only the modifier portion of toughness (counters + temporary modifiers),
     * without the base toughness. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getToughnessModifiers() {
        return SaturatingMath.clamp((long) toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
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
     * This permanent's power from its own stored state (base + modifiers + counters), WITHOUT
     * layer-7d switches: P/T switching lives in floating continuous effects applied by the
     * layered pass ({@code GameQueryService.getEffectivePower} swaps the finished values per
     * CR 613.4d). This accessor is the legacy pre-switch fallback for direct {@code Permanent}
     * readers (views' raw term, last-known-information reads, predicate leaves).
     */
    public int getEffectivePower() {
        return SaturatingMath.clamp((long) getBasePower() + persistentPowerModifier + powerModifier
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
        return SaturatingMath.clamp((long) getBaseToughness() + toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
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
     * The base power of this permanent from its own stored state, WITHOUT modifiers or counters.
     * This is NOT the CR 613 layer-7b precedence decision — that lives in the layered pass
     * ({@code LayerSystemService}), which orders every base-P/T setter (static, one-shot,
     * animation, exchange) by timestamp and surfaces the winner through
     * {@code GameQueryService.getEffectivePower}. This accessor is the legacy fallback used by
     * direct {@code Permanent} readers (views, last-known-information reads, predicate leaves)
     * when no layered 7b entry applies; when only one of these fields is set, the two agree.
     */
    public void setAnimatedPower(int animatedPower) {
        this.animatedPower = animatedPower;
        this.animatedPtFromCard = false;
    }

    public int getBasePower() {
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            return basePowerOverride;
        }
        if (basePowerOverriddenPermanently) {
            return permanentBasePowerOverride;
        }
        if (animatedUntilEndOfTurn || animatedUntilEndOfCombat) {
            return animatedPtFromCard ? (card.getPower() != null ? card.getPower() : 0) : animatedPower;
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
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
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
            return animatedPtFromCard ? (card.getToughness() != null ? card.getToughness() : 0) : animatedToughness;
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
        if (!faceDown && getCounterCount(CounterType.AWAKENING) > 0) {
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
        if (removedKeywords.contains(keyword)) return false;
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
        addTemporaryAbility(LegacySlots.ability(slot, effect));
    }

    /** Grants {@code ability} until end of turn. */
    public void addTemporaryAbility(Ability ability) {
        temporaryAbilities.add(ability);
    }

    /** Drops every temporarily granted ability; used when a later-timestamp effect removes all abilities. */
    public void clearTemporaryTriggeredEffects() {
        temporaryAbilities.clear();
    }

    /** The effects in {@code slot} of the abilities granted until end of turn, end of combat or next upkeep. */
    public List<CardEffect> getTemporaryTriggeredEffects(EffectSlot slot) {
        List<CardEffect> effects = LegacySlots.effectsIn(temporaryAbilities, slot);
        List<CardEffect> untilEndOfCombat = LegacySlots.effectsIn(combatAbilities, slot);
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

    /**
     * The effects of the static abilities granted until end of turn, end of combat or next upkeep, in
     * that order.
     */
    public List<CardEffect> getTemporaryStaticEffects() {
        List<CardEffect> effects = LegacySlots.staticEffectsIn(temporaryAbilities);
        List<CardEffect> untilEndOfCombat = LegacySlots.staticEffectsIn(combatAbilities);
        List<CardEffect> untilNextUpkeep = new ArrayList<>();
        untilNextUpkeepAbilities.values().forEach(abilities -> untilNextUpkeep.addAll(LegacySlots.staticEffectsIn(abilities)));
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
        addCombatAbility(LegacySlots.ability(slot, effect));
    }

    /** Grants {@code ability} until end of combat. */
    public void addCombatAbility(Ability ability) {
        combatAbilities.add(ability);
    }

    public void setTransformed(boolean transformed) {
        if (this.transformed != transformed) {
            transformationSequence++;
        }
        this.transformed = transformed;
    }

    public void addPersistentTriggeredEffect(EffectSlot slot, CardEffect effect) {
        addPersistentAbility(LegacySlots.ability(slot, effect));
    }

    /** Grants {@code ability} indefinitely. */
    public void addPersistentAbility(Ability ability) {
        persistentAbilities.add(ability);
    }

    /** Removes the indefinitely granted abilities in {@code slot} whose effect matches {@code filter}. */
    public void removePersistentTriggeredEffectsIf(EffectSlot slot, Predicate<CardEffect> filter) {
        persistentAbilities.removeIf(ability -> LegacySlots.slotOf(ability) == slot
                && LegacySlots.effectsOf(ability).stream().anyMatch(filter));
    }

    public void addDelayedDeathEffect(CardEffect effect) {
        delayedDeathEffects.add(effect);
    }

    public List<CardEffect> getDelayedDeathEffects() {
        return List.copyOf(delayedDeathEffects);
    }

    /** The effects in {@code slot} of the abilities granted indefinitely. */
    public List<CardEffect> getPersistentTriggeredEffects(EffectSlot slot) {
        return LegacySlots.effectsIn(persistentAbilities, slot);
    }

    /** The effects of the static abilities granted indefinitely. */
    public List<CardEffect> getPersistentStaticEffects() {
        return LegacySlots.staticEffectsIn(persistentAbilities);
    }

    /** Removes the indefinitely granted static abilities whose effect matches {@code filter}. */
    public void removePersistentStaticEffectsIf(Predicate<CardEffect> filter) {
        persistentAbilities.removeIf(ability -> ability instanceof StaticAbility statik
                && statik.effects().stream().anyMatch(filter));
    }

    public void addUntilNextUpkeepTriggeredEffect(UUID expiryPlayerId, EffectSlot slot, CardEffect effect) {
        addUntilNextUpkeepAbility(expiryPlayerId, LegacySlots.ability(slot, effect));
    }

    /** Grants {@code ability} until the next upkeep of {@code expiryPlayerId}. */
    public void addUntilNextUpkeepAbility(UUID expiryPlayerId, Ability ability) {
        untilNextUpkeepAbilities
                .computeIfAbsent(expiryPlayerId, ignored -> new ArrayList<>())
                .add(ability);
    }

    private List<CardEffect> getUntilNextUpkeepTriggeredEffects(EffectSlot slot) {
        if (untilNextUpkeepAbilities.isEmpty()) {
            return List.of();
        }
        List<CardEffect> effects = new ArrayList<>();
        untilNextUpkeepAbilities.values().forEach(abilities -> effects.addAll(LegacySlots.effectsIn(abilities, slot)));
        return effects;
    }

    public void clearUntilNextUpkeepTriggeredEffects(UUID expiryPlayerId) {
        untilNextUpkeepAbilities.remove(expiryPlayerId);
    }

    public void preventPhaseOutUntilUpkeepOf(UUID playerId) {
        cantPhaseOutUntilUpkeepsOf.add(playerId);
    }

    public void expirePhaseOutPreventionAtUpkeepOf(UUID playerId) {
        cantPhaseOutUntilUpkeepsOf.remove(playerId);
    }

    public boolean canPhaseOut() {
        return cantPhaseOutUntilUpkeepsOf.isEmpty();
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

    /** Removes exactly this static effect object from the permanent until end of turn. */
    public void suppressStaticEffectInstanceUntilEndOfTurn(CardEffect effect) {
        suppressedStaticEffectInstancesUntilEndOfTurn.add(effect);
    }

    /** Whether this static effect is suppressed, either as its whole type or as this exact object. */
    public boolean isStaticEffectSuppressed(CardEffect effect) {
        return suppressedStaticEffectsUntilEndOfTurn.contains(effect.getClass())
                || suppressedStaticEffectInstancesUntilEndOfTurn.contains(effect);
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
        this.cantBeBlockedGrantedAbility = false;
        this.cantAttackThisTurn = false;
        this.cantBlockThisTurn = false;
        this.cantBlockGrantedAbilityThisTurn = false;
        this.additionalBlocksUntilEndOfTurn = 0;
        this.mustBlockThisTurnIfAble = false;
        this.mustBlockEachAttackingCreatureThisTurnIfAble = false;
        this.mustAttackThisCombat = false;
        this.mustAttackThisTurn = false;
        this.mustAttackTargetId = null;
        this.mustBeBlockedThisTurn = false;
        this.mustBeBlockedByAllThisTurn = false;
        this.blockedWithoutBlockers = false;
        this.creaturesThatCrewedThisTurn.clear();
        this.auraEffectsIgnoredByPlayersThisTurn.clear();
        this.dampingEngineIgnoredByPlayersThisTurn.clear();
        this.cantRegenerateThisTurn = false;
        this.damageCantBePreventedOrRedirectedThisTurn = false;
        this.damagedCreaturesCantRegenerateThisTurn = false;
        this.exileDamagedCreaturesInsteadOfDyingThisTurn = false;
        this.exileInsteadOfDieThisTurn = false;
        this.exileInsteadOfDieSourcePermanentId = null;
        this.hasDamageToOpponentCreatureBounce = false;
        this.temporaryAbilities.clear();
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
        this.protectionFromPlayerIdsUntilEndOfTurn.clear();
        this.protectionFromColorlessUntilEndOfTurn = false;
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.clear();
        this.protectionFromOpponentCreaturesUntilEndOfTurn = false;
        this.protectionRemovedUntilEndOfTurn = false;
        this.blockRestrictionsUntilEndOfTurn.clear();
        this.unblockableIfDefenderControlsUntilEndOfTurn.clear();
        this.exileIfLeavesBattlefieldUntilEndOfTurn = false;
        this.shroudIgnoredByPlayersUntilEndOfTurn.clear();
        this.cantBlockIds.clear();
        this.mustBlockIds.clear();
        this.mustBlockIdsUntilEndOfCombat.clear();
        this.losesAllAbilitiesUntilEndOfTurn = false;
        this.suppressedStaticEffectsUntilEndOfTurn.clear();
        this.suppressedStaticEffectInstancesUntilEndOfTurn.clear();
        this.losesAllCreatureTypesUntilEndOfTurn = false;
        this.transientRemovedSubtypes.clear();
        this.temporaryActivatedAbilities.clear();
        this.adaptOverridesUntilEndOfTurn = 0;
        expireTemporaryTextReplacements();
    }

    /**
     * Captures every "until end of turn" / "this turn" store that {@link #resetModifiers()} or the
     * cleanup step's shield reset clears (CR 514.2), except the combat-only ones, including the turn-history
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
            case POWER_MODIFIER -> powerModifier;
            case TOUGHNESS_MODIFIER -> toughnessModifier;
            case ADDITIONAL_BLOCKS -> additionalBlocksUntilEndOfTurn;
            case BASE_POWER_TOUGHNESS_OVERRIDDEN -> basePowerToughnessOverriddenUntilEndOfTurn;
            case BASE_POWER_OVERRIDE -> basePowerOverride;
            case BASE_TOUGHNESS_OVERRIDE -> baseToughnessOverride;
            case CANT_BE_BLOCKED -> cantBeBlocked;
            case CANT_BE_BLOCKED_GRANTED_ABILITY -> cantBeBlockedGrantedAbility;
            case CANT_ATTACK_THIS_TURN -> cantAttackThisTurn;
            case CANT_BLOCK_THIS_TURN -> cantBlockThisTurn;
            case CANT_BLOCK_GRANTED_ABILITY_THIS_TURN -> cantBlockGrantedAbilityThisTurn;
            case MUST_BLOCK_THIS_TURN_IF_ABLE -> mustBlockThisTurnIfAble;
            case MUST_BLOCK_EACH_ATTACKING_CREATURE_THIS_TURN_IF_ABLE -> mustBlockEachAttackingCreatureThisTurnIfAble;
            case MUST_ATTACK_THIS_TURN -> mustAttackThisTurn;
            case MUST_ATTACK_TARGET_ID -> mustAttackTargetId;
            case MUST_BE_BLOCKED_THIS_TURN -> mustBeBlockedThisTurn;
            case MUST_BE_BLOCKED_BY_ALL_THIS_TURN -> mustBeBlockedByAllThisTurn;
            case BLOCKED_WITHOUT_BLOCKERS -> blockedWithoutBlockers;
            case AURA_EFFECTS_IGNORED_BY_PLAYERS -> auraEffectsIgnoredByPlayersThisTurn;
            case DAMPING_ENGINE_IGNORED_BY_PLAYERS -> dampingEngineIgnoredByPlayersThisTurn;
            case CANT_REGENERATE_THIS_TURN -> cantRegenerateThisTurn;
            case DAMAGE_CANT_BE_PREVENTED_OR_REDIRECTED -> damageCantBePreventedOrRedirectedThisTurn;
            case DAMAGED_CREATURES_CANT_REGENERATE -> damagedCreaturesCantRegenerateThisTurn;
            case EXILE_DAMAGED_CREATURES_INSTEAD_OF_DYING -> exileDamagedCreaturesInsteadOfDyingThisTurn;
            case EXILE_INSTEAD_OF_DIE -> exileInsteadOfDieThisTurn;
            case EXILE_INSTEAD_OF_DIE_SOURCE_PERMANENT_ID -> exileInsteadOfDieSourcePermanentId;
            case DAMAGE_TO_OPPONENT_CREATURE_BOUNCE -> hasDamageToOpponentCreatureBounce;
            case TEMPORARY_ABILITIES -> temporaryAbilities;
            case SADDLED -> saddled;
            case ANIMATED_UNTIL_END_OF_TURN -> animatedUntilEndOfTurn;
            case ANIMATED_POWER -> animatedPower;
            case ANIMATED_TOUGHNESS -> animatedToughness;
            case ANIMATED_COLOR -> animatedColor;
            case GRANTED_KEYWORDS -> grantedKeywords;
            case REMOVED_KEYWORDS -> removedKeywords;
            case TRANSIENT_COLORS -> transientColors;
            case COLOR_OVERRIDDEN -> colorOverridden;
            case TRANSIENT_SUBTYPES -> transientSubtypes;
            case TRANSIENT_LAND_TYPE_OVERRIDE -> transientLandTypeOverride;
            case TRANSIENT_CREATURE_TYPE_OVERRIDE -> transientCreatureTypeOverride;
            case TRANSIENT_CREATURE_TYPE_OVERRIDES -> transientCreatureTypeOverrides;
            case GRANTED_CARD_TYPES -> grantedCardTypes;
            case PROTECTION_FROM_CARD_TYPES -> protectionFromCardTypes;
            case PROTECTION_FROM_COLORS -> protectionFromColorsUntilEndOfTurn;
            case PROTECTION_FROM_PLAYERS -> protectionFromPlayerIdsUntilEndOfTurn;
            case PROTECTION_FROM_COLORLESS -> protectionFromColorlessUntilEndOfTurn;
            case PROTECTION_FROM_NON_SUBTYPE_CREATURES -> protectionFromNonSubtypeCreaturesUntilEndOfTurn;
            case PROTECTION_FROM_OPPONENT_CREATURES -> protectionFromOpponentCreaturesUntilEndOfTurn;
            case PROTECTION_REMOVED -> protectionRemovedUntilEndOfTurn;
            case BLOCK_RESTRICTIONS -> blockRestrictionsUntilEndOfTurn;
            case UNBLOCKABLE_IF_DEFENDER_CONTROLS -> unblockableIfDefenderControlsUntilEndOfTurn;
            case EXILE_IF_LEAVES_BATTLEFIELD -> exileIfLeavesBattlefieldUntilEndOfTurn;
            case SHROUD_IGNORED_BY_PLAYERS -> shroudIgnoredByPlayersUntilEndOfTurn;
            case CANT_BLOCK_IDS -> cantBlockIds;
            case MUST_BLOCK_IDS -> mustBlockIds;
            case LOSES_ALL_ABILITIES -> losesAllAbilitiesUntilEndOfTurn;
            case SUPPRESSED_STATIC_EFFECT_TYPES -> suppressedStaticEffectsUntilEndOfTurn;
            case SUPPRESSED_STATIC_EFFECT_INSTANCES -> new ArrayList<>(suppressedStaticEffectInstancesUntilEndOfTurn);
            case LOSES_ALL_CREATURE_TYPES -> losesAllCreatureTypesUntilEndOfTurn;
            case TRANSIENT_REMOVED_SUBTYPES -> transientRemovedSubtypes;
            case TEMPORARY_ACTIVATED_ABILITIES -> temporaryActivatedAbilities;
            case ADAPT_OVERRIDES -> adaptOverridesUntilEndOfTurn;
            case TEXT_REPLACEMENTS -> textReplacements.stream().filter(TextReplacement::untilEndOfTurn).toList();
            case DAMAGE_PREVENTION_SHIELD -> damagePreventionShield;
            case DAMAGE_TO_COUNTER_PREVENTION_SHIELD -> damageToCounterPreventionShield;
            case DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD -> damageToPlusOnePlusOneCounterPreventionShield;
            case ALL_DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD -> allDamageToPlusOnePlusOneCounterPreventionShield;
            case DAMAGE_REPLACED_BY_PLUS_ONE_PLUS_ONE_COUNTERS -> damageReplacedByPlusOnePlusOneCounters;
            case DAMAGE_DESTRUCTION_SHIELD -> damageDestructionShield;
            case LAND_DESTRUCTION_SHIELD -> landDestructionShield;
            case REGENERATION_SHIELD -> regenerationShield;
            case MINUS_ONE_COUNTER_REGENERATION_SHIELD -> minusOneCounterRegenerationShield;
            case OPPONENT_DRAW_REGENERATION_SHIELD -> opponentDrawRegenerationShield;
            case OPPONENT_DRAW_REGENERATION_SHIELD_GRANTS -> opponentDrawRegenerationShieldGrants();
            case PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD -> plusOnePlusOneCounterRegenerationShield;
            case PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD_CONTROLLERS ->
                    plusOnePlusOneCounterRegenerationShieldControllers.stream().filter(Objects::nonNull).toList();
            case GAIN_CONTROL_REGENERATION_SHIELDS ->
                    gainControlRegenerationShields.stream().filter(Objects::nonNull).toList();
            case TIMES_REGENERATED -> timesRegeneratedThisTurn;
            case TAPPED_PERMANENTS_FOR_ABILITY -> List.copyOf(tappedPermanentsForAbilityThisTurn);
            case CREATURES_THAT_CREWED -> Set.copyOf(creaturesThatCrewedThisTurn);
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addEndOfTurnStateValue(EndOfTurnStateField field, Object value) {
        switch (field) {
            case POWER_MODIFIER -> powerModifier = SaturatingMath.add(powerModifier, (Integer) value);
            case TOUGHNESS_MODIFIER -> toughnessModifier = SaturatingMath.add(toughnessModifier, (Integer) value);
            case ADDITIONAL_BLOCKS -> additionalBlocksUntilEndOfTurn += (Integer) value;
            case BASE_POWER_TOUGHNESS_OVERRIDDEN -> basePowerToughnessOverriddenUntilEndOfTurn = true;
            case BASE_POWER_OVERRIDE -> basePowerOverride = (Integer) value;
            case BASE_TOUGHNESS_OVERRIDE -> baseToughnessOverride = (Integer) value;
            case CANT_BE_BLOCKED -> cantBeBlocked = true;
            case CANT_BE_BLOCKED_GRANTED_ABILITY -> cantBeBlockedGrantedAbility = true;
            case CANT_ATTACK_THIS_TURN -> cantAttackThisTurn = true;
            case CANT_BLOCK_THIS_TURN -> cantBlockThisTurn = true;
            case CANT_BLOCK_GRANTED_ABILITY_THIS_TURN -> cantBlockGrantedAbilityThisTurn = true;
            case MUST_BLOCK_THIS_TURN_IF_ABLE -> mustBlockThisTurnIfAble = true;
            case MUST_BLOCK_EACH_ATTACKING_CREATURE_THIS_TURN_IF_ABLE -> mustBlockEachAttackingCreatureThisTurnIfAble = true;
            case MUST_ATTACK_THIS_TURN -> mustAttackThisTurn = true;
            case MUST_ATTACK_TARGET_ID -> mustAttackTargetId = (UUID) value;
            case MUST_BE_BLOCKED_THIS_TURN -> mustBeBlockedThisTurn = true;
            case MUST_BE_BLOCKED_BY_ALL_THIS_TURN -> mustBeBlockedByAllThisTurn = true;
            case BLOCKED_WITHOUT_BLOCKERS -> blockedWithoutBlockers = true;
            case AURA_EFFECTS_IGNORED_BY_PLAYERS -> auraEffectsIgnoredByPlayersThisTurn.addAll((Set<UUID>) value);
            case DAMPING_ENGINE_IGNORED_BY_PLAYERS -> dampingEngineIgnoredByPlayersThisTurn.addAll((Set<UUID>) value);
            case CANT_REGENERATE_THIS_TURN -> cantRegenerateThisTurn = true;
            case DAMAGE_CANT_BE_PREVENTED_OR_REDIRECTED -> damageCantBePreventedOrRedirectedThisTurn = true;
            case DAMAGED_CREATURES_CANT_REGENERATE -> damagedCreaturesCantRegenerateThisTurn = true;
            case EXILE_DAMAGED_CREATURES_INSTEAD_OF_DYING -> exileDamagedCreaturesInsteadOfDyingThisTurn = true;
            case EXILE_INSTEAD_OF_DIE -> exileInsteadOfDieThisTurn = true;
            case EXILE_INSTEAD_OF_DIE_SOURCE_PERMANENT_ID -> exileInsteadOfDieSourcePermanentId = (UUID) value;
            case DAMAGE_TO_OPPONENT_CREATURE_BOUNCE -> hasDamageToOpponentCreatureBounce = true;
            case TEMPORARY_ABILITIES -> temporaryAbilities.addAll((List<Ability>) value);
            case SADDLED -> saddled = true;
            case ANIMATED_UNTIL_END_OF_TURN -> animatedUntilEndOfTurn = true;
            case ANIMATED_POWER -> animatedPower = (Integer) value;
            case ANIMATED_TOUGHNESS -> animatedToughness = (Integer) value;
            case ANIMATED_COLOR -> animatedColor = (CardColor) value;
            case GRANTED_KEYWORDS -> grantedKeywords.addAll((Set<Keyword>) value);
            case REMOVED_KEYWORDS -> removedKeywords.addAll((Set<Keyword>) value);
            case TRANSIENT_COLORS -> transientColors.addAll((Set<CardColor>) value);
            case COLOR_OVERRIDDEN -> colorOverridden = true;
            case TRANSIENT_SUBTYPES -> transientSubtypes.addAll((List<CardSubtype>) value);
            case TRANSIENT_LAND_TYPE_OVERRIDE -> transientLandTypeOverride = (CardSubtype) value;
            case TRANSIENT_CREATURE_TYPE_OVERRIDE -> transientCreatureTypeOverride = (CardSubtype) value;
            case TRANSIENT_CREATURE_TYPE_OVERRIDES -> transientCreatureTypeOverrides.addAll((List<CardSubtype>) value);
            case GRANTED_CARD_TYPES -> grantedCardTypes.addAll((Set<CardType>) value);
            case PROTECTION_FROM_CARD_TYPES -> protectionFromCardTypes.addAll((Set<CardType>) value);
            case PROTECTION_FROM_COLORS -> protectionFromColorsUntilEndOfTurn.addAll((Set<CardColor>) value);
            case PROTECTION_FROM_PLAYERS -> protectionFromPlayerIdsUntilEndOfTurn.addAll((Set<UUID>) value);
            case PROTECTION_FROM_COLORLESS -> protectionFromColorlessUntilEndOfTurn = true;
            case PROTECTION_FROM_NON_SUBTYPE_CREATURES ->
                    protectionFromNonSubtypeCreaturesUntilEndOfTurn.addAll((Set<CardSubtype>) value);
            case PROTECTION_FROM_OPPONENT_CREATURES -> protectionFromOpponentCreaturesUntilEndOfTurn = true;
            case PROTECTION_REMOVED -> protectionRemovedUntilEndOfTurn = true;
            case BLOCK_RESTRICTIONS -> blockRestrictionsUntilEndOfTurn.addAll((List<CanBeBlockedOnlyByFilterEffect>) value);
            case UNBLOCKABLE_IF_DEFENDER_CONTROLS ->
                    unblockableIfDefenderControlsUntilEndOfTurn.addAll((List<PermanentPredicate>) value);
            case EXILE_IF_LEAVES_BATTLEFIELD -> exileIfLeavesBattlefieldUntilEndOfTurn = true;
            case SHROUD_IGNORED_BY_PLAYERS -> shroudIgnoredByPlayersUntilEndOfTurn.addAll((Set<UUID>) value);
            case CANT_BLOCK_IDS -> cantBlockIds.addAll((Set<UUID>) value);
            case MUST_BLOCK_IDS -> mustBlockIds.addAll((Set<UUID>) value);
            case LOSES_ALL_ABILITIES -> losesAllAbilitiesUntilEndOfTurn = true;
            case SUPPRESSED_STATIC_EFFECT_TYPES -> suppressedStaticEffectsUntilEndOfTurn.addAll((Set) value);
            case SUPPRESSED_STATIC_EFFECT_INSTANCES ->
                    suppressedStaticEffectInstancesUntilEndOfTurn.addAll((List<CardEffect>) value);
            case LOSES_ALL_CREATURE_TYPES -> losesAllCreatureTypesUntilEndOfTurn = true;
            case TRANSIENT_REMOVED_SUBTYPES -> transientRemovedSubtypes.addAll((Set<CardSubtype>) value);
            case TEMPORARY_ACTIVATED_ABILITIES -> temporaryActivatedAbilities.addAll((List<ActivatedAbility>) value);
            case ADAPT_OVERRIDES -> adaptOverridesUntilEndOfTurn += (Integer) value;
            case TEXT_REPLACEMENTS -> textReplacements.addAll((List<TextReplacement>) value);
            case DAMAGE_PREVENTION_SHIELD -> damagePreventionShield += (Integer) value;
            case DAMAGE_TO_COUNTER_PREVENTION_SHIELD -> damageToCounterPreventionShield += (Integer) value;
            case DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD ->
                    damageToPlusOnePlusOneCounterPreventionShield += (Integer) value;
            case ALL_DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD -> allDamageToPlusOnePlusOneCounterPreventionShield = true;
            case DAMAGE_REPLACED_BY_PLUS_ONE_PLUS_ONE_COUNTERS -> damageReplacedByPlusOnePlusOneCounters = true;
            case DAMAGE_DESTRUCTION_SHIELD -> damageDestructionShield += (Integer) value;
            case LAND_DESTRUCTION_SHIELD -> landDestructionShield += (Integer) value;
            case REGENERATION_SHIELD -> regenerationShield += (Integer) value;
            case MINUS_ONE_COUNTER_REGENERATION_SHIELD -> minusOneCounterRegenerationShield += (Integer) value;
            case OPPONENT_DRAW_REGENERATION_SHIELD -> opponentDrawRegenerationShield += (Integer) value;
            case OPPONENT_DRAW_REGENERATION_SHIELD_GRANTS -> ((List<Map.Entry<UUID, UUID>>) value).forEach(grant -> {
                opponentDrawRegenerationShieldRecipients.add(grant.getKey());
                opponentDrawRegenerationShieldControllers.add(grant.getValue());
            });
            case PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD -> plusOnePlusOneCounterRegenerationShield += (Integer) value;
            case PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD_CONTROLLERS ->
                    plusOnePlusOneCounterRegenerationShieldControllers.addAll((List<UUID>) value);
            case GAIN_CONTROL_REGENERATION_SHIELDS -> gainControlRegenerationShields.addAll((List<UUID>) value);
            case TIMES_REGENERATED -> timesRegeneratedThisTurn += (Integer) value;
            case TAPPED_PERMANENTS_FOR_ABILITY -> ((List<UUID>) value).forEach(this::recordTappedPermanentForAbility);
            case CREATURES_THAT_CREWED -> creaturesThatCrewedThisTurn.addAll((Set<UUID>) value);
            default -> {
            }
        }
    }

    /** Soldevi Sentry's shield recipients paired with the controllers of their draw triggers, in grant order. */
    private List<Map.Entry<UUID, UUID>> opponentDrawRegenerationShieldGrants() {
        List<Map.Entry<UUID, UUID>> grants = new ArrayList<>();
        int pairs = Math.min(opponentDrawRegenerationShieldRecipients.size(),
                opponentDrawRegenerationShieldControllers.size());
        for (int i = 0; i < pairs; i++) {
            grants.add(new AbstractMap.SimpleImmutableEntry<>(opponentDrawRegenerationShieldRecipients.get(i),
                    opponentDrawRegenerationShieldControllers.get(i)));
        }
        return grants;
    }

    /** Drops the "until end of turn" word substitutions (CR 612). */
    private void expireTemporaryTextReplacements() {
        this.textReplacements.removeIf(TextReplacement::untilEndOfTurn);
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
        this.nonCopiableKeywords.clear();
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
    public void addProtectionFromPlayerUntilNextTurnOf(UUID untilTurnOfPlayerId, UUID protectedFromPlayerId) {
        protectionFromPlayerIdsUntilNextTurnOf
                .computeIfAbsent(untilTurnOfPlayerId, ignored -> new HashSet<>()).add(protectedFromPlayerId);
    }

    /** Ends the protection-from-player grants that last until {@code untilTurnOfPlayerId}'s next turn. */
    public void clearProtectionFromPlayersUntilTurnOf(UUID untilTurnOfPlayerId) {
        protectionFromPlayerIdsUntilNextTurnOf.remove(untilTurnOfPlayerId);
    }

    public boolean hasProtectionFromPlayerUntilNextTurn(UUID protectedFromPlayerId) {
        return protectionFromPlayerIdsUntilNextTurnOf.values().stream()
                .anyMatch(playerIds -> playerIds.contains(protectedFromPlayerId));
    }

    public void clearUntilNextTurnEffects() {
        this.untilNextTurnActivatedAbilities.clear();
        if (this.untilNextTurnAnimationGrantorId == null) {
            clearUntilNextTurnAnimation();
        }
        this.untilNextTurnLandTypeOverride = null;
    }

    /**
     * Ends an "until your next turn" animation granted by {@code playerId}'s effect when that player's
     * turn begins. An animation an opponent granted outlasts the permanent's own controller's turns.
     */
    public void clearUntilNextTurnAnimationGrantedBy(UUID playerId) {
        if (playerId != null && playerId.equals(this.untilNextTurnAnimationGrantorId)) {
            clearUntilNextTurnAnimation();
        }
    }

    private void clearUntilNextTurnAnimation() {
        this.animatedUntilNextTurn = false;
        this.untilNextTurnAnimatedPower = 0;
        this.untilNextTurnAnimatedToughness = 0;
        this.untilNextTurnSubtypes.clear();
        this.untilNextTurnKeywords.clear();
        this.untilNextTurnAnimationGrantorId = null;
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
        this.untilNextTurnCopyLayers.clear();
        if (this.copyUntilControllerNextTurn && this.untilNextTurnPreCopyCard != null) {
            this.card = this.untilNextTurnPreCopyCard;
        }
        this.copyUntilControllerNextTurn = false;
        this.untilNextTurnPreCopyCard = null;
        this.copyUntilNextTurnControllerId = null;
    }

    /** Records the copy just applied by an effect that lasts until {@code controllerId}'s next turn. */
    public void pushUntilNextTurnCopyLayer(UUID controllerId) {
        this.untilNextTurnCopyLayers.add(new UntilNextTurnCopyLayer(controllerId, this.card));
    }

    /**
     * Ends the copies that last until {@code controllerId}'s next turn. If the newest copy ended while an
     * earlier one is still active, that earlier copy applies again; otherwise the permanent reverts.
     */
    public void expireUntilNextTurnCopyLayers(UUID controllerId) {
        if (untilNextTurnCopyLayers.isEmpty()) {
            return;
        }
        UntilNextTurnCopyLayer newestBefore = untilNextTurnCopyLayers.getLast();
        untilNextTurnCopyLayers.removeIf(layer -> layer.controllerId().equals(controllerId));
        if (untilNextTurnCopyLayers.isEmpty()) {
            revertUntilNextTurnCopy();
        } else if (!untilNextTurnCopyLayers.getLast().equals(newestBefore)) {
            UntilNextTurnCopyLayer newest = untilNextTurnCopyLayers.getLast();
            this.card = newest.copyCard();
            this.copyUntilNextTurnControllerId = newest.controllerId();
        }
    }

    public boolean hasUntilNextTurnCopyLayers() {
        return !untilNextTurnCopyLayers.isEmpty();
    }

    public void ignoreAuraEffectsThisTurnFor(UUID playerId) {
        auraEffectsIgnoredByPlayersThisTurn.add(playerId);
    }

    public boolean isAuraEffectsIgnoredThisTurn() {
        return !auraEffectsIgnoredByPlayersThisTurn.isEmpty();
    }

    public boolean isAuraEffectsIgnoredFor(UUID playerId) {
        return auraEffectsIgnoredByPlayersThisTurn.contains(playerId);
    }
}
