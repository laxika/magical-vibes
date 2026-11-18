package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.DeclarationReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.effect.SetMaximumLifeTotalEffect;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesEventLog;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpellColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardCantBePlayedFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardRevealedInHandEffect;
import com.github.laxika.magicalvibes.model.effect.SenTripletsUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextInstantSorceryUncounterableThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextSpellUncounterableThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeForNextSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPayLifeEqualToSpellManaValueForNextSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantConvokeToNextSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWithChosenPlanarModePlayAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OnlyChosenCreaturesCanAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.RenewExilePlayPermissionOnLoreCounterEffect;
import com.github.laxika.magicalvibes.model.effect.LukkaExileCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileLookPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyManaTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.LimitControllerToOneMoreSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastSpellTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ForageOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.RemoveAllMireCountersFromChosenLandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class GameData {

    public GameSession session = new GameSession(this);
    public boolean subgameRequested;
    public boolean waitingForSubgame;
    public final List<PendingAncestorTransfer> pendingAncestorTransfers = new ArrayList<>();
    // Physical cards also remain in the subgame while an interaction temporarily holds them.
    public final Map<UUID, Card> subgameCards = new HashMap<>();
    public final List<StackEntry> suspendedZoneTriggers = new ArrayList<>();

    public final UUID id;
    public final String gameName;
    public final UUID createdByUserId;
    public final String createdByUsername;
    public final LocalDateTime createdAt;
    public volatile GameStatus status;
    /** "All Random" game mode: every player is dealt a randomly generated deck. */
    public volatile boolean allRandom;
    public com.github.laxika.magicalvibes.model.planar.PlanechaseState planechase;
    /** For an {@link #allRandom} game: the set code the random decks draw from, or {@code null} for all sets. */
    public volatile String randomSetCode;
    public final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
    public final List<UUID> orderedPlayerIds = Collections.synchronizedList(new ArrayList<>());
    public final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, String> playerIdToName = new ConcurrentHashMap<>();
    public final Map<UUID, String> playerDeckChoices = new ConcurrentHashMap<>();
    /** Counters retained by a card-specific zone-change ability while the card is off the battlefield. */
    public final Map<UUID, Map<CounterType, Integer>> countersPreservedAcrossZoneChanges =
            new ConcurrentHashMap<>();
    /** Persistent intensity for digital cards; keyed by physical card identity and retained across zones. */
    public final Map<UUID, Integer> cardIntensities = new ConcurrentHashMap<>();
    /**
     * Rules events the trigger pipeline hasn't taken yet, and what describing zone changes needs (see
     * {@link RulesEventLog}). The zone lists below report every card added to them here.
     */
    public final RulesEventLog rulesEvents = new RulesEventLog(this::rulesEventBatchClosed,
            this::resolvingControllerForRulesEvents);
    /**
     * Test-only watcher of rules events and of the triggered abilities put toward the stack (the rules event recorder);
     * {@code null} in real games and never copied into simulations.
     */
    public RulesObserver rulesObserver;
    /** The engine's trigger pipeline, handed every rules event batch as it closes; set at game setup. */
    private transient volatile BiConsumer<GameData, RulesEventBatch> rulesEventListener;
    /**
     * The record of a trigger event on its way to the triggered ability it belongs to: the entry the ability's
     * collector builds for an ability that chooses nothing ({@link #handOffTriggerRecord}), taken once by whatever
     * the ability goes through next. A choice queued ({@link #queueInteraction}) or asked
     * ({@link InteractionState#beginInteraction}) keeps it until it is taken out or answered, when it is in hand
     * again; the triggered ability that then reaches the stack or a holding queue is given it
     * ({@code StackEntry.carryTriggerRecord}). So a mode (CR 603.3c), a target (CR 603.3d) or a graveyard card chosen
     * as the ability goes on the stack can't lose what the event was.
     */
    private StackEntry triggerRecordInHand;
    /** The record each queued interaction took from the hand, by the interaction's identity. */
    private final IdentityHashMap<PendingInteraction, StackEntry> queuedTriggerRecords = new IdentityHashMap<>();
    /** How many collectors are routing an ability right now ({@link #beginTriggerRouting}). */
    private int triggerRoutingDepth;
    /** Whether the collector now routing an ability has handed off its record. */
    private boolean triggerRecordHandedOff;
    /**
     * Told of every interaction queued or asked with no trigger record in hand, and whether a collector that is
     * routing an ability did so without having handed off the ability's record. {@code null} in real games; the test
     * fixtures set one that fails the test for such a collector.
     */
    public transient BiConsumer<PendingInteraction, Boolean> interactionWithoutTriggerRecord;
    /**
     * Told of every trigger record that leaves the hand without having been taken by a choice or an ability: the
     * record of an ability removed for want of a legal choice (CR 603.3d), or of a route that reached the stack
     * before its record was in hand. {@code null} in real games; the test fixtures' audit sets one.
     */
    public transient java.util.function.Consumer<StackEntry> triggerRecordLeftBehind;
    public final Map<UUID, List<Card>> playerDecks = new LibraryMap();
    /** The size of each player's deck before the opening hand was drawn. */
    public final Map<UUID, Integer> startingDeckSizes = new ConcurrentHashMap<>();
    /** Physical card identities that began in each player's starting deck. */
    public final Map<UUID, Set<UUID>> startingDeckCardIds = new ConcurrentHashMap<>();
    /** Cards owned by each player that began outside the game, such as a sideboard. */
    public final Map<UUID, List<Card>> playerSideboards = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerHands = new CommanderHandMap();
    public final Map<UUID, Integer> mulliganCounts = new ConcurrentHashMap<>();
    public final Set<UUID> playerKeptHand = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerNeedsToBottom = new ConcurrentHashMap<>();
    /**
     * Players whose current hand already had this mulligan's cards put on the bottom before keeping. The
     * engine normally defers bottoming to keep, but a hand holding Serum Powder bottoms right after the
     * mulligan, as CR 103.5 orders it, so Powder exiles the post-bottom hand.
     */
    public final Set<UUID> playerBottomedBeforeKeeping = ConcurrentHashMap.newKeySet();
    /** Stable identities for the currently open pregame decisions. */
    public final Map<UUID, UUID> playerMulliganDecisionIds = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> playerBottomDecisionIds = new ConcurrentHashMap<>();
    public final List<GameLogEntry> gameLog = Collections.synchronizedList(new ArrayList<>());
    public UUID startingPlayerId;
    public TurnStep currentStep;
    public UUID activePlayerId;
    /** Active player of the turn immediately preceding the current turn, including extra turns. */
    public UUID previousTurnActivePlayerId;
    /** Whether Runed Terror has split the current turn into sequential phases. */
    public boolean runedTerrorPhaseSequenceActive;
    /** The player who began the current sequential phase cycle. */
    public UUID runedTerrorPhaseSequenceFirstPlayerId;
    /** Whether a sequential beginning phase is currently performing its untap work. */
    public boolean runedTerrorSequentialUntapInProgress;
    /** Number of the active player's permanents that became untapped during the current untap step. */
    public int untapStepUntappedPermanentCount;
    /** Player whose untap step owns {@link #untapStepUntappedPermanentCount}. */
    public UUID untapStepPlayerId;
    /**
     * The player whose untap-step phasing has already been applied ahead of a Storage Matrix choice,
     * so {@code UntapStepService.untapPermanents} does not apply it a second time.
     */
    public UUID untapPhasingAppliedPlayerId;
    public int turnNumber;

    /** Counts main phases that have begun during the current turn. */
    public int mainPhasesBegunThisTurn;
    /** Counts postcombat main phases that have begun during the current turn. */
    public int postcombatMainPhasesBegunThisTurn;
    /** Greatest number of distinct spell or ability sources simultaneously on the stack this turn. */
    private int greatestStackSourceCountThisTurn;
    /** Whether the turn currently in progress was taken from the extra-turn queue. */
    public boolean currentTurnIsExtraTurn;

    /** Most recent normal turn, used to resume turn order after extra turns. */
    public UUID lastNormalTurnPlayerId;
    /** Whether the current turn's untap step was skipped by an extra-turn or similar effect. */
    public boolean turnUntapStepSkipped;
    /**
     * The current extra turn skips every untap step it has (Savor the Moment's "skip the untap step of
     * that turn"), including the untap step of an additional beginning phase (Sphinx of the Second Sun).
     * No official ruling covers a turn with two untap steps; the engine reads the skip as covering the
     * whole turn, matching the card's intent.
     */
    public boolean extraTurnSkipsAllUntapSteps;
    /** Whether the draw step most recently entered was skipped, so it offers no priority window (rule 500.11). */
    public boolean drawStepSkippedThisStep;
    /** Whether Power-up abilities are prohibited during the current turn. */
    public boolean powerUpAbilitiesCantBeActivatedThisTurn;
    public Long currentExtraTurnSequence;
    public long nextExtraTurnSequence = 1;
    /**
     * Number of turns each player has taken so far this game, including the turn currently in progress
     * and any extra turns. Used by cards that speak of "your first, second, or third turns of the game".
     */
    public final Map<UUID, Integer> turnsTakenByPlayer = new ConcurrentHashMap<>();
    /** Final authoritative runtime result; null until the game has ended. */
    public GameEventFact.GameResult gameResult;
    public UUID winnerPlayerId;
    /** Players who have lost a game in the current match. */
    public final Set<UUID> playersWhoLostGameThisMatch = ConcurrentHashMap.newKeySet();
    /** Creature types replaced for the rest of the match by revealed errata sources (Ruff, Underdog Champ). */
    public final Map<CardSubtype, CardSubtype> matchCreatureTypeErrata = new ConcurrentHashMap<>();
    public final Set<UUID> priorityPassedBy = ConcurrentHashMap.newKeySet();
    /**
     * The player who last received priority by acting while holding it (CR 117.3c: a player who
     * casts a spell, activates an ability, or takes a special action while holding priority
     * receives priority afterward). Priority moves on from this player in turn order, skipping
     * players in {@link #priorityPassedBy}. {@code null} means the active player, who receives
     * priority at the beginning of a step and after a spell or ability resolves (CR 117.3a, 117.3b).
     */
    public volatile UUID priorityHolderId;
    /** Players who met Knight of Lost Causes' "way behind" condition at any point this turn. */
    public final Set<UUID> playersWhoWereWayBehindThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> landsPlayedThisTurn = new ConcurrentHashMap<>();
    /** The lands each player played this turn, in play order (their mana values are read by Coststorm). */
    public final Map<UUID, List<Card>> landCardsPlayedThisTurn = new ConcurrentHashMap<>();
    /** Extra land plays granted this turn (e.g. Summer Bloom), on top of the normal one-per-turn. */
    public final Map<UUID, Integer> additionalLandsThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /**
     * While non-null, token creation puts its tokens onto the battlefield but queues the entry notifications here,
     * so one effect that creates tokens for several players can run them all once every token is on the battlefield
     * (CR 603.6a). Only touched while the game's mutation lock is held.
     */
    public List<Runnable> deferredTokenEntryNotifications;
    /** Permanents sacrificed by each player this turn, retaining last-known card characteristics. */
    public final Map<UUID, List<Card>> permanentsSacrificedThisTurn = new ConcurrentHashMap<>();
    /** Permanent card ids currently being sacrificed as an additional cost of a spell cast. */
    public final Set<UUID> currentCastSacrificedPermanentIds = ConcurrentHashMap.newKeySet();
    /** Spell whose explicit "behold or pay" cost was waived by the cast request being processed, if any. */
    public UUID beholdCostWaivedCardId;
    /** Hand card revealed to waive {@link #beholdCostWaivedCardId}'s cost, or null when a permanent was beheld. */
    public Card beholdCostRevealedCard;
    /** Face-down creatures that entered under each player's control this turn, including ones that have since left or turned face up. */
    public final Map<UUID, List<Card>> faceDownCreaturesEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Face-down permanents that entered under each player's control this turn, including ones that have since left or turned face up. */
    public final Map<UUID, List<Card>> faceDownPermanentsEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Players who turned a permanent face up this turn. */
    public final Set<UUID> playersWhoTurnedPermanentsFaceUpThisTurn = ConcurrentHashMap.newKeySet();
    /** Battlefield permanent identities that were turned face up this turn. */
    public final Set<UUID> permanentsTurnedFaceUpThisTurn = ConcurrentHashMap.newKeySet();
    /** Snapshot of permanents that entered under each player's control during the immediately preceding turn. */
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldLastTurn = new ConcurrentHashMap<>();
    /**
     * For each player, the cards of permanents that entered the battlefield since that player's most recent turn
     * ended (the list is cleared when the player's turn ends, so extra turns and consecutive turns are handled).
     */
    public final Map<UUID, Set<UUID>> permanentsEnteredSincePlayerLastTurnEnded = new ConcurrentHashMap<>();
    /** Players who cast a spell or put a nontoken permanent onto the battlefield during the active player's own turn. */
    public final Set<UUID> playersWhoActedDuringTheirTurn = ConcurrentHashMap.newKeySet();
    /** Players who took either Arboria action during their own most recently completed turn. */
    public final Set<UUID> playersWhoActedDuringTheirLastTurn = ConcurrentHashMap.newKeySet();
    /** All spells cast by each player this turn. Access via {@link #recordSpellCast}, {@link #getSpellsCastThisTurnCount}, etc. */
    private final Map<UUID, List<Card>> spellsCastThisTurn = new ConcurrentHashMap<>();
    /** Mana value each spell cast this turn had on the stack, including its chosen X, keyed by card id. */
    private final Map<UUID, Integer> spellManaValuesWhenCastThisTurn = new ConcurrentHashMap<>();
    /** Players who cast a spell targeting a creature permanent this turn. */
    private final Map<UUID, Set<UUID>> spellsCastTargetingCreatureThisTurn = new ConcurrentHashMap<>();
    /** Card ids of spells cast with Treasure-produced mana this turn. */
    private final Set<UUID> spellsCastUsingTreasureManaThisTurn = ConcurrentHashMap.newKeySet();
    /** Card ids of spells cast this turn that had convoke granted by a next-spell convoke grant. */
    private final Set<UUID> spellsCastWithConvokeGrantThisTurn = ConcurrentHashMap.newKeySet();
    /** Per-player spell-cast counts by source zone for this turn. */
    private final Map<UUID, Map<Zone, Integer>> spellCastCountsByZoneThisTurn = new ConcurrentHashMap<>();
    /** The spells each player cast this turn, grouped by the zone they were cast from. */
    private final Map<UUID, Map<Zone, List<Card>>> spellCardsCastByZoneThisTurn = new ConcurrentHashMap<>();
    /** Card ids of spells that were kicked when cast, grouped by caster for the current turn. */
    private final Map<UUID, Set<UUID>> kickedSpellsCastThisTurn = new ConcurrentHashMap<>();
    /** Number of spells each player cast from somewhere other than their hand this turn. */
    private final Map<UUID, Integer> spellsCastFromOutsideHandThisTurn = new ConcurrentHashMap<>();
    /** Whether any spell was cast for its Warp cost this turn. */
    public boolean spellWarpedThisTurn;
    public final Map<UUID, List<StackEntry>> crimeCandidatesThisTurn = new ConcurrentHashMap<>();
    private final Set<UUID> spellsCastFromHandThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose creature spell was countered by an opponent this turn (Summoning Trap). */
    public final Set<UUID> playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who searched their own libraries this turn, including searches that found no card. */
    public final Set<UUID> playersWhoSearchedLibraryThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have received the city's blessing for the rest of the game. */
    public final Set<UUID> playersWithCityBlessing = ConcurrentHashMap.newKeySet();
    /** Creature types each player has written on their buddy list for the rest of this game. */
    public final Map<UUID, Set<CardSubtype>> playerBuddyLists = new ConcurrentHashMap<>();
    /** Players who have received the enduring story designation for the rest of the game. */
    public final Set<UUID> playersWithEnduringStory = ConcurrentHashMap.newKeySet();
    /** Players who have completed at least one dungeon this game. */
    public final Set<UUID> playersWhoCompletedDungeon = ConcurrentHashMap.newKeySet();
    /** Distinct dungeons completed by each player this game. */
    public final Map<UUID, Set<Dungeon>> completedDungeonsByPlayer = new ConcurrentHashMap<>();
    /** The current dungeon and room marked by each player's venture marker. */
    public final Map<UUID, DungeonProgress> playerDungeonProgress = new ConcurrentHashMap<>();
    /** Players who ventured into a dungeon at least once this turn. */
    public final Set<UUID> playersWhoVenturedIntoDungeonThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who played at least one card from exile this turn. */
    public final Set<UUID> playersWhoPlayedCardFromExileThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who flipped at least one coin this turn. */
    public final Set<UUID> playersWhoFlippedCoinsThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who played a land or cast a spell from somewhere other than their hand this turn. */
    public final Set<UUID> playersWhoPlayedOrCastFromOutsideHandThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who investigated this turn. */
    public final Set<UUID> playersWhoInvestigatedThisTurn = ConcurrentHashMap.newKeySet();
    /** Counts permanents sacrificed by subtype and controller this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> sacrificedPermanentSubtypeCountThisTurn = new ConcurrentHashMap<>();
    /** Counts permanents sacrificed by each player this turn. */
    public final Map<UUID, Integer> sacrificedPermanentCountThisTurn = new ConcurrentHashMap<>();
    /** Players who surveilled at least once this turn. */
    public final Set<UUID> playersWhoSurveilledThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who controlled a permanent that explored at least once this turn. */
    public final Set<UUID> playersWhoControlledPermanentThatExploredThisTurn = ConcurrentHashMap.newKeySet();
    /**
     * Card IDs of every spell cast this turn by any player, in cast order. Unlike
     * {@link #spellsCastThisTurn} this preserves the global ordering across players, which is what
     * "the second spell cast this turn" (Second Guess) needs. Read via
     * {@link #getSpellCastOrdinalThisTurn}.
     */
    private final List<UUID> spellCastOrderThisTurn = Collections.synchronizedList(new ArrayList<>());
    /**
     * Spells cast this turn that were artifact spells at the time they were cast, whether printed or
     * through a static grant such as Encroaching Mycosynth's. A grant that later ends doesn't
     * change what an earlier cast was.
     */
    private final Set<UUID> artifactSpellsCastThisTurn = ConcurrentHashMap.newKeySet();
    /** Mana value each X spell cast this turn had on the stack, X included (CR 202.3e). */
    private final Map<UUID, Integer> spellCastStackManaValuesThisTurn = new ConcurrentHashMap<>();
    /** The spell most recently cast by any player this turn, regardless of controller. */
    private Card mostRecentSpellCastThisTurn;
    /**
     * Per-player count of spells cast this game, keyed by spell name. Unlike {@link #spellsCastThisTurn}
     * this persists for the whole game. Populated by {@link #recordSpellCast}; read via
     * {@link #getSpellsCastThisGameByNameCount} (Approach of the Second Sun's "cast another spell named ... this game").
     */
    private final Map<UUID, Map<String, Integer>> spellNameCastCountsThisGame = new ConcurrentHashMap<>();
    /** Per-player count of cards cycled this game, keyed by card name. */
    private final Map<UUID, Map<String, Integer>> cardNameCycleCountsThisGame = new ConcurrentHashMap<>();
    /** Total mana each player has spent to cast spells this turn. */
    public final Map<UUID, Integer> manaSpentToCastSpellsThisTurn = new ConcurrentHashMap<>();
    /**
     * Transient mana spent to cast a spell, keyed by spell card instance id.
     * Populated during spell payment and consumed when spell-cast triggers fire.
     */
    public final Map<UUID, Integer> spellCastManaSpent = new ConcurrentHashMap<>();
    /**
     * Mana spent and mana value of each spell countered this game, keyed by its targetable id. A
     * counter removes the spell from the stack, so later effects of the same spell read it from here.
     */
    public final Map<UUID, CounteredSpellManaCost> counteredSpellManaCosts = new ConcurrentHashMap<>();
    /** Amount of mana produced by creatures and spent to cast each spell. */
    public final Map<UUID, Integer> spellCastCreatureManaSpent = new ConcurrentHashMap<>();
    /**
     * Transient Converge value for a spell, keyed by spell card instance id.
     * Populated during spell payment and consumed when the spell resolves.
     */
    public final Map<UUID, Integer> spellCastConvergeValue = new ConcurrentHashMap<>();
    /**
     * Transient set of mana colors spent to cast a spell, keyed by spell card instance id.
     * Populated during spell payment and consumed when the spell resolves (e.g. Repel Intruders).
     */
    public final Map<UUID, java.util.EnumSet<ManaColor>> spellCastColorsSpent = new ConcurrentHashMap<>();
    /** Per-color amounts of mana spent to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, java.util.EnumMap<ManaColor, Integer>> spellCastManaSpentByColor = new ConcurrentHashMap<>();
    /** Amount of mana spent from snow sources to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, Integer> spellCastSnowManaSpent = new ConcurrentHashMap<>();
    /** Amount of snow-produced mana spent for each color, keyed by spell card instance id. */
    public final Map<UUID, java.util.EnumMap<ManaColor, Integer>> spellCastSnowManaSpentByColor = new ConcurrentHashMap<>();
    /** Amount of mana produced by Treasures spent to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, Integer> spellCastTreasureManaSpent = new ConcurrentHashMap<>();
    /** Amount of mana produced by artifact sources spent to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, Integer> spellCastArtifactManaSpent = new ConcurrentHashMap<>();
    /** Amount of mana produced by Cave sources spent to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, Integer> spellCastCaveManaSpent = new ConcurrentHashMap<>();
    /** Amount of mana produced by Desert sources spent to cast a spell, keyed by spell card instance id. */
    public final Map<UUID, Integer> spellCastDesertManaSpent = new ConcurrentHashMap<>();
    /** Producing permanents whose tagged mana was spent to cast each spell. */
    public final Map<UUID, Set<UUID>> spellCastManaSourceIds = new ConcurrentHashMap<>();
    /** How much mana each producing permanent contributed to each spell, for per-mana spell-cast riders (CR 106.6). */
    public final Map<UUID, Map<UUID, Integer>> spellCastManaSourceAmounts = new ConcurrentHashMap<>();
    /** Whether mana produced by a Treasure was spent to cast each spell. */
    public final Map<UUID, Boolean> spellCastUsedTreasureMana = new ConcurrentHashMap<>();
    /**
     * Names of the cards spliced onto a spell (CR 702.47), keyed by host spell card instance id.
     * Populated as splice costs are paid and read while the spell is on the stack (Minamo's Meddling).
     */
    public final Map<UUID, List<String>> spellCastSplicedNames = new ConcurrentHashMap<>();
    /** Keywords granted to a spell while on the stack by cards spliced onto it, keyed by host spell id. */
    public final Map<UUID, Set<Keyword>> spellCastSplicedKeywords = new ConcurrentHashMap<>();
    /** Per-color mana removed specifically to pay X (not the rest of the cost). Cleared on resolution. */
    public final Map<UUID, java.util.EnumMap<ManaColor, Integer>> spellCastManaSpentOnX = new ConcurrentHashMap<>();
    /** Tracks which permanent types each player has cast from graveyard this turn via Muldrotha-style effects. */
    public final Map<UUID, Set<CardType>> permanentTypesCastFromGraveyardThisTurn = new ConcurrentHashMap<>();
    /** Permanents whose once-per-your-turn graveyard play or cast permission has been used this
     * turn, keyed by the granting permanent's id. */
    public final Set<UUID> oncePerTurnGraveyardCastPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Permanents whose once-per-your-turn land-from-graveyard permission has been used this turn. */
    public final Set<UUID> oncePerTurnGraveyardLandPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Permanents whose once-per-your-turn spell-from-graveyard permission has been used this turn. */
    public final Set<UUID> oncePerTurnGraveyardSpellPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Snapshot of per-player spell counts from the previous turn. Used by werewolf transform triggers. */
    public final Map<UUID, Integer> spellsCastLastTurn = new ConcurrentHashMap<>();
    /** Snapshot of the actual spells cast during the previous turn for filtered history queries. */
    public final Map<UUID, List<Card>> spellsCastLastTurnCards = new ConcurrentHashMap<>();
    /** The game's current day/night designation. */
    public DayNight dayNight = DayNight.NEITHER;
    /** The player who currently is the monarch, or {@code null} when no player is monarch. */
    public UUID monarchPlayerId;
    /** The player who currently has the initiative, or {@code null} when no player has it. */
    public UUID initiativePlayerId;
    /** The Ring's current ability level for each player; presence means that player has The Ring emblem. */
    public final Map<UUID, Integer> ringLevels = new ConcurrentHashMap<>();
    /** The permanent currently designated as each player's Ring-bearer. */
    public final Map<UUID, UUID> ringBearerIds = new ConcurrentHashMap<>();
    /** Tracks which players declared at least one attacker this turn (for Angelic Arbiter etc.). */
    public final Set<UUID> playersDeclaredAttackersThisTurn = ConcurrentHashMap.newKeySet();
    /** Tracks which players declared at least one token as an attacker this turn. */
    public final Set<UUID> playersWhoAttackedWithTokenThisTurn = ConcurrentHashMap.newKeySet();
    /** Tracks which players declared a commander as an attacker this turn. */
    public final Set<UUID> playersWhoAttackedWithCommanderThisTurn = ConcurrentHashMap.newKeySet();
    /** Permanent IDs declared as attackers in the current combat. */
    public final Set<UUID> declaredAttackerIdsThisCombat = ConcurrentHashMap.newKeySet();
    /**
     * IDs of the attacking creatures that enlisted a creature in the current combat (CR 702.154c): their controller paid
     * their enlist cost by tapping another creature as they attacked. Read by
     * {@code PermanentEnlistedCreatureThisCombatPredicate}; cleared with {@link #declaredAttackerIdsThisCombat}.
     */
    public final Set<UUID> creaturesThatEnlistedThisCombat = ConcurrentHashMap.newKeySet();
    /** Raging River: each attacking permanent maps to the independent nonflying pile restrictions
     * chosen for it during the current combat. */
    /** Players who put at least one counter on a creature this turn. */
    public final Set<UUID> playersWhoPutCountersOnCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Permanent IDs keyed by the player who put a counter on them during this turn. */
    public final Map<UUID, Set<UUID>> permanentsWithCountersPutByPlayerThisTurn = new ConcurrentHashMap<>();
    /** Players who put at least one +1/+1 counter on a creature this turn. */
    public final Set<UUID> playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who removed at least one oil counter from a permanent they controlled this turn. */
    public final Set<UUID> playersWhoRemovedOilCountersFromControlledPermanentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Whether a permanent carrying an oil counter was put into a graveyard this turn. */
    public boolean permanentWithOilCounterPutIntoGraveyardThisTurn;
    public boolean artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn;
    public boolean permanentPutIntoGraveyardFromBattlefieldThisTurn;
    /** Players whose graveyard received an enchantment from the battlefield this turn. */
    public final Set<UUID> playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Players who controlled a land put into a graveyard from the battlefield this turn. */
    public final Set<UUID> playersWhoControlledLandPutIntoGraveyardFromBattlefieldThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Number of permanents put into graveyards from the battlefield this turn, including tokens. */
    public int permanentsPutIntoGraveyardFromBattlefieldThisTurn;
    /** Players who controlled a permanent that received a +1/+1 counter this turn. */
    public final Set<UUID> playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn = ConcurrentHashMap.newKeySet();
    public final Set<UUID> permanentsThatReceivedPlusOnePlusOneCountersThisTurn = ConcurrentHashMap.newKeySet();
    /**
     * Permanents that had +1/+1 counters put on them this turn, grouped by the player who put those
     * counters. A placement stays recorded if the counters are later removed; cleared when the turn
     * advances. Predicates in static abilities read it, so it is part of the layer-system
     * fingerprint.
     */
    public final Map<UUID, Set<UUID>> permanentsWithPlusOneCountersPutByPlayerThisTurn = new ConcurrentHashMap<>();
    /** Per-player count of +1/+1 counters put on creatures they controlled this turn. */
    public final Map<UUID, Integer> plusOnePlusOneCountersPutOnControlledCreaturesThisTurn = new ConcurrentHashMap<>();
    /** Players who created at least one token this turn. */
    public final Set<UUID> playersWhoCreatedTokensThisTurn = ConcurrentHashMap.newKeySet();
    /** Card identities that were made tokens by a continuous effect and are awaiting zone cleanup. */
    public final Set<UUID> dynamicTokenCardIds = ConcurrentHashMap.newKeySet();
    /** Number of Treasure tokens created under each player's control this turn. */
    public final Map<UUID, Integer> treasureTokensCreatedThisTurn = new ConcurrentHashMap<>();
    /** Permanent IDs of creatures that have fought at least once this turn. */
    public final Set<UUID> permanentsThatFoughtThisTurn = ConcurrentHashMap.newKeySet();
    /** Per-player count of tokens created this turn. */
    public final Map<UUID, Integer> tokensCreatedThisTurn = new ConcurrentHashMap<>();
    /** Players who sacrificed at least one permanent this turn. */
    public final Set<UUID> playersWhoSacrificedPermanentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who sacrificed at least one artifact this turn. */
    public final Set<UUID> playersWhoSacrificedArtifactsThisTurn = ConcurrentHashMap.newKeySet();
    /** Cumulative count of attacking creatures each player declared this turn (for Windbrisk Heights etc.). */
    public final Map<UUID, Integer> creaturesAttackedCountThisTurn = new ConcurrentHashMap<>();
    /** Distinct permanents each player attacked with this turn; an additional combat's repeat attacker counts once. */
    public final Map<UUID, Set<UUID>> creaturesAttackedWithThisTurn = new ConcurrentHashMap<>();
    public final Set<UUID> permanentsThatAttackedBattlesThisTurn = ConcurrentHashMap.newKeySet();
    /** Cumulative count of attacking creatures by subtype each player declared this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> creaturesAttackedCountBySubtypeThisTurn = new ConcurrentHashMap<>();
    /**
     * Result of each player's most recent clash, keyed by the clashing player's id. Written by the
     * clash-source effect ({@code ClashEffect}) and read within the same spell/ability resolution by
     * the {@code WonClash} condition (e.g. Whirlpool Whelm's "if you win, ..." clause).
     */
    public final Map<UUID, Boolean> lastClashWonByController = new ConcurrentHashMap<>();
    /**
     * Imprinted cards (Mimic Vat, Semblance Anvil, Prototype Portal, ...), keyed by the
     * imprinting card's id. Lives on GameData rather than as a field on {@link Card} so that
     * AI simulation copies (which share Card instances with the real game) can't leak a
     * simulated imprint into the real game. Keyed by card id (not permanent id) because
     * imprint-consuming abilities may resolve after the source permanent left the battlefield
     * (e.g. Hoarding Dragon's death trigger, Clone Shell's sacrifice ability).
     */
    public final Map<UUID, Card> imprintedCards = new ConcurrentHashMap<>();
    /** Perpetual ETB abilities granted to cards, keyed by the card's stable id. */
    public final Map<UUID, List<CardEffect>> perpetualEnterEffectsByCardId = new ConcurrentHashMap<>();
    /** Perpetual power/toughness modifiers granted to physical cards, keyed by stable card id. */
    public final Map<UUID, CardPowerToughnessModifier> perpetualCardPowerToughnessModifiers =
            new ConcurrentHashMap<>();
    /**
     * A perpetual base-power/base-toughness setter, retaining its effect timestamp across zones. A
     * {@code null} component is left to the card's own value (a power-only exchange).
     */
    public record PerpetualBasePowerToughness(Integer power, Integer toughness, long timestamp) {
    }

    /** Mana spent to cast a countered spell and that spell's mana value at the time it was on the stack. */
    public record CounteredSpellManaCost(int manaSpent, int manaValue) {
    }
    /**
     * Perpetual base-power/base-toughness setters keyed by physical card identity, each with the timestamp of the
     * effect that set it. They apply as base power and toughness (CR 613.4b) to the card on the battlefield and
     * in every other zone.
     */
    public final Map<UUID, PerpetualBasePowerToughness> perpetualCardBasePowerToughness =
            new ConcurrentHashMap<>();
    /** Perpetual static effects granted to physical cards while they are on the battlefield. */
    public final Map<UUID, List<CardEffect>> perpetualCardBattlefieldEffectGrants = new ConcurrentHashMap<>();
    /** Perpetual valued spell-casting abilities granted to physical cards, keyed by card id. */
    public final Map<UUID, List<SpellCastingAbilityGrantingEffect>> perpetualCardSpellCastingAbilityGrants =
            new ConcurrentHashMap<>();
    /** Perpetual generic spell-cost reductions granted to physical cards, keyed by stable card id. */
    public final Map<UUID, Integer> perpetualCardCastCostReductions = new ConcurrentHashMap<>();
    /** Perpetual generic spell-cost increases granted to physical cards, keyed by stable card id. */
    public final Map<UUID, Integer> perpetualCardCastCostIncreases = new ConcurrentHashMap<>();
    /** Perpetual power/toughness changes keyed by the affected card's identity. */
    public final Map<UUID, com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier> perpetualPowerToughnessModifiers =
            new ConcurrentHashMap<>();
    /** Perpetual keyword grants keyed by the affected card's identity. */
    public final Map<UUID, Set<Keyword>> perpetualKeywords = new ConcurrentHashMap<>();
    /** Total toxic value perpetually gained by a card (each grant adds 1), keyed by the card's identity. */
    public final Map<UUID, Integer> perpetualToxicValues = new ConcurrentHashMap<>();
    /** Perpetual generic cast-cost increases keyed by the affected card's identity. */
    public final Map<UUID, Integer> perpetualGenericCastCostIncreases = new ConcurrentHashMap<>();
    /** Perpetual colored mana-cost additions keyed by the affected card's identity. */
    public final Map<UUID, ManaCost> perpetualManaCostIncreases = new ConcurrentHashMap<>();
    /** Perpetual noncombat damage bonuses keyed by the affected card's identity, one per grant. */
    public final Map<UUID, List<DamageBonusGrant>> perpetualNoncombatDamageBonuses = new ConcurrentHashMap<>();
    /** Cards that perpetually let their controller spend mana as any color to cast them. */
    public final Set<UUID> perpetualAnyColorManaForCastCardIds = ConcurrentHashMap.newKeySet();
    /** Cards that perpetually enter the battlefield tapped, keyed by card identity. */
    public final Set<UUID> perpetualEnterTappedCardIds = ConcurrentHashMap.newKeySet();
    /** Cards that perpetually exile themselves instead of dying, keyed by card identity. */
    public final Set<UUID> perpetualExileInsteadOfDyingCardIds = ConcurrentHashMap.newKeySet();
    /** Perpetual triggered abilities keyed by the affected card's identity. */
    public final Map<UUID, List<CardEffect>> perpetualTriggeredAbilities = new ConcurrentHashMap<>();
    /** Perpetually removed printed triggered abilities keyed by the affected card's identity. */
    public final Map<UUID, Set<CardEffect>> perpetuallyRemovedTriggeredAbilities = new ConcurrentHashMap<>();
    /**
     * Perpetually granted abilities keyed by the affected card's identity, each an immutable list
     * in grant order; reapplied when the card enters the battlefield. Change them through
     * {@link #grantPerpetualAbility} and {@link #removePerpetualAbilitiesIf}.
     */
    public final Map<UUID, List<Ability>> perpetualTriggeredAbilityGrants = new ConcurrentHashMap<>();
    /** Perpetual graveyard-activated abilities keyed by the affected card's identity. */
    public final Map<UUID, List<ActivatedAbility>> perpetualGraveyardAbilities = new ConcurrentHashMap<>();
    /** Perpetual card types keyed by the affected card's identity. */
    public final Map<UUID, Set<CardType>> perpetualCardTypes = new ConcurrentHashMap<>();
    /** Perpetual card subtypes keyed by the affected card's identity. */
    public final Map<UUID, Set<CardSubtype>> perpetualCardSubtypes = new ConcurrentHashMap<>();
    /** Cards that perpetually became a creature type, so their printed creature types no longer apply. */
    public final Set<UUID> perpetualCreatureTypeReplacedCardIds = ConcurrentHashMap.newKeySet();
    /** Perpetual activated abilities keyed by the affected card's identity. */
    public final Map<UUID, List<ActivatedAbility>> perpetualActivatedAbilities = new ConcurrentHashMap<>();
    /** Perpetual Evoke alternate costs keyed by the affected card's identity. */
    public final Map<UUID, List<AlternateHandCast>> perpetualEvokeAlternateCasts = new ConcurrentHashMap<>();
    /** Perpetual power bonuses attached to individual card identities, keyed by card id. */
    public final Map<UUID, Integer> perpetualCardPowerModifiers = new ConcurrentHashMap<>();
    /** Perpetually removed keywords attached to individual card identities, keyed by card id. */
    public final Map<UUID, Set<Keyword>> perpetualCardRemovedKeywords = new ConcurrentHashMap<>();
    /**
     * Additional instances of a stackable triggered keyword (battle cry, exalted, …) perpetually granted
     * to a card that already had it, keyed by card id. The first instance lives in the card's keywords.
     */
    public final Map<UUID, Map<Keyword, Integer>> perpetualCardExtraKeywordInstances = new ConcurrentHashMap<>();
    /** Card identities that have perpetually gained unearth. */
    public final Set<UUID> cardsGrantedPerpetualUnearth = ConcurrentHashMap.newKeySet();
    /** Keyword or ability word chosen for a card's Legacy pregame ability, keyed by card id. */
    public final Map<UUID, String> legacyChosenWordsByCardId = new ConcurrentHashMap<>();
    /** The mission index each Secret Mission card's owner secretly chose before the game, keyed by card id. */
    public final Map<UUID, Integer> secretMissionChoices = new ConcurrentHashMap<>();
    /**
     * Type and amount of mana noted on a permanent (Ice Cauldron — "note the type and amount of
     * mana spent to pay this activation cost"), keyed by the noting card's id like
     * {@link #imprintedCards}. Overwritten by each new note.
     */
    public final Map<UUID, Map<ManaColor, Integer>> notedMana = new ConcurrentHashMap<>();
    /**
     * Mana that was just spent to activate an ability, per color, keyed by the source permanent's
     * card id. Written by {@code AbilityActivationService} at activation time so a "note the mana
     * spent to pay this activation cost" effect can read it when the ability resolves.
     */
    public final Map<UUID, Map<ManaColor, Integer>> abilityActivationManaSpent = new ConcurrentHashMap<>();
    /** Amount of Treasure-produced mana spent to activate an ability, keyed by source card id. */
    public final Map<UUID, Integer> abilityActivationTreasureManaSpent = new ConcurrentHashMap<>();
    /** Whether mana produced by a Treasure was spent on each most recently activated ability. */
    public final Map<UUID, Boolean> abilityActivationUsedTreasureMana = new ConcurrentHashMap<>();
    public final Map<UUID, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
    /**
     * Phased-out permanents (CR 702.26b), keyed by the player who controlled them when they phased
     * out. They are held here instead of on a battlefield precisely because a phased-out permanent
     * "is treated as though it does not exist" — keeping them out of {@link #playerBattlefields} is
     * what makes every battlefield query, continuous effect and state-based action ignore them
     * without a special case. Phasing is not a zone change (CR 702.26d): the {@code Permanent}
     * objects (with their counters, attachments and damage) are moved back verbatim when they phase
     * in, and no leave/enter-the-battlefield trigger fires either way.
     */
    public final Map<UUID, List<Permanent>> phasedOutPermanents = new ConcurrentHashMap<>();
    /** Directly phased-out permanent ids that remain phased out while their source stays controlled. */
    public final Map<UUID, Map<UUID, Integer>> phasedOutWhileSourceControlled = new ConcurrentHashMap<>();
    /** Directly phased-out permanent ids that remain phased out while their source stays tapped. */
    public final Map<UUID, Set<UUID>> phasedOutWhileSourceTapped = new ConcurrentHashMap<>();
    public final Map<UUID, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<UUID, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    /**
     * Players who let the game put their simultaneous triggered abilities on the stack in the order
     * they triggered instead of being asked for an order (CR 603.3b leaves the order to the player).
     */
    public final Set<UUID> playersAutoOrderingTriggers = ConcurrentHashMap.newKeySet();
    /**
     * Player ids controlled by an AI opponent. Auto-pass must always hand these players a
     * priority window whenever they can act (so the AI can respond at instant speed), whereas
     * human players are auto-passed through any step outside their configured auto-stop set.
     */
    public final Set<UUID> aiPlayerIds = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerPoisonCounters = new ConcurrentHashMap<>();
    /** Rad counters are held by players and resolve during their precombat main phase. */
    public final Map<UUID, Integer> playerRadCounters = new ConcurrentHashMap<>();
    /** Players for whom Melira's poison replacement effect has already applied this turn. */
    public final Set<UUID> playersAffectedByMeliraPoisonReplacementThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerEnergyCounters = new ConcurrentHashMap<>();
    /** Energy counters each player has paid or lost during the current turn. */
    public final Map<UUID, Integer> energyCountersPaidOrLostThisTurn = new ConcurrentHashMap<>();
    /** Spark counters are held by players and pay for spark abilities. */
    public final Map<UUID, Integer> playerSparkCounters = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerExperienceCounters = new ConcurrentHashMap<>();
    /** Persistent coolness values, measured in percentage points. */
    public final Map<UUID, Integer> playerCoolness = new ConcurrentHashMap<>();
    /** Persistent speed values; absent means the player has not started their engines. */
    public final Map<UUID, Integer> playerSpeeds = new ConcurrentHashMap<>();
    /** Players whose speed has already increased during the current turn. */
    public final Set<UUID> playersWhoseSpeedIncreasedThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who used Highway Reaver's max-speed free unearth this turn. */
    public final Set<UUID> playersWhoUsedMaxSpeedFreeUnearthThisTurn = ConcurrentHashMap.newKeySet();
    public final InteractionState interaction = new InteractionState(this::takeTriggerRecordInHand,
            this::reportInteractionWithoutTriggerRecord);
    public final List<StackEntry> stack = Collections.synchronizedList(new TriggerAwareStackList(this));
    /** CR 603.3 — triggers from mana-ability sacrifices wait here until the next time a player
     *  would receive priority, so they don't block sorcery-speed spell casting. */
    public final List<StackEntry> pendingManaAbilityTriggers = Collections.synchronizedList(new ArrayList<>());
    /** Triggered abilities collected while a spell's casting costs are being paid. */
    public final List<StackEntry> pendingSpellCastCostTriggers = Collections.synchronizedList(new ArrayList<>());
    /** Triggered abilities collected while an activated ability's discard cost is being paid. */
    public final List<StackEntry> pendingActivatedAbilityCostTriggers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Set while a multi-card discard cost is being paid one card at a time: the cards are put into the
     * graveyard simultaneously (CR 601.2h), so a rules event batch stays open until the last card.
     */
    public boolean discardCostBatchOpen;
    /** Spellbook cards drafted but not yet put onto the battlefield (The Hourglass Coven). */
    public final List<Card> heldSpellbookCards = Collections.synchronizedList(new ArrayList<>());
    /** Permanents chosen to be sacrificed for the activated ability whose costs are being paid. They
     *  stay on the battlefield until every cost choice is made, then leave together with any
     *  sacrificed source as one simultaneous event (CR 603.10a look-back for their dies triggers). */
    public final List<UUID> pendingActivationCostSacrificeIds = Collections.synchronizedList(new ArrayList<>());
    /** Mana-ability activations that can still be undone by the MTGO-style "cancel casting" UI
     *  (untap the source, drain the produced mana). Cleared whenever a non-mana action happens
     *  (casting, non-mana ability activation, passing priority, step change); each entry is also
     *  re-validated against the pool at revert time. Deliberately NOT copied by
     *  {@link #simulationCopy()} — the AI never reverts, and the entries reference live
     *  {@link StackEntry} objects from this game (enforced by {@code SimulationCopyCompletenessTest}'s
     *  deliberately-not-copied list, which is what keeps a bulk "copy everything" sweep from
     *  quietly reinstating it). */
    public final List<ManaActivation> revertableManaActivations = Collections.synchronizedList(new ArrayList<>());
    /** A revertable mana activation waiting on its colour choice; completed into
     *  {@link #revertableManaActivations} once the chosen colour lands in the pool. Never outlives
     *  that one prompt: the answer handler consumes it unconditionally, and a pending colour choice
     *  blocks every other action, so nothing can intervene. Not copied by {@link #simulationCopy()}
     *  — it is scratch state spanning a single prompt, and the AI never reverts. */
    public PendingManaActivation pendingRevertableManaActivation;
    /** CR 603.2 / 603.3 — depth counter for nested mana-ability resolution. While > 0,
     *  triggered abilities that fire from effects resolving inside a mana ability (e.g. a life-gain
     *  effect triggering Sanguine Bond) route to {@link #pendingManaAbilityTriggers} instead of the
     *  main stack. Incremented/decremented in a try/finally pair around mana-ability resolution. */
    public int manaAbilityResolutionDepth;
    /**
     * Set while several tokens from one effect are entering together. "One or more creatures enter"
     * boons then wait for the whole batch instead of firing for the first token alone, so their
     * controller can choose among every creature that entered.
     */
    public boolean deferCreatureEntersBoons;
    private int activeTriggeredAbilityCopies = 1;
    public final Map<UUID, List<Card>> playerGraveyards = new ConcurrentHashMap<>() {
        @Override public List<Card> put(UUID id, List<Card> cards) {
            return super.put(id, new CommanderGraveyardList(id, cards));
        }
        @Override public List<Card> putIfAbsent(UUID id, List<Card> cards) {
            return super.putIfAbsent(id, new CommanderGraveyardList(id, cards));
        }
        @Override public List<Card> computeIfAbsent(UUID id, Function<? super UUID, ? extends List<Card>> factory) {
            return super.computeIfAbsent(id, key -> new CommanderGraveyardList(key, factory.apply(key)));
        }
        @Override public void putAll(Map<? extends UUID, ? extends List<Card>> values) { values.forEach(this::put); }
    };
    /** Latest graveyard-entry identity for each card, used by effects that require continuous graveyard presence. */
    public final Map<UUID, Long> graveyardEntryVersions = new ConcurrentHashMap<>();
    public final Map<UUID, Long> exileEntryVersions = new ConcurrentHashMap<>();
    /** Number of times each card has entered a hand through {@link #addCardToHand}. A card that returns to hand is a new object (CR 400.7), so a delayed effect that refers to an earlier hand entry checks this version. */
    public final Map<UUID, Long> handEntryVersions = new ConcurrentHashMap<>();
    private long graveyardEntryVersion;
    /** Cards in each player's command zone (CR 903.6). Used by Eminence and similar command-zone abilities. */
    public final Map<UUID, List<Card>> playerCommandZones = new CommandZoneMap();
    /** Command-zone cards that have been turned face down by a resolving ability. */
    public final Set<UUID> faceDownCommandZoneCards = ConcurrentHashMap.newKeySet();
    /**
     * Each player's scheme deck, top card first. The cards also stay in {@link #playerCommandZones}
     * and are face down, because a scheme deck is part of the command zone (CR 904.4).
     */
    public final Map<UUID, List<Card>> playerSchemeDecks = new ConcurrentHashMap<>();
    /**
     * The permanent view each face-up scheme triggers from, by card id, so the scheme keeps one
     * source id while it stays face up. Rebuilt lazily, so copies start empty.
     */
    private final Map<UUID, Permanent> faceUpSchemeSourceViews = new ConcurrentHashMap<>();
    /** Cards currently designated as each player's commander, including commanders on the battlefield. */
    public final Map<UUID, List<Card>> playerCommanders = new ConcurrentHashMap<>();
    /** Commander tax by card identity. */
    public final Map<UUID, Integer> commanderTaxByCardId = new ConcurrentHashMap<>();
    /** Number of times each player has cast a commander from the command zone this game. */
    public final Map<UUID, Integer> commanderCastsFromCommandZoneThisGame = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Tracks all non-token card IDs (any type) put into each player's graveyard from the battlefield this turn (e.g. Twilight Shepherd). */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Counts artifacts put into graveyards from the battlefield during this turn. */
    public int artifactsPutIntoGraveyardFromBattlefieldThisTurn;
    /** Tracks all non-token card IDs put into each player's graveyard from any zone this turn (e.g. Garna, the Bloodflame). */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromAnywhereThisTurn = new ConcurrentHashMap<>();
    /** Per player, how many times a card already counted in {@link #cardsPutIntoGraveyardFromAnywhereThisTurn} entered a graveyard again this turn (each entry is a new object, CR 400.7). */
    public final Map<UUID, Integer> cardsPutIntoGraveyardAgainThisTurn = new ConcurrentHashMap<>();
    /** Tracks non-token card IDs put into each player's graveyard from a library this turn. */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromLibraryThisTurn = new ConcurrentHashMap<>();
    /** Tracks non-token card IDs milled into each player's graveyard this turn (CR 701.17a). */
    public final Map<UUID, Set<UUID>> cardsMilledThisTurn = new ConcurrentHashMap<>();
    /** Tracks card IDs put into each player's graveyard by surveil this turn. */
    public final Map<UUID, Set<UUID>> cardsSurveilledThisTurn = new ConcurrentHashMap<>();
    /** Tracks card IDs put into each player's hand this turn. */
    public final Map<UUID, Set<UUID>> cardsPutIntoHandThisTurn = new ConcurrentHashMap<>();
    /** Tracks non-token card IDs put into each player's graveyard from hand this turn. */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromHandThisTurn = new ConcurrentHashMap<>();
    /**
     * Tracks non-token card IDs put into each player's graveyard from their hand or library this turn.
     * Unlike {@link #cardsPutIntoGraveyardFromLibraryThisTurn}, entries are never pruned when a card later
     * leaves the graveyard, because the count is of events that happened this turn (Welcome the Dead).
     */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromHandOrLibraryThisTurn = new ConcurrentHashMap<>();
    /** Tracks non-token creature card IDs put into graveyards from any zone this turn. */
    public final Map<UUID, Set<UUID>> creatureCardsPutIntoGraveyardFromAnywhereThisTurn = new ConcurrentHashMap<>();
    /** Number of times creature cards were put into each owner's graveyard this turn (repeats count). */
    public final Map<UUID, Integer> creatureCardGraveyardEntriesThisTurn = new ConcurrentHashMap<>();
    /** Counts land-card arrivals even when no land-graveyard trigger source is present. */
    public final Map<UUID, Integer> landCardGraveyardEntriesThisTurn = new ConcurrentHashMap<>();
    /** Players who put a permanent card into their graveyard from anywhere this turn. */
    public final Set<UUID> playersWhoDescendedThisTurn = ConcurrentHashMap.newKeySet();
    /** Counts each permanent card put into each player's graveyard from anywhere this turn. */
    public final Map<UUID, Integer> descentsThisTurn = new ConcurrentHashMap<>();
    /** Tracks non-token card IDs put into each player's graveyard during the current combat phase. */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardThisCombat = new ConcurrentHashMap<>();
    /** Non-token card IDs exiled from a graveyard during this turn. */
    public final Set<UUID> cardsExiledFromGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose noncreature permanents were destroyed by an opponent's spell or ability this turn. */
    public final Set<UUID> playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn = ConcurrentHashMap.newKeySet();
    /** Tracks card IDs each player cycled or discarded this turn (populated in the central discard hook
     *  {@link #discarded(UUID, Card, boolean)}; cycling is a discard). Used by Shadow of the Grave. */
    public final Map<UUID, Set<UUID>> cardsDiscardedOrCycledThisTurn = new ConcurrentHashMap<>();
    /** Players whose hands received a permanent from the battlefield this turn. */
    public final Set<UUID> playersWhoReceivedPermanentFromBattlefieldToHandThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Tracks card IDs each player discarded because of an opponent's spell or ability this turn. */
    public final Map<UUID, Set<UUID>> cardsDiscardedByOpponentThisTurn = new ConcurrentHashMap<>();
    /** Players who controlled a permanent that left the battlefield this turn. */
    public final Set<UUID> playersWhosePermanentsLeftBattlefieldThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Counts creatures that left the battlefield this turn, per controller. */
    public final Map<UUID, Integer> creatureLeftBattlefieldCountThisTurn = new ConcurrentHashMap<>();
    /** Whether any nonland permanent left the battlefield this turn. */
    public boolean nonlandPermanentLeftBattlefieldThisTurn;
    /**
     * When non-null, the card with this ID is currently being put into a graveyard as the discard cost
     * of activating a cycling ability. Read by {@link com.github.laxika.magicalvibes.model.effect.OwnGraveyardExileReplacement}
     * (Abandoned Sarcophagus "and it wasn't cycled").
     */
    public UUID cardEnteringGraveyardByCycling;
    /** Counts all creature deaths (including tokens) from battlefield this turn, per controller. */
    public final Map<UUID, Integer> creatureDeathCountThisTurn = new ConcurrentHashMap<>();
    /** Last-known battlefield names of creatures that died this turn, including tokens. */
    public final Set<String> creatureNamesDiedThisTurn = ConcurrentHashMap.newKeySet();
    /** Battlefield incarnations of creatures that actually died during this turn. */
    public final Set<UUID> creaturePermanentIdsDiedThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who controlled a modified creature when it died this turn. */
    public final Set<UUID> playersWhoControlledModifiedCreatureDiedThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> creaturesPutIntoOwnGraveyardThisTurnCount = new ConcurrentHashMap<>();
    /** Counts nontoken creatures put into each owner's graveyard from the battlefield this turn. */
    public final Map<UUID, Integer> nontokenCreaturesPutIntoOwnGraveyardThisTurnCount = new ConcurrentHashMap<>();
    /** Counts nontoken creature deaths from the battlefield this turn, per controller. */
    public final Map<UUID, Integer> nontokenCreatureDeathCountThisTurn = new ConcurrentHashMap<>();
    /** Counts creatures exiled from the battlefield by controller this turn. */
    public final Map<UUID, Integer> creatureExileCountThisTurn = new ConcurrentHashMap<>();
    /** Counts creature deaths by effective creature subtype and controller this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> creatureSubtypeDeathCountThisTurn = new ConcurrentHashMap<>();
    /** Sums the last-known effective power of creature deaths by effective subtype and controller this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> creatureSubtypeDeathPowerThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnBySourcePermanent = new ConcurrentHashMap<>();
    /**
     * Source permanent ids whose resolved ability gave them "if a creature dealt damage by this creature this turn
     * would die, exile it instead" (Runesword). The effect outlives the source leaving the battlefield.
     */
    public final Set<UUID> exileDamagedCreaturesSourceIdsThisTurn = ConcurrentHashMap.newKeySet();
    /** Source object id (permanent or spell card) → creature card ids it damaged this turn. */
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnBySource = new ConcurrentHashMap<>();
    /** Controller id → creature card ids damaged this turn by a source that controller controlled at the time of damage. */
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnByController = new ConcurrentHashMap<>();
    /** Permanent IDs that dealt damage to another creature this turn; survives that creature leaving the battlefield. */
    public final Set<UUID> sourcesThatDealtDamageToCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /**
     * Source permanent ids that dealt damage to a creature which later died this turn (Krovikan Vampire
     * intervening-if). Survives the card leaving the graveyard; cleared at turn cleanup.
     */
    public final Set<UUID> sourcesWhoseDamagedCreaturesDiedThisTurn = ConcurrentHashMap.newKeySet();
    /**
     * Source permanent id → creature card ids it damaged that died this turn and are still continuously
     * in a graveyard (Krovikan Vampire return). Pruned when a card leaves any graveyard.
     */
    public final Map<UUID, Set<UUID>> creatureCardsDamagedBySourceThatDiedThisTurn = new ConcurrentHashMap<>();
    /** Permanents each exploit source sacrificed as its exploit ability resolved, keyed by the source permanent id. */
    public final Map<UUID, Set<UUID>> exploitedPermanentIdsBySource = new ConcurrentHashMap<>();
    /** Seraph: source Seraph permanent id → permanent ids of the creatures it returned under a player's control. */
    public final Map<UUID, Set<UUID>> seraphReturnedCreatures = new ConcurrentHashMap<>();
    /** Seraph: source Seraph permanent id → the player who last controlled it, watched for control-loss sacrifices. */
    public final Map<UUID, UUID> seraphControlWatch = new ConcurrentHashMap<>();
    /** Seraph: source Seraph permanent id → the player ("you") who controlled the return ability; only that player's loss of control sacrifices. */
    public final Map<UUID, UUID> seraphLinkedController = new ConcurrentHashMap<>();
    /**
     * Gustha's Scepter: source permanent id → the player who last controlled it. When the
     * controller changes or the permanent leaves the battlefield, every card exiled with it is put
     * into its owner's graveyard ("When you lose control of this artifact, ...").
     */
    public final Map<UUID, ExiledCardsControlLossWatch> exiledCardsToGraveyardOnControlLossWatch =
            new ConcurrentHashMap<>();
    /**
     * Debt of Loyalty: permanent id → the player who gains control of it because its regeneration
     * shield was just spent. Regeneration happens inside the state-based-action sweep, which is
     * iterating the battlefields, so the control change (which moves the permanent between
     * battlefield lists) is queued here and applied once that sweep is done.
     */
    public final Map<UUID, UUID> pendingRegenerationControlChanges = new ConcurrentHashMap<>();
    /** Source permanent id → ids of the tokens created with it ("tokens created with this permanent"; Tetravus, Tombstone Stairwell). */
    public final Map<UUID, Set<UUID>> sourceCreatedTokens = new ConcurrentHashMap<>();
    /** Unified exile zone: every exiled card with its owner and optional source permanent. */
    public final List<ExiledCardEntry> exiledCards = Collections.synchronizedList(new ExileZoneList());
    /** Original card objects replaced by Bombardment until cleanup. */
    public final Map<UUID, Card> bombardmentOriginalCardsUntilEndOfTurn = new ConcurrentHashMap<>();
    /** Number of cards put into exile during the current turn. */
    public int cardsExiledThisTurn;
    /** Whether any card has been put into exile during the current turn. */
    public boolean cardPutIntoExileThisTurn;
    /** Greatest mana value noted by each permanent that observes cards entering exile this turn. */
    public final Map<UUID, Integer> greatestManaValueNotedForPermanentThisTurn = new ConcurrentHashMap<>();
    /** Number of distinct spells or abilities that caused a guess or pile grouping this turn. */
    public int pileGroupingOrGuessCountThisTurn;
    /** Exiled haunting card UUID -> the permanent UUID it haunts. */
    public final Map<UUID, UUID> hauntingCardToPermanentId = new ConcurrentHashMap<>();
    /** Exiled Cosima card UUID → voyage counters accumulated while it remains exiled. */
    public final Map<UUID, Integer> exiledVoyageCounters = new ConcurrentHashMap<>();
    /** Exiled Cosima card UUID → controller of the ability that exiled it. */
    public final Map<UUID, UUID> exiledVoyageControllerIds = new ConcurrentHashMap<>();
    /** Cards exiled by the foretell special action and therefore castable for their foretell cost. */
    public final Set<UUID> foretoldCardIds = ConcurrentHashMap.newKeySet();
    /** Players who have used the foretell special action during the current turn. */
    public final Set<UUID> playersWhoForetoldThisTurn = ConcurrentHashMap.newKeySet();
    /** Foretell costs assigned when cards were granted foretell dynamically. */
    public final Map<UUID, ManaCost> foretoldCardCosts = new ConcurrentHashMap<>();
    /** Every player's turns-begun count when a card was foretold, so a later controller can count turns since. */
    public final Map<UUID, Map<UUID, Integer>> foretoldCardTurnsTaken = new ConcurrentHashMap<>();
    /** Card IDs currently represented as cards in the ante zone. Ante is modelled through exile. */
    public final Set<UUID> antedCardIds = ConcurrentHashMap.newKeySet();
    /** Tapped Time Vaults whose controller declined the turn-start skip choice since the last turn began.
     *  A declined Vault is not offered again before the turn starts. */
    public final Set<UUID> timeVaultsDeclinedThisTurnStart = ConcurrentHashMap.newKeySet();
    /** Players who accepted Rebirth's ante choice; their ante and life change wait until every player has decided (CR 608.2e). */
    public final Set<UUID> rebirthAnteAcceptedPlayerIds = ConcurrentHashMap.newKeySet();
    /** Maps exiled card UUID → egg counter count (for Darigaaz Reincarnated-style effects). */
    public final Map<UUID, Integer> exiledCardEggCounters = new ConcurrentHashMap<>();
    /** Maps exiled card UUID → scream counter count (for All Hallow's Eve-style effects). */
    public final Map<UUID, Integer> exiledCardScreamCounters = new ConcurrentHashMap<>();
    /** Maps suspended exiled card UUID → remaining time counter count. */
    public final Map<UUID, Integer> exiledCardTimeCounters = new ConcurrentHashMap<>();
    /** Exiled card UUIDs with time counters that do not have suspend's automatic upkeep trigger. */
    public final Set<UUID> exiledCardsWithNonSuspendTimeCounters = ConcurrentHashMap.newKeySet();
    /** Alaundo's last-time-counter casting ability, retained for as long as the card stays exiled. */
    public final Set<UUID> exiledCardsWithAlaundoCastAbility = ConcurrentHashMap.newKeySet();
    /** Maps exiled card UUID → dream counter count (Goliath Daydreamer). */
    public final Map<UUID, Integer> exiledCardDreamCounters = new ConcurrentHashMap<>();
    /** Maps exiled card UUID → hit counter count (Etrata, the Silencer). */
    public final Map<UUID, Integer> exiledCardHitCounters = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> exiledCardRefineCounters = new ConcurrentHashMap<>();
    /** Spells whose successful resolution is replaced by exile with a dream counter. */
    public final Set<UUID> spellsWithDreamCounterOnResolution = ConcurrentHashMap.newKeySet();
    public final Set<UUID> spellsWithPlotOnResolution = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have silver counters (Karn, Scion of Urza). */
    public final Set<UUID> exiledCardsWithSilverCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have ice counters (Draugr Necromancer). */
    public final Set<UUID> exiledCardsWithIceCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have blood counters (Rayami, First of the Fallen). */
    public final Set<UUID> exiledCardsWithBloodCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have croak counters (Grolnok, the Omnivore). */
    public final Set<UUID> exiledCardsWithCroakCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have void counters (Dauthi Voidwalker). */
    public final Set<UUID> exiledCardsWithVoidCounters = ConcurrentHashMap.newKeySet();
    /** Exiled cards marked with discovery counters. */
    public final Set<UUID> exiledCardsWithDiscoveryCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have collection counters (Evelyn, the Covetous). */
    public final Set<UUID> exiledCardsWithCollectionCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have fetch counters (Pako, Arcane Retriever). */
    public final Set<UUID> exiledCardsWithFetchCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have hatching counters (The Dragon-Kami Reborn). */
    public final Set<UUID> exiledCardsWithHatchingCounters = ConcurrentHashMap.newKeySet();
    public final Set<UUID> exiledCardsWithStudyCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have intel counters (Flamewar, Streetwise Operative). */
    public final Set<UUID> exiledCardsWithIntelCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have kick counters (Zethi, Arcane Blademaster). */
    public final Set<UUID> exiledCardsWithKickCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled creature card UUIDs that have memory counters (Altaïr Ibn-La'Ahad). */
    public final Set<UUID> exiledCardsWithMemoryCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled creature card UUIDs that have takeover counters (The Master, Formed Anew). */
    public final Set<UUID> exiledCardsWithTakeoverCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have brain counters (Rex, Cyber-Hound). */
    public final Set<UUID> exiledCardsWithBrainCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have cage counters (Mairsil, the Pretender). */
    public final Set<UUID> exiledCardsWithCageCounters = ConcurrentHashMap.newKeySet();
    /** Spells exiled with delay counters and waiting to go back onto the stack (Ertai's Meddling). */
    public final List<DelayedSpellExile> delayedSpellExiles = Collections.synchronizedList(new ArrayList<>());
    /**
     * Spells exiled with suspend counters and waiting for their owners' upkeeps. Effects that add
     * or remove time counters on suspended cards apply to these as well as to cards suspended from
     * a hand; removing the last counter queues the owner's optional free cast (CR 702.62a).
     */
    public final List<SuspendedSpellExile> suspendedSpellExiles = Collections.synchronizedList(new ArrayList<>());
    /**
     * "All creatures with [counter] counters on them block that creature this turn if able": the counter
     * types per attacking creature. The requirement is not a characteristic change, so it applies to every
     * creature that has such a counter whenever blockers are declared (CR 611.2c), not just to those that had
     * one when the ability resolved.
     */
    /** When true, all damage to all creatures (both players') is prevented this turn (Blinding Fog). */
    /**
     * The "one or more" abilities, by source, already put on the stack for the current simultaneous creature-entry
     * event: a later permanent of the event joins that ability's stack object instead of triggering it again.
     */
    public final Map<UUID, Set<TriggeredAbility>> simultaneousCreatureEntryTriggers = new HashMap<>();
    public boolean collectingSimultaneousCreatureEntryTriggers;
    /** Permanents put onto the battlefield together whose enter abilities are still being processed. An
     *  as-enters choice of one of them (Rescuer Sphinx) can't pick another, as they weren't there yet. */
    public final Set<UUID> permanentsEnteringSimultaneously = ConcurrentHashMap.newKeySet();

    /** When non-null, creatures NOT matching this predicate are prevented from dealing combat damage this turn. */
    public PermanentPredicate combatDamageExemptPredicate;
    public UUID combatDamageExemptControllerId;
    /** Opponents chosen for a simultaneous attacking-token-copy event, keyed by its choice batch. */
    public final Map<UUID, List<UUID>> pendingAttackingCopyOpponents = new ConcurrentHashMap<>();
    /** Remaining independent opponent choices for each attacking-token-copy event. */
    public final Map<UUID, Integer> pendingAttackingCopyChoices = new ConcurrentHashMap<>();
    /** Counts already applied to members of the current simultaneous enchantment creature entry event. */
    public final Map<UUID, Integer> activeAdditionalCountersForEnchantmentCreatureEntryBatch =
            new ConcurrentHashMap<>();
    public final List<PendingMayAbility> pendingMayAbilities = new ObservedMayAbilityList();
    public PendingGemstoneCavernsChoice pendingGemstoneCavernsChoice;
    /** Tariff: players (APNAP order) still to be processed after the one currently being resolved. */
    public final List<UUID> tariffRemainingPlayers = new ArrayList<>();
    /**
     * Tariff: creatures whose controllers declined to pay, sacrificed together once every player has
     * decided (CR 101.4).
     */
    public final List<UUID> tariffPendingSacrificeIds = new ArrayList<>();
    /**
     * ForcedCostOrElse ({@code anyPlayerMayPay}): players still to be offered the pay prompt after
     * the one currently deciding (APNAP remainder). Cleared when someone pays or the queue drains.
     */
    public final List<UUID> forcedCostOrElseRemainingPlayers = new ArrayList<>();
    /**
     * ForcedCostOrElse ({@code anyPlayerMayPay}): controller of the source permanent when the
     * APNAP pay sequence began — used for the sacrifice/penalty synthetic entry after everyone
     * declines (the pending ability's {@code controllerId} is the deciding player, not the source).
     */
    public UUID forcedCostOrElseSourceControllerId;
    /** MayPayManaEffect ({@code ANY_PLAYER}): players still to be offered the payment in APNAP order. */
    public final List<UUID> anyPlayerMayPayManaRemainingPlayers = new ArrayList<>();
    /**
     * EachPlayerTakesDamageUnlessPays: players still to be offered the pay-or-take-damage prompt
     * after the one currently deciding (APNAP remainder). Cleared when the queue drains.
     */
    public final List<UUID> eachPlayerDamageUnlessPaysRemaining = new ArrayList<>();
    /**
     * Players (one entry per declined prompt) who declined to pay in the current pay-or-take-damage
     * queue; their damage is dealt together once every prompt has been answered.
     */
    public final List<UUID> eachPlayerDamageUnlessPaysDeclined = new ArrayList<>();

    /** True when declined damage is held until every prompt is answered (payers derived with repeats). */
    public boolean eachPlayerDamageUnlessPaysBatched;
    /** Progress state for Whirlwind Denial's opponent-by-opponent pay-or-counter sequence. */
    public final WhirlwindDenialState whirlwindDenial = new WhirlwindDenialState();
    /**
     * RevealHandDiscardMatchingCardsUnlessPaysLife: ids of the revealed cards still to be offered the
     * pay-or-discard prompt after the one currently being decided. Cleared when the queue drains.
     */
    public final List<UUID> revealHandDiscardUnlessPaysRemaining = new ArrayList<>();

    /** Remaining individual draws after an interactive replacement, in resolution order. */
    public final Deque<UUID> pendingCardDraws = new ArrayDeque<>();
    /** Draws produced by a draw replacement (e.g. "draw two cards instead") still to be performed. */
    public final Deque<UUID> pendingReplacementCardDraws = new ArrayDeque<>();
    /**
     * Draws a player still has to perform that already came out of a draw-doubling replacement they chose
     * to apply first, so draw doublers do not replace them again (CR 614.5). Consumed one per draw.
     */
    public final Map<UUID, Integer> drawsFromAppliedDoubling = new ConcurrentHashMap<>();
    /** Players whose draw currently being resolved came out of an already applied draw-doubling replacement. */
    public final Set<UUID> currentDrawDoublingApplied = ConcurrentHashMap.newKeySet();
    /** Optional replacement sources declined for the draw currently being resolved. */
    public final Map<UUID, Set<UUID>> declinedDrawReplacementSources = new ConcurrentHashMap<>();
    /** Preserves the turn-based draw exemption while an optional replacement is answered. */
    public final Map<UUID, Boolean> pendingDrawFirstDrawStepFlags = new ConcurrentHashMap<>();

    public PendingInteraction.DiscardChoice pendingDiscardToLibraryChoice;
    public int pendingDiscardToLibraryCardIndex = -1;
    public Boolean pendingDiscardToLibraryDecision;
    /**
     * DestroyCreaturesThatDamagedSourceUnlessControllerPaysLife: ids of the damaging creatures still
     * to be offered the pay-or-destroy prompt after the one currently being decided. Cleared when the
     * queue drains.
     */
    public final List<UUID> destroyDamagersUnlessPaysRemaining = new ArrayList<>();
    /**
     * ReturnMatchingPermanentsUnlessOwnerPays: ids of the matching permanents still to be
     * offered the pay-or-be-bounced prompt (each to that permanent's owner) after the one
     * currently being decided. Cleared when the queue drains.
     */
    public final List<UUID> bounceUnlessPaysRemaining = new ArrayList<>();
    public final GraveyardTargetOperationState graveyardTargetOperation = new GraveyardTargetOperationState();
    public final QueenKaylaBinKroogOperationState queenKaylaBinKroogOperation =
            new QueenKaylaBinKroogOperationState();
    public final CloneOperationState cloneOperation = new CloneOperationState();
    /** Batch whose native as-entry choices must finish before any of its cards change zones. */
    public PermanentChoiceContext.AuraEntryBatchChoice pendingBattlefieldEntryBatch;
    /** Answered and pending entry requests, keyed by the physical card's ID. */
    public final Map<UUID, BattlefieldEntryRequest> pendingBattlefieldEntryRequests = new ConcurrentHashMap<>();
    /**
     * Departure from the battlefield waiting for an answer before any of its permanents leave; it
     * resumes at its stage once the answer arrives. {@code null} when no departure is pending.
     */
    public DepartureBatch pendingDepartureBatch;
    /**
     * Answered departures of earlier events of the same damage event set aside so a later event could ask its own
     * questions (a combat damage step that asked the defending player's Lich's Duel Mastery before a blocker's
     * "sacrifice it instead"); each waits for its event to perform the same intents.
     */
    public final List<DepartureBatch> answeredEventDepartureBatches = new ArrayList<>();
    /** A game loss whose replacement waits for a departure's answers; {@code null} when none does. */
    public PendingLossReplacement pendingLossReplacement;
    /**
     * Entries that reached their "enter as a copy" choice while another copy choice was open, such as
     * the second of two Clones entering together. Each is offered its own choice once {@link #cloneOperation}
     * is free again.
     */
    public final List<PendingEntryCopyChoice> pendingEntryCopyChoices = new ArrayList<>();
    /**
     * Hand cards already chosen for an "any number of cards from your hand" put (Ghalta, Stampede
     * Tyrant). They stay in hand, out of the remaining choices, until the player stops choosing, then
     * all enter in one batch.
     */
    public final List<UUID> pendingAnyNumberHandPutCardIds = new ArrayList<>();
    /**
     * While a {@code PutCardsOntoBattlefieldTogetherEffect} resolves its steps, hand cards chosen to be
     * put onto the battlefield are held in {@link #heldHandBattlefieldPuts} (still in hand) instead of
     * entering, so they all enter together once every step has chosen.
     */
    public boolean holdingHandBattlefieldPuts;
    /** Whether held hand puts have already left the hand, to be returned to it just before they enter. */
    public boolean heldHandBattlefieldPutsLeaveHand;
    public final List<BattlefieldEntryCard> heldHandBattlefieldPuts = new ArrayList<>();
    /**
     * The spell or ability whose effects are resolving now, innermost first while one resolution runs inside another;
     * {@code null} between resolutions. A delayed triggered ability made while it resolves, by code that isn't
     * handed its stack entry, takes it as its source and controller (CR 603.7d).
     */
    public StackEntry resolvingEntry;
    public StackEntry pendingEffectResolutionEntry;
    public int pendingEffectResolutionIndex;
    /**
     * How many stack objects have begun to resolve this game: the identity of the resolution in progress, or of the
     * last one. A {@code DelayedPermanentAction} whose delayed triggered abilities are grouped is stamped with it as
     * it is scheduled ({@code DelayedPermanentAction#scheduledBy}), so the delayed triggered ability
     * one resolution creates (CR 603.7a) is told from the one another resolution of the same source creates.
     */
    public long resolutionOrdinal;
    /**
     * The resolutions parked while a nested one runs, innermost first: an optional action a resolving spell or
     * ability offered is carried out in that resolution (CR 608.2d), by an entry of its own that says who acts and
     * on what. {@code entry} resumes at {@code index} once {@code nested} has finished, also when the nested
     * resolution stopped for an answer in between ({@code EffectResolutionService.resolveNestedInParkedResolution}).
     */
    public final Deque<ParkedResolution> parkedOuterResolutions = new ArrayDeque<>();

    /** One element of {@link #parkedOuterResolutions}. */
    public record ParkedResolution(StackEntry entry, int index, StackEntry nested) {
    }
    /**
     * "If you don't" effect of the hand-card choice currently open for a "you may put a card from your hand
     * onto the battlefield; if you don't, ..." effect (Origin of the Avengers). It resolves when the player
     * declines the choice and is dropped once the choice is answered.
     */
    public StackEntry handCardChoiceDeclineEntry;
    /** Votes collected while a voting spell or ability is resolving, keyed by its controller. */
    public final Map<UUID, Map<UUID, Set<String>>> votingChoicesByController = new ConcurrentHashMap<>();
    /**
     * Additional votes each player has taken in the vote now resolving under "you may vote an additional
     * time"; a declined offer counts as every one of them taken, so it isn't offered again.
     */
    public final Map<UUID, Integer> additionalVotesTaken = new ConcurrentHashMap<>();
    /**
     * Answer to an additional-vote offer made by a vote that reruns its effect to read it (Truth or
     * Consequences); {@code null} while no answer is waiting.
     */
    public Boolean additionalVoteAccepted;
    public PendingReverseMiracleSearch pendingReverseMiracleSearch;
    /** CR 603.5 — set when a MayEffect is encountered during stack resolution, cleared after player responds. */
    public boolean resolvingMayEffectFromStack;
    /** CR 603.5 — stores the player's response to a resolution-time MayEffect (true=accepted, false=declined, null=no pending choice). */
    public Boolean resolvedMayAccepted;
    /**
     * The resolving entry that waits for a card its effect has its controller choose from a graveyard as it resolves
     * (CR 608.2d: "choose an instant or sorcery card in your graveyard", "you may cast a spell from your graveyard");
     * the choice's handler records the card on it and resumes it. Never a target: an ability's targets are chosen as
     * it is put on the stack (CR 603.3d).
     */
    public StackEntry resolvedMayTargetingEntry;
    public Integer chosenXValue;
    /**
     * The player whose library shuffles once the free cast that is currently being set up has put its
     * spell on the stack ("may cast that card without paying its mana cost. Then the player shuffles").
     */
    public UUID libraryToShuffleAfterFreeCast;
    /** Target awaiting a resolution-time damage-allocation answer for a divided damage effect. */
    public UUID pendingDividedDamageTargetId;
    public PendingAbilityCounterCostActivation pendingAbilityCounterCostActivation;
    /**
     * The counter kind the controller picked for a "remove a counter" activation cost, set only while
     * the suspended activation is re-entered after that choice and cleared once the cost is paid.
     */
    public CounterType chosenCostCounterType;
    /**
     * The counter kinds the controller picked, one per counter, for a "remove N counters from this
     * permanent" activation cost with N above one. Set only while the suspended activation is
     * re-entered after those choices and cleared once the cost is paid.
     */
    public List<CounterType> chosenCostCounterTypes;
    /**
     * Generic cost reduction left over after the cast spell's own mana cost was paid (its generic
     * part cannot go below zero). The kicker or offspring payment made next applies it to its own
     * generic cost, since reductions apply to the spell's total cost.
     */
    public int unusedSpellCostReduction;

    /**
     * Mana of the spell's other cost components that count toward a minimum-total-cost effect (Trinisphere):
     * a paid buyback or kicker cost adds to the total cost, while a Phyrexian symbol paid with life subtracts
     * from it. Set only while a cast is being paid for; always 0 otherwise.
     */
    public transient int castCostMinimumManaOffset;
    /**
     * Resolution-time "choose a creature type" answer for a spell/ability that has no permanent
     * to hang the choice on (e.g. Coordinated Barrage). Set by the choice handler, read and
     * cleared by the effect handler that re-runs after the choice completes.
     */
    public CardSubtype chosenSpellSubtype;
    /** Resolution-time "choose a color" answer for a spell with no permanent to store it on. */
    public CardColor chosenSpellColor;
    /** Resolution-time odd/even mana-value answer for a spell or ability with no permanent to store it on. */
    public ManaValueParity chosenSpellParity;
    /** Resolution-time "choose a number" answer for a spell with no permanent to store it on. */
    public Integer chosenSpellNumber;
    /** Resolution-time odd/even mana value quality choice for a spell with no permanent to store it on. */
    public ManaValueParity chosenSpellManaValueParity;
    /** Resolution-time Turnabout permanent type choice. */
    public CardType chosenSpellPermanentType;
    /** Resolution-time card type choice for a spell with no permanent to store it on. */
    public CardType chosenSpellCardType;
    /** Resolution-time land/nonland choice for a spell with no permanent to store it on. */
    public Boolean chosenSpellLandOrNonland;
    /** Resolution-time Turnabout action choice: true to tap, false to untap. */
    public Boolean turnaboutTap;
    /**
     * Generic re-entry signal: when set, {@code EffectResolutionService} re-runs the current
     * effect (rather than advancing to the next) after the pending interaction completes.
     * Set by handlers that drive a multi-step, self-continuing flow through an interaction whose
     * completion is not itself an X-value choice (e.g. the each-player discard-then-draw of Flux).
     */
    public boolean rerunCurrentEffectAfterInteraction;
    /** Draw-count checkpoint for a draw/reveal/discard effect paused by a replacement choice. */
    public final Map<UUID, Integer> pendingDrawRevealDiscardDrawCounts = new ConcurrentHashMap<>();
    /**
     * CR 704.3 / 104.3b — while a stack entry's effect list is being resolved (including across an
     * async pause/resume and any nested sub-resolutions), the player-loss state-based action is not
     * checked. {@link com.github.laxika.magicalvibes.service.GameOutcomeService#checkWinCondition}
     * returns early while this is set, so a controller who is momentarily at 0 or less life between
     * two effects of the same spell (e.g. "deal N damage to yourself, then gain N life") survives
     * if a later effect restores them. Cleared and checked once when the outermost effect resolution
     * finishes. Managed only by {@code EffectResolutionService}.
     */
    public boolean deferPlayerLossCheck;
    /**
     * Reentrancy depth of {@code EffectResolutionService.resolveEffectsFrom}. Some handlers resolve
     * a nested effect list synchronously (e.g. Kinship, counter riders); only unwinding the
     * outermost frame (depth 0) finalizes {@link #deferPlayerLossCheck}, so a nested completion
     * does not prematurely re-enable the loss check for the enclosing resolution.
     */
    public int effectResolutionDepth;
    /** Progress state for Flux's "each player discards any number, then draws that many" flow. */
    public final EachPlayerRummageState eachPlayerRummage = new EachPlayerRummageState();
    /**
     * Cards chosen so far by an "each player discards" queue ({@code DiscardFollowUp} with an
     * each-player controller). Every chooser picks in APNAP order and the chosen cards are discarded
     * together once the queue drains (CR 101.4).
     */
    public final List<EachPlayerRummageState.SelectedDiscard> eachPlayerDiscardSelections = new ArrayList<>();
    /** Progress state for placing the cards revealed by a clash (CR 701.30c). */
    public final ClashPlacementState clashPlacement = new ClashPlacementState();
    /** Progress state for Sadistic Augermage's each-player hand-card-to-library-top trigger. */
    public final EachPlayerPutsCardFromHandOnTopOfLibraryState eachPlayerPutsCardFromHandOnTopOfLibrary =
            new EachPlayerPutsCardFromHandOnTopOfLibraryState();
    /** Progress state for each-player discard effects with an opponent life-loss fallback. */
    public final EachPlayerDiscardsOrLosesLifeState eachPlayerDiscardsOrLosesLife =
            new EachPlayerDiscardsOrLosesLifeState();
    /** Progress state for each-player discard effects that check for discarded creature cards. */
    public final EachPlayerDiscardsCreatureOrLosesLifeState eachPlayerDiscardsCreatureOrLosesLife =
            new EachPlayerDiscardsCreatureOrLosesLifeState();
    /** Progress state for each-player discard effects restricted to nonland cards. */
    public final EachPlayerDiscardsNonlandCardState eachPlayerDiscardsNonlandCard =
            new EachPlayerDiscardsNonlandCardState();
    /** Progress state for each player's optional graveyard exile and remaining-card life loss. */
    public final EachPlayerMayExileGraveyardCardsState eachPlayerMayExileGraveyardCards =
            new EachPlayerMayExileGraveyardCardsState();
    /** Progress state for Creeping Dread's each-player discard comparison. */
    public final CreepingDreadState creepingDread = new CreepingDreadState();
    /**
     * Player whose next discard choice is only recorded in {@link #deferredDiscardSelections} instead
     * of discarding the card. Each-player discard effects set it for the current chooser so that every
     * player chooses before any card is discarded (CR 101.4); the effect discards the batch itself.
     */
    public UUID deferredDiscardChooserId;
    /** Discard picks recorded for {@link #deferredDiscardChooserId}, in the order they were chosen. */
    public final List<EachPlayerRummageState.SelectedDiscard> deferredDiscardSelections = new ArrayList<>();
    /**
     * Cards chosen so far in an each-player exile from hand (Mind Swords). They leave the hand when
     * chosen so later picks index the remaining cards, but stay hidden and are exiled together once
     * the last player has chosen (CR 101.4).
     */
    public final List<StagedHandExile> stagedHandExiles = new ArrayList<>();
    /** Progress state for each-player may-discard effects with discard-type riders. */
    public final EachPlayerMayDiscardOneThenApplyEffectsState eachPlayerMayDiscardOneThenApplyEffects =
            new EachPlayerMayDiscardOneThenApplyEffectsState();
    /** Progress state for each player's optional discard followed by a basic-land search. */
    public final EachPlayerMayDiscardThenSearchBasicLandState eachPlayerMayDiscardThenSearchBasicLand =
            new EachPlayerMayDiscardThenSearchBasicLandState();
    /** Progress state for Scythe Specter's opponent discard and mana-value comparison. */
    public final ScytheSpecterState scytheSpecter = new ScytheSpecterState();
    /** Progress state for Cait's two-player draw/discard mana-value comparison. */
    public final CaitCageBrawlerState caitCageBrawler = new CaitCageBrawlerState();
    /** Progress state for Eumidian Wastewaker's attack-trigger discard-or-sacrifice choices. */
    public final EumidianWastewakerState eumidianWastewaker = new EumidianWastewakerState();
    /** Progress state for collecting one discarded card from every player before drawing. */
    public final EachPlayerDiscardsOneThenDrawsForEachCardTypeState
            eachPlayerDiscardsOneThenDrawsForEachCardType =
            new EachPlayerDiscardsOneThenDrawsForEachCardTypeState();
    /** Progress state for Dispersal's opponent-by-opponent return-then-discard sequence. */
    public final DispersalState dispersal = new DispersalState();
    /** Progress state for an opponent-by-opponent discard-two-unless-nonland sequence. */
    public final EachOpponentDiscardsTwoUnlessNonlandState eachOpponentDiscardsTwoUnlessNonland =
            new EachOpponentDiscardsTwoUnlessNonlandState();
    /** Progress state for Plague of Vermin's "each player may pay any amount of life" flow. */
    public final EachPlayerPayLifeState eachPlayerPayLife = new EachPlayerPayLifeState();
    /** Progress state for single-pass per-player mana-payment effects. */
    public final EachPlayerPayManaState eachPlayerPayMana = new EachPlayerPayManaState();
    /** Progress state for Goblin Game's hidden item-count choices. */
    public final GoblinGameState goblinGame = new GoblinGameState();
    /** Progress state for Wheel of Misfortune's hidden number choices. */
    public final WheelOfMisfortuneState wheelOfMisfortune = new WheelOfMisfortuneState();
    /** Progress state for Truth or Consequences' hidden votes. */
    public final TruthOrConsequencesState truthOrConsequences = new TruthOrConsequencesState();
    /** Progress state for Expert-Level Safe's two-player hidden-number choice. */
    public final ExpertLevelSafeState expertLevelSafe = new ExpertLevelSafeState();
    /** Progress state for Illicit Auction's "each player may bid life for control" auction. */
    public final IllicitAuctionState illicitAuction = new IllicitAuctionState();
    /** Progress state for Torment of Hailfire's "repeat X times: each opponent loses life unless…" flow. */
    public final TormentState torment = new TormentState();
    /** Progress state for Indulgent Tormentor's target-opponent choice. */
    public final IndulgentTormentorState indulgentTormentor = new IndulgentTormentorState();
    /** Progress state for Forbidden Ritual's "sacrifice nontoken; opponent loses life unless…" loop. */
    public final ForbiddenRitualState forbiddenRitual = new ForbiddenRitualState();
    /** Progress state for each-player discard-or-sacrifice effects such as Possessed Portal. */
    public final EachPlayerSacrificeOrDiscardState eachPlayerSacrificeOrDiscard =
            new EachPlayerSacrificeOrDiscardState();
    /** Progress state for each-player sacrifice-or-life-loss effects. */
    public final EachPlayerSacrificeOrLoseLifeState eachPlayerSacrificeOrLoseLife =
            new EachPlayerSacrificeOrLoseLifeState();
    /** Progress state for opponent-by-opponent villainous choices. */
    public final VillainousChoiceState villainousChoice = new VillainousChoiceState();
    /** Progress state for Plaguecrafter's simultaneous sacrifice-then-discard effect. */
    public final PlaguecrafterState plaguecrafter = new PlaguecrafterState();
    /** Progress state for Winter's Chill's per-target "may pay {1} or {2}" flow. */
    public final WintersChillState wintersChill = new WintersChillState();
    /** Progress state for Disorienting Choice's per-target optional exile flow. */
    public final DisorientingChoiceState disorientingChoice = new DisorientingChoiceState();
    /** Progress state for Forgotten Lore and Shrouded Lore's repeating graveyard-choice flow. */
    public final ForgottenLoreState forgottenLore = new ForgottenLoreState();

    public PendingAbilityActivation pendingAbilityActivation;
    /** A graveyard-activated ability suspended on its "Discard a card" cost choice (Eternalize). */
    public PendingGraveyardAbilityActivation pendingGraveyardAbilityActivation;
    /** Players whose draw redirect (Plagiarize) has already been applied to the draw being resolved (CR 614.5). */
    public final Set<UUID> drawRedirectsAppliedToCurrentDraw = ConcurrentHashMap.newKeySet();
    /** Number of Chains of Mephistopheles replacements already applied to the current draw. */
    public final Map<UUID, Integer> chainsDrawReplacementsApplied = new ConcurrentHashMap<>();
    /** Player IDs that have already taken their first draw of their own draw step this turn. Used by
     *  Notion Thief-style replacements that exempt "the first card they draw in each of their draw
     *  steps". Cleared at end-of-turn cleanup. */
    public final Set<UUID> drawStepFirstDrawTaken = ConcurrentHashMap.newKeySet();
    /** Source permanents whose first eligible non-draw-step draw replacement has been used this turn. */
    public final Set<UUID> firstNonDrawStepDrawReplacementsUsedThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisTurn = new ConcurrentHashMap<>();
    /** Players who have begun activating an exhaust ability this turn. */
    public final Set<UUID> playersWhoActivatedExhaustAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who activated an ability of a card in a graveyard this turn. */
    public final Set<UUID> playersWhoActivatedAbilityOfGraveyardCardThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have activated an equip ability this turn. */
    public final Set<UUID> playersWhoActivatedEquipAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who activated an ability of an artifact this turn (the first such activation each turn is discounted by Tezzeret, Betrayer of Flesh). */
    public final Set<UUID> playersWhoActivatedArtifactAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have activated a Power-up ability this turn. */
    public final Set<UUID> playersWhoActivatedPowerUpAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have activated a loyalty ability of a planeswalker this turn, backing the
     *  {@code DidntActivateLoyaltyAbilityThisTurn} intervening-if (The Chain Veil). Recorded when the
     *  loyalty cost is paid, so an activation whose ability is countered still counts. */
    public final Set<UUID> playersWhoActivatedLoyaltyAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who activated a spark ability this turn; spark is global across their permanents. */
    public final Set<UUID> playersWhoActivatedSparkAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Per-permanent, per-ability-index count of activations for the whole game, backing "Activate
     *  only once" ({@code ActivatedAbility.maxActivationsPerGame}, e.g. Goblin Ski Patrol). Keyed by
     *  permanent id, so a permanent that leaves and re-enters the battlefield is a new object and may
     *  activate again (CR 400.7). Never cleared at turn cleanup — only by Karn's game restart. */
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisGame = new ConcurrentHashMap<>();
    /** Per-permanent activation-count keys of abilities its card doesn't print (granted, temporary, ...):
     *  each such ability's identity (its acquisition, or granting object and wording, CR 602.5c) maps to
     *  the key its uses are counted under in {@link #activatedAbilityUsesThisTurn} and
     *  {@link #activatedAbilityUsesThisGame}. Kept for the whole game so a key never moves to another
     *  ability when an earlier granted ability goes away. */
    public final Map<UUID, Map<String, Integer>> nonPrintedAbilityUseKeys = new ConcurrentHashMap<>();
     /** Per-permanent count of how many times its resolution-counting ability has resolved
     *  this turn (the {@code NthAbilityResolutionThisTurn} condition, e.g. Ashling the Pilgrim or
     *  Nissa, Resurgent Animist).
     *  Keyed by source permanent id; reset at the start of each turn. */
    public final Map<UUID, Integer> permanentAbilityResolutionsThisTurn = new ConcurrentHashMap<>();
    /** Maps a permanent that is not controlled by its owner to that owner (recorded on the first
     *  control change away from the owner, removed when control reverts to the owner or the
     *  permanent leaves the battlefield). This is the OWNERSHIP record used to route cards to the
     *  right graveyard/hand/library on leave; which player CONTROLS the permanent is derived from
     *  the floating {@code L2_CONTROL} effects (see {@link #deriveControllerOf}). */
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    /** Cards whose ownership changed during this game (Tempest Efreet's ante exchange), mapped to
     *  their current owner. A frozen {@link Card} keeps its stamped owner, so this record overrides
     *  it; read it through {@link #ownerOf(Card)}. */
    public final Map<UUID, UUID> cardOwnerOverrides = new ConcurrentHashMap<>();
    /** Reflexive Equipment-unattach triggers registered until the end of the current turn. */
    public final Map<UUID, List<ControlLossUnattachTrigger>> controlLossUnattachTriggers =
            new ConcurrentHashMap<>();
    /** Delayed tap triggers registered until the watched permanent changes controllers. */
    public final Map<UUID, List<ControlLossTapTrigger>> controlLossTapTriggers =
            new ConcurrentHashMap<>();
    /** Number of end steps that have begun during the current turn. */
    public int endStepsThisTurn;
    /** Extra end steps waiting to occur during the current turn. */
    public int additionalEndStepsPending;
    public boolean endTurnRequested;
    /** Set while a resolving Do-Over spell is replacing the current turn state. */
    public boolean restartTurnRequested;
    /**
     * Unified queue of pending player interactions (decisions awaiting player input).
     * Replaces the former per-kind {@code Deque} fields. Consumers service one kind at a
     * time via the type-filtered helpers below; since every producer appends with
     * {@link #queueInteraction} and every consumer takes the first entry of its own kind,
     * the original per-kind FIFO ordering is preserved exactly. Accessed under
     * {@code synchronized (gameData)} blocks in the engine, like the fields it replaced.
     */
    public final Deque<PendingInteraction> pendingInteractions = new ArrayDeque<>();
    public boolean discardCausedByOpponent;
    public PendingReturnToHandOnDiscardType pendingReturnToHandOnDiscardType;
    public PendingGainLifeOnDiscardType pendingGainLifeOnDiscardType;
    public PendingTransformOnCreatureDiscard pendingTransformOnCreatureDiscard;
    public PendingBoostSourceByDiscardedManaValue pendingBoostSourceByDiscardedManaValue;
    public PendingConnive pendingConnive;
    public PendingUntapOnDiscardType pendingUntapOnDiscardType;
    public final Deque<UUID> extraTurns = new ArrayDeque<>();
    /**
     * Parallel to {@link #extraTurns}: whether the correspondingly-positioned extra turn skips its
     * untap step (e.g. Savor the Moment). Maintained in lockstep — every push/poll of {@link #extraTurns}
     * in engine code pushes/polls this deque at the same end.
     */
    public final Deque<Boolean> extraTurnSkipsUntap = new ArrayDeque<>();
    /** Parallel to {@link #extraTurns}: whether each queued extra turn prohibits Power-up abilities. */
    public final Deque<Boolean> extraTurnPowerUpAbilitiesDisabled = new ArrayDeque<>();
    /**
     * Parallel to {@link #extraTurns}: whether the correspondingly-positioned extra turn makes
     * damage unpreventable. Maintained in lockstep with the other extra-turn queues.
     */
    public final Deque<Boolean> extraTurnDamageCantBePrevented = new ArrayDeque<>();
    public final Deque<Long> extraTurnSequences = new ArrayDeque<>();

    /**
     * Ends the delayed triggered abilities that wait for the end step of the extra turn {@code sequence} ("at the
     * beginning of that turn's end step"): the turn was skipped or ended without its end step, so the event they
     * wait for never happens and they can't trigger in another turn (CR 603.7).
     */
    public void endDelayedAbilitiesOfExtraTurn(long sequence) {
        temporaryGlobalTriggeredAbilities.removeIf(delayed -> delayed.referents().extraTurnSequence() == sequence);
    }

    public long queueExtraTurnFirst(UUID playerId, boolean skipsUntap) {
        return queueExtraTurnFirst(playerId, skipsUntap, false);
    }

    public long queueExtraTurnFirst(UUID playerId, boolean skipsUntap, boolean damageCantBePrevented) {
        return queueExtraTurnFirst(playerId, skipsUntap, damageCantBePrevented, false);
    }

    public long queueExtraTurnFirst(UUID playerId, boolean skipsUntap, boolean damageCantBePrevented, boolean powerUpAbilitiesDisabled) {
        extraTurnPowerUpAbilitiesDisabled.addFirst(powerUpAbilitiesDisabled);
        long sequence = nextExtraTurnSequence++;
        extraTurnDamageCantBePrevented.addFirst(damageCantBePrevented);
        extraTurns.addFirst(playerId);
        extraTurnSkipsUntap.addFirst(skipsUntap);
        extraTurnSequences.addFirst(sequence);
        return sequence;
    }
    /** Additional upkeep steps still to be taken during the active player's current turn. */
    public int additionalUpkeepsRemaining;
    /** Whether the current upkeep step was inserted after the turn's first upkeep. */
    public boolean currentUpkeepIsAdditional;
    public int additionalCombatMainPhasePairs;
    /** The ordinary phase to resume after the inserted combat/main phase pairs. */
    public TurnStep additionalCombatMainPhasePairsReturnStep;
    /** Pairs in {@link #additionalCombatMainPhasePairs} that wait for the turn's second main phase to end. */
    public int additionalCombatMainPhasePairsAfterSecondMain;
    /** Additional beginning phases queued after the current postcombat main phase. */
    public int additionalBeginningPhasesAfterPostcombatMain;
    /** How many of the queued postcombat-main beginning phases were created after the queued
     *  combat/main pairs and therefore happen before them. */
    public int additionalBeginningPhasesBeforeQueuedCombat;
    /** The ordinary phase to resume after inserted beginning phases. */
    public TurnStep additionalBeginningPhaseReturnStep;
    /** Additional combat phases with NO additional main phase (e.g. Finest Hour), queued after the
     *  current combat phase and consumed when leaving END_OF_COMBAT. Reset at the start of each turn. */
    public int additionalCombatPhasesOnly;
    /** Pending aggressive combat phases created by Aggressive at the end of the first combat. */
    public int aggressiveCombatPhasesOnly;
    /** Original active player while an additional combat is being conducted for another controller. */
    public UUID additionalCombatReturnActivePlayerId;
    /** Whether only creatures with Aggressive may attack during the current additional combat. */
    public boolean onlyAggressiveCreaturesCanAttackThisCombat;
    /** Additional combat phases inserted after the resolving main phase, with no main phase between them. */
    public int additionalCombatPhasesAfterMain;
    /** The normal phase to resume after {@link #additionalCombatPhasesAfterMain} is exhausted. */
    public TurnStep additionalCombatPhasesAfterMainReturnStep;
    /** How many combat phases have begun this turn (1 during the first combat phase). Reset at the
     *  start of each turn and incremented on entering BEGINNING_OF_COMBAT; read by FirstCombatPhase. */
    public int combatPhasesThisTurn;
    /** Whether the first combat's window for spells restricted to before combat damage has closed. */
    public boolean firstCombatDamageTimingClosed;
    public UUID draftId;
    public final Deque<LibraryBottomReorderRequest> pendingLibraryBottomReorders = new ArrayDeque<>();
    /** The next permanent-auction pick, held while an as-enters choice or Aura host choice for the previous pick is answered. */
    public PendingInteraction.PermanentAuctionChoice pendingAuctionNext;
    public final WarpWorldOperationState warpWorldOperation = new WarpWorldOperationState();
    public final RetetherOperationState retetherOperation = new RetetherOperationState();
    public final AuspiciousStarrixOperationState auspiciousStarrixOperation =
            new AuspiciousStarrixOperationState();
    public boolean cleanupDiscardPending;
    /** Tracks exile-until-source-leaves connections (O-ring style).
     *  Maps source permanent UUID to the exiled cards + owner info.
     *  When the source permanent leaves the battlefield, the exiled cards return.
     *  A source may hold more than one pending return (e.g. Realm Razer exiles all lands). */
    public final Map<UUID, List<PendingExileReturn>> exileReturnOnPermanentLeave = new ConcurrentHashMap<>();
    /** Maps a permanent to the card its "when this leaves the battlefield" return puts into hand (Ugin, the Ineffable's
     *  Spirit). Kept outside the permanent's abilities, so losing abilities or being a copy does not affect it. */
    public final Map<UUID, ReturnExiledCardToHandEffect> exiledCardReturnToHandOnPermanentLeave = new ConcurrentHashMap<>();
    /** Exile returns keyed by the player whose opponent must become monarch. */
    public final Map<UUID, List<PendingExileReturn>> exileReturnOnOpponentBecomesMonarch = new ConcurrentHashMap<>();
    /** Opal-Eye shields already consumed by the open simultaneous damage event; they keep redirecting the
     *  rest of that event's damage from the same source and are dropped once the event is dealt. */
    public final List<SourceNextDamageRedirectToPermanentShield> sourceRedirectShieldsUsedInOpenEvent =
            Collections.synchronizedList(new ArrayList<>());
    /** Shadowbane-style creature shields already consumed by the open simultaneous damage event; they keep
     *  preventing the rest of that event's damage from the same source and are dropped once it is dealt. */
    public final List<PlayerSourceNextDamageShield> playerSourceShieldsUsedInOpenEvent =
            Collections.synchronizedList(new ArrayList<>());
    /** Pending Eye for an Eye reflected damage to deal after a shield matches (populated by
     *  DamagePreventionService, consumed by the damage-dealing services). */
    public final List<EyeForAnEyeReflection> pendingEyeForAnEyeReflections = Collections.synchronizedList(new ArrayList<>());
    /** Per-source damage accumulated during one non-combat damage event (a single stack-entry
     *  resolution), keyed by the damage source card's id. Flushed after the resolution completes so a
     *  global "whenever a [color] source deals damage" watcher (Justice) reflects the summed total
     *  once per source (CR ruling). Combat damage batches separately via {@code combatDamageDealt}. */
    public final Map<UUID, PendingSourceDamage> pendingSourceDamageForReflection = new LinkedHashMap<>();
    /** Lifelink gains summed per source and player during the open damage event of a resolution;
     *  applied as one life-gain event each when that damage event ends. */
    public final List<PendingLifelinkGain> pendingLifelinkGains = new ArrayList<>();
    /** Times each player is to proliferate for damage their proliferatelink sources dealt in the open damage event. */
    public final Map<UUID, Integer> pendingProliferateLinks = new java.util.LinkedHashMap<>();
    /** The damage event the resolving spell or ability is in, or null outside a resolution. */
    public ResolutionDamageEvent openDamageEvent;
    /** Suppresses re-offering an optional creature-damage redirect while a declined event resumes. */
    public boolean resolvingDeclinedAllCreatureDamageRedirect;
    /**
     * Players who play with their hand revealed for as long as a given source permanent remains on
     * the battlefield (Stromgald Spy), keyed by that source permanent's id. Entries stay until the
     * source leaves the battlefield; readers must check that the source is still there.
     */
    public final Map<UUID, Set<UUID>> handsRevealedWhileSourceOnBattlefield = new ConcurrentHashMap<>();
    /**
     * Permanents that can't have counters put on them for as long as a given source permanent
     * remains on the battlefield (Suncleanser's first mode), keyed by that source permanent's id.
     * Entries stay until the source leaves; readers must check that the source is still there.
     */
    public final Map<UUID, Set<UUID>> countersLockedPermanentsWhileSourceOnBattlefield = new ConcurrentHashMap<>();
    /**
     * Players who can't get counters for as long as a given source permanent remains on the
     * battlefield (Suncleanser's second mode), keyed by that source permanent's id. Same lifetime
     * rule as {@link #countersLockedPermanentsWhileSourceOnBattlefield}.
     */
    public final Map<UUID, Set<UUID>> countersLockedPlayersWhileSourceOnBattlefield = new ConcurrentHashMap<>();
    /**
     */
    /**
     * Set for the duration of a single unpreventable damage event ("the damage can't be prevented",
     * e.g. Flames of the Blood Hand), so the shared prevention gate {@code isDamagePreventable}
     * turns off for that one event only. Always cleared in a {@code finally} by the effect handler
     * that raised it — it is never observable outside a resolution.
     */
    public boolean unpreventableDamageInProgress = false;
    /**
     * Number of active "if a creature would deal combat damage to a creature this turn, it deals
     * double that damage instead" replacement effects (Blind Fury). Each one doubles the damage, so
     * the multiplier is {@code 2^count}. Cleared at turn cleanup.
     */
    /**
     * Active "whenever a card is put into an opponent's graveyard from anywhere this turn, that
     * player loses 1 life" delayed triggers (Duskmantle Guildmage). One entry per activation, so
     * repeated activations stack. Cleared at turn cleanup.
     */
    public final List<OpponentGraveyardLifeLossWatcher> opponentGraveyardLifeLossWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /**
     * Active "whenever you gain life this turn, each opponent loses that much life" delayed triggers
     * (Vizkopa Guildmage). One entry per activation, so repeated activations stack. Cleared at turn
     * cleanup.
     */
    public final List<LifeGainOpponentLifeLossWatcher> lifeGainOpponentLifeLossWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Global triggered abilities registered by resolving spells until the current turn ends. */
    public final List<TemporaryGlobalTriggeredAbility> temporaryGlobalTriggeredAbilities =
            Collections.synchronizedList(new ArrayList<>());
    /** Finite sacrifice-trigger boons, retained until all of their uses have fired. */
    public final List<SacrificeBoonWatcher> sacrificeBoonWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Depth of simultaneous-sacrifice batches; a "one or more permanents" boon fires once per player per batch. */
    public int simultaneousSacrificeDepth;
    /** Players whose sacrifice boon has already fired during the current simultaneous-sacrifice batch. */
    public final Set<UUID> sacrificeBoonBatchPlayerIds = new HashSet<>();
    public final List<CreatureEntersTriggerWatcher> allyCreatureEntersTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Pending redirect damage to deal after damage prevention (populated by DamagePreventionService,
            consumed by callers). */
    public final List<DamageRedirectShield> pendingRedirectDamage = Collections.synchronizedList(new ArrayList<>());
    /** Pending source redirect damage to deal after source-specific prevention (populated by DamagePreventionService, consumed by callers). */
    public final List<SourceDamageRedirectShield> pendingSourceRedirectDamage = Collections.synchronizedList(new ArrayList<>());
    /**
     * A replacement event paused while an effect applied to it waits for a decision outside the
     * CR 616.1 ordering prompt (a Cleric's "you may prevent"); that decision resumes it.
     */
    public com.github.laxika.magicalvibes.model.replacement.ReplacementEvent suspendedReplacementEvent;
    /**
     * The family of the damage replacement step ("destroy it instead", "sacrifice it") whose departure's questions
     * {@link #suspendedReplacementEvent} waits for (CR 616.1f), or null. The step is applied, and the event continued,
     * once the pending departure batch is answered.
     */
    public String suspendedDamageDepartureStep;
    /**
     * The sacrifice a life loss replacement would perform instead of the life loss of damage being dealt to a player,
     * whose questions were asked before the damage (CR 616.1f), or null. While it is {@code suspended},
     * {@link #suspendedReplacementEvent} waits for the answers; afterwards it is kept until the life loss happens, so
     * an answered batch the life loss didn't perform is dropped then.
     */
    public AskedLifeLossDeparture askedLifeLossDeparture;
    /**
     * The result of Game of Chaos's latest coin flip ({@code true} when the spell's controller won it), kept from the
     * flip until its life changes are made, so a flip whose loser's Lich's Duel Mastery sacrifice stops it for a
     * CR 616.1 question is not flipped again when it is resolved or answered again; {@code null} otherwise.
     */
    public Boolean gameOfChaosFlipControllerWins;
    /**
     * The permanent spell whose resolution stopped before it began, so the Lich's Duel Mastery sacrifice that
     * replaces the life loss of its "as this enters, you lose life" (Lich) could have its death replacements chosen
     * before it enters (CR 616.1f); it is resolved once they are answered. {@code null} otherwise.
     */
    public UUID spellAwaitingEntryLifeLossDepartureId;
    /**
     * A permanent an effect is putting onto the battlefield whose entry stopped before anything of it happened, so
     * the Lich's Duel Mastery sacrifice that replaces the life loss of its "as this enters, you lose life" (Lich)
     * could have its death replacements chosen first (CR 616.1f); the entry goes on once they are answered.
     * {@code null} otherwise.
     */
    public BattlefieldEntryRequest entryAwaitingLifeLossDeparture;
    /** The damage events of the effect being resolved, while one of them awaits its player's order. */
    public com.github.laxika.magicalvibes.model.replacement.DamageJournal damageJournal;
    /**
     * Damage the steps of a simultaneous damage event started, held until the event ends and then
     * dealt combined per recipient and source.
     */
    public final List<com.github.laxika.magicalvibes.model.replacement.PendingSimultaneousDamage>
            pendingSimultaneousDamage = new ArrayList<>();
    /** The simultaneous damage event whose step is being dispatched, or null when none is. */
    public ResolutionDamageEvent collectingDamageEvent;
    /** The effect index of the step {@link #collectingDamageEvent} is dispatching. */
    public int collectingDamageStep;
    /**
     * Replacement events that became due while another one awaited its player's decision. They are
     * handled in order, with the effects applicable at that time, once no decision is pending.
     */
    public final List<com.github.laxika.magicalvibes.model.replacement.ReplacementEvent> pendingReplacementEvents =
            Collections.synchronizedList(new ArrayList<>());
    /** Queue for "each player returns up to N cards from graveyard to battlefield" choices. */
    public final List<PendingGraveyardReturnChoice> pendingGraveyardReturnQueue = Collections.synchronizedList(new ArrayList<>());
    public final List<DeferredSpellCopy> deferredGraveyardCastCopies = Collections.synchronizedList(new ArrayList<>());
    /**
     * Spell copies whose controller is still choosing new targets, in creation order. A copy is put onto the
     * stack only once its targets are decided (CR 707.10c), so it stays here, off the stack, while its
     * "choose new targets" prompt or any per-target choice is pending.
     */
    public final List<StackEntry> stagedSpellCopies = Collections.synchronizedList(new ArrayList<>());
    public PendingGraveyardReturnBatch pendingGraveyardReturnBatch;
    /**
     * Set while the {@link #pendingGraveyardReturnQueue} choices of one effect share a single
     * graveyard-leave batch, so "one or more cards leave your graveyard" triggers once for all of
     * them (Grave Sifter). The batch ends when the queue runs out.
     */
    public boolean graveyardReturnQueueLeaveBatchOpen;
    /**
     * Set while a multi-card graveyard exile choice (a count above one) runs as a single graveyard-leave
     * batch, so "one or more cards leave your graveyard" triggers once for all cards it exiles (Watchers
     * of the Dead). The batch ends when the choice chain runs out.
     */
    public boolean graveyardExileChoiceLeaveBatchOpen;
    /**
     * Set while the {@link #pendingGraveyardReturnQueue} choices of an "each player returns cards from
     * their graveyard to their hand" effect are being made: the chosen cards stay in their graveyards
     * (recorded in {@link #heldGraveyardHandReturns}) until every player has chosen, then move together.
     */
    public boolean holdGraveyardHandReturns;
    /** Set while a continuation of an earlier library search begins, so it fires no new "searches" triggers. */
    public boolean suppressNextLibrarySearchTriggers;
    /** Cards chosen but not yet returned while {@link #holdGraveyardHandReturns} is set, mapped to the player returning each. */
    public final Map<UUID, UUID> heldGraveyardHandReturns = Collections.synchronizedMap(new LinkedHashMap<>());
    /** Owners whose libraries Don't Blink's replacement must shuffle once after a batch of creatures enters from exile together; null outside such a batch (never spans a mutation, so not copied). */
    public Set<UUID> exileEntryShuffleBatchOwners;
    /** APNAP-ordered queue of players still to act in an "each player may draw" effect (Temporary Truce): the head player is prompted while choices are made, then each player in turn draws their chosen count. */
    public final List<UUID> pendingEachPlayerDrawUpToQueue = Collections.synchronizedList(new ArrayList<>());
    /** Each player's chosen draw count for the current "each player may draw up to N" effect, in APNAP order. */
    public final Map<UUID, Integer> pendingEachPlayerDrawUpToChoices = Collections.synchronizedMap(new LinkedHashMap<>());
    /** Draw count before the current optional draw instruction, retained across replacement choices. */
    public Integer pendingEachPlayerDrawUpToInitialCount;
    /** Each player's draw count before the simultaneous optional draw of the current "each player may draw up to N" effect began, retained across replacement choices; null until every player has chosen. */
    public Map<UUID, Integer> pendingEachPlayerDrawUpToInitialCounts;
    /** Hypergenesis-style put-from-hand process paused behind the prompt opened by the card just put. */
    public PendingEachPlayerPutContinuation pendingEachPlayerPutContinuation;
    /** Primal Surge-style repeat step paused behind the prompt opened by the permanent just put onto the battlefield. */
    public PendingExileTopCardRepeat pendingExileTopCardRepeat;
    /** Chosen count for the current optional draw instruction, retained across replacement choices. */
    public Integer pendingEachPlayerMayDrawChosenCount;
    /** APNAP-ordered players choosing for "each player may draw, then drawers gain life" effects. */
    public final List<UUID> pendingEachPlayerMayDrawThenGainLifeQueue =
            Collections.synchronizedList(new ArrayList<>());
    /** Players who actually drew during the current "may draw, then gain life" resolution. */
    public final List<UUID> pendingEachPlayerMayDrawThenGainLifeDrawers =
            Collections.synchronizedList(new ArrayList<>());
    /** Draw count before the current Kwain-style optional draw instruction. */
    public Integer pendingEachPlayerMayDrawThenGainLifeInitialCount;
    /** APNAP-ordered queue of players still to choose for "each other player may draw up to N" effects. */
    public final List<UUID> pendingEachOtherPlayerDrawUpToQueue = Collections.synchronizedList(new ArrayList<>());
    /** The Ring emblem state for players who have been tempted by the Ring. */
    public final Map<UUID, RingState> ringStates = new ConcurrentHashMap<>();
    public final List<Emblem> emblems = Collections.synchronizedList(new ArrayList<>());
    /** Finite-use triggered abilities granted directly to players. */
    public final List<Boon> boons = Collections.synchronizedList(new ArrayList<>());
    /** Players who play with their hand revealed for the rest of the game (e.g. Truth or Dare). */
    public final Set<UUID> playersWithHandRevealed = ConcurrentHashMap.newKeySet();

    /** Tracks source-linked animations (Awakener Druid-style).
     *  Maps animated target permanent UUID → source permanent UUID.
     *  When the source leaves the battlefield, the target's animation is cleared. */
    public final Map<UUID, UUID> sourceLinkedAnimations = new ConcurrentHashMap<>();

    /** Temporary characteristic changes applied to a card while it remains in a graveyard: an effect on that card. */
    public record GraveyardCardAnimation(int power, int toughness, Set<CardColor> grantedColors,
                                         Set<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes)
            implements CardEffect {
        public GraveyardCardAnimation {
            grantedColors = Set.copyOf(grantedColors);
            grantedSubtypes = Set.copyOf(grantedSubtypes);
            grantedCardTypes = Set.copyOf(grantedCardTypes);
        }
    }






    /** Card IDs of individual spells on the stack that have been made uncounterable (e.g. Vexing Shusher's
     *  "Target spell can't be countered"). Only relevant while the spell is on the stack. */
    public final Set<UUID> spellsMadeUncounterable = ConcurrentHashMap.newKeySet();

    /** Text-change replacements applied to a spell on the stack (e.g. Glamerdye targeting a spell).
     *  Keyed by the spell's card id; carried onto the permanent it resolves into (CR 400.7a). */
    public final Map<UUID, List<TextReplacement>> spellTextReplacements = new ConcurrentHashMap<>();

    /** Color override applied to a spell on the stack (e.g. Purelace targeting a spell — "becomes white",
     *  or Ersatz Gnomes — "becomes colorless", recorded as an empty set). Keyed by the spell's card id;
     *  carried onto the permanent it resolves into (CR 400.7a), where it replaces that permanent's colors
     *  indefinitely. */
    public final Map<UUID, Set<CardColor>> spellColorOverrides = new ConcurrentHashMap<>();

















    /** Land subtype -&gt; the mana color lands of that subtype produce instead of any other type this
     *  turn (Chaos Moon's even branch: "that Mountain produces colorless mana instead of any other
     *  type"). Amount is unchanged. Cleared at end of turn. */


    /** The mana color every land produces instead of any other color this turn (Hall of Gemstone,
     *  chosen by the player whose upkeep it is). Amount is unchanged. Null when inactive; cleared at
     *  end of turn. */





    /**
     * Players who tapped a land for mana this turn (Desolation). Recorded whenever a land is tapped
     * for mana, regardless of whether Desolation is on the battlefield. Cleared at turn start.
     */
    public final Set<UUID> playersWhoTappedLandForManaThisTurn = ConcurrentHashMap.newKeySet();








    /**
     * Whether {@code playerId} may play lands and cast spells from {@code handOwnerId}'s hand this turn: Sen Triplets'
     * floating effect on that player, made by {@code playerId}.
     */
    public boolean canPlayFromHandOf(UUID playerId, UUID handOwnerId) {
        return playerId != null && handOwnerId != null && !playerId.equals(handOwnerId)
                && floatingEffectsOfTypes(SenTripletsUpkeepEffect.class).stream().anyMatch(fe ->
                playerId.equals(fe.controllerId()) && handOwnerId.equals(fe.affectedPlayerId()));
    }

    /** The player whose hand {@code playerId} may play lands and cast spells from this turn (Sen Triplets), or {@code null}. */
    public UUID handPlayableBy(UUID playerId) {
        return floatingEffectsOfTypes(SenTripletsUpkeepEffect.class).stream()
                .filter(fe -> playerId != null && playerId.equals(fe.controllerId()) && fe.affectedPlayerId() != null
                        && !playerId.equals(fe.affectedPlayerId()))
                .map(FloatingContinuousEffect::affectedPlayerId).findFirst().orElse(null);
    }

    /** Whether some player may play from the hand of a player other than {@code playerId} this turn (Sen Triplets). */
    public boolean handRevealedToSenTripletsController(UUID playerId) {
        return floatingEffectsOfTypes(SenTripletsUpkeepEffect.class).stream()
                .anyMatch(fe -> fe.affectedPlayerId() != null && !fe.affectedPlayerId().equals(playerId));
    }

    /** Whether the card {@code cardId} is revealed in its owner's hand ({@code handOwnerId}) by an effect. */
    public boolean isCardRevealedInHand(UUID cardId, UUID handOwnerId) {
        return floatingEffectsOnCard(cardId, CardRevealedInHandEffect.class).stream()
                .anyMatch(fe -> handOwnerId.equals(fe.controllerId()));
    }

    /** Whether an effect bars {@code playerId} from playing the card {@code cardId} from their hand. */
    public boolean isCardPlayBarredInHand(UUID cardId, UUID playerId) {
        return floatingEffectsOnCard(cardId, CardCantBePlayedFromHandEffect.class).stream()
                .anyMatch(fe -> playerId.equals(fe.controllerId()));
    }




    /** Exile entries created by Warp, bound to the specific stay in exile. */
    public final Map<UUID, Long> warpExileEntryVersions = new ConcurrentHashMap<>();












    /** Extra +1/+1 counters a spell's permanent enters the battlefield with, keyed by card id
     *  (Savage Summoning). Consumed as an as-enters replacement by {@code BattlefieldEntryService}
     *  and cleared at end of turn. */
    public final Map<UUID, Integer> spellAdditionalEnterCounters = new ConcurrentHashMap<>();

    /** Additional +1/+1 counters a spell's permanent enters with whose number is counted as it enters
     *  (Torgal, A Fine Hound), keyed by card id; each entry carries the controller to count for. */
    public final Map<UUID, List<EnterCounterAmount>> spellAdditionalEnterCounterAmounts = new ConcurrentHashMap<>();

    /** An as-enters +1/+1 counter count, evaluated for {@code controllerId} when the permanent enters. */
    public record EnterCounterAmount(com.github.laxika.magicalvibes.model.amount.DynamicAmount amount, UUID controllerId) {
    }

    /** Entry counters granted to a permanent spell by a cast permission. */
    public final Map<UUID, Map<CounterType, Integer>> spellEntryCounters = new ConcurrentHashMap<>();

    /** Subtypes granted to a permanent spell by a one-shot graveyard cast permission. */
    public final Map<UUID, Set<CardSubtype>> spellGrantedSubtypesOnEntry = new ConcurrentHashMap<>();


    /** Mystic Reflection replacements already applied to the current simultaneous entry event. */
    public final List<PendingMysticReflection> activeMysticReflectionsForEntryBatch =
            Collections.synchronizedList(new ArrayList<>());

    /** Card ids of creature spells paid for with haste-granting mana (Generator Servant), so the
     *  permanent they become enters with haste until end of turn. Consumed by
     *  {@code BattlefieldEntryService} and cleared at end of turn. */
    public final Set<UUID> spellsGrantedHasteOnEntry = ConcurrentHashMap.newKeySet();
    /** The subset of {@link #spellsGrantedHasteOnEntry} whose haste has no duration (Hall of the Bandit Lord). */
    public final Set<UUID> spellsGrantedPermanentHasteOnEntry = ConcurrentHashMap.newKeySet();
    /** Native suspend haste awaiting the completion of an exile-cast target choice. */
    public final Set<UUID> spellsGrantedSuspendHasteOnEntry = ConcurrentHashMap.newKeySet();

    /** Card ids of creature spells paid for with riot-granting mana, so the permanents they become
     *  enter with riot. Consumed by {@code BattlefieldEntryService} and cleared at end of turn. */
    public final Set<UUID> spellsGrantedRiotOnEntry = ConcurrentHashMap.newKeySet();




    /**
     * A one-shot permission to cast a specific graveyard card for its normal cost.
     *
     * @param copySourceActivatedAbilities Havengul Lich's rider — when the card is cast this way the
     *                                     source permanent gains its activated abilities
     * @param exileInsteadOfGraveyard      Toshiro Umezawa's rider — the resulting spell is exiled
     *                                     rather than put into a graveyard
     */
    public record GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                              boolean copySourceActivatedAbilities,
                                              boolean exileInsteadOfGraveyard,
                                              int additionalGenericCost,
                                              boolean anyManaType,
                                              boolean withoutPayingManaCost,
                                              int additionalGraveyardExileCount,
                                              boolean escape) implements CardEffect {
        public GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                           boolean copySourceActivatedAbilities,
                                           boolean exileInsteadOfGraveyard,
                                           int additionalGenericCost,
                                           boolean anyManaType) {
            this(sourcePermanentId, castingPlayerId, copySourceActivatedAbilities,
                    exileInsteadOfGraveyard, additionalGenericCost, anyManaType, false, 0, false);
        }

        public GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                           boolean copySourceActivatedAbilities,
                                           boolean exileInsteadOfGraveyard,
                                           boolean withoutPayingManaCost) {
            this(sourcePermanentId, castingPlayerId, copySourceActivatedAbilities,
                    exileInsteadOfGraveyard, 0, false, withoutPayingManaCost, 0, false);
        }

        public GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                           boolean copySourceActivatedAbilities,
                                           boolean exileInsteadOfGraveyard,
                                           int additionalGenericCost) {
            this(sourcePermanentId, castingPlayerId, copySourceActivatedAbilities,
                    exileInsteadOfGraveyard, additionalGenericCost, false, false, 0, false);
        }

        public GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                            boolean copySourceActivatedAbilities,
                                            boolean exileInsteadOfGraveyard) {
            this(sourcePermanentId, castingPlayerId, copySourceActivatedAbilities,
                    exileInsteadOfGraveyard, 0, false, false, 0, false);
        }

        public GraveyardCardCastPermission(UUID sourcePermanentId, UUID castingPlayerId,
                                           boolean copySourceActivatedAbilities,
                                           boolean exileInsteadOfGraveyard,
                                           int additionalGenericCost,
                                           boolean anyManaType,
                                           boolean withoutPayingManaCost,
                                           int additionalGraveyardExileCount,
                                           boolean escape) {
            this.sourcePermanentId = sourcePermanentId;
            this.castingPlayerId = castingPlayerId;
            this.copySourceActivatedAbilities = copySourceActivatedAbilities;
            this.exileInsteadOfGraveyard = exileInsteadOfGraveyard;
            this.additionalGenericCost = additionalGenericCost;
            this.anyManaType = anyManaType;
            this.withoutPayingManaCost = withoutPayingManaCost;
            this.additionalGraveyardExileCount = additionalGraveyardExileCount;
            this.escape = escape;
        }
    }

    /** A temporary permission to cast a specific card from its controller's graveyard as an Adventure, through {@code expireTurn}. */
    public record GraveyardAdventureCastPermission(UUID castingPlayerId) implements CardEffect {}

    /** "You may play that card from the graveyard", as an effect on the card; {@code entersTapped} is its rider. */
    public record GraveyardCardPlayGrant(UUID playerId, boolean entersTapped) implements CardEffect {}


    /** A turn-scoped grant letting {@code playerId} cast cards matching {@code filter} from a
     *  permitted graveyard, paying their normal costs. */
    public record GraveyardCastFilterPermission(UUID playerId, CardPredicate filter,
                                                boolean singleUse, CounterType entryCounterType,
                                                CardSubtype grantedSubtype, boolean anyGraveyard,
                                                boolean exileInsteadOfGraveyard,
                                                ForageOrPayManaCost additionalCost,
                                                CounterType enterWithCounter,
                                                int enterWithCounterCount,
                                                UUID graveyardOwnerId,
                                                boolean anyManaType) implements CardEffect {
        public GraveyardCastFilterPermission(UUID playerId, CardPredicate filter) {
            this(playerId, filter, false, null, null, false, false, null, null, 0, null, false);
        }

        public GraveyardCastFilterPermission(UUID playerId, CardPredicate filter,
                                             boolean singleUse, CounterType entryCounterType,
                                             CardSubtype grantedSubtype) {
            this(playerId, filter, singleUse, entryCounterType, grantedSubtype,
                    false, false, null, null, 0, null, false);
        }

        public GraveyardCastFilterPermission(UUID playerId, CardPredicate filter,
                                             boolean anyGraveyard, boolean exileInsteadOfGraveyard) {
            this(playerId, filter, false, null, null, anyGraveyard,
                    exileInsteadOfGraveyard, null, null, 0, null, false);
        }

        public GraveyardCastFilterPermission(UUID playerId, CardPredicate filter,
                                             boolean singleUse, CounterType entryCounterType,
                                             CardSubtype grantedSubtype, boolean anyGraveyard,
                                             boolean exileInsteadOfGraveyard,
                                             ForageOrPayManaCost additionalCost,
                                             CounterType enterWithCounter,
                                             int enterWithCounterCount) {
            this(playerId, filter, singleUse, entryCounterType, grantedSubtype, anyGraveyard,
                    exileInsteadOfGraveyard, additionalCost, enterWithCounter,
                    enterWithCounterCount, null, false);
        }

        public GraveyardCastFilterPermission(UUID playerId, CardPredicate filter, boolean singleUse,
                                             UUID graveyardOwnerId, boolean anyManaType) {
            this(playerId, filter, singleUse, null, null, false, false, null, null, 0,
                    graveyardOwnerId, anyManaType);
        }
    }

    /** A turn-scoped grant letting {@code playerId} play matching cards from the named graveyards. */
    public record GraveyardPlayFilterPermission(UUID playerId, GraveyardSearchScope scope,
                                                CardPredicate filter) implements CardEffect {}

    /**
     * A one-card, source-linked cast grant that also covers cards exiled with the source later in the
     * turn (CR 611.2c: a continuous effect that only modifies the rules is not limited to the objects
     * that existed when it began). Each card it covers is materialised as an {@link ExileCastPermission}
     * sharing {@code grantId}; consuming one removes the whole grant.
     */
    public record ExileCastWildcardPermission(UUID grantId, UUID sourcePermanentId, UUID sourceCardId,
                                              UUID castingPlayerId, boolean ownOnly, CardPredicate filter,
                                              boolean withoutPayingManaCost,
                                              boolean putOnBottomOfOwnersLibrary) implements CardEffect {}

    /** One source-linked, one-card cast grant created by a temporary activated ability. */
    public record ExileCastPermission(UUID grantId, UUID sourcePermanentId, UUID castingPlayerId,
                                      UUID cardId, boolean withoutPayingManaCost,
                                      boolean putOnBottomOfOwnersLibrary,
                                      boolean payLifeEqualToManaValue) implements CardEffect {
        public ExileCastPermission(UUID grantId, UUID sourcePermanentId, UUID castingPlayerId,
                                   UUID cardId, boolean withoutPayingManaCost) {
            this(grantId, sourcePermanentId, castingPlayerId, cardId, withoutPayingManaCost,
                    false, false);
        }

        public ExileCastPermission(UUID grantId, UUID sourcePermanentId, UUID castingPlayerId,
                                   UUID cardId, boolean withoutPayingManaCost,
                                   boolean putOnBottomOfOwnersLibrary) {
            this(grantId, sourcePermanentId, castingPlayerId, cardId, withoutPayingManaCost,
                    putOnBottomOfOwnersLibrary, false);
        }
    }

    /** A turn-scoped single-use grant to cast one foretold card owned by the player. */
    public record ForetoldCardCastPermission(UUID playerId, boolean singleUse) implements CardEffect {
    }



    /** Number of conspire copies still to queue for each spell cast with conspire cost(s) paid
     *  (CR 702.78). Each instance of conspire is paid separately and queues its own "copy it, you
     *  may choose a new target" trigger. Taken into the cast's rules event as the spell becomes cast
     *  ({@code SpellEvents}). */
    public final Map<UUID, Integer> conspiredSpellCopyCounts = new ConcurrentHashMap<>();

    /** Number of casualty copies still to queue for each just-cast spell. */
    public final Map<UUID, Integer> casualtySpellCopyCounts = new ConcurrentHashMap<>();


    /** Mana choices awaiting completion whose chosen mana carries a {@link ManaCopyRider} (Primal
     *  Wellspring's color choice; Sunken Palace's mana after a land-mana replacement prompt), keyed by
     *  source permanent id, then by rider, to the number of units still to be tagged. */
    public final Map<UUID, Map<ManaCopyRider, Integer>> pendingCopyRiderManaChoices = new ConcurrentHashMap<>();





    /** Spells whose mana was paid using a pending any-type mana permission. */
    public final Set<UUID> spellsPaidUsingPendingAnyManaTypeThisTurn = ConcurrentHashMap.newKeySet();


    /**
     * Paradigm (CR 702.192): delayed triggers that fire at the beginning of each of the
     * controller's precombat main phases for the rest of the game.
     */
    public final List<ParadigmDelayedTrigger> paradigmDelayedTriggers = Collections.synchronizedList(new ArrayList<>());

    public record ParadigmDelayedTrigger(UUID controllerId, Card spellPrototype) {}

    /**
     * A spell exiled with delay counters by Ertai's Meddling.
     *
     * @param cardId        the exiled card
     * @param controllerId  the player whose upkeeps remove the counters — the spell's controller
     * @param counters      delay counters still on the card
     * @param originalEntry a snapshot of the stack entry the spell had, restored when the last
     *                      counter is removed so the spell keeps its X value and targets
     * @param meddlerId     the controller of the resolved Ertai's Meddling, who controls its delay triggers
     * @param meddlingCard  the Ertai's Meddling card that is the source of its delay triggers
     */
    public record DelayedSpellExile(UUID cardId, UUID controllerId, int counters, StackEntry originalEntry,
                                    UUID meddlerId, Card meddlingCard) {}

    /** A spell exiled with suspend counters, waiting to be cast by its owner when the last counter is removed. */
    public record SuspendedSpellExile(UUID cardId, UUID ownerId, int counters) {}

    /** Spell names a player has already resolved while controlling (for Paradigm's "first time" check). */
    public final Map<UUID, Set<String>> paradigmResolvedSpellNames = new ConcurrentHashMap<>();

    /**
     * Remaining exiled cards to cast for free during an in-progress resolution (Improvisation
     * Capstone, Brilliant Ultimatum, Spelltwine's copies). Drained one card at a time so a cast that
     * pauses for target selection can resume the rest.
     */
    public final Deque<UUID> pendingFreeCastQueue = new ArrayDeque<>();

    /**
     * Ids in {@link #pendingFreeCastQueue} that are copies, not real cards — they are cast as copies
     * and cease to exist on resolution (CR 707.10a) instead of being put into a graveyard.
     */
    public final Set<UUID> pendingFreeCastAsCopyIds = ConcurrentHashMap.newKeySet();
    /** The value of X already announced and paid for a queued copy that is cast without paying its mana cost. */
    public final Map<UUID, Integer> pendingFreeCastXValues = new ConcurrentHashMap<>();

    /**
     * Offered free-cast spells whose owner loses life equal to the spell's mana value if it is cast
     * (Kefka, Dancing Mad); resolved and cleared when the free-cast queue finishes.
     */
    public final List<PendingFreeCastLifeLoss> pendingFreeCastLifeLosses = new ArrayList<>();

    /** Free casts from exile paused on a kicker offer, keyed by the exiled card's id. */
    public final Map<UUID, FreeCastKickerOffer> pendingFreeCastKickerOffers = new ConcurrentHashMap<>();

    /**
     * Permanents whose exiled cards must go to the bottom of their owners' libraries once the free cast that is
     * still choosing its targets (or kicker) finishes (Unpredictable Cyclone, Tibalt's Trickery, Possibility Storm).
     */
    public final Set<UUID> deferredExileBottomSourceIds = ConcurrentHashMap.newKeySet();

    /** Exiled cards whose free cast already had its kicker offer answered (so it is not offered twice). */
    public final Set<UUID> freeCastKickerDecidedIds = ConcurrentHashMap.newKeySet();

    /** Exiled cards whose kicker cost was paid for the free cast in progress; the spell enters the stack kicked. */
    public final Set<UUID> freeCastKickedIds = ConcurrentHashMap.newKeySet();

    /**
     * Number of modes chosen for a queued free cast of an escalate spell, keyed by the id of the card
     * being cast, when more than one mode was chosen; its escalate cost is paid once targets are chosen.
     */
    public final Map<UUID, Integer> freeCastEscalateModeCounts = new ConcurrentHashMap<>();

    /**
     * Lands in {@link #pendingFreeCastQueue} that the resolving effect lets its controller play
     * rather than cast (Brilliant Ultimatum, Gix, Yawgmoth Praetor). They are played only on the
     * controller's turn while they have a land play left.
     */
    public final Set<UUID> pendingFreeCastLandPlayIds = ConcurrentHashMap.newKeySet();

    /**
     * Adventurer cards in {@link #pendingFreeCastQueue} that are cast as their Adventure rather than as
     * the card itself, because only the Adventure's mana value fits the effect's limit (CR 716.3a).
     */
    public final Set<UUID> pendingFreeCastAdventureIds = ConcurrentHashMap.newKeySet();

    /**
     * Total mana value still available to the spells in {@link #pendingFreeCastQueue} when their
     * cast shares one limit (Primeval Spawn), or null when it is not limited. Each spell spends its
     * own mana value as it is cast, so a split card spends only the half being cast (CR 709.3a).
     */
    public Integer pendingFreeCastManaValueBudget;

    /** Spellweaver Volute copy state waiting for the copied spell to be cast and the Aura reattached. */
    public PendingSpellweaverVoluteReattachment pendingSpellweaverVoluteReattachment;

    /**
     * Cards exiled by a free-cast process that should go to their owners' graveyards when casting
     * finishes (Epic Experiment). Empty when unused. Cleared by
     * {@code ExileFreeCastQueueSupport.putRemainderIntoOwnersGraveyards}.
     */
    public final List<UUID> pendingExileFreeCastRemainderToGraveyard = new ArrayList<>();

    /** Cards exiled by a free-cast process that should return to their owner's library bottom. */
    public final List<UUID> pendingExileFreeCastRemainderToLibraryBottom = new ArrayList<>();

    /** Cards exiled by a free-cast process that should be put into their owners' hands. */
    public final List<UUID> pendingExileFreeCastRemainderToHand = new ArrayList<>();

    /** Delayed triggers from Chancellor-style opening hand reveals.
     *  Fires once per opponent when they cast their first spell of the game. */
    public final List<OpeningHandRevealTrigger> openingHandRevealTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Delayed mana triggers from Chancellor-style opening hand reveals.
     *  Fires at the beginning of the revealing player's first precombat main phase. */
    public final List<OpeningHandRevealTrigger> openingHandManaTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Mandatory first-upkeep abilities registered by opening-hand reveals. */
    public final List<OpeningHandRevealTrigger> openingHandUpkeepTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Tracks which players have cast their first spell of the game (for opening hand triggers). */
    public final Set<UUID> playersWhoCastFirstSpellInGame = ConcurrentHashMap.newKeySet();




    /** Remaining plays for each shared limited exile-play permission group. */
    public final Map<UUID, Integer> exilePlayPermissionGroupUsesRemaining = new ConcurrentHashMap<>();
    /** Spell type choices already used by permissions allowing one spell of each card type. */
    public final Map<UUID, List<Set<CardType>>> exileSpellTypePermissionHistory = new ConcurrentHashMap<>();
    public final Set<UUID> plottedCardIds = ConcurrentHashMap.newKeySet();
    /** Maps a Discord-created exiled copy to the Discord permanent whose ability created it. */
    public final Map<UUID, UUID> discordCopySourcePermanents = new ConcurrentHashMap<>();
    /** Exiled card UUIDs that have stash counters. */
    public final Set<UUID> stashCounterCardIds = ConcurrentHashMap.newKeySet();
    /** Depth counter for batching "cards leave graveyard" triggers (one trigger per batch). */
    public int graveyardLeaveNotificationDepth = 0;
    /** Depth counter for tap events that are paid or performed as one batch. */
    public int permanentTapTriggerBatchDepth;
    /**
     * The "one or more" tap abilities that have already triggered in the current tap batch, by source permanent: each
     * by its ability's limit key ({@code TriggeredAbility.limitKey()}).
     */
    public final Map<UUID, Set<String>> permanentTapTriggerBatchFiredAbilities = new ConcurrentHashMap<>();

    /**
     * Opens a batch of permanents tapping together (tapping several as a cost, attackers tapping as they're
     * declared): the "whenever one or more permanents become tapped" abilities that fired in it are forgotten when the
     * outermost batch opens and closes. Batches nest.
     */
    public void beginPermanentTapTriggerBatch() {
        if (permanentTapTriggerBatchDepth++ == 0) {
            permanentTapTriggerBatchFiredAbilities.clear();
        }
    }

    /** Closes the batch {@link #beginPermanentTapTriggerBatch} opened. */
    public void endPermanentTapTriggerBatch() {
        if (permanentTapTriggerBatchDepth <= 0) {
            throw new IllegalStateException("No permanent tap trigger batch is active");
        }
        if (--permanentTapTriggerBatchDepth == 0) {
            permanentTapTriggerBatchFiredAbilities.clear();
        }
    }
    /** Players who had one or more cards leave their graveyard this turn (cleared at turn cleanup). Used by Wilt in the Heat cost reduction. */
    public final Set<UUID> playersWhoseCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose graveyards had one or more creature cards leave this turn. */
    public final Set<UUID> playersWhoseCreatureCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Number of cards that left each player's graveyard this turn. */
    public final Map<UUID, Integer> cardsLeftGraveyardCountThisTurn = new ConcurrentHashMap<>();
    /** Depth counter for batching non-dying battlefield departures into one trigger event. */
    public int permanentLeaveNotificationDepth;
    /**
     * Depth counter for a group of simultaneous deaths. While it is positive, CR 616.1 death
     * replacement prompts wait in {@link #deferredDeathReplacementChoices} and are opened in APNAP
     * order when the outermost group ends.
     */
    public int deathReplacementChoiceBatchDepth;
    /** Death replacement prompts raised inside the current group of simultaneous deaths. */
    public final List<PendingInteraction.ColorChoice> deferredDeathReplacementChoices = new ArrayList<>();
    /**
     * CR 616.1 outside state-based actions: {@code true} while the resolving effect's handler has
     * changed nothing yet, so a creature it would kill that has competing death replacements (one of
     * them keeping the card) can be asked about before anything happens, and the effect resolved
     * again once its controller answers. The first removal attempt clears it.
     */
    public boolean deathReplacementsCanRerunEffect;
    /**
     * {@code true} while the resolving effect's handler has changed nothing since it began or since its last
     * coin-flip checkpoint, so a coin flip Krark's Thumb replaces can stop it while the flipping player chooses which
     * results to ignore. Each coin-flip call of the handler consumes it; a checkpoint reopens it.
     */
    public boolean coinFlipChoicesCanRerunEffect;
    /**
     * The coin flips the resolving effect made since it began or since its last coin-flip checkpoint, in order,
     * while a Krark's Thumb result may still stop it; the effect resolved again reads them back instead of flipping.
     */
    public final List<RecordedCoinFlip> recordedCoinFlips = new ArrayList<>();
    /**
     * The answer to Pokey, the Scallywagg's "you may instead roll a d20" question that stopped the resolving effect, kept
     * until the effect resolved again reads it for that same flip; {@code null} when no such answer is waiting.
     */
    public Boolean pokeyReplacementAnswer;
    /** How many of {@link #recordedCoinFlips} the current run of the resolving effect has read back so far. */
    public int replayedCoinFlips;
    /**
     * The resolving effect whose handler resumes at coin-flip checkpoints, or {@code null}. Only that effect's own
     * checkpoints reopen the Krark's Thumb choice window; an effect it dispatches sees a different value.
     */
    public CardEffect coinFlipCheckpointEffect;
    /** Where {@link #coinFlipCheckpointEffect} resumes when resolved again after a Krark's Thumb choice. */
    public int coinFlipResumePoint;
    /**
     * A player's answer whose completion flips a coin before changing anything else and was stopped by a Krark's Thumb
     * flip whose results disagree (a "flip again?" answer, a choice made before a flip). Once every kept result is
     * chosen, the same answer is completed again and its flips read {@link #recordedCoinFlips} back. {@code null} when
     * no completion is waiting.
     */
    public CoinFlipChoiceReplay pendingCoinFlipChoiceReplay;

    /** A player's answer to complete again once the Krark's Thumb results its coin flips asked about are chosen. */
    public sealed interface CoinFlipChoiceReplay permits MayAnswerReplay, InteractionAnswerReplay {
    }
    /**
     * The step (the effect of the resolving stack entry's effect list) whose handler is running and may end on a
     * pending departure batch, or {@code null}. Only that step's own departure may pause: a handler it dispatches
     * for a nested effect, or a second departure of the same step, sees a different value (CR 608.2c).
     */
    public CardEffect stepDepartureEffect;
    /**
     * Death replacements chosen before the resolving effect is resolved again, by dying permanent.
     * The rerun consumes them as its deaths happen; whatever is left is dropped when the rerun ends.
     */
    public final Map<UUID, ChoiceContext.DeathExileReplacement> chosenDeathReplacementsForRerun =
            new ConcurrentHashMap<>();
    /**
     * Permanents whose only death replacement, a "you may put it on top of its owner's library
     * instead", is being asked ahead of a stopped effect or choice completion; the answer is recorded
     * in {@link #chosenDeathReplacementsForRerun}.
     */
    public final Set<UUID> rerunMayLibraryChoices = ConcurrentHashMap.newKeySet();
    /**
     * A permanent choice, multi-permanent choice, list choice or "you may" answer whose completion was stopped before its first removal so
     * that a creature it would kill could have its death replacement chosen first (CR 616.1). Once
     * every such answer is in, the same answer is completed again and its deaths consume
     * {@link #chosenDeathReplacementsForRerun}. {@code null} when no completion is waiting.
     */
    public DeathReplacementChoiceReplay pendingDeathReplacementChoiceReplay;

    /** A player's answer to complete again once the death replacements it asked about are chosen. */
    public sealed interface DeathReplacementChoiceReplay
            permits PermanentChoiceReplay, MultiPermanentChoiceReplay, MayAnswerReplay, ListChoiceReplay,
                    PileChoiceReplay, ActivationCostReplay, SpellCastReplay,
                    GraveyardCastReplay, LibraryTopCastReplay, TurnFaceUpReplay,
                    GraveyardAbilityCostReplay, HandAbilityCostReplay, InteractionAnswerReplay,
                    HandAbilityActivationReplay, AttackDeclarationReplay, BlockDeclarationReplay,
                    ActivationRequestReplay, ManaAbilityLifeCostReplay, MayPaymentReplay, MadnessConvokeReplay,
                    PlotReplay, ExileCastReplay, SplitCastPaymentReplay, FreeCastLifeCostReplay {
    }

    /**
     * A permanent's activated ability stopped before it paid anything, so that the Lich's Duel Mastery its Phyrexian
     * mana paid with life would sacrifice could have its death replacement chosen first (CR 602.2b, CR 616.1): the
     * activation request to make again once every answer is in. The permanent is named by id;
     * {@code preResolvedSource} tells whether the request named it directly rather than by its battlefield index.
     */
    /**
     * An accepted "you may cast it" (a madness cast, an artifact cast by paying life equal to its mana value) stopped
     * before it paid any cost, so that the Lich's Duel Mastery its life payment would sacrifice could have its death
     * replacement chosen first (CR 601.2h, CR 616.1): the prompt to accept again once every answer is in.
     */
    public record MayPaymentReplay(Player player, PendingMayAbility ability) implements DeathReplacementChoiceReplay {
    }

    /**
     * A madness cast paid with convoke stopped before it tapped or paid anything, so that the Lich's Duel Mastery its
     * life payment would sacrifice could have its death replacement chosen first (CR 601.2h, CR 616.1): the convoke
     * choice to complete again once every answer is in.
     */
    public record MadnessConvokeReplay(Player player, MultiPermanentChoiceContext.MadnessConvoke context,
                                       java.util.List<UUID> creatureIds) implements DeathReplacementChoiceReplay {
    }

    /**
     * A spell cast from exile (or from outside the game) stopped once its costs were validated and before any was
     * paid, so that the Lich's Duel Mastery its life payment would sacrifice could have its death replacement chosen
     * first (CR 601.2h, CR 616.1): the cast request, the card by id, to make again once every answer is in.
     */
    public record ExileCastReplay(Player player, UUID exileCardId, Integer xValue, UUID targetId,
                                  java.util.List<UUID> exileCounterCostPermanentIds,
                                  java.util.List<UUID> convokeCreatureIds, java.util.List<UUID> waterbendPermanentIds,
                                  boolean waterbendPaid, java.util.List<UUID> targetIds, boolean autoPass,
                                  boolean copy, boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard,
                                  boolean flashforwardCast, Zone sourceZoneOverride, boolean fromBattlefield,
                                  java.util.List<CounterType> chosenCounterTypes)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A prepared free or "you may cast it" cast stopped before it paid its mana cost, so that the Lich's Duel Mastery
     * the life it pays would sacrifice could have its death replacement chosen first (CR 601.2h, CR 616.1): the
     * prepared cast with its chosen targets, to put on the stack again once every answer is in.
     */
    public record SplitCastPaymentReplay(SplitCardCast.Targeting targeting) implements DeathReplacementChoiceReplay {
    }

    /**
     * A prepared free cast paused before it paid life for its "discard a card or pay life" cost, so that the Lich's
     * Duel Mastery that payment would sacrifice could have its death replacement chosen first (CR 601.2h, CR 616.1):
     * the prepared spell, which its completion changes as it pays the cost, and whether the completion checks
     * "becomes the target of a spell" triggers, as the paused path did.
     */
    public record FreeCastLifeCostReplay(StackEntry entry, boolean checksTargetTriggers)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * Plotting a card from hand (a special action) stopped before it paid its plot cost, so that the Lich's Duel
     * Mastery its Phyrexian mana paid with life would sacrifice could have its death replacement chosen first
     * (CR 616.1): the card, by id, to plot again once every answer is in.
     */
    public record PlotReplay(Player player, UUID cardId) implements DeathReplacementChoiceReplay {
    }

    /**
     * A mana ability stopped before it paid its life cost, so that the Lich's Duel Mastery that payment would
     * sacrifice could have its death replacement chosen first (CR 605.3a, CR 616.1): the land tapped for mana by id,
     * or {@code null} for Channel's "pay 1 life: add {C}".
     */
    public record ManaAbilityLifeCostReplay(Player player, UUID permanentId) implements DeathReplacementChoiceReplay {
    }

    public record ActivationRequestReplay(Player player, UUID permanentId, boolean preResolvedSource,
                                          int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId,
                                          Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                          java.util.List<UUID> targetIds, Map<UUID, Integer> damageAssignments,
                                          java.util.List<Integer> exileXGraveyardCardIndices,
                                          java.util.List<UUID> exileAnyGraveyardCardIds,
                                          UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen,
                                          java.util.List<UUID> revealedHandCardIds,
                                          UUID putExiledCardIntoGraveyardCardId,
                                          java.util.List<UUID> putBottomLibraryCardIds)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A hand card's activated ability stopped once its costs were validated and before any of them was paid, so that
     * the Lich's Duel Mastery its life payment would sacrifice could have its death replacement chosen first
     * (CR 602.2b, CR 616.1): the activation request to make again once every answer is in, the card by id.
     */
    public record HandAbilityActivationReplay(Player player, UUID cardId, Integer abilityIndex, UUID targetId,
                                              Integer xValue, java.util.List<UUID> targetIds)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * An attack declaration stopped once its costs were validated and before any of them was paid, so that the Lich's
     * Duel Mastery its life payment would sacrifice could have its death replacement chosen first (CR 508.1j, CR 616.1):
     * the declaration prompt and the declared attackers, to declare again once every answer is in.
     */
    public record AttackDeclarationReplay(Player player, PendingInteraction.AttackerDeclaration declaration,
                                          java.util.List<Integer> attackerIndices, Map<Integer, UUID> attackTargets,
                                          java.util.List<java.util.List<Integer>> bands)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A block declaration stopped once its costs were validated and before any of them was paid, so that the Lich's
     * Duel Mastery its life payment would sacrifice could have its death replacement chosen first (CR 509.1f,
     * CR 616.1): the declaration prompt and the declared blocks, to declare again (with the costs accepted) once every
     * answer is in.
     */
    public record BlockDeclarationReplay(com.github.laxika.magicalvibes.model.effect.PayBlockCostsEffect blocks)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A spell cast from a graveyard (flashback, escape, a graveyard-cast permission, an Adventure, ...) stopped once its
     * costs were validated and before any of them was paid, so that a permanent it sacrifices as a cost could have its
     * death replacement chosen first (CR 616.1, CR 601.2h): the cast request to make again once every answer is in.
     * Plain data: the card by id in {@code graveyardOwnerId}'s graveyard, permanents by id.
     */
    public record GraveyardCastReplay(Player player, UUID graveyardOwnerId, UUID castCardId, Integer xValue,
                                      UUID targetId, java.util.List<UUID> targetIds,
                                      java.util.List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                      java.util.List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                      UUID sacrificePermanentId, java.util.List<UUID> additionalCostSacrificePermanentIds,
                                      Map<UUID, Integer> damageAssignments, java.util.List<UUID> beholdPermanentIds,
                                      java.util.List<Integer> beholdHandCardIndices,
                                      java.util.List<Integer> discardHandCardIndices,
                                      java.util.List<UUID> convokeCreatureIds, Integer sharedColorDiscardHandCardIndex)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A spell cast from the top of a library stopped once its costs were validated and before any of them was paid,
     * so that a permanent it sacrifices as a cost could have its death replacement chosen first (CR 616.1,
     * CR 601.2h): the cast request to make again once every answer is in, while {@code castCardId} is still the top
     * card of {@code libraryOwnerId}'s library.
     */
    public record LibraryTopCastReplay(Player player, Integer xValue, UUID targetId,
                                       java.util.List<UUID> counterCostPermanentIds,
                                       java.util.List<UUID> additionalCostSacrificePermanentIds, boolean castFaceDown,
                                       boolean castAsAdventure, UUID castCardId, UUID libraryOwnerId)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * Turning a face-down permanent face up (a special action, CR 116.2b) stopped once its costs were validated and
     * before any of them was paid, so that a permanent its morph cost sacrifices could have its death replacement
     * chosen first (CR 616.1): the request to make again once every answer is in. The face-down permanent and the
     * cost's permanents are named by id.
     */
    public record TurnFaceUpReplay(Player player, UUID permanentId, Integer revealedHandCardIndex,
                                   java.util.List<UUID> costPermanentIds, Integer xValue, Boolean payMorphCost)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A spell cast from hand stopped once its costs were validated and before any of them was paid, so that a
     * permanent it sacrifices as a cost could have its death replacement chosen first (CR 616.1, CR 601.2h): the
     * cast request to make again once every answer is in. Plain data (permanents and cards by id), so a simulation
     * copy casts it on its own state. {@code opponentChosenTargetPlayerId} is the opponent who chose the spell's
     * target player for an opponent-chosen target spell, stamped on its stack entry after the cast; otherwise
     * {@code null}. {@code commandCastCardId} is the commander being cast from the command zone (the cast is made
     * again from there), or {@code null} for a cast from hand. {@code foreignHandOwnerId} is the player whose hand
     * the spell is cast from when that isn't the caster's own (Sen Triplets), or {@code null}.
     */
    public record SpellCastReplay(Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, java.util.List<UUID> targetIds,
                                  java.util.List<UUID> convokeCreatureIds, boolean fromGraveyard,
                                  UUID sacrificePermanentId, Integer phyrexianLifeCount,
                                  java.util.List<UUID> alternateCostSacrificePermanentIds,
                                  Integer exileGraveyardCardIndex, java.util.List<Integer> exileGraveyardCardIndices,
                                  boolean kicked, Integer discardHandCardIndex,
                                  java.util.List<Integer> discardHandCardIndices, boolean forceAlternateCost,
                                  java.util.List<UUID> conspireCreatureIds, Integer modalXValue,
                                  java.util.List<Integer> spliceHandCardIndices,
                                  java.util.List<UUID> imposedSacrificePermanentIds,
                                  java.util.List<UUID> additionalCostSacrificePermanentIds,
                                  java.util.List<String> repeatedAdditionalCosts, boolean buyback, boolean overloaded,
                                  java.util.List<UUID> spliceCostPermanentIds, Integer sharedColorDiscardHandCardIndex,
                                  UUID beholdPermanentId, Integer beholdHandCardIndex,
                                  java.util.List<UUID> beholdPermanentIds, java.util.List<Integer> beholdHandCardIndices,
                                  CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                                  boolean waterbendPaid, Boolean payLifeForAdditionalCost, boolean adventure,
                                  boolean giftPromised, UUID chosenAdditionalCostObjectId,
                                  java.util.List<UUID> casualtyCreatureIds, UUID castCardId,
                                  UUID opponentChosenTargetPlayerId, UUID commandCastCardId,
                                  UUID foreignHandOwnerId)
            implements DeathReplacementChoiceReplay {

        /** This replay, stamping {@code playerId} as the opponent who chose the spell's target player. */
        public SpellCastReplay withOpponentChosenTargetPlayerId(UUID playerId) {
            return new SpellCastReplay(player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                    convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                    alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                    discardHandCardIndex, discardHandCardIndices, forceAlternateCost, conspireCreatureIds,
                    modalXValue, spliceHandCardIndices, imposedSacrificePermanentIds,
                    additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback, overloaded,
                    spliceCostPermanentIds, sharedColorDiscardHandCardIndex, beholdPermanentId, beholdHandCardIndex,
                    beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType,
                    waterbendPaid, payLifeForAdditionalCost, adventure, giftPromised, chosenAdditionalCostObjectId,
                    casualtyCreatureIds, castCardId, playerId, commandCastCardId, foreignHandOwnerId);
        }
    }

    /**
     * An activation stopped right before it pays its sacrifice costs, so that a permanent it sacrifices could have
     * its death replacement chosen first (CR 616.1, CR 601.2h): the arguments to complete it with once every answer
     * is in. Its activated permanent is named by id, so a simulation copy completes it on its own battlefield.
     */
    public record ActivationCostReplay(Player player, UUID permanentId, ActivatedAbility ability,
                                       java.util.List<com.github.laxika.magicalvibes.model.effect.CardEffect> abilityEffects,
                                       int xValue, UUID targetId, Zone targetZone, boolean nonTargeting,
                                       java.util.List<UUID> targetIds, Map<UUID, Integer> damageAssignments,
                                       java.util.List<UUID> chosenCostPermanentIds, java.util.List<UUID> sacrificedCardIds,
                                       Card discardedCardSnapshot, Card exiledCostCardSnapshot,
                                       java.util.List<UUID> exiledCardIds)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A graveyard card's activated ability stopped right before it sacrifices the permanents chosen for its costs, so
     * that one of them could have its death replacement chosen first (CR 616.1, CR 601.2h): the arguments to complete
     * the activation with once every answer is in. The chosen permanents stay in
     * {@link #pendingActivationCostSacrificeIds}.
     */
    public record GraveyardAbilityCostReplay(Player player, Card card, ActivatedAbility ability, int xValue,
                                             UUID targetId, java.util.List<UUID> graveyardTargetIds)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A hand card's activated ability stopped right before it sacrifices the permanents chosen for its costs, so that
     * one of them could have its death replacement chosen first (CR 616.1, CR 601.2h): the arguments to complete the
     * activation with once every answer is in. {@code paysSourceCost} tells whether the completion still discards or
     * reveals the card as its cost; otherwise it keeps the card where it is. The chosen permanents stay in
     * {@link #pendingActivationCostSacrificeIds}.
     */
    public record HandAbilityCostReplay(Player player, Card card, ActivatedAbility ability, int abilityIndex,
                                        int xValue, UUID targetId, int stackSizeBeforeCosts,
                                        java.util.List<UUID> targetIds, boolean paysSourceCost)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * {@code replay} for a simulation copy: a prepared free cast's spell, which its completion changes as it pays the
     * cost, is copied; every other replay is plain data and is shared.
     */
    private static DeathReplacementChoiceReplay copyDeathReplacementChoiceReplay(DeathReplacementChoiceReplay replay) {
        if (replay instanceof FreeCastLifeCostReplay lifeCostReplay) {
            return new FreeCastLifeCostReplay(new StackEntry(lifeCostReplay.entry()),
                    lifeCostReplay.checksTargetTriggers());
        }
        if (replay instanceof PermanentChoiceReplay choiceReplay
                && choiceReplay.choice().context() instanceof PermanentChoiceContext.FreeCastSacrificeCost cost) {
            PendingInteraction.PermanentChoice choice = choiceReplay.choice();
            return new PermanentChoiceReplay(new PendingInteraction.PermanentChoice(choice.playerId(),
                    choice.validPermanentIds(), choice.validPlayerIds(), cost.deepCopy(), choice.prompt()),
                    choiceReplay.permanentId());
        }
        if (replay instanceof MultiPermanentChoiceReplay multiReplay
                && multiReplay.choice().context() instanceof MultiPermanentChoiceContext.FreeCastTapCost cost) {
            PendingInteraction.MultiPermanentChoice choice = multiReplay.choice();
            return new MultiPermanentChoiceReplay(new PendingInteraction.MultiPermanentChoice(choice.playerId(),
                    choice.validIds(), choice.validPlayerIds(), choice.validCardIds(), choice.maxCount(),
                    new MultiPermanentChoiceContext.FreeCastTapCost(new StackEntry(cost.entry()),
                            cost.requiredCount()), choice.prompt()), multiReplay.permanentIds());
        }
        return replay;
    }

    /** A pile separation's "which pile" answer whose chosen pile is destroyed or sacrificed. */
    public record PileChoiceReplay(boolean accepted) implements DeathReplacementChoiceReplay {
    }

    /** A permanent choice answer to complete again: the prompt that was answered and the chosen permanent. */
    public record PermanentChoiceReplay(PendingInteraction.PermanentChoice choice, UUID permanentId)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A multi-permanent choice answer to complete again: the prompt that was answered and the chosen
     * permanents.
     */
    public record MultiPermanentChoiceReplay(PendingInteraction.MultiPermanentChoice choice,
                                             java.util.List<UUID> permanentIds)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * A "you may" prompt answer to complete again: the player who answered it, the prompt and whether
     * it was accepted.
     */
    public record MayAnswerReplay(Player player, PendingMayAbility ability, boolean accepted)
            implements DeathReplacementChoiceReplay, CoinFlipChoiceReplay {
    }

    /**
     * A list choice answer to complete again: the prompt that was answered and the chosen option
     * (an "unless that player pays" prompt whose refusal only destroys, e.g. Erosion).
     */
    public record ListChoiceReplay(PendingInteraction.ColorChoice choice, String chosen)
            implements DeathReplacementChoiceReplay {
    }

    /**
     * An answer to a registry-managed interaction to complete again (Sylvan Library's choice, Lim-Dûl's Vault's
     * "pay 1 life and look again", a library reveal choice that pays life for each card): the player who answered,
     * the interaction, made active again before the answer is dispatched to it once more, and the answer. The answer
     * is the engine's interaction answer record, which this module can't name.
     */
    public record InteractionAnswerReplay(Player player, PendingInteraction interaction, Object answer)
            implements DeathReplacementChoiceReplay, CoinFlipChoiceReplay {
    }
    /** Transient field: while a player is choosing a card to exile from hand, identifies the player who should
     *  gain permission to play that card for as long as it remains exiled (e.g. Fiend of the Shadows). Null when
     *  the exiling effect does not grant play permission to a controller. */

    /** Tracks how many cards each player has drawn this turn. */
    public final Map<UUID, Integer> cardsDrawnThisTurn = new ConcurrentHashMap<>();

    /** Tracks how many cards each player drew during the immediately preceding turn. */
    public final Map<UUID, Integer> cardsDrawnLastTurn = new ConcurrentHashMap<>();

    /** Tracks the card ids each player has drawn this turn, in draw order. Used by effects that must
     *  identify the specific cards "drawn this turn" (e.g. Sylvan Library). Cleared each turn. */
    public final Map<UUID, List<UUID>> cardsDrawnThisTurnIds = new ConcurrentHashMap<>();

    /** Tracks how many cards each player has discarded this turn (any discard, any source). Used by
     *  "cards discarded this turn" effects, e.g. Dream Salvage. Cleared at the start of each turn. */
    public final Map<UUID, Integer> cardsDiscardedThisTurn = new ConcurrentHashMap<>();

    /** Whether life payments are currently being summed into {@link #activationLifePaid}: set when an
     *  activated ability's activation begins and cleared once its activation triggers are collected. */
    public boolean trackingActivationLifePaid;

    /** Life paid so far as part of the activation currently being announced (including life paid in
     *  place of Phyrexian mana), so "if life was paid to activate it" triggers see the amount actually
     *  paid (Verrak, Warped Sengir). */
    public int activationLifePaid;

    /** Tracks how many nonland cards each player has discarded this turn (any discard, any source), so
     *  "for the first time each turn" discard triggers can count discards made before their source
     *  entered (Veronica, Dissident Scribe). Cleared at the start of each turn. */
    public final Map<UUID, Integer> nonlandCardsDiscardedThisTurn = new ConcurrentHashMap<>();

    /** Tracks how many cards each player cycled this turn. Cleared at the start of each turn. */
    public final Map<UUID, Integer> cardsCycledThisTurn = new ConcurrentHashMap<>();

    /** Player and count for a discard event whose cards are being processed one at a time. */
    public UUID discardEventPlayerId;
    public int discardEventCardCount;
    /** The cards in the active discard event, retained for event-level type conditions. */
    public final List<Card> discardEventCards = new ArrayList<>();

    /** Mana value of the most recently discarded card (any player, any source), recorded by the
     *  central discard hook. Read by {@code LastDiscardedCardManaValue} so a later effect of the same
     *  spell can scale off the card just discarded (Blast of Genius). */
    public int lastDiscardedCardManaValue;
    /** Greatest mana value among cards discarded by the current discard effect. */
    public int greatestDiscardedCardManaValue;
    /** Card types of the most recently discarded card, including additional card types. */
    public Set<CardType> lastDiscardedCardTypes = Set.of();

    /** Counts colored mana symbols on the most recently milled card. */
    public final Map<ManaColor, Integer> lastMilledCardColorSymbols = new ConcurrentHashMap<>();

    /**
     * Cards that players picked during an "each player searches ... reveal ... put into hand" instruction
     * (Weird Harvest), held out per player until every player has chosen: the choices are made in APNAP
     * order but the cards are revealed and put into hands at the same time (CR 101.4).
     */
    public final Map<UUID, List<Card>> eachPlayerSearchDeferredToHand = new ConcurrentHashMap<>();

    /**
     * Basic lands that players picked during an "each player searches ... puts it onto the battlefield"
     * instruction (Field of Ruin), held out of the library per player until every player has chosen:
     * the choices are made in APNAP order but the lands enter the battlefield at the same time (CR 101.4).
     */
    public final Map<UUID, List<Card>> eachPlayerSearchDeferredToBattlefield = new ConcurrentHashMap<>();

    /** Revealed hand cards waiting to be stamped onto the spell whose additional cost they paid. */
    public final Map<UUID, Card> pendingRevealedPowerCostCards = new ConcurrentHashMap<>();

    /** Card discarded to pay a spell's discard cost, keyed by the spell's card id, waiting to be stamped
     *  onto the spell's stack entry when a kicker discard is what it paid for. */
    public final Map<UUID, Card> pendingDiscardedCostCards = new ConcurrentHashMap<>();


    /** Tracks how much life each player has gained so far this turn (for "if you gained life this turn"
     *  conditions, e.g. Streets of New Capenna's Infusion cards). Cleared at the start of each turn. */
    public final Map<UUID, Integer> lifeGainedThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much life each player has lost so far this turn (damage causes loss of life, so this
     *  accumulates both direct life loss and damage). Recorded from the shared life-loss trigger hook
     *  and direct life-payment bookkeeping. Used by Wound Reflection ("each opponent loses life equal
     *  to the life they lost this turn"). Cleared at the start of each turn. */
    public final Map<UUID, Integer> lifeLostThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much life each player lost during the immediately preceding turn. */
    public final Map<UUID, Integer> lifeLostLastTurn = new ConcurrentHashMap<>();

    /** Tracks which permanents dealt combat damage to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. */
    public final Map<UUID, Set<UUID>> combatDamageToPlayersThisTurn = new ConcurrentHashMap<>();
    /** Tracks which players were dealt combat damage by a creature with each name this game. */
    public final Map<String, Set<UUID>> combatDamageToPlayersByCreatureNameThisGame =
            new ConcurrentHashMap<>();
    /** Tracks which players each permanent dealt combat damage to during the current combat. */
    public final Map<UUID, Set<UUID>> combatDamageToPlayersThisCombat = new ConcurrentHashMap<>();
    /** Tracks how much combat damage each player was dealt this turn. */
    public final Map<UUID, Integer> combatDamageDealtToPlayersThisTurn = new ConcurrentHashMap<>();
    public final Set<UUID> playersDealtCombatDamageSinceTheirLastTurn = ConcurrentHashMap.newKeySet();
    public final Set<UUID> playersDealtCombatDamageLastTurn = ConcurrentHashMap.newKeySet();

    /** Permanent IDs of creatures that dealt combat damage to a creature this turn. */
    public final Set<UUID> combatDamageSourcesThatDealtToCreaturesThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents dealt noncombat damage (spells/abilities) to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. Combined with
     *  {@link #combatDamageToPlayersThisTurn}, gives every source that dealt any damage to a player
     *  (Giltspire Avenger). Cleared at turn cleanup. */
    public final Map<UUID, Set<UUID>> noncombatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Names of permanents that dealt damage to each player during this turn. */
    public final Map<UUID, Set<String>> permanentDamageSourceNamesToPlayersThisTurn =
            new ConcurrentHashMap<>();

    /** Names of permanents that dealt damage to each player during the immediately preceding turn. */
    public final Map<UUID, Set<String>> permanentDamageSourceNamesToPlayersLastTurn =
            new ConcurrentHashMap<>();

    /** Tracks which creature permanents dealt damage to which players this turn. */
    public final Map<UUID, Set<UUID>> creatureDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creatures attacked which players this turn ("target creature that attacked you
     *  this turn", Jabari's Influence). Maps attacking permanent UUID → set of attacked player UUIDs;
     *  creatures that attacked only a planeswalker never appear, since attacking a planeswalker its
     *  controller owns is not attacking that player. Cleared at turn cleanup. */
    public final Map<UUID, Set<UUID>> playersAttackedThisTurn = new ConcurrentHashMap<>();
    /** Controller and creature subtypes each attacker had when it was declared as an attacker this turn. */
    public final Map<UUID, Set<DeclaredAttackerSnapshot>> declaredAttackerSnapshotsThisTurn =
            new ConcurrentHashMap<>();
    /** Players made afraid by each controller's qualifying attacks, even after the attacker leaves. */
    public final Map<UUID, Set<UUID>> playersAfraidOfControllerAfterAttackThisTurn = new ConcurrentHashMap<>();
    /** Players directly attacked during the current combat's attacker declaration. */
    public final Set<UUID> playersAttackedThisCombat = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Set<UUID>> creaturesThatSaddledPermanentThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creaturesThatCrewedPermanentThisTurn = new ConcurrentHashMap<>();
    /** Creature subtypes represented by creatures that crewed each permanent this turn. */
    public final Map<UUID, Set<CardSubtype>> crewedPermanentSubtypesThisTurn = new ConcurrentHashMap<>();
    public final TargetOpponentsDiscardThenDrawState targetOpponentsDiscardThenDraw =
            new TargetOpponentsDiscardThenDrawState();
    public final EachOpponentDrawsThenControllerDrawsState eachOpponentDrawsThenControllerDraws =
            new EachOpponentDrawsThenControllerDrawsState();
    /** Full beginning phases queued after the current combat phase. */
    public int additionalBeginningPhasesAfterCombat;
    /** The normal step to resume after all queued beginning phases are complete. */
    /** Whether the engine is processing an inserted beginning phase's untap step. */
    public boolean additionalBeginningPhaseUntapInProgress;
    public int additionalUpkeepStepsAfterCombat;
    /** True when the pending extra combat phase was created after the pending additional upkeep steps, so it
     *  occurs first (CR 500.8, 500.9). */
    public boolean additionalCombatCreatedAfterUpkeepSteps;
    public TurnStep additionalUpkeepReturnStep;

    /** Tracks which players attacked each player or one of that player's planeswalkers this turn. */
    public final Map<UUID, Set<UUID>> playersWhoAttackedPlayerOrPlaneswalkerThisTurn = new ConcurrentHashMap<>();
    /** Tracks which players declared attackers against each player this turn. */
    public final Map<UUID, Set<UUID>> playersWhoAttackedPlayersThisTurn = new ConcurrentHashMap<>();
    /** Tracks which players directly attacked each player during their most recent turn. */
    public final Map<UUID, Set<UUID>> playersWhoAttackedPlayersLastTurn = new ConcurrentHashMap<>();

    /** An attacker's controller and effective creature subtypes at the moment it was declared as an attacker. */
    public record DeclaredAttackerSnapshot(UUID controllerId, Set<CardSubtype> subtypes) {
        public DeclaredAttackerSnapshot {
            subtypes = Set.copyOf(subtypes);
        }
    }

    /** Snapshots an attacker's controller and creature subtypes as it is declared as an attacker. */
    public void recordDeclaredAttackerSnapshot(UUID attackerPermanentId, UUID controllerId,
                                               Set<CardSubtype> subtypes) {
        if (attackerPermanentId == null || controllerId == null) {
            return;
        }
        declaredAttackerSnapshotsThisTurn
                .computeIfAbsent(attackerPermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(new DeclaredAttackerSnapshot(controllerId, subtypes));
    }

    /** Records that {@code attackerPermanentId} was declared as an attacker against {@code playerId}. */
    public void recordAttackAgainstPlayer(UUID attackerPermanentId, UUID playerId) {
        if (attackerPermanentId == null || playerId == null) {
            return;
        }
        playersAttackedThisTurn
                .computeIfAbsent(attackerPermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
        playersAttackedThisCombat.add(playerId);
    }

    /** Records that a player declared one or more attackers against another player this turn. */
    public void recordPlayerAttackAgainstPlayer(UUID attackingPlayerId, UUID playerId) {
        if (attackingPlayerId == null || playerId == null) {
            return;
        }
        playersWhoAttackedPlayersThisTurn
                .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                .add(attackingPlayerId);
    }

    /** Snapshots one player's direct player attacks for that player's next "last turn" lookup. */
    public void snapshotPlayerAttacksForLastTurn(UUID attackingPlayerId) {
        if (attackingPlayerId == null) return;
        playersWhoAttackedPlayersLastTurn.values().forEach(attackingPlayers ->
                attackingPlayers.remove(attackingPlayerId));
        playersWhoAttackedPlayersThisTurn.forEach((attackedPlayerId, attackingPlayers) -> {
            if (attackingPlayers.contains(attackingPlayerId)) {
                playersWhoAttackedPlayersLastTurn
                        .computeIfAbsent(attackedPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(attackingPlayerId);
            }
        });
        playersWhoAttackedPlayersLastTurn.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /** Records that a player attacked a player or one of that player's planeswalkers this turn. */
    public void recordAttackAgainstPlayerOrPlaneswalker(UUID attackingPlayerId, UUID playerId) {
        if (attackingPlayerId == null || playerId == null) {
            return;
        }
        playersWhoAttackedPlayerOrPlaneswalkerThisTurn
                .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                .add(attackingPlayerId);
    }

    public void recordCreatureSaddlingPermanent(UUID saddledPermanentId, UUID creatureId) {
        if (saddledPermanentId == null || creatureId == null) return;
        creaturesThatSaddledPermanentThisTurn
                .computeIfAbsent(saddledPermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(creatureId);
    }

    public void recordCreatureCrewingPermanent(UUID crewedPermanentId, UUID creatureId) {
        if (crewedPermanentId == null || creatureId == null) return;
        creaturesThatCrewedPermanentThisTurn
                 .computeIfAbsent(crewedPermanentId, ignored -> ConcurrentHashMap.newKeySet())
                 .add(creatureId);
    }

    public void recordCreatureCrewingPermanentSubtype(UUID crewedPermanentId, CardSubtype subtype) {
        if (crewedPermanentId == null || subtype == null) return;
        crewedPermanentSubtypesThisTurn
                .computeIfAbsent(crewedPermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(subtype);
    }

    /** Records that {@code sourcePermanentId} dealt noncombat damage to {@code playerId} this turn.
     *  No-op when the source permanent is unknown. */
    public void recordNoncombatDamageSourceToPlayer(UUID sourcePermanentId, UUID playerId) {
        if (sourcePermanentId == null) {
            return;
        }
        noncombatDamageToPlayersThisTurn
                .computeIfAbsent(sourcePermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
    }

    /** Records the name of a permanent that dealt damage to a player this turn. */
    public void recordPermanentDamageSourceNameToPlayer(String sourceName, UUID playerId) {
        if (sourceName == null || playerId == null) {
            return;
        }
        permanentDamageSourceNamesToPlayersThisTurn
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(sourceName);
    }

    /** Records that a creature permanent dealt damage to a player this turn. */
    public void recordCreatureDamageSourceToPlayer(UUID sourcePermanentId, UUID playerId) {
        if (sourcePermanentId == null || playerId == null) {
            return;
        }
        creatureDamageToPlayersThisTurn
                .computeIfAbsent(sourcePermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
        recordDamageRecipientBySource(sourcePermanentId, playerId);
    }

    /** Tracks how much damage each source dealt this turn, to every recipient (players, planeswalkers,
     *  battles and creatures; combat and noncombat alike). Maps permanent or spell source UUID to total damage.
     *  Used by "if [this] has dealt N or more damage this turn" (Chandra, Fire of Kaladesh).
     *  Cleared at turn cleanup. */
    public final Map<UUID, Integer> damageDealtThisTurnBySource = new ConcurrentHashMap<>();

    /** Tracks how much damage each source dealt to each player this turn. Cleared at turn cleanup. */
    public final Map<UUID, Map<UUID, Integer>> damageDealtToPlayersBySourceThisTurn =
            new ConcurrentHashMap<>();

    /** Tracks actual damage dealt by each sorcery spell cast this turn, keyed by card UUID. */
    public final Map<UUID, Integer> sorcerySpellDamageDealtThisTurn = new ConcurrentHashMap<>();

    /** Tracks source objects that have dealt damage at least once. */
    public final Set<UUID> permanentsThatHaveDealtDamage = ConcurrentHashMap.newKeySet();

    /** Tracks source objects that dealt damage to a creature this turn. */
    public final Set<UUID> sourcePermanentsThatDealtDamageToCreaturesThisTurn =
            ConcurrentHashMap.newKeySet();

    /** Tracks every player or permanent that each source permanent has dealt damage to this game. */
    public final Map<UUID, Set<UUID>> damageRecipientsBySource = new ConcurrentHashMap<>();

    /** Tracks distinct damage sources by the player who controlled them when they dealt damage this turn. */
    public final Map<UUID, Set<UUID>> damageSourcesControlledByPlayerThisTurn = new ConcurrentHashMap<>();

    /** Records damage by a permanent or spell source. No-op for unknown sources or non-positive damage. */
    public void recordDamageDealtBySource(UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) {
            return;
        }
        damageDealtThisTurnBySource.merge(sourcePermanentId, amount, Integer::sum);
        permanentsThatHaveDealtDamage.add(sourcePermanentId);
    }

    /** Records that a source object dealt actual damage to another creature this turn. */
    public void recordDamageDealtToCreatureBySource(UUID sourceId, UUID damagedPermanentId) {
        if (sourceId != null && damagedPermanentId != null && !sourceId.equals(damagedPermanentId)) {
            sourcePermanentsThatDealtDamageToCreaturesThisTurn.add(sourceId);
        }
    }

    /** Records actual damage dealt by a sorcery spell this turn. */
    public void recordSorcerySpellDamage(UUID spellCardId, int amount) {
        if (spellCardId == null || amount <= 0) {
            return;
        }
        sorcerySpellDamageDealtThisTurn.merge(spellCardId, amount, Integer::sum);
    }

    /** Records a distinct source as having dealt damage while controlled by {@code controllerId}. */
    public void recordDamageSourceControlledBy(UUID sourceId, UUID controllerId) {
        if (sourceId == null || controllerId == null) {
            return;
        }
        damageSourcesControlledByPlayerThisTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(sourceId);
    }

    /** Records a player or permanent that a source permanent actually dealt damage to. */
    public void recordDamageRecipientBySource(UUID sourcePermanentId, UUID recipientId) {
        if (sourcePermanentId == null || recipientId == null) {
            return;
        }
        damageRecipientsBySource
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(recipientId);
    }

    /** Records actual damage dealt by a source permanent to a player this turn. */
    public void recordDamageDealtBySourceToPlayer(UUID sourcePermanentId, UUID playerId, int amount) {
        if (sourcePermanentId == null || playerId == null || amount <= 0) {
            return;
        }
        recordDamageRecipientBySource(sourcePermanentId, playerId);
        damageDealtToPlayersBySourceThisTurn
                .computeIfAbsent(sourcePermanentId, ignored -> new ConcurrentHashMap<>())
                .merge(playerId, amount, Integer::sum);
        String sourceName = playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> sourcePermanentId.equals(permanent.getId()))
                .map(permanent -> permanent.getCard().getName())
                .findFirst()
                .orElse(null);
        recordPermanentDamageSourceNameToPlayer(sourceName, playerId);
    }

    /** Returns the damage a source permanent has dealt to a player this turn. */
    public int damageDealtBySourceToPlayerThisTurn(UUID sourcePermanentId, UUID playerId) {
        if (sourcePermanentId == null || playerId == null) {
            return 0;
        }
        return damageDealtToPlayersBySourceThisTurn
                .getOrDefault(sourcePermanentId, Map.of())
                .getOrDefault(playerId, 0);
    }

    /** Tracks which players have been dealt damage this turn (from any source — combat, spells, abilities). */
    public final Set<UUID> playersDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks how much damage each player has been dealt this turn (from any source — combat, spells,
     *  abilities; includes damage dealt as poison). Cleared at turn cleanup. Used by Final Punishment. */
    public final Map<UUID, Integer> damageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much damage each player has been dealt this turn by artifact sources. */
    public final Map<UUID, Integer> artifactDamageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much noncombat damage each player has been dealt this turn. */
    public final Map<UUID, Integer> noncombatDamageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much noncombat damage red sources controlled by each player dealt this turn. */
    public final Map<UUID, Integer> redSourceNoncombatDamageThisTurn = new ConcurrentHashMap<>();

    /** Records that {@code amount} damage was dealt to {@code playerId} this turn: marks the player as
     *  having been dealt damage and accumulates the amount (for effects that read the total). No-op for
     *  non-positive amounts. */
    public void recordDamageToPlayer(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }
        playersDealtDamageThisTurn.add(playerId);
        damageDealtToPlayersThisTurn.merge(playerId, amount, Integer::sum);
    }

    /** Records damage dealt to a player, including the portion dealt by artifact sources. */
    public void recordDamageToPlayer(UUID playerId, int amount, int artifactAmount) {
        if (amount <= 0) {
            return;
        }
        recordDamageToPlayer(playerId, amount);
        if (artifactAmount > 0) {
            artifactDamageDealtToPlayersThisTurn.merge(playerId, Math.min(amount, artifactAmount), Integer::sum);
        }
    }

    /** Records that {@code amount} combat damage was dealt to {@code playerId} this turn. */
    public void recordCombatDamageToPlayer(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }
        combatDamageDealtToPlayersThisTurn.merge(playerId, amount, Integer::sum);
    }

    /** Records that a creature with {@code creatureName} dealt combat damage to a player this game. */
    public void recordCombatDamageByCreatureNameToPlayer(String creatureName, UUID playerId) {
        if (creatureName == null || playerId == null) {
            return;
        }
        recordPermanentDamageSourceNameToPlayer(creatureName, playerId);
        combatDamageToPlayersByCreatureNameThisGame
                .computeIfAbsent(creatureName, ignored -> ConcurrentHashMap.newKeySet())
                .add(playerId);
    }

    /** Records that {@code amount} noncombat damage was dealt to {@code playerId} this turn. */
    public void recordNoncombatDamageToPlayer(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }
        noncombatDamageDealtToPlayersThisTurn.merge(playerId, amount, Integer::sum);
    }

    /** Records noncombat damage dealt by a red source controlled by {@code controllerId}. */
    public void recordRedSourceNoncombatDamage(UUID controllerId, Set<CardColor> sourceColors, int amount) {
        if (controllerId == null || sourceColors == null || !sourceColors.contains(CardColor.RED) || amount <= 0) {
            return;
        }
        redSourceNoncombatDamageThisTurn.merge(controllerId, amount, Integer::sum);
    }

    /** Tracks, per damaged player, the controller of the most recent red instant or sorcery spell that
     *  dealt damage to them this turn. Maps damaged player UUID → that spell's controller UUID.
     *  Used by Suffocation ("the controller of the last red instant or sorcery spell that dealt damage
     *  to you this turn"). Cleared at turn cleanup. */
    public final Map<UUID, UUID> lastRedSpellDamagerThisTurn = new ConcurrentHashMap<>();

    /** Records that a red instant or sorcery spell controlled by {@code spellControllerId} dealt damage
     *  to {@code damagedPlayerId} this turn, replacing any earlier such spell. */
    public void recordRedSpellDamageToPlayer(UUID damagedPlayerId, UUID spellControllerId) {
        if (damagedPlayerId == null || spellControllerId == null) {
            return;
        }
        lastRedSpellDamagerThisTurn.put(damagedPlayerId, spellControllerId);
    }

    /** Snapshot of how many untapped lands each player controlled at the beginning of their most recent
     *  turn (recorded before that turn's untap action). Locked so responses that tap lands
     *  don't change it. Read via {@code UntappedLandsAtTurnStart} for Power Surge. */
    public final Map<UUID, Integer> untappedLandsAtTurnStart = new ConcurrentHashMap<>();
    /** Snapshot of each active player's hand size when their current turn began. */
    public final Map<UUID, Integer> handSizeAtTurnStart = new ConcurrentHashMap<>();

    /** Tracks which permanents (by UUID) have been dealt damage this turn (from any source — combat, spells, abilities).
     *  Survives regeneration (which removes marked damage but does not undo "was dealt damage").
     *  Cleared at start of new turn. */
    public final Set<UUID> permanentsDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents have been dealt noncombat damage this turn. */
    public final Set<UUID> permanentsDealtNoncombatDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents have been dealt excess damage this turn, surviving regeneration. */
    public final Set<UUID> permanentsDealtExcessDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks the total damage actually dealt to each permanent this turn. Prevented damage is not
     *  included, and the total survives regeneration. Cleared at start of new turn. */
    public final Map<UUID, Integer> damageDealtToPermanentsThisTurn = new ConcurrentHashMap<>();

    /** Tracks actual damage dealt to each permanent this turn, grouped by damage source object. */
    public final Map<UUID, Map<UUID, Integer>> damageDealtToPermanentsBySourceThisTurn =
            new ConcurrentHashMap<>();

    /** Tracks actual damage dealt to each permanent this turn, grouped by the controller of each damage source. */
    public final Map<UUID, Map<UUID, Integer>> damageDealtToPermanentsBySourceControllerThisTurn =
            new ConcurrentHashMap<>();

    /** Snapshots the names of damage source objects seen this turn. */
    public final Map<UUID, String> damageSourceNamesThisTurn = new ConcurrentHashMap<>();

    /** Tracks controllers whose Giant, Wizard, or spell dealt damage to each permanent this turn. */
    public final Map<UUID, Set<UUID>> qualifyingDamageControllersByPermanentThisTurn = new ConcurrentHashMap<>();

    /** Tracks the controllers whose creatures or planeswalkers were dealt excess damage this turn. */
    public final Set<UUID> controllersOfPermanentsDealtExcessDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Records that {@code amount} damage was dealt to {@code permanentId} this turn. */
    public void recordDamageToPermanent(UUID permanentId, int amount) {
        if (permanentId == null || amount <= 0) {
            return;
        }
        permanentsDealtDamageThisTurn.add(permanentId);
        damageDealtToPermanentsThisTurn.merge(permanentId, amount, Integer::sum);
    }

    /** Records that {@code amount} noncombat damage was dealt to {@code permanentId} this turn. */
    public void recordNoncombatDamageToPermanent(UUID permanentId, int amount) {
        if (permanentId == null || amount <= 0) {
            return;
        }
        recordDamageToPermanent(permanentId, amount);
        permanentsDealtNoncombatDamageThisTurn.add(permanentId);
    }

    /** Records that a permanent was dealt excess damage this turn. */
    public void recordPermanentDealtExcessDamageThisTurn(UUID permanentId) {
        if (permanentId != null) {
            permanentsDealtExcessDamageThisTurn.add(permanentId);
        }
    }

    public void recordExcessDamageToPermanent(UUID permanentId) {
        recordPermanentDealtExcessDamageThisTurn(permanentId);
    }

    /** Records actual damage dealt to a permanent by a particular source object this turn. */
    public void recordDamageToPermanent(UUID permanentId, int amount, UUID sourceId, String sourceName) {
        recordDamageToPermanent(permanentId, amount);
        recordDamageToPermanentFromSource(permanentId, amount, sourceId, sourceName);
    }

    /** Records actual damage dealt to a permanent together with the controller of its source. */
    public void recordDamageToPermanent(UUID permanentId, int amount, UUID sourceId, String sourceName,
                                        UUID sourceControllerId) {
        recordDamageToPermanent(permanentId, amount);
        recordDamageToPermanentFromSource(permanentId, amount, sourceId, sourceName, sourceControllerId);
    }

    /** Records the source attribution for actual damage already recorded for a permanent. */
    public void recordDamageToPermanentFromSource(UUID permanentId, int amount, UUID sourceId,
                                                  String sourceName) {
        recordDamageToPermanentFromSource(permanentId, amount, sourceId, sourceName, null);
    }

    /** Records source attribution and source-controller attribution for actual damage. */
    public void recordDamageToPermanentFromSource(UUID permanentId, int amount, UUID sourceId,
                                                  String sourceName, UUID sourceControllerId) {
        if (permanentId == null || amount <= 0) {
            return;
        }
        if (sourceId != null) {
            damageDealtToPermanentsBySourceThisTurn
                    .computeIfAbsent(permanentId, ignored -> new ConcurrentHashMap<>())
                    .merge(sourceId, amount, Integer::sum);
            if (sourceName != null) {
                damageSourceNamesThisTurn.putIfAbsent(sourceId, sourceName);
            }
        }
        if (sourceControllerId != null) {
            damageDealtToPermanentsBySourceControllerThisTurn
                    .computeIfAbsent(permanentId, ignored -> new ConcurrentHashMap<>())
                    .merge(sourceControllerId, amount, Integer::sum);
        }
    }

    /** Records a controller whose qualifying Giant, Wizard, or spell dealt damage to a permanent. */
    public void recordQualifyingDamageControllerToPermanent(UUID permanentId, UUID controllerId) {
        if (permanentId == null || controllerId == null) {
            return;
        }
        qualifyingDamageControllersByPermanentThisTurn
                .computeIfAbsent(permanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(controllerId);
    }

    /** Records an excess-damage event against a creature or planeswalker this turn. */
    public void recordControllerOfPermanentDealtExcessDamageThisTurn(UUID controllerId) {
        if (controllerId == null) {
            return;
        }
        controllersOfPermanentsDealtExcessDamageThisTurn.add(controllerId);
    }

    /** Returns whether an opponent-controlled creature or planeswalker was dealt excess damage this turn. */
    public boolean wasOpponentPermanentDealtExcessDamageThisTurn(UUID controllerId) {
        return controllerId != null && controllersOfPermanentsDealtExcessDamageThisTurn.stream()
                .anyMatch(damagedControllerId -> !controllerId.equals(damagedControllerId));
    }

    /** Tracks which permanents (by UUID) have already provided their once-each-turn "you may pay {0}"
     *  alternative cast cost this turn (As Foretold). Cleared at start of new turn. */
    public final Set<UUID> freeCastPermanentUsedThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which source permanents' once-per-turn permissions to cast cards from exile have been
     *  used this turn. Cleared at start of new turn. */
    public final Set<UUID> oncePerTurnExileCastPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which source permanents' one-shot permissions to cast cards from exile have been used. */
    public final Set<UUID> oneShotExileCastPermissionsUsed = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents' once-each-turn library-cast permissions have been used this turn. */
    public final Set<UUID> oncePerTurnLibraryCastPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents' shared once-each-turn library-top play permissions have been used. */
    public final Set<UUID> oncePerTurnLibraryPlayPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();


    /** Tracks every permanent that has had counters put on it this turn, regardless of which
     *  abilities it had at the time. Used for "for the first time each turn" counter triggers that
     *  may be granted after an earlier placement. Cleared at start of new turn. */
    public final Set<UUID> permanentsWithCountersPutThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which controller has used each permanent's first-card-cycled-free permission this turn. */
    public final Map<UUID, Set<UUID>> firstCardCycledFreeUsesThisTurn = new ConcurrentHashMap<>();

    /**
     * The once-each-turn limits used this turn, by source permanent or graveyard card and then by the limited
     * ability's key ({@code TriggeredAbility.limitKey()}): "triggers only once each turn" as the ability triggers, "do
     * this only once each turn" (CR 603.2h) as its action is taken, and the first-time marks derived from an ability's
     * key. Cleared at the start of a new turn; a graveyard card's entries go when it leaves the graveyard.
     */
    public final Map<UUID, Set<String>> keyedOncePerTurnTriggersFiredThisTurn = new ConcurrentHashMap<>();
    /**
     * The "triggers only once" abilities that have triggered, by source object id: the trigger pipeline keys each
     * ability of an object separately, for abilities with {@code TriggerLimit.ONCE}.
     */
    public final Map<UUID, Set<String>> keyedOnceOnlyTriggersFired = new ConcurrentHashMap<>();

    /** Tracks source permanent IDs to creature IDs whose first counter-placement trigger has fired
     *  this turn. Cleared at start of new turn. */
    public final Map<UUID, Set<UUID>> oncePerCreatureTriggersFiredThisTurn = new ConcurrentHashMap<>();

    /** Creatures that have had counters put on them this turn (Stalwart Successor's "first time"). */
    public final Set<UUID> creaturesThatReceivedCountersThisTurn = ConcurrentHashMap.newKeySet();

    /** Counts how many times each creature permanent became tapped during the current turn. */
    public final Map<UUID, Integer> creatureTapCountsThisTurn = new ConcurrentHashMap<>();

    /** Tracks permanents that have added mana with a source ability this turn. */
    public final Set<UUID> permanentsThatAddedManaWithAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Tracks keyed triggered-ability resolutions that have already happened for each permanent
     *  this turn. Cleared at start of new turn. */
    public final Map<UUID, Set<String>> firstResolutionTriggerKeysThisTurn = new ConcurrentHashMap<>();

    /** Permanent IDs that have replaced a token creation event this turn. */
    public final Set<UUID> tokenCreationReplacementUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Token creation event paused for a once-per-turn replacement choice. */
    public PendingTokenCreationReplacement pendingTokenCreationReplacement;


    /** Permanents (by UUID) exerted this turn, as an attack choice or an activation cost — "if this
     *  creature hasn't been exerted this turn" (Combat Celebrant). Cleared at start of new turn. */
    public final Set<UUID> permanentsExertedThisTurn = ConcurrentHashMap.newKeySet();

    public final Map<UUID, Set<BendingType>> bendingTypesCompletedThisTurn = new ConcurrentHashMap<>();

    /**
     * Creatures dying together in the current simultaneous-death event (CR 700.1 / destroy batch /
     * SBA lethal pass). Maps permanent id → permanent (last-known) and controller id. Used so
     * "whenever another creature dies" watchers that are themselves dying still see the other
     * deaths (e.g. Morbid Opportunist). Cleared when the batch ends; not turn-scoped.
     */
    public final Map<UUID, Permanent> simultaneousDyingCreatures = new ConcurrentHashMap<>();
    /**
     * Cards that just arrived in a graveyard in the zone-change event whose leaves-the-battlefield
     * triggers are being collected. Graveyard-count conditions (threshold) ignore them so the
     * trigger source is evaluated as it was immediately before the event (CR 603.10a). Only
     * populated for the duration of that collection.
     */
    public final Set<UUID> graveyardLookBackExcludedCardIds = ConcurrentHashMap.newKeySet();
    /**
     * Battlefield permanents whose printed abilities were already removed by continuous effects just
     * before the most recent creature left the battlefield. Creature-death watchers in this set do not
     * trigger for that death, since leaves-the-battlefield abilities look back in time (CR 603.10a).
     * Recomputed on every departure from the battlefield.
     */
    public final Set<UUID> lostPrintedAbilityWatcherIdsBeforeLeave = ConcurrentHashMap.newKeySet();
    public final Map<UUID, UUID> simultaneousDyingControllers = new ConcurrentHashMap<>();
    public final Map<UUID, Permanent> simultaneousDyingPermanents = new ConcurrentHashMap<>();
    /** Last departure snapshots by card identity, retained for sacrifice-event collection this turn. */
    public final Map<UUID, Permanent> departedPermanentSnapshots = new ConcurrentHashMap<>();
    /** Controller each departed permanent had immediately before it left the battlefield, keyed by card identity. */
    public final Map<UUID, UUID> departedPermanentControllerIds = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> simultaneousDyingPermanentControllers = new ConcurrentHashMap<>();
    /** Last-known effective powers of creatures in the current simultaneous death event. */
    public final Map<UUID, Integer> simultaneousDyingPowers = new ConcurrentHashMap<>();
    /** Last-known persist / undying instance counts of creatures in the current simultaneous death event. */
    public final Map<UUID, Map<Keyword, Integer>> simultaneousDyingTriggerKeywordInstances =
            new ConcurrentHashMap<>();
    /**
     * Continuously granted abilities that trigger on a zone change, the permanent's own ("when this dies") or
     * another object's ("whenever a creature dies"), as they were before any permanent left a simultaneous death
     * event.
     */
    public final Map<UUID, List<TriggeredAbility>> simultaneousDyingGrantedZoneChangeAbilities =
            new ConcurrentHashMap<>();

    /** Tracks subtypes of creatures that dealt combat damage to players this turn.
     *  Maps source permanent UUID → set of subtypes the creature had at the time of dealing damage.
     *  Used by end-step triggers that check which subtypes dealt combat damage (e.g. Admiral Beckett Brass). */
    public final Map<UUID, Set<CardSubtype>> combatDamageSourceSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Names of creature sources that dealt combat damage to players this turn. */
    public final Map<UUID, String> combatDamageSourceNamesThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creatures that dealt combat damage to players this turn had the Changeling keyword.
     *  These creatures count as having all creature subtypes for subtype-conditional triggers. */
    public final Set<UUID> combatDamageSourcesWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which sources that dealt combat damage to players this turn were legendary. */
    public final Set<UUID> combatDamageSourcesWithLegendaryThisTurn = ConcurrentHashMap.newKeySet();

    /** Players dealt combat damage by each creature while that source was legendary. */
    public final Map<UUID, Set<UUID>> legendaryCreatureCombatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks, per player who controlled the source at damage time, the union of subtypes of creatures
     *  they controlled that dealt combat damage to a player this turn. Used to evaluate the prowl
     *  alternative cost ("if you dealt combat damage to a player this turn with a [subtype]"). */
    public final Map<UUID, Set<CardSubtype>> combatDamageToPlayerControllerSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Per player who controlled the source at damage time, the creatures that dealt combat damage to a
     *  player this turn, kept after the creatures leave the battlefield (Excess). */
    public final Map<UUID, Set<UUID>> combatDamageDealersByControllerThisTurn = new ConcurrentHashMap<>();

    /** Tracks which players dealt combat damage to a player this turn with a Changeling creature they
     *  controlled (which counts as every creature subtype for prowl). */
    public final Set<UUID> controllersDealtCombatDamageWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks permanent IDs of commanders that dealt combat damage to a player this turn. */
    public final Set<UUID> combatDamageSourcesThatWereCommandersThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks, per creature that participated in a block this turn, the union of subtypes the creatures it
     *  blocked or was blocked by had at the moment of the block (recorded at declare-blockers time). Used by
     *  "target creature that blocked or was blocked by a [subtype] this turn" spells (Time to Reflect). */
    public final Map<UUID, Set<CardSubtype>> combatBlockOpponentSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Tracks, per creature that participated in a block this turn, the union of colors the creatures it
     *  blocked or was blocked by had at the moment of the block (recorded at declare-blockers time). Used by
     *  "activate only if this creature blocked or was blocked by a [color] creature this turn" abilities
     *  (Sea Troll). */
    public final Map<UUID, Set<CardColor>> combatBlockOpponentColorsThisTurn = new ConcurrentHashMap<>();

    /** Tracks creatures that blocked or were blocked by a Changeling creature this turn (which counts as
     *  every creature subtype). Complements {@link #combatBlockOpponentSubtypesThisTurn}. */
    public final Set<UUID> creaturesInCombatWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks, per creature that participated in a block this turn, the IDs of the creatures it blocked or
     *  was blocked by (recorded at declare-blockers time, both directions). Turn-scoped and independent of
     *  the current combat, so it still answers "all creatures that blocked or were blocked by it this turn"
     *  across multiple combat phases. Used by Venomous Breath. */
    public final Map<UUID, Set<UUID>> combatBlockOpponentIdsThisTurn = new ConcurrentHashMap<>();

    /** Tracks, per creature and blocking opponent, who controlled that opponent when the block happened. */
    public final Map<UUID, Map<UUID, UUID>> combatBlockOpponentControllerIdsThisTurn = new ConcurrentHashMap<>();

    /** Tracks, per creature, the creatures it blocked or was blocked by in the current combat.
     *  Unlike the turn-scoped map above, this is cleared when the next combat begins so last-known
     *  combat relationships remain precise while end-of-combat triggers resolve. */
    public final Map<UUID, Set<UUID>> combatBlockOpponentIdsThisCombat = new ConcurrentHashMap<>();

    /** Tracks, per blocker, the attacking creatures it blocked this turn. Used by Triton Tactics,
     * which affects creatures blocked by its chosen creatures rather than creatures that blocked them. */
    public final Map<UUID, Set<UUID>> combatOpponentIdsBlockedByThisTurn = new ConcurrentHashMap<>();

    /** Tracks the attacking creatures that became blocked this turn (recorded at declare-blockers time,
     *  attacker direction only — blocking does not count). Turn-scoped and independent of the current
     *  combat, and keyed by permanent ID so it still answers "if it was blocked this turn" after the
     *  creature has left the battlefield. Used by Fyndhorn Druid's dies trigger. */
    public final Set<UUID> creaturesBlockedThisTurn = ConcurrentHashMap.newKeySet();

    /** Creatures put onto the battlefield blocking this turn; they were never declared as blockers. */
    public final Set<UUID> creaturesEnteredBlockingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which Leonin Arbiter permanent IDs each player has paid {2} for this turn. */
    public final Map<UUID, Set<UUID>> paidSearchTaxPermanentIds = new ConcurrentHashMap<>();

    /** Word of Command's short-lived player-control window and its parked resolution. */
    public UUID wordOfCommandControllerPlayerId;
    public UUID wordOfCommandControlledPlayerId;
    public UUID wordOfCommandCardId;
    public StackEntry wordOfCommandPendingResolutionEntry;
    public int wordOfCommandPendingResolutionIndex;
    public boolean wordOfCommandCastingCard;
    public boolean wordOfCommandAwaitingCardResolution;

    // Taunt — "creatures that player controls attack you if able" during their next turn


    // Oracle en-Vec — "the chosen creatures attack if able, and other creatures can't attack"

    /**
     * Records that {@code playerId} chose {@code chosen} to attack during their next turn (Oracle en-Vec) as a floating
     * effect that applies during that turn, and makes the delayed triggered ability "at the beginning of that turn's end step, destroy each of the chosen creatures
     * that didn't attack this turn" (CR 603.7a) for the ability of {@code sourceCard} controlled by
     * {@code controllerId}: it waits for the end step of {@code playerId}'s next turn.
     */
    public void restrictAttackersNextTurn(UUID playerId, Set<UUID> chosen, UUID controllerId, Card sourceCard) {
        addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceCard.getName(), null, controllerId,
                new OnlyChosenCreaturesCanAttackEffect(chosen), playerId, Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
        if (!chosen.isEmpty()) {
            List<UUID> chosenIds = List.copyOf(chosen);
            temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.CHOSEN_PLAYER),
                            new DestroyEachTargetPermanentEffect(new PermanentNotPredicate(
                                    new PermanentAttackedThisTurnPredicate()))),
                    null, chosenIds.getFirst(), playerId,
                    DelayedReferents.permanents(chosenIds).afterTurn(turnNumber)));
        }
    }


    /**
     * Whether {@code permanentId} assigns no combat damage: an effect that says so lasts this turn or this combat. It
     * isn't prevention, so "damage can't be prevented" doesn't override it.
     */
    public boolean assignsNoCombatDamage(UUID permanentId) {
        return hasFloatingEffectOnPermanent(permanentId, AssignNoCombatDamageEffect.class);
    }

    /**
     * The floating effects that replace the declaration of attackers or blockers in the way {@code kind} names, oldest
     * first; the newest one is the one that decides (it was made last).
     */
    public List<FloatingContinuousEffect> declarationReplacements(DeclarationReplacementEffect.Kind kind) {
        return floatingEffectsOfTypes(DeclarationReplacementEffect.class).stream()
                .filter(effect -> ((DeclarationReplacementEffect) effect.effect()).kind() == kind)
                .toList();
    }

    /** Whether {@code playerId} skips the step or phase {@code kind} this turn (an effect that lasts the turn). */
    public boolean skipsStepOrPhase(UUID playerId, SkipStepOrPhaseKind kind) {
        return floatingPlayerEffects(playerId, SkipChosenStepOrPhaseEffect.class).stream()
                .anyMatch(effect -> ((SkipChosenStepOrPhaseEffect) effect).kind() == kind);
    }

    /** Active this turn: affectedPlayerId -> creatures allowed to attack by a pile effect. */
    public final Map<UUID, Set<UUID>> attackableCreaturesThisTurn = new ConcurrentHashMap<>();

    /** Creatures that were able to attack as each player reached their declare-attackers step this turn. */
    public final Map<UUID, Set<UUID>> creaturesAbleToAttackAtDeclareAttackersThisTurn = new ConcurrentHashMap<>();

    /** Active this turn: affectedPlayerId -> creatures allowed to block by a pile effect. */
    public final Map<UUID, Set<UUID>> blockableCreaturesThisTurn = new ConcurrentHashMap<>();

    /** Intimidation Bolt — "Other creatures can't attack this turn." Each resolution appends the
     *  targeted (exempted) creature's permanent ID; a creature may attack only if its ID equals every
     *  entry, so multiple copies stack and an empty list means no restriction. A dead target's ID
     *  matches no living creature, locking everyone (CR-accurate: if the target dies, no creature may
     *  attack). Enforced in {@code CombatAttackService.canCreatureAttack}, so it also covers creatures
     *  that enter later this turn. Cleared at each turn transition. */

    /**
     * Peace Talks — "This turn and next turn, creatures can't attack, and players and permanents
     * can't be the targets of spells or activated abilities." Set to {@code 2} on resolution
     * (current + next); decremented at each turn advance. Active while {@code > 0}. Triggered
     * abilities are unaffected. Enforced in {@code AttackLegalityService} and targeting services.
     */
    public int peaceTalksTurnsRemaining = 0;

    /** Stores context for a pending Leonin Arbiter search tax MayAbility choice. */
    public PendingSearchContext pendingSearchContext;

    /**
     * Controller of the spell or ability currently resolving off the stack, or {@code null} when no
     * spell/ability is resolving (e.g. during cost payment, combat, or state-based actions). Used to
     * determine causation for effects like Sacred Ground that care whether a permanent left the
     * battlefield because of "a spell or ability an opponent controls".
     */
    public UUID currentlyResolvingControllerId;

    /**
     * True only while a spell's sacrifice additional cost is being paid. Paying a cost is the casting
     * player's own action, not something a resolving spell or ability causes (Sacred Ground).
     */
    public boolean sacrificingAsSpellCost;

    /**
     * The controller of the spell or ability whose resolution causes a zone change now, as rules events record it:
     * the resolving one, or the one whose resolution waits for a player's input. Paying a spell's sacrifice cost is
     * the casting player's own action, not something a resolving spell or ability causes.
     */
    private UUID resolvingControllerForRulesEvents() {
        if (sacrificingAsSpellCost) {
            return null;
        }
        if (currentlyResolvingControllerId != null) {
            return currentlyResolvingControllerId;
        }
        return pendingEffectResolutionEntry == null ? null : pendingEffectResolutionEntry.getControllerId();
    }

    /** Controller of the triggered ability currently resolving, or {@code null} otherwise. */
    public UUID currentlyResolvingTriggeredAbilityControllerId;

    /**
     * Scratch of the resolution-time divisions a handler asks for one target at a time (the counters of
     * {@code DistributeCountersAmongControlledCreaturesEffect}, {@code MoveCountersFromSourceToOtherCreaturesEffect},
     * a restricted {@code DealDividedDamageEffect} resolving with targets but no division). A triggered ability's
     * division is never staged here: it is announced as the ability is put on the stack (CR 603.3d) and kept on its
     * entry.
     */
    public Map<UUID, Integer> pendingETBDamageAssignments = Map.of();

    // Combat damage assignment state
    public final Map<Integer, Map<UUID, Integer>> combatDamagePlayerAssignments = new HashMap<>();
    /** Damage source, prevention source, recipient, and the allocated prevention amount. */
    public final Map<UUID, Map<UUID, Map<UUID, Integer>>> combatSharedDamagePreventionAllocations = new HashMap<>();
    /** The assignment mode each creature's controller chose for the combat damage step being dealt, by creature id. */
    public final Map<UUID, com.github.laxika.magicalvibes.model.effect.CombatDamageAssignmentMode>
            combatDamageAssignmentModeChoices = new HashMap<>();
    public final List<Integer> combatDamagePendingIndices = new ArrayList<>();
    /** CR 510.1d — the defending player's damage division for creatures blocking 2+ attackers
     *  (defending-battlefield index → attacker permanent id → damage). */
    public final Map<Integer, Map<UUID, Integer>> combatDamageBlockerAssignments = new HashMap<>();
    public final List<Integer> combatDamagePendingBlockerIndices = new ArrayList<>();
    /** True while damage assignments for the first-strike combat damage step are being collected
     *  (the regular step uses {@link #combatDamagePhase1Complete} instead). */
    public boolean combatDamageFirstStrikeAssignmentPhase = false;
    /** True while damage assignments for the firstest-strike combat damage step are collected. */
    public boolean combatDamageFirstestStrikeAssignmentPhase = false;
    public boolean combatDamageFirstestStrikeStepComplete = false;
    /** Creatures that had firstest strike as this combat's firstest-strike damage step began; they do not
     *  deal damage again in the regular step, while a creature that gains it later does. */
    public final Set<UUID> creaturesInFirstestStrikeDamageStepThisCombat = ConcurrentHashMap.newKeySet();
    public boolean combatDamageFirstStrikeStepComplete = false;
    public boolean combatDamagePhase1Complete = false;
    public CombatDamagePhase1State combatDamagePhase1State;
    /** Combat damage assignment waiting on the stack, when a static effect enables that rule. */
    public CombatDamageState pendingCombatDamageState;
    /**
     * The combat damage step whose damage events are being run right now; only set while they run,
     * so it is never copied for simulation (a waiting step is in {@link #pendingCombatDamageState}).
     */
    public CombatDamageState combatDamageStateInProgress;
    public boolean pendingCombatDamageFirstStrikePhase;
    public boolean pendingCombatDamageFirstestStrikePhase;

    // CR 704.5b — track players who attempted to draw from an empty library
    public final Set<UUID> playersAttemptedDrawFromEmptyLibrary = ConcurrentHashMap.newKeySet();

    /** Tracks individual state-triggered abilities (rule 603.8) currently on the stack.
     *  Each key is a (permanentId, effectIndex) pair so multiple state triggers on the
     *  same permanent are tracked independently. Cleaned up when the ability resolves,
     *  is countered, or otherwise leaves the stack. */
    public final Set<StateTriggerKey> stateTriggerOnStack = ConcurrentHashMap.newKeySet();

    /** When true, this GameData is an MCTS simulation copy — suppress all external side effects
     *  (broadcasting, session messages, registry mutations, logging). */
    public boolean simulation;

    /**
     * When true, auto-pass offers every seat a priority window whenever it can act, exactly as it
     * does for {@link #aiPlayerIds}. Deterministic tests set this so passing priority leaves the
     * opponent able to respond at instant speed, <em>without</em> marking the seats AI-controlled:
     * AI seats are never registered as transport consumers, so marking them would silently suppress
     * every human projection (see {@code GameEventProjectionSubscriber}).
     */
    public boolean alwaysOfferPriorityWindows;

    /**
     * Monotonic metadata for transport-independent domain events. Both counters are advanced only
     * by the engine's outermost mutation coordinator while holding this GameData's monitor.
     */
    private long domainActionSequence;
    private long domainEventSequence;
    private long domainStateVersion;

    /**
     * Advances and returns the next completed game-local causal action identity.
     */
    public long nextDomainActionSequence() {
        requireGameMonitor("nextDomainActionSequence");
        return ++domainActionSequence;
    }

    /**
     * Advances and returns the version of authoritative mutable state committed by an outermost
     * domain-event mutation.
     */
    public long advanceDomainStateVersion() {
        requireGameMonitor("advanceDomainStateVersion");
        return ++domainStateVersion;
    }

    /**
     * Advances and returns the next game-local event sequence.
     */
    public long nextDomainEventSequence() {
        requireGameMonitor("nextDomainEventSequence");
        return ++domainEventSequence;
    }

    public synchronized long domainEventSequence() {
        return domainEventSequence;
    }

    public synchronized long domainActionSequence() {
        return domainActionSequence;
    }

    public synchronized long domainStateVersion() {
        return domainStateVersion;
    }

    private void requireGameMonitor(String operation) {
        if (!Thread.holdsLock(this)) {
            throw new IllegalStateException(operation + " requires holding the GameData monitor");
        }
    }

    /** Monotonic CR 613.7 timestamp source. Advanced via {@link #nextTimestamp()} whenever a
     *  permanent enters a battlefield, an Aura/Equipment becomes attached (CR 613.7e), or a
     *  resolving spell/ability creates a continuous effect. Never reset during a game. */
    public long timestampCounter;

    /** Timestamp boundary separating floating effects created before the current upkeep from
     *  those created during it. */
    public long upkeepStartTimestamp;

    /** Timestamp boundary separating floating effects created before the current turn from those
     *  created during it. */
    public long turnStartTimestamp;

    /** Snapshot used by Do-Over to reconstruct the game at the start of the current turn. */
    private GameData turnStartSnapshot;

    /** Returns the next CR 613.7 timestamp (strictly increasing, starting at 1). */
    public long nextTimestamp() {
        return ++timestampCounter;
    }

    /**
     * Returns the player who currently holds priority: starting from {@link #priorityHolderId}
     * (the active player when unset), the first player in turn order who is not in
     * {@link #priorityPassedBy}. Returns {@code null} once every player has passed in succession
     * (CR 117.4) or before the game has an active player.
     */
    public UUID currentPriorityPlayerId() {
        UUID activeId = activePlayerId;
        if (activeId == null) {
            return null;
        }
        List<UUID> order;
        synchronized (orderedPlayerIds) {
            order = new ArrayList<>(orderedPlayerIds);
        }
        UUID holderId = priorityHolderId;
        int start = order.indexOf(holderId != null ? holderId : activeId);
        if (start < 0) {
            start = Math.max(0, order.indexOf(activeId));
        }
        for (int i = 0; i < order.size(); i++) {
            UUID candidate = order.get((start + i) % order.size());
            if (!priorityPassedBy.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Hands priority back to the active player with no passes recorded, as at the beginning of a
     * step and after a spell or ability resolves (CR 117.3a, 117.3b).
     */
    public void resetPriorityToActivePlayer() {
        priorityHolderId = null;
        priorityPassedBy.clear();
    }

    /**
     * Removes a resolving instant or sorcery from the stack. It stays on the stack, targetable and
     * counterable, until the final part of its resolution (CR 608.2n), or until it leaves the stack
     * earlier while it continues to resolve (CR 608.2m).
     */
    public void removeResolvingSpellFromStack(StackEntry entry) {
        stack.removeIf(stackEntry -> stackEntry == entry);
    }

    private StackEntry copyOnStack(GameData copy, StackEntry original, StackEntry fallback) {
        if (original == null) {
            return fallback;
        }
        synchronized (stack) {
            for (int i = 0; i < stack.size(); i++) {
                if (stack.get(i) == original) {
                    return copy.stack.get(i);
                }
            }
        }
        return fallback;
    }

    /** Captures the current turn state without exposing the snapshot to views or callers. */
    public void captureTurnStartSnapshot() {
        GameData snapshot = simulationFrameCopy();
        snapshot.turnStartSnapshot = null;
        turnStartSnapshot = snapshot;
    }

    /** Returns whether the given player was the monarch when the current turn began. */
    public boolean wasMonarchAtTurnStart(UUID playerId) {
        return playerId != null
                && turnStartSnapshot != null
                && playerId.equals(turnStartSnapshot.monarchPlayerId);
    }

    /**
     * Restores the mutable game state captured at the start of the current turn.
     *
     * <p>The snapshot is a regular simulation copy, so cards remain shared while mutable
     * permanents and collections are independent. Final collection fields are restored in place;
     * this preserves the collection objects that the engine uses as synchronization points.</p>
     *
     * @return {@code true} when a turn snapshot was available
     */
    public boolean restoreTurnStartSnapshot() {
        GameData snapshot = turnStartSnapshot;
        if (snapshot == null) {
            return false;
        }
        // Snapshots are shared read-only by simulations; restore from a fresh mutable copy.
        restoreMutableStateFrom(snapshot.simulationFrameCopy());
        turnStartSnapshot = null;
        return true;
    }

    private void restoreMutableStateFrom(GameData snapshot) {
        // Refilling the zone lists moves no card; the rules event log is restored from the snapshot afterwards.
        rulesEvents.suppress();
        try {
            for (java.lang.reflect.Field field : GameData.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        || preservedDuringTurnRestart(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                Object currentValue = field.get(this);
                Object snapshotValue = field.get(snapshot);
                if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    restoreFinalValue(currentValue, snapshotValue);
                } else {
                    field.set(this, snapshotValue);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not restore the turn-start snapshot", e);
        } finally {
            rulesEvents.resume();
        }
        rulesEvents.copyFrom(snapshot.rulesEvents);
    }

    private static boolean preservedDuringTurnRestart(String fieldName) {
        return switch (fieldName) {
            case "session", "subgameRequested", "waitingForSubgame", "pendingAncestorTransfers",
                    "subgameCards", "suspendedZoneTriggers", "turnStartSnapshot", "gameLog", "playersWhoLostGameThisMatch", "matchCreatureTypeErrata", "timestampCounter",
                    "nextExtraTurnSequence", "graveyardEntryVersion",
                    "domainActionSequence", "domainEventSequence", "domainStateVersion",
                    "currentlyResolvingControllerId", "pendingEffectResolutionEntry", "resolvingEntry",
                    "pendingEffectResolutionIndex", "resolutionOrdinal", "parkedOuterResolutions",
                    "resolvingMayEffectFromStack",
                    "resolvedMayAccepted", "resolvedMayTargetingEntry",
                    "rerunCurrentEffectAfterInteraction", "deathReplacementsCanRerunEffect", "stepDepartureEffect",
                    "coinFlipChoicesCanRerunEffect", "recordedCoinFlips", "replayedCoinFlips", "pokeyReplacementAnswer",
                    "coinFlipCheckpointEffect", "coinFlipResumePoint", "pendingCoinFlipChoiceReplay",
                    "chosenDeathReplacementsForRerun", "rerunMayLibraryChoices", "pendingDeathReplacementChoiceReplay",
                    "deferPlayerLossCheck",
                    "effectResolutionDepth", "restartTurnRequested", "simulation",
                    "triggerRoutingDepth", "triggerRecordHandedOff", "interactionWithoutTriggerRecord",
                    "triggerRecordLeftBehind",
                    "alwaysOfferPriorityWindows", "rulesEvents", "rulesObserver" -> true;
            default -> false;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void restoreFinalValue(Object currentValue, Object snapshotValue) {
        if (currentValue instanceof Map currentMap && snapshotValue instanceof Map snapshotMap) {
            currentMap.clear();
            currentMap.putAll(snapshotMap);
        } else if (currentValue instanceof Collection currentCollection
                && snapshotValue instanceof Collection snapshotCollection) {
            currentCollection.clear();
            currentCollection.addAll(snapshotCollection);
        } else if (currentValue != null && snapshotValue != null
                && !isImmutableValue(currentValue)) {
            restoreObjectState(currentValue, snapshotValue);
        }
    }

    private static boolean isImmutableValue(Object value) {
        return value instanceof UUID || value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof Character || value.getClass().isEnum()
                || value.getClass().getPackageName().startsWith("java.")
                // A lambda or method reference holds no game state of its own; one bound to this game (the
                // interaction state's trigger record callbacks) stays bound to it.
                || value.getClass().isHidden();
    }

    private static void restoreObjectState(Object currentValue, Object snapshotValue) {
        try {
            for (Class<?> type = currentValue.getClass(); type != null && type != Object.class;
                 type = type.getSuperclass()) {
                for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object currentFieldValue = field.get(currentValue);
                    Object snapshotFieldValue = field.get(snapshotValue);
                    if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                        restoreFinalValue(currentFieldValue, snapshotFieldValue);
                    } else {
                        field.set(currentValue, snapshotFieldValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not restore nested turn-start state", e);
        }
    }

    /** Records a new continuous stay of a card in a graveyard and returns its identity. */
    public synchronized long markGraveyardEntry(Card card) {
        long version = ++graveyardEntryVersion;
        graveyardEntryVersions.put(card.getId(), version);
        java.util.stream.Stream.of(stack, pendingManaAbilityTriggers, suspendedZoneTriggers)
                .flatMap(java.util.Collection::stream)
                .filter(entry -> card.getId().equals(entry.getTriggeringCardId())
                        && entry.getTriggeringCardGraveyardEntryVersion() == -1L)
                .forEach(entry -> entry.setTriggeringCardGraveyardEntryVersion(version));
        java.util.stream.Stream.of(stack, pendingManaAbilityTriggers, suspendedZoneTriggers)
                .flatMap(java.util.Collection::stream)
                .filter(entry -> entry.isAwaitingSourceGraveyardEntry() && entry.getCard() != null
                        && card.getId().equals(entry.getCard().getId()))
                .forEach(entry -> {
                    entry.setSourceGraveyardEntryVersion(version);
                    entry.setAwaitingSourceGraveyardEntry(false);
                });
        return version;
    }

    /**
     * Stamps the card's current graveyard stay on the triggered abilities waiting to resolve that refer to it and
     * have none yet, as {@link #markGraveyardEntry} does for the abilities queued before the card arrived.
     */
    public synchronized void stampGraveyardEntryOnPendingTriggers(UUID cardId) {
        long version = graveyardEntryVersion(cardId);
        java.util.stream.Stream.of(stack, pendingManaAbilityTriggers, suspendedZoneTriggers)
                .flatMap(java.util.Collection::stream)
                .filter(entry -> cardId.equals(entry.getTriggeringCardId())
                        && entry.getTriggeringCardGraveyardEntryVersion() == -1L)
                .forEach(entry -> entry.setTriggeringCardGraveyardEntryVersion(version));
    }

    /**
     * Ends a card's continuous stay in a graveyard. The card is a new object once it has left, so
     * anything remembering the old stay's identity no longer refers to it.
     */
    public synchronized void markGraveyardExit(UUID cardId) {
        graveyardEntryVersions.put(cardId, ++graveyardEntryVersion);
    }

    /**
     * The exile entry versions of the cards {@code cardIds} as they are now, in their order (0 for one never exiled):
     * what a delayed ability about "those cards" remembers, so a card that left exile and was exiled again is a new
     * object it no longer refers to (CR 400.7).
     */
    public java.util.Map<UUID, Long> exileEntryVersionsOf(java.util.Collection<UUID> cardIds) {
        java.util.Map<UUID, Long> versions = new java.util.LinkedHashMap<>();
        cardIds.forEach(cardId -> versions.put(cardId, exileEntryVersions.getOrDefault(cardId, 0L)));
        return versions;
    }

    /** Returns the identity of the card's latest graveyard entry, or zero if it is untracked. */
    public long graveyardEntryVersion(UUID cardId) {
        return graveyardEntryVersions.getOrDefault(cardId, 0L);
    }

    /**
     * Creates a battlefield list that stamps any still-unstamped permanent (timestamp 0) with
     * this game's next CR 613.7 timestamp as it is inserted. The engine's entry funnel
     * ({@code BattlefieldEntryService.putPermanentOntoBattlefield}) stamps before its own add, so
     * this is a no-op for the real entry; it makes direct insertions (test setups building
     * battlefields by hand) carry real insertion-order timestamps instead of relying on the
     * position fallback. Control-change moves re-insert already-stamped permanents and keep their
     * stamp (CR 613.7c).
     *
     * <p>One exception to "no-op for the real entry": the CR 614.12 subtype lookahead splices the
     * entering permanent in <em>before</em> the funnel assigns its timestamp, so that insert
     * stamps it early. Harmless — the funnel overwrites the stamp unconditionally right after,
     * and the only cost is one burned counter value — but it means an entering permanent may
     * carry a provisional timestamp while the lookahead runs.
     */
    public List<Permanent> newBattlefieldList() {
        return Collections.synchronizedList(new TimestampingBattlefieldList());
    }

    private final class TimestampingBattlefieldList extends ArrayList<Permanent> {
        @Override
        public boolean add(Permanent permanent) {
            stamp(permanent);
            return super.add(permanent);
        }

        @Override
        public void add(int index, Permanent permanent) {
            stamp(permanent);
            super.add(index, permanent);
        }

        @Override
        public boolean addAll(Collection<? extends Permanent> permanents) {
            permanents.forEach(this::stamp);
            return super.addAll(permanents);
        }

        @Override
        public boolean addAll(int index, Collection<? extends Permanent> permanents) {
            permanents.forEach(this::stamp);
            return super.addAll(index, permanents);
        }

        private void stamp(Permanent permanent) {
            if (permanent.getTimestamp() == 0) {
                permanent.setTimestamp(nextTimestamp());
            }
        }
    }

    /** Continuous effects created by resolved spells/abilities (CR 611.2), for the CR 613 layer
     *  engine (see {@code agent-docs/LAYER_SYSTEM.md}). Stamped via {@link #addFloatingEffect}.
     *  A duration that ends at an event ends at the rules event (the engine's duration clock); a
     *  "for as long as" duration ends where its condition stops holding: the source leaving the
     *  battlefield, becoming unattached or untapped, or changing controllers (CR 611.2b). */
    public final List<FloatingContinuousEffect> floatingEffects = Collections.synchronizedList(new ArrayList<>());

    /** Floating effects created with a duration that begins later ("during your next turn", "after this turn"):
     *  stamped as they were created, they do nothing until the duration clock moves them into
     *  {@link #floatingEffects} as their start comes. */
    public final List<FloatingContinuousEffect> scheduledFloatingEffects =
            Collections.synchronizedList(new ArrayList<>());

    /** Active player at the moment each floating effect was created, keyed by the effect's id. Under
     *  Runed Terror's sequential phases an "until end of turn" effect ends only at the cleanup step of
     *  the player who was active when it began (Runed Terror ruling, CR 514.2). */
    public final Map<UUID, UUID> floatingEffectCreatingActivePlayerIds = new ConcurrentHashMap<>();

    /** Under Runed Terror's sequential phases: each permanent's (and each player's) "until end of turn" /
     *  "this turn" state, split by the active player it was created under (keyed by permanent or player
     *  id, then active player id, ordered by most recent credit), so a cleanup step ends only the active
     *  player's share (Runed Terror ruling, CR 514.2). */
    public final Map<UUID, Map<UUID, EndOfTurnStateShare>> phaseSequenceEndOfTurnShares = new ConcurrentHashMap<>();

    /** Under Runed Terror's sequential phases: per permanent or player id, the state already credited
     *  to some player in {@link #phaseSequenceEndOfTurnShares}, so the next attribution credits only
     *  what changed since. */
    public final Map<UUID, EndOfTurnStateShare> phaseSequenceAttributedEndOfTurnState = new ConcurrentHashMap<>();

    /** Keywords that remain with a card through zone changes. */
    public final Map<UUID, Set<Keyword>> perpetualCardKeywords = new ConcurrentHashMap<>();

    /**
     * Opaque slot for the engine's memoized CR 613 layered board
     * ({@code LayerSystemService.BoardCache} — the engine owns the type, this module cannot
     * reference it; see {@code agent-docs/LAYER_SYSTEM.md} "Board cache"). Deliberately NOT
     * copied by {@link #simulationCopy()}: AI simulation copies must start with a cold cache so
     * a simulated board can never be served for the real game or vice versa. Holds an immutable
     * entry published by a volatile write; concurrent fillers race benignly (last write wins).
     */
    public transient volatile Object layeredBoardCache;

    /**
     * Runs {@code exiling} as one batch of card exiles, one rules event batch, so that "one or more cards are put
     * into exile" abilities trigger once for the whole batch rather than once per card (CR 603.2c). Per-card
     * bookkeeping (exile versions, turn counters) still happens for every card.
     */
    public void runAsSingleCardExileBatch(Runnable exiling) {
        rulesEvents.beginBatch();
        try {
            exiling.run();
        } finally {
            rulesEvents.endBatch();
        }
    }

    private void notifyCardsExiled(Card card, boolean faceDown) {
        if (card != null) {
            long version = exileEntryVersions.merge(card.getId(), 1L, Long::sum);
            java.util.stream.Stream.of(stack, pendingManaAbilityTriggers, suspendedZoneTriggers)
                    .flatMap(java.util.Collection::stream)
                    .filter(entry -> card.getId().equals(entry.getTriggeringCardId())
                            && entry.getTriggeringCardExileEntryVersion() == -1L
                            && entry.getEffectsToResolve().stream().anyMatch(effect -> effect instanceof
                            com.github.laxika.magicalvibes.model.effect.ReturnEarthbendedLandEffect earthbend
                            && earthbend.fromExile()))
                    .forEach(entry -> entry.setTriggeringCardExileEntryVersion(version));
        }
        if (card == null || card.isToken()) {
            return;
        }
        int manaValue = faceDown ? 0 : card.getManaValue();
        playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getStaticEffects().stream()
                        .anyMatch(CardEffect::notesManaValueOfExiledCards))
                .forEach(permanent -> greatestManaValueNotedForPermanentThisTurn.merge(
                        permanent.getId(), manaValue, Math::max));
        cardsExiledThisTurn++;
        cardPutIntoExileThisTurn = true;
    }

    /**
     * Stamps the given floating effect with the next CR 613.7 timestamp, stores it, and returns
     * the stamped instance (the passed-in effect's own timestamp is ignored). An effect whose duration begins later is
     * kept in {@link #scheduledFloatingEffects} until it begins.
     */
    public FloatingContinuousEffect addFloatingEffect(FloatingContinuousEffect effect) {
        FloatingContinuousEffect stamped = effect.withTimestamp(nextTimestamp());
        if (stamped.duration() != null && !stamped.duration().startsNow()) {
            scheduledFloatingEffects.add(stamped);
            return stamped;
        }
        floatingEffects.add(stamped);
        if (stamped.id() != null && activePlayerId != null) {
            floatingEffectCreatingActivePlayerIds.put(stamped.id(), activePlayerId);
        }
        observeFloatingEffect(stamped, true);
        return stamped;
    }

    /**
     * Stores the given floating effect with the timestamp it carries: the effect of a static ability that keeps the
     * timestamp it had (CR 613.7a), or a control effect an Aura's attachment makes.
     */
    public void putFloatingEffect(FloatingContinuousEffect effect) {
        floatingEffects.add(effect);
        observeFloatingEffect(effect, true);
    }

    /** Ends the given floating effect, if it still applies, or drops it if it has not begun. */
    public void endFloatingEffect(FloatingContinuousEffect effect) {
        scheduledFloatingEffects.removeIf(floating -> floating == effect);
        expireFloatingEffects(floating -> floating == effect);
    }

    /**
     * Begins the scheduled floating effects {@code starts} matches (see {@link #scheduledFloatingEffects}), with the
     * timestamps they were created with, and returns them.
     */
    public List<FloatingContinuousEffect> startScheduledFloatingEffects(Predicate<FloatingContinuousEffect> starts) {
        List<FloatingContinuousEffect> started = new ArrayList<>();
        synchronized (scheduledFloatingEffects) {
            var it = scheduledFloatingEffects.iterator();
            while (it.hasNext()) {
                FloatingContinuousEffect effect = it.next();
                if (starts.test(effect)) {
                    it.remove();
                    started.add(effect);
                }
            }
        }
        started.forEach(this::putFloatingEffect);
        return started;
    }

    /**
     * Whether a "skip your next …" effect of {@code kind} waits on {@code playerId} (CR 614.10): a floating
     * {@link com.github.laxika.magicalvibes.model.effect.SkipNextEffect} on that player.
     */
    public boolean hasSkipNext(UUID playerId, com.github.laxika.magicalvibes.model.effect.SkipKind kind) {
        return nextSkip(playerId, kind) != null;
    }

    /**
     * Applies one "skip your next …" effect of {@code kind} waiting on {@code playerId}, as the turn, step or phase it
     * replaces would begin, and returns whether one applied. Each effect is its own replacement and is used up as it
     * applies, so two make the player skip the next two (CR 614.10a); one that lasts only this turn applies first.
     */
    public boolean applySkipNext(UUID playerId, com.github.laxika.magicalvibes.model.effect.SkipKind kind) {
        FloatingContinuousEffect skip = nextSkip(playerId, kind);
        if (skip == null) {
            return false;
        }
        endFloatingEffect(skip);
        return true;
    }

    /**
     * How many "skip your next …" effects of {@code kind} wait on {@code playerId}, or on every player when
     * {@code playerId} is {@code null}.
     */
    public int skipNextCount(UUID playerId, com.github.laxika.magicalvibes.model.effect.SkipKind kind) {
        int count = 0;
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect effect : floatingEffects) {
                if (effect.effect() instanceof com.github.laxika.magicalvibes.model.effect.SkipNextEffect skip
                        && skip.kind() == kind && !skip.allCombatPhasesOfNextTurn()
                        && effect.affectedPlayerId() != null
                        && (playerId == null || playerId.equals(effect.affectedPlayerId()))) {
                    count++;
                }
            }
        }
        return count;
    }

    private FloatingContinuousEffect nextSkip(UUID playerId, com.github.laxika.magicalvibes.model.effect.SkipKind kind) {
        FloatingContinuousEffect next = null;
        for (FloatingContinuousEffect effect : floatingEffectsOnPlayer(playerId,
                com.github.laxika.magicalvibes.model.effect.SkipNextEffect.class)) {
            var skip = (com.github.laxika.magicalvibes.model.effect.SkipNextEffect) effect.effect();
            if (skip.kind() != kind || skip.allCombatPhasesOfNextTurn() || effect.affectedPlayerId() == null) {
                continue;
            }
            if (skip.thisTurnOnly()) {
                return effect;
            }
            if (next == null) {
                next = effect;
            }
        }
        return next;
    }

    /** Replaces each scheduled floating effect {@code matches} selects with what {@code change} makes of it. */
    public void replaceScheduledFloatingEffects(Predicate<FloatingContinuousEffect> matches,
                                                java.util.function.UnaryOperator<FloatingContinuousEffect> change) {
        synchronized (scheduledFloatingEffects) {
            scheduledFloatingEffects.replaceAll(effect -> matches.test(effect) ? change.apply(effect) : effect);
        }
    }

    /** Drops the scheduled floating effects {@code drops} matches: they will never begin. */
    public void dropScheduledFloatingEffects(Predicate<FloatingContinuousEffect> drops) {
        scheduledFloatingEffects.removeIf(drops);
    }

    /**
     * Ends every floating effect on the card {@code cardId}, begun or not, in whatever zone: a token that ceases to
     * exist. Returns the ended effects that had begun.
     */
    public List<FloatingContinuousEffect> endFloatingEffectsOnCard(UUID cardId) {
        scheduledFloatingEffects.removeIf(effect -> cardId.equals(effect.affectedCardId()));
        return expireFloatingEffects(effect -> cardId.equals(effect.affectedCardId()));
    }

    /**
     * Ends the floating effects on the card {@code cardId} in {@code zone}, begun or not, as the card leaves that
     * zone: it becomes a new object (CR 400.7). An effect on the card in another zone stays, so a late report of the
     * card leaving a graveyard does not end what it was given after it reached exile. Returns the ended effects that
     * had begun.
     */
    public List<FloatingContinuousEffect> endFloatingEffectsOnCard(UUID cardId, Zone zone) {
        Predicate<FloatingContinuousEffect> inZone = effect -> cardId.equals(effect.affectedCardId())
                && zone == effect.affectedCardZone();
        scheduledFloatingEffects.removeIf(inZone);
        return expireFloatingEffects(inZone);
    }

    /**
     * Ends the floating effects on the card {@code cardId} in any zone but {@code zone}, begun or not, as the card is
     * put into {@code zone}: it left whichever zone it was in (CR 400.7). Returns the ended effects that had begun.
     */
    public List<FloatingContinuousEffect> endFloatingEffectsOnCardOutside(UUID cardId, Zone zone) {
        Predicate<FloatingContinuousEffect> elsewhere = effect -> cardId.equals(effect.affectedCardId())
                && zone != effect.affectedCardZone();
        scheduledFloatingEffects.removeIf(elsewhere);
        return expireFloatingEffects(elsewhere);
    }

    /**
     * Puts {@code replacement} in place of {@code effect}, keeping its place in the list: a grant that perpetually
     * lost part of what it gives. Reported as {@code effect} ending and {@code replacement} beginning.
     */
    public void replaceFloatingEffect(FloatingContinuousEffect effect, FloatingContinuousEffect replacement) {
        synchronized (floatingEffects) {
            int index = floatingEffects.indexOf(effect);
            if (index < 0) {
                return;
            }
            floatingEffects.set(index, replacement);
        }
        observeFloatingEffect(effect, false);
        observeFloatingEffect(replacement, true);
    }

    /** Reports a floating effect that began or ended to {@link #rulesObserver}. */
    private void observeFloatingEffect(FloatingContinuousEffect effect, boolean began) {
        if (rulesObserver == null || rulesEvents.isSuppressed()) {
            return;
        }
        if (began) {
            rulesObserver.floatingEffectBegan(effect);
        } else {
            rulesObserver.floatingEffectEnded(effect);
        }
    }

    /**
     * Returns {@code true} if the permanent with the given id is prevented from dealing damage,
     * whether by a this-turn floating prevention effect (Soul Parry) or an until-your-next-turn one
     * (Gideon of the Trials +1).
     */
    public boolean isPreventedFromDealingDamage(UUID permanentId) {
        return hasPreventionOnPermanent(permanentId, false,
                PreventionScope.ALL_BY_TARGET_CREATURES, PreventionScope.ALL_BY_SELF)
                || hasPreventionOnPermanent(permanentId,
                PreventionScope.ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN,
                PreventionScope.ALL_TO_AND_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN);
    }

    /** Returns whether all damage to the permanent is prevented until a player's next turn. */
    public boolean isProtectedFromDamageUntilNextTurn(UUID permanentId) {
        return hasPreventionOnPermanent(permanentId, PreventionScope.ALL_TO_AND_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN);
    }

    /**
     * Drops the "when you lose control of that Equipment this turn" triggers (Stolen Uniform) that end at this
     * cleanup step, keeping only those still credited to another player under Runed Terror's sequential phases
     * (Runed Terror ruling, CR 514.2). The cleanup step calls this after recomputing control, so a trigger can
     * still fire as its control effect ends.
     */
    public void retainControlLossUnattachTriggers() {
        List<Map.Entry<UUID, ControlLossUnattachTrigger>> retained = runedTerrorPhaseSequenceActive
                ? phaseSequenceAttributedEndOfTurnState.getOrDefault(GAME_END_OF_TURN_STATE_ID,
                        EndOfTurnStateShare.EMPTY).elements(EndOfTurnStateField.GAME_CONTROL_LOSS_UNATTACH_TRIGGERS)
                : List.of();
        controlLossUnattachTriggers.clear();
        addSynchronizedListMapEntries(controlLossUnattachTriggers, retained);
    }

    public void registerControlLossUnattachTrigger(UUID equipmentId, UUID controllerId, Card sourceCard) {
        controlLossUnattachTriggers
                .computeIfAbsent(equipmentId, ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(new ControlLossUnattachTrigger(controllerId, sourceCard));
    }

    public List<ControlLossUnattachTrigger> controlLossUnattachTriggersFor(UUID equipmentId) {
        List<ControlLossUnattachTrigger> triggers = controlLossUnattachTriggers.get(equipmentId);
        if (triggers == null) {
            return List.of();
        }
        synchronized (triggers) {
            return List.copyOf(triggers);
        }
    }

    public void registerControlLossTapTrigger(UUID permanentId, UUID controllerId, Card sourceCard) {
        controlLossTapTriggers
                .computeIfAbsent(permanentId, ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(new ControlLossTapTrigger(controllerId, sourceCard));
    }

    public List<ControlLossTapTrigger> controlLossTapTriggersFor(UUID permanentId) {
        List<ControlLossTapTrigger> triggers = controlLossTapTriggers.get(permanentId);
        if (triggers == null) {
            return List.of();
        }
        synchronized (triggers) {
            return List.copyOf(triggers);
        }
    }

        /** The id under which {@link #phaseSequenceEndOfTurnShares} tracks the game-wide {@code GAME_} stores; it is
     *  the nil UUID, which no player or permanent has. */
    public static final UUID GAME_END_OF_TURN_STATE_ID = new UUID(0L, 0L);

    /**
     * Captures the game-wide "until end of turn" / "this turn" stores named by the {@code GAME_}
     * {@link EndOfTurnStateField}s (damage prevention flags, shields and sets, the turn-long life, search
     * and attack restrictions, player protections, casting / activation restrictions and permissions,
     * "this turn" zone-change replacements, graveyard card animations, Circle of Protection colour shields, damage
     * doublings, bonuses and replacements, enter-tapped and extra-counter replacements, block requirements, extra
     * loyalty activations, the mana-fixing / mana-replacement stores, player-granted static effects, damage life
     * floors and spell limits, the one-shot or turn-long redirection / prevention shields that are added and
     * removed whole, the pending "next spell you cast this turn" grants, spell colour overrides, the per-card
     * "this turn" casting permissions and granted alternative costs, the unpaired pending "next spell / ability this
     * turn" copy, Storm, uncounterable and any-mana-type grants, the "this turn" delayed copy / draw / untap
     * triggers, the source-linked exile and foretold cast grants, the whole-turn chosen-source prevention with its
     * colour and life-gain riders, the one-shot "next damage from a matching source" shields, the pending extra
     * counters for the next enchantment creature entry, the pending "next instant or sorcery / loyalty / exhaust
     * ability this turn" copy triggers with their sources, Sen Triplets' hand permission, the per-card exile / graveyard
     * play permissions that end this turn with their current-turn any-mana-type riders, the source-linked "until end
     * of turn, you may play cards exiled with it" grants, and the shield lists whose records are rewritten in place or
     * replaced by a reduced copy when partly used, the "this turn" delayed-trigger watchers and "until end of turn"
     * global triggered abilities, the pending "the next time you would draw a card this turn" replacements and
     * Mystic Reflection's pending entry copies, Plagiarize's draw redirections, the permanents whose
     * source subtype choice lasts until end of turn, Call for Aid's sacrifice restriction, Stolen Uniform's
     * control-loss unattach triggers, the chosen skipped steps or phases, and the turn-history records of speed
     * increases, plays from exile or outside the hand, foretelling, first-non-draw-step draw replacements, exiled
     * cards, cards leaving graveyards and creature exiles), which the cleanup step clears
     * (CR 514.2). Used to split that state between the players active when it was created under Runed Terror's sequential phases.
     */
    public EndOfTurnStateShare captureGameEndOfTurnState() {
        Map<EndOfTurnStateField, Object> raw = new EnumMap<>(EndOfTurnStateField.class);
        for (EndOfTurnStateField field : EndOfTurnStateField.values()) {
            raw.put(field, gameEndOfTurnStateValue(field));
        }
        return EndOfTurnStateShare.of(raw);
    }

    /**
     * Adds {@code share} onto the current game-wide "until end of turn" state: flags become true, the
     * shield grows and set elements (or map entries) are added. The cleanup step calls this after
     * clearing those stores, to re-apply what other players created under Runed Terror's sequential
     * phases.
     */
    public void addGameEndOfTurnState(EndOfTurnStateShare share) {
        share.values().forEach(this::addGameEndOfTurnStateValue);
    }

    private Object gameEndOfTurnStateValue(EndOfTurnStateField field) {
        return switch (field) {
            case GAME_OPPONENT_GRAVEYARD_LIFE_LOSS_WATCHERS -> synchronizedCopy(opponentGraveyardLifeLossWatchers);
            case GAME_LIFE_GAIN_OPPONENT_LIFE_LOSS_WATCHERS -> synchronizedCopy(lifeGainOpponentLifeLossWatchers);
            case GAME_ALLY_CREATURE_ENTERS_TRIGGER_WATCHERS -> synchronizedCopy(allyCreatureEntersTriggerWatchers);
            case GAME_TEMPORARY_GLOBAL_TRIGGERED_ABILITIES_THIS_TURN -> synchronizedCopy(temporaryGlobalTriggeredAbilities)
                    .stream()
                    .filter(TemporaryGlobalTriggeredAbility::endsThisTurn)
                    .toList();
            case GAME_CONTROL_LOSS_UNATTACH_TRIGGERS -> synchronizedListMapEntries(controlLossUnattachTriggers);
            case GAME_PLAYERS_WHOSE_SPEED_INCREASED -> playersWhoseSpeedIncreasedThisTurn;
            case GAME_PLAYERS_WHO_PLAYED_CARD_FROM_EXILE -> playersWhoPlayedCardFromExileThisTurn;
            case GAME_PLAYERS_WHO_PLAYED_OR_CAST_FROM_OUTSIDE_HAND -> playersWhoPlayedOrCastFromOutsideHandThisTurn;
            case GAME_PLAYERS_WHO_FORETOLD -> playersWhoForetoldThisTurn;
            case GAME_CARDS_EXILED -> cardsExiledThisTurn;
            case GAME_FIRST_NON_DRAW_STEP_DRAW_REPLACEMENTS_USED -> firstNonDrawStepDrawReplacementsUsedThisTurn;
            case GAME_PLAYERS_WHOSE_CARDS_LEFT_GRAVEYARD -> playersWhoseCardsLeftGraveyardThisTurn;
            case GAME_PLAYERS_WHOSE_CREATURE_CARDS_LEFT_GRAVEYARD -> playersWhoseCreatureCardsLeftGraveyardThisTurn;
            case GAME_CARDS_LEFT_GRAVEYARD_COUNTS -> new HashMap<>(cardsLeftGraveyardCountThisTurn);
            case GAME_CREATURE_EXILE_COUNTS -> new HashMap<>(creatureExileCountThisTurn);
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private void addGameEndOfTurnStateValue(EndOfTurnStateField field, Object value) {
        switch (field) {
            case GAME_OPPONENT_GRAVEYARD_LIFE_LOSS_WATCHERS ->
                    opponentGraveyardLifeLossWatchers.addAll((List<OpponentGraveyardLifeLossWatcher>) value);
            case GAME_LIFE_GAIN_OPPONENT_LIFE_LOSS_WATCHERS ->
                    lifeGainOpponentLifeLossWatchers.addAll((List<LifeGainOpponentLifeLossWatcher>) value);
            case GAME_ALLY_CREATURE_ENTERS_TRIGGER_WATCHERS ->
                    allyCreatureEntersTriggerWatchers.addAll((List<CreatureEntersTriggerWatcher>) value);
            case GAME_TEMPORARY_GLOBAL_TRIGGERED_ABILITIES_THIS_TURN ->
                    temporaryGlobalTriggeredAbilities.addAll((List<TemporaryGlobalTriggeredAbility>) value);
            // Not cleared before the retained state is re-applied: the cleanup step drops only the ending triggers,
            // after control is recomputed (see retainControlLossUnattachTriggers).
            case GAME_CONTROL_LOSS_UNATTACH_TRIGGERS -> {
            }
            case GAME_PLAYERS_WHOSE_SPEED_INCREASED -> playersWhoseSpeedIncreasedThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_PLAYED_CARD_FROM_EXILE ->
                    playersWhoPlayedCardFromExileThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_PLAYED_OR_CAST_FROM_OUTSIDE_HAND ->
                    playersWhoPlayedOrCastFromOutsideHandThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_FORETOLD -> playersWhoForetoldThisTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_EXILED -> cardsExiledThisTurn += (Integer) value;
            case GAME_FIRST_NON_DRAW_STEP_DRAW_REPLACEMENTS_USED ->
                    firstNonDrawStepDrawReplacementsUsedThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHOSE_CARDS_LEFT_GRAVEYARD ->
                    playersWhoseCardsLeftGraveyardThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHOSE_CREATURE_CARDS_LEFT_GRAVEYARD ->
                    playersWhoseCreatureCardsLeftGraveyardThisTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_LEFT_GRAVEYARD_COUNTS ->
                    addCounts(cardsLeftGraveyardCountThisTurn, (Map<UUID, Integer>) value);
            case GAME_CREATURE_EXILE_COUNTS -> addCounts(creatureExileCountThisTurn, (Map<UUID, Integer>) value);
            default -> {
            }
        }
    }

    private static <T> List<T> synchronizedCopy(List<T> list) {
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    private static <K, V> Set<Map.Entry<K, V>> mapEntries(Map<K, Set<V>> map) {
        Set<Map.Entry<K, V>> entries = new HashSet<>();
        map.forEach((key, values) -> values.forEach(value -> entries.add(Map.entry(key, value))));
        return entries;
    }

    private static <K, V> void addMapEntries(Map<K, Set<V>> map, Set<Map.Entry<K, V>> entries) {
        entries.forEach(entry ->
                map.computeIfAbsent(entry.getKey(), key -> ConcurrentHashMap.newKeySet()).add(entry.getValue()));
    }

    private static <K, V> List<Map.Entry<K, V>> plainMapEntries(Map<K, V> map) {
        return map.entrySet().stream().map(entry -> Map.entry(entry.getKey(), entry.getValue())).toList();
    }

    private static <K, V> Map<Map.Entry<K, V>, Integer> nestedCounts(Map<K, Map<V, Integer>> map) {
        Map<Map.Entry<K, V>, Integer> counts = new HashMap<>();
        map.forEach((key, inner) -> inner.forEach((value, count) -> counts.put(Map.entry(key, value), count)));
        return counts;
    }

    private static <K> void addCounts(Map<K, Integer> map, Map<K, Integer> counts) {
        counts.forEach((key, count) -> map.merge(key, count, Integer::sum));
    }

    private static <K, V> List<Map.Entry<K, V>> listMapEntries(Map<K, List<V>> map) {
        List<Map.Entry<K, V>> entries = new ArrayList<>();
        map.forEach((key, values) -> values.forEach(value -> entries.add(Map.entry(key, value))));
        return entries;
    }

    private static <K, V> List<Map.Entry<K, V>> synchronizedListMapEntries(Map<K, List<V>> map) {
        List<Map.Entry<K, V>> entries = new ArrayList<>();
        map.forEach((key, values) -> {
            synchronized (values) {
                values.forEach(value -> entries.add(Map.entry(key, value)));
            }
        });
        return entries;
    }

    private static <K, V> void addSynchronizedListMapEntries(Map<K, List<V>> map, List<Map.Entry<K, V>> entries) {
        entries.forEach(entry -> map.computeIfAbsent(entry.getKey(),
                key -> Collections.synchronizedList(new ArrayList<>())).add(entry.getValue()));
    }

    private static <K, V> void addListMapEntries(Map<K, List<V>> map, List<Map.Entry<K, V>> entries) {
        entries.forEach(entry ->
                map.computeIfAbsent(entry.getKey(), key -> new CopyOnWriteArrayList<>()).add(entry.getValue()));
    }

    /** Marks the beginning of an upkeep for effects that last through the end of that upkeep. */
    public void markUpkeepStart() {
        upkeepStartTimestamp = timestampCounter + 1;
    }

    public List<FloatingContinuousEffect> expireFloatingEffects(Predicate<FloatingContinuousEffect> expired) {
        List<FloatingContinuousEffect> removed = new ArrayList<>();
        var it = floatingEffects.iterator();
        while (it.hasNext()) {
            FloatingContinuousEffect fe = it.next();
            if (expired.test(fe)) {
                removed.add(fe);
                it.remove();
                if (fe.id() != null) {
                    floatingEffectCreatingActivePlayerIds.remove(fe.id());
                }
            }
        }
        removed.forEach(fe -> observeFloatingEffect(fe, false));
        pruneExilePlayPermissionGroups(removed);
        return removed;
    }

    // ── Derived control state (CR 613.2/613.7 layer 2) ─────────────────────────────────────

    /**
     * Removes and returns all control-changing floating effects that apply to the given
     * permanent. Called when the permanent leaves the battlefield (its control effects can
     * never apply to a new object — CR 611.2c).
     */
    public List<FloatingContinuousEffect> expireControlEffectsForDepartedPermanent(UUID permanentId) {
        return expireFloatingEffects(fe -> fe.isControlEffect()
                && permanentId.equals(fe.affectedPermanentId()));
    }

    /**
     * The floating effects of any of {@code types} that apply to {@code playerId} (they name that player, or no object
     * at all), oldest first: the rules-modifying effects resolutions made on players (CR 611.2a). A {@code null}
     * player matches only the effects on every player.
     */
    @SafeVarargs
    public final List<FloatingContinuousEffect> floatingEffectsOnPlayer(UUID playerId,
                                                                         Class<? extends CardEffect>... types) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.appliesToPlayer(playerId) && isAnyOf(fe.effect(), types)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /**
     * The effects of the floating effects of any of {@code types} that apply to {@code playerId} (see
     * {@link #floatingEffectsOnPlayer}), oldest first.
     */
    @SafeVarargs
    public final List<CardEffect> floatingPlayerEffects(UUID playerId, Class<? extends CardEffect>... types) {
        return floatingEffectsOnPlayer(playerId, types).stream().map(FloatingContinuousEffect::effect).toList();
    }

    /**
     * The floating effects of any of {@code types} that apply to some player (each names one), oldest first. Read the
     * player from {@link FloatingContinuousEffect#affectedPlayerId()}.
     */
    @SafeVarargs
    public final List<FloatingContinuousEffect> floatingEffectsOnNamedPlayers(Class<? extends CardEffect>... types) {
        return floatingEffectsOfTypes(types).stream().filter(fe -> fe.affectedPlayerId() != null).toList();
    }

    /**
     * Permits {@code playerId} to play {@code cardIds} from outside the game until end of turn (CR 611.2a), as a
     * floating {@link com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect} on that player.
     * Nothing is added for no cards.
     */
    public void permitPlayFromOutsideGame(String sourceCardName, UUID sourcePermanentId, UUID playerId,
                                          java.util.Collection<UUID> cardIds, boolean singlePlay,
                                          boolean additionalModalMode) {
        if (cardIds.isEmpty()) {
            return;
        }
        addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceCardName, sourcePermanentId, playerId,
                new com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect(
                        Set.copyOf(cardIds), singlePlay, additionalModalMode),
                playerId, com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN));
    }

    /** Whether a floating permission lets the card {@code cardId} be played from outside the game. */
    public boolean mayPlayFromOutsideGame(UUID cardId) {
        return !outsideGamePermissionsFor(cardId).isEmpty();
    }

    /** Whether a modal card cast from outside the game with a permission may choose one more mode. */
    public boolean mayChooseAdditionalModeFromOutsideGame(UUID cardId) {
        return outsideGamePermissionsFor(cardId).stream().anyMatch(fe ->
                ((com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect) fe.effect())
                        .additionalModalMode());
    }

    /**
     * Uses up a permission to play {@code cardId} from outside the game as the card is played or leaves outside the game:
     * the oldest one naming it is spent (a single-play one for every card it named, another only for that card), and the
     * others stop naming the card, which is a new object once it moves (CR 400.7).
     */
    public void spendPlayFromOutsideGame(UUID cardId) {
        boolean spent = false;
        for (FloatingContinuousEffect fe : outsideGamePermissionsFor(cardId)) {
            var permission = (com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect) fe.effect();
            Set<UUID> rest = new HashSet<>(permission.cardIds());
            rest.remove(cardId);
            if ((!spent && permission.singlePlay()) || rest.isEmpty()) {
                endFloatingEffect(fe);
            } else {
                replaceFloatingEffect(fe, fe.withEffect(
                        new com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect(
                                rest, permission.singlePlay(), permission.additionalModalMode())));
            }
            spent = true;
        }
    }

    private List<FloatingContinuousEffect> outsideGamePermissionsFor(UUID cardId) {
        return floatingEffectsOfTypes(com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect.class)
                .stream()
                .filter(fe -> ((com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect) fe.effect())
                        .cardIds().contains(cardId))
                .toList();
    }

    /**
     * Makes the card {@code cardId} be exiled instead of put into a graveyard this turn, the spell it becomes on the
     * stack included (CR 614.1a): a floating {@link com.github.laxika.magicalvibes.model.effect.ExileCardInsteadOfGraveyardEffect}
     * on {@code controllerId}, made by {@code sourceCardName}. A card already under one gets no second.
     */
    public void exileInsteadOfGraveyard(String sourceCardName, UUID controllerId, UUID cardId) {
        if (exilesInsteadOfGraveyard(cardId)) {
            return;
        }
        addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceCardName, null, controllerId,
                new com.github.laxika.magicalvibes.model.effect.ExileCardInsteadOfGraveyardEffect(cardId), controllerId,
                com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN));
    }

    /** {@link #exileInsteadOfGraveyard(String, UUID, UUID)} made by the resolving {@code entry}. */
    public void exileInsteadOfGraveyard(StackEntry entry, UUID cardId) {
        exileInsteadOfGraveyard(entry.getCard() == null ? null : entry.getCard().getName(), entry.getControllerId(),
                cardId);
    }

    /** Whether the card {@code cardId} is to be exiled instead of put into a graveyard. */
    public boolean exilesInsteadOfGraveyard(UUID cardId) {
        return !exileInsteadOfGraveyardEffects(cardId).isEmpty();
    }

    /**
     * Uses up the "exile it instead" effect on the card {@code cardId}, as the card is cast with it or exiled by it, and
     * returns whether there was one.
     */
    public boolean endExileInsteadOfGraveyard(UUID cardId) {
        List<FloatingContinuousEffect> effects = exileInsteadOfGraveyardEffects(cardId);
        effects.forEach(this::endFloatingEffect);
        return !effects.isEmpty();
    }

    private List<FloatingContinuousEffect> exileInsteadOfGraveyardEffects(UUID cardId) {
        return floatingEffectsOfTypes(com.github.laxika.magicalvibes.model.effect.ExileCardInsteadOfGraveyardEffect.class)
                .stream()
                .filter(fe -> ((com.github.laxika.magicalvibes.model.effect.ExileCardInsteadOfGraveyardEffect) fe.effect())
                        .cardId().equals(cardId))
                .toList();
    }

    /**
     * Adds a "permanents [matching it] don't untap during [that player's] next untap step" effect on {@code playerId}
     * (CR 611.2c: it applies to whatever matches as that untap step begins). It is used up there, or, for a later step
     * of several, replaced by one for one step fewer.
     */
    public void restrictNextUntapStep(StackEntry entry, UUID playerId, SkipNextUntapEffect restriction) {
        addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard() == null ? null : entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), restriction, playerId,
                com.github.laxika.magicalvibes.model.effect.Duration.PERMANENT));
    }

    /** The "don't untap during the next untap step" effects waiting on {@code playerId}'s untap step, oldest first. */
    public List<FloatingContinuousEffect> nextUntapStepRestrictions(UUID playerId) {
        return floatingEffectsOnPlayer(playerId, SkipNextUntapEffect.class).stream()
                .filter(fe -> playerId.equals(fe.affectedPlayerId()))
                .toList();
    }

    /**
     * "If that creature would die this turn, exile it instead" (CR 614.1a) on the permanent {@code permanentId}, made by
     * the resolving {@code entry}: a floating {@link com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect} until
     * end of turn. With {@code trackWithSource} the card exiled this way is linked to the entry's source permanent.
     */
    public void exileInsteadOfDying(StackEntry entry, UUID permanentId, boolean trackWithSource) {
        addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard() == null ? null : entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect(trackWithSource), permanentId,
                com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN));
    }

    /** {@link #exileInsteadOfDying(StackEntry, UUID, boolean)} with no link to the source. */
    public void exileInsteadOfDying(StackEntry entry, UUID permanentId) {
        exileInsteadOfDying(entry, permanentId, false);
    }

    /** Whether the permanent {@code permanentId} is to be exiled instead of dying this turn. */
    public boolean exilesInsteadOfDying(UUID permanentId) {
        return hasFloatingEffectOnPermanent(permanentId, com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect.class);
    }

    /**
     * The source permanent the newest linked "exile it instead of dying" effect on {@code permanentId} links the exiled
     * card to, or {@code null}.
     */
    public UUID exileInsteadOfDyingLinkedSource(UUID permanentId) {
        UUID linked = null;
        for (FloatingContinuousEffect fe : floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect.class)) {
            if (((com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect) fe.effect()).trackWithSource()) {
                linked = fe.sourcePermanentId();
            }
        }
        return linked;
    }

    /**
     * A rules-modifying effect the resolving {@code entry} makes on the permanent {@code permanentId} for
     * {@code duration} (CR 611.2a): "it can't block this turn", "it must be blocked this turn if able". The effect stays
     * with that permanent and ends with its duration; a permanent that leaves the battlefield and returns is a new
     * object it doesn't apply to.
     */
    public void addEffectOnPermanent(StackEntry entry, CardEffect effect, UUID permanentId,
                                     com.github.laxika.magicalvibes.model.effect.Duration duration) {
        addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard() == null ? null : entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, permanentId, duration));
    }

    /**
     * The player the newest "attacks that player if able" requirement on {@code permanentId} names
     * ({@link com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect#defenderId()}), or {@code null} when
     * none names one.
     */
    public UUID mustAttackDefender(UUID permanentId) {
        UUID defender = null;
        for (FloatingContinuousEffect fe : floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect.class)) {
            UUID named = ((com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect) fe.effect()).defenderId();
            if (named != null) {
                defender = named;
            }
        }
        return defender;
    }

    /**
     * How many creatures beyond one the permanent {@code permanentId} can block by the resolved "can block an
     * additional creature this turn" effects on it, {@link Integer#MAX_VALUE} once one lets it block any number.
     */
    public int additionalBlocksThisTurn(UUID permanentId) {
        int total = 0;
        for (FloatingContinuousEffect fe : floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect.class,
                com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect.class)) {
            if (!(fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect grant)) {
                return Integer.MAX_VALUE;
            }
            total += grant.additionalBlocks();
        }
        return total;
    }

    /**
     * The attackers the permanent {@code permanentId} must block if able (CR 509.1c), by the resolved "blocks [that
     * creature] this turn / this combat if able" effects on it
     * ({@link com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect#sourcePermanentId()}).
     */
    public Set<UUID> requiredBlockSourceIds(UUID permanentId) {
        Set<UUID> sources = new LinkedHashSet<>();
        for (FloatingContinuousEffect fe : floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect.class)) {
            sources.add(((com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect) fe.effect()).sourcePermanentId());
        }
        return sources;
    }

    /**
     * The attackers the permanent {@code permanentId} can't block, by the resolved "can't block [that creature] this
     * turn" effects on it ({@link com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect#sourcePermanentId()}).
     */
    public Set<UUID> cantBlockSourceIds(UUID permanentId) {
        Set<UUID> sources = new LinkedHashSet<>();
        for (FloatingContinuousEffect fe : floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect.class)) {
            sources.add(((com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect) fe.effect()).sourcePermanentId());
        }
        return sources;
    }

    /** The resolved "can't be blocked except by …" effects on the permanent {@code permanentId}, oldest first. */
    public List<com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect> blockRestrictions(
            UUID permanentId) {
        return floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect.class).stream()
                .map(fe -> (com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect) fe.effect())
                .toList();
    }

    /**
     * What the defending player must control for the permanent {@code permanentId} to be unblockable, by the resolved
     * landwalk grants on it (Barbarian Guides' snow landwalk of a chosen type).
     */
    public List<PermanentPredicate> unblockableIfDefenderControls(UUID permanentId) {
        return floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect.class)
                .stream()
                .map(fe -> ((com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect)
                        fe.effect()).defenderPermanentPredicate())
                .toList();
    }

    /**
     * Whether a resolved effect makes the permanent {@code permanentId} a creature: a floating animation or card-type
     * effect on it that adds or sets the creature type ("becomes a 3/3 creature until end of turn", crew). For readers
     * outside the layered pass that answer "is it a creature" from the permanent alone; the layered pass applies these
     * effects in timestamp order.
     */
    public boolean animatedByFloatingEffect(UUID permanentId) {
        return floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect.class,
                com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect.class,
                com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect.class).stream()
                .anyMatch(fe -> switch (fe.effect()) {
                    case com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect grant ->
                            grant.cardType() == CardType.CREATURE;
                    case com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect set ->
                            set.cardTypes().contains(CardType.CREATURE);
                    case com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect animate ->
                            animate.becomesCreature();
                    default -> false;
                });
    }

    /**
     * Whether damage that would be dealt to the permanent {@code permanentId} this turn can't be prevented or dealt
     * instead to another permanent or player, by a resolved effect on it (Whippoorwill).
     */
    public boolean damageCantBePreventedOrRedirected(UUID permanentId) {
        return hasFloatingEffectOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.DamageToTargetCreatureCantBePreventedOrRedirectedThisTurnEffect.class);
    }

    /** Whether the permanent {@code permanentId} can't be regenerated this turn, by a resolved effect on it. */
    public boolean cantBeRegenerated(UUID permanentId) {
        return hasFloatingEffectOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect.class);
    }

    /**
     * Whether {@code playerId} ignores the effect of the permanent {@code permanentId}, by a resolved "that player
     * ignores this effect until end of turn" effect of {@code type} on it (Volrath's Curse, Damping Engine). Each is
     * made for the player who paid, its controller; another player is still bound by the permanent's effect.
     */
    public boolean ignoresEffectOf(UUID permanentId, Class<? extends CardEffect> type, UUID playerId) {
        return playerId != null && floatingEffectsOnPermanent(permanentId, type).stream()
                .anyMatch(fe -> playerId.equals(fe.controllerId()));
    }

    /**
     * Whether spells and abilities {@code playerId} controls can target the permanent {@code permanentId} as though
     * it didn't have shroud, by the resolved effects on it that name that player (Autumn Willow).
     */
    public boolean mayTargetIgnoringShroud(UUID permanentId, UUID playerId) {
        return playerId != null && floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.AllowTargetPlayerToTargetSourceIgnoringShroudEffect.class)
                .stream()
                .anyMatch(fe -> playerId.equals(((com.github.laxika.magicalvibes.model.effect
                        .AllowTargetPlayerToTargetSourceIgnoringShroudEffect) fe.effect()).playerId()));
    }

    /**
     * Uses up the oldest "the next time it adapts this turn, it adapts as though it had no +1/+1 counters on it"
     * effect on the permanent {@code permanentId} (Biomancer's Familiar), returning whether there was one.
     */
    public boolean useAdaptOverride(UUID permanentId) {
        List<FloatingContinuousEffect> overrides = floatingEffectsOnPermanent(permanentId,
                com.github.laxika.magicalvibes.model.effect.AllowTargetCreatureToAdaptEffect.class);
        if (overrides.isEmpty()) {
            return false;
        }
        endFloatingEffect(overrides.getFirst());
        return true;
    }

    /**
     * Makes {@code controllerId} control {@code controlledId} for {@code duration} (CR 724): a floating
     * {@link com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect} on the controlled player. Of the effects on one player the last one created is
     * the one that works (CR 724.1a), so this ends the older ones, begun or not; an older one that promises an extra
     * turn is kept when this one promises one too, since each such effect makes its own (Emrakul, the Promised End).
     */
    public void controlPlayer(String sourceCardName, UUID sourcePermanentId, UUID controllerId, UUID controlledId,
                              com.github.laxika.magicalvibes.model.effect.Duration duration, boolean extraTurnAfter) {
        Predicate<FloatingContinuousEffect> overwritten = fe -> controlledId.equals(fe.affectedPlayerId())
                && fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect control
                && !(extraTurnAfter && control.extraTurnAfter());
        dropScheduledFloatingEffects(overwritten);
        expireFloatingEffects(overwritten);
        addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceCardName, sourcePermanentId, controllerId,
                new com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect(controllerId, extraTurnAfter), controlledId, duration));
    }

    /** The player-control effect that works now, or {@code null}: the newest one begun (CR 724.1a). */
    public FloatingContinuousEffect playerControl() {
        FloatingContinuousEffect working = null;
        for (FloatingContinuousEffect fe : floatingEffectsOnNamedPlayers(com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect.class)) {
            if (working == null || fe.timestamp() >= working.timestamp()) {
                working = fe;
            }
        }
        return working;
    }

    /**
     * The control effects waiting for the controlled player's next turn, as controlled player to controller (one per
     * player, CR 724.1a).
     */
    public Map<UUID, UUID> pendingTurnControl() {
        return pendingPlayerControl(com.github.laxika.magicalvibes.model.effect.Duration.Start.AFFECTED_PLAYERS_NEXT_TURN);
    }

    /** The control effects waiting for the controlled player's next combat phase, as controlled player to controller. */
    public Map<UUID, UUID> pendingCombatControl() {
        return pendingPlayerControl(com.github.laxika.magicalvibes.model.effect.Duration.Start.AFFECTED_CONTROLLERS_NEXT_COMBAT);
    }

    /** The controlled player of each waiting control effect that promises an extra turn after it (Emrakul). */
    public List<UUID> pendingTurnControlExtraTurns() {
        synchronized (scheduledFloatingEffects) {
            return scheduledFloatingEffects.stream()
                    .filter(fe -> fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect control && control.extraTurnAfter())
                    .map(FloatingContinuousEffect::affectedPlayerId)
                    .toList();
        }
    }

    private Map<UUID, UUID> pendingPlayerControl(com.github.laxika.magicalvibes.model.effect.Duration.Start start) {
        Map<UUID, UUID> pending = new HashMap<>();
        synchronized (scheduledFloatingEffects) {
            for (FloatingContinuousEffect fe : scheduledFloatingEffects) {
                if (fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect control && fe.duration().start() == start) {
                    pending.put(fe.affectedPlayerId(), control.controllerPlayerId());
                }
            }
        }
        return pending;
    }

    /** The player being controlled by another player now, or {@code null}. */
    public UUID mindControlledPlayerId() {
        FloatingContinuousEffect control = playerControl();
        return control == null ? null : control.affectedPlayerId();
    }

    /** The player controlling another player now, or {@code null}. */
    public UUID mindControllerPlayerId() {
        FloatingContinuousEffect control = playerControl();
        return control == null ? null : ((com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect) control.effect()).controllerPlayerId();
    }

    /** Ends {@code controllerId}'s control of {@code controlledId} (Word of Command's, as its window closes). */
    public void endPlayerControl(UUID controllerId, UUID controlledId) {
        expireFloatingEffects(fe -> controlledId.equals(fe.affectedPlayerId())
                && fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect control
                && controllerId.equals(control.controllerPlayerId()));
    }

    /** Whether a floating effect of any of {@code types} applies to {@code playerId}. */
    @SafeVarargs
    public final boolean hasFloatingEffectOnPlayer(UUID playerId, Class<? extends CardEffect>... types) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.appliesToPlayer(playerId) && isAnyOf(fe.effect(), types)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The floating effects of any of {@code types} that name the permanent {@code permanentId} as the one they affect,
     * oldest first. An effect whose affected set is a {@code scope} predicate is not among them: judge those against
     * the permanent where they are read.
     */
    @SafeVarargs
    public final List<FloatingContinuousEffect> floatingEffectsOnPermanent(UUID permanentId,
                                                                            Class<? extends CardEffect>... types) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (permanentId.equals(fe.affectedPermanentId()) && isAnyOf(fe.effect(), types)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /**
     * The floating effects of any of {@code types} that name the card {@code cardId} (a card outside the battlefield)
     * as the one they affect, oldest first.
     */
    @SafeVarargs
    public final List<FloatingContinuousEffect> floatingEffectsOnCard(UUID cardId,
                                                                       Class<? extends CardEffect>... types) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (cardId.equals(fe.affectedCardId()) && isAnyOf(fe.effect(), types)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /** Whether a floating effect of any of {@code types} names the card {@code cardId}. */
    @SafeVarargs
    public final boolean hasFloatingEffectOnCard(UUID cardId, Class<? extends CardEffect>... types) {
        return !floatingEffectsOnCard(cardId, types).isEmpty();
    }

    /**
     * The payloads of type {@code type} of the floating effects that name the card {@code cardId} (a card outside the
     * battlefield) as the one they affect, oldest first.
     */
    public final <T extends CardEffect> List<T> cardEffects(UUID cardId, Class<T> type) {
        List<T> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (cardId.equals(fe.affectedCardId()) && type.isInstance(fe.effect())) {
                    result.add(type.cast(fe.effect()));
                }
            }
        }
        return result;
    }

    /** The newest payload of type {@code type} of the floating effects on the card {@code cardId}, if any. */
    public final <T extends CardEffect> Optional<T> newestCardEffect(UUID cardId, Class<T> type) {
        List<T> all = cardEffects(cardId, type);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.getLast());
    }

    /** The newest grant of {@code ability} on the card {@code cardId}, if it was granted it for a while. */
    public final Optional<GrantedCardAbilityEffect> grantedCardAbility(UUID cardId, GrantedCardAbilityEffect.Ability ability) {
        List<GrantedCardAbilityEffect> grants = cardEffects(cardId, GrantedCardAbilityEffect.class);
        for (int i = grants.size() - 1; i >= 0; i--) {
            if (grants.get(i).ability() == ability) {
                return Optional.of(grants.get(i));
            }
        }
        return Optional.empty();
    }

    /** The fixed cost of the newest flashback grant on the card {@code cardId} that fixes one, or {@code null}. */
    public final String grantedFlashbackCost(UUID cardId) {
        List<GrantedCardAbilityEffect> grants = cardEffects(cardId, GrantedCardAbilityEffect.class);
        for (int i = grants.size() - 1; i >= 0; i--) {
            if (grants.get(i).ability() == GrantedCardAbilityEffect.Ability.FLASHBACK && grants.get(i).cost() != null) {
                return grants.get(i).cost();
            }
        }
        return null;
    }

    /** Whether a flashback grant on the card {@code cardId} lets it be cast without paying its mana cost. */
    public final boolean hasFlashbackGrantWithoutPayingManaCost(UUID cardId) {
        return cardEffects(cardId, GrantedCardAbilityEffect.class).stream().anyMatch(grant ->
                grant.ability() == GrantedCardAbilityEffect.Ability.FLASHBACK && grant.withoutPayingManaCost());
    }

    /** Whether a flashback grant on the card {@code cardId} was made while graveyard cards had lost all abilities. */
    public final boolean hasFlashbackGrantDespiteLostAbilities(UUID cardId) {
        return cardEffects(cardId, GrantedCardAbilityEffect.class).stream().anyMatch(grant ->
                grant.ability() == GrantedCardAbilityEffect.Ability.FLASHBACK && grant.despiteLostAbilities());
    }

    /** Whether the card {@code cardId} was granted {@code ability} for a while and still has it. */
    public final boolean hasGrantedCardAbility(UUID cardId, GrantedCardAbilityEffect.Ability ability) {
        return grantedCardAbility(cardId, ability).isPresent();
    }

    /**
     * Adds the effect a resolving spell or ability makes on the card {@code cardId} outside the battlefield (CR 611.2a):
     * "it gains flashback until end of turn", "you may play that card this turn". It ends as the card leaves its zone
     * (CR 400.7).
     */
    public FloatingContinuousEffect addCardEffect(StackEntry entry, CardEffect effect, UUID cardId, Zone zone,
                                                  Duration duration) {
        return addFloatingEffect(FloatingContinuousEffect.onCard(
                entry.getCard() == null ? null : entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), effect, cardId, zone, duration));
    }

    /** The newest "play this card from the graveyard" grant on the card {@code cardId}, if any. */
    public final Optional<GraveyardCardPlayGrant> graveyardCardPlayGrant(UUID cardId) {
        return newestCardEffect(cardId, GraveyardCardPlayGrant.class);
    }

    /** The player the newest play grant on the card {@code cardId} lets play it from the graveyard, or {@code null}. */
    public final UUID graveyardPlayPermittedPlayer(UUID cardId) {
        return graveyardCardPlayGrant(cardId).map(GraveyardCardPlayGrant::playerId).orElse(null);
    }

    /** Whether a play grant on the card {@code cardId} makes the permanent it becomes enter tapped. */
    public final boolean graveyardPlayEntersTapped(UUID cardId) {
        return cardEffects(cardId, GraveyardCardPlayGrant.class).stream().anyMatch(GraveyardCardPlayGrant::entersTapped);
    }

    /** Whether some card has a play grant for {@code playerId}. */
    public final boolean hasGraveyardCardPlayGrantFor(UUID playerId) {
        return floatingEffectsOfTypes(GraveyardCardPlayGrant.class).stream()
                .anyMatch(fe -> playerId.equals(((GraveyardCardPlayGrant) fe.effect()).playerId()));
    }

    /** The newest permission to cast the card {@code cardId} from a graveyard that {@code playerId} holds. */
    public final Optional<GraveyardCardCastPermission> graveyardCardCastPermission(UUID cardId, UUID playerId) {
        List<GraveyardCardCastPermission> grants = graveyardCardCastGrants(cardId);
        for (int i = grants.size() - 1; i >= 0; i--) {
            if (playerId.equals(grants.get(i).castingPlayerId())) {
                return Optional.of(grants.get(i));
            }
        }
        return Optional.empty();
    }

    /** Every permission to cast the card {@code cardId} from a graveyard, oldest first, each its own grant. */
    public final List<GraveyardCardCastPermission> graveyardCardCastGrants(UUID cardId) {
        return cardEffects(cardId, GraveyardCardCastPermission.class);
    }

    /** The permission to cast the card {@code cardId} as an Adventure from a graveyard, if it has one. */
    public final Optional<GraveyardAdventureCastPermission> graveyardAdventureCastPermission(UUID cardId) {
        return newestCardEffect(cardId, GraveyardAdventureCastPermission.class);
    }

    /** The turn-wide permissions to cast cards matching a filter from a graveyard, oldest first. */
    public final List<GraveyardCastFilterPermission> graveyardCastFilterPermissions() {
        return floatingEffectsOfTypes(GraveyardCastFilterPermission.class).stream()
                .map(fe -> (GraveyardCastFilterPermission) fe.effect()).toList();
    }

    /** Uses up the single-use permission {@code permission}: its effect ends. */
    public final void endGraveyardCastFilterPermission(GraveyardCastFilterPermission permission) {
        floatingEffectsOfTypes(GraveyardCastFilterPermission.class).stream()
                .filter(fe -> fe.effect().equals(permission)).findFirst().ifPresent(this::endFloatingEffect);
    }

    /** The turn-wide permissions to play cards matching a filter from graveyards, oldest first. */
    public final List<GraveyardPlayFilterPermission> graveyardPlayFilterPermissions() {
        return floatingEffectsOfTypes(GraveyardPlayFilterPermission.class).stream()
                .map(fe -> (GraveyardPlayFilterPermission) fe.effect()).toList();
    }

    /** The newest animation of the card {@code cardId} in a graveyard, if it has one. */
    public final Optional<GraveyardCardAnimation> graveyardCardAnimation(UUID cardId) {
        return newestCardEffect(cardId, GraveyardCardAnimation.class);
    }

    /**
     * Adds the rules-modifying effect a resolving spell or ability makes on the player {@code playerId} (CR 611.2a),
     * or on every player when it is {@code null}.
     */
    public FloatingContinuousEffect addPlayerEffect(StackEntry entry, CardEffect effect, UUID playerId,
                                                    Duration duration) {
        return addFloatingEffect(FloatingContinuousEffect.onPlayer(
                entry.getCard() == null ? null : entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), effect, playerId, duration));
    }

    /** The colors the spell {@code cardId} has become until end of turn, if some effect made it one. */
    public final Optional<Set<CardColor>> spellColorUntilEndOfTurn(UUID cardId) {
        return newestCardEffect(cardId, SpellColorUntilEndOfTurnEffect.class)
                .map(effect -> Set.of(effect.color()));
    }

    /** Ends what made the spell {@code cardId} a color until end of turn: it resolved, was countered or was cast again. */
    public final void endSpellColorUntilEndOfTurn(UUID cardId) {
        floatingEffectsOnCard(cardId, SpellColorUntilEndOfTurnEffect.class).forEach(this::endFloatingEffect);
    }

    /** Whether a floating effect of any of {@code types} names the permanent {@code permanentId}. */
    @SafeVarargs
    public final boolean hasFloatingEffectOnPermanent(UUID permanentId, Class<? extends CardEffect>... types) {
        return !floatingEffectsOnPermanent(permanentId, types).isEmpty();
    }

    /**
     * The floating effects of any of {@code types}, whatever they apply to, oldest first: for a rule that looks at
     * every player's effect of a kind (each Gather Specimens is its own replacement effect, CR 614.5).
     */
    @SafeVarargs
    public final List<FloatingContinuousEffect> floatingEffectsOfTypes(Class<? extends CardEffect>... types) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (isAnyOf(fe.effect(), types)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /**
     * The floating damage-prevention effects (CR 615) whose {@link PreventDamageEffect#scope()} is any of
     * {@code scopes}, oldest first, whoever and whatever they apply to.
     */
    public final List<FloatingContinuousEffect> preventionEffects(PreventionScope... scopes) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (hasPreventionScope(fe, scopes)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /** Whether a floating damage-prevention effect of any of {@code scopes} is in force, on whomever. */
    public final boolean hasPrevention(PreventionScope... scopes) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (hasPreventionScope(fe, scopes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The floating damage-prevention effects of any of {@code scopes} that apply to {@code playerId} (they name that
     * player, or every player), oldest first.
     */
    public final List<FloatingContinuousEffect> preventionEffectsOnPlayer(UUID playerId, PreventionScope... scopes) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.appliesToPlayer(playerId) && hasPreventionScope(fe, scopes)) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /** Whether a floating damage-prevention effect of any of {@code scopes} applies to {@code playerId}. */
    public final boolean hasPreventionOnPlayer(UUID playerId, PreventionScope... scopes) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.appliesToPlayer(playerId) && hasPreventionScope(fe, scopes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a floating damage-prevention effect of any of {@code scopes} whose combat-only flag is
     * {@code combatOnly} names the permanent {@code permanentId}.
     */
    public final boolean hasPreventionOnPermanent(UUID permanentId, boolean combatOnly, PreventionScope... scopes) {
        if (permanentId == null) {
            return false;
        }
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (permanentId.equals(fe.affectedPermanentId()) && hasPreventionScope(fe, scopes)
                        && ((PreventDamageEffect) fe.effect()).combatOnly() == combatOnly) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Whether a floating damage-prevention effect of any of {@code scopes} names the permanent {@code permanentId}. */
    public final boolean hasPreventionOnPermanent(UUID permanentId, PreventionScope... scopes) {
        if (permanentId == null) {
            return false;
        }
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (permanentId.equals(fe.affectedPermanentId()) && hasPreventionScope(fe, scopes)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasPreventionScope(FloatingContinuousEffect fe, PreventionScope[] scopes) {
        if (!(fe.effect() instanceof PreventDamageEffect prevention)) {
            return false;
        }
        for (PreventionScope scope : scopes) {
            if (prevention.scope() == scope) {
                return true;
            }
        }
        return false;
    }

    /**
     * The floating effects whose payload is a {@code type}, oldest first: the shields and redirections of one kind that
     * are in force (CR 615, CR 614). The list is a snapshot; end or reduce a shield through
     * {@link #endFloatingEffect} / {@link #reduceShield}.
     */
    public final <T extends CardEffect> List<FloatingContinuousEffect> shieldEffects(Class<T> type) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (type.isInstance(fe.effect())) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /** The payloads of {@link #shieldEffects}, oldest first. */
    public final <T extends CardEffect> List<T> shields(Class<T> type) {
        List<T> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (type.isInstance(fe.effect())) {
                    result.add(type.cast(fe.effect()));
                }
            }
        }
        return result;
    }

    /** Whether a shield whose payload is a {@code type} is in force. */
    public final boolean hasShields(Class<? extends CardEffect> type) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (type.isInstance(fe.effect())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Starts a shield as a floating effect that lasts "until end of turn" (CR 611.2a), on no player or permanent in
     * particular: the shield's own fields say whom it protects.
     */
    public final FloatingContinuousEffect addShield(CardEffect shield) {
        return addShield(shield, Duration.UNTIL_END_OF_TURN);
    }

    /** As {@link #addShield(CardEffect)} with the {@code duration} the card states. */
    public final FloatingContinuousEffect addShield(CardEffect shield, Duration duration) {
        return addFloatingEffect(FloatingContinuousEffect.onPlayer("", null, null, shield, null, duration));
    }

    /**
     * Starts a shield for the card {@code sourceCardName}, controlled by {@code controllerId}, lasting {@code duration},
     * affecting {@code playerId} ({@code null}: whom the shield's own fields name).
     */
    public final FloatingContinuousEffect addShield(String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                                    UUID playerId, CardEffect shield, Duration duration) {
        return addFloatingEffect(FloatingContinuousEffect.onPlayer(
                sourceCardName, sourcePermanentId, controllerId, shield, playerId, duration));
    }

    /** Ends the oldest shield that is this very payload object. Returns whether there was one. */
    public final boolean endShield(CardEffect shield) {
        FloatingContinuousEffect found = null;
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.effect() == shield) {
                    found = fe;
                    break;
                }
            }
        }
        if (found == null) {
            return false;
        }
        endFloatingEffect(found);
        return true;
    }

    /** Ends every shield of {@code type} that {@code which} matches. */
    public final <T extends CardEffect> void endShields(Class<T> type, Predicate<T> which) {
        for (FloatingContinuousEffect fe : shieldEffects(type)) {
            if (which.test(type.cast(fe.effect()))) {
                endFloatingEffect(fe);
            }
        }
    }

    /**
     * Uses part of a shield: puts {@code reduced} in place of the shield {@code used}, keeping its place in the list and
     * its timestamp.
     */
    public final void reduceShield(FloatingContinuousEffect used, CardEffect reduced) {
        replaceFloatingEffect(used, used.withEffect(reduced));
    }

    /**
     * Starts "prevent the next {@code amount} damage that would be dealt to {@code playerId}" (CR 615.7) for the card
     * {@code sourceCardName}, lasting until end of turn. {@code combatOnly}: it prevents combat damage only.
     */
    public final FloatingContinuousEffect addPlayerDamagePreventionShield(
            String sourceCardName, UUID controllerId, UUID playerId, int amount, boolean combatOnly) {
        return addShield(sourceCardName, null, controllerId, playerId,
                new PlayerDamagePreventionShield(amount, combatOnly), Duration.UNTIL_END_OF_TURN);
    }

    /** As the five-argument method, for a shield that prevents all damage and has no card behind it. */
    public final FloatingContinuousEffect addPlayerDamagePreventionShield(UUID playerId, int amount) {
        return addPlayerDamagePreventionShield("", null, playerId, amount, false);
    }

    /**
     * Starts "prevent the next {@code amount} damage that would be dealt to any target" (CR 615.7), the shield any
     * damage may use, until end of turn.
     */
    public final FloatingContinuousEffect addGlobalDamagePreventionShield(int amount) {
        return addPlayerDamagePreventionShield("", null, null, amount, false);
    }

    /** The damage the shared shield can still prevent, added up. */
    public final int globalDamagePreventionShield() {
        return playerDamagePreventionShield(null, false);
    }

    /** The damage the shields of {@code playerId} (none: the shared shield) of the given kind can still prevent. */
    public final int playerDamagePreventionShield(UUID playerId, boolean combatOnly) {
        int total = 0;
        for (FloatingContinuousEffect fe : shieldEffects(PlayerDamagePreventionShield.class)) {
            PlayerDamagePreventionShield shield = (PlayerDamagePreventionShield) fe.effect();
            if (shield.combatOnly() == combatOnly && Objects.equals(playerId, fe.affectedPlayerId())) {
                total += shield.amount();
            }
        }
        return total;
    }

    /**
     * Prevents up to {@code damage} of the damage that would be dealt to {@code playerId} (none: to anything) with that
     * player's shields of the given kind, oldest first; a used-up shield ends, a partly used one is replaced by what is left of it.
     * Returns the damage prevented.
     */
    public final int usePlayerDamagePreventionShields(UUID playerId, boolean combatOnly, int damage) {
        int remaining = damage;
        for (FloatingContinuousEffect fe : shieldEffects(PlayerDamagePreventionShield.class)) {
            if (remaining <= 0) {
                break;
            }
            PlayerDamagePreventionShield shield = (PlayerDamagePreventionShield) fe.effect();
            if (shield.combatOnly() != combatOnly || !Objects.equals(playerId, fe.affectedPlayerId())) {
                continue;
            }
            int prevented = Math.min(shield.amount(), remaining);
            remaining -= prevented;
            if (prevented >= shield.amount()) {
                endFloatingEffect(fe);
            } else {
                reduceShield(fe, shield.reducedBy(prevented));
            }
        }
        return damage - remaining;
    }

    /**
     * The "prevent the next N damage" shield of every player that has one, added up, as a map: what the card tests
     * assert on. A player with none is absent.
     */
    public final Map<UUID, Integer> playerDamagePreventionShields() {
        return playerDamagePreventionShieldTotals(false);
    }

    /** As {@link #playerDamagePreventionShields()} for the shields that prevent combat damage only. */
    public final Map<UUID, Integer> playerCombatDamagePreventionShields() {
        return playerDamagePreventionShieldTotals(true);
    }

    private Map<UUID, Integer> playerDamagePreventionShieldTotals(boolean combatOnly) {
        Map<UUID, Integer> totals = new LinkedHashMap<>();
        for (FloatingContinuousEffect fe : shieldEffects(PlayerDamagePreventionShield.class)) {
            PlayerDamagePreventionShield shield = (PlayerDamagePreventionShield) fe.effect();
            if (shield.combatOnly() == combatOnly && fe.affectedPlayerId() != null && shield.amount() > 0) {
                totals.merge(fe.affectedPlayerId(), shield.amount(), Integer::sum);
            }
        }
        return totals;
    }

    /**
     * Puts back a shield that ended while the damage event it belongs to was still being dealt, with the timestamp it
     * had, so it keeps its place among the shields that are in force. Nothing happens when it is in force.
     */
    public final void reinstateShield(FloatingContinuousEffect effect) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe == effect) {
                    return;
                }
            }
            int index = 0;
            while (index < floatingEffects.size() && floatingEffects.get(index).timestamp() <= effect.timestamp()) {
                index++;
            }
            floatingEffects.add(index, effect);
        }
        if (effect.id() != null && activePlayerId != null) {
            floatingEffectCreatingActivePlayerIds.putIfAbsent(effect.id(), activePlayerId);
        }
        observeFloatingEffect(effect, true);
    }

    /**
     * Replaces the payload of every shield of {@code type} {@code update} returns another payload for, keeping the
     * shield's place and timestamp.
     */
    public final <T extends CardEffect> void updateShields(Class<T> type, java.util.function.UnaryOperator<T> update) {
        for (FloatingContinuousEffect fe : shieldEffects(type)) {
            T shield = type.cast(fe.effect());
            T updated = update.apply(shield);
            if (updated != shield) {
                reduceShield(fe, updated);
            }
        }
    }

    /**
     * The shield that prevents combat damage to {@code playerId} and creates tokens for it (CR 615), or {@code null}.
     * One player has at most one: a second one resolving while it is in force changes nothing.
     */
    public final CombatDamagePreventionTokenShield combatDamagePreventionTokenShield(UUID playerId) {
        List<FloatingContinuousEffect> shields = floatingEffectsOnPlayer(playerId, CombatDamagePreventionTokenShield.class);
        return shields.isEmpty() ? null : (CombatDamagePreventionTokenShield) shields.getFirst().effect();
    }

    /** Whether an effect makes "damage can't be prevented this turn" (CR 615.1, CR 611.2a). */
    public final boolean damageCantBePreventedThisTurn() {
        return hasShields(com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect.class);
    }

    /** Remembers the card chosen as the damage source the shield stored as {@code sourceId} is waiting for. */
    public final void addWaitingDamageSource(UUID sourceId, Card card) {
        addShield(new WaitingDamageSource(sourceId, card));
    }

    /** The card chosen as the damage source the shield stored as {@code sourceId} is waiting for, or {@code null}. */
    public final Card waitingDamageSourceCard(UUID sourceId) {
        Card found = null;
        for (WaitingDamageSource waiting : shields(WaitingDamageSource.class)) {
            if (sourceId.equals(waiting.sourceId())) {
                found = waiting.card();
            }
        }
        return found;
    }

    /** The players a floating effect of {@code type} names, in the order the effects began. */
    public final Set<UUID> playersWithFloatingEffect(Class<? extends CardEffect> type) {
        Set<UUID> players = new LinkedHashSet<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.affectedPlayerId() != null && type.isInstance(fe.effect())) {
                    players.add(fe.affectedPlayerId());
                }
            }
        }
        return players;
    }

    private static boolean isAnyOf(CardEffect effect, Class<? extends CardEffect>[] types) {
        for (Class<? extends CardEffect> type : types) {
            if (type.isInstance(effect)) {
                return true;
            }
        }
        return false;
    }

    /** All active control-changing floating effects that apply to the given permanent. */
    public List<FloatingContinuousEffect> controlEffectsFor(UUID permanentId) {
        List<FloatingContinuousEffect> result = new ArrayList<>();
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (fe.isControlEffect() && permanentId.equals(fe.affectedPermanentId())) {
                    result.add(fe);
                }
            }
        }
        return result;
    }

    /** The newest (highest CR 613.7 timestamp) active control effect for the permanent, or {@code null}. */
    public FloatingContinuousEffect newestControlEffectFor(UUID permanentId) {
        FloatingContinuousEffect newest = null;
        for (FloatingContinuousEffect fe : controlEffectsFor(permanentId)) {
            if (newest == null || fe.timestamp() > newest.timestamp()) {
                newest = fe;
            }
        }
        return newest;
    }

    /**
     * The player a control effect gives control to. For attachment-backed effects
     * ({@code WHILE_ATTACHED}, e.g. In Bolas's Clutches) this is the CURRENT controller of the
     * source Aura — the static ability grants control to whoever controls the Aura right now;
     * for everything else it is the controller of the spell/ability that created the effect.
     */
    public UUID resolveControlEffectController(FloatingContinuousEffect fe) {
        if (fe.effect() instanceof ControlEnchantedCreatureEffect control
                && control.controlledByMonarch()) {
            return monarchPlayerId != null ? monarchPlayerId : defaultControllerOf(fe.affectedPermanentId());
        }
        if (Duration.WHILE_ATTACHED.equals(fe.duration()) && fe.sourcePermanentId() != null) {
            UUID auraController = findControllerOf(fe.sourcePermanentId());
            if (auraController != null) {
                return auraController;
            }
        }
        return fe.controllerId();
    }

    /**
     * The card's current owner: an in-game ownership change (see {@link #cardOwnerOverrides}) wins
     * over the owner stamped on the card at game setup. May be null for hand-built test cards.
     */
    public UUID ownerOf(Card card) {
        UUID overridden = cardOwnerOverrides.get(card.getId());
        return overridden != null ? overridden : card.getOwnerId();
    }

    /**
     * The player who controls the given permanent per CR 613.2: the newest active control
     * effect wins; with none active, the default controller (see {@link #defaultControllerOf}).
     * Purely derived — moving the permanent between battlefield lists to match is
     * {@code CreatureControlService.recomputeControl}'s job.
     */
    public UUID deriveControllerOf(UUID permanentId) {
        FloatingContinuousEffect newest = newestControlEffectFor(permanentId);
        if (newest != null) {
            return resolveControlEffectController(newest);
        }
        return defaultControllerOf(permanentId);
    }

    /**
     * Who controls the permanent when no control effect applies: its recorded original owner
     * ({@link #stolenCreatures}), else the owner stamped on the card at game setup, else the
     * battlefield it currently sits on (hand-built test permanents and tokens carry no owner).
     */
    public UUID defaultControllerOf(UUID permanentId) {
        UUID recordedOwner = stolenCreatures.get(permanentId);
        if (recordedOwner != null) {
            return recordedOwner;
        }
        Permanent permanent = null;
        UUID holder = null;
        for (UUID playerId : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    permanent = p;
                    holder = playerId;
                    break;
                }
            }
            if (permanent != null) break;
        }
        if (permanent != null) {
            UUID cardOwner = ownerOf(permanent.getCard());
            if (cardOwner != null && playerIds.contains(cardOwner)) {
                return cardOwner;
            }
        }
        return holder;
    }

    /**
     * Whether the permanent is currently held by a controller who only has it until end of
     * turn: the newest active control effect is {@code UNTIL_END_OF_TURN} and control would
     * fall to a different player once it expires. Derived replacement for the old
     * {@code untilEndOfTurnStolenCreatures} set (used by the AI to zero a stolen attacker's
     * loss value).
     */
    public boolean isStolenUntilEndOfTurn(UUID permanentId) {
        FloatingContinuousEffect newest = newestControlEffectFor(permanentId);
        if (newest == null || !Duration.UNTIL_END_OF_TURN.equals(newest.duration())) {
            return false;
        }
        UUID withEffect = resolveControlEffectController(newest);
        FloatingContinuousEffect newestSurviving = null;
        for (FloatingContinuousEffect fe : controlEffectsFor(permanentId)) {
            if (Duration.UNTIL_END_OF_TURN.equals(fe.duration())) continue;
            if (newestSurviving == null || fe.timestamp() > newestSurviving.timestamp()) {
                newestSurviving = fe;
            }
        }
        UUID withoutEffect = newestSurviving != null
                ? resolveControlEffectController(newestSurviving)
                : defaultControllerOf(permanentId);
        return withEffect != null && !withEffect.equals(withoutEffect);
    }

    /** The player whose battlefield currently holds the permanent, or {@code null}. */
    public UUID findControllerOf(Permanent permanent) {
        return findControllerOf(permanent.getId());
    }

    /**
     * The player whose battlefield currently holds the permanent with the given ID, or {@code null}.
     *
     * @deprecated Use {@link #findControllerOf(Permanent)} so the lookup is scoped to a permanent.
     */
    @Deprecated(forRemoval = true)
    public UUID findControllerOf(UUID permanentId) {
        for (UUID playerId : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    return playerId;
                }
            }
        }
        return null;
    }

    public GameData(UUID id, String gameName, UUID createdByUserId, String createdByUsername) {
        this.id = id;
        this.gameName = gameName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.createdAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
    }

    /** Updates a player's energy total and records any counters removed by the update. */
    public void setPlayerEnergyCounters(UUID playerId, int amount) {
        int updated = Math.max(0, amount);
        int current = playerEnergyCounters.getOrDefault(playerId, 0);
        if (updated < current) {
            energyCountersPaidOrLostThisTurn.merge(playerId, current - updated, Integer::sum);
        }
        playerEnergyCounters.put(playerId, updated);
    }

    /** Removes up to {@code amount} energy counters, recording the counters that were lost. */
    public int removePlayerEnergyCounters(UUID playerId, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int current = playerEnergyCounters.getOrDefault(playerId, 0);
        int removed = Math.min(current, amount);
        if (removed > 0) {
            setPlayerEnergyCounters(playerId, current - removed);
        }
        return removed;
    }

    /**
     * Routes a triggered-ability {@link StackEntry} to the main stack, or to
     * {@link #pendingManaAbilityTriggers} when a mana ability is currently resolving
     * (CR 603.2 / 603.3). Deferred triggers are flushed by the existing flush points
     * (priority grant, auto-pass, {@code SpellCastingService.finishSpellCast}). Only the stack door reaches it
     * ({@link StackDoor#enqueue}).
     */
    void enqueueTrigger(StackEntry entry) {
        refuseNonStackObject(entry);
        giveTriggerRecordInHand(entry);
        if (waitingForSubgame && session.active() != this) {
            observeTrigger(RulesObserver.Route.SUSPENDED_QUEUE, entry);
            suspendedZoneTriggers.add(entry);
            for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
                suspendedZoneTriggers.add(entry.copyForNewStackObject());
            }
            return;
        }
        if (manaAbilityResolutionDepth > 0) {
            observeTrigger(RulesObserver.Route.MANA_ABILITY_QUEUE, entry);
            pendingManaAbilityTriggers.add(entry);
            for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
                pendingManaAbilityTriggers.add(entry.copyForNewStackObject());
            }
        } else {
            stack.add(entry);
        }
    }

    /**
     * Begins a trigger-collection scope in which newly queued triggered abilities are multiplied.
     * Returns the previous scope value for restoration by the caller.
     */
    public int beginTriggeredAbilityCopies(int copies) {
        int previous = activeTriggeredAbilityCopies;
        activeTriggeredAbilityCopies = Math.max(1, copies);
        return previous;
    }

    public void restoreTriggeredAbilityCopies(int previous) {
        activeTriggeredAbilityCopies = previous;
    }

    /**
     * Refuses {@code entry} as a stack object: a carrier stands for an object in a resolution and is never one
     * ({@link StackEntry#carrier}), and a triggered ability's entry reaches the stack only as the stack door releases it
     * ({@link StackDoor#enqueue}); a copy of a stack object keeps what it copies.
     */
    private static void refuseNonStackObject(StackEntry entry) {
        if (entry.isCarrier()) {
            throw new IllegalStateException("A carrier is never a stack object: " + entry.getDescription());
        }
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY && !entry.isReleasedToStack()) {
            throw new IllegalStateException("A triggered ability reaches the stack through the stack door: "
                    + entry.getDescription());
        }
    }

    private void recordGraveyardTargetVersions(StackEntry entry) {
        if (entry.getTargetCardIds() == null) {
            return;
        }
        for (UUID cardId : entry.getTargetCardIds()) {
            entry.recordTargetCardGraveyardVersion(cardId, graveyardEntryVersion(cardId));
        }
    }

    /**
     * Gives a spell entering the stack its CR 613.7d timestamp. An entry that already carries one
     * (a simulation copy of a spell already on the stack) keeps it; abilities are not stamped.
     */
    private void stampSpellEnteringStack(StackEntry entry) {
        if (entry.getStackTimestamp() != 0
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return;
        }
        entry.setStackTimestamp(nextTimestamp());
    }

    private int activeTriggeredAbilityCopies() {
        return activeTriggeredAbilityCopies;
    }

    /**
     * A spell card moving to the stack is a zone change (CR 601.2a), reported to {@link #rulesEvents}; a copy of a
     * spell is no card. A triggered ability reaching the stack is reported to {@link #rulesObserver}.
     */
    private void reportStackArrival(StackEntry entry) {
        StackEntryType type = entry.getEntryType();
        if (type == StackEntryType.TRIGGERED_ABILITY) {
            observeTrigger(RulesObserver.Route.STACK, entry);
        } else if (type != StackEntryType.ACTIVATED_ABILITY && !entry.isCopy() && entry.getCard() != null) {
            Card card = entry.getCard();
            ZoneChangeCause previous = rulesEvents.setCause(card.getId(), ZoneChangeCause.CAST);
            rulesEvents.arrived(card, ownerOf(card), Zone.STACK);
            rulesEvents.restoreCause(card.getId(), previous);
        }
    }

    private static final class TriggerAwareStackList extends ArrayList<StackEntry> {

        private final GameData gameData;

        private TriggerAwareStackList(GameData gameData) {
            this.gameData = gameData;
        }

        @Override
        public boolean add(StackEntry entry) {
            refuseNonStackObject(entry);
            gameData.stampSpellEnteringStack(entry);
            gameData.giveTriggerRecordInHand(entry);
            boolean added = super.add(entry);
            if (added) {
                gameData.recordStackEntryForCrime(entry);
                gameData.recordGraveyardTargetVersions(entry);
                gameData.reportStackArrival(entry);
            }
            if (added && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                // Each additional time the ability triggers puts another object on the stack (CR 603.3), with an
                // identity of its own: its controller orders them (CR 603.3b).
                for (int i = 1; i < gameData.activeTriggeredAbilityCopies(); i++) {
                    StackEntry copy = entry.copyForNewStackObject();
                    super.add(copy);
                    gameData.recordStackEntryForCrime(copy);
                    gameData.observeTrigger(RulesObserver.Route.STACK_ADDITIONAL, copy);
                }
            }
            if (added) {
                gameData.recordCurrentStackSourceCount();
            }
            return added;
        }

        @Override
        public void add(int index, StackEntry entry) {
            refuseNonStackObject(entry);
            gameData.stampSpellEnteringStack(entry);
            gameData.giveTriggerRecordInHand(entry);
            super.add(index, entry);
            gameData.recordStackEntryForCrime(entry);
            gameData.recordGraveyardTargetVersions(entry);
            gameData.reportStackArrival(entry);
            gameData.recordCurrentStackSourceCount();
        }

        @Override
        public boolean addAll(java.util.Collection<? extends StackEntry> entries) {
            entries.forEach(GameData::refuseNonStackObject);
            entries.forEach(gameData::stampSpellEnteringStack);
            entries.forEach(gameData::giveTriggerRecordInHand);
            boolean added = super.addAll(entries);
            if (added) {
                entries.forEach(gameData::recordStackEntryForCrime);
                entries.forEach(gameData::recordGraveyardTargetVersions);
                entries.forEach(gameData::reportStackArrival);
                gameData.recordCurrentStackSourceCount();
            }
            return added;
        }

        @Override
        public boolean addAll(int index, java.util.Collection<? extends StackEntry> entries) {
            entries.forEach(GameData::refuseNonStackObject);
            entries.forEach(gameData::stampSpellEnteringStack);
            entries.forEach(gameData::giveTriggerRecordInHand);
            boolean added = super.addAll(index, entries);
            if (added) {
                entries.forEach(gameData::recordStackEntryForCrime);
                entries.forEach(gameData::recordGraveyardTargetVersions);
                entries.forEach(gameData::reportStackArrival);
                gameData.recordCurrentStackSourceCount();
            }
            return added;
        }
    }

    public int getGreatestStackSourceCountThisTurn() {
        return greatestStackSourceCountThisTurn;
    }

    public void clearGreatestStackSourceCountThisTurn() {
        greatestStackSourceCountThisTurn = 0;
    }

    private void recordCurrentStackSourceCount() {
        Set<UUID> spellSourceIds = new HashSet<>();
        Set<UUID> abilitySourceIds = new HashSet<>();
        synchronized (stack) {
            for (StackEntry entry : stack) {
                UUID targetableId = entry.getTargetableId();
                boolean ability = entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                        || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
                if (ability) {
                    // Abilities from one source are one source; a spell is a separate source from them.
                    boolean cardSourced = entry.getCard() != null && entry.getSourcePlanarObject() == null;
                    abilitySourceIds.add(cardSourced ? entry.getCard().getId() : targetableId);
                } else if (targetableId != null) {
                    spellSourceIds.add(targetableId);
                }
            }
        }
        greatestStackSourceCountThisTurn = Math.max(greatestStackSourceCountThisTurn,
                spellSourceIds.size() + abilitySourceIds.size());
    }

    /**
     * Appends a pending interaction to the tail of the unified queue.
     */
    public void queueInteraction(PendingInteraction interaction) {
        observeTrigger(RulesObserver.Route.INTERACTION, interaction);
        keepTriggerRecordWith(interaction);
        pendingInteractions.addLast(interaction);
        for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
            pendingInteractions.addLast(interaction);
        }
    }

    /**
     * Puts a pending interaction at the head of the unified queue. Used when an
     * in-progress multi-step interaction must be serviced before anything else
     * (e.g. re-queuing an updated {@code ETBTokenMultiTargetTrigger} between target slots).
     */
    public void queueInteractionFirst(PendingInteraction interaction) {
        observeTrigger(RulesObserver.Route.INTERACTION, interaction);
        keepTriggerRecordWith(interaction);
        pendingInteractions.addFirst(interaction);
        for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
            pendingInteractions.addFirst(interaction);
        }
    }

    /**
     * Returns {@code true} if the queue holds at least one interaction of the given kind.
     */
    public boolean hasPendingInteraction(Class<? extends PendingInteraction> type) {
        for (PendingInteraction interaction : pendingInteractions) {
            if (type.isInstance(interaction)) return true;
        }
        return false;
    }

    /**
     * Returns the first queued interaction of the given kind without removing it,
     * or {@code null} if none is queued.
     */
    public <T extends PendingInteraction> T peekPendingInteraction(Class<T> type) {
        for (PendingInteraction interaction : pendingInteractions) {
            if (type.isInstance(interaction)) return type.cast(interaction);
        }
        return null;
    }

    /**
     * Removes and returns the first queued interaction of the given kind,
     * or {@code null} if none is queued.
     */
    public <T extends PendingInteraction> T pollPendingInteraction(Class<T> type) {
        var it = pendingInteractions.iterator();
        while (it.hasNext()) {
            PendingInteraction interaction = it.next();
            if (type.isInstance(interaction)) {
                it.remove();
                takeQueuedTriggerRecord(interaction);
                return type.cast(interaction);
            }
        }
        return null;
    }

    /**
     * Hands off {@code record}, the entry a collector builds for an ability that chooses nothing, for the ability it
     * is routing: the choice the ability is queued or asked with, or the entry that reaches the stack for it, takes
     * it (see {@link #triggerRecordInHand}). Putting {@code record} itself on the stack takes it too.
     * Returns the record that was in hand.
     */
    StackEntry handOffTriggerRecord(StackEntry record) {
        StackEntry previous = triggerRecordInHand;
        triggerRecordInHand = record;
        if (record != null) {
            triggerRecordHandedOff = true;
        }
        return previous;
    }

    /**
     * Marks a collector as routing one ability, with nothing in hand, and returns the state to give
     * {@link #endTriggerRouting}. While it routes, a choice queued or asked with no record handed off is reported to
     * {@link #triggerRecordMissing}.
     */
    public TriggerRouting beginTriggerRouting() {
        TriggerRouting previous = new TriggerRouting(triggerRecordInHand, triggerRecordHandedOff);
        triggerRecordInHand = null;
        triggerRecordHandedOff = false;
        triggerRoutingDepth++;
        return previous;
    }

    public void endTriggerRouting(TriggerRouting previous) {
        reportTriggerRecordLeftBehind();
        triggerRoutingDepth--;
        triggerRecordInHand = previous.recordInHand();
        triggerRecordHandedOff = previous.handedOff();
    }

    /**
     * Runs {@code routing}, a collector putting one triggered ability toward the stack, as one routing
     * ({@link #beginTriggerRouting}).
     */
    public void routeTriggeredAbility(Runnable routing) {
        TriggerRouting previous = beginTriggerRouting();
        try {
            routing.run();
        } finally {
            endTriggerRouting(previous);
        }
    }

    /** {@link #routeTriggeredAbility(Runnable)} for a collector that returns a result. */
    public <T> T routeTriggeredAbility(Supplier<T> routing) {
        TriggerRouting previous = beginTriggerRouting();
        try {
            return routing.get();
        } finally {
            endTriggerRouting(previous);
        }
    }

    /**
     * Drops the trigger record in hand: nothing is being routed any more (a stack object starts to resolve, or a
     * game action is over), so a record a route left behind (an ability removed for want of a legal target,
     * CR 603.3d) can't reach another ability.
     */
    public void dropTriggerRecordInHand() {
        reportTriggerRecordLeftBehind();
        triggerRecordInHand = null;
    }

    /**
     * Every stack object, and the record of every triggered ability that has triggered and is not on the stack yet
     * (the one in hand, those kept with a queued choice or with a prompt). What a permanent leaving the battlefield
     * stamps on the stack entries that name it (how it last existed, CR 608.2h) is stamped on each of them, so an
     * ability whose target is chosen after the permanent left records the event as the one that chose nothing.
     */
    public List<StackEntry> stackEntriesAndTriggerRecords() {
        List<StackEntry> entries;
        synchronized (stack) {
            entries = new ArrayList<>(stack);
        }
        Set<StackEntry> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        seen.addAll(entries);
        List<StackEntry> records = new ArrayList<>();
        records.add(triggerRecordInHand);
        records.addAll(queuedTriggerRecords.values());
        records.addAll(interaction.triggerRecords());
        for (StackEntry record : records) {
            if (record != null && seen.add(record)) {
                entries.add(record);
            }
        }
        return entries;
    }

    /** What was in hand before a collector began routing an ability ({@link #beginTriggerRouting}). */
    public record TriggerRouting(StackEntry recordInHand, boolean handedOff) {
    }

    /**
     * The record kept with {@code interaction}, a queued choice of a triggered ability going on the stack: the entry
     * its collector built for the ability that chooses nothing, which the choice reads the event from (the damage
     * dealt, the triggering spell or permanent a target is restricted by); {@code null} when it was queued with none.
     */
    public StackEntry queuedTriggerRecord(PendingInteraction interaction) {
        return queuedTriggerRecords.get(interaction);
    }

    /** Takes the record in hand, leaving none. */
    StackEntry takeTriggerRecordInHand() {
        StackEntry record = triggerRecordInHand;
        triggerRecordInHand = null;
        return record;
    }

    private void reportTriggerRecordLeftBehind() {
        if (triggerRecordInHand != null && triggerRecordLeftBehind != null) {
            triggerRecordLeftBehind.accept(triggerRecordInHand);
        }
    }

    private void reportInteractionWithoutTriggerRecord(PendingInteraction interaction) {
        if (interactionWithoutTriggerRecord != null) {
            interactionWithoutTriggerRecord.accept(interaction, triggerRoutingDepth > 0 && !triggerRecordHandedOff);
        }
    }

    private void keepTriggerRecordWith(PendingInteraction interaction) {
        StackEntry record = takeTriggerRecordInHand();
        if (record != null) {
            queuedTriggerRecords.put(interaction, record);
        } else {
            reportInteractionWithoutTriggerRecord(interaction);
        }
    }

    private void takeQueuedTriggerRecord(PendingInteraction interaction) {
        boolean queuedAgain = false;
        for (PendingInteraction queued : pendingInteractions) {
            if (queued == interaction) {
                queuedAgain = true;
                break;
            }
        }
        reportTriggerRecordLeftBehind();
        triggerRecordInHand = queuedAgain
                ? queuedTriggerRecords.get(interaction) : queuedTriggerRecords.remove(interaction);
    }

    private static boolean sameSourceCard(StackEntry record, StackEntry entry) {
        Card recordCard = record.getCard();
        Card entryCard = entry.getCard();
        return recordCard == entryCard || recordCard != null && entryCard != null
                && recordCard.getId() != null && recordCard.getId().equals(entryCard.getId());
    }

    /**
     * Gives {@code entry}, a triggered ability arriving on the stack or in a holding queue, the record in hand when
     * it is that ability's (the same source card): the first such entry takes it. The record itself arriving is the
     * ability that chose nothing.
     */
    private void giveTriggerRecordInHand(StackEntry entry) {
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY || entry.isTriggerRecordSettled()) {
            return;
        }
        StackEntry record = triggerRecordInHand;
        if (record == entry) {
            triggerRecordInHand = null;
        } else if (record != null && sameSourceCard(record, entry)) {
            triggerRecordInHand = null;
            entry.carryTriggerRecord(record);
        }
        entry.settleTriggerRecord();
    }

    /**
     * Removes every queued interaction of the given kind (e.g. Karn restart wiping trigger state).
     */
    public void clearPendingInteractions(Class<? extends PendingInteraction> type) {
        queuedTriggerRecords.keySet().removeIf(type::isInstance);
        pendingInteractions.removeIf(type::isInstance);
    }

    /** Marks a land as already cleaned by the corresponding Cyclopean Tomb delayed trigger. */
    public void markCyclopeanTombLandRemoved(UUID actionId, UUID landId) {
        changeCyclopeanTombCleanups(delayed -> true, effect -> actionId.equals(effect.delayedActionId()),
                effect -> effect.withLandRemoved(landId));
    }

    /** Tracks lands a mire counter was put onto with the Tomb {@code tombPermanentId} after it left the battlefield. */
    public void trackCyclopeanTombLands(UUID tombPermanentId, java.util.Collection<UUID> landIds) {
        changeCyclopeanTombCleanups(delayed -> tombPermanentId.equals(delayed.sourcePermanentId()),
                effect -> true, effect -> effect.withLandsTracked(Set.copyOf(landIds)));
    }

    private void changeCyclopeanTombCleanups(
            Predicate<TemporaryGlobalTriggeredAbility> which,
            Predicate<RemoveAllMireCountersFromChosenLandEffect> ofEffect,
            java.util.function.UnaryOperator<RemoveAllMireCountersFromChosenLandEffect> change) {
        synchronized (temporaryGlobalTriggeredAbilities) {
            for (int i = 0; i < temporaryGlobalTriggeredAbilities.size(); i++) {
                TemporaryGlobalTriggeredAbility delayed = temporaryGlobalTriggeredAbilities.get(i);
                if (delayed.ability().effects().getFirst().effect() instanceof RemoveAllMireCountersFromChosenLandEffect
                        cleanup && which.test(delayed) && ofEffect.test(cleanup)) {
                    temporaryGlobalTriggeredAbilities.set(i, delayed.withAbility(
                            change.apply(cleanup).atEachOfYourUpkeeps()));
                }
            }
        }
    }

    /**
     * The delayed triggered abilities waiting for the beginning of the step or phase {@code moment} ("at the beginning
     * of the next end step"), in the order they were made.
     */
    public List<TemporaryGlobalTriggeredAbility> delayedStepAbilities(StepTrigger.Moment moment) {
        return synchronizedCopy(temporaryGlobalTriggeredAbilities).stream()
                .filter(delayed -> delayed.ability().conditions().stream().anyMatch(condition ->
                        condition.trigger() instanceof StepTrigger step && step.moment() == moment))
                .toList();
    }

    /**
     * Starts the prevention shield of a Sacred Boon on {@code permanentId}: it prevents the next {@code amount} damage
     * this turn (CR 615.7), and a tally counts what it prevents for the delayed triggered ability that puts the counters.
     * The tally begins only when {@code counting}, which is when that ability exists.
     *
     * @return the id the shield and the tally are read by
     */
    public UUID registerSacredBoonShield(String sourceCardName, UUID controllerId, UUID permanentId, int amount,
                                         boolean counting) {
        UUID shieldId = UUID.randomUUID();
        addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, null, controllerId,
                new PreventedDamageCounterShield(shieldId, amount), permanentId, Duration.UNTIL_END_OF_TURN));
        if (counting) {
            addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, null, controllerId,
                    new PreventedDamageCounterTally(shieldId, 0), permanentId, Duration.PERMANENT));
        }
        return shieldId;
    }

    /**
     * Prevents up to {@code damage} of the damage that would be dealt to {@code permanentId} with its Sacred Boon
     * shields, oldest first, and counts it. Returns the damage prevented.
     */
    public int consumeSacredBoonShields(UUID permanentId, int damage) {
        int remainingDamage = damage;
        for (FloatingContinuousEffect fe : shieldEffects(PreventedDamageCounterShield.class)) {
            if (remainingDamage <= 0) {
                break;
            }
            PreventedDamageCounterShield shield = (PreventedDamageCounterShield) fe.effect();
            if (!permanentId.equals(fe.affectedPermanentId()) || shield.remaining() <= 0) {
                continue;
            }
            int prevented = Math.min(shield.remaining(), remainingDamage);
            remainingDamage -= prevented;
            if (prevented >= shield.remaining()) {
                endFloatingEffect(fe);
            } else {
                reduceShield(fe, shield.reducedBy(prevented));
            }
            for (FloatingContinuousEffect tallyEffect : shieldEffects(PreventedDamageCounterTally.class)) {
                PreventedDamageCounterTally tally = (PreventedDamageCounterTally) tallyEffect.effect();
                if (tally.shieldId().equals(shield.shieldId())) {
                    reduceShield(tallyEffect, new PreventedDamageCounterTally(
                            tally.shieldId(), tally.prevented() + prevented));
                }
            }
        }
        return damage - remainingDamage;
    }

    /**
     * Ends the tally of the Sacred Boon shield {@code shieldId} as its delayed triggered ability resolves, and returns
     * the damage it counted ({@code 0} when there is none).
     */
    public int takeSacredBoonTally(UUID shieldId) {
        for (FloatingContinuousEffect fe : shieldEffects(PreventedDamageCounterTally.class)) {
            PreventedDamageCounterTally tally = (PreventedDamageCounterTally) fe.effect();
            if (tally.shieldId().equals(shieldId)) {
                endFloatingEffect(fe);
                return tally.prevented();
            }
        }
        return 0;
    }

    /**
     * Records a spell cast by the given player this turn.
     */
    public void recordSpellCast(UUID playerId, Card card) {
        // Per-spell entry grants belong to this cast object. Clear a stale grant when a countered
        // card is cast again before end-of-turn cleanup, then let current grants be recorded below.
        spellAdditionalEnterCounters.remove(card.getId());
        spellAdditionalEnterCounterAmounts.remove(card.getId());
        spellsCastThisTurn.computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>())).add(card);
        if (spellTargetsCreaturePermanent(card, playerId)) {
            spellsCastTargetingCreatureThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
        }
        if (stack.stream().anyMatch(entry -> entry.getCard() != null
                && entry.getCard().getId().equals(card.getId())
                && entry.wasKicked())) {
            kickedSpellsCastThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
        }
        recordPlayerActionDuringOwnTurn(playerId);
        spellCastOrderThisTurn.add(card.getId());
        if (card.hasType(CardType.ARTIFACT)
                || perpetualCardTypes.getOrDefault(card.getId(), Set.of()).contains(CardType.ARTIFACT)
                || grantedArtifactSpellType(playerId, card)) {
            artifactSpellsCastThisTurn.add(card.getId());
        }
        int xSymbols = card.getManaCost() == null ? 0 : card.getManaCost().split("\\{X\\}", -1).length - 1;
        if (xSymbols > 0) {
            stack.stream()
                    .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(card.getId()))
                    .findFirst()
                    .ifPresent(entry -> spellCastStackManaValuesThisTurn.put(card.getId(),
                            card.getManaValue() + xSymbols * Math.max(0, entry.getXValue())));
        }
        if (spellCastUsedTreasureMana(card.getId())) {
            spellsCastUsingTreasureManaThisTurn.add(card.getId());
        }
        mostRecentSpellCastThisTurn = card;
        int manaSpent = getSpellCastManaSpent(card.getId());
        if (manaSpent > 0) {
            manaSpentToCastSpellsThisTurn.merge(playerId, manaSpent, Integer::sum);
        }
        spellNameCastCountsThisGame.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .merge(card.getName(), 1, Integer::sum);
        if (stack.stream().anyMatch(entry -> entry.getCard() != null
                && entry.getCard().getId().equals(card.getId())
                && playerId.equals(entry.getControllerId())
                && entry.getSourceZone() == Zone.COMMAND)) {
            recordCommanderCastFromCommandZone(playerId);
        }
        consumePendingAnyManaTypePermission(playerId, card);
        if (hasNextSpellConvokeGrant(playerId)) {
            spellsCastWithConvokeGrantThisTurn.add(card.getId());
        }
        applyNextSpellRiders(playerId, card);
    }

    /** Records a card cycled by the given player during this game. */
    /**
     * Opens a discard event of {@code playerId}: the cards they discard until {@link #finishDiscardEvent} are one
     * occurrence of "one or more cards" discarded at once ({@link LibraryEvent.DiscardedCards}).
     */
    public void beginDiscardEvent(UUID playerId) {
        if (discardEventPlayerId == null) {
            discardEventPlayerId = playerId;
            discardEventCardCount = 0;
            discardEventCards.clear();
        } else if (!discardEventPlayerId.equals(playerId)) {
            throw new IllegalStateException("A discard event is already in progress for another player");
        }
    }

    /**
     * Closes the discard event {@link #beginDiscardEvent} opened and emits it as one occurrence
     * ({@link LibraryEvent.DiscardedCards}) when a card was discarded in it.
     */
    public void finishDiscardEvent() {
        UUID playerId = discardEventPlayerId;
        int count = discardEventCardCount;
        List<Card> cards = List.copyOf(discardEventCards);
        discardEventPlayerId = null;
        discardEventCardCount = 0;
        discardEventCards.clear();
        if (playerId != null && count > 0) {
            rulesEvents.occur(new LibraryEvent.DiscardedCards(playerId, cards));
        }
    }

    /** {@link #discarded(UUID, Card, boolean)} of a discard that isn't a cycling. */
    public void discarded(UUID playerId, Card card) {
        discarded(playerId, card, false);
    }

    /**
     * Records that {@code playerId} discarded {@code card} (CR 701.9a), {@code cycled} when to pay a cycling cost
     * (CR 702.29a), in this turn's discard counts and the last-discarded card's values, and emits the discard as an
     * occurrence of its own ({@link LibraryEvent.Discarded}). Every discard path calls this after moving the card.
     */
    public void discarded(UUID playerId, Card card, boolean cycled) {
        if (cycled) {
            recordCardCycled(playerId, card);
        }
        cardsDiscardedThisTurn.merge(playerId, 1, Integer::sum);
        if (card != null && !card.hasType(CardType.LAND)) {
            nonlandCardsDiscardedThisTurn.merge(playerId, 1, Integer::sum);
        }
        if (card != null) {
            lastDiscardedCardManaValue = card.getManaValue();
            greatestDiscardedCardManaValue = Math.max(greatestDiscardedCardManaValue, card.getManaValue());
            EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
            if (card.getType() != null) {
                cardTypes.add(card.getType());
            }
            cardTypes.addAll(card.getAdditionalTypes());
            lastDiscardedCardTypes = Set.copyOf(cardTypes);
        } else {
            lastDiscardedCardTypes = Set.of();
        }
        if (card != null && !card.isToken()) {
            cardsDiscardedOrCycledThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
            if (discardCausedByOpponent) {
                cardsDiscardedByOpponentThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(card.getId());
            }
        }
        if (card != null && discardEventPlayerId != null) {
            discardEventCardCount++;
            discardEventCards.add(card);
        }
        rulesEvents.occur(new LibraryEvent.Discarded(playerId, card, cycled, discardCausedByOpponent,
                discardEventPlayerId == null));
    }

    /**
     * Records that {@code playerId} surveilled (CR 701.25a) and emits it as an occurrence of its own
     * ({@link LibraryEvent.Surveilled}), the first of the turn when they hadn't surveilled yet.
     */
    public void surveilled(UUID playerId) {
        rulesEvents.occur(new LibraryEvent.Surveilled(playerId, playersWhoSurveilledThisTurn.add(playerId)));
    }

    public void recordCardCycled(UUID playerId, Card card) {
        if (card == null) return;
        cardsCycledThisTurn.merge(playerId, 1, Integer::sum);
        if (card.getName() != null) {
            cardNameCycleCountsThisGame.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                    .merge(card.getName(), 1, Integer::sum);
        }
    }

    /** Records a spell cast and whether it was cast for an alternate Warp cost. */
    private boolean grantedArtifactSpellType(UUID playerId, Card card) {
        if (card.getType() == null || !card.getType().isPermanentType() || card.hasType(CardType.LAND)) {
            return false;
        }
        for (Permanent source : playerBattlefields.getOrDefault(playerId, List.of())) {
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof GrantCardTypeToOwnNonlandPermanentsEffect grant
                        && grant.cardType() == CardType.ARTIFACT) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Whether the spell with this card id was an artifact spell when it was cast this turn. */
    public boolean wasArtifactSpellWhenCastThisTurn(UUID cardId) {
        return artifactSpellsCastThisTurn.contains(cardId);
    }

    /**
     * Notes that a spell the player just cast targets a permanent that is currently a creature, for
     * creature types the card alone cannot show (an animated land).
     */
    public void recordSpellCastTargetingCreature(UUID playerId, UUID cardId) {
        spellsCastTargetingCreatureThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(cardId);
    }

    public void recordSpellCast(UUID playerId, Card card, boolean castWithWarp) {
        recordSpellCast(playerId, card);
        if (castWithWarp) {
            spellWarpedThisTurn = true;
        }
    }

    public void recordNontokenPermanentPutOntoBattlefield(UUID playerId, Card card) {
        if (card != null && !card.isToken()) {
            recordPlayerActionDuringOwnTurn(playerId);
        }
    }

    private void recordPlayerActionDuringOwnTurn(UUID playerId) {
        if (playerId != null && playerId.equals(activePlayerId)) {
            playersWhoActedDuringTheirTurn.add(playerId);
        }
    }

    public void snapshotPlayerActionsForLastTurn(UUID playerId) {
        if (playerId == null) return;
        if (playersWhoActedDuringTheirTurn.remove(playerId)) {
            playersWhoActedDuringTheirLastTurn.add(playerId);
        } else {
            playersWhoActedDuringTheirLastTurn.remove(playerId);
        }
    }

    public void recordSpellCastFromHand(Card card) {
        if (card != null) spellsCastFromHandThisTurn.add(card.getId());
    }

    public void recordSpellCastFromOutsideHand(UUID playerId) {
        if (playerId != null) {
            spellsCastFromOutsideHandThisTurn.merge(playerId, 1, Integer::sum);
        }
    }

    /** Records the source zone of a spell cast for zone-restricted spell-cast triggers. */
    public void recordSpellCastFromZone(UUID playerId, Zone sourceZone) {
        if (playerId == null || sourceZone == null) return;
        spellCastCountsByZoneThisTurn
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .merge(sourceZone, 1, Integer::sum);
    }

    /** Records the source zone of a spell cast, remembering the spell itself for "first spell from [zone]" checks. */
    public void recordSpellCastFromZone(UUID playerId, Zone sourceZone, Card spell) {
        recordSpellCastFromZone(playerId, sourceZone);
        if (playerId == null || sourceZone == null || spell == null) return;
        spellCardsCastByZoneThisTurn
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(sourceZone, ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(spell);
    }

    /** The spells the player cast this turn from the given zone, in cast order. */
    public List<Card> getSpellsCastThisTurn(UUID playerId, Zone sourceZone) {
        if (playerId == null || sourceZone == null) return List.of();
        List<Card> spells = spellCardsCastByZoneThisTurn.getOrDefault(playerId, Map.of()).get(sourceZone);
        return spells == null ? List.of() : List.copyOf(spells);
    }

    /** Records that a player activated an ability of a card in a graveyard this turn. */
    public void recordActivatedAbilityOfGraveyardCard(UUID playerId) {
        if (playerId != null) playersWhoActivatedAbilityOfGraveyardCardThisTurn.add(playerId);
    }

    public int getSpellsCastThisTurnCount(UUID playerId, Zone sourceZone) {
        if (playerId == null || sourceZone == null) return 0;
        return spellCastCountsByZoneThisTurn.getOrDefault(playerId, Map.of())
                .getOrDefault(sourceZone, 0);
    }

    public int getTotalSpellsCastThisTurnCount(Zone sourceZone) {
        if (sourceZone == null) return getTotalSpellsCastThisTurnCount();
        return spellCastCountsByZoneThisTurn.values().stream()
                .mapToInt(counts -> counts.getOrDefault(sourceZone, 0))
                .sum();
    }

    public boolean wasSpellCastFromHandThisTurn(UUID cardId) {
        return cardId != null && spellsCastFromHandThisTurn.contains(cardId);
    }

    public void clearSpellsCastFromHandThisTurn() {
        spellsCastFromHandThisTurn.clear();
    }

    public void clearSpellsCastFromOutsideHandThisTurn() {
        spellsCastFromOutsideHandThisTurn.clear();
    }

    public void recordLandPlayedThisTurn(UUID playerId, Card land) {
        landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
        landCardsPlayedThisTurn.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(land);
    }

    public void recordStackEntryForCrime(StackEntry entry) {
        if (entry == null || entry.getControllerId() == null || entry.isNonTargeting()
                || !targetsOpponentOrOpponentObject(entry)) return;
        crimeCandidatesThisTurn
                .computeIfAbsent(entry.getControllerId(), ignored ->
                        Collections.synchronizedList(new ArrayList<>()))
                .add(entry);
    }

    public boolean isCommittedCrime(StackEntry entry) {
        if (entry == null || entry.getControllerId() == null || entry.isNonTargeting()) return false;
        List<StackEntry> candidates = crimeCandidatesThisTurn.get(entry.getControllerId());
        return candidates != null && candidates.contains(entry);
    }

    public boolean hasCommittedCrimeThisTurn(UUID playerId) {
        List<StackEntry> candidates = crimeCandidatesThisTurn.get(playerId);
        return candidates != null && candidates.stream().anyMatch(entry -> !entry.isNonTargeting());
    }

    private boolean targetsOpponentOrOpponentObject(StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            return targetsOpponentGraveyardCard(entry.getTargetId(), controllerId)
                    || entry.getTargetCardIds().stream()
                    .anyMatch(cardId -> targetsOpponentGraveyardCard(cardId, controllerId));
        }
        if (entry.getTargetZone() == Zone.STACK) {
            return targetsOpponentStackObject(entry.getTargetId(), controllerId)
                    || entry.getTargetIds().stream()
                    .anyMatch(targetId -> targetsOpponentStackObject(targetId, controllerId));
        }
        return targetsOpponentPlayerOrPermanent(entry.getTargetId(), controllerId)
                || entry.getTargetIds().stream()
                .anyMatch(targetId -> targetsOpponentPlayerOrPermanent(targetId, controllerId))
                || entry.getTargetCardIds().stream()
                .anyMatch(cardId -> targetsOpponentGraveyardCard(cardId, controllerId));
    }

    private boolean targetsOpponentPlayerOrPermanent(UUID targetId, UUID controllerId) {
        if (targetId == null) return false;
        if (playerIds.contains(targetId)) return !controllerId.equals(targetId);
        for (Map.Entry<UUID, List<Permanent>> battlefield : playerBattlefields.entrySet()) {
            if (battlefield.getValue().stream().anyMatch(permanent -> permanent.getId().equals(targetId))) {
                return !controllerId.equals(battlefield.getKey());
            }
        }
        return false;
    }

    private boolean targetsOpponentGraveyardCard(UUID targetId, UUID controllerId) {
        if (targetId == null) return false;
        for (Map.Entry<UUID, List<Card>> graveyard : playerGraveyards.entrySet()) {
            if (graveyard.getValue().stream().anyMatch(card -> card.getId().equals(targetId))) {
                return !controllerId.equals(graveyard.getKey());
            }
        }
        return false;
    }

    private boolean targetsOpponentStackObject(UUID targetId, UUID controllerId) {
        if (targetId == null) return false;
        synchronized (stack) {
            return stack.stream()
                    .filter(stackEntry -> stackEntry.getCard() != null
                            && stackEntry.getCard().getId().equals(targetId))
                    .anyMatch(stackEntry -> !controllerId.equals(stackEntry.getControllerId()));
        }
    }

    /** Records that a creature spell cast by {@code playerId} was countered by an opponent this turn. */
    public void recordCreatureSpellCounteredByOpponentThisTurn(UUID playerId) {
        if (playerId != null) {
            playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn.add(playerId);
        }
    }

    /** Records that a player played a real card from exile this turn. */
    public void recordCardPlayedFromExile(UUID playerId) {
        playersWhoPlayedCardFromExileThisTurn.add(playerId);
    }

    /** Records a permanent sacrificed by the given player this turn. */
    public void recordSacrificedPermanent(UUID playerId, Card card) {
        if (playerId == null || card == null) return;
        playersWhoSacrificedPermanentsThisTurn.add(playerId);
        sacrificedPermanentCountThisTurn.merge(playerId, 1, Integer::sum);
        permanentsSacrificedThisTurn
                .computeIfAbsent(playerId, ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(card);
        if (card.hasType(CardType.ARTIFACT)) {
            playersWhoSacrificedArtifactsThisTurn.add(playerId);
        }
        Map<CardSubtype, Integer> subtypeCounts = sacrificedPermanentSubtypeCountThisTurn
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
        card.getSubtypes().stream().distinct().forEach(subtype -> subtypeCounts.merge(subtype, 1, Integer::sum));
    }

    public void markSpellPaidUsingPendingAnyManaType(UUID cardId) {
        if (cardId != null) {
            spellsPaidUsingPendingAnyManaTypeThisTurn.add(cardId);
        }
    }

    public boolean spellWasCastWithConvokeGrant(UUID spellCardId) {
        return spellsCastWithConvokeGrantThisTurn.contains(spellCardId);
    }

    /** Whether the player holds a grant to give the next spell they cast convoke. */
    public boolean hasNextSpellConvokeGrant(UUID playerId) {
        return hasFloatingEffectOnPlayer(playerId, GrantConvokeToNextSpellThisTurnEffect.class);
    }

    /** Whether the player holds the alternative cost of paying life equal to the next spell's mana value. */
    public boolean hasNextSpellPayLifeEqualToManaValue(UUID playerId) {
        return hasFloatingEffectOnPlayer(playerId, GrantPayLifeEqualToSpellManaValueForNextSpellEffect.class);
    }

    /** Whether the player holds a permission to spend mana of any type on the next spell they cast. */
    public boolean hasPendingAnyManaTypeForNextSpellThisTurn(UUID playerId) {
        return hasFloatingEffectOnPlayer(playerId, SpendAnyManaTypeForNextSpellThisTurnEffect.class);
    }

    /**
     * Applies the riders "the next spell you cast this turn can't be countered" and "the next creature spell you cast
     * this turn can't be countered / enters with additional +1/+1 counters" to {@code card}, the spell {@code playerId}
     * has just cast. The duration clock ends the effects with the same cast.
     */
    private void applyNextSpellRiders(UUID playerId, Card card) {
        if (hasFloatingEffectOnPlayer(playerId, MakeNextSpellUncounterableThisTurnEffect.class)) {
            spellsMadeUncounterable.add(card.getId());
        }
        if ((card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && hasFloatingEffectOnPlayer(playerId, MakeNextInstantSorceryUncounterableThisTurnEffect.class)) {
            spellsMadeUncounterable.add(card.getId());
        }
        if (!card.hasType(CardType.CREATURE)) {
            return;
        }
        for (FloatingContinuousEffect empowerment
                : floatingEffectsOnPlayer(playerId, EmpowerNextCreatureSpellThisTurnEffect.class)) {
            var grant = (EmpowerNextCreatureSpellThisTurnEffect) empowerment.effect();
            if (grant.uncounterable()) {
                spellsMadeUncounterable.add(card.getId());
            }
            if (grant.additionalPlusOneCounters() > 0) {
                spellAdditionalEnterCounters.merge(card.getId(), grant.additionalPlusOneCounters(), Integer::sum);
            }
        }
    }

    /**
     * Uses up one permission to spend mana of any type on the next spell, if the spell {@code card} was paid with it:
     * the permission ends with that cast.
     */
    private void consumePendingAnyManaTypePermission(UUID playerId, Card card) {
        if (!spellsPaidUsingPendingAnyManaTypeThisTurn.remove(card.getId())) {
            return;
        }
        floatingEffectsOnPlayer(playerId, SpendAnyManaTypeForNextSpellThisTurnEffect.class).stream()
                .findFirst().ifPresent(this::endFloatingEffect);
    }

    /**
     * Binds the "next instant or sorcery spell you cast from your hand this turn" returns-to-hand effect to the spell
     * being cast: every such effect the player holds applies to this same spell and ends here. Returns whether one
     * did.
     */
    public boolean bindNextInstantSorceryCastFromHandToHand(UUID playerId) {
        List<FloatingContinuousEffect> bound = floatingEffectsOnPlayer(playerId,
                ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect.class);
        bound.forEach(this::endFloatingEffect);
        return !bound.isEmpty();
    }

    /**
     * Returns how many spells with the given name the player has cast this game (persists across turns).
     */
    public int getSpellsCastThisGameByNameCount(UUID playerId, String name) {
        return spellNameCastCountsThisGame.getOrDefault(playerId, Map.of()).getOrDefault(name, 0);
    }

    /** Returns how many cards with the given name the player has cycled this game. */
    public int getCardsCycledThisGameByNameCount(UUID playerId, String name) {
        return cardNameCycleCountsThisGame.getOrDefault(playerId, Map.of()).getOrDefault(name, 0);
    }

    /**
     * Returns the total number of spells the given player has cast during this game.
     */
    public int getSpellsCastThisGameCount(UUID playerId) {
        return spellNameCastCountsThisGame.getOrDefault(playerId, Map.of()).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * The scheme cards face up in the player's command zone. Only these schemes' static abilities
     * apply and triggered abilities trigger (CR 314.4).
     */
    public List<Card> faceUpSchemes(UUID playerId) {
        List<Card> commandZone = playerCommandZones.get(playerId);
        if (commandZone == null) {
            return List.of();
        }
        List<Card> schemes = new ArrayList<>();
        for (Card card : commandZone) {
            if (isFaceUpScheme(card)) {
                schemes.add(card);
            }
        }
        return schemes;
    }

    /**
     * Calls {@code action} with each face-up scheme's owner and a permanent view of the scheme, in
     * player order, so trigger collectors that scan permanents also see the triggered abilities of
     * face-up schemes in the command zone (CR 314.4). The view is not on any battlefield.
     */
    public void forEachFaceUpSchemeSource(BiConsumer<UUID, Permanent> action) {
        for (UUID playerId : orderedPlayerIds) {
            for (Card scheme : faceUpSchemes(playerId)) {
                action.accept(playerId, faceUpSchemeSource(scheme));
            }
        }
    }

    /**
     * The player's battlefield followed by permanent views of their face-up schemes, whose triggered
     * abilities trigger from the command zone (CR 314.4). Returns the battlefield list itself when
     * the player has no face-up scheme.
     */
    public List<Permanent> permanentsAndFaceUpSchemes(UUID playerId) {
        List<Permanent> battlefield = playerBattlefields.get(playerId);
        List<Card> schemes = faceUpSchemes(playerId);
        if (schemes.isEmpty()) {
            return battlefield == null ? List.of() : battlefield;
        }
        List<Permanent> sources = battlefield == null ? new ArrayList<>() : new ArrayList<>(battlefield);
        for (Card scheme : schemes) {
            sources.add(faceUpSchemeSource(scheme));
        }
        return sources;
    }

    /** The face-up scheme whose trigger-source view has this id, or {@code null}. */
    public Card findFaceUpSchemeBySourceId(UUID sourceId) {
        if (sourceId == null) {
            return null;
        }
        for (Permanent view : faceUpSchemeSourceViews.values()) {
            if (view.getId().equals(sourceId) && isFaceUpScheme(view.getCard())) {
                return view.getCard();
            }
        }
        return null;
    }

    private Permanent faceUpSchemeSource(Card scheme) {
        return faceUpSchemeSourceViews.computeIfAbsent(scheme.getId(), ignored -> new Permanent(scheme));
    }

    /** Whether the card is a scheme face up in its owner's command zone (CR 314.4). */
    public boolean isFaceUpScheme(Card card) {
        if (card == null || !card.hasType(CardType.SCHEME) || faceDownCommandZoneCards.contains(card.getId())) {
            return false;
        }
        for (List<Card> commandZone : playerCommandZones.values()) {
            if (commandZone.contains(card)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Abandons a scheme in a player's command zone: it is turned face down and put on the bottom
     * of its owner's scheme deck (CR 701.33b). Returns false when the card is not in a command zone.
     */
    public boolean abandonScheme(Card scheme) {
        for (Map.Entry<UUID, List<Card>> commandZone : playerCommandZones.entrySet()) {
            if (!commandZone.getValue().contains(scheme)) {
                continue;
            }
            faceDownCommandZoneCards.add(scheme.getId());
            faceUpSchemeSourceViews.remove(scheme.getId());
            List<Card> schemeDeck = playerSchemeDecks.computeIfAbsent(
                    commandZone.getKey(), ignored -> new ArrayList<>());
            schemeDeck.remove(scheme);
            schemeDeck.add(scheme);
            return true;
        }
        return false;
    }


    /** Records a commander cast for cast paths that do not use the ordinary spell stack recorder. */
    public void recordCommanderCastFromCommandZone(UUID playerId) {
        if (playerId != null) {
            commanderCastsFromCommandZoneThisGame.merge(playerId, 1, Integer::sum);
        }
    }

    /** Counts a spell or ability once when it causes a guess or causes cards/permanents to be piled. */
    public void recordPileGroupingOrGuess(StackEntry entry) {
        if (entry != null && !entry.isCausedPileGroupingOrGuessThisTurn()) {
            entry.setCausedPileGroupingOrGuessThisTurn(true);
            pileGroupingOrGuessCountThisTurn++;
        }
    }

    /** Counts the currently resolving spell or ability for a player-choice flow. */
    public void recordPileGroupingOrGuess() {
        recordPileGroupingOrGuess(pendingEffectResolutionEntry);
    }

    /** Returns how many commanders the player has cast from the command zone this game. */
    public int getCommanderCastsFromCommandZoneThisGame(UUID playerId) {
        return commanderCastsFromCommandZoneThisGame.getOrDefault(playerId, 0);
    }

    public void addSpellCastManaSpent(UUID spellCardId, int manaSpent) {
        if (manaSpent > 0) {
            spellCastManaSpent.merge(spellCardId, manaSpent, Integer::sum);
        }
    }

    /** Records that {@code amount} mana {@code sourcePermanentId} is about to add once its color is chosen carries {@code rider}. */
    public void markPendingCopyRiderManaChoice(UUID sourcePermanentId, ManaCopyRider rider, int amount) {
        if (sourcePermanentId != null && amount > 0) {
            pendingCopyRiderManaChoices.computeIfAbsent(sourcePermanentId, ignored -> new ConcurrentHashMap<>())
                    .merge(rider, amount, Integer::sum);
        }
    }

    /** How much of the {@code amount} mana just chosen for {@code sourcePermanentId} carries {@code rider}. */
    public int consumePendingCopyRiderManaChoice(UUID sourcePermanentId, ManaCopyRider rider, int amount) {
        if (sourcePermanentId == null) {
            return 0;
        }
        Map<ManaCopyRider, Integer> pending = pendingCopyRiderManaChoices.get(sourcePermanentId);
        if (pending == null) {
            return 0;
        }
        int consumed = consumePendingManaChoice(pending, rider, amount);
        if (pending.isEmpty()) {
            pendingCopyRiderManaChoices.remove(sourcePermanentId);
        }
        return consumed;
    }

    private static <K> int consumePendingManaChoice(Map<K, Integer> pendingChoices, K key, int amount) {
        if (key == null || amount <= 0) {
            return 0;
        }
        int[] consumed = {0};
        pendingChoices.computeIfPresent(key, (ignored, pending) -> {
            consumed[0] = Math.min(pending, amount);
            int remaining = pending - consumed[0];
            return remaining > 0 ? remaining : null;
        });
        return consumed[0];
    }

    public void recordCounteredSpellManaCost(UUID spellTargetableId, int manaSpent, int manaValue) {
        counteredSpellManaCosts.put(spellTargetableId, new CounteredSpellManaCost(manaSpent, manaValue));
    }

    public int getSpellCastManaSpent(UUID spellCardId) {
        return spellCastManaSpent.getOrDefault(spellCardId, 0);
    }

    public int getManaSpentToCastSpellsThisTurn(UUID playerId) {
        return manaSpentToCastSpellsThisTurn.getOrDefault(playerId, 0);
    }

    public void clearSpellCastManaSpent(UUID spellCardId) {
        spellCastManaSpent.remove(spellCardId);
    }

    public void markSpellCastUsedTreasureMana(UUID spellCardId) {
        spellCastUsedTreasureMana.put(spellCardId, true);
    }

    public boolean spellCastUsedTreasureMana(UUID spellCardId) {
        return spellCastUsedTreasureMana.getOrDefault(spellCardId, false);
    }

    public void recordTreasureTokenCreated(UUID playerId) {
        if (playerId != null) {
            treasureTokensCreatedThisTurn.merge(playerId, 1, Integer::sum);
        }
    }

    public int getTreasureTokensCreatedThisTurn(UUID playerId) {
        return playerId == null ? 0 : treasureTokensCreatedThisTurn.getOrDefault(playerId, 0);
    }

    public void clearSpellCastTreasureMana(UUID spellCardId) {
        spellCastUsedTreasureMana.remove(spellCardId);
    }

    public void recordBending(UUID playerId, BendingType type) {
        bendingTypesCompletedThisTurn
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(type);
    }

    public boolean completedAllBendingTypes(UUID playerId) {
        Set<BendingType> completed = bendingTypesCompletedThisTurn.get(playerId);
        return completed != null && completed.containsAll(java.util.EnumSet.allOf(BendingType.class));
    }

    public void addSpellCastManaSource(UUID spellCardId, UUID sourcePermanentId) {
        if (sourcePermanentId != null) {
            spellCastManaSourceIds.computeIfAbsent(spellCardId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(sourcePermanentId);
        }
    }

    public void addSpellCastManaSourceAmount(UUID spellCardId, UUID sourcePermanentId, int amount) {
        if (sourcePermanentId != null && amount > 0) {
            spellCastManaSourceAmounts.computeIfAbsent(spellCardId, ignored -> new ConcurrentHashMap<>())
                    .merge(sourcePermanentId, amount, Integer::sum);
        }
    }

    public int getSpellCastManaSourceAmount(UUID spellCardId, UUID sourcePermanentId) {
        return spellCastManaSourceAmounts.getOrDefault(spellCardId, Map.of()).getOrDefault(sourcePermanentId, 0);
    }

    public boolean spellCastUsedManaFromSource(UUID spellCardId, UUID sourcePermanentId) {
        return spellCastManaSourceIds.getOrDefault(spellCardId, Set.of()).contains(sourcePermanentId);
    }

    public void clearSpellCastManaSources(UUID spellCardId) {
        spellCastManaSourceIds.remove(spellCardId);
        spellCastManaSourceAmounts.remove(spellCardId);
    }

    public void setSpellCastCreatureManaSpent(UUID spellCardId, int manaSpent) {
        spellCastCreatureManaSpent.put(spellCardId, manaSpent);
    }

    public int getSpellCastCreatureManaSpent(UUID spellCardId) {
        return spellCastCreatureManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastCreatureManaSpent(UUID spellCardId) {
        spellCastCreatureManaSpent.remove(spellCardId);
    }

    public void setSpellCastConvergeValue(UUID spellCardId, int convergeValue) {
        spellCastConvergeValue.put(spellCardId, convergeValue);
    }

    public int getSpellCastConvergeValue(UUID spellCardId) {
        return spellCastConvergeValue.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastConvergeValue(UUID spellCardId) {
        spellCastConvergeValue.remove(spellCardId);
    }

    public void setSpellCastSplicedNames(UUID spellCardId, List<String> names) {
        spellCastSplicedNames.put(spellCardId, List.copyOf(names));
    }

    public List<String> getSpellCastSplicedNames(UUID spellCardId) {
        return spellCastSplicedNames.getOrDefault(spellCardId, List.of());
    }

    public void clearSpellCastSplicedNames(UUID spellCardId) {
        spellCastSplicedNames.remove(spellCardId);
    }

    public void setSpellCastSplicedKeywords(UUID spellCardId, Set<Keyword> keywords) {
        spellCastSplicedKeywords.put(spellCardId, Set.copyOf(keywords));
    }

    public Set<Keyword> getSpellCastSplicedKeywords(UUID spellCardId) {
        return spellCastSplicedKeywords.getOrDefault(spellCardId, Set.of());
    }

    public void clearSpellCastSplicedKeywords(UUID spellCardId) {
        spellCastSplicedKeywords.remove(spellCardId);
    }

    public void setSpellCastColorsSpent(UUID spellCardId, java.util.EnumSet<ManaColor> colorsSpent) {
        spellCastColorsSpent.put(spellCardId, colorsSpent);
    }

    public java.util.Set<ManaColor> getSpellCastColorsSpent(UUID spellCardId) {
        return spellCastColorsSpent.getOrDefault(spellCardId, java.util.EnumSet.noneOf(ManaColor.class));
    }

    public void clearSpellCastColorsSpent(UUID spellCardId) {
        spellCastColorsSpent.remove(spellCardId);
    }

    public void setSpellCastManaSpentByColor(UUID spellCardId,
                                             java.util.EnumMap<ManaColor, Integer> manaSpentByColor) {
        spellCastManaSpentByColor.put(spellCardId, new java.util.EnumMap<>(manaSpentByColor));
    }

    public int getSpellCastManaSpentByColor(UUID spellCardId, ManaColor color) {
        java.util.EnumMap<ManaColor, Integer> spent = spellCastManaSpentByColor.get(spellCardId);
        return spent == null ? 0 : spent.getOrDefault(color, 0);
    }

    public void clearSpellCastManaSpentByColor(UUID spellCardId) {
        spellCastManaSpentByColor.remove(spellCardId);
    }

    public void setSpellCastSnowManaSpent(UUID spellCardId, int snowManaSpent) {
        spellCastSnowManaSpent.put(spellCardId, snowManaSpent);
    }

    public int getSpellCastSnowManaSpent(UUID spellCardId) {
        return spellCastSnowManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastSnowManaSpent(UUID spellCardId) {
        spellCastSnowManaSpent.remove(spellCardId);
    }

    public void setSpellCastSnowManaSpentByColor(UUID spellCardId,
                                                 java.util.EnumMap<ManaColor, Integer> snowManaSpentByColor) {
        spellCastSnowManaSpentByColor.put(spellCardId, new java.util.EnumMap<>(snowManaSpentByColor));
    }

    public int getSpellCastSnowManaSpentByColor(UUID spellCardId, ManaColor color) {
        java.util.EnumMap<ManaColor, Integer> spent = spellCastSnowManaSpentByColor.get(spellCardId);
        return spent == null ? 0 : spent.getOrDefault(color, 0);
    }

    public void clearSpellCastSnowManaSpentByColor(UUID spellCardId) {
        spellCastSnowManaSpentByColor.remove(spellCardId);
    }

    /** Records a completed dungeon for the player, preserving both the legacy flag and its name. */
    public void recordCompletedDungeon(UUID playerId, Dungeon dungeon) {
        if (playerId == null || dungeon == null) return;
        playersWhoCompletedDungeon.add(playerId);
        completedDungeonsByPlayer
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(dungeon);
    }

    public Set<CardSubtype> getBuddyList(UUID playerId) {
        return Collections.unmodifiableSet(playerBuddyLists.getOrDefault(playerId, Set.of()));
    }

    public boolean addBuddy(UUID playerId, CardSubtype subtype) {
        if (playerId == null || subtype == null) return false;
        return playerBuddyLists.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet()).add(subtype);
    }

    public void addSpellCastTreasureManaSpent(UUID spellCardId, int amount) {
        if (amount > 0) {
            spellCastTreasureManaSpent.merge(spellCardId, amount, Integer::sum);
        }
    }

    public int getSpellCastTreasureManaSpent(UUID spellCardId) {
        return spellCastTreasureManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastTreasureManaSpent(UUID spellCardId) {
        spellCastTreasureManaSpent.remove(spellCardId);
    }

    public void addSpellCastArtifactManaSpent(UUID spellCardId, int amount) {
        if (amount > 0) {
            spellCastArtifactManaSpent.merge(spellCardId, amount, Integer::sum);
        }
    }

    public int getSpellCastArtifactManaSpent(UUID spellCardId) {
        return spellCastArtifactManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastArtifactManaSpent(UUID spellCardId) {
        spellCastArtifactManaSpent.remove(spellCardId);
    }

    public void setSpellCastCaveManaSpent(UUID spellCardId, int caveManaSpent) {
        spellCastCaveManaSpent.put(spellCardId, caveManaSpent);
    }

    public int getSpellCastCaveManaSpent(UUID spellCardId) {
        return spellCastCaveManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastCaveManaSpent(UUID spellCardId) {
        spellCastCaveManaSpent.remove(spellCardId);
    }

    public void setSpellCastDesertManaSpent(UUID spellCardId, int desertManaSpent) {
        spellCastDesertManaSpent.put(spellCardId, desertManaSpent);
    }

    public int getSpellCastDesertManaSpent(UUID spellCardId) {
        return spellCastDesertManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastDesertManaSpent(UUID spellCardId) {
        spellCastDesertManaSpent.remove(spellCardId);
    }

    public void setSpellCastManaSpentOnX(UUID spellCardId, java.util.EnumMap<ManaColor, Integer> spentOnX) {
        spellCastManaSpentOnX.put(spellCardId, spentOnX);
    }

    public int getSpellCastManaSpentOnX(UUID spellCardId, ManaColor color) {
        java.util.EnumMap<ManaColor, Integer> spent = spellCastManaSpentOnX.get(spellCardId);
        return spent == null ? 0 : spent.getOrDefault(color, 0);
    }

    public void clearSpellCastManaSpentOnX(UUID spellCardId) {
        spellCastManaSpentOnX.remove(spellCardId);
    }

    /**
     * Returns the number of spells the given player has cast this turn.
     */
    public int getSpellsCastThisTurnCount(UUID playerId) {
        return spellsCastThisTurn.getOrDefault(playerId, List.of()).size();
    }

    public int getSpellsCastFromOutsideHandThisTurnCount(UUID playerId) {
        return spellsCastFromOutsideHandThisTurn.getOrDefault(playerId, 0);
    }

    /** Whether mana a Treasure produced was spent to cast the spell {@code cardId} this turn. */
    public boolean wasSpellCastUsingTreasureManaThisTurn(UUID cardId) {
        return cardId != null && spellsCastUsingTreasureManaThisTurn.contains(cardId);
    }

    /** Returns the number of spells cast with Treasure-produced mana by the player this turn. */
    public long getSpellsCastUsingTreasureManaThisTurnCount(UUID playerId) {
        return spellsCastThisTurn.getOrDefault(playerId, List.of()).stream()
                .filter(card -> spellsCastUsingTreasureManaThisTurn.contains(card.getId()))
                .count();
    }

    /**
     * The most spells {@code playerId} may cast this turn under the limits the resolving "you can cast only one more
     * spell this turn" effects made: the most restrictive of them, or {@link Integer#MAX_VALUE} when none applies.
     */
    public int spellLimitThisTurn(UUID playerId) {
        int limit = Integer.MAX_VALUE;
        for (FloatingContinuousEffect fe : floatingEffectsOnPlayer(playerId,
                LimitControllerToOneMoreSpellThisTurnEffect.class)) {
            limit = Math.min(limit, ((LimitControllerToOneMoreSpellThisTurnEffect) fe.effect()).maxSpells());
        }
        return limit;
    }

    /**
     * Prevents {@code playerId} from casting any further spells this turn: a floating effect that bars every
     * spell type but land (CR 611.2a), made by the spell or ability named {@code sourceName}.
     */
    public void preventAdditionalSpellCastsThisTurn(String sourceName, UUID playerId) {
        if (playerId == null) return;

        EnumSet<CardType> allSpellTypes = EnumSet.allOf(CardType.class);
        allSpellTypes.remove(CardType.LAND);
        addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceName, null, playerId,
                new TargetPlayerCantCastSpellTypesThisTurnEffect(allSpellTypes), playerId,
                Duration.UNTIL_END_OF_TURN));
    }

    /** Records an oil counter removal from a permanent currently controlled by a player. */
    public void recordOilCounterRemoved(Permanent permanent, int amount) {
        if (permanent == null || amount <= 0) {
            return;
        }
        playerBattlefields.forEach((controllerId, battlefield) -> {
            if (battlefield.contains(permanent)) {
                playersWhoRemovedOilCountersFromControlledPermanentsThisTurn.add(controllerId);
            }
        });
    }

    /** Records that a permanent carrying an oil counter entered a graveyard this turn. */
    public void recordPermanentWithOilCounterPutIntoGraveyard() {
        permanentWithOilCounterPutIntoGraveyardThisTurn = true;
    }

    /**
     * Returns the total number of spells cast by all players this turn (used by the Storm keyword).
     */
    public int getTotalSpellsCastThisTurnCount() {
        return spellsCastThisTurn.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Total lands the given player may play this turn: the normal one, plus any additional grants
     * ({@code additionalLandsThisTurn}), plus one for each {@link EachPlayerPlaysAdditionalLandEffect}
     * static permanent on any battlefield (Storm Cauldron — symmetric, benefits every player), plus
     * the {@code amount} of each {@link PlaysAdditionalLandEachTurnEffect} static permanent the player
     * themselves controls (The Gitrog Monster / Azusa, Lost but Seeking — controller-only). Face-up
     * planar cards are command-zone sources, so their land-play static effects are included as well.
     */
    public int getMaxLandsThisTurn(UUID playerId) {
        long extraFromStatics = 0;
        for (UUID pid : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof EachPlayerPlaysAdditionalLandEffect) {
                        extraFromStatics = Math.min(Integer.MAX_VALUE, extraFromStatics + 1);
                    } else if (effect instanceof PlaysAdditionalLandEachTurnEffect additional && pid.equals(playerId)) {
                        extraFromStatics = Math.min(Integer.MAX_VALUE,
                                extraFromStatics + additional.amount());
                    }
                }
            }
        }
        if (planechase != null) {
            for (var planar : planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof EachPlayerPlaysAdditionalLandEffect) {
                        extraFromStatics = Math.min(Integer.MAX_VALUE, extraFromStatics + 1);
                    } else if (effect instanceof PlayersWithChosenPlanarModePlayAdditionalLandEffect mode
                            && mode.mode().equals(planar.getChosenModeByPlayer().get(playerId))) {
                        extraFromStatics = Math.min(Integer.MAX_VALUE, extraFromStatics + 1);
                    } else if (effect instanceof PlaysAdditionalLandEachTurnEffect additional
                            && playerId.equals(planechase.controllerId)) {
                        extraFromStatics = Math.min(Integer.MAX_VALUE,
                                extraFromStatics + additional.amount());
                    }
                }
            }
        }
        long total = 1L + additionalLandsThisTurn.getOrDefault(playerId, 0) + extraFromStatics;
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    /**
     * Returns an unmodifiable view of the spells the given player has cast this turn.
     */
    /** The mana value a spell cast this turn had on the stack, including its chosen X (CR 202.3e). */
    public int getSpellCastStackManaValue(Card card) {
        return spellCastStackManaValuesThisTurn.getOrDefault(card.getId(), card.getManaValue());
    }

    public List<Card> getSpellsCastThisTurn(UUID playerId) {
        return Collections.unmodifiableList(spellsCastThisTurn.getOrDefault(playerId, List.of()));
    }

    /** Records the mana value a spell had on the stack when it was cast, including the X it was cast with. */
    public void recordSpellManaValueWhenCast(UUID cardId, int manaValue) {
        spellManaValuesWhenCastThisTurn.put(cardId, manaValue);
    }

    /** The mana value {@code spell} had when cast this turn, or its printed mana value when none was recorded. */
    public int getSpellManaValueWhenCast(Card spell) {
        return spellManaValuesWhenCastThisTurn.getOrDefault(spell.getId(), spell.getManaValue());
    }

    /** Returns an unmodifiable view of the spells the given player cast during the previous turn. */
    public List<Card> getSpellsCastLastTurn(UUID playerId) {
        return Collections.unmodifiableList(spellsCastLastTurnCards.getOrDefault(playerId, List.of()));
    }

    public boolean hasSpellCastTargetingCreatureThisTurn(UUID playerId) {
        return !spellsCastTargetingCreatureThisTurn.getOrDefault(playerId, Set.of()).isEmpty();
    }

    private boolean spellTargetsCreaturePermanent(Card card, UUID playerId) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackEntry entry = stack.get(i);
            if (entry.getCard() == null || !entry.getCard().getId().equals(card.getId())
                    || !playerId.equals(entry.getControllerId())) {
                continue;
            }
            List<UUID> targetIds = new ArrayList<>(entry.getDeclaredTargetIds());
            if (entry.getTargetId() != null && !targetIds.contains(entry.getTargetId())) {
                targetIds.add(entry.getTargetId());
            }
            return targetIds.stream().anyMatch(this::isCreaturePermanent);
        }
        return false;
    }

    private boolean isCreaturePermanent(UUID targetId) {
        return targetId != null && playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(permanent -> permanent.getId().equals(targetId)
                        && permanent.getCard().hasType(CardType.CREATURE));
    }

    public Set<UUID> getKickedSpellsCastThisTurn(UUID playerId) {
        return Collections.unmodifiableSet(kickedSpellsCastThisTurn.getOrDefault(playerId, Set.of()));
    }

    /**
     * Returns the spell most recently cast by any player this turn, or {@code null} if no spell has
     * been cast yet.
     */
    public Card getMostRecentSpellCastThisTurn() {
        return mostRecentSpellCastThisTurn;
    }

    /**
     * Returns true if no spells have been cast by any player this turn.
     */
    public boolean isSpellsCastThisTurnEmpty() {
        return spellsCastThisTurn.isEmpty();
    }

    /**
     * Returns the 1-based position of the given spell card in this turn's global cast order, or 0 if
     * that card was not cast this turn. "The second spell cast this turn" is ordinal 2. A card cast
     * again after changing zones is a new object (CR 400.7), so its latest cast is the one counted.
     */
    public int getSpellCastOrdinalThisTurn(UUID cardId) {
        return spellCastOrderThisTurn.lastIndexOf(cardId) + 1;
    }

    /**
     * The spells cast this turn by every player, as card ids in the order they were cast. A card cast again after
     * changing zones is a new object (CR 400.7) and appears again.
     */
    public List<UUID> getSpellCastOrderThisTurn() {
        synchronized (spellCastOrderThisTurn) {
            return List.copyOf(spellCastOrderThisTurn);
        }
    }

    /**
     * The 0-based place of the given card's latest casting among the spells {@code playerId} cast this turn
     * ({@link #getSpellsCastThisTurn(UUID)}), or {@code -1} when that player didn't cast it this turn.
     */
    public int getSpellCastIndexThisTurn(UUID playerId, UUID cardId) {
        List<Card> spells = getSpellsCastThisTurn(playerId);
        for (int i = spells.size() - 1; i >= 0; i--) {
            if (spells.get(i).getId().equals(cardId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Snapshots per-player spell counts into the given target map, then clears spell tracking for the new turn.
     */
    public void snapshotSpellCountsAndClear(Map<UUID, Integer> target) {
        target.clear();
        spellsCastLastTurnCards.clear();
        spellsCastThisTurn.forEach((id, spells) -> target.put(id, spells.size()));
        spellsCastThisTurn.forEach((id, spells) -> spellsCastLastTurnCards.put(id, new ArrayList<>(spells)));
        spellsCastThisTurn.clear();
        spellManaValuesWhenCastThisTurn.clear();
        spellsCastTargetingCreatureThisTurn.clear();
        spellsCastUsingTreasureManaThisTurn.clear();
        spellsCastWithConvokeGrantThisTurn.clear();
        spellCastCountsByZoneThisTurn.clear();
        spellCardsCastByZoneThisTurn.clear();
        kickedSpellsCastThisTurn.clear();
        spellCastOrderThisTurn.clear();
        artifactSpellsCastThisTurn.clear();
        spellCastStackManaValuesThisTurn.clear();
        mostRecentSpellCastThisTurn = null;
        spellWarpedThisTurn = false;
        manaSpentToCastSpellsThisTurn.clear();
        greatestManaValueNotedForPermanentThisTurn.clear();
    }

    /** Returns the greatest mana value noted for the given permanent during this turn. */
    public int getGreatestManaValueNotedForPermanentThisTurn(UUID permanentId) {
        return greatestManaValueNotedForPermanentThisTurn.getOrDefault(permanentId, 0);
    }

    /** Scoped to one casting action; never changes the player's actual hand. */
    public final Map<UUID, CommanderBounceContext> commanderBounceContexts = new ConcurrentHashMap<>();
    public final List<CommanderZoneMove> pendingCommanderZoneMoves = new ArrayList<>();
    public UUID completingCommanderZoneMove;
    public boolean deferCommanderZoneMove(Card card, Zone destination, int index) {
        if (!isCommander(card.getId()) || card.getId().equals(completingCommanderZoneMove)) return false;
        UUID owner = playerCommanders.entrySet().stream().filter(entry -> entry.getValue().stream()
                .anyMatch(commander -> commander.getId().equals(card.getId()))).map(Map.Entry::getKey).findFirst().orElse(card.getOwnerId());
        if (pendingCommanderZoneMoves.stream().noneMatch(move -> move.card().getId().equals(card.getId())))
            pendingCommanderZoneMoves.add(new CommanderZoneMove(owner, card, destination, index, false));
        return true;
    }
    public boolean isPendingCommanderMove(UUID cardId) {
        return pendingCommanderZoneMoves.stream().anyMatch(move -> move.card().getId().equals(cardId));
    }
    public final Set<UUID> commanderReturnCandidates = ConcurrentHashMap.newKeySet();
    public void commanderEnteredReturnZone(Card card) {
        if (isCommander(card.getId())) commanderReturnCandidates.add(card.getId());
    }
    private final class CommanderHandMap extends ConcurrentHashMap<UUID, List<Card>> {
        private List<Card> wrap(UUID playerId, List<Card> cards) {
            return cards instanceof CommanderHandList ? cards : new CommanderHandList(playerId, cards);
        }
        @Override public List<Card> put(UUID key, List<Card> cards) { return super.put(key, wrap(key, cards)); }
        @Override public List<Card> putIfAbsent(UUID key, List<Card> cards) { return super.putIfAbsent(key, wrap(key, cards)); }
        @Override public List<Card> computeIfAbsent(UUID key, Function<? super UUID, ? extends List<Card>> factory) {
            return super.computeIfAbsent(key, id -> wrap(id, factory.apply(id)));
        }
        @Override public void putAll(Map<? extends UUID, ? extends List<Card>> values) { values.forEach(this::put); }
    }
    private final class CommanderHandList extends ArrayList<Card> {
        private final UUID playerId;

        CommanderHandList(UUID playerId, List<Card> cards) {
            super(cards);
            this.playerId = playerId;
            cards.forEach(card -> rulesEvents.track(card, playerId, Zone.HAND));
        }

        private void recordCardPutIntoHand(Card card) {
            cardsPutIntoHandThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
            rulesEvents.arrived(card, playerId, Zone.HAND);
        }

        @Override public boolean add(Card card) {
            if (deferCommanderZoneMove(card, Zone.HAND, -1)) return false;
            boolean changed = super.add(card);
            if (changed) recordCardPutIntoHand(card);
            return changed;
        }
        @Override public void add(int index, Card card) {
            if (!deferCommanderZoneMove(card, Zone.HAND, -1)) {
                super.add(index, card);
                recordCardPutIntoHand(card);
            }
        }
        @Override public boolean addAll(Collection<? extends Card> cards) {
            boolean changed = false;
            for (Card card : List.copyOf(cards)) changed |= add(card);
            return changed;
        }
        @Override public boolean addAll(int index, Collection<? extends Card> cards) {
            int before = size();
            for (Card card : List.copyOf(cards)) {
                int oldSize = size(); add(index, card); if (size() > oldSize) index++;
            }
            return size() != before;
        }
    }
    private final class CommanderGraveyardList extends ArrayList<Card> {
        private final UUID playerId;

        CommanderGraveyardList(UUID playerId, List<Card> cards) {
            super(cards);
            this.playerId = playerId;
            cards.forEach(card -> rulesEvents.track(card, playerId, Zone.GRAVEYARD));
        }
        private void arrived(Card card) { rulesEvents.arrived(card, playerId, Zone.GRAVEYARD); }
        @Override public boolean add(Card card) { commanderEnteredReturnZone(card); boolean added = super.add(card); arrived(card); return added; }
        @Override public void add(int index, Card card) { commanderEnteredReturnZone(card); super.add(index, card); arrived(card); }
        @Override public boolean addAll(java.util.Collection<? extends Card> cards) { List<Card> added = List.copyOf(cards); added.forEach(GameData.this::commanderEnteredReturnZone); boolean changed = super.addAll(added); added.forEach(this::arrived); return changed; }
        @Override public boolean addAll(int index, java.util.Collection<? extends Card> cards) { List<Card> added = List.copyOf(cards); added.forEach(GameData.this::commanderEnteredReturnZone); boolean changed = super.addAll(index, added); added.forEach(this::arrived); return changed; }
    }
    /** A command zone list that reports its arrivals to {@link #rulesEvents}. */
    private final class CommandZoneList extends ArrayList<Card> {
        private final UUID playerId;

        CommandZoneList(UUID playerId, List<Card> cards) {
            super(cards);
            this.playerId = playerId;
            cards.forEach(card -> rulesEvents.track(card, playerId, Zone.COMMAND));
        }
        private void arrived(Card card) { rulesEvents.arrived(card, playerId, Zone.COMMAND); }
        @Override public boolean add(Card card) { boolean added = super.add(card); arrived(card); return added; }
        @Override public void add(int index, Card card) { super.add(index, card); arrived(card); }
        @Override public boolean addAll(java.util.Collection<? extends Card> cards) { List<Card> added = List.copyOf(cards); boolean changed = super.addAll(added); added.forEach(this::arrived); return changed; }
        @Override public boolean addAll(int index, java.util.Collection<? extends Card> cards) { List<Card> added = List.copyOf(cards); boolean changed = super.addAll(index, added); added.forEach(this::arrived); return changed; }
    }
    private final class CommandZoneMap extends ConcurrentHashMap<UUID, List<Card>> {
        private List<Card> wrap(UUID playerId, List<Card> cards) {
            return cards instanceof CommandZoneList ? cards : new CommandZoneList(playerId, cards);
        }
        @Override public List<Card> put(UUID key, List<Card> cards) { return super.put(key, wrap(key, cards)); }
        @Override public List<Card> putIfAbsent(UUID key, List<Card> cards) { return super.putIfAbsent(key, wrap(key, cards)); }
        @Override public List<Card> computeIfAbsent(UUID key, Function<? super UUID, ? extends List<Card>> factory) {
            return super.computeIfAbsent(key, id -> wrap(id, factory.apply(id)));
        }
        @Override public void putAll(Map<? extends UUID, ? extends List<Card>> values) { values.forEach(this::put); }
    }
    /**
     * The exile zone list, which reports its arrivals to {@link #rulesEvents}; replacing an entry moves nothing. An
     * anted card it holds stands for the ante zone (CR 407.4), so its arrival is only tracked.
     */
    private final class ExileZoneList extends ArrayList<ExiledCardEntry> {
        private void arrived(ExiledCardEntry entry) {
            if (antedCardIds.contains(entry.card().getId())) {
                rulesEvents.track(entry.card(), entry.ownerId(), Zone.EXILE);
            } else {
                rulesEvents.arrived(entry.card(), entry.ownerId(), Zone.EXILE);
            }
        }
        @Override public boolean add(ExiledCardEntry entry) { boolean added = super.add(entry); arrived(entry); return added; }
        @Override public void add(int index, ExiledCardEntry entry) { super.add(index, entry); arrived(entry); }
        @Override public boolean addAll(java.util.Collection<? extends ExiledCardEntry> entries) { List<ExiledCardEntry> added = List.copyOf(entries); boolean changed = super.addAll(added); added.forEach(this::arrived); return changed; }
        @Override public boolean addAll(int index, java.util.Collection<? extends ExiledCardEntry> entries) { List<ExiledCardEntry> added = List.copyOf(entries); boolean changed = super.addAll(index, added); added.forEach(this::arrived); return changed; }
    }
    /** The pending may abilities, which reports each one queued to {@link #rulesObserver}. */
    private final class ObservedMayAbilityList extends ArrayList<PendingMayAbility> {
        @Override public boolean add(PendingMayAbility ability) { boolean added = super.add(ability); observeTrigger(RulesObserver.Route.MAY_ABILITY, ability); return added; }
        @Override public void add(int index, PendingMayAbility ability) { super.add(index, ability); observeTrigger(RulesObserver.Route.MAY_ABILITY, ability); }
        @Override public boolean addAll(java.util.Collection<? extends PendingMayAbility> abilities) { List<PendingMayAbility> added = List.copyOf(abilities); boolean changed = super.addAll(added); added.forEach(ability -> observeTrigger(RulesObserver.Route.MAY_ABILITY, ability)); return changed; }
        @Override public boolean addAll(int index, java.util.Collection<? extends PendingMayAbility> abilities) { List<PendingMayAbility> added = List.copyOf(abilities); boolean changed = super.addAll(index, added); added.forEach(ability -> observeTrigger(RulesObserver.Route.MAY_ABILITY, ability)); return changed; }
    }

    /** Reports a triggered ability (or the question leading to one) put on {@code route} to {@link #rulesObserver}. */
    public void observeTrigger(RulesObserver.Route route, Object item) {
        if (rulesObserver != null && !rulesEvents.isSuppressed()) {
            rulesObserver.triggerQueued(route, item);
        }
    }

    /** Sets the engine's trigger pipeline, which matches every rules event batch as it closes. */
    public void setRulesEventListener(BiConsumer<GameData, RulesEventBatch> rulesEventListener) {
        this.rulesEventListener = rulesEventListener;
    }

    private void rulesEventBatchClosed(RulesEventBatch batch) {
        BiConsumer<GameData, RulesEventBatch> listener = rulesEventListener;
        if (listener != null) {
            listener.accept(this, batch);
        }
    }

    /**
     * Takes the closed rules event batches for the trigger pipeline (see {@link RulesEventLog#drain}) and shows
     * them to {@link #rulesObserver}.
     */
    public List<RulesEventBatch> drainRulesEvents() {
        List<RulesEventBatch> drained = rulesEvents.drain();
        if (rulesObserver != null && !drained.isEmpty()) {
            rulesObserver.eventsDrained(drained);
        }
        return drained;
    }
    public final Map<UUID, UUID> pendingCommandCasts = new ConcurrentHashMap<>();
    /** While set, {@link #commandCastPlayerId} is casting {@link #commandCastCardId} from the command zone. */
    public UUID commandCastPlayerId;
    public UUID commandCastCardId;
    /** While set, {@link #foreignHandPlayPlayerId} is playing a card from {@link #foreignHandOwnerId}'s hand
     *  (Sen Triplets): the hand cast and land-play paths read that hand instead of the player's own. */
    public UUID foreignHandPlayPlayerId;
    public UUID foreignHandOwnerId;
    /**
     * The cards a hand cast or land play by {@code playerId} takes its card from: the player's command zone during
     * a commander cast, another player's hand during a foreign-hand play, otherwise the player's own hand. The hand
     * cast and land-play paths read the card through this and never through {@link #playerHands}, so one cast path
     * serves every source zone. Costs are still paid from the caster's own pool and hand.
     */
    public List<Card> castingSourceCards(UUID playerId) {
        if (playerId.equals(commandCastPlayerId)) return playerCommandZones.get(playerId);
        if (playerId.equals(foreignHandPlayPlayerId)) return playerHands.get(foreignHandOwnerId);
        return playerHands.get(playerId);
    }
    public boolean isForeignHandPlay(UUID playerId) { return playerId.equals(foreignHandPlayPlayerId); }
    public Zone castingSourceZone(UUID playerId) { return playerId.equals(commandCastPlayerId) ? Zone.COMMAND : Zone.HAND; }
    public DeckFormat format = DeckFormat.CASUAL;
    public final Map<UUID, DeckDefinition> acceptedDecks = new ConcurrentHashMap<>();
    public final Map<UUID, Map<UUID, Integer>> commanderDamageReceived = new ConcurrentHashMap<>();
    public int startingLife() { return format.startingLife(); }
    public boolean isCommander(UUID cardId) {
        return playerCommanders.values().stream().flatMap(List::stream).anyMatch(card -> card.getId().equals(cardId));
    }
    public static final int STARTING_LIFE_TOTAL = 20;

    /**
     * Returns the current life total for the given player, defaulting to 20 if not yet set.
     */
    public int getLife(UUID playerId) {
        return playerLifeTotals.getOrDefault(playerId, startingLife());
    }

    /** Returns the player's maximum life total, or no effective maximum when none is set. */
    public int getMaximumLifeTotal(UUID playerId) {
        // Effects on players apply in timestamp order, so the newest maximum applies (CR 613.10).
        return floatingEffectsOnPlayer(playerId, SetMaximumLifeTotalEffect.class).stream()
                .max(Comparator.comparingLong(FloatingContinuousEffect::timestamp))
                .map(newest -> ((SetMaximumLifeTotalEffect) newest.effect()).maximumLifeTotal())
                .orElse(Integer.MAX_VALUE);
    }

    /** Applies the player's maximum life total to a proposed new life total. */
    public int capLifeTotal(UUID playerId, int lifeTotal) {
        return Math.min(lifeTotal, getMaximumLifeTotal(playerId));
    }

    /**
     * Discards copy-rider mana (Sunken Palace, Primal Wellspring, Pyromancer's Goggles) spent during an action without a spell cast or an
     * ability activated to claim it: mana spent on anything else, such as a tax or a special action,
     * never triggers the rider (CR 106.6).
     */
    public void discardUnclaimedSpentManaRiders() {
        playerManaPools.values().forEach(ManaPool::discardSpentCopyGrantingMana);
    }

    /** Records the current board snapshot for turn-history conditions such as "way behind". */
    public void recordWayBehindState() {
        for (UUID controllerId : orderedPlayerIds) {
            int controllerLife = getLife(controllerId);
            int controllerHandSize = playerHands.getOrDefault(controllerId, List.of()).size();
            int controllerCreatureCount = countCreaturePermanents(controllerId);
            for (UUID opponentId : orderedPlayerIds) {
                if (controllerId.equals(opponentId)) continue;
                if (getLife(opponentId) - controllerLife >= 10
                        || playerHands.getOrDefault(opponentId, List.of()).size() - controllerHandSize >= 3
                        || countCreaturePermanents(opponentId) - controllerCreatureCount >= 3) {
                    playersWhoWereWayBehindThisTurn.add(controllerId);
                    break;
                }
            }
        }
    }

    private int countCreaturePermanents(UUID playerId) {
        return (int) playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE)
                        || animatedByFloatingEffect(permanent.getId())
                        || permanent.isAnimatedUntilNextTurn()
                        || permanent.isPermanentlyAnimated())
                .count();
    }

    /**
     * Returns the other seated player's id in a two-player game, or {@code null} when none is
     * present (single-player / not yet seated).
     */
    public UUID getOpponentId(UUID playerId) {
        for (UUID id : orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Battlefield of {@link #getOpponentId(UUID)}'s opponent. Empty when there is no opponent or
     * that player has no battlefield list yet.
     */
    public List<Permanent> getOpponentBattlefield(UUID playerId) {
        UUID opponentId = getOpponentId(playerId);
        if (opponentId == null) {
            return List.of();
        }
        return playerBattlefields.getOrDefault(opponentId, List.of());
    }

    /**
     * Returns how much life the given player has gained so far this turn (0 if none).
     */
    public int getLifeGainedThisTurn(UUID playerId) {
        return lifeGainedThisTurn.getOrDefault(playerId, 0);
    }

    /**
     * Returns whether the given player has gained life this turn (for Infusion-style conditions).
     */
    public boolean hasGainedLifeThisTurn(UUID playerId) {
        return getLifeGainedThisTurn(playerId) > 0;
    }

    /**
     * Adds a card to the given player's hand.
     */
    public void addCardToHand(UUID playerId, Card card) {
        card = restoreBombardmentCardForZoneChange(card);
        if (deferCommanderZoneMove(card, Zone.HAND, -1)) return;
        endFloatingEffectsOnCardOutside(card.getId(), Zone.HAND);
        countersPreservedAcrossZoneChanges.remove(card.getId());
        // Returning from the graveyard makes the card a new object (CR 400.7), so graveyard permissions
        // and the exile-instead-of-graveyard replacement granted to the old object no longer apply.
        endExileInsteadOfGraveyard(card.getId());
        handEntryVersions.merge(card.getId(), 1L, Long::sum);
        playerHands.get(playerId).add(card);
    }

    /** Makes {@code card} the only commander designated for {@code playerId}, with tax zero. */
    public void makeCommander(UUID playerId, Card card) {
        List<Card> previousCommanders = playerCommanders.put(playerId, new ArrayList<>(List.of(card)));
        if (previousCommanders != null) {
            previousCommanders.forEach(previous -> commanderTaxByCardId.remove(previous.getId()));
        }
        commanderTaxByCardId.put(card.getId(), 0);
    }

    /** Designates an additional commander for the player (e.g. a second commander alongside the first). */
    public void addCommander(UUID playerId, Card card) {
        playerCommanders.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(card);
        commanderTaxByCardId.put(card.getId(), 0);
    }

    private List<Card> asLibrary(UUID playerId, List<Card> cards) {
        if (cards instanceof LibraryCardList) {
            return cards;
        }
        LibraryCardList wrapped = new LibraryCardList(playerId);
        wrapped.addAllWithoutClearing(cards);
        cards.forEach(card -> rulesEvents.track(card, playerId, Zone.LIBRARY));
        return wrapped;
    }

    private final class LibraryMap extends ConcurrentHashMap<UUID, List<Card>> {

        @Override
        public List<Card> put(UUID key, List<Card> value) {
            return super.put(key, asLibrary(key, value));
        }

        @Override
        public List<Card> putIfAbsent(UUID key, List<Card> value) {
            return super.putIfAbsent(key, asLibrary(key, value));
        }

        @Override
        public List<Card> computeIfAbsent(UUID key,
                                          Function<? super UUID, ? extends List<Card>> mappingFunction) {
            return super.computeIfAbsent(key, ignored -> asLibrary(key, mappingFunction.apply(key)));
        }

        @Override
        public void putAll(Map<? extends UUID, ? extends List<Card>> map) {
            map.forEach(this::put);
        }
    }

    private final class LibraryCardList extends ArrayList<Card> {

        private final UUID playerId;

        private LibraryCardList(UUID playerId) {
            this.playerId = playerId;
        }

        private void forgetPreservedCounters(Card card) {
            if (card != null) {
                countersPreservedAcrossZoneChanges.remove(card.getId());
            }
        }

        private boolean addAllWithoutClearing(Collection<? extends Card> cards) {
            return super.addAll(cards);
        }

        @Override
        public boolean add(Card card) {
            if (deferCommanderZoneMove(card, Zone.LIBRARY, size())) return false;
            forgetPreservedCounters(card);
            boolean added = super.add(card);
            rulesEvents.arrived(card, playerId, Zone.LIBRARY);
            return added;
        }

        @Override
        public void add(int index, Card card) {
            if (deferCommanderZoneMove(card, Zone.LIBRARY, index)) return;
            forgetPreservedCounters(card);
            super.add(index, card);
            rulesEvents.arrived(card, playerId, Zone.LIBRARY);
        }

        @Override
        public boolean addAll(Collection<? extends Card> cards) {
            boolean changed = false;
            for (Card card : List.copyOf(cards)) changed |= add(card);
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Card> cards) {
            int before = size();
            for (Card card : List.copyOf(cards)) {
                int oldSize = size(); add(Math.min(index, size()), card);
                if (size() > oldSize) index++;
            }
            return size() != before;
        }

        @Override
        public Card set(int index, Card card) {
            forgetPreservedCounters(card);
            return super.set(index, card);
        }
    }

    /**
     * Iterates over each player's battlefield list in player order.
     * Skips null battlefields.
     */
    public void forEachBattlefield(BiConsumer<UUID, List<Permanent>> action) {
        for (UUID playerId : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            action.accept(playerId, battlefield);
        }
    }

    /**
     * Iterates over every permanent on every battlefield in player order.
     * Skips null battlefields.
     */
    public void forEachPermanent(BiConsumer<UUID, Permanent> action) {
        for (UUID playerId : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                action.accept(playerId, perm);
            }
        }
    }

    /**
     * Returns {@code true} if any permanent on any battlefield matches the given predicate.
     * Short-circuits on the first match.
     */
    public boolean anyPermanentMatches(Predicate<Permanent> predicate) {
        for (UUID playerId : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (predicate.test(perm)) return true;
            }
        }
        return false;
    }

    /** Applies Bombardment's temporary characteristic replacement to the controller's card objects. */
    public void applyBombardment(UUID controllerId, StackEntry resolvingEntry) {
        Map<UUID, Card> replacements = new HashMap<>();
        transformZoneForBombardment(playerHands.get(controllerId), controllerId, replacements);
        transformZoneForBombardment(playerDecks.get(controllerId), controllerId, replacements);
        transformZoneForBombardment(playerGraveyards.get(controllerId), controllerId, replacements);
        transformZoneForBombardment(playerSideboards.get(controllerId), controllerId, replacements);
        transformZoneForBombardment(playerCommandZones.get(controllerId), controllerId, replacements);
        transformZoneForBombardment(playerCommanders.get(controllerId), controllerId, replacements);
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry entry = exiledCards.get(i);
                if (controllerId.equals(entry.ownerId())) {
                    Card transformed = transformCardForBombardment(entry.card(), replacements);
                    if (transformed != entry.card()) {
                        exiledCards.set(i, new ExiledCardEntry(transformed, entry.ownerId(),
                                entry.sourcePermanentId(), entry.faceDown(), entry.exilerId(),
                                entry.exiledTurnNumber(), entry.controllerTurnsTakenAtExile(), entry.abilityLink()));
                    }
                }
            }
        }
        synchronized (stack) {
            for (StackEntry entry : List.copyOf(stack)) {
                transformStackEntryForBombardment(entry, controllerId, replacements, true);
            }
        }
        if (resolvingEntry != null) {
            transformStackEntryForBombardment(resolvingEntry, controllerId, replacements, false);
        }
    }

    private void transformZoneForBombardment(List<Card> zone, UUID controllerId,
                                             Map<UUID, Card> replacements) {
        if (zone == null) return;
        for (int i = 0; i < zone.size(); i++) {
            Card card = zone.get(i);
            Card transformed = transformCardForBombardment(card, replacements);
            if (transformed != card) {
                zone.set(i, transformed);
            }
        }
    }

    private void transformStackEntryForBombardment(StackEntry entry, UUID controllerId,
                                                   Map<UUID, Card> replacements,
                                                   boolean convertSpellState) {
        if (!controllerId.equals(entry.getOwnerId())) return;
        Card castCard = entry.getCastCard();
        Card physicalCard = entry.getPhysicalCard();
        Card bestowCard = entry.getBestowOriginalCard();
        Card transformed = castCard == null
                ? transformCardForBombardment(physicalCard, replacements)
                : transformCardForBombardment(castCard, replacements);
        Card transformedPhysical = transformCardForBombardment(physicalCard, replacements);
        Card transformedBestow = transformCardForBombardment(bestowCard, replacements);
        if (transformed == null && transformedPhysical == null && transformedBestow == null) return;
        Card missile = transformed != null ? transformed
                : transformedPhysical != null ? transformedPhysical : transformedBestow;
        entry.setCastCard(missile);
        entry.setPhysicalCard(transformedPhysical != null ? transformedPhysical : missile);
        if (transformedBestow != null) {
            entry.setBestowOriginalCard(transformedBestow);
        }
        if (convertSpellState && isSpellEntry(entry.getEntryType())) {
            entry.convertToBombardmentMissileSpell(missile.getCastingCostsAndSpellEffects());
        }
    }

    private Card transformCardForBombardment(Card card, Map<UUID, Card> replacements) {
        if (card == null || isCardOnBattlefield(card.getId())) return null;
        Card replacement = replacements.get(card.getId());
        if (replacement != null) return replacement;
        Card original = bombardmentOriginalCardsUntilEndOfTurn.putIfAbsent(card.getId(), card);
        if (original == null) original = card;
        Card missile = original.createRuntimeCopy();
        missile.becomeMissile();
        missile.freeze();
        replacements.put(card.getId(), missile);
        return missile;
    }

    private boolean isCardOnBattlefield(UUID cardId) {
        if (cardId == null) return false;
        for (List<Permanent> battlefield : playerBattlefields.values()) {
            if (battlefield != null && battlefield.stream().anyMatch(
                    permanent -> permanent.getCard() != null && cardId.equals(permanent.getCard().getId()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpellEntry(StackEntryType type) {
        return type == StackEntryType.CREATURE_SPELL || type == StackEntryType.ENCHANTMENT_SPELL
                || type == StackEntryType.ARTIFACT_SPELL || type == StackEntryType.PLANESWALKER_SPELL
                || type == StackEntryType.BATTLE_SPELL || type == StackEntryType.SORCERY_SPELL
                || type == StackEntryType.INSTANT_SPELL;
    }

    public void restoreBombardmentCardForBattlefield(Permanent permanent) {
        Card original = permanent == null || permanent.getCard() == null
                ? null : bombardmentOriginalCardsUntilEndOfTurn.get(permanent.getCard().getId());
        if (original != null) {
            permanent.restoreBombardmentCard(original);
        }
    }

    /** Ends Bombardment's characteristic replacement when the card changes zones. */
    public Card restoreBombardmentCardForZoneChange(Card card) {
        Card original = bombardmentOriginalCardsUntilEndOfTurn.remove(card.getId());
        return original != null ? original : card;
    }

    /** Randomized library cards become new objects and lose their Missile characteristics. */
    public void restoreBombardmentLibraryCards(UUID playerId) {
        List<Card> deck = playerDecks.get(playerId);
        if (deck == null) return;
        for (int i = 0; i < deck.size(); i++) {
            deck.set(i, restoreBombardmentCardForZoneChange(deck.get(i)));
        }
    }

    /** Restores all temporary Bombardment replacements at cleanup. */
    public void restoreBombardmentCards() {
        restoreZoneFromBombardment(playerHands);
        restoreZoneFromBombardment(playerDecks);
        restoreZoneFromBombardment(playerGraveyards);
        restoreZoneFromBombardment(playerSideboards);
        restoreZoneFromBombardment(playerCommandZones);
        restoreZoneFromBombardment(playerCommanders);
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry entry = exiledCards.get(i);
                Card original = bombardmentOriginalCardsUntilEndOfTurn.get(entry.card().getId());
                if (original != null) {
                    exiledCards.set(i, new ExiledCardEntry(original, entry.ownerId(), entry.sourcePermanentId(),
                            entry.faceDown(), entry.exilerId(), entry.exiledTurnNumber(),
                            entry.controllerTurnsTakenAtExile(), entry.abilityLink()));
                }
            }
        }
        synchronized (stack) {
            for (StackEntry entry : stack) {
                restoreStackEntryFromBombardment(entry);
            }
        }
        bombardmentOriginalCardsUntilEndOfTurn.clear();
    }

    private void restoreZoneFromBombardment(Map<UUID, List<Card>> zones) {
        for (List<Card> zone : zones.values()) {
            for (int i = 0; i < zone.size(); i++) {
                Card original = bombardmentOriginalCardsUntilEndOfTurn.get(zone.get(i).getId());
                if (original != null) zone.set(i, original);
            }
        }
    }

    private void restoreStackEntryFromBombardment(StackEntry entry) {
        Card castCard = entry.getCastCard();
        Card original = castCard == null ? null : bombardmentOriginalCardsUntilEndOfTurn.get(castCard.getId());
        if (original != null) entry.setCastCard(original);
        Card physicalCard = entry.getPhysicalCard();
        original = physicalCard == null ? null : bombardmentOriginalCardsUntilEndOfTurn.get(physicalCard.getId());
        if (original != null) entry.setPhysicalCard(original);
        Card bestowCard = entry.getBestowOriginalCard();
        original = bestowCard == null ? null : bombardmentOriginalCardsUntilEndOfTurn.get(bestowCard.getId());
        if (original != null) entry.setBestowOriginalCard(original);
        entry.restoreBombardmentSpellState();
    }

    // ===== Exile zone helpers =====

    /** Returns cards in a player's exile zone (by owner). Never null. */
    public List<Card> getPlayerExiledCards(UUID ownerId) {
        return exiledCards.stream()
                .filter(e -> e.ownerId().equals(ownerId) && !antedCardIds.contains(e.card().getId()))
                .map(ExiledCardEntry::card)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /** Returns cards exiled by a specific permanent (by source permanent ID). Never null. */
    public List<Card> getCardsExiledByPermanent(UUID sourcePermanentId) {
        if (sourcePermanentId == null) return new ArrayList<>();
        return exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .map(ExiledCardEntry::card)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /** Re-links cards exiled with a permanent to the new permanent object after a craft return. */
    public void transferCardsExiledByPermanent(UUID oldSourcePermanentId, UUID newSourcePermanentId) {
        if (oldSourcePermanentId == null || newSourcePermanentId == null
                || oldSourcePermanentId.equals(newSourcePermanentId)) {
            return;
        }
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry entry = exiledCards.get(i);
                if (oldSourcePermanentId.equals(entry.sourcePermanentId())) {
                    exiledCards.set(i, new ExiledCardEntry(
                            entry.card(), entry.ownerId(), newSourcePermanentId, entry.faceDown(),
                            entry.exilerId(), entry.exiledTurnNumber(), entry.controllerTurnsTakenAtExile(), entry.abilityLink()));
                }
            }
        }
    }

    /** Adds a card to exile without source tracking. */
    public void addToExile(UUID ownerId, Card card) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, null, false, turnNumber));
        notifyCardsExiled(card, false);
    }

    /**
     * Adds a card to the ante zone, represented by an untracked exile entry. It is marked first, so the exile list
     * doesn't report it as put into exile: anteing puts it into the ante zone (CR 407.4).
     */
    public void addToAnte(UUID ownerId, Card card) {
        markCardAsAnted(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, null, false, turnNumber));
    }

    /** Marks an exiled card as being in the ante zone. */
    public void markCardAsAnted(Card card) {
        if (card != null) {
            antedCardIds.add(card.getId());
        }
    }

    /** Adds a card to exile with source permanent tracking. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, false, turnNumber));
        notifyCardsExiled(card, false);
    }

    /** Associates an already-exiled card with a source permanent without creating a second exile event. */
    public boolean associateExiledCardWithSource(UUID cardId, UUID sourcePermanentId) {
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry exiled = exiledCards.get(i);
                if (exiled.card().getId().equals(cardId)) {
                    exiledCards.set(i, new ExiledCardEntry(exiled.card(), exiled.ownerId(),
                            sourcePermanentId, exiled.faceDown(), exiled.exilerId(),
                            exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(), exiled.abilityLink()));
                    return true;
                }
            }
        }
        return false;
    }

    /** Marks an existing exile entry with a fetch counter and records the player who exiled it. */
    public boolean markExiledCardWithFetchCounter(UUID cardId, UUID exilerId) {
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry exiled = exiledCards.get(i);
                if (exiled.card().getId().equals(cardId)) {
                    exiledCards.set(i, new ExiledCardEntry(exiled.card(), exiled.ownerId(),
                            exiled.sourcePermanentId(), exiled.faceDown(), exilerId,
                            exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(), exiled.abilityLink()));
                    exiledCardsWithFetchCounters.add(cardId);
                    return true;
                }
            }
        }
        return false;
    }

    /** Adds a card to exile with a stash counter. Stash counters are independent of any source permanent. */
    public void addToExileWithStashCounter(UUID ownerId, Card card) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        addToExile(ownerId, card);
        stashCounterCardIds.add(card.getId());
    }

    /** Adds a card to exile and marks it with an ice counter. */
    public void addToExileWithIceCounter(UUID ownerId, Card card) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        addToExile(ownerId, card);
        exiledCardsWithIceCounters.add(card.getId());
    }

    /** Adds a card to exile and marks it with a croak counter. */
    public void addToExileWithCroakCounter(UUID ownerId, Card card) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        addToExile(ownerId, card);
        exiledCardsWithCroakCounters.add(card.getId());
    }

    /** Adds a card to exile and marks it with a void counter. */
    public void addToExileWithVoidCounter(UUID ownerId, Card card) {
        addToExile(ownerId, card);
        exiledCardsWithVoidCounters.add(card.getId());
    }

    public void addToExileWithVoidCounter(UUID ownerId, Card card, UUID sourcePermanentId) {
        addToExile(ownerId, card, sourcePermanentId, false);
        exiledCardsWithVoidCounters.add(card.getId());
    }

    public void addToExileWithDiscoveryCounter(UUID ownerId, Card card) {
        addToExile(ownerId, card);
        exiledCardsWithDiscoveryCounters.add(card.getId());
    }

    /** Adds a card to exile with a fetch counter and records the player whose ability exiled it. */
    public void addToExileWithFetchCounter(UUID ownerId, Card card, UUID exilerId) {
        addToExileWithFetchCounter(ownerId, card, null, exilerId);
    }

    /** Adds a card to exile with a fetch counter and source-permanent tracking. */
    public void addToExileWithFetchCounter(UUID ownerId, Card card, UUID sourcePermanentId,
                                           UUID exilerId) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        addToExile(ownerId, card, sourcePermanentId, false, exilerId);
        exiledCardsWithFetchCounters.add(card.getId());
    }

    /** Adds a card to exile with a collection counter and records the exiling ability's controller. */
    public void addToExileWithCollectionCounter(UUID ownerId, Card card, UUID exilerId) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, null, false, exilerId, turnNumber));
        exiledCardsWithCollectionCounters.add(card.getId());
        notifyCardsExiled(card, false);
    }

    /** Adds a card to exile with source permanent tracking and an explicit face-down status. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId, boolean faceDown) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, faceDown, turnNumber));
        notifyCardsExiled(card, faceDown);
    }

    /** Adds a card to exile with source tracking, face-down status, and its exiling player. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId, boolean faceDown,
                           UUID exilerId) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        spellsWithPlotOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(ownerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, faceDown, exilerId));
        notifyCardsExiled(card, faceDown);
    }

    /** Exiles a card from hand face down as a foretell special action. */
    public void addForetoldCardToExile(UUID playerId, Card card) {
        addForetoldCardToExile(playerId, card, null);
    }

    /** Exiles a card from hand face down and remembers the foretell cost used for that action. */
    public void addForetoldCardToExile(UUID playerId, Card card, ManaCost foretellCost) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        if (putOnBottomOfLibraryInsteadOfExile(playerId, card)) return;
        commanderEnteredReturnZone(card);
        exiledCards.add(new ExiledCardEntry(card, playerId, null, true, playerId, turnNumber,
                turnsTakenByPlayer.getOrDefault(playerId, 0)));
        foretoldCardIds.add(card.getId());
        foretoldCardTurnsTaken.put(card.getId(), Map.copyOf(turnsTakenByPlayer));
        if (foretellCost != null) {
            foretoldCardCosts.put(card.getId(), foretellCost);
        }
        notifyCardsExiled(card, true);
    }

    /** Marks an already-exiled card as foretold without creating a second exile event. */
    public boolean markExiledCardAsForetold(UUID cardId, UUID foretellingPlayerId,
                                            ManaCost foretellCost) {
        synchronized (exiledCards) {
            for (int i = 0; i < exiledCards.size(); i++) {
                ExiledCardEntry entry = exiledCards.get(i);
                if (!entry.card().getId().equals(cardId)) continue;
                exiledCards.set(i, new ExiledCardEntry(entry.card(), entry.ownerId(),
                        entry.sourcePermanentId(), true, foretellingPlayerId, turnNumber,
                        foretellingPlayerId == null
                                ? -1
                                : turnsTakenByPlayer.getOrDefault(foretellingPlayerId, 0)));
                foretoldCardIds.add(cardId);
                foretoldCardTurnsTaken.put(cardId, Map.copyOf(turnsTakenByPlayer));
                if (foretellCost != null) {
                    foretoldCardCosts.put(cardId, foretellCost);
                }
                return true;
            }
        }
        return false;
    }

    private boolean putOnBottomOfLibraryInsteadOfExile(UUID ownerId, Card card) {
        if (card.isToken()
                || !hasFloatingEffectOnPlayer(ownerId,
                com.github.laxika.magicalvibes.model.effect.PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect.class)) {
            return false;
        }
        List<Card> deck = playerDecks.get(ownerId);
        if (deck == null) return false;
        deck.add(card);
        return true;
    }

    /**
     * Exile entries displayed tucked under the given permanent: its imprinted card (if still in
     * exile) plus all entries tracked with the permanent as source. An imprinted card that is
     * also source-tracked (e.g. Ixalan's Binding records both) appears only once.
     */
    public List<ExiledCardEntry> getExiledWithPermanentEntries(UUID permanentId, UUID permanentCardId) {
        List<ExiledCardEntry> entries = new ArrayList<>();
        Card imprinted = permanentCardId != null ? imprintedCards.get(permanentCardId) : null;
        if (imprinted != null) {
            ExiledCardEntry imprintEntry = findExiledCard(imprinted.getId());
            if (imprintEntry != null) {
                entries.add(imprintEntry);
            }
        }
        if (permanentId != null) {
            for (ExiledCardEntry e : exiledCards) {
                if (permanentId.equals(e.sourcePermanentId())
                        && (imprinted == null || !e.card().getId().equals(imprinted.getId()))) {
                    entries.add(e);
                }
            }
        }
        return entries;
    }

    /** Registers a pending exile-return linked to a source permanent (O-ring style).
     *  When the source leaves the battlefield, all pending returns for it are processed. */
    public void addExileReturnOnPermanentLeave(UUID sourcePermanentId, PendingExileReturn pending) {
        exileReturnOnPermanentLeave.computeIfAbsent(sourcePermanentId, k -> new ArrayList<>()).add(pending);
    }

    /** Registers a pending exile return for the next monarch change to an opponent of {@code controllerId}. */
    public void addExileReturnOnOpponentBecomesMonarch(UUID controllerId, PendingExileReturn pending) {
        exileReturnOnOpponentBecomesMonarch.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(pending);
    }

    /** Removes an exiled card by card ID. Returns true if found and removed. */
    public boolean removeFromExile(UUID cardId) {
        boolean removed = exiledCards.removeIf(e -> e.card().getId().equals(cardId));
        if (removed) {
            // A card that leaves exile is a new object (CR 400.7): delay-counter tracking from its
            // earlier exile no longer applies, even if it is exiled again later.
            delayedSpellExiles.removeIf(pending -> pending.cardId().equals(cardId));
            exiledVoyageCounters.remove(cardId);
            exiledVoyageControllerIds.remove(cardId);
            hauntingCardToPermanentId.remove(cardId);
            foretoldCardIds.remove(cardId);
            foretoldCardCosts.remove(cardId);
            foretoldCardTurnsTaken.remove(cardId);
            antedCardIds.remove(cardId);
            stashCounterCardIds.remove(cardId);
            exiledCardScreamCounters.remove(cardId);
            exiledCardsWithSilverCounters.remove(cardId);
            exiledCardsWithIceCounters.remove(cardId);
            exiledCardsWithBloodCounters.remove(cardId);
            exiledCardsWithCroakCounters.remove(cardId);
            exiledCardsWithVoidCounters.remove(cardId);
            exiledCardsWithDiscoveryCounters.remove(cardId);
            exiledCardsWithCollectionCounters.remove(cardId);
            exiledCardsWithFetchCounters.remove(cardId);
            exiledCardsWithHatchingCounters.remove(cardId);
            exiledCardsWithStudyCounters.remove(cardId);
            exiledCardsWithIntelCounters.remove(cardId);
            exiledCardsWithKickCounters.remove(cardId);
            exiledCardsWithMemoryCounters.remove(cardId);
            exiledCardsWithTakeoverCounters.remove(cardId);
            exiledCardsWithBrainCounters.remove(cardId);
            exiledCardsWithCageCounters.remove(cardId);
            exiledCardRefineCounters.remove(cardId);
            plottedCardIds.remove(cardId);
            endExileInsteadOfGraveyard(cardId);
            cardsExiledFromGraveyardThisTurn.remove(cardId);
            exiledCardTimeCounters.remove(cardId);
            exiledCardsWithNonSuspendTimeCounters.remove(cardId);
            exiledCardsWithAlaundoCastAbility.remove(cardId);
            exiledCardHitCounters.remove(cardId);
            suspendedSpellExiles.removeIf(pending -> cardId.equals(pending.cardId()));
            // Whatever it was granted while exiled ends as it leaves: it becomes a new object (CR 400.7).
            endFloatingEffectsOnCard(cardId, Zone.EXILE);
            // A grant that lasts for as long as its card remains exiled ends as the card leaves (CR 611.2b).
            expireFloatingEffects(fe -> Duration.WHILE_SOURCE_CARD_EXILED.equals(fe.duration())
                    && fe.effect() instanceof com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect grant
                    && cardId.equals(grant.expirationCardId()));
        }
        return removed;
    }

    /**
     * Registers a shared quota for a group of exiled cards that may be played ("you may play one of them"): each
     * card's play permission counts against {@code groupId}, which allows {@code maxUses} plays in all. A card
     * without a play permission is not part of the group.
     */
    public synchronized void registerExilePlayPermissionGroup(UUID groupId, int maxUses,
                                                               Collection<UUID> cardIds) {
        if (maxUses < 0) {
            throw new IllegalArgumentException("maximum exile plays must not be negative");
        }
        exilePlayPermissionGroupUsesRemaining.put(groupId, maxUses);
        for (UUID cardId : cardIds) {
            updateExilePlayPermission(cardId, permission -> permission.withGroupId(groupId));
        }
    }

    /** Returns whether a card's shared limited exile-play permission still has a use available. */
    public synchronized boolean hasExilePlayPermissionRemaining(UUID cardId) {
        UUID groupId = exilePlayPermissionGroupId(cardId);
        return groupId == null
                || exilePlayPermissionGroupUsesRemaining.getOrDefault(groupId, 0) > 0;
    }

    /** Adds a one-spell-per-card-type restriction to an existing exile permission group. */
    public synchronized void registerExileSpellTypePermissionGroup(UUID groupId) {
        exileSpellTypePermissionHistory.put(groupId, new ArrayList<>());
    }

    /** Checks whether the spell and earlier spells can each use a distinct permitted card type. */
    public synchronized boolean hasExileSpellTypePermission(UUID cardId, Card spell) {
        UUID groupId = exilePlayPermissionGroupId(cardId);
        List<Set<CardType>> history = groupId == null ? null : exileSpellTypePermissionHistory.get(groupId);
        if (history == null) return true;
        List<Set<CardType>> choices = new ArrayList<>(history);
        choices.add(exilePermissionSpellTypes(spell));
        return assignExilePermissionSpellTypes(choices, 0, EnumSet.noneOf(CardType.class));
    }

    /** Records a spell cast using a one-spell-per-card-type exile permission. */
    public synchronized boolean consumeExileSpellTypePermission(UUID cardId, Card spell) {
        if (!hasExileSpellTypePermission(cardId, spell)) return false;
        UUID groupId = exilePlayPermissionGroupId(cardId);
        List<Set<CardType>> history = groupId == null ? null : exileSpellTypePermissionHistory.get(groupId);
        if (history != null) history.add(exilePermissionSpellTypes(spell));
        return true;
    }

    private Set<CardType> exilePermissionSpellTypes(Card spell) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        for (CardType type : List.of(CardType.ARTIFACT, CardType.BATTLE, CardType.CREATURE,
                CardType.ENCHANTMENT, CardType.INSTANT, CardType.KINDRED,
                CardType.PLANESWALKER, CardType.SORCERY)) {
            if (spell.hasType(type)) types.add(type);
        }
        return types;
    }

    private boolean assignExilePermissionSpellTypes(List<Set<CardType>> choices, int index,
                                                   Set<CardType> assigned) {
        if (index == choices.size()) return true;
        for (CardType type : choices.get(index)) {
            if (!assigned.add(type)) continue;
            if (assignExilePermissionSpellTypes(choices, index + 1, assigned)) return true;
            assigned.remove(type);
        }
        return false;
    }

    /** Consumes one use of a card's shared limited exile-play permission, if it has one. */
    public synchronized boolean consumeExilePlayPermission(UUID cardId) {
        UUID groupId = exilePlayPermissionGroupId(cardId);
        if (groupId == null) {
            return true;
        }
        int remaining = exilePlayPermissionGroupUsesRemaining.getOrDefault(groupId, 0);
        if (remaining <= 0) {
            return false;
        }
        exilePlayPermissionGroupUsesRemaining.put(groupId, remaining - 1);
        return true;
    }

    /** The quota group the card's play permission counts against, or {@code null}. */
    private UUID exilePlayPermissionGroupId(UUID cardId) {
        return exilePlayPermission(cardId).map(ExilePlayPermissionEffect::groupId).orElse(null);
    }

    /**
     * Drops the quota of a group none of whose permissions remains, as the last of them ends. A quota is the count of
     * plays a group of permissions shares, not an effect of its own, so it lives only while a permission names it.
     */
    private void pruneExilePlayPermissionGroups(List<FloatingContinuousEffect> ended) {
        for (FloatingContinuousEffect fe : ended) {
            if (!(fe.effect() instanceof ExilePlayPermissionEffect permission) || permission.groupId() == null) {
                continue;
            }
            UUID groupId = permission.groupId();
            if (!floatingEffectsOfTypes(ExilePlayPermissionEffect.class).stream()
                    .anyMatch(other -> groupId.equals(((ExilePlayPermissionEffect) other.effect()).groupId()))) {
                exilePlayPermissionGroupUsesRemaining.remove(groupId);
                exileSpellTypePermissionHistory.remove(groupId);
            }
        }
    }

    /** The floating effect that is the card's play permission, begun or still waiting to begin, or {@code null}. */
    private FloatingContinuousEffect findExilePlayPermissionEffect(UUID cardId, boolean includeScheduled) {
        synchronized (floatingEffects) {
            for (FloatingContinuousEffect fe : floatingEffects) {
                if (cardId.equals(fe.affectedCardId()) && fe.effect() instanceof ExilePlayPermissionEffect) {
                    return fe;
                }
            }
        }
        if (includeScheduled) {
            synchronized (scheduledFloatingEffects) {
                for (FloatingContinuousEffect fe : scheduledFloatingEffects) {
                    if (cardId.equals(fe.affectedCardId()) && fe.effect() instanceof ExilePlayPermissionEffect) {
                        return fe;
                    }
                }
            }
        }
        return null;
    }

    /** The permission to play the exiled card {@code cardId} that applies now, if any. */
    public Optional<ExilePlayPermissionEffect> exilePlayPermission(UUID cardId) {
        FloatingContinuousEffect fe = findExilePlayPermissionEffect(cardId, false);
        return fe == null ? Optional.empty() : Optional.of((ExilePlayPermissionEffect) fe.effect());
    }

    /** The player who holds the play permission that applies now to the exiled card {@code cardId}, or {@code null}. */
    public UUID exilePlayPermissionHolder(UUID cardId) {
        return exilePlayPermission(cardId).map(ExilePlayPermissionEffect::holderId).orElse(null);
    }

    /** The duration of the play permission on the exiled card {@code cardId}, or {@code null} when it has none. */
    public Duration exilePlayPermissionDuration(UUID cardId) {
        FloatingContinuousEffect fe = findExilePlayPermissionEffect(cardId, true);
        return fe == null ? null : fe.duration();
    }

    /**
     * Grants {@code holderId} the permission to play the exiled card for {@code duration}, in place of any permission
     * the card has. "Your" in a duration (the next turn, the next end step) is {@code holderId}'s.
     */
    public FloatingContinuousEffect grantExilePlayPermission(UUID cardId, UUID holderId, Duration duration) {
        return grantExilePlayPermission(cardId, new ExilePlayPermissionEffect(holderId), null, holderId, duration);
    }

    /**
     * Grants {@code holderId} the permission to play the exiled card for {@code duration}, which names the turn or end
     * step of {@code durationPlayerId} ("until the end of target player's next turn").
     */
    public FloatingContinuousEffect grantExilePlayPermission(UUID cardId, UUID holderId, UUID durationPlayerId,
                                                              Duration duration) {
        return grantExilePlayPermission(cardId, new ExilePlayPermissionEffect(holderId), null, durationPlayerId,
                duration);
    }

    /**
     * Grants {@code permission} on the exiled card for {@code duration}, in place of any permission the card has.
     * {@code sourcePermanentId} is the permanent a "for as long as you control it" duration follows, and
     * {@code controllerId} the player a duration's "your" names.
     */
    public FloatingContinuousEffect grantExilePlayPermission(UUID cardId, ExilePlayPermissionEffect permission,
                                                              UUID sourcePermanentId, UUID controllerId,
                                                              Duration duration) {
        endExilePlayPermission(cardId);
        return addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), sourcePermanentId,
                controllerId, permission, cardId, Zone.EXILE, duration));
    }

    /** Changes the play permission on the exiled card (begun or not); returns whether the card has one. */
    public boolean updateExilePlayPermission(UUID cardId,
                                             java.util.function.UnaryOperator<ExilePlayPermissionEffect> change) {
        FloatingContinuousEffect fe = findExilePlayPermissionEffect(cardId, true);
        if (fe == null) {
            return false;
        }
        ExilePlayPermissionEffect updated = change.apply((ExilePlayPermissionEffect) fe.effect());
        if (updated.equals(fe.effect())) {
            return true;
        }
        FloatingContinuousEffect replacement = new FloatingContinuousEffect(fe.id(), fe.sourceCardName(),
                fe.sourcePermanentId(), fe.controllerId(), updated, null, null, null, fe.duration(),
                fe.timestamp(), false, cardId, Zone.EXILE);
        synchronized (scheduledFloatingEffects) {
            int index = scheduledFloatingEffects.indexOf(fe);
            if (index >= 0) {
                scheduledFloatingEffects.set(index, replacement);
                return true;
            }
        }
        replaceFloatingEffect(fe, replacement);
        return true;
    }

    /** Ends the play permission on the exiled card, begun or not; returns whether it had one. */
    public boolean endExilePlayPermission(UUID cardId) {
        boolean had = findExilePlayPermissionEffect(cardId, true) != null;
        scheduledFloatingEffects.removeIf(fe -> cardId.equals(fe.affectedCardId())
                && fe.effect() instanceof ExilePlayPermissionEffect);
        expireFloatingEffects(fe -> cardId.equals(fe.affectedCardId())
                && fe.effect() instanceof ExilePlayPermissionEffect);
        return had;
    }

    /** Ends the play permissions of the cards a source permanent granted "until it exiles another card". */
    public void endExilePlayPermissionsUntilAnotherCard(UUID sourcePermanentId) {
        expireFloatingEffects(fe -> sourcePermanentId.equals(fe.sourcePermanentId())
                && fe.effect() instanceof ExilePlayPermissionEffect permission && permission.untilAnotherCard());
    }

    /** The cards that have a play permission now, each with the player who holds it. */
    public Map<UUID, UUID> exilePlayPermissionHolders() {
        Map<UUID, UUID> holders = new HashMap<>();
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(ExilePlayPermissionEffect.class)) {
            holders.put(fe.affectedCardId(), ((ExilePlayPermissionEffect) fe.effect()).holderId());
        }
        return holders;
    }

    /** The cards whose play permission has {@code duration}, begun or waiting to begin. */
    public Set<UUID> exilePlayCardsWithDuration(Duration duration) {
        return exilePlayDurationPlayers(duration).keySet();
    }

    /**
     * The cards whose play permission has {@code duration}, begun or waiting to begin, each with the player the
     * duration's "your" names.
     */
    public Map<UUID, UUID> exilePlayDurationPlayers(Duration duration) {
        Map<UUID, UUID> players = new HashMap<>();
        List<FloatingContinuousEffect> all = new ArrayList<>(floatingEffects);
        all.addAll(scheduledFloatingEffects);
        for (FloatingContinuousEffect fe : all) {
            if (fe.effect() instanceof ExilePlayPermissionEffect && duration.equals(fe.duration())) {
                players.put(fe.affectedCardId(), fe.controllerId());
            }
        }
        return players;
    }

    /** The cards whose play permission lets them be played without paying their mana cost. */
    public Set<UUID> exilePlayWithoutPayingCardIds() {
        Set<UUID> cardIds = exilePlayCardIds(ExilePlayPermissionEffect::withoutPayingManaCost);
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(ExileCastWithoutPayingManaCostEffect.class)) {
            cardIds.add(fe.affectedCardId());
        }
        return cardIds;
    }

    /** The cards that may be cast spending mana of any type, by their play permission or on their own. */
    public Set<UUID> exilePlayAnyManaTypeCardIds() {
        Set<UUID> cardIds = exilePlayCardIds(ExilePlayPermissionEffect::anyManaType);
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(ExileAnyManaTypeEffect.class)) {
            cardIds.add(fe.affectedCardId());
        }
        return cardIds;
    }

    /** The cards whose play permission lets them be cast by paying life equal to their mana value. */
    public Set<UUID> exilePlayForLifeCardIds() {
        return exilePlayCardIds(ExilePlayPermissionEffect::forLifeEqualToManaValue);
    }

    private Set<UUID> exilePlayCardIds(java.util.function.Predicate<ExilePlayPermissionEffect> matches) {
        Set<UUID> cardIds = new HashSet<>();
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(ExilePlayPermissionEffect.class)) {
            if (matches.test((ExilePlayPermissionEffect) fe.effect())) {
                cardIds.add(fe.affectedCardId());
            }
        }
        return cardIds;
    }

    /**
     * Lets mana of any type be spent to cast the exiled card {@code cardId} for {@code duration}, whichever permission
     * lets it be cast. {@code controllerId} is the player a duration's "your" names.
     */
    public void grantExileAnyManaType(UUID cardId, UUID controllerId, Duration duration) {
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), null, controllerId,
                new ExileAnyManaTypeEffect(), cardId, Zone.EXILE, duration));
    }

    /** Lets mana of any type be spent to cast the exiled card {@code cardId} for {@code duration}. */
    public void grantExileAnyManaType(UUID cardId, Duration duration) {
        grantExileAnyManaType(cardId, null, duration);
    }

    /** Whether mana of any type may be spent to cast the exiled card {@code cardId} whichever permission casts it. */
    public boolean hasExileAnyManaType(UUID cardId) {
        return hasFloatingEffectOnCard(cardId, ExileAnyManaTypeEffect.class);
    }

    /**
     * Changes the cost of playing the exiled card {@code cardId} by {@code modifier}: part of the card's play
     * permission when it has one, otherwise an effect of its own that lasts as long as the card remains exiled. A
     * {@code null} modifier removes it.
     */
    public void setExilePlayCostModifier(UUID cardId, ExilePlayCostModifier modifier) {
        endFloatingEffectsOnCard(cardId, ExilePlayCostModifier.class);
        if (updateExilePlayPermission(cardId, granted -> granted.withCostModifier(modifier)) || modifier == null) {
            return;
        }
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), null, modifier.permittedPlayerId(),
                modifier, cardId, Zone.EXILE, Duration.PERMANENT));
    }

    /**
     * Lets the exiled card {@code cardId} be cast without paying its mana cost, or withdraws that: part of the card's
     * play permission when it has one, otherwise an effect of its own that lasts as long as the card remains exiled.
     */
    public void setExilePlayWithoutPayingManaCost(UUID cardId, boolean withoutPaying) {
        endFloatingEffectsOnCard(cardId, ExileCastWithoutPayingManaCostEffect.class);
        if (updateExilePlayPermission(cardId, granted -> granted.withWithoutPayingManaCost(withoutPaying))
                || !withoutPaying) {
            return;
        }
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), null, null,
                new ExileCastWithoutPayingManaCostEffect(), cardId, Zone.EXILE, Duration.PERMANENT));
    }

    /** Whether the exiled card {@code cardId} may be cast without paying its mana cost, by its permission or alone. */
    public boolean exilePlayWithoutPayingManaCost(UUID cardId) {
        return exilePlayPermission(cardId).filter(ExilePlayPermissionEffect::withoutPayingManaCost).isPresent()
                || hasFloatingEffectOnCard(cardId, ExileCastWithoutPayingManaCostEffect.class);
    }

    /** The cost change that applies to playing the exiled card {@code cardId}, or {@code null}. */
    public ExilePlayCostModifier exilePlayCostModifier(UUID cardId) {
        ExilePlayCostModifier fromPermission = exilePlayPermission(cardId)
                .map(ExilePlayPermissionEffect::costModifier).orElse(null);
        if (fromPermission != null) {
            return fromPermission;
        }
        List<FloatingContinuousEffect> own = floatingEffectsOnCard(cardId, ExilePlayCostModifier.class);
        return own.isEmpty() ? null : (ExilePlayCostModifier) own.getFirst().effect();
    }

    /** The name an effect on the exiled card {@code cardId} shows as its source: the card's own. */
    private String exiledCardName(UUID cardId) {
        ExiledCardEntry entry = findExiledCard(cardId);
        return entry != null ? entry.card().getName() : "an exiled card";
    }

    /**
     * Lets {@code viewerId} look at the face-down exiled card {@code cardId} for as long as it remains exiled
     * (CR 406.3). A viewer stays until the card leaves exile, also after the source that granted it is gone.
     */
    public void grantExileLookPermission(UUID cardId, UUID viewerId) {
        if (!mayLookAtExiledCard(cardId, viewerId)) {
            addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), null, viewerId,
                    new ExileLookPermissionEffect(viewerId), cardId, Zone.EXILE, Duration.PERMANENT));
        }
    }

    /** The players who may look at the face-down exiled card {@code cardId}. */
    public Set<UUID> exileLookViewers(UUID cardId) {
        Set<UUID> viewers = new LinkedHashSet<>();
        for (FloatingContinuousEffect fe : floatingEffectsOnCard(cardId, ExileLookPermissionEffect.class)) {
            viewers.add(((ExileLookPermissionEffect) fe.effect()).viewerId());
        }
        return viewers;
    }

    /** Whether {@code playerId} may look at the face-down exiled card {@code cardId}. */
    public boolean mayLookAtExiledCard(UUID cardId, UUID playerId) {
        return exileLookViewers(cardId).contains(playerId);
    }

    /** Ends every permission to look at the exiled card {@code cardId}. */
    public void endExileLookPermissions(UUID cardId) {
        endFloatingEffectsOnCard(cardId, ExileLookPermissionEffect.class);
    }

    /** Lets {@code playerId} cast the exiled creature card {@code cardId} for as long as it remains exiled (Lukka). */
    public void grantLukkaExileCastPermission(UUID cardId, UUID playerId) {
        endFloatingEffectsOnCard(cardId, LukkaExileCastPermissionEffect.class);
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), null, playerId,
                new LukkaExileCastPermissionEffect(playerId), cardId, Zone.EXILE, Duration.PERMANENT));
    }

    /** The player who may cast the exiled card {@code cardId} through Lukka's permission, or {@code null}. */
    public UUID lukkaExileCastPermissionHolder(UUID cardId) {
        List<FloatingContinuousEffect> effects = floatingEffectsOnCard(cardId, LukkaExileCastPermissionEffect.class);
        return effects.isEmpty() ? null : ((LukkaExileCastPermissionEffect) effects.getFirst().effect()).playerId();
    }

    /**
     * Makes the exiled card {@code cardId} renew its play permission each time a lore counter is put on the Saga
     * {@code sagaPermanentId} (Summon: Brynhildr), for as long as it remains exiled.
     */
    public void grantExilePlayPermissionRenewalOnLoreCounter(UUID cardId, UUID sagaPermanentId, UUID playerId) {
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(cardId), sagaPermanentId, playerId,
                new RenewExilePlayPermissionOnLoreCounterEffect(sagaPermanentId), cardId, Zone.EXILE, Duration.PERMANENT));
    }

    /** The exiled cards that renew their play permission as a lore counter is put on {@code sagaPermanentId}. */
    public List<UUID> cardsRenewingExilePlayPermissionOnLoreCounter(UUID sagaPermanentId) {
        List<UUID> cardIds = new ArrayList<>();
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(RenewExilePlayPermissionOnLoreCounterEffect.class)) {
            if (sagaPermanentId.equals(((RenewExilePlayPermissionOnLoreCounterEffect) fe.effect()).sagaPermanentId())) {
                cardIds.add(fe.affectedCardId());
            }
        }
        return cardIds;
    }

    /**
     * Lets the cards exiled with {@code sourcePermanentId} be played this turn at the given cost change (Urianger
     * Augurelt), also the ones exiled with it later that turn (CR 611.2c). A grant of the same source replaces it.
     */
    public void grantExilePlayUntilEndOfTurnBySource(UUID sourcePermanentId, ExilePlayCostModifier modifier) {
        endFloatingEffectsOnPermanent(sourcePermanentId, ExilePlayCostModifier.class);
        addFloatingEffect(FloatingContinuousEffect.onPermanent("an exile permission", sourcePermanentId,
                modifier.permittedPlayerId(), modifier, sourcePermanentId, Duration.UNTIL_END_OF_TURN));
    }

    /** The "play the cards exiled with it this turn" grant of {@code sourcePermanentId}, or {@code null}. */
    public ExilePlayCostModifier exilePlayGrantBySource(UUID sourcePermanentId) {
        List<FloatingContinuousEffect> effects = floatingEffectsOnPermanent(sourcePermanentId,
                ExilePlayCostModifier.class);
        return effects.isEmpty() ? null : (ExilePlayCostModifier) effects.getFirst().effect();
    }

    /** Ends the floating effects of any of {@code types} on the permanent {@code permanentId}. */
    @SafeVarargs
    public final void endFloatingEffectsOnPermanent(UUID permanentId, Class<? extends CardEffect>... types) {
        expireFloatingEffects(fe -> permanentId.equals(fe.affectedPermanentId()) && isAnyOf(fe.effect(), types));
    }

    /** Ends the floating effects of any of {@code types} on the card {@code cardId}. */
    @SafeVarargs
    public final void endFloatingEffectsOnCard(UUID cardId, Class<? extends CardEffect>... types) {
        scheduledFloatingEffects.removeIf(fe -> cardId.equals(fe.affectedCardId()) && isAnyOf(fe.effect(), types));
        expireFloatingEffects(fe -> cardId.equals(fe.affectedCardId()) && isAnyOf(fe.effect(), types));
    }

    /** Lets the player cast the card from exile this turn through a source-linked grant (CR 611.2a). */
    public void grantExileCastPermission(ExileCastPermission permission) {
        addFloatingEffect(FloatingContinuousEffect.onCard(exiledCardName(permission.cardId()),
                permission.sourcePermanentId(), permission.castingPlayerId(), permission, permission.cardId(),
                Zone.EXILE, Duration.UNTIL_END_OF_TURN));
    }

    /** The one-card source-linked exile cast grants that apply now, oldest first. */
    public List<ExileCastPermission> exileCastPermissions() {
        return floatingEffectsOfTypes(ExileCastPermission.class).stream()
                .map(fe -> (ExileCastPermission) fe.effect()).toList();
    }

    /** Grants a source-linked exile cast permission that also covers cards exiled with the source later this turn. */
    public void grantExileCastWildcardPermission(ExileCastWildcardPermission permission) {
        addFloatingEffect(FloatingContinuousEffect.onPlayer("an exile permission", permission.sourcePermanentId(),
                permission.castingPlayerId(), permission, permission.castingPlayerId(),
                Duration.UNTIL_END_OF_TURN));
    }

    /** The wildcard exile cast grants that apply now, oldest first. */
    public List<ExileCastWildcardPermission> exileCastWildcardPermissions() {
        return floatingEffectsOfTypes(ExileCastWildcardPermission.class).stream()
                .map(fe -> (ExileCastWildcardPermission) fe.effect()).toList();
    }

    /** Ends the exile cast grant {@code grantId}: the one-card permissions it made and its wildcard. */
    public void endExileCastGrant(UUID grantId) {
        expireFloatingEffects(fe -> (fe.effect() instanceof ExileCastPermission permission
                && permission.grantId().equals(grantId))
                || (fe.effect() instanceof ExileCastWildcardPermission wildcard
                && wildcard.grantId().equals(grantId)));
    }

    /** Lets a player cast one foretold card they own for free this turn. */
    public void grantForetoldCardCastPermission(ForetoldCardCastPermission permission) {
        addFloatingEffect(FloatingContinuousEffect.onPlayer("foretell", null, permission.playerId(), permission,
                permission.playerId(), Duration.UNTIL_END_OF_TURN));
    }

    /** The grants to cast a foretold card for free that apply now, oldest first. */
    public List<ForetoldCardCastPermission> foretoldCardCastPermissions() {
        return floatingEffectsOfTypes(ForetoldCardCastPermission.class).stream()
                .map(fe -> (ForetoldCardCastPermission) fe.effect()).toList();
    }

    /** Ends the one foretold-card cast grant {@code permission} (the first equal one, if several). */
    public void endForetoldCardCastPermission(ForetoldCardCastPermission permission) {
        for (FloatingContinuousEffect fe : floatingEffectsOfTypes(ForetoldCardCastPermission.class)) {
            if (permission.equals(fe.effect())) {
                endFloatingEffect(fe);
                return;
            }
        }
    }

    /** Finds an exiled card entry by card ID, or null if not found. */
    public ExiledCardEntry findExiledCard(UUID cardId) {
        return exiledCards.stream()
                .filter(e -> e.card().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /** The card imprinted on {@code source} (see {@link #imprintedCards}), or null if none. */
    public Card getImprintedCard(Card source) {
        return source != null ? imprintedCards.get(source.getId()) : null;
    }

    /** Imprints {@code imprinted} on {@code source}; a null {@code imprinted} clears the imprint. */
    public void setImprintedCard(Card source, Card imprinted) {
        if (imprinted == null) {
            imprintedCards.remove(source.getId());
        } else {
            imprintedCards.put(source.getId(), imprinted);
        }
    }

    /**
     * Shuffles, in place, the relative order of the exile entries tracked with the given source
     * permanent, leaving every other exile entry where it is. Backs "shuffle that pile" for a
     * face-down exile pile whose top card is its first entry (Mangara's Tome).
     */
    public void shuffleExilePile(UUID sourcePermanentId) {
        if (sourcePermanentId == null) return;
        synchronized (exiledCards) {
            List<Integer> positions = new ArrayList<>();
            List<ExiledCardEntry> pile = new ArrayList<>();
            for (int i = 0; i < exiledCards.size(); i++) {
                if (sourcePermanentId.equals(exiledCards.get(i).sourcePermanentId())) {
                    positions.add(i);
                    pile.add(exiledCards.get(i));
                }
            }
            Collections.shuffle(pile);
            for (int i = 0; i < positions.size(); i++) {
                exiledCards.set(positions.get(i), pile.get(i));
            }
        }
    }

    /** The first (top) exile entry tracked with the given source permanent, or null if the pile is empty. */
    public ExiledCardEntry topOfExilePile(UUID sourcePermanentId) {
        if (sourcePermanentId == null) return null;
        synchronized (exiledCards) {
            for (ExiledCardEntry e : exiledCards) {
                if (sourcePermanentId.equals(e.sourcePermanentId())) {
                    return e;
                }
            }
        }
        return null;
    }

    /** Removes all exile entries tracked with the given source permanent. */
    public void clearExiledByPermanent(UUID sourcePermanentId) {
        List<UUID> removedIds;
        synchronized (exiledCards) {
            removedIds = exiledCards.stream()
                    .filter(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                    .map(e -> e.card().getId())
                    .toList();
            exiledCards.removeIf(e -> sourcePermanentId.equals(e.sourcePermanentId()));
        }
        removedIds.forEach(exiledCardDreamCounters::remove);
        removedIds.forEach(exiledCardHitCounters::remove);
        removedIds.forEach(exiledCardScreamCounters::remove);
        removedIds.forEach(exiledCardRefineCounters::remove);
        removedIds.forEach(exiledCardsWithStudyCounters::remove);
        removedIds.forEach(exiledCardsWithIntelCounters::remove);
        removedIds.forEach(exiledCardsWithKickCounters::remove);
        removedIds.forEach(exiledCardsWithMemoryCounters::remove);
        removedIds.forEach(exiledCardsWithTakeoverCounters::remove);
        removedIds.forEach(exiledCardsWithFetchCounters::remove);
        removedIds.forEach(exiledCardsWithBrainCounters::remove);
        removedIds.forEach(exiledCardsWithCageCounters::remove);
        removedIds.forEach(antedCardIds::remove);
        removedIds.forEach(cardId -> {
            endExilePlayPermission(cardId);
            endExileLookPermissions(cardId);
            endFloatingEffectsOnCard(cardId, LukkaExileCastPermissionEffect.class);
        });
    }

    /** Removes source tracking from exile entries (sets sourcePermanentId to null). Used by Karn restart. */
    public void clearAllSourceTracking() {
        expireFloatingEffects(fe -> fe.effect() instanceof ExilePlayPermissionEffect
                && Duration.WHILE_SOURCE_CONTROLLED.equals(fe.duration()));
        List<ExiledCardEntry> updated = new ArrayList<>();
        var it = exiledCards.iterator();
        while (it.hasNext()) {
            ExiledCardEntry e = it.next();
            if (e.sourcePermanentId() != null) {
                it.remove();
                updated.add(new ExiledCardEntry(e.card(), e.ownerId(), null, e.faceDown(), e.exilerId(),
                        e.exiledTurnNumber(), e.controllerTurnsTakenAtExile(), e.abilityLink()));
            }
        }
        exiledCards.addAll(updated);
    }

    /** Queues a may ability while keeping its controller distinct from the choosing player. */
    public void queueMayAbilityForPlayer(Card sourceCard, UUID controllerId, MayEffect may,
                                         UUID targetCardId, UUID sourcePermanentId, UUID choicePlayerId,
                                         Permanent sourcePermanentSnapshot) {
        pendingMayAbilities.add(new PendingMayAbility(
                sourceCard,
                controllerId,
                new ArrayList<>(List.of(may.wrapped())),
                sourceCard.getName() + " - " + may.prompt(),
                targetCardId,
                null,
                sourcePermanentId,
                null,
                0,
                0,
                null,
                null,
                choicePlayerId,
                sourcePermanentSnapshot
        ));
    }

    /** Queues a may ability with the triggering permanent's last-known power snapshot. */
    public void queueMayAbilityForPlayer(Card sourceCard, UUID controllerId, MayEffect may,
                                         UUID targetCardId, UUID sourcePermanentId, UUID choicePlayerId,
                                         Permanent sourcePermanentSnapshot, UUID sourceControllerId,
                                         UUID triggeringPermanentId, Integer triggeringPermanentPowerAtTrigger) {
        pendingMayAbilities.add(new PendingMayAbility(
                sourceCard,
                controllerId,
                new ArrayList<>(List.of(may.wrapped())),
                sourceCard.getName() + " - " + may.prompt(),
                targetCardId,
                null,
                sourcePermanentId,
                null,
                0,
                0,
                null,
                null,
                choicePlayerId,
                sourcePermanentSnapshot,
                sourceControllerId,
                null,
                0,
                triggeringPermanentId,
                triggeringPermanentPowerAtTrigger,
                null
        ));
    }

    /**
     * Creates a deep copy of this game state for AI simulation (MCTS).
     * Uses plain (non-synchronized) collections for map values since
     * simulations are single-threaded.
     * <ul>
     *   <li>Card objects are shared (immutable after construction)</li>
     *   <li>Permanent objects are deep-copied (mutable state)</li>
     *   <li>Collections are copied to new independent instances</li>
     *   <li>Primitive/enum/UUID/String fields are assigned directly</li>
     * </ul>
     */
    public GameData simulationCopy() {
        return session.simulationCopy(id);
    }

    public int getCardIntensity(UUID cardId) {
        return cardId == null ? 0 : cardIntensities.getOrDefault(cardId, 0);
    }

    public int getCardIntensity(Card card) {
        return card == null ? 0 : cardIntensities.getOrDefault(card.getId(), startingIntensity(card));
    }

    public void initializeCardIntensity(Card card) {
        if (card != null && startingIntensity(card) > 0) {
            cardIntensities.putIfAbsent(card.getId(), startingIntensity(card));
        }
    }

    public void intensifyCard(Card card, int amount) {
        if (card != null && amount > 0) {
            cardIntensities.compute(card.getId(), (ignored, current) ->
                    (current == null ? startingIntensity(card) : current) + amount);
        }
    }

    private int startingIntensity(Card card) {
        return Math.max(card.getStartingIntensity(), card.getSpellEffects().stream()
                .filter(com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect.class::cast)
                .mapToInt(com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect::amount)
                .max().orElse(0));
    }

    public void initializeCardIntensity(Card card, int amount) {
        if (card != null && amount > 0) {
            cardIntensities.putIfAbsent(card.getId(), amount);
        }
    }

    private static CombatDamageState copyCombatDamageState(CombatDamageState source,
            java.util.function.Function<Permanent, Permanent> permanentCopy) {
        if (source == null) return null;
        CombatDamageState copy = new CombatDamageState();
        copy.sharedRedirectShields = new ArrayList<>(source.sharedRedirectShields);
        copy.sourceDamageShields = new ArrayList<>(source.sourceDamageShields);
        copy.reflectDamageShields = new ArrayList<>(source.reflectDamageShields);
        copy.pendingOptionalDamageChoices = source.pendingOptionalDamageChoices;
        copy.awaitingOptionalDamageChoices = source.awaitingOptionalDamageChoices;
        copy.proliferateLinksChecked = source.proliferateLinksChecked;
        copy.parkedForProliferateLinks = source.parkedForProliferateLinks;
        copy.pendingProliferateLinks.putAll(source.pendingProliferateLinks);
        copy.damageToDefendingPlayer = source.damageToDefendingPlayer;
        copy.poisonDamageToDefendingPlayer = source.poisonDamageToDefendingPlayer;
        copy.unpreventableDamageToDefendingPlayer = source.unpreventableDamageToDefendingPlayer;
        copy.damageRedirectedToGuard = source.damageRedirectedToGuard;
        copy.infectDamageRedirectedToGuard = source.infectDamageRedirectedToGuard;
        copy.deathtouchDamageRedirectedToGuard = source.deathtouchDamageRedirectedToGuard;
        copy.deadAttackerIndices.addAll(source.deadAttackerIndices);
        copy.deadDefenderIndices.addAll(source.deadDefenderIndices);
        source.atkDamageTaken.forEach((key, value) -> copy.atkDamageTaken.put(key, value));
        source.defDamageTaken.forEach((key, value) -> copy.defDamageTaken.put(key, value));
        source.atkDamageTakenBySource.forEach((key, value) -> copy.atkDamageTakenBySource.put(key, new HashMap<>(value)));
        source.defDamageTakenBySource.forEach((key, value) -> copy.defDamageTakenBySource.put(key, new HashMap<>(value)));
        source.unpreventableAtkDamageTaken.forEach((key, value) -> copy.unpreventableAtkDamageTaken.put(key, value));
        source.unpreventableDefDamageTaken.forEach((key, value) -> copy.unpreventableDefDamageTaken.put(key, value));
        source.damageDealtToPermanentsBeforeStep.forEach((key, value) -> copy.damageDealtToPermanentsBeforeStep.put(key, value));
        source.markedDamageBeforeStep.forEach((key, value) -> copy.markedDamageBeforeStep.put(key, value));
        source.toughnessBeforeStep.forEach((key, value) -> copy.toughnessBeforeStep.put(key, value));
        source.lethalDamageThresholdBeforeStep.forEach((key, value) -> copy.lethalDamageThresholdBeforeStep.put(key, value));
        source.loyaltyBeforeStep.forEach((key, value) -> copy.loyaltyBeforeStep.put(key, value));
        copy.deathtouchDamagedAttackerIndices.addAll(source.deathtouchDamagedAttackerIndices);
        copy.deathtouchDamagedDefenderIndices.addAll(source.deathtouchDamagedDefenderIndices);
        source.damageToPlaneswalkers.forEach((key, value) -> copy.damageToPlaneswalkers.put(key, value));
        source.combatDamageDealt.forEach((key, value) -> copy.combatDamageDealt.put(permanentCopy.apply(key), value));
        source.combatDamageDealtToPlayer.forEach((key, value) -> copy.combatDamageDealtToPlayer.put(permanentCopy.apply(key), value));
        source.combatDamagePlayerRecipients.forEach((key, value) -> copy.combatDamagePlayerRecipients.put(permanentCopy.apply(key), value));
        copy.normalDamageToPlayers.putAll(source.normalDamageToPlayers);
        copy.infectDamageToPlayers.putAll(source.infectDamageToPlayers);
        copy.unpreventableDamageToPlayers.putAll(source.unpreventableDamageToPlayers);
        source.combatDamageDealtToPlaneswalker.forEach((key, value) -> copy.combatDamageDealtToPlaneswalker.put(permanentCopy.apply(key), value));
        source.combatDamageAmountsToBattles.forEach((key, value) -> copy.combatDamageAmountsToBattles.put(permanentCopy.apply(key), new HashMap<>(value)));
        source.combatDamageAmountsToPlaneswalkers.forEach((key, value) -> copy.combatDamageAmountsToPlaneswalkers.put(permanentCopy.apply(key), new HashMap<>(value)));
        source.combatDamageRecipientControllers.forEach((key, value) -> copy.combatDamageRecipientControllers.put(permanentCopy.apply(key), new HashSet<>(value)));
        source.combatDamageDealtToCreatures.forEach((key, value) -> copy.combatDamageDealtToCreatures.put(permanentCopy.apply(key), new ArrayList<>(value)));
        copy.combatDamageToBlockingCreatureSources.addAll(source.combatDamageToBlockingCreatureSources);
        source.combatDamageDealerControllers.forEach((key, value) -> copy.combatDamageDealerControllers.put(permanentCopy.apply(key), value));
        source.combatDamageAmountsToCreatures.forEach((key, value) -> copy.combatDamageAmountsToCreatures.put(permanentCopy.apply(key), new HashMap<>(value)));
        source.combatDamageTargetControllers.forEach((key, value) -> copy.combatDamageTargetControllers.put(key, value));
        source.pendingDralnuReplacementTargets.forEach((key, value) -> copy.pendingDralnuReplacementTargets.put(key, permanentCopy.apply(value)));
        source.pendingDralnuReplacementDamage.forEach((key, value) -> copy.pendingDralnuReplacementDamage.put(key, value));
        copy.defenderDamageAsInfect = source.defenderDamageAsInfect;
        source.collectedPortions.forEach((key, value) -> copy.collectedPortions.put(key,
                new ArrayList<>(value.stream().map(com.github.laxika.magicalvibes.model.replacement.DamagePortion::copyForSimulation).toList())));
        source.pendingDamageEvents.forEach(event -> copy.pendingDamageEvents.add(event.copyForSimulation()));
        source.permanentsToShuffleAfterDamage.forEach((key, value) ->
                copy.permanentsToShuffleAfterDamage.put(key, permanentCopy.apply(value)));
        return copy;
    }

    GameData simulationFrameCopy() {
        GameData copy = new GameData(id, gameName, createdByUserId, createdByUsername);
        // Filling the copy's zone lists moves no card; the copy takes this game's rules event log at the end.
        copy.rulesEvents.suppress();
        copy.subgameRequested = subgameRequested;
        copy.waitingForSubgame = waitingForSubgame;
        copy.pendingAncestorTransfers.addAll(pendingAncestorTransfers);
        copy.subgameCards.putAll(subgameCards);
        suspendedZoneTriggers.forEach(entry -> copy.suspendedZoneTriggers.add(new StackEntry(entry)));
        copy.rulesEventListener = this.rulesEventListener;

        // --- Primitives, enums, UUIDs, Strings ---
        copy.status = this.status;
        copy.turnStartSnapshot = this.turnStartSnapshot;
        copy.playersWhoLostGameThisMatch.addAll(this.playersWhoLostGameThisMatch);
        copy.matchCreatureTypeErrata.putAll(this.matchCreatureTypeErrata);
        copy.completingCommanderZoneMove = this.completingCommanderZoneMove;
        copy.commandCastPlayerId = this.commandCastPlayerId;
        copy.commandCastCardId = this.commandCastCardId;
        copy.foreignHandPlayPlayerId = this.foreignHandPlayPlayerId;
        copy.foreignHandOwnerId = this.foreignHandOwnerId;
        copy.pendingCombatDamageFirstStrikePhase = this.pendingCombatDamageFirstStrikePhase;
        copy.pendingCombatDamageFirstestStrikePhase = this.pendingCombatDamageFirstestStrikePhase;
        copy.startingPlayerId = this.startingPlayerId;
        copy.currentStep = this.currentStep;
        copy.activePlayerId = this.activePlayerId;
        copy.previousTurnActivePlayerId = this.previousTurnActivePlayerId;
        copy.runedTerrorPhaseSequenceActive = this.runedTerrorPhaseSequenceActive;
        copy.runedTerrorPhaseSequenceFirstPlayerId = this.runedTerrorPhaseSequenceFirstPlayerId;
        copy.runedTerrorSequentialUntapInProgress = this.runedTerrorSequentialUntapInProgress;
        copy.monarchPlayerId = this.monarchPlayerId;
        copy.initiativePlayerId = this.initiativePlayerId;
        copy.untapStepUntappedPermanentCount = this.untapStepUntappedPermanentCount;
        copy.untapStepPlayerId = this.untapStepPlayerId;
        copy.untapPhasingAppliedPlayerId = this.untapPhasingAppliedPlayerId;
        copy.turnNumber = this.turnNumber;
        copy.mainPhasesBegunThisTurn = this.mainPhasesBegunThisTurn;
        copy.postcombatMainPhasesBegunThisTurn = this.postcombatMainPhasesBegunThisTurn;
        copy.greatestStackSourceCountThisTurn = this.greatestStackSourceCountThisTurn;
        copy.cardPutIntoExileThisTurn = this.cardPutIntoExileThisTurn;
        copy.greatestManaValueNotedForPermanentThisTurn.putAll(
                this.greatestManaValueNotedForPermanentThisTurn);
        copy.currentTurnIsExtraTurn = this.currentTurnIsExtraTurn;
        copy.lastNormalTurnPlayerId = this.lastNormalTurnPlayerId;
        copy.turnUntapStepSkipped = this.turnUntapStepSkipped;
        copy.extraTurnSkipsAllUntapSteps = this.extraTurnSkipsAllUntapSteps;
        copy.drawStepSkippedThisStep = this.drawStepSkippedThisStep;
        copy.powerUpAbilitiesCantBeActivatedThisTurn = this.powerUpAbilitiesCantBeActivatedThisTurn;
        copy.cardsExiledThisTurn = this.cardsExiledThisTurn;
        copy.pileGroupingOrGuessCountThisTurn = this.pileGroupingOrGuessCountThisTurn;
        copy.currentExtraTurnSequence = this.currentExtraTurnSequence;
        copy.nextExtraTurnSequence = this.nextExtraTurnSequence;
        copy.additionalUpkeepsRemaining = this.additionalUpkeepsRemaining;
        copy.currentUpkeepIsAdditional = this.currentUpkeepIsAdditional;
        copy.permanentWithOilCounterPutIntoGraveyardThisTurn = this.permanentWithOilCounterPutIntoGraveyardThisTurn;
        copy.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn =
                this.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn;
        copy.permanentPutIntoGraveyardFromBattlefieldThisTurn =
                this.permanentPutIntoGraveyardFromBattlefieldThisTurn;
        copy.permanentsPutIntoGraveyardFromBattlefieldThisTurn =
                this.permanentsPutIntoGraveyardFromBattlefieldThisTurn;
        copy.gameResult = this.gameResult;
        copy.winnerPlayerId = this.winnerPlayerId;
        copy.collectingSimultaneousCreatureEntryTriggers = this.collectingSimultaneousCreatureEntryTriggers;
        this.simultaneousCreatureEntryTriggers.forEach((id, abilities) ->
                copy.simultaneousCreatureEntryTriggers.put(id, new HashSet<>(abilities)));
        copy.combatDamageExemptPredicate = this.combatDamageExemptPredicate;
        this.pendingAttackingCopyOpponents.forEach((batchId, opponents) ->
                copy.pendingAttackingCopyOpponents.put(batchId, new ArrayList<>(opponents)));
        copy.pendingAttackingCopyChoices.putAll(this.pendingAttackingCopyChoices);
        copy.combatDamageExemptControllerId = this.combatDamageExemptControllerId;
        copy.activeAdditionalCountersForEnchantmentCreatureEntryBatch.putAll(
                this.activeAdditionalCountersForEnchantmentCreatureEntryBatch);
        copy.pendingEffectResolutionEntry = this.pendingEffectResolutionEntry != null
                ? new StackEntry(this.pendingEffectResolutionEntry) : null;
        copy.resolvingEntry = this.resolvingEntry == null ? null
                : this.resolvingEntry == this.pendingEffectResolutionEntry ? copy.pendingEffectResolutionEntry
                : new StackEntry(this.resolvingEntry);
        copy.pendingEffectResolutionIndex = this.pendingEffectResolutionIndex;
        copy.resolutionOrdinal = this.resolutionOrdinal;
        copy.handCardChoiceDeclineEntry = this.handCardChoiceDeclineEntry != null
                ? new StackEntry(this.handCardChoiceDeclineEntry) : null;
        this.votingChoicesByController.forEach((controllerId, choicesByPlayer) -> {
            Map<UUID, Set<String>> choicesCopy = new LinkedHashMap<>();
            choicesByPlayer.forEach((playerId, choices) ->
                    choicesCopy.put(playerId, new LinkedHashSet<>(choices)));
            copy.votingChoicesByController.put(controllerId, choicesCopy);
        });
        copy.additionalVotesTaken.putAll(this.additionalVotesTaken);
        copy.additionalVoteAccepted = this.additionalVoteAccepted;
        copy.wordOfCommandPendingResolutionEntry = this.wordOfCommandPendingResolutionEntry != null
                ? new StackEntry(this.wordOfCommandPendingResolutionEntry) : null;
        copy.pendingReverseMiracleSearch = this.pendingReverseMiracleSearch;
        copy.resolvingMayEffectFromStack = this.resolvingMayEffectFromStack;
        copy.resolvedMayAccepted = this.resolvedMayAccepted;
        // resolvedMayTargetingEntry usually aliases pendingEffectResolutionEntry (the graveyard choice
        // of a resolving effect records the chosen card through the alias and resumes through
        // pendingEffectResolutionEntry) — preserve the shared identity in the copy, otherwise the
        // simulated choice answer would record the card on a dead copy.
        copy.resolvedMayTargetingEntry = this.resolvedMayTargetingEntry == this.pendingEffectResolutionEntry
                ? copy.pendingEffectResolutionEntry
                : (this.resolvedMayTargetingEntry != null ? new StackEntry(this.resolvedMayTargetingEntry) : null);
        copy.chosenXValue = this.chosenXValue;
        copy.libraryToShuffleAfterFreeCast = this.libraryToShuffleAfterFreeCast;
        copy.pendingDividedDamageTargetId = this.pendingDividedDamageTargetId;
        copy.pendingAbilityCounterCostActivation = this.pendingAbilityCounterCostActivation;
        copy.chosenCostCounterType = this.chosenCostCounterType;
        copy.chosenCostCounterTypes = this.chosenCostCounterTypes == null ? null : List.copyOf(this.chosenCostCounterTypes);
        copy.unusedSpellCostReduction = this.unusedSpellCostReduction;
        copy.chosenSpellSubtype = this.chosenSpellSubtype;
        copy.chosenSpellColor = this.chosenSpellColor;
        copy.chosenSpellParity = this.chosenSpellParity;
        copy.chosenSpellNumber = this.chosenSpellNumber;
        copy.chosenSpellManaValueParity = this.chosenSpellManaValueParity;
        copy.chosenSpellPermanentType = this.chosenSpellPermanentType;
        copy.chosenSpellCardType = this.chosenSpellCardType;
        copy.chosenSpellLandOrNonland = this.chosenSpellLandOrNonland;
        copy.turnaboutTap = this.turnaboutTap;
        copy.rerunCurrentEffectAfterInteraction = this.rerunCurrentEffectAfterInteraction;
        copy.pendingDrawRevealDiscardDrawCounts.putAll(this.pendingDrawRevealDiscardDrawCounts);
        copy.deferPlayerLossCheck = this.deferPlayerLossCheck;
        copy.effectResolutionDepth = this.effectResolutionDepth;
        copy.eachPlayerRummage.active = this.eachPlayerRummage.active;
        copy.eachPlayerRummage.currentPlayerId = this.eachPlayerRummage.currentPlayerId;
        copy.eachPlayerRummage.pendingDraw = this.eachPlayerRummage.pendingDraw;
        copy.eachPlayerRummage.remaining.addAll(this.eachPlayerRummage.remaining);
        copy.eachPlayerRummage.chosenAmounts.putAll(this.eachPlayerRummage.chosenAmounts);
        copy.eachPlayerRummage.deferDiscards = this.eachPlayerRummage.deferDiscards;
        copy.eachPlayerRummage.selectedDiscards.addAll(this.eachPlayerRummage.selectedDiscards);
        copy.eachPlayerDiscardSelections.addAll(this.eachPlayerDiscardSelections);
        copy.clashPlacement.active = this.clashPlacement.active;
        copy.clashPlacement.currentPlayerId = this.clashPlacement.currentPlayerId;
        copy.clashPlacement.remaining.addAll(this.clashPlacement.remaining);
        copy.clashPlacement.placements.addAll(this.clashPlacement.placements);
        copy.eachPlayerPutsCardFromHandOnTopOfLibrary.active =
                this.eachPlayerPutsCardFromHandOnTopOfLibrary.active;
        copy.eachPlayerPutsCardFromHandOnTopOfLibrary.remaining.addAll(
                this.eachPlayerPutsCardFromHandOnTopOfLibrary.remaining);
        copy.eachPlayerPutsCardFromHandOnTopOfLibrary.chosen.putAll(
                this.eachPlayerPutsCardFromHandOnTopOfLibrary.chosen);
        copy.eachPlayerDiscardsOrLosesLife.active = this.eachPlayerDiscardsOrLosesLife.active;
        copy.eachPlayerDiscardsOrLosesLife.currentPlayerId = this.eachPlayerDiscardsOrLosesLife.currentPlayerId;
        copy.eachPlayerDiscardsOrLosesLife.discardPending = this.eachPlayerDiscardsOrLosesLife.discardPending;
        copy.eachPlayerDiscardsOrLosesLife.remaining.addAll(this.eachPlayerDiscardsOrLosesLife.remaining);
        copy.eachPlayerDiscardsCreatureOrLosesLife.active = this.eachPlayerDiscardsCreatureOrLosesLife.active;
        copy.eachPlayerDiscardsCreatureOrLosesLife.currentPlayerId = this.eachPlayerDiscardsCreatureOrLosesLife.currentPlayerId;
        copy.eachPlayerDiscardsCreatureOrLosesLife.remaining.addAll(this.eachPlayerDiscardsCreatureOrLosesLife.remaining);
        copy.eachPlayerDiscardsNonlandCard.active = this.eachPlayerDiscardsNonlandCard.active;
        copy.eachPlayerDiscardsNonlandCard.controllerId = this.eachPlayerDiscardsNonlandCard.controllerId;
        copy.eachPlayerDiscardsNonlandCard.currentPlayerId = this.eachPlayerDiscardsNonlandCard.currentPlayerId;
        copy.eachPlayerDiscardsNonlandCard.remaining.addAll(this.eachPlayerDiscardsNonlandCard.remaining);
        copy.eachPlayerMayExileGraveyardCards.active = this.eachPlayerMayExileGraveyardCards.active;
        copy.eachPlayerMayExileGraveyardCards.currentPlayerId = this.eachPlayerMayExileGraveyardCards.currentPlayerId;
        copy.eachPlayerMayExileGraveyardCards.remaining.addAll(this.eachPlayerMayExileGraveyardCards.remaining);
        copy.deferredDiscardChooserId = this.deferredDiscardChooserId;
        copy.deferredDiscardSelections.addAll(this.deferredDiscardSelections);
        copy.stagedHandExiles.addAll(this.stagedHandExiles);
        copy.creepingDread.active = this.creepingDread.active;
        copy.creepingDread.controllerId = this.creepingDread.controllerId;
        copy.creepingDread.currentPlayerId = this.creepingDread.currentPlayerId;
        copy.creepingDread.remaining.addAll(this.creepingDread.remaining);
        copy.creepingDread.selectedDiscards.addAll(this.creepingDread.selectedDiscards);
        this.creepingDread.discardedCardTypes.forEach((playerId, types) ->
                copy.creepingDread.discardedCardTypes.put(playerId, Set.copyOf(types)));
        copy.eachPlayerMayDiscardOneThenApplyEffects.active =
                this.eachPlayerMayDiscardOneThenApplyEffects.active;
        copy.eachPlayerMayDiscardOneThenApplyEffects.controllerId =
                this.eachPlayerMayDiscardOneThenApplyEffects.controllerId;
        copy.eachPlayerMayDiscardOneThenApplyEffects.currentPlayerId =
                this.eachPlayerMayDiscardOneThenApplyEffects.currentPlayerId;
        copy.eachPlayerMayDiscardOneThenApplyEffects.remaining.addAll(
                this.eachPlayerMayDiscardOneThenApplyEffects.remaining);
        copy.eachPlayerMayDiscardThenSearchBasicLand.active =
                this.eachPlayerMayDiscardThenSearchBasicLand.active;
        copy.eachPlayerMayDiscardThenSearchBasicLand.controllerId =
                this.eachPlayerMayDiscardThenSearchBasicLand.controllerId;
        copy.eachPlayerMayDiscardThenSearchBasicLand.currentPlayerId =
                this.eachPlayerMayDiscardThenSearchBasicLand.currentPlayerId;
        copy.eachPlayerMayDiscardThenSearchBasicLand.remaining.addAll(
                this.eachPlayerMayDiscardThenSearchBasicLand.remaining);
        copy.scytheSpecter.active = this.scytheSpecter.active;
        copy.scytheSpecter.controllerId = this.scytheSpecter.controllerId;
        copy.scytheSpecter.currentPlayerId = this.scytheSpecter.currentPlayerId;
        copy.scytheSpecter.remaining.addAll(this.scytheSpecter.remaining);
        copy.scytheSpecter.discardedManaValues.putAll(this.scytheSpecter.discardedManaValues);
        copy.caitCageBrawler.active = this.caitCageBrawler.active;
        copy.caitCageBrawler.controllerId = this.caitCageBrawler.controllerId;
        copy.caitCageBrawler.defendingPlayerId = this.caitCageBrawler.defendingPlayerId;
        copy.caitCageBrawler.currentPlayerId = this.caitCageBrawler.currentPlayerId;
        copy.caitCageBrawler.remaining.addAll(this.caitCageBrawler.remaining);
        copy.caitCageBrawler.discardedManaValues.putAll(this.caitCageBrawler.discardedManaValues);
        copy.eumidianWastewaker.active = this.eumidianWastewaker.active;
        copy.eumidianWastewaker.currentPlayerId = this.eumidianWastewaker.currentPlayerId;
        copy.eumidianWastewaker.remaining.addAll(this.eumidianWastewaker.remaining);
        copy.eumidianWastewaker.chosenMode = this.eumidianWastewaker.chosenMode;
        copy.eumidianWastewaker.pendingDiscard = this.eumidianWastewaker.pendingDiscard;
        copy.eumidianWastewaker.pendingSacrificeChoice = this.eumidianWastewaker.pendingSacrificeChoice;
        copy.eumidianWastewaker.pendingSacrificeLandIds.addAll(
                this.eumidianWastewaker.pendingSacrificeLandIds);
        copy.eumidianWastewaker.landCardsPutIntoGraveyard =
                this.eumidianWastewaker.landCardsPutIntoGraveyard;
        copy.eachPlayerDiscardsOneThenDrawsForEachCardType.discardedCardTypes.addAll(
                this.eachPlayerDiscardsOneThenDrawsForEachCardType.discardedCardTypes);
        copy.dispersal.active = this.dispersal.active;
        copy.dispersal.remainingOpponentIds.addAll(this.dispersal.remainingOpponentIds);
        copy.dispersal.currentOpponentId = this.dispersal.currentOpponentId;
        copy.dispersal.selectedPermanentId = this.dispersal.selectedPermanentId;
        copy.dispersal.awaitingDiscard = this.dispersal.awaitingDiscard;
        copy.eachOpponentDiscardsTwoUnlessNonland.active = this.eachOpponentDiscardsTwoUnlessNonland.active;
        copy.eachOpponentDiscardsTwoUnlessNonland.remainingOpponentIds
                .addAll(this.eachOpponentDiscardsTwoUnlessNonland.remainingOpponentIds);
        copy.eachOpponentDiscardsTwoUnlessNonland.currentOpponentId =
                this.eachOpponentDiscardsTwoUnlessNonland.currentOpponentId;
        copy.eachOpponentDiscardsTwoUnlessNonland.awaitingMayChoice =
                this.eachOpponentDiscardsTwoUnlessNonland.awaitingMayChoice;
        copy.eachOpponentDiscardsTwoUnlessNonland.awaitingDiscard =
                this.eachOpponentDiscardsTwoUnlessNonland.awaitingDiscard;
        copy.eachPlayerPayLife.active = this.eachPlayerPayLife.active;
        copy.eachPlayerPayLife.order.addAll(this.eachPlayerPayLife.order);
        copy.eachPlayerPayLife.index = this.eachPlayerPayLife.index;
        copy.eachPlayerPayLife.playersActedThisRound = this.eachPlayerPayLife.playersActedThisRound;
        copy.eachPlayerPayLife.paidThisRound = this.eachPlayerPayLife.paidThisRound;
        copy.eachPlayerPayLife.lifePaid.putAll(this.eachPlayerPayLife.lifePaid);
        copy.eachPlayerPayLife.currentPlayerId = this.eachPlayerPayLife.currentPlayerId;
        copy.eachPlayerPayLife.sourceSetCode = this.eachPlayerPayLife.sourceSetCode;
        copy.eachPlayerPayMana.active = this.eachPlayerPayMana.active;
        copy.eachPlayerPayMana.order.addAll(this.eachPlayerPayMana.order);
        copy.eachPlayerPayMana.index = this.eachPlayerPayMana.index;
        copy.eachPlayerPayMana.manaPaid.putAll(this.eachPlayerPayMana.manaPaid);
        copy.eachPlayerPayMana.currentPlayerId = this.eachPlayerPayMana.currentPlayerId;
        copy.eachPlayerPayMana.sourceSetCode = this.eachPlayerPayMana.sourceSetCode;
        copy.goblinGame.active = this.goblinGame.active;
        copy.goblinGame.order.addAll(this.goblinGame.order);
        copy.goblinGame.index = this.goblinGame.index;
        copy.goblinGame.itemCounts.putAll(this.goblinGame.itemCounts);
        copy.goblinGame.currentPlayerId = this.goblinGame.currentPlayerId;
        copy.truthOrConsequences.active = this.truthOrConsequences.active;
        copy.truthOrConsequences.order.addAll(this.truthOrConsequences.order);
        copy.truthOrConsequences.index = this.truthOrConsequences.index;
        copy.truthOrConsequences.choices.putAll(this.truthOrConsequences.choices);
        copy.truthOrConsequences.voteChoices.addAll(this.truthOrConsequences.voteChoices);
        copy.truthOrConsequences.currentPlayerId = this.truthOrConsequences.currentPlayerId;
        copy.expertLevelSafe.active = this.expertLevelSafe.active;
        copy.expertLevelSafe.sourcePermanentId = this.expertLevelSafe.sourcePermanentId;
        copy.expertLevelSafe.controllerId = this.expertLevelSafe.controllerId;
        copy.expertLevelSafe.opponentId = this.expertLevelSafe.opponentId;
        copy.expertLevelSafe.currentPlayerId = this.expertLevelSafe.currentPlayerId;
        copy.expertLevelSafe.controllerChoice = this.expertLevelSafe.controllerChoice;
        copy.expertLevelSafe.opponentChoice = this.expertLevelSafe.opponentChoice;
        copy.wheelOfMisfortune.active = this.wheelOfMisfortune.active;
        copy.wheelOfMisfortune.order.addAll(this.wheelOfMisfortune.order);
        copy.wheelOfMisfortune.index = this.wheelOfMisfortune.index;
        copy.wheelOfMisfortune.chosenNumbers.putAll(this.wheelOfMisfortune.chosenNumbers);
        copy.wheelOfMisfortune.currentPlayerId = this.wheelOfMisfortune.currentPlayerId;
        copy.illicitAuction.active = this.illicitAuction.active;
        copy.illicitAuction.order.addAll(this.illicitAuction.order);
        copy.illicitAuction.index = this.illicitAuction.index;
        copy.illicitAuction.highBid = this.illicitAuction.highBid;
        copy.illicitAuction.highBidderId = this.illicitAuction.highBidderId;
        copy.illicitAuction.currentBidderId = this.illicitAuction.currentBidderId;
        copy.torment.active = this.torment.active;
        copy.torment.remainingIterations = this.torment.remainingIterations;
        copy.torment.remaining.addAll(this.torment.remaining);
        copy.torment.currentOpponentId = this.torment.currentOpponentId;
        copy.torment.chosenMode = this.torment.chosenMode;
        copy.torment.deferred = this.torment.deferred;
        copy.torment.opponentPassesLeft = this.torment.opponentPassesLeft;
        copy.torment.totalPasses = this.torment.totalPasses;
        copy.torment.chosenSacrifices.addAll(this.torment.chosenSacrifices);
        copy.torment.lifeLossCounts.putAll(this.torment.lifeLossCounts);
        copy.indulgentTormentor.active = this.indulgentTormentor.active;
        copy.indulgentTormentor.waitingForSacrifice = this.indulgentTormentor.waitingForSacrifice;
        copy.indulgentTormentor.chosenMode = this.indulgentTormentor.chosenMode;
        copy.forbiddenRitual.active = this.forbiddenRitual.active;
        copy.forbiddenRitual.controllerSacrificed = this.forbiddenRitual.controllerSacrificed;
        copy.forbiddenRitual.lifeLoss = this.forbiddenRitual.lifeLoss;
        copy.forbiddenRitual.chosenMode = this.forbiddenRitual.chosenMode;
        copy.forbiddenRitual.repeatAccepted = this.forbiddenRitual.repeatAccepted;
        copy.eachPlayerSacrificeOrDiscard.active = this.eachPlayerSacrificeOrDiscard.active;
        copy.eachPlayerSacrificeOrDiscard.remaining.addAll(this.eachPlayerSacrificeOrDiscard.remaining);
        copy.eachPlayerSacrificeOrDiscard.currentPlayerId = this.eachPlayerSacrificeOrDiscard.currentPlayerId;
        copy.eachPlayerSacrificeOrDiscard.chosenMode = this.eachPlayerSacrificeOrDiscard.chosenMode;
        copy.eachPlayerSacrificeOrDiscard.deferSacrifices = this.eachPlayerSacrificeOrDiscard.deferSacrifices;
        copy.eachPlayerSacrificeOrDiscard.chosenSacrifices.addAll(this.eachPlayerSacrificeOrDiscard.chosenSacrifices);
        copy.eachPlayerSacrificeOrLoseLife.active = this.eachPlayerSacrificeOrLoseLife.active;
        copy.eachPlayerSacrificeOrLoseLife.remaining.addAll(this.eachPlayerSacrificeOrLoseLife.remaining);
        copy.eachPlayerSacrificeOrLoseLife.currentPlayerId = this.eachPlayerSacrificeOrLoseLife.currentPlayerId;
        copy.eachPlayerSacrificeOrLoseLife.chosenMode = this.eachPlayerSacrificeOrLoseLife.chosenMode;
        copy.eachPlayerSacrificeOrLoseLife.sacrificeIds.addAll(this.eachPlayerSacrificeOrLoseLife.sacrificeIds);
        copy.eachPlayerSacrificeOrLoseLife.lifeLossPlayerIds.addAll(this.eachPlayerSacrificeOrLoseLife.lifeLossPlayerIds);
        copy.villainousChoice.active = this.villainousChoice.active;
        copy.villainousChoice.remaining.addAll(this.villainousChoice.remaining);
        copy.villainousChoice.currentPlayerId = this.villainousChoice.currentPlayerId;
        copy.villainousChoice.currentTargetId = this.villainousChoice.currentTargetId;
        copy.villainousChoice.additionalChoicesRemaining = this.villainousChoice.additionalChoicesRemaining;
        copy.villainousChoice.choicePlayerId = this.villainousChoice.choicePlayerId;
        copy.villainousChoice.choiceSourceCardName = this.villainousChoice.choiceSourceCardName;
        copy.villainousChoice.choicePutOption = this.villainousChoice.choicePutOption;
        copy.villainousChoice.choiceOptions = List.copyOf(this.villainousChoice.choiceOptions);
        copy.villainousChoice.choiceDescription = this.villainousChoice.choiceDescription;
        copy.villainousChoice.chosenMode = this.villainousChoice.chosenMode;
        copy.villainousChoice.waitingForDiscard = this.villainousChoice.waitingForDiscard;
        copy.villainousChoice.waitingForDraw = this.villainousChoice.waitingForDraw;
        copy.villainousChoice.waitingForControllerMay = this.villainousChoice.waitingForControllerMay;
        copy.villainousChoice.waitingForCardChoice = this.villainousChoice.waitingForCardChoice;
        copy.plaguecrafter.active = this.plaguecrafter.active;
        copy.plaguecrafter.sacrificeChoicesInProgress = this.plaguecrafter.sacrificeChoicesInProgress;
        copy.plaguecrafter.completed = this.plaguecrafter.completed;
        copy.plaguecrafter.sourceControllerId = this.plaguecrafter.sourceControllerId;
        copy.plaguecrafter.playersWhoCannotSacrifice.addAll(this.plaguecrafter.playersWhoCannotSacrifice);
        copy.plaguecrafter.remainingDiscardPlayers.addAll(this.plaguecrafter.remainingDiscardPlayers);
        copy.plaguecrafter.selectedDiscards.addAll(this.plaguecrafter.selectedDiscards);
        copy.wintersChill.active = this.wintersChill.active;
        copy.wintersChill.remainingTargetIds.addAll(this.wintersChill.remainingTargetIds);
        copy.wintersChill.currentTargetId = this.wintersChill.currentTargetId;
        copy.wintersChill.chosenMode = this.wintersChill.chosenMode;
        copy.disorientingChoice.active = this.disorientingChoice.active;
        copy.disorientingChoice.remainingTargetIds.addAll(this.disorientingChoice.remainingTargetIds);
        copy.disorientingChoice.selectedTargetIds.addAll(this.disorientingChoice.selectedTargetIds);
        copy.disorientingChoice.currentTargetId = this.disorientingChoice.currentTargetId;
        copy.disorientingChoice.chosenMode = this.disorientingChoice.chosenMode;
        copy.forgottenLore.active = this.forgottenLore.active;
        copy.forgottenLore.chosenCardIds.addAll(this.forgottenLore.chosenCardIds);
        copy.forgottenLore.lastChosenCardId = this.forgottenLore.lastChosenCardId;
        copy.forgottenLore.pendingChosenCardId = this.forgottenLore.pendingChosenCardId;
        copy.forgottenLore.chosenMode = this.forgottenLore.chosenMode;
        copy.pendingAbilityActivation = this.pendingAbilityActivation; // immutable record
        copy.pendingGraveyardAbilityActivation = this.pendingGraveyardAbilityActivation; // immutable record
        copy.endTurnRequested = this.endTurnRequested;
        copy.restartTurnRequested = this.restartTurnRequested;
        copy.endStepsThisTurn = this.endStepsThisTurn;
        copy.additionalEndStepsPending = this.additionalEndStepsPending;
        copy.discardCausedByOpponent = this.discardCausedByOpponent;
        copy.cardEnteringGraveyardByCycling = this.cardEnteringGraveyardByCycling;
        copy.additionalCombatMainPhasePairs = this.additionalCombatMainPhasePairs;
        copy.additionalCombatMainPhasePairsReturnStep = this.additionalCombatMainPhasePairsReturnStep;
        copy.additionalCombatMainPhasePairsAfterSecondMain = this.additionalCombatMainPhasePairsAfterSecondMain;
        copy.additionalBeginningPhasesAfterPostcombatMain = this.additionalBeginningPhasesAfterPostcombatMain;
        copy.additionalBeginningPhasesBeforeQueuedCombat = this.additionalBeginningPhasesBeforeQueuedCombat;
        copy.additionalBeginningPhaseReturnStep = this.additionalBeginningPhaseReturnStep;
        copy.additionalCombatPhasesOnly = this.additionalCombatPhasesOnly;
        copy.aggressiveCombatPhasesOnly = this.aggressiveCombatPhasesOnly;
        copy.additionalCombatReturnActivePlayerId = this.additionalCombatReturnActivePlayerId;
        copy.onlyAggressiveCreaturesCanAttackThisCombat = this.onlyAggressiveCreaturesCanAttackThisCombat;
        copy.additionalCombatPhasesAfterMain = this.additionalCombatPhasesAfterMain;
        copy.additionalCombatPhasesAfterMainReturnStep = this.additionalCombatPhasesAfterMainReturnStep;
        copy.combatPhasesThisTurn = this.combatPhasesThisTurn;
        copy.firstCombatDamageTimingClosed = this.firstCombatDamageTimingClosed;
        copy.draftId = this.draftId;
        copy.cleanupDiscardPending = this.cleanupDiscardPending;
        copy.simulation = true;
        copy.alwaysOfferPriorityWindows = this.alwaysOfferPriorityWindows;
        copy.domainActionSequence = this.domainActionSequence;
        copy.domainEventSequence = this.domainEventSequence;
        copy.domainStateVersion = this.domainStateVersion;
        copy.timestampCounter = this.timestampCounter;
        copy.upkeepStartTimestamp = this.upkeepStartTimestamp;
        copy.turnStartTimestamp = this.turnStartTimestamp;
        copy.graveyardEntryVersion = this.graveyardEntryVersion;
        copy.graveyardEntryVersions.putAll(this.graveyardEntryVersions);
        copy.exileEntryVersions.putAll(this.exileEntryVersions);
        copy.handEntryVersions.putAll(this.handEntryVersions);
        copy.combatDamageFirstStrikeStepComplete = this.combatDamageFirstStrikeStepComplete;
        copy.combatDamageFirstestStrikeStepComplete = this.combatDamageFirstestStrikeStepComplete;
        copy.creaturesInFirstestStrikeDamageStepThisCombat.addAll(this.creaturesInFirstestStrikeDamageStepThisCombat);
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.combatDamageFirstStrikeAssignmentPhase = this.combatDamageFirstStrikeAssignmentPhase;
        copy.combatDamageFirstestStrikeAssignmentPhase = this.combatDamageFirstestStrikeAssignmentPhase;
        copy.pendingGraveyardReturnQueue.addAll(this.pendingGraveyardReturnQueue);
        copy.deferredGraveyardCastCopies.addAll(this.deferredGraveyardCastCopies);
        this.stagedSpellCopies.forEach(se -> copy.stagedSpellCopies.add(new StackEntry(se)));
        copy.pendingGraveyardReturnBatch = this.pendingGraveyardReturnBatch;
        copy.graveyardReturnQueueLeaveBatchOpen = this.graveyardReturnQueueLeaveBatchOpen;
        copy.graveyardExileChoiceLeaveBatchOpen = this.graveyardExileChoiceLeaveBatchOpen;
        copy.holdGraveyardHandReturns = this.holdGraveyardHandReturns;
        copy.heldGraveyardHandReturns.putAll(this.heldGraveyardHandReturns);
        copy.pendingEachPlayerDrawUpToQueue.addAll(this.pendingEachPlayerDrawUpToQueue);
        copy.pendingEachPlayerDrawUpToInitialCount = this.pendingEachPlayerDrawUpToInitialCount;
        copy.pendingEachPlayerDrawUpToChoices.putAll(this.pendingEachPlayerDrawUpToChoices);
        copy.pendingEachPlayerDrawUpToInitialCounts = this.pendingEachPlayerDrawUpToInitialCounts == null
                ? null : new LinkedHashMap<>(this.pendingEachPlayerDrawUpToInitialCounts);
        copy.pendingEachPlayerPutContinuation = this.pendingEachPlayerPutContinuation;
        copy.pendingExileTopCardRepeat = this.pendingExileTopCardRepeat;
        copy.pendingEachPlayerMayDrawChosenCount = this.pendingEachPlayerMayDrawChosenCount;
        copy.pendingEachPlayerMayDrawThenGainLifeQueue.addAll(this.pendingEachPlayerMayDrawThenGainLifeQueue);
        copy.pendingEachPlayerMayDrawThenGainLifeDrawers.addAll(this.pendingEachPlayerMayDrawThenGainLifeDrawers);
        copy.pendingEachPlayerMayDrawThenGainLifeInitialCount = this.pendingEachPlayerMayDrawThenGainLifeInitialCount;
        copy.pendingEachOtherPlayerDrawUpToQueue.addAll(this.pendingEachOtherPlayerDrawUpToQueue);
        copy.pendingRegenerationControlChanges.putAll(this.pendingRegenerationControlChanges);
        copy.unpreventableDamageInProgress = this.unpreventableDamageInProgress;

        // --- Set<UUID> (ConcurrentHashMap.newKeySet()) ---
        copy.playerIds.addAll(this.playerIds);
        copy.aiPlayerIds.addAll(this.aiPlayerIds);
        copy.playerKeptHand.addAll(this.playerKeptHand);
        copy.priorityPassedBy.addAll(this.priorityPassedBy);
        copy.priorityHolderId = this.priorityHolderId;
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);
        copy.resolvingDeclinedAllCreatureDamageRedirect = this.resolvingDeclinedAllCreatureDamageRedirect;
        copy.playersWhoActivatedLoyaltyAbilityThisTurn.addAll(this.playersWhoActivatedLoyaltyAbilityThisTurn);
        copy.playersWhoActivatedSparkAbilityThisTurn.addAll(this.playersWhoActivatedSparkAbilityThisTurn);
        copy.playersWhoPlayedCardFromExileThisTurn.addAll(this.playersWhoPlayedCardFromExileThisTurn);
        copy.playersWhoFlippedCoinsThisTurn.addAll(this.playersWhoFlippedCoinsThisTurn);
        copy.playersWhoPlayedOrCastFromOutsideHandThisTurn.addAll(this.playersWhoPlayedOrCastFromOutsideHandThisTurn);
        copy.permanentsThatReceivedPlusOnePlusOneCountersThisTurn
                .addAll(this.permanentsThatReceivedPlusOnePlusOneCountersThisTurn);
        this.permanentsWithPlusOneCountersPutByPlayerThisTurn.forEach((playerId, permanentIds) ->
                copy.permanentsWithPlusOneCountersPutByPlayerThisTurn.put(playerId, new HashSet<>(permanentIds)));
        copy.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn
                .putAll(this.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn);
        copy.permanentsThatAttackedBattlesThisTurn.addAll(this.permanentsThatAttackedBattlesThisTurn);
        copy.playersDealtCombatDamageSinceTheirLastTurn.addAll(this.playersDealtCombatDamageSinceTheirLastTurn);
        copy.playersDealtCombatDamageLastTurn.addAll(this.playersDealtCombatDamageLastTurn);
        copy.playersWhoActivatedExhaustAbilityThisTurn.addAll(this.playersWhoActivatedExhaustAbilityThisTurn);
        copy.playersWhoActivatedAbilityOfGraveyardCardThisTurn
                .addAll(this.playersWhoActivatedAbilityOfGraveyardCardThisTurn);
        copy.playersWhoActivatedEquipAbilityThisTurn.addAll(this.playersWhoActivatedEquipAbilityThisTurn);
        copy.playersWhoActivatedArtifactAbilityThisTurn.addAll(this.playersWhoActivatedArtifactAbilityThisTurn);
        copy.playersWhoActivatedPowerUpAbilityThisTurn.addAll(this.playersWhoActivatedPowerUpAbilityThisTurn);
        this.handsRevealedWhileSourceOnBattlefield.forEach((sourceId, playerIds) -> {
            Set<UUID> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(playerIds);
            copy.handsRevealedWhileSourceOnBattlefield.put(sourceId, copied);
        });
        this.countersLockedPermanentsWhileSourceOnBattlefield.forEach((sourceId, permanentIds) -> {
            Set<UUID> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(permanentIds);
            copy.countersLockedPermanentsWhileSourceOnBattlefield.put(sourceId, copied);
        });
        this.countersLockedPlayersWhileSourceOnBattlefield.forEach((sourceId, playerIds) -> {
            Set<UUID> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(playerIds);
            copy.countersLockedPlayersWhileSourceOnBattlefield.put(sourceId, copied);
        });
        copy.opponentGraveyardLifeLossWatchers.addAll(this.opponentGraveyardLifeLossWatchers);
        copy.lifeGainOpponentLifeLossWatchers.addAll(this.lifeGainOpponentLifeLossWatchers);
        copy.temporaryGlobalTriggeredAbilities.addAll(this.temporaryGlobalTriggeredAbilities);
        copy.sacrificeBoonWatchers.addAll(this.sacrificeBoonWatchers);
        copy.allyCreatureEntersTriggerWatchers.addAll(this.allyCreatureEntersTriggerWatchers);
        copy.sourceRedirectShieldsUsedInOpenEvent.addAll(this.sourceRedirectShieldsUsedInOpenEvent);
        copy.playerSourceShieldsUsedInOpenEvent.addAll(this.playerSourceShieldsUsedInOpenEvent);
        copy.pendingEyeForAnEyeReflections.addAll(this.pendingEyeForAnEyeReflections);
        this.pendingSourceDamageForReflection.forEach((sourceId, pending) ->
                copy.pendingSourceDamageForReflection.put(sourceId, pending.copy()));
        copy.pendingLifelinkGains.addAll(this.pendingLifelinkGains);
        copy.pendingProliferateLinks.putAll(this.pendingProliferateLinks);
        copy.openDamageEvent = this.openDamageEvent;
        copy.stateTriggerOnStack.addAll(this.stateTriggerOnStack);
        copy.foretoldCardIds.addAll(this.foretoldCardIds);
        copy.exiledCardsWithIceCounters.addAll(this.exiledCardsWithIceCounters);
        copy.exiledCardsWithBloodCounters.addAll(this.exiledCardsWithBloodCounters);
        copy.exiledCardsWithCroakCounters.addAll(this.exiledCardsWithCroakCounters);
        copy.exiledCardsWithVoidCounters.addAll(this.exiledCardsWithVoidCounters);
        copy.exiledCardsWithDiscoveryCounters.addAll(this.exiledCardsWithDiscoveryCounters);
        copy.exiledCardsWithCollectionCounters.addAll(this.exiledCardsWithCollectionCounters);
        copy.exiledCardsWithFetchCounters.addAll(this.exiledCardsWithFetchCounters);
        copy.exiledCardsWithHatchingCounters.addAll(this.exiledCardsWithHatchingCounters);
        copy.exiledCardsWithIntelCounters.addAll(this.exiledCardsWithIntelCounters);
        copy.exiledCardsWithMemoryCounters.addAll(this.exiledCardsWithMemoryCounters);
        copy.exiledCardsWithTakeoverCounters.addAll(this.exiledCardsWithTakeoverCounters);
        copy.exiledCardsWithBrainCounters.addAll(this.exiledCardsWithBrainCounters);
        copy.exiledCardsWithCageCounters.addAll(this.exiledCardsWithCageCounters);

        // --- List<UUID> (synchronized) ---
        copy.orderedPlayerIds.addAll(this.orderedPlayerIds);
        copy.playerNames.addAll(this.playerNames);

        // --- Map<UUID, String/Integer> ---
        copy.playerIdToName.putAll(this.playerIdToName);
        copy.imprintedCards.putAll(this.imprintedCards);
        this.perpetualEnterEffectsByCardId.forEach((cardId, effects) ->
                copy.perpetualEnterEffectsByCardId.put(cardId,
                        Collections.synchronizedList(new ArrayList<>(effects))));
        copy.perpetualCardPowerToughnessModifiers.putAll(this.perpetualCardPowerToughnessModifiers);
        copy.perpetualCardBasePowerToughness.putAll(this.perpetualCardBasePowerToughness);
        this.perpetualCardBattlefieldEffectGrants.forEach((cardId, effects) ->
                copy.perpetualCardBattlefieldEffectGrants.put(cardId,
                        Collections.synchronizedList(new ArrayList<>(effects))));
        this.perpetualCardSpellCastingAbilityGrants.forEach((cardId, grants) ->
                copy.perpetualCardSpellCastingAbilityGrants.put(cardId,
                        Collections.synchronizedList(new ArrayList<>(grants))));
        copy.perpetualPowerToughnessModifiers.putAll(this.perpetualPowerToughnessModifiers);
        this.perpetualKeywords.forEach((cardId, keywords) ->
                copy.perpetualKeywords.put(cardId, Set.copyOf(keywords)));
        copy.perpetualToxicValues.putAll(this.perpetualToxicValues);
        copy.perpetualGenericCastCostIncreases.putAll(this.perpetualGenericCastCostIncreases);
        copy.perpetualManaCostIncreases.putAll(this.perpetualManaCostIncreases);
        copyDamageBonusGrants(this.perpetualNoncombatDamageBonuses, copy.perpetualNoncombatDamageBonuses);
        copy.perpetualAnyColorManaForCastCardIds.addAll(this.perpetualAnyColorManaForCastCardIds);
        copy.perpetualEnterTappedCardIds.addAll(this.perpetualEnterTappedCardIds);
        copy.perpetualExileInsteadOfDyingCardIds.addAll(this.perpetualExileInsteadOfDyingCardIds);
        this.perpetualTriggeredAbilities.forEach((cardId, effects) ->
                copy.perpetualTriggeredAbilities.put(cardId, List.copyOf(effects)));
        this.perpetuallyRemovedTriggeredAbilities.forEach((cardId, effects) ->
                copy.perpetuallyRemovedTriggeredAbilities.put(cardId, Set.copyOf(effects)));
        copy.perpetualTriggeredAbilityGrants.putAll(this.perpetualTriggeredAbilityGrants);
        this.perpetualGraveyardAbilities.forEach((cardId, abilities) ->
                copy.perpetualGraveyardAbilities.put(cardId, List.copyOf(abilities)));
        this.perpetualCardTypes.forEach((cardId, types) ->
                copy.perpetualCardTypes.put(cardId, Set.copyOf(types)));
        this.perpetualCardSubtypes.forEach((cardId, subtypes) ->
                copy.perpetualCardSubtypes.put(cardId, Set.copyOf(subtypes)));
        copy.perpetualCreatureTypeReplacedCardIds.addAll(this.perpetualCreatureTypeReplacedCardIds);
        this.perpetualActivatedAbilities.forEach((cardId, abilities) ->
                copy.perpetualActivatedAbilities.put(cardId, List.copyOf(abilities)));
        this.perpetualEvokeAlternateCasts.forEach((cardId, alternateCasts) ->
                copy.perpetualEvokeAlternateCasts.put(cardId, List.copyOf(alternateCasts)));
        copy.perpetualCardCastCostReductions.putAll(this.perpetualCardCastCostReductions);
        copy.perpetualCardCastCostIncreases.putAll(this.perpetualCardCastCostIncreases);
        copy.perpetualCardPowerModifiers.putAll(this.perpetualCardPowerModifiers);
        copy.cardsGrantedPerpetualUnearth.addAll(this.cardsGrantedPerpetualUnearth);
        this.perpetualCardRemovedKeywords.forEach((cardId, keywords) -> {
            Set<Keyword> copiedKeywords = ConcurrentHashMap.newKeySet();
            copiedKeywords.addAll(keywords);
            copy.perpetualCardRemovedKeywords.put(cardId, copiedKeywords);
        });
        this.perpetualCardExtraKeywordInstances.forEach((cardId, instances) ->
                copy.perpetualCardExtraKeywordInstances.put(cardId, new ConcurrentHashMap<>(instances)));
        copy.legacyChosenWordsByCardId.putAll(this.legacyChosenWordsByCardId);
        copy.secretMissionChoices.putAll(this.secretMissionChoices);
        copy.exiledVoyageCounters.putAll(this.exiledVoyageCounters);
        copy.exiledVoyageControllerIds.putAll(this.exiledVoyageControllerIds);
        copy.foretoldCardCosts.putAll(this.foretoldCardCosts);
        copy.foretoldCardTurnsTaken.putAll(this.foretoldCardTurnsTaken);
        this.notedMana.forEach((cardId, mana) -> copy.notedMana.put(cardId, new EnumMap<>(mana)));
        this.abilityActivationManaSpent.forEach((cardId, mana) ->
                copy.abilityActivationManaSpent.put(cardId, new EnumMap<>(mana)));
        copy.abilityActivationTreasureManaSpent.putAll(this.abilityActivationTreasureManaSpent);
        copy.abilityActivationUsedTreasureMana.putAll(this.abilityActivationUsedTreasureMana);
        copy.playerDeckChoices.putAll(this.playerDeckChoices);
        copy.startingDeckSizes.putAll(this.startingDeckSizes);
        this.startingDeckCardIds.forEach((playerId, cardIds) ->
                copy.startingDeckCardIds.put(playerId, new HashSet<>(cardIds)));
        copy.cardIntensities.putAll(this.cardIntensities);
        copy.mulliganCounts.putAll(this.mulliganCounts);
        copy.playerNeedsToBottom.putAll(this.playerNeedsToBottom);
        copy.playerBottomedBeforeKeeping.addAll(this.playerBottomedBeforeKeeping);
        copy.playerMulliganDecisionIds.putAll(this.playerMulliganDecisionIds);
        copy.playerBottomDecisionIds.putAll(this.playerBottomDecisionIds);
        copy.landsPlayedThisTurn.putAll(this.landsPlayedThisTurn);
        this.landCardsPlayedThisTurn.forEach((playerId, lands) ->
                copy.landCardsPlayedThisTurn.put(playerId, new ArrayList<>(lands)));
        copy.turnsTakenByPlayer.putAll(this.turnsTakenByPlayer);
        copy.additionalLandsThisTurn.putAll(this.additionalLandsThisTurn);
        this.permanentsEnteredBattlefieldThisTurn.forEach((k, v) ->
                copy.permanentsEnteredBattlefieldThisTurn.put(k, new ArrayList<>(v)));
        this.permanentsSacrificedThisTurn.forEach((k, v) ->
                copy.permanentsSacrificedThisTurn.put(k, new ArrayList<>(v)));
        copy.currentCastSacrificedPermanentIds.addAll(this.currentCastSacrificedPermanentIds);
        this.faceDownCreaturesEnteredBattlefieldThisTurn.forEach((k, v) ->
                copy.faceDownCreaturesEnteredBattlefieldThisTurn.put(k, new ArrayList<>(v)));
        this.faceDownPermanentsEnteredBattlefieldThisTurn.forEach((k, v) ->
                copy.faceDownPermanentsEnteredBattlefieldThisTurn.put(k, new ArrayList<>(v)));
        copy.playersWhoTurnedPermanentsFaceUpThisTurn.addAll(this.playersWhoTurnedPermanentsFaceUpThisTurn);
        copy.permanentsTurnedFaceUpThisTurn.addAll(this.permanentsTurnedFaceUpThisTurn);
        this.permanentsEnteredBattlefieldLastTurn.forEach((k, v) ->
                copy.permanentsEnteredBattlefieldLastTurn.put(k, new ArrayList<>(v)));
        this.permanentsEnteredSincePlayerLastTurnEnded.forEach((k, v) ->
                copy.permanentsEnteredSincePlayerLastTurnEnded.put(k, ConcurrentHashMap.newKeySet()));
        this.permanentsEnteredSincePlayerLastTurnEnded.forEach((k, v) ->
                copy.permanentsEnteredSincePlayerLastTurnEnded.get(k).addAll(v));
        copy.playersWhoActedDuringTheirTurn.addAll(this.playersWhoActedDuringTheirTurn);
        copy.playersWhoActedDuringTheirLastTurn.addAll(this.playersWhoActedDuringTheirLastTurn);
        this.spellsCastThisTurn.forEach((k, v) ->
                copy.spellsCastThisTurn.put(k, new ArrayList<>(v)));
        copy.spellManaValuesWhenCastThisTurn.putAll(this.spellManaValuesWhenCastThisTurn);
        this.spellsCastTargetingCreatureThisTurn.forEach((k, v) ->
                copy.spellsCastTargetingCreatureThisTurn.put(k, ConcurrentHashMap.newKeySet()));
        this.spellsCastTargetingCreatureThisTurn.forEach((k, v) ->
                copy.spellsCastTargetingCreatureThisTurn.get(k).addAll(v));
        copy.spellsCastUsingTreasureManaThisTurn.addAll(this.spellsCastUsingTreasureManaThisTurn);
        copy.spellsCastWithConvokeGrantThisTurn.addAll(this.spellsCastWithConvokeGrantThisTurn);
        this.spellCastCountsByZoneThisTurn.forEach((k, v) ->
                copy.spellCastCountsByZoneThisTurn.put(k, new ConcurrentHashMap<>(v)));
        this.spellCardsCastByZoneThisTurn.forEach((k, v) -> {
            Map<Zone, List<Card>> zoneCopy = new ConcurrentHashMap<>();
            v.forEach((zone, spells) -> zoneCopy.put(zone, Collections.synchronizedList(new ArrayList<>(spells))));
            copy.spellCardsCastByZoneThisTurn.put(k, zoneCopy);
        });
        this.kickedSpellsCastThisTurn.forEach((k, v) ->
                copy.kickedSpellsCastThisTurn.put(k, ConcurrentHashMap.newKeySet()));
        this.kickedSpellsCastThisTurn.forEach((k, v) ->
                copy.kickedSpellsCastThisTurn.get(k).addAll(v));
        copy.spellWarpedThisTurn = this.spellWarpedThisTurn;
        copy.spellCastOrderThisTurn.addAll(this.spellCastOrderThisTurn);
        copy.artifactSpellsCastThisTurn.addAll(this.artifactSpellsCastThisTurn);
        copy.spellCastStackManaValuesThisTurn.putAll(this.spellCastStackManaValuesThisTurn);
        copy.mostRecentSpellCastThisTurn = this.mostRecentSpellCastThisTurn;
        this.spellNameCastCountsThisGame.forEach((k, v) ->
                copy.spellNameCastCountsThisGame.put(k, new ConcurrentHashMap<>(v)));
        copy.commanderCastsFromCommandZoneThisGame.putAll(this.commanderCastsFromCommandZoneThisGame);
        this.cardNameCycleCountsThisGame.forEach((k, v) ->
                copy.cardNameCycleCountsThisGame.put(k, new ConcurrentHashMap<>(v)));
        copy.spellsCastLastTurn.putAll(this.spellsCastLastTurn);
        this.spellsCastLastTurnCards.forEach((k, v) ->
                copy.spellsCastLastTurnCards.put(k, new ArrayList<>(v)));
        copy.manaSpentToCastSpellsThisTurn.putAll(this.manaSpentToCastSpellsThisTurn);
        copy.dayNight = this.dayNight;
        copy.monarchPlayerId = this.monarchPlayerId;
        copy.initiativePlayerId = this.initiativePlayerId;
        copy.ringLevels.putAll(this.ringLevels);
        copy.ringBearerIds.putAll(this.ringBearerIds);
        copy.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn
                .addAll(this.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn);
        copy.playersWithCityBlessing.addAll(this.playersWithCityBlessing);
        this.playerBuddyLists.forEach((playerId, buddies) -> {
            Set<CardSubtype> copiedBuddies = ConcurrentHashMap.newKeySet();
            copiedBuddies.addAll(buddies);
            copy.playerBuddyLists.put(playerId, copiedBuddies);
        });
        copy.playersWithEnduringStory.addAll(this.playersWithEnduringStory);
        copy.playersWhoCompletedDungeon.addAll(this.playersWhoCompletedDungeon);
        this.completedDungeonsByPlayer.forEach((playerId, dungeons) -> {
            Set<Dungeon> copiedDungeons = ConcurrentHashMap.newKeySet();
            copiedDungeons.addAll(dungeons);
            copy.completedDungeonsByPlayer.put(playerId, copiedDungeons);
        });
        copy.playerDungeonProgress.putAll(this.playerDungeonProgress);
        copy.playersWhoVenturedIntoDungeonThisTurn.addAll(this.playersWhoVenturedIntoDungeonThisTurn);
        copy.playersWhoSurveilledThisTurn.addAll(this.playersWhoSurveilledThisTurn);
        copy.playersWhoControlledPermanentThatExploredThisTurn
                .addAll(this.playersWhoControlledPermanentThatExploredThisTurn);
        copy.playersWhoWereWayBehindThisTurn.addAll(this.playersWhoWereWayBehindThisTurn);
        copy.playersDeclaredAttackersThisTurn.addAll(this.playersDeclaredAttackersThisTurn);
        copy.playersAttackedThisCombat.addAll(this.playersAttackedThisCombat);
        copy.playersWhoAttackedWithTokenThisTurn.addAll(this.playersWhoAttackedWithTokenThisTurn);
        copy.playersWhoAttackedWithCommanderThisTurn.addAll(this.playersWhoAttackedWithCommanderThisTurn);
        copy.declaredAttackerIdsThisCombat.addAll(this.declaredAttackerIdsThisCombat);
        copy.creaturesThatEnlistedThisCombat.addAll(this.creaturesThatEnlistedThisCombat);
        copy.playersWhoPutCountersOnCreaturesThisTurn.addAll(this.playersWhoPutCountersOnCreaturesThisTurn);
        this.permanentsWithCountersPutByPlayerThisTurn.forEach((playerId, permanentIds) ->
                copy.permanentsWithCountersPutByPlayerThisTurn.put(playerId, new HashSet<>(permanentIds)));
        copy.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn
                .addAll(this.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn);
        copy.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn
                .addAll(this.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn);
        copy.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn
                .addAll(this.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn);
        copy.playersWhoSacrificedPermanentsThisTurn.addAll(this.playersWhoSacrificedPermanentsThisTurn);
        copy.playersWhoCreatedTokensThisTurn.addAll(this.playersWhoCreatedTokensThisTurn);
        copy.dynamicTokenCardIds.addAll(this.dynamicTokenCardIds);
        copy.treasureTokensCreatedThisTurn.putAll(this.treasureTokensCreatedThisTurn);
        copy.permanentsThatFoughtThisTurn.addAll(this.permanentsThatFoughtThisTurn);
        copy.tokensCreatedThisTurn.putAll(this.tokensCreatedThisTurn);
        copy.sacrificedPermanentCountThisTurn.putAll(this.sacrificedPermanentCountThisTurn);
        copy.playersWhoSacrificedArtifactsThisTurn.addAll(this.playersWhoSacrificedArtifactsThisTurn);
        copy.sacrificedPermanentCountThisTurn.putAll(this.sacrificedPermanentCountThisTurn);
        copy.creaturesAttackedCountThisTurn.putAll(this.creaturesAttackedCountThisTurn);
        this.creaturesAttackedWithThisTurn.forEach((k, v) -> {
            Set<UUID> attackers = ConcurrentHashMap.newKeySet();
            attackers.addAll(v);
            copy.creaturesAttackedWithThisTurn.put(k, attackers);
        });
        this.creaturesAttackedCountBySubtypeThisTurn.forEach((playerId, counts) ->
                copy.creaturesAttackedCountBySubtypeThisTurn.put(playerId, new ConcurrentHashMap<>(counts)));
        copy.playerLifeTotals.putAll(this.playerLifeTotals);
        copy.playerPoisonCounters.putAll(this.playerPoisonCounters);
        copy.playerRadCounters.putAll(this.playerRadCounters);
        copy.playersAffectedByMeliraPoisonReplacementThisTurn
                .addAll(this.playersAffectedByMeliraPoisonReplacementThisTurn);
        copy.playerEnergyCounters.putAll(this.playerEnergyCounters);
        copy.energyCountersPaidOrLostThisTurn.putAll(this.energyCountersPaidOrLostThisTurn);
        copy.playerSparkCounters.putAll(this.playerSparkCounters);
        copy.playerExperienceCounters.putAll(this.playerExperienceCounters);
        copy.playerCoolness.putAll(this.playerCoolness);
        copy.playerSpeeds.putAll(this.playerSpeeds);
        copy.playersWhoseSpeedIncreasedThisTurn.addAll(this.playersWhoseSpeedIncreasedThisTurn);
        copy.playersWhoUsedMaxSpeedFreeUnearthThisTurn.addAll(this.playersWhoUsedMaxSpeedFreeUnearthThisTurn);
        copy.stolenCreatures.putAll(this.stolenCreatures);
        copy.cardOwnerOverrides.putAll(this.cardOwnerOverrides);
        this.controlLossUnattachTriggers.forEach((equipmentId, triggers) ->
                copy.controlLossUnattachTriggers.put(equipmentId,
                        Collections.synchronizedList(new ArrayList<>(triggers))));
        this.controlLossTapTriggers.forEach((permanentId, triggers) ->
                copy.controlLossTapTriggers.put(permanentId,
                        Collections.synchronizedList(new ArrayList<>(triggers))));
        copy.drawRedirectsAppliedToCurrentDraw.addAll(this.drawRedirectsAppliedToCurrentDraw);
        copy.chainsDrawReplacementsApplied.putAll(this.chainsDrawReplacementsApplied);
        copy.drawStepFirstDrawTaken.addAll(this.drawStepFirstDrawTaken);
        copy.firstNonDrawStepDrawReplacementsUsedThisTurn
                .addAll(this.firstNonDrawStepDrawReplacementsUsedThisTurn);
        copy.activeMysticReflectionsForEntryBatch.addAll(this.activeMysticReflectionsForEntryBatch);
        copy.cardsDrawnThisTurn.putAll(this.cardsDrawnThisTurn);
        copy.cardsDrawnLastTurn.putAll(this.cardsDrawnLastTurn);
        this.cardsDrawnThisTurnIds.forEach((k, v) -> copy.cardsDrawnThisTurnIds.put(k, new ArrayList<>(v)));
        copy.cardsDiscardedThisTurn.putAll(this.cardsDiscardedThisTurn);
        copy.nonlandCardsDiscardedThisTurn.putAll(this.nonlandCardsDiscardedThisTurn);
        copy.cardsCycledThisTurn.putAll(this.cardsCycledThisTurn);
        copy.discardEventPlayerId = this.discardEventPlayerId;
        copy.discardEventCardCount = this.discardEventCardCount;
        copy.discardEventCards.addAll(this.discardEventCards);
        copy.lastDiscardedCardManaValue = this.lastDiscardedCardManaValue;
        copy.greatestDiscardedCardManaValue = this.greatestDiscardedCardManaValue;
        copy.lastDiscardedCardTypes = Set.copyOf(this.lastDiscardedCardTypes);
        copy.lastMilledCardColorSymbols.putAll(this.lastMilledCardColorSymbols);
        this.eachPlayerSearchDeferredToHand.forEach((playerId, cards) ->
                copy.eachPlayerSearchDeferredToHand.put(playerId, new ArrayList<>(cards)));
        this.eachPlayerSearchDeferredToBattlefield.forEach((playerId, cards) ->
                copy.eachPlayerSearchDeferredToBattlefield.put(playerId, new ArrayList<>(cards)));
        copy.pendingRevealedPowerCostCards.putAll(this.pendingRevealedPowerCostCards);
        copy.pendingDiscardedCostCards.putAll(this.pendingDiscardedCostCards);
        copy.lifeGainedThisTurn.putAll(this.lifeGainedThisTurn);
        this.combatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.combatDamageToPlayersByCreatureNameThisGame.forEach((k, v) ->
                copy.combatDamageToPlayersByCreatureNameThisGame.put(k, new HashSet<>(v)));
        this.combatDamageToPlayersThisCombat.forEach((k, v) ->
                copy.combatDamageToPlayersThisCombat.put(k, new HashSet<>(v)));
        copy.combatDamageDealtToPlayersThisTurn.putAll(this.combatDamageDealtToPlayersThisTurn);
        copy.combatDamageSourcesThatDealtToCreaturesThisTurn
                .addAll(this.combatDamageSourcesThatDealtToCreaturesThisTurn);
        this.noncombatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.noncombatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.permanentDamageSourceNamesToPlayersThisTurn.forEach((k, v) ->
                copy.permanentDamageSourceNamesToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.permanentDamageSourceNamesToPlayersLastTurn.forEach((k, v) ->
                copy.permanentDamageSourceNamesToPlayersLastTurn.put(k, new HashSet<>(v)));
        this.creatureDamageToPlayersThisTurn.forEach((k, v) ->
                copy.creatureDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.playersAttackedThisTurn.forEach((k, v) ->
                copy.playersAttackedThisTurn.put(k, new HashSet<>(v)));
        this.playersAfraidOfControllerAfterAttackThisTurn.forEach((k, v) ->
                copy.playersAfraidOfControllerAfterAttackThisTurn.put(k, new HashSet<>(v)));
        this.declaredAttackerSnapshotsThisTurn.forEach((k, v) ->
                copy.declaredAttackerSnapshotsThisTurn.put(k, new HashSet<>(v)));
        this.playersWhoAttackedPlayerOrPlaneswalkerThisTurn.forEach((k, v) ->
                copy.playersWhoAttackedPlayerOrPlaneswalkerThisTurn.put(k, new HashSet<>(v)));
        this.playersWhoAttackedPlayersThisTurn.forEach((k, v) ->
                copy.playersWhoAttackedPlayersThisTurn.put(k, new HashSet<>(v)));
        this.playersWhoAttackedPlayersLastTurn.forEach((k, v) ->
                copy.playersWhoAttackedPlayersLastTurn.put(k, new HashSet<>(v)));
        copy.damageDealtThisTurnBySource.putAll(this.damageDealtThisTurnBySource);
        this.damageDealtToPlayersBySourceThisTurn.forEach((sourceId, playerDamage) -> {
            Map<UUID, Integer> copiedPlayerDamage = new ConcurrentHashMap<>();
            copiedPlayerDamage.putAll(playerDamage);
            copy.damageDealtToPlayersBySourceThisTurn.put(sourceId, copiedPlayerDamage);
        });
        copy.sorcerySpellDamageDealtThisTurn.putAll(this.sorcerySpellDamageDealtThisTurn);
        copy.permanentsThatHaveDealtDamage.addAll(this.permanentsThatHaveDealtDamage);
        copy.sourcePermanentsThatDealtDamageToCreaturesThisTurn
                .addAll(this.sourcePermanentsThatDealtDamageToCreaturesThisTurn);
        this.damageRecipientsBySource.forEach((k, v) -> {
            Set<UUID> recipients = ConcurrentHashMap.newKeySet();
            recipients.addAll(v);
            copy.damageRecipientsBySource.put(k, recipients);
        });
        this.damageSourcesControlledByPlayerThisTurn.forEach((k, v) -> {
            Set<UUID> sources = ConcurrentHashMap.newKeySet();
            sources.addAll(v);
            copy.damageSourcesControlledByPlayerThisTurn.put(k, sources);
        });
        copy.playersDealtDamageThisTurn.addAll(this.playersDealtDamageThisTurn);
        copy.damageDealtToPlayersThisTurn.putAll(this.damageDealtToPlayersThisTurn);
        copy.artifactDamageDealtToPlayersThisTurn.putAll(this.artifactDamageDealtToPlayersThisTurn);
        copy.noncombatDamageDealtToPlayersThisTurn.putAll(this.noncombatDamageDealtToPlayersThisTurn);
        copy.redSourceNoncombatDamageThisTurn.putAll(this.redSourceNoncombatDamageThisTurn);
        copy.lastRedSpellDamagerThisTurn.putAll(this.lastRedSpellDamagerThisTurn);
        copy.untappedLandsAtTurnStart.putAll(this.untappedLandsAtTurnStart);
        copy.handSizeAtTurnStart.putAll(this.handSizeAtTurnStart);
        copy.permanentsDealtDamageThisTurn.addAll(this.permanentsDealtDamageThisTurn);
        copy.permanentsDealtNoncombatDamageThisTurn.addAll(this.permanentsDealtNoncombatDamageThisTurn);
        copy.permanentsDealtExcessDamageThisTurn.addAll(this.permanentsDealtExcessDamageThisTurn);
        copy.damageDealtToPermanentsThisTurn.putAll(this.damageDealtToPermanentsThisTurn);
        this.damageDealtToPermanentsBySourceThisTurn.forEach((k, v) -> {
            Map<UUID, Integer> sources = new ConcurrentHashMap<>();
            sources.putAll(v);
            copy.damageDealtToPermanentsBySourceThisTurn.put(k, sources);
        });
        this.damageDealtToPermanentsBySourceControllerThisTurn.forEach((k, v) -> {
            Map<UUID, Integer> controllers = new ConcurrentHashMap<>();
            controllers.putAll(v);
            copy.damageDealtToPermanentsBySourceControllerThisTurn.put(k, controllers);
        });
        copy.damageSourceNamesThisTurn.putAll(this.damageSourceNamesThisTurn);
        copy.controllersOfPermanentsDealtExcessDamageThisTurn.addAll(
                this.controllersOfPermanentsDealtExcessDamageThisTurn);
        this.qualifyingDamageControllersByPermanentThisTurn.forEach((k, v) -> {
            Set<UUID> controllers = ConcurrentHashMap.newKeySet();
            controllers.addAll(v);
            copy.qualifyingDamageControllersByPermanentThisTurn.put(k, controllers);
        });
        copy.freeCastPermanentUsedThisTurn.addAll(this.freeCastPermanentUsedThisTurn);
        copy.oncePerTurnExileCastPermissionsUsedThisTurn.addAll(this.oncePerTurnExileCastPermissionsUsedThisTurn);
        copy.oneShotExileCastPermissionsUsed.addAll(this.oneShotExileCastPermissionsUsed);
        copy.oncePerTurnLibraryCastPermissionsUsedThisTurn.addAll(this.oncePerTurnLibraryCastPermissionsUsedThisTurn);
        copy.oncePerTurnLibraryPlayPermissionsUsedThisTurn.addAll(this.oncePerTurnLibraryPlayPermissionsUsedThisTurn);
        copy.permanentsWithCountersPutThisTurn.addAll(this.permanentsWithCountersPutThisTurn);
        this.keyedOnceOnlyTriggersFired.forEach((sourceId, keys) -> {
            Set<String> copiedKeys = ConcurrentHashMap.newKeySet();
            copiedKeys.addAll(keys);
            copy.keyedOnceOnlyTriggersFired.put(sourceId, copiedKeys);
        });
        this.firstCardCycledFreeUsesThisTurn.forEach((k, v) ->
                copy.firstCardCycledFreeUsesThisTurn.put(k, new HashSet<>(v)));
        this.keyedOncePerTurnTriggersFiredThisTurn.forEach((k, v) -> {
            Set<String> keys = ConcurrentHashMap.newKeySet();
            keys.addAll(v);
            copy.keyedOncePerTurnTriggersFiredThisTurn.put(k, keys);
        });
        this.oncePerCreatureTriggersFiredThisTurn.forEach((k, v) ->
                copy.oncePerCreatureTriggersFiredThisTurn.put(k, new HashSet<>(v)));
        copy.creaturesThatReceivedCountersThisTurn.addAll(this.creaturesThatReceivedCountersThisTurn);
        copy.creatureTapCountsThisTurn.putAll(this.creatureTapCountsThisTurn);
        copy.permanentsThatAddedManaWithAbilityThisTurn.addAll(this.permanentsThatAddedManaWithAbilityThisTurn);
        this.firstResolutionTriggerKeysThisTurn.forEach((k, v) -> {
            Set<String> keys = ConcurrentHashMap.newKeySet();
            keys.addAll(v);
            copy.firstResolutionTriggerKeysThisTurn.put(k, keys);
        });
        copy.permanentsExertedThisTurn.addAll(this.permanentsExertedThisTurn);
        this.bendingTypesCompletedThisTurn.forEach((k, v) ->
                copy.bendingTypesCompletedThisTurn.put(k, new java.util.HashSet<>(v)));
        copy.tokenCreationReplacementUsedThisTurn.addAll(this.tokenCreationReplacementUsedThisTurn);
        copy.pendingTokenCreationReplacement = this.pendingTokenCreationReplacement;
        copy.simultaneousDyingCreatures.putAll(this.simultaneousDyingCreatures);
        copy.simultaneousDyingControllers.putAll(this.simultaneousDyingControllers);
        copy.simultaneousDyingPermanents.putAll(this.simultaneousDyingPermanents);
        this.departedPermanentSnapshots.forEach((cardId, permanent) ->
                copy.departedPermanentSnapshots.put(cardId, new Permanent(permanent)));
        copy.departedPermanentControllerIds.putAll(this.departedPermanentControllerIds);
        copy.simultaneousDyingPermanentControllers.putAll(this.simultaneousDyingPermanentControllers);
        copy.simultaneousDyingPowers.putAll(this.simultaneousDyingPowers);
        copy.simultaneousDyingTriggerKeywordInstances.putAll(this.simultaneousDyingTriggerKeywordInstances);
        this.simultaneousDyingGrantedZoneChangeAbilities.forEach((permanentId, abilities) ->
                copy.simultaneousDyingGrantedZoneChangeAbilities.put(permanentId, List.copyOf(abilities)));
        this.combatDamageSourceSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageSourceSubtypesThisTurn.put(k, new HashSet<>(v)));
        copy.combatDamageSourceNamesThisTurn.putAll(this.combatDamageSourceNamesThisTurn);
        copy.combatDamageSourcesWithChangelingThisTurn.addAll(this.combatDamageSourcesWithChangelingThisTurn);
        copy.combatDamageSourcesWithLegendaryThisTurn.addAll(this.combatDamageSourcesWithLegendaryThisTurn);
        this.legendaryCreatureCombatDamageToPlayersThisTurn.forEach((sourceId, playerIds) ->
                copy.legendaryCreatureCombatDamageToPlayersThisTurn.put(sourceId, new HashSet<>(playerIds)));
        this.combatDamageToPlayerControllerSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayerControllerSubtypesThisTurn.put(k, new HashSet<>(v)));
        this.combatDamageDealersByControllerThisTurn.forEach((k, v) ->
                copy.combatDamageDealersByControllerThisTurn.put(k, new HashSet<>(v)));
        copy.controllersDealtCombatDamageWithChangelingThisTurn.addAll(this.controllersDealtCombatDamageWithChangelingThisTurn);
        copy.combatDamageSourcesThatWereCommandersThisTurn.addAll(this.combatDamageSourcesThatWereCommandersThisTurn);
        this.combatBlockOpponentSubtypesThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentSubtypesThisTurn.put(k, new HashSet<>(v)));
        this.combatBlockOpponentColorsThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentColorsThisTurn.put(k, new HashSet<>(v)));
        copy.creaturesInCombatWithChangelingThisTurn.addAll(this.creaturesInCombatWithChangelingThisTurn);
        copy.creaturesBlockedThisTurn.addAll(this.creaturesBlockedThisTurn);
        copy.creaturesEnteredBlockingThisTurn.addAll(this.creaturesEnteredBlockingThisTurn);
        this.combatBlockOpponentIdsThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentIdsThisTurn.put(k, new HashSet<>(v)));
        this.combatBlockOpponentControllerIdsThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentControllerIdsThisTurn.put(k, new HashMap<>(v)));
        this.combatBlockOpponentIdsThisCombat.forEach((k, v) ->
                copy.combatBlockOpponentIdsThisCombat.put(k, new HashSet<>(v)));
        this.combatOpponentIdsBlockedByThisTurn.forEach((k, v) ->
                copy.combatOpponentIdsBlockedByThisTurn.put(k, new HashSet<>(v)));

        // --- Map<UUID, Set<TurnStep>> ---
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.put(k, new HashSet<>(v)));
        copy.playersAutoOrderingTriggers.addAll(this.playersAutoOrderingTriggers);

        // --- Map<UUID, List<Card>> (shared Card refs) ---
        this.playerDecks.forEach((k, v) -> copy.playerDecks.put(k, new ArrayList<>(v)));
        this.countersPreservedAcrossZoneChanges.forEach((k, v) ->
                copy.countersPreservedAcrossZoneChanges.put(k, new EnumMap<>(v)));
        this.playerSideboards.forEach((k, v) -> copy.playerSideboards.put(k, new ArrayList<>(v)));
        this.playerHands.forEach((k, v) -> copy.playerHands.put(k, new ArrayList<>(v)));
        this.playerGraveyards.forEach((k, v) -> copy.playerGraveyards.put(k, new ArrayList<>(v)));
        this.playerCommandZones.forEach((k, v) -> copy.playerCommandZones.put(k, new ArrayList<>(v)));
        copy.faceDownCommandZoneCards.addAll(this.faceDownCommandZoneCards);
        this.playerSchemeDecks.forEach((k, v) -> copy.playerSchemeDecks.put(k, new ArrayList<>(v)));
        this.playerCommanders.forEach((k, v) -> copy.playerCommanders.put(k, new ArrayList<>(v)));
        copy.commanderTaxByCardId.putAll(this.commanderTaxByCardId);
        copy.commanderCastsFromCommandZoneThisGame.putAll(this.commanderCastsFromCommandZoneThisGame);
        copy.commanderReturnCandidates.addAll(this.commanderReturnCandidates);
        copy.pendingCommanderZoneMoves.addAll(this.pendingCommanderZoneMoves);
        this.commanderBounceContexts.forEach((id, context) -> copy.commanderBounceContexts.put(id,
                new CommanderBounceContext(new Permanent(context.permanent()), context.controllerId(), context.ownerId(), context.wasCreature())));
        copy.pendingCommandCasts.putAll(this.pendingCommandCasts);
        copy.format = this.format;
        copy.acceptedDecks.putAll(this.acceptedDecks);
        this.commanderDamageReceived.forEach((id, damage) -> copy.commanderDamageReceived.put(id, new java.util.HashMap<>(damage)));
        copy.exiledCards.addAll(this.exiledCards);
        copy.bombardmentOriginalCardsUntilEndOfTurn.putAll(this.bombardmentOriginalCardsUntilEndOfTurn);
        copy.hauntingCardToPermanentId.putAll(this.hauntingCardToPermanentId);
        copy.antedCardIds.addAll(this.antedCardIds);
        copy.rebirthAnteAcceptedPlayerIds.addAll(this.rebirthAnteAcceptedPlayerIds);
        copy.exiledCardEggCounters.putAll(this.exiledCardEggCounters);
        copy.exiledCardScreamCounters.putAll(this.exiledCardScreamCounters);
        copy.exiledCardTimeCounters.putAll(this.exiledCardTimeCounters);
        copy.exiledCardsWithNonSuspendTimeCounters.addAll(this.exiledCardsWithNonSuspendTimeCounters);
        copy.exiledCardsWithAlaundoCastAbility.addAll(this.exiledCardsWithAlaundoCastAbility);
        copy.cardsExiledFromGraveyardThisTurn.addAll(this.cardsExiledFromGraveyardThisTurn);
        copy.exiledCardDreamCounters.putAll(this.exiledCardDreamCounters);
        copy.exiledCardHitCounters.putAll(this.exiledCardHitCounters);
        copy.exiledCardRefineCounters.putAll(this.exiledCardRefineCounters);
        copy.spellsWithDreamCounterOnResolution.addAll(this.spellsWithDreamCounterOnResolution);
        copy.exiledCardsWithSilverCounters.addAll(this.exiledCardsWithSilverCounters);
        copy.exiledCardsWithStudyCounters.addAll(this.exiledCardsWithStudyCounters);
        copy.exiledCardsWithFetchCounters.addAll(this.exiledCardsWithFetchCounters);
        copy.exiledCardsWithKickCounters.addAll(this.exiledCardsWithKickCounters);
        copy.exiledCardsWithMemoryCounters.addAll(this.exiledCardsWithMemoryCounters);
        copy.exiledCardsWithTakeoverCounters.addAll(this.exiledCardsWithTakeoverCounters);
        copy.exiledCardsWithBrainCounters.addAll(this.exiledCardsWithBrainCounters);
        copy.exiledCardsWithCageCounters.addAll(this.exiledCardsWithCageCounters);
        copy.delayedSpellExiles.addAll(this.delayedSpellExiles);
        copy.suspendedSpellExiles.addAll(this.suspendedSpellExiles);

        // --- Map<UUID, List<Permanent>> (deep copy each Permanent) ---
        this.playerBattlefields.forEach((k, v) -> {
            List<Permanent> battlefieldCopy = copy.newBattlefieldList();
            v.stream().map(Permanent::new).forEach(battlefieldCopy::add);
            copy.playerBattlefields.put(k, battlefieldCopy);
        });
        this.phasedOutPermanents.forEach((k, v) -> copy.phasedOutPermanents.put(k,
                Collections.synchronizedList(v.stream().map(Permanent::new)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))));
        this.phasedOutWhileSourceControlled.forEach((sourceId, targetSequences) -> {
            Map<UUID, Integer> targetSequenceCopy = new ConcurrentHashMap<>();
            targetSequenceCopy.putAll(targetSequences);
            copy.phasedOutWhileSourceControlled.put(sourceId, targetSequenceCopy);
        });
        this.phasedOutWhileSourceTapped.forEach((sourceId, targetIds) -> {
            Set<UUID> targetIdCopy = ConcurrentHashMap.newKeySet();
            targetIdCopy.addAll(targetIds);
            copy.phasedOutWhileSourceTapped.put(sourceId, targetIdCopy);
        });

        // --- Map<UUID, ManaPool> (deep copy each ManaPool) ---
        this.playerManaPools.forEach((k, v) -> copy.playerManaPools.put(k, new ManaPool(v)));

        // --- List<StackEntry> (deep copy each StackEntry) ---
        this.stack.forEach(se -> copy.stack.add(new StackEntry(se)));
        // A resolving instant or sorcery is still on the stack (CR 608.2n), so its parked entry must
        // stay the same object as its stack copy for the final step to remove it.
        copy.pendingEffectResolutionEntry = copyOnStack(copy, this.pendingEffectResolutionEntry,
                copy.pendingEffectResolutionEntry);
        copy.wordOfCommandPendingResolutionEntry = copyOnStack(copy, this.wordOfCommandPendingResolutionEntry,
                copy.wordOfCommandPendingResolutionEntry);
        // A parked resolution and its nested one keep their identities: the nested entry is the pending one or the
        // next parked one, and a parked spell is still the object on the stack.
        Map<StackEntry, StackEntry> parkedCopies = new IdentityHashMap<>();
        if (this.pendingEffectResolutionEntry != null) {
            parkedCopies.put(this.pendingEffectResolutionEntry, copy.pendingEffectResolutionEntry);
        }
        List<ParkedResolution> parkedInnermostFirst = new ArrayList<>(this.parkedOuterResolutions);
        for (ParkedResolution parked : parkedInnermostFirst) {
            StackEntry nestedCopy = parkedCopies.computeIfAbsent(parked.nested(), StackEntry::new);
            StackEntry entryCopy = parkedCopies.computeIfAbsent(parked.entry(),
                    entry -> copyOnStack(copy, entry, new StackEntry(entry)));
            copy.parkedOuterResolutions.addLast(new ParkedResolution(entryCopy, parked.index(), nestedCopy));
        }
        this.pendingManaAbilityTriggers.forEach(se -> copy.pendingManaAbilityTriggers.add(new StackEntry(se)));
        this.pendingSpellCastCostTriggers.forEach(se -> copy.pendingSpellCastCostTriggers.add(new StackEntry(se)));
        this.pendingActivatedAbilityCostTriggers.forEach(se -> copy.pendingActivatedAbilityCostTriggers.add(new StackEntry(se)));
        copy.pendingActivationCostSacrificeIds.addAll(this.pendingActivationCostSacrificeIds);

        // --- InteractionState ---
        InteractionState copiedInteraction = this.interaction.deepCopy();
        copyInteractionInto(copy, copiedInteraction);

        // --- Map<UUID, Set<UUID>> ---
        this.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) ->
                copy.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, new HashSet<>(v)));
        copy.artifactsPutIntoGraveyardFromBattlefieldThisTurn = this.artifactsPutIntoGraveyardFromBattlefieldThisTurn;
        copy.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn
                .addAll(this.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn);
        copy.playersWhoDescendedThisTurn.addAll(this.playersWhoDescendedThisTurn);
        copy.descentsThisTurn.putAll(this.descentsThisTurn);
        this.cardsPutIntoGraveyardFromAnywhereThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromAnywhereThisTurn.put(k, new HashSet<>(v)));
        copy.cardsPutIntoGraveyardAgainThisTurn.putAll(this.cardsPutIntoGraveyardAgainThisTurn);
        this.cardsPutIntoGraveyardFromLibraryThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromLibraryThisTurn.put(k, new HashSet<>(v)));
        this.cardsMilledThisTurn.forEach((k, v) ->
                copy.cardsMilledThisTurn.put(k, new HashSet<>(v)));
        this.cardsSurveilledThisTurn.forEach((k, v) ->
                copy.cardsSurveilledThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoHandThisTurn.forEach((k, v) ->
                copy.cardsPutIntoHandThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoGraveyardFromHandThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromHandThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoGraveyardFromHandOrLibraryThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromHandOrLibraryThisTurn.put(k, new HashSet<>(v)));
        this.creatureCardsPutIntoGraveyardFromAnywhereThisTurn.forEach((k, v) ->
                copy.creatureCardsPutIntoGraveyardFromAnywhereThisTurn.put(k, new HashSet<>(v)));
        copy.creatureCardGraveyardEntriesThisTurn.putAll(this.creatureCardGraveyardEntriesThisTurn);
        copy.landCardGraveyardEntriesThisTurn.putAll(this.landCardGraveyardEntriesThisTurn);
        this.cardsPutIntoGraveyardThisCombat.forEach((k, v) ->
                copy.cardsPutIntoGraveyardThisCombat.put(k, new HashSet<>(v)));
        copy.playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn
                .addAll(this.playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn);
        copy.playersWhoControlledLandPutIntoGraveyardFromBattlefieldThisTurn
                .addAll(this.playersWhoControlledLandPutIntoGraveyardFromBattlefieldThisTurn);
        this.cardsDiscardedOrCycledThisTurn.forEach((k, v) ->
                copy.cardsDiscardedOrCycledThisTurn.put(k, new HashSet<>(v)));
        copy.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn
                .addAll(this.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn);
        this.cardsDiscardedByOpponentThisTurn.forEach((k, v) ->
                copy.cardsDiscardedByOpponentThisTurn.put(k, new HashSet<>(v)));
        copy.playersWhosePermanentsLeftBattlefieldThisTurn
                .addAll(this.playersWhosePermanentsLeftBattlefieldThisTurn);
        copy.creatureLeftBattlefieldCountThisTurn.putAll(this.creatureLeftBattlefieldCountThisTurn);
        copy.nonlandPermanentLeftBattlefieldThisTurn = this.nonlandPermanentLeftBattlefieldThisTurn;
        copy.creatureDeathCountThisTurn.putAll(this.creatureDeathCountThisTurn);
        copy.creatureNamesDiedThisTurn.addAll(this.creatureNamesDiedThisTurn);
        copy.creaturePermanentIdsDiedThisTurn.addAll(this.creaturePermanentIdsDiedThisTurn);
        copy.playersWhoControlledModifiedCreatureDiedThisTurn
                .addAll(this.playersWhoControlledModifiedCreatureDiedThisTurn);
        copy.creaturesPutIntoOwnGraveyardThisTurnCount.putAll(this.creaturesPutIntoOwnGraveyardThisTurnCount);
        copy.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount.putAll(
                this.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount);
        copy.nontokenCreatureDeathCountThisTurn.putAll(this.nontokenCreatureDeathCountThisTurn);
        copy.creatureExileCountThisTurn.putAll(this.creatureExileCountThisTurn);
        this.creatureSubtypeDeathCountThisTurn.forEach((k, v) ->
                copy.creatureSubtypeDeathCountThisTurn.put(k, new HashMap<>(v)));
        this.creatureSubtypeDeathPowerThisTurn.forEach((k, v) ->
                copy.creatureSubtypeDeathPowerThisTurn.put(k, new HashMap<>(v)));
        this.sacrificedPermanentSubtypeCountThisTurn.forEach((k, v) ->
                copy.sacrificedPermanentSubtypeCountThisTurn.put(k, new HashMap<>(v)));
        this.creatureCardsDamagedThisTurnBySourcePermanent.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnBySourcePermanent.put(k, new HashSet<>(v)));
        copy.exileDamagedCreaturesSourceIdsThisTurn.addAll(this.exileDamagedCreaturesSourceIdsThisTurn);
        this.creatureCardsDamagedThisTurnBySource.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnBySource.put(k, new HashSet<>(v)));
        this.creatureCardsDamagedThisTurnByController.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnByController.put(k, new HashSet<>(v)));
        copy.sourcesThatDealtDamageToCreaturesThisTurn.addAll(this.sourcesThatDealtDamageToCreaturesThisTurn);
        copy.sourcesWhoseDamagedCreaturesDiedThisTurn.addAll(this.sourcesWhoseDamagedCreaturesDiedThisTurn);
        this.creatureCardsDamagedBySourceThatDiedThisTurn.forEach((k, v) ->
                copy.creatureCardsDamagedBySourceThatDiedThisTurn.put(k, new HashSet<>(v)));
        this.exploitedPermanentIdsBySource.forEach((k, v) ->
                copy.exploitedPermanentIdsBySource.put(k, new HashSet<>(v)));
        this.seraphReturnedCreatures.forEach((k, v) ->
                copy.seraphReturnedCreatures.put(k, new HashSet<>(v)));
        copy.seraphControlWatch.putAll(this.seraphControlWatch);
        copy.seraphLinkedController.putAll(this.seraphLinkedController);
        copy.exiledCardsToGraveyardOnControlLossWatch.putAll(this.exiledCardsToGraveyardOnControlLossWatch);
        this.sourceCreatedTokens.forEach((k, v) ->
                copy.sourceCreatedTokens.put(k, new HashSet<>(v)));

        // --- Map<UUID, Map<CardColor, Integer>> ---

        // --- PendingMayAbility list (records with shared Card refs) ---
        this.pendingMayAbilities.stream().map(PendingMayAbility::deepCopyEntryRequests)
                .forEach(copy.pendingMayAbilities::add);
        copy.pendingGemstoneCavernsChoice = this.pendingGemstoneCavernsChoice;
        copy.tariffRemainingPlayers.addAll(this.tariffRemainingPlayers);
        copy.tariffPendingSacrificeIds.addAll(this.tariffPendingSacrificeIds);
        copy.forcedCostOrElseRemainingPlayers.addAll(this.forcedCostOrElseRemainingPlayers);
        copy.forcedCostOrElseSourceControllerId = this.forcedCostOrElseSourceControllerId;
        copy.anyPlayerMayPayManaRemainingPlayers.addAll(this.anyPlayerMayPayManaRemainingPlayers);
        copy.eachPlayerDamageUnlessPaysRemaining.addAll(this.eachPlayerDamageUnlessPaysRemaining);
        copy.eachPlayerDamageUnlessPaysDeclined.addAll(this.eachPlayerDamageUnlessPaysDeclined);
        copy.eachPlayerDamageUnlessPaysBatched = this.eachPlayerDamageUnlessPaysBatched;
        copy.whirlwindDenial.copyFrom(this.whirlwindDenial);
        copy.revealHandDiscardUnlessPaysRemaining.addAll(this.revealHandDiscardUnlessPaysRemaining);
        copy.pendingCardDraws.addAll(this.pendingCardDraws);
        copy.pendingReplacementCardDraws.addAll(this.pendingReplacementCardDraws);
        copy.drawsFromAppliedDoubling.putAll(this.drawsFromAppliedDoubling);
        copy.currentDrawDoublingApplied.addAll(this.currentDrawDoublingApplied);
        this.declinedDrawReplacementSources.forEach((playerId, sources) ->
                copy.declinedDrawReplacementSources.put(playerId, new java.util.HashSet<>(sources)));
        copy.pendingDrawFirstDrawStepFlags.putAll(this.pendingDrawFirstDrawStepFlags);
        copy.pendingDiscardToLibraryChoice = this.pendingDiscardToLibraryChoice == null
                ? null : this.pendingDiscardToLibraryChoice.deepCopy();
        copy.pendingDiscardToLibraryCardIndex = this.pendingDiscardToLibraryCardIndex;
        copy.pendingDiscardToLibraryDecision = this.pendingDiscardToLibraryDecision;
        copy.destroyDamagersUnlessPaysRemaining.addAll(this.destroyDamagersUnlessPaysRemaining);
        copy.bounceUnlessPaysRemaining.addAll(this.bounceUnlessPaysRemaining);

        // --- Unified delayed-action queue (immutable records, shallow copy — shared Card refs, as the
        //     per-mechanic fields it replaced were copied) ---

        // --- Exile-until-source-leaves map (O-ring style) ---
        this.exileReturnOnPermanentLeave.forEach((k, v) ->
                copy.exileReturnOnPermanentLeave.put(k, new ArrayList<>(v)));
        copy.exiledCardReturnToHandOnPermanentLeave.putAll(this.exiledCardReturnToHandOnPermanentLeave);
        this.exileReturnOnOpponentBecomesMonarch.forEach((k, v) ->
                copy.exileReturnOnOpponentBecomesMonarch.put(k, new ArrayList<>(v)));

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---

        // --- GraveyardTargetOperationState ---
        copy.graveyardTargetOperation.card = this.graveyardTargetOperation.card;
        copy.graveyardTargetOperation.controllerId = this.graveyardTargetOperation.controllerId;
        copy.graveyardTargetOperation.effects = this.graveyardTargetOperation.effects;
        copy.graveyardTargetOperation.entryType = this.graveyardTargetOperation.entryType;
        copy.graveyardTargetOperation.xValue = this.graveyardTargetOperation.xValue;
        copy.graveyardTargetOperation.anyNumber = this.graveyardTargetOperation.anyNumber;
        copy.graveyardTargetOperation.giftPromised = this.graveyardTargetOperation.giftPromised;
        copy.graveyardTargetOperation.singleGraveyard = this.graveyardTargetOperation.singleGraveyard;
        copy.graveyardTargetOperation.cumulativeUpkeepPayment = this.graveyardTargetOperation.cumulativeUpkeepPayment;
        copy.graveyardTargetOperation.controllerGraveyardPayment =
                this.graveyardTargetOperation.controllerGraveyardPayment;
        copy.graveyardTargetOperation.replacementGraveyardExile =
                this.graveyardTargetOperation.replacementGraveyardExile;
        copy.graveyardTargetOperation.targetPlayerId = this.graveyardTargetOperation.targetPlayerId;
        copy.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects =
                this.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects == null
                        ? List.of() : new ArrayList<>(this.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects);
        copy.graveyardTargetOperation.activeSpellGraveyardChoiceEffect =
                this.graveyardTargetOperation.activeSpellGraveyardChoiceEffect;
        copy.graveyardTargetOperation.spellGraveyardCardIdsByEffect.putAll(
                this.graveyardTargetOperation.spellGraveyardCardIdsByEffect);
        copy.graveyardTargetOperation.sourcePermanentId = this.graveyardTargetOperation.sourcePermanentId;
        copy.graveyardTargetOperation.triggeringPermanentPowerAtTrigger =
                this.graveyardTargetOperation.triggeringPermanentPowerAtTrigger;
        copy.graveyardTargetOperation.graveyardTargetMaxManaValue =
                this.graveyardTargetOperation.graveyardTargetMaxManaValue;
        copy.graveyardTargetOperation.triggeringPermanentId =
                this.graveyardTargetOperation.triggeringPermanentId;
        copy.graveyardTargetOperation.triggeringCardId = this.graveyardTargetOperation.triggeringCardId;
        copy.graveyardTargetOperation.triggeringCardGraveyardEntryVersion =
                this.graveyardTargetOperation.triggeringCardGraveyardEntryVersion;
        copy.graveyardTargetOperation.triggeringSpell = this.graveyardTargetOperation.triggeringSpell == null
                ? null : new StackEntry(this.graveyardTargetOperation.triggeringSpell);
        copy.graveyardTargetOperation.triggerRecord = this.graveyardTargetOperation.triggerRecord == null
                ? null : new StackEntry(this.graveyardTargetOperation.triggerRecord);
        copy.graveyardTargetOperation.spellCounterTargetId = this.graveyardTargetOperation.spellCounterTargetId;
        copy.graveyardTargetOperation.permanentTargetIds = this.graveyardTargetOperation.permanentTargetIds == null
                ? null : new ArrayList<>(this.graveyardTargetOperation.permanentTargetIds);
        copy.graveyardTargetOperation.kicked = this.graveyardTargetOperation.kicked;
        copy.graveyardTargetOperation.resolutionTimeCollectEvidenceResume =
                this.graveyardTargetOperation.resolutionTimeCollectEvidenceResume;
        copy.graveyardTargetOperation.resolutionTimeExileResume = this.graveyardTargetOperation.resolutionTimeExileResume;
        copy.graveyardTargetOperation.resolutionTimeExileOwnGraveyardResume = this.graveyardTargetOperation.resolutionTimeExileOwnGraveyardResume;
        copy.graveyardTargetOperation.resolutionTimeExileThenMayBecomeCopyResume =
                this.graveyardTargetOperation.resolutionTimeExileThenMayBecomeCopyResume;
        copy.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume =
                this.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume;
        copy.graveyardTargetOperation.resolutionTimeBargainedReturnChoiceResume =
                this.graveyardTargetOperation.resolutionTimeBargainedReturnChoiceResume;
        copy.graveyardTargetOperation.resolutionTimeBargainedReturnTargetCardIds =
                new ArrayList<>(this.graveyardTargetOperation.resolutionTimeBargainedReturnTargetCardIds);
        copy.graveyardTargetOperation.resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume =
                this.graveyardTargetOperation.resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume;
        copy.graveyardTargetOperation.eachPlayerExilesCardFromGraveyard =
                this.graveyardTargetOperation.eachPlayerExilesCardFromGraveyard;
        copy.graveyardTargetOperation.resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume =
                this.graveyardTargetOperation.resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume;
        copy.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeResume =
                this.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeResume;
        copy.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChosenCardId =
                this.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureResume =
                this.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureResume;
        copy.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId =
                this.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeExileThenEffectResume =
                this.graveyardTargetOperation.resolutionTimeExileThenEffectResume;
        copy.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId =
                this.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesResume =
                this.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesResume;
        copy.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesChosenCardIds =
                this.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesChosenCardIds == null
                        ? null : new ArrayList<>(this.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesChosenCardIds);
        copy.graveyardTargetOperation.resolutionTimeDragonApproachResume =
                this.graveyardTargetOperation.resolutionTimeDragonApproachResume;
        copy.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersResume =
                this.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersResume;
        copy.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId =
                this.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsResume =
                this.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsResume;
        copy.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsChoiceMade =
                this.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsChoiceMade;
        copy.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsCandidateIds =
                new ArrayList<>(this.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsCandidateIds);
        copy.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsChosenCardId =
                this.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsSourcePermanentId =
                this.graveyardTargetOperation.resolutionTimeExileOneOfDiscardedCardsSourcePermanentId;
        copy.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsResume =
                this.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsResume;
        copy.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChoiceMade =
                this.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChoiceMade;
        copy.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChosenCardId =
                this.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeForgottenLoreResume =
                this.graveyardTargetOperation.resolutionTimeForgottenLoreResume;
        copy.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume =
                this.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume;
        copy.graveyardTargetOperation.phyrexianGrimoireChosenCardId =
                this.graveyardTargetOperation.phyrexianGrimoireChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume =
                this.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume;
        copy.graveyardTargetOperation.opponentChoosesCardToHandChosenOpponentId =
                this.graveyardTargetOperation.opponentChoosesCardToHandChosenOpponentId;
        copy.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId =
                this.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId;
        copy.graveyardTargetOperation.resolutionTimeDawnbreakReclaimerOpponentCardChoiceResume =
                this.graveyardTargetOperation.resolutionTimeDawnbreakReclaimerOpponentCardChoiceResume;
        copy.graveyardTargetOperation.resolutionTimeDawnbreakReclaimerOwnCardChoiceResume =
                this.graveyardTargetOperation.resolutionTimeDawnbreakReclaimerOwnCardChoiceResume;
        copy.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentId =
                this.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentId;
        copy.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentCardId =
                this.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentCardId;
        copy.graveyardTargetOperation.dawnbreakReclaimerChosenOwnCardId =
                this.graveyardTargetOperation.dawnbreakReclaimerChosenOwnCardId;
        copy.graveyardTargetOperation.asEntersOpponentExileToGraveyard =
                this.graveyardTargetOperation.asEntersOpponentExileToGraveyard;
        copy.graveyardTargetOperation.milledCreatureExile =
                this.graveyardTargetOperation.milledCreatureExile;
        copy.graveyardTargetOperation.milledSagaAndLandReturn =
                this.graveyardTargetOperation.milledSagaAndLandReturn;

        copy.queenKaylaBinKroogOperation.active = this.queenKaylaBinKroogOperation.active;
        copy.queenKaylaBinKroogOperation.controllerId = this.queenKaylaBinKroogOperation.controllerId;
        copy.queenKaylaBinKroogOperation.discardedCardIds.addAll(
                this.queenKaylaBinKroogOperation.discardedCardIds);
        copy.queenKaylaBinKroogOperation.chosenCardIds.addAll(this.queenKaylaBinKroogOperation.chosenCardIds);
        copy.queenKaylaBinKroogOperation.nextManaValue = this.queenKaylaBinKroogOperation.nextManaValue;
        copy.queenKaylaBinKroogOperation.awaitingChoice = this.queenKaylaBinKroogOperation.awaitingChoice;
        copy.queenKaylaBinKroogOperation.choiceMade = this.queenKaylaBinKroogOperation.choiceMade;
        copy.queenKaylaBinKroogOperation.chosenCardId = this.queenKaylaBinKroogOperation.chosenCardId;

        // --- CloneOperationState ---
        copy.cloneOperation.card = this.cloneOperation.card;
        copy.pendingBattlefieldEntryBatch = this.pendingBattlefieldEntryBatch == null
                ? null : this.pendingBattlefieldEntryBatch.deepCopy();
        this.pendingBattlefieldEntryRequests.forEach((id, request) ->
                copy.pendingBattlefieldEntryRequests.put(id, request.deepCopy()));
        copy.pendingDepartureBatch = this.pendingDepartureBatch == null
                ? null : this.pendingDepartureBatch.deepCopy();
        this.answeredEventDepartureBatches.forEach(batch -> copy.answeredEventDepartureBatches.add(batch.deepCopy()));
        copy.pendingLossReplacement = this.pendingLossReplacement;
        this.pendingEntryCopyChoices.forEach(choice -> copy.pendingEntryCopyChoices.add(choice.deepCopy()));
        copy.pendingAnyNumberHandPutCardIds.addAll(this.pendingAnyNumberHandPutCardIds);
        copy.holdingHandBattlefieldPuts = this.holdingHandBattlefieldPuts;
        copy.heldHandBattlefieldPutsLeaveHand = this.heldHandBattlefieldPutsLeaveHand;
        copy.heldHandBattlefieldPuts.addAll(this.heldHandBattlefieldPuts);
        copy.cloneOperation.physicalCard = this.cloneOperation.physicalCard;
        copy.cloneOperation.preparedPermanent = this.cloneOperation.preparedPermanent == null
                ? null : new Permanent(this.cloneOperation.preparedPermanent);
        copy.cloneOperation.battlefieldEntryBatch = this.cloneOperation.battlefieldEntryBatch == null
                ? null : this.cloneOperation.battlefieldEntryBatch.deepCopy();
        copy.cloneOperation.entryRequest = this.cloneOperation.entryRequest == null
                ? null : this.cloneOperation.entryRequest.deepCopy();
        copy.cloneOperation.copyablePermanentIds = this.cloneOperation.copyablePermanentIds;
        copy.cloneOperation.transformed = this.cloneOperation.transformed;
        copy.cloneOperation.controllerId = this.cloneOperation.controllerId;
        copy.cloneOperation.etbTargetId = this.cloneOperation.etbTargetId;
        copy.cloneOperation.powerOverride = this.cloneOperation.powerOverride;
        copy.cloneOperation.toughnessOverride = this.cloneOperation.toughnessOverride;
        copy.cloneOperation.copyPowerToughnessFromSource = this.cloneOperation.copyPowerToughnessFromSource;
        copy.cloneOperation.additionalTypesOverride = this.cloneOperation.additionalTypesOverride;
        copy.cloneOperation.cardTypesOverride = this.cloneOperation.cardTypesOverride;
        copy.cloneOperation.additionalActivatedAbilities = this.cloneOperation.additionalActivatedAbilities;
        copy.cloneOperation.nameOverride = this.cloneOperation.nameOverride;
        copy.cloneOperation.additionalSupertypesOverride = this.cloneOperation.additionalSupertypesOverride;
        copy.cloneOperation.embalmColorOverride = this.cloneOperation.embalmColorOverride;
        copy.cloneOperation.embalmAddedSubtype = this.cloneOperation.embalmAddedSubtype;
        copy.cloneOperation.embalmRemoveManaCost = this.cloneOperation.embalmRemoveManaCost;
        copy.cloneOperation.additionalPlusOnePlusOneCounters = this.cloneOperation.additionalPlusOnePlusOneCounters;
        copy.cloneOperation.additionalKeywordsOverride = this.cloneOperation.additionalKeywordsOverride;
        copy.cloneOperation.additionalColorsOverride = this.cloneOperation.additionalColorsOverride;
        copy.cloneOperation.additionalCreatureOnlyCharacteristics = this.cloneOperation.additionalCreatureOnlyCharacteristics;
        copy.cloneOperation.additionalSubtypesOverride = this.cloneOperation.additionalSubtypesOverride;
        copy.cloneOperation.additionalAbilities = this.cloneOperation.additionalAbilities;
        copy.cloneOperation.reflexiveEffects = this.cloneOperation.reflexiveEffects;
        copy.cloneOperation.shieldCounterIfControllerControlsCopiedPermanent =
                this.cloneOperation.shieldCounterIfControllerControlsCopiedPermanent;
        copy.cloneOperation.xValue = this.cloneOperation.xValue;
        copy.cloneOperation.graveyardCopyChoicePending = this.cloneOperation.graveyardCopyChoicePending;
        copy.cloneOperation.graveyardCopyEffect = this.cloneOperation.graveyardCopyEffect;
        copy.cloneOperation.exileCopiedGraveyardCardAfterEntry =
                this.cloneOperation.exileCopiedGraveyardCardAfterEntry;
        copy.cloneOperation.graveyardCardToExileAfterEntry = this.cloneOperation.graveyardCardToExileAfterEntry;
        copy.cloneOperation.exileTwoAndAddOtherPowerCounters =
                this.cloneOperation.exileTwoAndAddOtherPowerCounters;
        copy.cloneOperation.selectedGraveyardCopyCardIds = this.cloneOperation.selectedGraveyardCopyCardIds;
        copy.cloneOperation.copyColor = this.cloneOperation.copyColor;
        copy.cloneOperation.copyUntilEndOfTurn = this.cloneOperation.copyUntilEndOfTurn;
        copy.cloneOperation.addVanishingIfCopiedPermanentLacksIt =
                this.cloneOperation.addVanishingIfCopiedPermanentLacksIt;
        copy.cloneOperation.removedSupertypesOverride = this.cloneOperation.removedSupertypesOverride;
        copy.cloneOperation.addTypeAppropriateCounters = this.cloneOperation.addTypeAppropriateCounters;
        copy.cloneOperation.entersTapped = this.cloneOperation.entersTapped;
        copy.cloneOperation.copyUntilEndOfTurn = this.cloneOperation.copyUntilEndOfTurn;
        copy.cloneOperation.ninjutsuEntry = this.cloneOperation.ninjutsuEntry;
        copy.cloneOperation.ninjutsuAttackTargetId = this.cloneOperation.ninjutsuAttackTargetId;
        copy.cloneOperation.landPlay = this.cloneOperation.landPlay;
        copy.cloneOperation.xValue = this.cloneOperation.xValue;
        copy.cloneOperation.copyCardFilter = this.cloneOperation.copyCardFilter;
        copy.cloneOperation.graveyardCopyChoicePending = this.cloneOperation.graveyardCopyChoicePending;
        copy.cloneOperation.graveyardCopyEffect = this.cloneOperation.graveyardCopyEffect;
        copy.cloneOperation.exileCopiedGraveyardCardAfterEntry =
                this.cloneOperation.exileCopiedGraveyardCardAfterEntry;
        copy.cloneOperation.graveyardCardToExileAfterEntry = this.cloneOperation.graveyardCardToExileAfterEntry;
        copy.cloneOperation.exileTwoAndAddOtherPowerCounters =
                this.cloneOperation.exileTwoAndAddOtherPowerCounters;
        copy.cloneOperation.selectedGraveyardCopyCardIds = this.cloneOperation.selectedGraveyardCopyCardIds;

        // --- WarpWorldOperationState ---
        copy.warpWorldOperation.pendingAuraChoices.addAll(this.warpWorldOperation.pendingAuraChoices);
        copy.warpWorldOperation.pendingEnchantmentPlacements.addAll(this.warpWorldOperation.pendingEnchantmentPlacements);
        copy.warpWorldOperation.enterTappedTypesSnapshot.addAll(this.warpWorldOperation.enterTappedTypesSnapshot);
        copy.warpWorldOperation.needsLegendChecks = this.warpWorldOperation.needsLegendChecks;
        copy.warpWorldOperation.sourceName = this.warpWorldOperation.sourceName;

        this.retetherOperation.pendingAuraChoices.stream().map(RetetherAuraChoiceRequest::deepCopy)
                .forEach(copy.retetherOperation.pendingAuraChoices::addLast);
        this.retetherOperation.pendingPlacements.stream().map(RetetherAuraPlacement::deepCopy)
                .forEach(copy.retetherOperation.pendingPlacements::add);
        copy.retetherOperation.activeChoice = this.retetherOperation.activeChoice == null ? null
                : this.retetherOperation.activeChoice.deepCopy();

        copy.auspiciousStarrixOperation.permanentCards.addAll(
                this.auspiciousStarrixOperation.permanentCards);
        copy.auspiciousStarrixOperation.pendingAuraChoices.addAll(
                this.auspiciousStarrixOperation.pendingAuraChoices);
        copy.auspiciousStarrixOperation.auraAttachmentTargets.putAll(
                this.auspiciousStarrixOperation.auraAttachmentTargets);
        copy.auspiciousStarrixOperation.enterTappedTypesSnapshot.addAll(
                this.auspiciousStarrixOperation.enterTappedTypesSnapshot);
        copy.auspiciousStarrixOperation.activeAuraChoice =
                this.auspiciousStarrixOperation.activeAuraChoice;
        copy.auspiciousStarrixOperation.controllerId = this.auspiciousStarrixOperation.controllerId;

        // --- Map<UUID, Map<Integer, Integer>> (activated ability uses) ---
        this.activatedAbilityUsesThisTurn.forEach((k, v) ->
                copy.activatedAbilityUsesThisTurn.put(k, new HashMap<>(v)));
        this.activatedAbilityUsesThisGame.forEach((k, v) ->
                copy.activatedAbilityUsesThisGame.put(k, new HashMap<>(v)));
        this.nonPrintedAbilityUseKeys.forEach((k, v) ->
                copy.nonPrintedAbilityUseKeys.put(k, new ConcurrentHashMap<>(v)));
        copy.permanentAbilityResolutionsThisTurn.putAll(this.permanentAbilityResolutionsThisTurn);

        // --- Deques ---
        IdentityHashMap<PendingInteraction, PendingInteraction> copiedInteractions = new IdentityHashMap<>();
        this.pendingInteractions.forEach(pending -> copy.pendingInteractions.add(
                copiedInteractions.computeIfAbsent(pending, original ->
                original instanceof PendingInteraction.LandManaDrainContinuation drain
                        ? new PendingInteraction.LandManaDrainContinuation(drain.entry(), drain.remainingLandIds(),
                                drain.delayedColorless())
                        : original instanceof PendingInteraction.ColorChoice choice
                        ? choice.copyCardTypeOnEnterPermanent()
                        : original instanceof PermanentChoiceContext.AttackCounterMoveFirstTarget trigger
                        ? trigger.withTriggerRecordCopy()
                        : original instanceof PermanentChoiceContext.PucasMischiefOwnTarget trigger
                        ? trigger.withTriggerRecordCopy()
                        : original instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger trigger
                        ? trigger.copyPlanarSnapshot()
                        : original instanceof PermanentChoiceContext.AttackTriggerTarget trigger
                        ? trigger.withTriggerRecordCopy()
                        : original instanceof PermanentChoiceContext.TriggeredModalTrigger trigger
                        ? trigger.withTriggerRecordCopy() : original)));
        this.queuedTriggerRecords.forEach((pending, record) -> {
            PendingInteraction copied = copiedInteractions.get(pending);
            if (copied != null) {
                copy.queuedTriggerRecords.put(copied, new StackEntry(record));
            }
        });
        copy.triggerRecordInHand = this.triggerRecordInHand == null ? null : new StackEntry(this.triggerRecordInHand);
        copy.extraTurns.addAll(this.extraTurns);
        copy.extraTurnSkipsUntap.addAll(this.extraTurnSkipsUntap);
        copy.extraTurnPowerUpAbilitiesDisabled.addAll(this.extraTurnPowerUpAbilitiesDisabled);
        copy.extraTurnDamageCantBePrevented.addAll(this.extraTurnDamageCantBePrevented);
        copy.extraTurnSequences.addAll(this.extraTurnSequences);
        copy.pendingAuctionNext = this.pendingAuctionNext;
        this.pendingLibraryBottomReorders.forEach(req ->
                copy.pendingLibraryBottomReorders.add(new LibraryBottomReorderRequest(req.playerId(), new ArrayList<>(req.cards()))));

        // --- Combat damage assignment state ---
        this.combatSharedDamagePreventionAllocations.forEach((sourceId, preventionSources) -> {
            Map<UUID, Map<UUID, Integer>> copiedSources = new HashMap<>();
            preventionSources.forEach((preventionId, amounts) -> copiedSources.put(preventionId,
                    new HashMap<>(amounts)));
            copy.combatSharedDamagePreventionAllocations.put(sourceId, copiedSources);
        });
        copy.combatDamageAssignmentModeChoices.putAll(this.combatDamageAssignmentModeChoices);
        this.combatDamagePlayerAssignments.forEach((k, v) ->
                copy.combatDamagePlayerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingIndices.addAll(this.combatDamagePendingIndices);
        this.combatDamageBlockerAssignments.forEach((k, v) ->
                copy.combatDamageBlockerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingBlockerIndices.addAll(this.combatDamagePendingBlockerIndices);
        copy.combatDamagePhase1State = this.combatDamagePhase1State; // read-only snapshot from phase 1

        // --- Emblems (records are immutable) ---
        copy.emblems.addAll(this.emblems);
        copy.ringStates.putAll(this.ringStates);
        copy.boons.addAll(this.boons);

        // --- Floating continuous effects (immutable records, safe to share) ---
        copy.floatingEffects.addAll(this.floatingEffects);
        copy.scheduledFloatingEffects.addAll(this.scheduledFloatingEffects);
        copy.floatingEffectCreatingActivePlayerIds.putAll(this.floatingEffectCreatingActivePlayerIds);
        this.phaseSequenceEndOfTurnShares.forEach((objectId, shares) ->
                copy.phaseSequenceEndOfTurnShares.put(objectId, new LinkedHashMap<>(shares)));
        copy.phaseSequenceAttributedEndOfTurnState.putAll(this.phaseSequenceAttributedEndOfTurnState);
        copy.perpetualCardPowerToughnessModifiers.putAll(this.perpetualCardPowerToughnessModifiers);
        copy.perpetualCardBasePowerToughness.putAll(this.perpetualCardBasePowerToughness);
        this.perpetualCardKeywords.forEach((cardId, keywords) ->
                copy.perpetualCardKeywords.put(cardId, keywords.isEmpty()
                        ? EnumSet.noneOf(Keyword.class) : EnumSet.copyOf(keywords)));
        this.perpetualCardSpellCastingAbilityGrants.forEach((cardId, grants) ->
                copy.perpetualCardSpellCastingAbilityGrants.put(cardId,
                        Collections.synchronizedList(new ArrayList<>(grants))));

        // --- Permanent no-max-hand-size grants ---

        // --- Permanent "can't gain life" grants (Stigma Lasher) ---
        copy.playersWithHandRevealed.addAll(this.playersWithHandRevealed);

        // --- Source-linked animations (Awakener Druid-style) ---
        copy.sourceLinkedAnimations.putAll(this.sourceLinkedAnimations);

        // --- Per-player spell/creature color protection (Autumn's Veil style) ---
        copy.spellsMadeUncounterable.addAll(this.spellsMadeUncounterable);
        this.spellTextReplacements.forEach((k, v) -> copy.spellTextReplacements.put(k, new ArrayList<>(v)));
        this.spellColorOverrides.forEach((k, v) -> copy.spellColorOverrides.put(k, new HashSet<>(v)));

        // --- Silence-style "opponents can't cast" flag ---
        copy.playersWhoTappedLandForManaThisTurn.addAll(this.playersWhoTappedLandForManaThisTurn);
        copy.counteredSpellManaCosts.putAll(this.counteredSpellManaCosts);

        copy.conspiredSpellCopyCounts.putAll(this.conspiredSpellCopyCounts);
        copy.casualtySpellCopyCounts.putAll(this.casualtySpellCopyCounts);

        // --- Pending one-shot spell copy triggers (Primal Wellspring) ---
        this.pendingCopyRiderManaChoices.forEach((sourceId, riders) ->
                copy.pendingCopyRiderManaChoices.put(sourceId, new ConcurrentHashMap<>(riders)));
        copy.spellsPaidUsingPendingAnyManaTypeThisTurn.addAll(this.spellsPaidUsingPendingAnyManaTypeThisTurn);

        this.exileSpellTypePermissionHistory.forEach((groupId, history) ->
                copy.exileSpellTypePermissionHistory.put(groupId, history.stream()
                        .map(types -> Set.copyOf(types)).collect(java.util.stream.Collectors.toCollection(ArrayList::new))));
        copy.exilePlayPermissionGroupUsesRemaining.putAll(this.exilePlayPermissionGroupUsesRemaining);
        copy.discordCopySourcePermanents.putAll(this.discordCopySourcePermanents);
        copy.stashCounterCardIds.addAll(this.stashCounterCardIds);
        copy.playersWhoForetoldThisTurn.addAll(this.playersWhoForetoldThisTurn);
        copy.graveyardLeaveNotificationDepth = this.graveyardLeaveNotificationDepth;
        copy.discardCostBatchOpen = this.discardCostBatchOpen;
        copy.permanentTapTriggerBatchDepth = this.permanentTapTriggerBatchDepth;
        this.permanentTapTriggerBatchFiredAbilities.forEach((sourceId, abilities) -> {
            Set<String> copiedAbilities = ConcurrentHashMap.newKeySet();
            copiedAbilities.addAll(abilities);
            copy.permanentTapTriggerBatchFiredAbilities.put(sourceId, copiedAbilities);
        });
        copy.playersWhoseCardsLeftGraveyardThisTurn.addAll(this.playersWhoseCardsLeftGraveyardThisTurn);
        copy.playersWhoseCreatureCardsLeftGraveyardThisTurn
                .addAll(this.playersWhoseCreatureCardsLeftGraveyardThisTurn);
        copy.cardsLeftGraveyardCountThisTurn.putAll(this.cardsLeftGraveyardCountThisTurn);
        copy.permanentLeaveNotificationDepth = this.permanentLeaveNotificationDepth;
        copy.deathReplacementChoiceBatchDepth = this.deathReplacementChoiceBatchDepth;
        copy.deferredDeathReplacementChoices.addAll(this.deferredDeathReplacementChoices);
        copy.deathReplacementsCanRerunEffect = this.deathReplacementsCanRerunEffect;
        copy.coinFlipChoicesCanRerunEffect = this.coinFlipChoicesCanRerunEffect;
        copy.recordedCoinFlips.addAll(this.recordedCoinFlips);
        copy.pokeyReplacementAnswer = this.pokeyReplacementAnswer;
        copy.replayedCoinFlips = this.replayedCoinFlips;
        copy.coinFlipCheckpointEffect = this.coinFlipCheckpointEffect;
        copy.coinFlipResumePoint = this.coinFlipResumePoint;
        copy.pendingCoinFlipChoiceReplay = this.pendingCoinFlipChoiceReplay;
        copy.stepDepartureEffect = this.stepDepartureEffect;
        copy.chosenDeathReplacementsForRerun.putAll(this.chosenDeathReplacementsForRerun);
        copy.rerunMayLibraryChoices.addAll(this.rerunMayLibraryChoices);
        copy.pendingDeathReplacementChoiceReplay = copyDeathReplacementChoiceReplay(
                this.pendingDeathReplacementChoiceReplay);

        // --- Search tax payments (Leonin Arbiter) ---
        this.paidSearchTaxPermanentIds.forEach((k, v) ->
                copy.paidSearchTaxPermanentIds.put(k, new HashSet<>(v)));

        // --- ETB / sacrifice damage assignments ---
        copy.pendingETBDamageAssignments = this.pendingETBDamageAssignments.isEmpty()
                ? Map.of() : new HashMap<>(this.pendingETBDamageAssignments);

        // --- Mindslaver turn control ---
        copy.wordOfCommandControllerPlayerId = this.wordOfCommandControllerPlayerId;
        copy.wordOfCommandControlledPlayerId = this.wordOfCommandControlledPlayerId;
        copy.wordOfCommandCardId = this.wordOfCommandCardId;
        copy.wordOfCommandPendingResolutionIndex = this.wordOfCommandPendingResolutionIndex;
        copy.wordOfCommandCastingCard = this.wordOfCommandCastingCard;
        copy.wordOfCommandAwaitingCardResolution = this.wordOfCommandAwaitingCardResolution;
        this.attackableCreaturesThisTurn.forEach((playerId, ids) -> copy.attackableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        this.creaturesAbleToAttackAtDeclareAttackersThisTurn.forEach((playerId, ids) ->
                copy.creaturesAbleToAttackAtDeclareAttackersThisTurn.put(playerId, Set.copyOf(ids)));
        this.blockableCreaturesThisTurn.forEach((playerId, ids) -> copy.blockableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        copy.peaceTalksTurnsRemaining = this.peaceTalksTurnsRemaining;
        copy.currentlyResolvingControllerId = this.currentlyResolvingControllerId;
        copy.currentlyResolvingTriggeredAbilityControllerId = this.currentlyResolvingTriggeredAbilityControllerId;

        // --- Opening hand reveal triggers (Chancellor cycle) ---
        copy.openingHandRevealTriggers.addAll(this.openingHandRevealTriggers);
        copy.openingHandManaTriggers.addAll(this.openingHandManaTriggers);
        copy.openingHandUpkeepTriggers.addAll(this.openingHandUpkeepTriggers);
        copy.playersWhoCastFirstSpellInGame.addAll(this.playersWhoCastFirstSpellInGame);
        copy.paradigmDelayedTriggers.addAll(this.paradigmDelayedTriggers);
        this.paradigmResolvedSpellNames.forEach((k, v) -> {
            Set<String> names = ConcurrentHashMap.newKeySet();
            names.addAll(v);
            copy.paradigmResolvedSpellNames.put(k, names);
        });
        copy.pendingFreeCastQueue.addAll(this.pendingFreeCastQueue);
        copy.pendingFreeCastAsCopyIds.addAll(this.pendingFreeCastAsCopyIds);
        copy.pendingFreeCastXValues.putAll(this.pendingFreeCastXValues);
        copy.pendingFreeCastLifeLosses.addAll(this.pendingFreeCastLifeLosses);
        copy.pendingFreeCastKickerOffers.putAll(this.pendingFreeCastKickerOffers);
        copy.freeCastKickerDecidedIds.addAll(this.freeCastKickerDecidedIds);
        copy.freeCastKickedIds.addAll(this.freeCastKickedIds);
        copy.freeCastEscalateModeCounts.putAll(this.freeCastEscalateModeCounts);
        copy.deferredExileBottomSourceIds.addAll(this.deferredExileBottomSourceIds);
        copy.pendingFreeCastLandPlayIds.addAll(this.pendingFreeCastLandPlayIds);
        copy.pendingFreeCastAdventureIds.addAll(this.pendingFreeCastAdventureIds);
        copy.pendingFreeCastManaValueBudget = this.pendingFreeCastManaValueBudget;
        copy.pendingExileFreeCastRemainderToGraveyard.addAll(this.pendingExileFreeCastRemainderToGraveyard);
        copy.pendingExileFreeCastRemainderToLibraryBottom.addAll(this.pendingExileFreeCastRemainderToLibraryBottom);
        copy.pendingExileFreeCastRemainderToHand.addAll(this.pendingExileFreeCastRemainderToHand);
        copy.pendingSpellweaverVoluteReattachment = this.pendingSpellweaverVoluteReattachment;

        // --- Turn-scoped counters ---
        // Read by ConditionEvaluationService / AmountEvaluationService / TurnProgressionService and
        // only reset at cleanup, so a copy taken mid-turn must carry them.
        copy.lifeLostThisTurn.putAll(this.lifeLostThisTurn);
        copy.lifeLostLastTurn.putAll(this.lifeLostLastTurn);
        copy.lastClashWonByController.putAll(this.lastClashWonByController);
        copy.playersWhoSearchedLibraryThisTurn.addAll(this.playersWhoSearchedLibraryThisTurn);
        copy.playersWhoInvestigatedThisTurn.addAll(this.playersWhoInvestigatedThisTurn);
        copy.manaAbilityResolutionDepth = this.manaAbilityResolutionDepth;
        copy.deferCreatureEntersBoons = this.deferCreatureEntersBoons;
        copy.activeTriggeredAbilityCopies = this.activeTriggeredAbilityCopies;
        this.permanentTypesCastFromGraveyardThisTurn.forEach((k, v) ->
                copy.permanentTypesCastFromGraveyardThisTurn.put(k, new HashSet<>(v)));
        copy.oncePerTurnGraveyardCastPermissionsUsedThisTurn.addAll(this.oncePerTurnGraveyardCastPermissionsUsedThisTurn);
        copy.oncePerTurnGraveyardLandPermissionsUsedThisTurn.addAll(this.oncePerTurnGraveyardLandPermissionsUsedThisTurn);
        copy.oncePerTurnGraveyardSpellPermissionsUsedThisTurn.addAll(this.oncePerTurnGraveyardSpellPermissionsUsedThisTurn);

        // --- Spell-cast payment tracking (X / converge / colors spent) ---
        copy.spellCastManaSpent.putAll(this.spellCastManaSpent);
        copy.spellCastCreatureManaSpent.putAll(this.spellCastCreatureManaSpent);
        copy.spellCastConvergeValue.putAll(this.spellCastConvergeValue);
        this.spellCastColorsSpent.forEach((k, v) ->
                copy.spellCastColorsSpent.put(k, java.util.EnumSet.copyOf(v)));
        this.spellCastManaSpentByColor.forEach((k, v) ->
                copy.spellCastManaSpentByColor.put(k, new java.util.EnumMap<>(v)));
        copy.spellCastSnowManaSpent.putAll(this.spellCastSnowManaSpent);
        this.spellCastSnowManaSpentByColor.forEach((k, v) ->
                copy.spellCastSnowManaSpentByColor.put(k, new java.util.EnumMap<>(v)));
        copy.spellCastTreasureManaSpent.putAll(this.spellCastTreasureManaSpent);
        copy.spellCastArtifactManaSpent.putAll(this.spellCastArtifactManaSpent);
        copy.spellCastCaveManaSpent.putAll(this.spellCastCaveManaSpent);
        copy.spellCastDesertManaSpent.putAll(this.spellCastDesertManaSpent);
        copy.spellCastUsedTreasureMana.putAll(this.spellCastUsedTreasureMana);
        this.spellCastManaSourceAmounts.forEach((k, v) ->
                copy.spellCastManaSourceAmounts.put(k, new ConcurrentHashMap<>(v)));
        this.spellCastManaSourceIds.forEach((k, v) -> {
            Set<UUID> sourceIds = ConcurrentHashMap.newKeySet();
            sourceIds.addAll(v);
            copy.spellCastManaSourceIds.put(k, sourceIds);
        });
        this.spellCastManaSpentOnX.forEach((k, v) ->
                copy.spellCastManaSpentOnX.put(k, new java.util.EnumMap<>(v)));
        copy.spellCastSplicedNames.putAll(this.spellCastSplicedNames);
        this.spellCastSplicedKeywords.forEach((k, v) ->
                copy.spellCastSplicedKeywords.put(k, Set.copyOf(v)));

        // --- Until-end-of-turn casting permissions ---
        copy.warpExileEntryVersions.putAll(this.warpExileEntryVersions);
        copy.spellAdditionalEnterCounters.putAll(this.spellAdditionalEnterCounters);
        copy.spellAdditionalEnterCounterAmounts.putAll(this.spellAdditionalEnterCounterAmounts);
        this.spellEntryCounters.forEach((cardId, counters) ->
                copy.spellEntryCounters.put(cardId, new ConcurrentHashMap<>(counters)));
        this.spellGrantedSubtypesOnEntry.forEach((cardId, subtypes) -> {
            Set<CardSubtype> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(subtypes);
            copy.spellGrantedSubtypesOnEntry.put(cardId, copied);
        });
        copy.spellsGrantedHasteOnEntry.addAll(this.spellsGrantedHasteOnEntry);
        copy.spellsGrantedPermanentHasteOnEntry.addAll(this.spellsGrantedPermanentHasteOnEntry);
        copy.spellsGrantedSuspendHasteOnEntry.addAll(this.spellsGrantedSuspendHasteOnEntry);
        copy.spellsGrantedRiotOnEntry.addAll(this.spellsGrantedRiotOnEntry);

        // --- Damage prevention / redirection still pending ---
        copy.pendingRedirectDamage.addAll(this.pendingRedirectDamage);
        copy.pendingSourceRedirectDamage.addAll(this.pendingSourceRedirectDamage);
        copy.suspendedReplacementEvent = this.suspendedReplacementEvent == null
                ? null : this.suspendedReplacementEvent.copyForSimulation();
        copy.suspendedDamageDepartureStep = this.suspendedDamageDepartureStep;
        copy.askedLifeLossDeparture = this.askedLifeLossDeparture;
        copy.gameOfChaosFlipControllerWins = this.gameOfChaosFlipControllerWins;
        copy.spellAwaitingEntryLifeLossDepartureId = this.spellAwaitingEntryLifeLossDepartureId;
        copy.entryAwaitingLifeLossDeparture = this.entryAwaitingLifeLossDeparture == null
                ? null : this.entryAwaitingLifeLossDeparture.deepCopy();
        copy.damageJournal = this.damageJournal == null ? null : this.damageJournal.copyForSimulation();
        this.pendingSimultaneousDamage.forEach(pending ->
                copy.pendingSimultaneousDamage.add(pending.copyForSimulation()));
        copy.collectingDamageEvent = this.collectingDamageEvent;
        copy.collectingDamageStep = this.collectingDamageStep;
        this.pendingReplacementEvents.forEach(event -> copy.pendingReplacementEvents.add(event.copyForSimulation()));

        // --- Pending discard / search follow-ups (immutable, so the reference is safe to share) ---
        copy.pendingReturnToHandOnDiscardType = this.pendingReturnToHandOnDiscardType;
        copy.pendingGainLifeOnDiscardType = this.pendingGainLifeOnDiscardType;
        copy.pendingTransformOnCreatureDiscard = this.pendingTransformOnCreatureDiscard;
        copy.pendingBoostSourceByDiscardedManaValue = this.pendingBoostSourceByDiscardedManaValue;
        copy.pendingConnive = this.pendingConnive;
        copy.pendingUntapOnDiscardType = this.pendingUntapOnDiscardType;
        copy.pendingSearchContext = this.pendingSearchContext;

        // --- Game-creation config ---
        copy.allRandom = this.allRandom;
        copy.planechase = this.planechase == null ? null : this.planechase.copy();
        copy.randomSetCode = this.randomSetCode;

        // --- Game log (share reference for simulation — not read during MCTS) ---
        copy.gameLog.addAll(this.gameLog);

        this.crimeCandidatesThisTurn.forEach((key, entries) ->
                copy.crimeCandidatesThisTurn.put(key,
                        Collections.synchronizedList(new ArrayList<>(entries))));
        copy.spellsCastFromHandThisTurn.addAll(this.spellsCastFromHandThisTurn);
        copy.spellsCastFromOutsideHandThisTurn.putAll(this.spellsCastFromOutsideHandThisTurn);
        copy.spellsWithPlotOnResolution.addAll(this.spellsWithPlotOnResolution);
        copy.plottedCardIds.addAll(this.plottedCardIds);
        this.creaturesThatSaddledPermanentThisTurn.forEach((key, ids) ->
                copy.creaturesThatSaddledPermanentThisTurn.put(key, new HashSet<>(ids)));
        this.creaturesThatCrewedPermanentThisTurn.forEach((key, ids) ->
                copy.creaturesThatCrewedPermanentThisTurn.put(key, new HashSet<>(ids)));
        this.crewedPermanentSubtypesThisTurn.forEach((key, subtypes) ->
                copy.crewedPermanentSubtypesThisTurn.put(key, new HashSet<>(subtypes)));
        copy.targetOpponentsDiscardThenDraw.active = this.targetOpponentsDiscardThenDraw.active;
        copy.targetOpponentsDiscardThenDraw.completed = this.targetOpponentsDiscardThenDraw.completed;
        copy.targetOpponentsDiscardThenDraw.controllerId = this.targetOpponentsDiscardThenDraw.controllerId;
        copy.targetOpponentsDiscardThenDraw.remainingTargetIds.addAll(
                this.targetOpponentsDiscardThenDraw.remainingTargetIds);
        copy.targetOpponentsDiscardThenDraw.noDiscardPlayers.addAll(
                this.targetOpponentsDiscardThenDraw.noDiscardPlayers);
        copy.targetOpponentsDiscardThenDraw.selectedDiscards.addAll(
                this.targetOpponentsDiscardThenDraw.selectedDiscards);
        copy.eachOpponentDrawsThenControllerDraws.active = this.eachOpponentDrawsThenControllerDraws.active;
        copy.eachOpponentDrawsThenControllerDraws.controllerId = this.eachOpponentDrawsThenControllerDraws.controllerId;
        copy.eachOpponentDrawsThenControllerDraws.remainingOpponentIds.addAll(
                this.eachOpponentDrawsThenControllerDraws.remainingOpponentIds);
        copy.eachOpponentDrawsThenControllerDraws.pendingOpponentId =
                this.eachOpponentDrawsThenControllerDraws.pendingOpponentId;
        copy.eachOpponentDrawsThenControllerDraws.pendingDrawCount =
                this.eachOpponentDrawsThenControllerDraws.pendingDrawCount;
        copy.eachOpponentDrawsThenControllerDraws.successfulDrawCount =
                this.eachOpponentDrawsThenControllerDraws.successfulDrawCount;
        copy.additionalBeginningPhasesAfterCombat = this.additionalBeginningPhasesAfterCombat;
        copy.additionalBeginningPhaseReturnStep = this.additionalBeginningPhaseReturnStep;
        copy.additionalBeginningPhaseUntapInProgress = this.additionalBeginningPhaseUntapInProgress;
        copy.additionalUpkeepStepsAfterCombat = this.additionalUpkeepStepsAfterCombat;
        copy.additionalCombatCreatedAfterUpkeepSteps = this.additionalCombatCreatedAfterUpkeepSteps;
        copy.additionalUpkeepReturnStep = this.additionalUpkeepReturnStep;

        Map<UUID, Permanent> copiedCombatPermanents = new HashMap<>();
        copy.playerBattlefields.values().forEach(permanents -> permanents.forEach(
                permanent -> copiedCombatPermanents.put(permanent.getId(), permanent)));
        copy.phasedOutPermanents.values().forEach(permanents -> permanents.forEach(
                permanent -> copiedCombatPermanents.put(permanent.getId(), permanent)));
        java.util.function.Function<Permanent, Permanent> copyCombatPermanent = permanent ->
                permanent == null ? null : copiedCombatPermanents.computeIfAbsent(
                        permanent.getId(), ignored -> new Permanent(permanent));
        copy.pendingCombatDamageState = copyCombatDamageState(this.pendingCombatDamageState, copyCombatPermanent);
        copy.rulesEvents.resume();
        copy.rulesEvents.copyFrom(this.rulesEvents);
        return copy;
    }

    /**
     * Copies the fields from a deep-copied InteractionState into this GameData's interaction.
     * Since GameData.interaction is final, we need to copy field-by-field.
     */
    private static void copyInteractionInto(GameData target, InteractionState source) {
        // The interaction field is final on GameData, so we replicate its state
        // through its public methods.

        // The permanent-choice pre-seed carrier is copied unconditionally: it can be set
        // outside any awaiting window (e.g. a clone-copy context pre-seeded across the
        // MAY_ABILITY_CHOICE window).
        target.interaction.setPermanentChoiceContext(source.permanentChoiceContext());

        // The active interaction record carries everything (immutable, shallow copy)
        if (source.activeInteraction() != null) {
            target.interaction.beginInteraction(
                    source.activeInteraction(), source.activeDecisionId());
            target.interaction.keepTriggerRecord(source.activeTriggerRecord());
        }
    }

    /** Records one damage bonus grant under {@code key} in a grant map such as {@link #perpetualNoncombatDamageBonuses}. */
    public static void addDamageBonusGrant(Map<UUID, List<DamageBonusGrant>> grants, UUID key, DamageBonusGrant grant) {
        grants.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(grant);
    }

    private static void copyDamageBonusGrants(Map<UUID, List<DamageBonusGrant>> from,
                                              Map<UUID, List<DamageBonusGrant>> to) {
        from.forEach((key, grants) -> to.put(key, new CopyOnWriteArrayList<>(grants)));
    }

    /** Perpetually grants the card {@code cardId} {@code ability}; a repeated grant stacks (CR 113.2c). */
    public void grantPerpetualAbility(UUID cardId, Ability ability) {
        perpetualTriggeredAbilityGrants.merge(cardId, List.of(ability), GameData::concatAbilities);
    }

    /** Like {@link #grantPerpetualAbility(UUID, Ability)}, but does nothing when the card already has an equal grant. */
    public void grantPerpetualAbilityOnce(UUID cardId, Ability ability) {
        perpetualTriggeredAbilityGrants.merge(cardId, List.of(ability),
                (existing, added) -> existing.contains(ability) ? existing : concatAbilities(existing, added));
    }

    /**
     * Removes the perpetual grants of the card {@code cardId} that are triggered abilities with {@code trigger} and
     * have an effect that matches {@code filter}.
     */
    public void removePerpetualAbilitiesIf(UUID cardId, Trigger trigger, Predicate<CardEffect> filter) {
        perpetualTriggeredAbilityGrants.computeIfPresent(cardId, (ignored, existing) -> {
            List<Ability> kept = existing.stream()
                    .filter(ability -> !(ability instanceof TriggeredAbility triggered && triggered.hasTrigger(trigger))
                            || AbilityEffects.effectsOf(ability).stream().noneMatch(filter))
                    .toList();
            return kept.isEmpty() ? null : kept;
        });
    }

    /** The perpetual grants of the card {@code cardId}, in grant order. */
    public List<Ability> perpetualAbilities(UUID cardId) {
        return perpetualTriggeredAbilityGrants.getOrDefault(cardId, List.of());
    }

    /**
     * The effects of the triggered abilities perpetually granted to the card {@code cardId} that have {@code trigger}
     * as a trigger condition functioning in {@code zone}, in grant order.
     */
    public List<CardEffect> perpetualTriggerEffects(UUID cardId, Trigger trigger, Zone zone) {
        List<CardEffect> effects = new ArrayList<>();
        for (Ability ability : perpetualAbilities(cardId)) {
            if (ability instanceof TriggeredAbility triggered && triggered.hasTrigger(trigger, zone)) {
                triggered.effects().forEach(registration -> effects.add(registration.effect()));
            }
        }
        return effects;
    }

    /** The effects of the static abilities perpetually granted to the card {@code cardId}, in grant order. */
    public List<CardEffect> perpetualStaticEffects(UUID cardId) {
        return AbilityEffects.staticEffectsIn(perpetualAbilities(cardId));
    }

    private static List<Ability> concatAbilities(List<Ability> existing, List<Ability> added) {
        List<Ability> all = new ArrayList<>(existing.size() + added.size());
        all.addAll(existing);
        all.addAll(added);
        return List.copyOf(all);
    }

}
