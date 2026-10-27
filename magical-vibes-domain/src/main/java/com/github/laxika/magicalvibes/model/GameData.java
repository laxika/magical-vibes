package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.action.CyclopeanTombUpkeepCleanup;
import com.github.laxika.magicalvibes.model.action.DelayedAction;
import com.github.laxika.magicalvibes.model.action.DestroyChosenCreaturesIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedStillExiledCardsEndStepTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWithChosenPlanarModePlayAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ForageOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.OtherAttackingCreatureReferenceEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    /** Amount of Path of Ancestry mana spent to cast each spell. */
    public final Map<UUID, Integer> spellCastPathOfAncestryManaSpent = new ConcurrentHashMap<>();
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
    /** Raging River: each attacking permanent maps to the independent nonflying pile restrictions
     * chosen for it during the current combat. */
    public final Map<UUID, List<Set<UUID>>> ragingRiverBlockRestrictionsThisCombat = new ConcurrentHashMap<>();
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
    /** Permanents receiving +1/+1 counters, grouped by the player who put those counters. */
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
    /** Perpetual base-power/base-toughness setters keyed by physical card identity. */
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
    /** Perpetual triggered-ability grants reapplied when the affected card enters the battlefield. */
    public final Map<UUID, Map<EffectSlot, List<CardEffect>>> perpetualTriggeredAbilityGrants =
            new ConcurrentHashMap<>();
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
    /** Directly phased-out permanent ids that remain phased out until their source leaves. */
    public final Map<UUID, Set<UUID>> phasedOutUntilSourceLeaves = new ConcurrentHashMap<>();
    /** Ids from {@link #phasedOutUntilSourceLeaves} that become tapped when they phase back in (Oubliette). */
    public final Set<UUID> tappedWhenSourceLeavesPhasesIn = ConcurrentHashMap.newKeySet();
    /** Directly phased-out permanent ids that remain phased out until a player planeswalks. */
    public final Set<UUID> phasedOutUntilPlaneswalk = ConcurrentHashMap.newKeySet();
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
    /** Persistent per-player maximum life totals, when an effect imposes one. */
    public final Map<UUID, Integer> playerMaximumLifeTotals = new ConcurrentHashMap<>();
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
    public final InteractionState interaction = new InteractionState();
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
     * graveyard simultaneously (CR 601.2h), so "one or more" graveyard triggers wait for the last card.
     */
    public boolean deferOncePerBatchGraveyardTriggers;
    /** Spellbook cards drafted but not yet put onto the battlefield (The Hourglass Coven). */
    public final List<Card> heldSpellbookCards = Collections.synchronizedList(new ArrayList<>());
    /** The first permanent card (and first land card) discarded while {@link #deferOncePerBatchGraveyardTriggers} was set. */
    public Card deferredBatchPermanentCard;
    public Card deferredBatchLandCard;
    public UUID deferredBatchOwnerId;
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
            return super.put(id, new CommanderGraveyardList(cards));
        }
        @Override public List<Card> putIfAbsent(UUID id, List<Card> cards) {
            return super.putIfAbsent(id, new CommanderGraveyardList(cards));
        }
        @Override public List<Card> computeIfAbsent(UUID id, Function<? super UUID, ? extends List<Card>> factory) {
            return super.computeIfAbsent(id, key -> new CommanderGraveyardList(factory.apply(key)));
        }
        @Override public void putAll(Map<? extends UUID, ? extends List<Card>> values) { values.forEach(this::put); }
    };
    /** Latest graveyard-entry identity for each card, used by effects that require continuous graveyard presence. */
    public final Map<UUID, Long> graveyardEntryVersions = new ConcurrentHashMap<>();
    /**
     * Player who activated each source's "for as long as this remains tapped" untap lock, keyed by the
     * source permanent (Giant Oyster). Its delayed triggers belong to that player (CR 603.7d), so
     * "each of your draw steps" means theirs even after the source changes control.
     */
    public final Map<UUID, UUID> untapLockActivatorBySource = new ConcurrentHashMap<>();
    public final Map<UUID, Long> exileEntryVersions = new ConcurrentHashMap<>();
    /** Number of times each card has entered a hand through {@link #addCardToHand}. A card that returns to hand is a new object (CR 400.7), so a delayed effect that refers to an earlier hand entry checks this version. */
    public final Map<UUID, Long> handEntryVersions = new ConcurrentHashMap<>();
    /**
     * Exile-entry version of the card at the moment a {@link PendingExileReturn} was queued, keyed by the
     * queued instance. A delayed return applies only to the exile object it was scheduled for (CR 400.7),
     * so a later re-exile of the same card id must not satisfy it.
     */
    public final Map<PendingExileReturn, Long> pendingExileReturnExileVersions =
            Collections.synchronizedMap(new IdentityHashMap<>());
    /**
     * Earliest turn number on which a queued {@link PendingExileReturn} may fire, keyed by the queued
     * instance. Used by returns worded "on your next turn", which must not fire during an extra
     * step of the turn on which they were created.
     */
    public final Map<PendingExileReturn, Integer> pendingExileReturnEarliestTurns =
            Collections.synchronizedMap(new IdentityHashMap<>());
    /** Affected permanent mapped to the player whose next turn ends the attack restriction. */
    public final Map<UUID, Set<UUID>> permanentsCantAttackUntilNextTurn = new ConcurrentHashMap<>();
    private long graveyardEntryVersion;
    /** Cards in each player's command zone (CR 903.6). Used by Eminence and similar command-zone abilities. */
    public final Map<UUID, List<Card>> playerCommandZones = new ConcurrentHashMap<>();
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
     *  {@code TriggerCollectionService.checkDiscardTriggers}; cycling is a discard). Used by Shadow of the Grave. */
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
     * Source permanent ids whose resolved ability gave them "creatures dealt damage by this creature this turn
     * can't be regenerated this turn" (Runesword, Bone Shaman). The effect outlives the source leaving the battlefield.
     */
    public final Set<UUID> damagedCreaturesCantRegenerateSourceIdsThisTurn = ConcurrentHashMap.newKeySet();
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
    /** Delayed trigger: creature card ID → poison counters to give its controller when it dies this turn. */
    public final Map<UUID, Integer> creatureGivingControllerPoisonOnDeathThisTurn = new ConcurrentHashMap<>();
    /** Delayed triggers: creature card ID → return details if it dies this turn (Graceful Reprieve, Supernatural Stamina, Adarkar Valkyrie). */
    public final Map<UUID, List<DelayedReturnOnDeath>> creaturesReturnedToBattlefieldOnDeathThisTurn = new ConcurrentHashMap<>();
    /** Permanents each exploit source sacrificed as its exploit ability resolved, keyed by the source permanent id. */
    public final Map<UUID, Set<UUID>> exploitedPermanentIdsBySource = new ConcurrentHashMap<>();
    /** Delayed trigger: creature card ID → effect registrations to resolve if it dies this turn (Skeletonize, Initiate of Blood). */
    public final Map<UUID, List<DelayedEffectOnDeath>> creatureTriggeringEffectOnDeathThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<DelayedEffectOnDeath>> permanentTriggeringEffectOnDeathThisTurn = new ConcurrentHashMap<>();
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
    public final List<ExiledCardEntry> exiledCards = Collections.synchronizedList(new ArrayList<>());
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
    /** Maps creature cards exiled by Lukka's first ability to the player who may cast them. */
    public final Map<UUID, UUID> lukkaExileCastPermissions = new ConcurrentHashMap<>();
    /** Spells exiled with delay counters and waiting to go back onto the stack (Ertai's Meddling). */
    public final List<DelayedSpellExile> delayedSpellExiles = Collections.synchronizedList(new ArrayList<>());
    /** Spells exiled with suspend counters and waiting for their owners' upkeeps. */
    public final List<SuspendedSpellExile> suspendedSpellExiles = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    /** Player IDs → remaining combat-damage-only prevention shields for this turn. */
    public final Map<UUID, Integer> playerCombatDamagePreventionShields = new ConcurrentHashMap<>();
    /** Player IDs → number of upcoming combat phases they must skip (Blinding Angel). Decremented as each is skipped. */
    public final Map<UUID, Integer> skipNextCombatPhaseCount = new ConcurrentHashMap<>();
    /** Subset of queued combat skips that expires during this turn's cleanup. */
    public final Map<UUID, Integer> skipCombatPhaseExpirationsThisTurn = new ConcurrentHashMap<>();
    /** Players whose next turn has no combat phases, including additional combats. */
    public final Set<UUID> skipCombatPhasesNextTurn = ConcurrentHashMap.newKeySet();
    /** Rules restricting blockers of matching creatures controlled by the keyed player this turn. */
    public final Map<UUID, List<GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect>>
            matchingCreatureBlockRestrictionsThisTurn = new ConcurrentHashMap<>();
    /**
     * "All creatures with [counter] counters on them block that creature this turn if able": the counter
     * types per attacking creature. The requirement is not a characteristic change, so it applies to every
     * creature that has such a counter whenever blockers are declared (CR 611.2c), not just to those that had
     * one when the ability resolved.
     */
    public final Map<UUID, Set<CounterType>> counterHolderMustBlockRequirementsThisTurn = new ConcurrentHashMap<>();
    /** Restrictions evaluated against the player's permanents during upcoming untap steps. */
    public final Map<UUID, List<SkipNextUntapEffect>>
            matchingPermanentUntapRestrictions = new ConcurrentHashMap<>();
    /** Player IDs → number of upcoming draw steps they must skip (Ivory Gargoyle). Decremented as each is skipped. */
    public final Map<UUID, Integer> skipNextDrawStepCount = new ConcurrentHashMap<>();
    /** Player IDs → turn number whose draw step is skipped by a current-turn effect. */
    public final Map<UUID, Integer> skipDrawStepThisTurn = new ConcurrentHashMap<>();
    /** Player IDs → number of upcoming turns they must skip (Chronatog). Decremented as each is skipped. */
    public final Map<UUID, Integer> skipNextTurnCount = new ConcurrentHashMap<>();
    /** Player IDs → steps or phases skipped for the rest of their current turn. */
    public final Map<UUID, Set<SkipStepOrPhaseKind>> skippedStepOrPhasesThisTurn = new ConcurrentHashMap<>();
    /**
     * Player IDs → number of upcoming untap steps they must skip (Yosei, the Morning Star).
     * Decremented as each is skipped. CR 500.11 / 614.10: the whole step is proceeded past, so no
     * phasing happens either (CR 702.26m) and no untap-restriction choice is offered.
     */
    public final Map<UUID, Integer> skipNextUntapStepCount = new ConcurrentHashMap<>();
    public int globalDamagePreventionShield;
    public boolean preventAllCombatDamage;
    /** When true, all combat damage that would be dealt by attacking creatures is prevented this turn (Harmless Assault). */
    public boolean preventAllCombatDamageByAttackingCreatures;
    /** When true, all combat damage that would be dealt to players is prevented this turn (Defend the Hearth). */
    public boolean preventAllCombatDamageToPlayers;
    /** When true, all damage to all creatures (both players') is prevented this turn (Blinding Fog). */
    /** Ability instances already collected during the current simultaneous creature-entry event. */
    public final Map<UUID, Set<com.github.laxika.magicalvibes.model.effect.CardEffect>> simultaneousCreatureEntryTriggers = new HashMap<>();
    public boolean collectingSimultaneousCreatureEntryTriggers;
    /** Permanents put onto the battlefield together whose enter abilities are still being processed. An
     *  as-enters choice of one of them (Rescuer Sphinx) can't pick another, as they weren't there yet. */
    public final Set<UUID> permanentsEnteringSimultaneously = ConcurrentHashMap.newKeySet();

    public boolean preventAllDamageToAllCreatures;
    /** When true, all damage that would be dealt by creatures is prevented this turn (Ethereal Haze). */
    public boolean preventAllDamageByCreatures;
    /** Controllers who gain life for damage prevented by their global creature-source shields this turn. */
    public final List<UUID> damageByCreaturesPreventionLifeGainPlayers =
            Collections.synchronizedList(new ArrayList<>());
    /** When true, all damage that would be dealt by non-Human sources is prevented this turn (Repel the Abominable). */
    public boolean preventAllDamageFromNonHumanSources;
    /** When non-null, creatures NOT matching this predicate are prevented from dealing combat damage this turn. */
    public PermanentPredicate combatDamageExemptPredicate;
    public UUID combatDamageExemptControllerId;
    /** Independent turn-long source exemptions, grouped by the controller evaluating each filter. */
    public final Map<UUID, Set<PermanentPredicate>> combatDamageExemptPredicatesByController = new ConcurrentHashMap<>();
    /** Opponents chosen for a simultaneous attacking-token-copy event, keyed by its choice batch. */
    public final Map<UUID, List<UUID>> pendingAttackingCopyOpponents = new ConcurrentHashMap<>();
    /** Remaining independent opponent choices for each attacking-token-copy event. */
    public final Map<UUID, Integer> pendingAttackingCopyChoices = new ConcurrentHashMap<>();
    public boolean allPermanentsEnterTappedThisTurn;
    /** Per-player filters for permanents that enter tapped under that player's control this turn. */
    public final Map<UUID, Set<PermanentPredicate>> permanentEnterTappedFiltersThisTurn = new ConcurrentHashMap<>();
    /**
     * Per-player count of additional +1/+1 counters that creatures entering under that player's
     * control receive for the rest of this turn (Zameck Guildmage). Turn-long replacement effect
     * (CR 614.1c) that survives the source leaving the battlefield.
     */
    public final Map<UUID, Integer> additionalEnterCountersThisTurn = new ConcurrentHashMap<>();
    /** Per-player count of additional +1/+1 counters that creatures entering under that player's
     *  control receive until the beginning of that player's next turn. */
    public final Map<UUID, Integer> additionalEnterCountersUntilNextTurn = new ConcurrentHashMap<>();
    /** Additional +1/+1 counters waiting for the next qualifying enchantment creature entry event this turn. */
    public final Map<UUID, Integer> pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn =
            new ConcurrentHashMap<>();
    /** Counts already applied to members of the current simultaneous enchantment creature entry event. */
    public final Map<UUID, Integer> activeAdditionalCountersForEnchantmentCreatureEntryBatch =
            new ConcurrentHashMap<>();
    /** Per-controller damage bonuses this turn for sources of one color (The Flame of Keld Chapter III), one per grant. */
    public final Map<UUID, List<DamageBonusGrant>> colorSourceDamageBonusThisTurn = new ConcurrentHashMap<>();
    /** Per-player damage bonuses this turn for all sources they control, one per grant. */
    public final Map<UUID, List<DamageBonusGrant>> controllerDamageBonusThisTurn = new ConcurrentHashMap<>();
    /** Per-player noncombat damage bonuses this turn for sources they control (Taii Wakeen), one per grant. */
    public final Map<UUID, List<DamageBonusGrant>> controllerNoncombatDamageBonusThisTurn = new ConcurrentHashMap<>();
    public final Set<CardColor> preventDamageFromColors = ConcurrentHashMap.newKeySet();
    public UUID combatDamageRedirectTarget;
    public UUID combatDamageRedirectPlayer;
    public final Map<UUID, Map<CardColor, Integer>> playerColorDamagePreventionCount = new ConcurrentHashMap<>();
    /** Target IDs to colors whose damage is prevented to that target until end of turn. */
    public final Map<UUID, Set<CardColor>> colorDamagePreventionUntilEndOfTurn = new ConcurrentHashMap<>();
    /**
     * Controller IDs to colors whose damage is prevented to every creature that controller controls,
     * including creatures that enter later this turn (one-shot prevention is not a continuous effect).
     */
    public final Map<UUID, Set<CardColor>> colorDamagePreventionForControlledCreaturesUntilEndOfTurn =
            new ConcurrentHashMap<>();
    /** Target IDs to which damage from colorless sources is prevented until end of turn. */
    public final Set<UUID> colorlessDamagePreventionUntilEndOfTurn = ConcurrentHashMap.newKeySet();
    public final List<PendingMayAbility> pendingMayAbilities = new ArrayList<>();
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
    public final List<BattlefieldEntryCard> heldHandBattlefieldPuts = new ArrayList<>();
    public StackEntry pendingEffectResolutionEntry;
    public int pendingEffectResolutionIndex;
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
    /** CR 603.5 — stores the StackEntry for resolution-time target selection so the target can be set on it. */
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
    /**
     * Unified queue of scheduled {@link DelayedAction}s ("do X later at timing point Y"). Replaces the
     * former per-mechanic ad-hoc fields (end-of-combat sacrifice/exile/equipment-destruction, end-step
     * token-exile/sacrifice/destroy/counter/untap/graveyard-returns, exile-until-step returns,
     * delayed combat-damage loot). Every producer appends via {@link #queueDelayedAction}; every drain
     * site takes all entries of its own kind in insertion order via {@link #drainDelayedActions}, so
     * the cross-family servicing order is fixed by the drain call-site chains, not the field layout.
     * Accessed under {@code synchronized (gameData)} in the engine, like the fields it replaced.
     */
    public final List<DelayedAction> delayedActions = Collections.synchronizedList(new ArrayList<>());

    public PendingAbilityActivation pendingAbilityActivation;
    /** A graveyard-activated ability suspended on its "Discard a card" cost choice (Eternalize). */
    public PendingGraveyardAbilityActivation pendingGraveyardAbilityActivation;
    public final Map<UUID, UUID> drawReplacementTargetToController = new ConcurrentHashMap<>();
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
    /** Aladdin's Lamp — one queued, turn-scoped delayed replacement per activation. Each entry is
     *  the activation's X value and is consumed by {@code DrawService.resolveDrawCard}. */
    public final Map<UUID, List<Integer>> pendingNextDrawLookAtTop = new ConcurrentHashMap<>();
    /** Words of Worship — one queued replacement per activation of "the next time you would draw a
     *  card this turn, you gain 5 life instead". Consumed by {@code DrawService.resolveDrawCard} and
     *  cleared at end-of-turn cleanup. */
    public final Map<UUID, Integer> pendingNextDrawGainLife = new ConcurrentHashMap<>();
    /** Words of Wilding - one queued replacement per activation of "the next time you would draw a
     *  card this turn, create a 2/2 green Bear creature token instead". Each value is the source set
     *  code used for the created token's art. */
    public final Map<UUID, List<String>> pendingNextDrawCreateBears = new ConcurrentHashMap<>();
    /** Words of War — one queued replacement per activation of "the next time you would draw a
     *  card this turn, this enchantment deals 2 damage to the chosen target instead". Each queue
     *  entry retains the activation's target and source card. */
    public final Map<UUID, List<PendingNextDrawDamageReplacement>> pendingNextDrawDamage = new ConcurrentHashMap<>();
    /** Words of Wind — one queued replacement per activation of the controller's next draw this
     *  turn. Each replacement is consumed by {@code DrawService.resolveDrawCard} and cleared at
     *  end-of-turn cleanup. */
    public final Map<UUID, Integer> pendingNextDrawReturnPermanents = new ConcurrentHashMap<>();
    /** Words of Waste — one queued replacement per activation of the controller's next draw this
     *  turn. Each replacement is consumed by {@code DrawService.resolveDrawCard} and cleared at
     *  end-of-turn cleanup. */
    public final Map<UUID, Integer> pendingNextDrawDiscardOpponents = new ConcurrentHashMap<>();
    /** Mangara's Tome — one-shot, turn-scoped delayed replacements of a player's next draws this
     *  turn: "instead put the top card of the exiled pile into its owner's hand." Keyed by drawing
     *  player id, value = a queue of source permanent ids (one entry per activation, since each
     *  replacement applies to one draw). Consumed in {@code DrawService.resolveDrawCard} and
     *  cleared at end-of-turn cleanup. */
    public final Map<UUID, List<UUID>> pendingNextDrawFromExiledPile = new ConcurrentHashMap<>();
    /** Ring of Ma'rûf — one queued, turn-scoped replacement per activation of the controller's next draw. */
    public final Map<UUID, Integer> pendingNextDrawFromOutsideGame = new ConcurrentHashMap<>();
    /** Player IDs → number of pending Urabrask-style next-draw replacements this turn. */
    public final Map<UUID, Integer> pendingNextDrawExileTopCard = new ConcurrentHashMap<>();
    /** Plots That Span Centuries: one persistent replacement per registration for the controller's next scheme set. */
    public final Map<UUID, Integer> pendingNextSchemeSetInMotionReplacements = new ConcurrentHashMap<>();
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
    /** Permanent id to the player who can't sacrifice it this turn (Call for Aid). */
    public final Map<UUID, UUID> sacrificeForbiddenThisTurn = new ConcurrentHashMap<>();
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
    /** Whether only land creatures may attack during the current additional combat phase. */
    public boolean onlyLandCreaturesCanAttackThisCombat;
    /** Whether only creatures with Aggressive may attack during the current additional combat. */
    public boolean onlyAggressiveCreaturesCanAttackThisCombat;
    /** Whether creatures cannot attack during the current combat. */
    public boolean creaturesCantAttackThisCombat;
    /** If non-null, only this permanent may attack during the current additional combat phase. */
    public UUID onlyPermanentCanAttackThisCombatId;
    /** If non-null, only these chosen permanents may attack during the current additional combat phase. */
    public Set<UUID> onlyPermanentsCanAttackThisCombatIds;
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
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionIds = new ConcurrentHashMap<>();
    /** Colors a chosen source must still have for {@link #playerSourceDamagePreventionIds} to apply, keyed by
     *  player and then source id; rechecked each time the source would deal damage (CR 609.7b). */
    public final Map<UUID, Map<UUID, Set<CardColor>>> playerSourceDamagePreventionRequiredColors = new ConcurrentHashMap<>();
    /** Whole-turn chosen-source prevention entries that gain life for black or red damage prevented. */
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionLifeGainIds = new ConcurrentHashMap<>();
    /** One-shot shields (Circle of Protection cycle): prevent the next damage event from a chosen source to a player. */
    public final List<PlayerSourceNextDamageShield> playerSourceNextDamageShields = Collections.synchronizedList(new ArrayList<>());
    /** One-shot shields (Sanctum Guardian / Honorable Passage): prevent the next damage event from a
     *  chosen source to ANY target (player, planeswalker, or creature). Consumed on first use. */
    public final List<SourceNextDamageToAnyTargetShield> sourceNextDamageToAnyTargetShields =
            Collections.synchronizedList(new ArrayList<>());
    /** One-shot reflection shields (Eye for an Eye): the next damage the chosen source deals to the
     *  protected player is also dealt back at that source's controller by Eye for an Eye. */
    public final List<EyeForAnEyeShield> eyeForAnEyeShields = Collections.synchronizedList(new ArrayList<>());
    /** One-shot redirection shields (Reflect Damage): the next damage event the chosen source would
     *  deal to any recipient this turn is dealt to that source's controller instead. Each entry is a
     *  chosen source permanent ID, consumed on first use. */
    public final List<UUID> reflectDamageToSourceControllerShields = Collections.synchronizedList(new ArrayList<>());
    /** One-shot redirection shields (Goblin Psychopath): the next combat damage from a source this
     *  turn is dealt to the stored controller instead. */
    public final List<SourceNextCombatDamageToControllerShield> sourceNextCombatDamageToControllerShields =
            Collections.synchronizedList(new ArrayList<>());
    /** One-shot redirection shields (Opal-Eye, Konda's Yojimbo): the next damage event the chosen source
     *  would deal to any recipient this turn is dealt to a fixed permanent instead. */
    public final List<SourceNextDamageRedirectToPermanentShield> sourceNextDamageRedirectToPermanentShields =
            Collections.synchronizedList(new ArrayList<>());
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
    /** The damage event the resolving spell or ability is in, or null outside a resolution. */
    public ResolutionDamageEvent openDamageEvent;
    public final Set<UUID> permanentsPreventedFromDealingDamage = ConcurrentHashMap.newKeySet();
    /** Creatures protected from damage caused by spells or abilities targeting them this turn. */
    public final Set<UUID> creaturesProtectedFromTargetingDamage = ConcurrentHashMap.newKeySet();
    /** Instant and sorcery spells whose damage is prevented for the rest of this turn. */
    public final List<TargetSpellDamagePreventionShield> targetSpellDamagePreventionShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Sorcery spells whose damage is redirected to their controllers for the rest of this turn. */
    public final List<TargetSorceryDamageRedirectShield> targetSorceryDamageRedirectShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Permanents prevented from dealing damage until a player's next turn (Gideon of the Trials +1),
     *  keyed by prevented permanent id → the player whose next turn ends the prevention. Unlike
     *  {@link #permanentsPreventedFromDealingDamage} these entries survive the cleanup step and are
     *  removed at the start of that player's next turn. */
    public final Map<UUID, UUID> permanentsPreventedFromDealingDamageUntilNextTurn = new ConcurrentHashMap<>();
    /** Permanents protected from all damage until a player's next turn, keyed by permanent id → expiring player id. */
    public final Map<UUID, UUID> permanentsProtectedFromDamageUntilNextTurn = new ConcurrentHashMap<>();
    /** Players whose damage (to themselves and their creatures) is fully prevented this turn (Safe Passage). */
    public final Set<UUID> playersWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players whose noncreature permanents are also fully protected from damage this turn (Endure). */
    public final Set<UUID> playersWithAllPermanentDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players whose creatures' damage is fully prevented this turn (Divine Light). */
    public final Set<UUID> playersWithAllCreatureDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players whose own damage (but not their creatures') is fully prevented this turn (Riot Control). */
    public final Set<UUID> playersWithAllPlayerDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players whose combat damage and controlled planeswalker damage are prevented this turn (Take the Bait). */
    public final Set<UUID> playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented = ConcurrentHashMap.newKeySet();
    /** Players whose own damage is fully prevented until the beginning of their next turn (Morningtide's Light). */
    public final Set<UUID> playersWithAllPlayerDamagePreventedUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players whose combat damage is prevented this turn; creates one token per damage prevented. */
    public final Map<UUID, CombatDamagePreventionTokenShield> combatDamagePreventionTokenShields =
            new ConcurrentHashMap<>();
    /** Players with protection from everything until the beginning of their next turn. */
    public final Set<UUID> playersWithProtectionFromEverythingUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players whose life totals can't change until the beginning of their next turn. */
    public final Set<UUID> playersWithLifeTotalCantChangeUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players for whom damage dealt by attacking creatures is prevented this turn (Deep Wood). */
    public final Set<UUID> playersWithDamageFromAttackersPrevented = ConcurrentHashMap.newKeySet();
    /** Players whose opponents' creatures cannot deal damage this turn (Thwart the Enemy). */
    public final Set<UUID> playersWithDamageFromOpponentCreaturesPrevented = ConcurrentHashMap.newKeySet();
    /** Players whose targeted opponent's creatures cannot deal combat damage this turn (Encircling Fissure). */
    public final Set<UUID> playersWithCombatDamageFromTargetOpponentCreaturesPrevented =
            ConcurrentHashMap.newKeySet();
    /** Players for whom damage from matching source permanents is prevented this turn. */
    public final Map<UUID, Set<PermanentPredicate>> playersWithDamageFromMatchingSourcesPrevented =
            new ConcurrentHashMap<>();
    /** Players for whom the next damage from a matching source is prevented. */
    public final Map<UUID, List<PermanentPredicate>> playerNextDamageFromMatchingSourcesPrevented =
            new ConcurrentHashMap<>();
    /** Players for whom damage from matching sources is prevented this turn to creatures they control. */
    public final Map<UUID, Set<PermanentPredicate>> playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented =
            new ConcurrentHashMap<>();
    /** Players whose permanents are protected for the game from damage dealt by named planes. */
    public final Map<UUID, Set<String>> playersWithDamageFromNamedPlanesPrevented = new ConcurrentHashMap<>();
    /**
     * One entry per Gather Specimens resolved this turn, naming its controller: each is a separate
     * replacement effect that gains control of a creature that would enter under an opponent's control.
     */
    public final List<UUID> playersGatheringSpecimensThisTurn = new java.util.concurrent.CopyOnWriteArrayList<>();
    /** Players who, this turn, gain control of tokens that would be created under an opponent's control (Crafty Cutpurse). */
    public final List<UUID> playersGatheringTokensThisTurn = new java.util.concurrent.CopyOnWriteArrayList<>();
    /** Players who, this turn, exile creatures that would enter without having been cast (Hallowed Moonlight). */
    public final Set<UUID> playersExilingUncastEnteringCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who, this turn, exile nontoken creatures that would enter without having been cast (Mistcaller). */
    public final Set<UUID> playersExilingUncastEnteringNontokenCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose effects, this turn, shuffle creatures entering from exile into their owners' libraries instead. */
    public final Set<UUID> playersShufflingCreaturesEnteringFromExileThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose effects, this turn, exile creatures that would die instead of putting them into graveyards. */
    public final Set<UUID> playersExilingCreaturesInsteadOfDyingThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose effects, this turn, exile creatures their opponents control instead of putting them into graveyards. */
    public final Set<UUID> playersExilingOpponentCreaturesInsteadOfDyingThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose effects, this turn, exile their controlled permanents instead of putting them into graveyards. */
    public final Set<UUID> playersExilingControlledPermanentsInsteadOfDyingThisTurn = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose damage is fully prevented this turn (Wellgabber Apothecary). */
    public final Set<UUID> creaturesWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players with an active effect that redirects damage dealt to any creature to them. */
    public final Set<UUID> playersRedirectingAllCreatureDamage = ConcurrentHashMap.newKeySet();
    /** Suppresses re-offering an optional creature-damage redirect while a declined event resumes. */
    public boolean resolvingDeclinedAllCreatureDamageRedirect;
    /**
     * Predicates whose matching permanents have all damage to them prevented this turn
     * (Ethersworn Shieldmage). Re-evaluated per damage event, so it covers permanents that
     * start/stop matching after the effect resolved (official ruling).
     */
    public final Set<PermanentPredicate> allDamagePreventionPredicates = ConcurrentHashMap.newKeySet();
    /** Predicates whose matching permanents controlled by the keyed player have all damage prevented. */
    public final Map<UUID, Set<PermanentPredicate>> allDamagePreventionPredicatesByController =
            new ConcurrentHashMap<>();
    /** Predicates whose matching permanents controlled by the keyed player have combat damage prevented. */
    public final Map<UUID, Set<PermanentPredicate>> combatDamagePreventionPredicatesByController =
            new ConcurrentHashMap<>();
    /** Specific creatures whose combat damage dealt to them is prevented this turn (Foxfire). */
    public final Set<UUID> creaturesWithCombatDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose combat damage is prevented this turn (Resistance Fighter). */
    public final Set<UUID> creaturesPreventedFromDealingCombatDamage = ConcurrentHashMap.newKeySet();
    /**
     * Creatures that assign no combat damage this turn (Carrion Wurm, Laccolith Titan). Unlike prevention this
     * can't be overridden by "damage can't be prevented" effects.
     */
    public final Set<UUID> creaturesAssigningNoCombatDamageThisTurn = ConcurrentHashMap.newKeySet();
    /** Creatures that assign no combat damage during the current combat only (cleared when it ends). */
    public final Set<UUID> creaturesAssigningNoCombatDamageThisCombat = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose combat damage dealt to them is prevented during the current combat. */
    public final Set<UUID> creaturesWithCombatDamagePreventedThisCombat = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose combat damage is prevented during the current combat. */
    public final Set<UUID> creaturesPreventedFromDealingCombatDamageThisCombat = ConcurrentHashMap.newKeySet();
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
    /** When true, damage can't be prevented this turn (Impractical Joke). Cleared at turn cleanup. */
    public boolean damageCantBePreventedThisTurn = false;
    /** Per-player life floors imposed by resolving effects until end of turn (Angel of Grace). */
    public final Map<UUID, Integer> damageLifeFloorsUntilEndOfTurn = new ConcurrentHashMap<>();
    /**
     * Every floor merged into {@link #damageLifeFloorsUntilEndOfTurn} this turn, as {@code (player, floor)} pairs.
     * The map keeps only the highest floor, so these are what lets a lower floor created during another player's
     * phase outlive the cleanup step that ends the higher one under Runed Terror's sequential phases.
     */
    public final List<Map.Entry<UUID, Integer>> damageLifeFloorContributionsUntilEndOfTurn = new CopyOnWriteArrayList<>();
    /** Active global damage replacement effects registered by spells for the current turn. */
    public final List<ReplaceDamageAboveThresholdThisTurnEffect> damageReplacementsThisTurn =
            Collections.synchronizedList(new ArrayList<>());
    /** When true, no player can gain life this turn (Skullcrack). Cleared at turn cleanup. */
    public boolean playersCantGainLifeThisTurn = false;
    /** When true, no player can lose life this turn (Everybody Lives!). Cleared at turn cleanup. */
    public boolean playersCantLoseLifeThisTurn = false;
    /** When true, no player can search a library this turn (Shadow of Doubt). Cleared at turn cleanup. */
    public boolean playersCantSearchLibrariesThisTurn = false;
    /** When true, no creature can attack this turn. Cleared at turn cleanup. */
    public boolean creaturesCantAttackThisTurn = false;
    /**
     * Turn-scoped rule restrictions on the creatures a player controls (CR 611.2c: a "can't attack"
     * effect modifies the rules, so it covers creatures that come under that player's control later and
     * stops covering creatures that leave it). Keyed by the player; each filter selects the creatures
     * affected. Cleared at turn cleanup.
     */
    public final Map<UUID, List<com.github.laxika.magicalvibes.model.filter.PermanentPredicate>>
            playerCreaturesCantAttackThisTurn = new ConcurrentHashMap<>();
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
    public int combatDamageToCreaturesDoublingsThisTurn = 0;
    /**
     * Per-player count of "if a source you control would deal damage this turn, it deals double
     * that damage instead" replacement effects (Insult). Each one doubles the damage, so the
     * multiplier is {@code 2^count}. Cleared at turn cleanup.
     */
    public final Map<UUID, Integer> controllerDamageDoublingsThisTurn = new ConcurrentHashMap<>();
    /**
     * Per-permanent count of effects that double damage dealt by that permanent this turn
     * (Overblaze). Each instance doubles the damage again, so the multiplier is {@code 2^count}.
     * Cleared at turn cleanup.
     */
    public final Map<UUID, Integer> permanentDamageDoublingsThisTurn = new ConcurrentHashMap<>();
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
    /** Active "whenever a creature dies this turn" delayed triggers, cleared at turn cleanup. */
    public final List<CreatureDeathTriggerWatcher> creatureDeathTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Active delayed triggers watching one exact creature permanent, removed when that permanent dies. */
    public final List<TargetedCreatureDeathTriggerWatcher> targetedCreatureDeathTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Active delayed triggers for creatures damaged by a particular source, cleared at turn cleanup. */
    public final List<DamagedCreatureDeathTriggerWatcher> damagedCreatureDeathTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    public final List<CreatureEntersTriggerWatcher> allyCreatureEntersTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Damage redirect shields (e.g. Vengeful Archon): prevention shields that redirect prevented damage to a target player. */
    public final List<DamageRedirectShield> damageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Comeuppance: prevent opponent-source damage to a player and their planeswalkers this turn. */
    public final List<ComeuppanceDamagePreventionShield> comeuppanceDamagePreventionShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Judgment of Alexander: prevent opponent-source damage to a player and retaliate against creature sources. */
    public final List<JudgmentOfAlexanderDamagePreventionShield> judgmentOfAlexanderDamagePreventionShields =
            Collections.synchronizedList(new ArrayList<>());
    public final List<ChannelHarmShield> channelHarmShields = Collections.synchronizedList(new ArrayList<>());
    /** Pending redirect damage to deal after damage prevention (populated by DamagePreventionService, consumed by callers). */
    public final List<DamageRedirectShield> pendingRedirectDamage = Collections.synchronizedList(new ArrayList<>());
    /** Source-specific damage redirect shields (e.g. Harm's Way): prevent damage from a chosen source and redirect to any target. */
    public final List<SourceDamageRedirectShield> sourceDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Target+source-specific damage prevention shields (e.g. Healing Grace): prevent next N damage from a chosen source to a specific target. */
    public final List<TargetSourceDamagePreventionShield> targetSourceDamagePreventionShields = Collections.synchronizedList(new ArrayList<>());
    /** Candles' Glow-style shields: prevent next N damage to a target and gain that much life. */
    public final List<DamagePreventionLifeGainShield> damagePreventionLifeGainShields = Collections.synchronizedList(new ArrayList<>());
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
    /** Creature-specific damage redirect shields (e.g. Oracle's Attendants): redirect all damage a chosen source would deal to a specific creature this turn onto another permanent. */
    public final List<CreatureDamageRedirectShield> creatureDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Saving Grace: redirect all damage this turn dealt to a protected player or their permanents onto a fixed creature (any source, unlimited). */
    public final List<TurnDamageRedirectToCreatureShield> turnDamageRedirectToCreatureShields = Collections.synchronizedList(new ArrayList<>());
    public final List<CreatureControllerDamageRedirectShield> creatureControllerDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Mirror Strike: redirect all combat damage this turn from a chosen source to the protected player onto its controller. */
    public final List<TurnSourceDamageRedirectToControllerShield> turnSourceDamageRedirectToControllerShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Martyrdom: redirect the next N damage this turn dealt to a protected player onto a fixed permanent (any source). */
    public final List<PlayerNextDamageRedirectShield> playerNextDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Glarecaster: redirect the next damage dealt to this permanent and/or its controller onto a fixed target. */
    public final List<SourcePermanentAndControllerNextDamageRedirectShield>
            sourcePermanentAndControllerNextDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** One-shot redirection shields (General's Regalia): the next damage event from a chosen source
     *  to the controller is dealt to a fixed creature instead. */
    public final List<PlayerSourceNextDamageRedirectShield> playerSourceNextDamageRedirectShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Aegis of Honor: redirect the next damage from an instant or sorcery spell dealt to a protected player onto that spell's controller. */
    public final List<UUID> playerNextInstantOrSorceryDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Soltari Guerrillas: redirect the next combat damage a specific source would deal to an opponent onto a fixed creature. */
    public final List<SourceNextCombatDamageToOpponentRedirectShield> sourceNextCombatDamageToOpponentRedirectShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Queue for "each player returns up to N cards from graveyard to battlefield" choices. */
    public final List<PendingGraveyardReturnChoice> pendingGraveyardReturnQueue = Collections.synchronizedList(new ArrayList<>());
    public final List<DeferredSpellCopy> deferredGraveyardCastCopies = Collections.synchronizedList(new ArrayList<>());
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
    /** Players who have been granted "no maximum hand size" for the rest of the game. */
    public final Set<UUID> playersWithNoMaximumHandSize = ConcurrentHashMap.newKeySet();
    /** Players who have no maximum hand size until the beginning of their next turn. */
    public final Set<UUID> playersWithNoMaximumHandSizeUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Timestamp of each player's latest set-based "no maximum hand size" grant, so later hand-size setters can override it (CR 613.7). */
    public final Map<UUID, Long> noMaximumHandSizeGrantTimestamps = new ConcurrentHashMap<>();
    /** Players who can't gain life for the rest of the game (e.g. Stigma Lasher). */
    public final Set<UUID> playersWhoCantGainLifeRestOfGame = ConcurrentHashMap.newKeySet();
    /** Players who play with their hand revealed for the rest of the game (e.g. Truth or Dare). */
    public final Set<UUID> playersWithHandRevealed = ConcurrentHashMap.newKeySet();
    /** Players who can't gain life this turn (e.g. opponents can't gain life). Cleared at turn cleanup. */
    public final Set<UUID> playersWhoCantGainLifeThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose life gain this turn is replaced with 0 (e.g. Flames of the Blood Hand). Cleared at turn cleanup. */
    public final Set<UUID> playersWhoGainNoLifeThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who can't lose life this turn (e.g. Courageous Resolve). Cleared at turn cleanup. */
    public final Set<UUID> playersWhoCantLoseLifeThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks source-linked animations (Awakener Druid-style).
     *  Maps animated target permanent UUID → source permanent UUID.
     *  When the source leaves the battlefield, the target's animation is cleared. */
    public final Map<UUID, UUID> sourceLinkedAnimations = new ConcurrentHashMap<>();

    /** Temporary characteristic changes applied to cards while they remain in a graveyard. */
    public record GraveyardCardAnimation(int power, int toughness, Set<CardColor> grantedColors,
                                         Set<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes,
                                         long graveyardEntryVersion) {
        public GraveyardCardAnimation {
            grantedColors = Set.copyOf(grantedColors);
            grantedSubtypes = Set.copyOf(grantedSubtypes);
            grantedCardTypes = Set.copyOf(grantedCardTypes);
        }
    }

    /** Keyed by card identity and the specific continuous stay in the graveyard it affects. */
    public final Map<UUID, GraveyardCardAnimation> temporaryGraveyardCardAnimationsUntilEndOfTurn =
            new ConcurrentHashMap<>();

    /** Per-player: spells controlled by this player can't be countered by spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerSpellsCantBeCounteredByColorsThisTurn = new ConcurrentHashMap<>();

    /** Players whose spells can't be countered this turn. Cleared at end of turn. */
    public final Set<UUID> playersSpellsCantBeCounteredThisTurn = ConcurrentHashMap.newKeySet();

    /** Players whose creature spells can't be countered this turn. Cleared at end of turn. */
    public final Set<UUID> playersCreatureSpellsCantBeCounteredThisTurn = ConcurrentHashMap.newKeySet();

    /** Per-player: creatures controlled by this player can't be the targets of spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerCreaturesCantBeTargetedByColorsThisTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has hexproof from these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerHexproofFromColorsThisTurn = new ConcurrentHashMap<>();
    public final Set<UUID> playersWithHexproofThisTurn = ConcurrentHashMap.newKeySet();
    public final Set<UUID> playersWithShroudThisTurn = ConcurrentHashMap.newKeySet();

    /** Per-permanent: this permanent has hexproof from these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> permanentHexproofFromColorsThisTurn = new ConcurrentHashMap<>();

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

    /** Temporary color override applied to a spell on the stack, cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> spellColorOverridesUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has protection from these colors until end of turn (e.g. Faith's Shield fateful hour). Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerProtectionFromColorsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has protection from these players until end of turn. */
    public final Map<UUID, Set<UUID>> playerProtectionFromPlayerIdsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has protection from these players until the beginning of their next turn. */
    public final Map<UUID, Set<UUID>> playerProtectionFromPlayerIdsUntilNextTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has a temporary targeting keyword until end of turn. */
    public final Map<UUID, Set<Keyword>> playerKeywordsUntilEndOfTurn = new ConcurrentHashMap<>();
    /** Per-player: this player has a temporary targeting keyword until the beginning of their next turn. */
    public final Map<UUID, Set<Keyword>> playerKeywordsUntilNextTurn = new ConcurrentHashMap<>();
    /** Static effects granted directly to players until end of turn (e.g. Angel's Grace). */
    public final Map<UUID, List<CardEffect>> playerStaticEffectsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Players who can't cast spells this turn (e.g. Silence). Cleared at end of turn and on new turn. */
    public final Set<UUID> playersSilencedThisTurn = ConcurrentHashMap.newKeySet();

    /** Maximum total spells each player may cast this turn from resolution-time restrictions. */
    public final Map<UUID, Integer> playersMaxSpellsThisTurn = new ConcurrentHashMap<>();
    /**
     * Every limit merged into {@link #playersMaxSpellsThisTurn} this turn, as {@code (player, limit)} pairs. The map
     * keeps only the most restrictive limit, so these are what lets a looser limit created during another player's
     * phase outlive the cleanup step that ends the tighter one under Runed Terror's sequential phases.
     */
    public final List<Map.Entry<UUID, Integer>> playersMaxSpellsContributionsThisTurn = new CopyOnWriteArrayList<>();

    /** Players who can't cast spells for the rest of the game because an Epic spell resolved. */
    public final Set<UUID> playersCantCastSpellsForRestOfGame = ConcurrentHashMap.newKeySet();

    /**
     * Card names opponents of the key player can't cast spells with, until that player's next turn
     * (Comply). Keyed by the naming controller → set of forbidden names. Cleared at the start of
     * that controller's next turn (survives end-of-turn cleanup).
     */
    public final Map<UUID, Set<String>> opponentsCantCastNamedSpellsUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Mana values opponents can't cast until the key player's next turn. */
    public final Map<UUID, Set<Integer>> opponentsCantCastSpellsWithManaValueUntilControllerNextTurn =
            new ConcurrentHashMap<>();
    /** Card names a specific player can't cast until the key player's next turn (Reflector Mage). */
    public final Map<UUID, Map<UUID, Set<String>>> playersCantCastNamedSpellsUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Card names no player may cast or play until the key player's next turn. */
    public final Map<UUID, Set<String>> spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Players who can't cast noncreature spells until the key player's next turn. */
    public final Map<UUID, Set<UUID>> playersCantCastNoncreatureSpellsUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Players who can't cast spells from outside their hands until the key player's next turn. */
    public final Map<UUID, Set<UUID>> playersCantCastSpellsFromOutsideHandUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Players who can't play cards from their hand until the key player's next turn. */
    public final Map<UUID, Set<UUID>> playersCantPlayCardsFromHandUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Card types all players can't cast until the end of the key player's next turn. */
    public final Map<UUID, Map<CardType, Integer>> playersCantCastSpellTypesUntilEndOfControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Cards revealed in their owner's hand until that owner's next turn. */
    public final Map<UUID, UUID> cardsRevealedInHandUntilOwnerNextTurn = new ConcurrentHashMap<>();

    /** Cards revealed in their owner's hand (e.g. Forecast) until the end of the current upkeep. */
    public final Map<UUID, UUID> cardsRevealedInHandUntilUpkeepEnd = new ConcurrentHashMap<>();

    /** Cards that can't be played from their owner's hand until that owner's next turn. */
    public final Map<UUID, UUID> cardsCantBePlayedInHandUntilOwnerNextTurn = new ConcurrentHashMap<>();

    /** Card predicates the key player may cast as though they had flash until their next turn. */
    public final Map<UUID, Set<CardPredicate>> cardTypeFlashGrantsUntilNextTurn =
            new ConcurrentHashMap<>();

    /** Land subtype -&gt; extra mana colors and creating controllers, used when a player taps that subtype for mana
     *  this turn (Chaos Moon's odd branch: "whenever a player taps a Mountain for mana, that player
     *  adds an additional {R}"). Each resolved effect contributes separately. Cleared at end of turn. */
    public final Map<CardSubtype, List<TemporaryLandTapMana>> extraManaOnLandSubtypeTapThisTurn = new ConcurrentHashMap<>();

    /** Land subtype -&gt; the mana color lands of that subtype produce instead of any other type this
     *  turn (Chaos Moon's even branch: "that Mountain produces colorless mana instead of any other
     *  type"). Amount is unchanged. Cleared at end of turn. */
    public final Map<CardSubtype, ManaColor> landSubtypeFixedManaColorThisTurn = new ConcurrentHashMap<>();

    /** The mana color nonbasic lands produce instead of any other color this turn. */
    public volatile ManaColor nonbasicLandsFixedManaColorThisTurn;

    /** The mana color every land produces instead of any other color this turn (Hall of Gemstone,
     *  chosen by the player whose upkeep it is). Amount is unchanged. Null when inactive; cleared at
     *  end of turn. */
    public volatile ManaColor allLandsFixedManaColorThisTurn;

    /** Players whose spells and abilities replace colored mana they would add with white mana this turn. */
    public final Set<UUID> playersWithColoredManaReplacementThisTurn = ConcurrentHashMap.newKeySet();

    /** Players who may spend white mana as though it were mana of any color this turn. */
    public final Set<UUID> playersWithWhiteManaAsAnyColorThisTurn = ConcurrentHashMap.newKeySet();

    /** Players whose land taps for mana produce one mana of a color chosen separately per tap. */
    public final Set<UUID> playersWithLandManaChoiceReplacementThisTurn = ConcurrentHashMap.newKeySet();

    /** The mana color the specified players' lands produce instead of any other type this turn. */
    public final Map<UUID, ManaColor> landManaFixedColorThisTurn = new ConcurrentHashMap<>();

    /**
     * Players who tapped a land for mana this turn (Desolation). Recorded whenever a land is tapped
     * for mana, regardless of whether Desolation is on the battlefield. Cleared at turn start.
     */
    public final Set<UUID> playersWhoTappedLandForManaThisTurn = ConcurrentHashMap.newKeySet();

    /** Players who can't play lands this turn (e.g. Moonhold). Cleared at end of turn. */
    public final Set<UUID> playersCantPlayLandsThisTurn = ConcurrentHashMap.newKeySet();

    /** Card types each player can't cast this turn (e.g. Moonhold, Abeyance). Cleared at end of turn. */
    public final Map<UUID, Set<CardType>> playersCantCastSpellTypesThisTurn = new ConcurrentHashMap<>();

    /** Card types each player can't cast during their next turn. Promoted when that turn begins. */
    public final Map<UUID, Set<CardType>> playersCantCastSpellTypesNextTurn = new ConcurrentHashMap<>();

    /** Players who can't cast noncreature spells this turn (e.g. Aurelia's Fury). Cleared at end of turn. */
    public final Set<UUID> playersCantCastNoncreatureSpellsThisTurn = ConcurrentHashMap.newKeySet();

    /** Players who can't activate abilities this turn — including mana abilities (e.g. Sen Triplets).
     *  Cleared at end of turn. */
    public final Set<UUID> playersCantActivateAbilitiesThisTurn = ConcurrentHashMap.newKeySet();

    /** Players who can't activate abilities that aren't mana abilities this turn (e.g. Abeyance).
     *  Cleared at end of turn. */
    public final Set<UUID> playersCantActivateNonManaAbilitiesThisTurn = ConcurrentHashMap.newKeySet();
    public final Set<UUID> playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Extra loyalty activations each planeswalker a player controls may use this turn, granted to the player
     *  (The Chain Veil); applies to planeswalkers that come under the player's control later in the turn too. */
    public final Map<UUID, Integer> playerExtraLoyaltyActivationsThisTurn = new ConcurrentHashMap<>();

    /** Players whose planeswalkers may activate loyalty abilities twice this turn rather than once (Urza Assembles
     *  the Titans chapter III). Membership is a cap, not a count: several such effects do not stack. */
    public final Set<UUID> playersAllowedTwiceLoyaltyActivationsThisTurn = ConcurrentHashMap.newKeySet();

    /** Sen Triplets: while set, {@link #senControllerPlayerId} may play lands and cast spells from
     *  {@link #senControlledPlayerId}'s hand this turn (that hand is also revealed to the controller,
     *  and the controlled player is silenced + can't activate abilities). Both cleared at end of turn. */
    public UUID senControllerPlayerId;
    public UUID senControlledPlayerId;

    /** Whether {@code playerId} may play lands and cast spells from {@code handOwnerId}'s hand this turn (Sen Triplets). */
    public boolean canPlayFromHandOf(UUID playerId, UUID handOwnerId) {
        return playerId != null && handOwnerId != null && !playerId.equals(handOwnerId)
                && playerId.equals(senControllerPlayerId) && handOwnerId.equals(senControlledPlayerId);
    }

    /** Card IDs that have been granted flashback until end of turn (e.g. Past in Flames).
     *  The flashback cost for these cards equals their mana cost unless overridden in
     *  {@link #cardsGrantedFlashbackCostsUntilEndOfTurn}. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedFlashbackUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Subset of {@link #cardsGrantedFlashbackUntilEndOfTurn} granted while graveyard cards had already lost all
     *  abilities, so the later grant survives that loss (e.g. Snapcaster Mage under Yixlid Jailer). */
    public final Set<UUID> cardsGrantedFlashbackDespiteLostAbilitiesUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Optional fixed flashback costs for cards granted flashback until end of turn. */
    public final Map<UUID, String> cardsGrantedFlashbackCostsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Card IDs that have been granted Warp {0} until end of turn. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedWarpUntilEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Exile entries created by Warp, bound to the specific stay in exile. */
    public final Map<UUID, Long> warpExileEntryVersions = new ConcurrentHashMap<>();

    /** Card IDs that have been granted harmonize until end of turn. The harmonize cost for these
     * cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedHarmonizeUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs that have been granted jump-start until end of turn. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedJumpStartUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs that have been granted embalm until end of turn (e.g. Cursecloth Wrappings).
     *  The embalm cost for these cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedEmbalmUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs and costs that have been granted unearth until end of turn. Cleared at end of turn. */
    public final Map<UUID, String> cardsGrantedUnearthUntilEndOfTurn = new ConcurrentHashMap<>();
    /** Graveyard-entry identity each unearth grant in {@link #cardsGrantedUnearthUntilEndOfTurn} was made for. */
    public final Map<UUID, Long> cardsGrantedUnearthGraveyardVersions = new ConcurrentHashMap<>();

    /** Player IDs that may cast spells as though they had flash until end of turn (Alchemist's
     *  Refuge, Vedalken Orrery-style one-shot grants). Cleared at end of turn. */
    public final Set<UUID> playersWithFlashUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may cast spells from hand without paying their mana costs until end of turn. */
    public final Set<UUID> playersWithFreeHandCastUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may look at face-down creatures they don't control until end of turn
     *  (Lumbering Laundry). Cleared at end of turn. */
    public final Set<UUID> playersWhoMayLookAtFaceDownCreaturesThisTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may cast spells of a given type as though they had flash until end of turn.
     *  Cleared at end of turn. */
    public final Map<UUID, Set<CardPredicate>> cardTypeFlashGrantsThisTurn = new ConcurrentHashMap<>();

    /** Pending "the next spell of this type you cast this turn can be cast as though it had flash"
     *  grants (Quicken), keyed by player. Each list element is one unconsumed grant; a grant is
     *  consumed by {@link #recordSpellCast} when a matching spell is cast. Cleared at end of turn. */
    public final Map<UUID, List<CardType>> nextSpellFlashGrantsThisTurn = new ConcurrentHashMap<>();
    /** Pending flash grants for the next spell of a chosen creature subtype, keyed by player. */
    public final Map<UUID, List<CardSubtype>> nextSpellChosenSubtypeFlashGrantsThisTurn = new ConcurrentHashMap<>();
    /** Pending one-shot convoke grants for the next spell each player casts this turn. */
    public final Map<UUID, Integer> nextSpellConvokeGrantsThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<NextSpellCostReduction>> nextSpellCostReductionsThisTurn = new ConcurrentHashMap<>();
    /** Pending life alternatives for the next spell, keyed by controller. */
    public final Map<UUID, Integer> nextSpellPayLifeEqualToManaValueThisTurn = new ConcurrentHashMap<>();
    /** Pending one-shot free-cast permissions for the next matching spell this turn. */
    public final Map<UUID, List<CardPredicate>> nextSpellFreeCastPermissionsThisTurn = new ConcurrentHashMap<>();

    /** Pending "the next creature spell you cast this turn ..." grants (Savage Summoning), keyed by
     *  player. Every unconsumed grant applies to the same next creature spell and is consumed by
     *  {@link #recordSpellCast}. Cleared at end of turn. */
    public final Map<UUID, List<CreatureSpellEmpowerment>> nextCreatureSpellEmpowermentsThisTurn = new ConcurrentHashMap<>();

    /** Pending "When you next cast a creature spell this turn, ..." delayed triggers, keyed by player.
     *  All fire on the next creature spell cast and are cleared at end of turn. */
    public final Map<UUID, List<DelayedSpellCastTrigger>> nextCreatureSpellDelayedTriggersThisTurn = new ConcurrentHashMap<>();

    /** Pending one-shot grants that give the next creature spell cascade, keyed by player. */
    public final Map<UUID, Integer> nextCreatureSpellCascadeThisTurn = new ConcurrentHashMap<>();
    /** Persistent one-time creature-spell boons, keyed by player and consumed by {@link #recordSpellCast}. */
    public final Map<UUID, List<CreatureSpellEmpowerment>> nextCreatureSpellEmpowerments = new ConcurrentHashMap<>();

    /** Extra +1/+1 counters a spell's permanent enters the battlefield with, keyed by card id
     *  (Savage Summoning). Consumed as an as-enters replacement by {@code BattlefieldEntryService}
     *  and cleared at end of turn. */
    public final Map<UUID, Integer> spellAdditionalEnterCounters = new ConcurrentHashMap<>();

    /** Entry counters granted to a permanent spell by a cast permission. */
    public final Map<UUID, Map<CounterType, Integer>> spellEntryCounters = new ConcurrentHashMap<>();

    /** Subtypes granted to a permanent spell by a one-shot graveyard cast permission. */
    public final Map<UUID, Set<CardSubtype>> spellGrantedSubtypesOnEntry = new ConcurrentHashMap<>();

    /** One-shot Mystic Reflection replacements waiting for the next creature or planeswalker entry event. */
    public final List<PendingMysticReflection> pendingMysticReflections =
            Collections.synchronizedList(new ArrayList<>());

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

    /** Player IDs that may tap lands they don't control for mana until end of turn (Piracy). The
     *  mana produced this way may only be spent to cast spells. Cleared at end of turn. */
    public final Set<UUID> mayTapLandsForSpellsUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may pay 1 life to add {C} any time they could activate a mana ability until end
     *  of turn (Channel). Cleared at end of turn. */
    public final Set<UUID> mayPayLifeForColorlessManaUntilEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Controller IDs to Guardian Angel target IDs that may receive another one-damage shield this turn. */
    public final Map<UUID, Set<UUID>> guardianAngelTargetsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Player IDs that may cast the top card of their graveyard if it is an instant or sorcery until
     *  end of turn (Bösium Strip). Spells cast this way exile instead of going to a graveyard.
     *  Cleared at end of turn. */
    public final Set<UUID> mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn = ConcurrentHashMap.newKeySet();

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
                                              boolean escape) {
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
    public record GraveyardAdventureCastPermission(UUID castingPlayerId, int expireTurn) {}

    /** Card UUIDs that may be cast as an Adventure from the controller's graveyard until a turn ends. */
    public final Map<UUID, GraveyardAdventureCastPermission> graveyardAdventureCastPermissions =
            new ConcurrentHashMap<>();

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
                                                boolean anyManaType) {
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
                                                CardPredicate filter) {}

    /**
     * A one-card, source-linked cast grant that also covers cards exiled with the source later in the
     * turn (CR 611.2c: a continuous effect that only modifies the rules is not limited to the objects
     * that existed when it began). Each card it covers is materialised as an {@link ExileCastPermission}
     * sharing {@code grantId}; consuming one removes the whole grant.
     */
    public record ExileCastWildcardPermission(UUID grantId, UUID sourcePermanentId, UUID sourceCardId,
                                              UUID castingPlayerId, boolean ownOnly, CardPredicate filter,
                                              boolean withoutPayingManaCost,
                                              boolean putOnBottomOfOwnersLibrary) {}

    /** One source-linked, one-card cast grant created by a temporary activated ability. */
    public record ExileCastPermission(UUID grantId, UUID sourcePermanentId, UUID castingPlayerId,
                                      UUID cardId, boolean withoutPayingManaCost,
                                      boolean putOnBottomOfOwnersLibrary,
                                      boolean payLifeEqualToManaValue) {
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
    public record ForetoldCardCastPermission(UUID playerId, boolean singleUse) {
    }

    /** Targeted cards that may be cast from a graveyard this turn.
     *  Maps graveyard card UUID -> source permanent and casting player (e.g. Havengul Lich).
     *  Cleared at end of turn. */
    public final Map<UUID, GraveyardCardCastPermission> graveyardCardCastPermissionsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Every cast-permission grant (Havengul Lich) made for a graveyard card this turn, one entry per
     *  activation. {@link #graveyardCardCastPermissionsUntilEndOfTurn} holds a single permission per
     *  card, so this keeps each grant's own "when you cast it" rider and each player's permission.
     *  Cleared at end of turn. */
    public final Map<UUID, List<GraveyardCardCastPermission>> graveyardCardCastPermissionGrantsUntilEndOfTurn =
            new ConcurrentHashMap<>();

    /** Number of separate "whenever you cast an instant or sorcery spell, copy it" abilities each player
     *  controls until end of turn (e.g. one per The Mirari Conjecture chapter III resolution). Each one
     *  triggers on its own. Cleared at end of turn. */
    public final Map<UUID, Integer> spellCopyGrantsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Number of conspire copies still to queue for each spell cast with conspire cost(s) paid
     *  (CR 702.78). Each instance of conspire is paid separately and queues its own "copy it, you
     *  may choose a new target" trigger. Consumed by {@code TriggerCollectionService} when the
     *  spell-cast triggers are collected. */
    public final Map<UUID, Integer> conspiredSpellCopyCounts = new ConcurrentHashMap<>();

    /** Number of casualty copies still to queue for each just-cast spell. */
    public final Map<UUID, Integer> casualtySpellCopyCounts = new ConcurrentHashMap<>();

    /** Pending one-shot spell copy triggers from mana abilities (e.g. Primal Wellspring).
     *  Each value tracks how many copies are pending for that player.
     *  Decremented when an instant/sorcery is cast; cleared when mana pools drain. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyCount = new ConcurrentHashMap<>();

    /** Path of Ancestry mana choices awaiting completion, keyed by source permanent id. */
    public final Map<UUID, Integer> pendingPathOfAncestryManaChoices = new ConcurrentHashMap<>();

    /** Pending one-shot spell copy triggers from mana abilities that only copy <em>red</em>
     *  instants and sorceries (Pyromancer's Goggles). Same lifecycle as
     *  {@link #pendingNextInstantSorceryCopyCount}: decremented when a matching spell is cast,
     *  cleared when mana pools drain. */
    public final Map<UUID, Integer> pendingNextRedInstantSorceryCopyCount = new ConcurrentHashMap<>();

    /** Pending one-shot "when you next cast an instant or sorcery spell this turn, copy that spell"
     *  delayed triggers (e.g. Chandra, the Firebrand's −2). Each value tracks how many copies are
     *  pending for that player. Decremented when an instant/sorcery is cast; unlike
     *  {@link #pendingNextInstantSorceryCopyCount} these survive mana drain and are cleared at end
     *  of turn. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyThisTurnCount = new ConcurrentHashMap<>();
    /** How many of the copies in {@link #pendingNextInstantSorceryCopyThisTurnCount} come from separate
     *  one-copy delayed triggers (Teach by Example). Each one triggers on its own (CR 603.7), so when the next
     *  spell is cast they are queued as separate copy triggers rather than one trigger copying several times. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyThisTurnSingleTriggerCount = new ConcurrentHashMap<>();
    /** The source card of the delayed trigger counted in {@link #pendingNextInstantSorceryCopyThisTurnCount},
     *  so the copy trigger's source is the object that created it (CR 603.7e), not the spell it copies. */
    public final Map<UUID, Card> pendingNextInstantSorceryCopyThisTurnCountSource = new ConcurrentHashMap<>();
    /** Pending one-shot grants that give the next instant or sorcery spell Storm this turn. */
    public final Map<UUID, Integer> pendingNextInstantSorceryStormThisTurnCount = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> pendingNextInstantSorceryCastFromHandToHandThisTurnCount =
            new ConcurrentHashMap<>();

    /** Pending mana-value-limited one-shot spell copy triggers for the current turn. */
    public final Map<UUID, List<Integer>> pendingNextInstantSorceryCopyThisTurnMaxManaValues =
            new ConcurrentHashMap<>();
    /** Pending one-shot spell-copy counts evaluated when the next instant or sorcery is cast. */
    public final Map<UUID, List<DynamicAmount>> pendingNextInstantSorceryCopyThisTurnDynamicCounts =
            new ConcurrentHashMap<>();
    /** Source cards of {@link #pendingNextInstantSorceryCopyThisTurnDynamicCounts}, in the same order. */
    public final Map<UUID, List<Card>> pendingNextInstantSorceryCopyThisTurnDynamicSources =
            new ConcurrentHashMap<>();
    /** Pending one-shot "when you next cast a spell this turn, copy that spell" delayed triggers. */
    public final Map<UUID, Integer> pendingNextSpellCopyThisTurnCount = new ConcurrentHashMap<>();
    /** Pending one-shot filtered spell-copy triggers for the current turn. */
    public final Map<UUID, List<CopyNextSpellCastThisTurnEffect>> pendingNextFilteredSpellCopiesThisTurn =
            new ConcurrentHashMap<>();
    /** Pending one-shot copies for the next activated ability with X in its activation cost. */
    public final Map<UUID, Integer> pendingNextXActivatedAbilityCopyThisTurnCount =
            new ConcurrentHashMap<>();

    /** Pending one-shot grants that make the next spell cast this turn uncounterable. */
    public final Map<UUID, Integer> pendingNextSpellUncounterableThisTurnCount = new ConcurrentHashMap<>();

    /** Pending permissions to spend mana of any type on one spell cast this turn (North Star). */
    public final Map<UUID, Integer> pendingAnyManaTypeForNextSpellThisTurnCount = new ConcurrentHashMap<>();

    /** Spells whose mana was paid using a pending any-type mana permission. */
    public final Set<UUID> spellsPaidUsingPendingAnyManaTypeThisTurn = ConcurrentHashMap.newKeySet();

    /** Pending one-shot grants that make the next instant or sorcery cast this turn uncounterable. */
    public final Map<UUID, Integer> pendingNextInstantSorceryUncounterableThisTurnCount = new ConcurrentHashMap<>();

    /** Pending one-shot loyalty-ability copy triggers for the current turn. */
    public final Map<UUID, Integer> pendingNextLoyaltyAbilityCopyThisTurnCount = new ConcurrentHashMap<>();

    /**
     * Players whose pending instant/sorcery and loyalty-ability copies come from one shared delayed
     * trigger (Repeated Reverberation). Whichever event consumes that trigger consumes both counts.
     */
    public final Set<UUID> pendingNextSpellOrLoyaltyCopyLinkedThisTurn = ConcurrentHashMap.newKeySet();

    /**
     * The source of each pending loyalty-ability copy in {@link #pendingNextLoyaltyAbilityCopyThisTurnCount},
     * in the order the copies were granted, so each copy trigger keeps the object that created it.
     */
    public final Map<UUID, List<PendingCopySource>> pendingNextLoyaltyAbilityCopySourcesThisTurn =
            new ConcurrentHashMap<>();

    /** The card and permanent (null for a non-permanent source) that granted a pending copy. */
    public record PendingCopySource(Card card, UUID sourcePermanentId) {}

    /**
     * One pending dynamic-count instant/sorcery copy trigger: an element of
     * {@link #pendingNextInstantSorceryCopyThisTurnDynamicCounts} paired with the source card at the same index of
     * {@link #pendingNextInstantSorceryCopyThisTurnDynamicSources} ({@code null} when none was recorded).
     */
    public record PendingDynamicCopy(DynamicAmount count, Card source) {}

    /** Permanents whose source subtype choice is temporary and expires during cleanup. */
    public final Set<UUID> temporaryChosenSubtypePermanentIds = ConcurrentHashMap.newKeySet();

    /**
     * Until-end-of-turn delayed triggers created by choosing a subtype for a source: whenever the
     * controller activates an ability of a planeswalker of that subtype, the ability is copied
     * (Leori, Sparktouched Hunter). Each choice is its own delayed trigger and outlives its source.
     */
    public final List<ChosenSubtypeActivationCopy> chosenSubtypeActivationCopiesThisTurn =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /** One delayed "copy abilities of planeswalkers of this type" trigger and the object that created it. */
    public record ChosenSubtypeActivationCopy(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                              CardSubtype subtype) {}

    /** Pending one-shot non-mana exhaust-ability copy triggers for the current turn. */
    public final Map<UUID, Integer> pendingNextExhaustAbilityCopyThisTurnCount = new ConcurrentHashMap<>();

    /**
     * The source of each pending copy in {@link #pendingNextExhaustAbilityCopyThisTurnCount}, in the
     * order the copies were granted, so each copy trigger keeps the object that created it.
     */
    public final Map<UUID, List<PendingCopySource>> pendingNextExhaustAbilityCopySourcesThisTurn =
            new ConcurrentHashMap<>();


    /** "Whenever a creature enters this turn, you may draw a card" delayed triggers (Beck). */
    public final Map<UUID, List<Card>> creatureEntersDrawSourcesThisTurn = new ConcurrentHashMap<>();

    /** Persistent "Whenever a creature you control enters, you may draw a card" triggers. */
    public final Map<UUID, List<Card>> creatureEntersDrawSources = new ConcurrentHashMap<>();

    /** "At the beginning of each combat this turn, untap all creatures that attacked this turn" delayed triggers. */
    public final Map<UUID, List<Card>> untapAttackedCreaturesEachCombatThisTurnSources = new ConcurrentHashMap<>();

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
     * the card itself, because only the Adventure's mana value fits the effect's limit (CR 715.3a).
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

    /** Player UUID to card UUID for a temporary free-play permission on the current library top card. */
    public final Map<UUID, UUID> libraryTopCardFreePlayPermissionsUntilEndOfTurn = new ConcurrentHashMap<>();
    /** Players who may look at and play cards from the top of their library until end of turn. */
    public final Set<UUID> playersAllowedToPlayFromLibraryTopUntilEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Players who may play cards from the top of their library for life until end of turn. */
    public final Set<UUID> libraryTopCardLifePlayPermissionsUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Pairs of caster and library owner for temporary access to another player's library top. */
    public record LibraryTopCardPermission(UUID castingPlayerId, UUID libraryOwnerId) {}

    /** Temporary permissions to look at and play another player's current library top card. */
    public final Set<LibraryTopCardPermission> libraryTopCardPermissionsUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Maps exiled card UUID → player UUID who has permission to play it (e.g. Praetor's Grasp). */
    public final Map<UUID, UUID> exilePlayPermissions = new ConcurrentHashMap<>();
    /** Face-down exiled cards a player may look at for as long as they remain exiled. */
    public final Map<UUID, UUID> exileLookPermissions = new ConcurrentHashMap<>();

    /** Players who retain permission to look at a face-down card while it remains exiled. */
    public final Map<UUID, Set<UUID>> additionalExileLookPermissions = new ConcurrentHashMap<>();
    /** Maps cards granted by one effect to their shared limited exile-play permission group. */
    public final Map<UUID, UUID> exilePlayPermissionGroups = new ConcurrentHashMap<>();
    /** Remaining plays for each shared limited exile-play permission group. */
    public final Map<UUID, Integer> exilePlayPermissionGroupUsesRemaining = new ConcurrentHashMap<>();
    /** Spell type choices already used by permissions allowing one spell of each card type. */
    public final Map<UUID, List<Set<CardType>>> exileSpellTypePermissionHistory = new ConcurrentHashMap<>();
    /** Card UUIDs the controller may play from their outside-the-game card pool this turn. */
    public final OutsideGamePlayPermissionSet outsideGamePlayPermissions = new OutsideGamePlayPermissionSet();
    /** One-shot permission for the additional modal mode granted by Your Wish Is My Command. */
    public final Set<UUID> outsideGameAdditionalModalModePermissions = ConcurrentHashMap.newKeySet();
    /** Optional condition that must remain true for an exiled card's play permission to be active. */
    public final Map<UUID, Condition> exilePlayPermissionConditions = new ConcurrentHashMap<>();
    /** Restricts the characteristics of a spell cast with a direct exile permission. */
    public final Map<UUID, CardPredicate> exilePlayPermissionSpellFilters = new ConcurrentHashMap<>();
    /** Exiled cards whose spells may be cast by paying life equal to mana value this turn. */
    public final Set<UUID> exilePlayForLifeEqualToManaValue = ConcurrentHashMap.newKeySet();
    public final Set<UUID> plottedCardIds = ConcurrentHashMap.newKeySet();
    /** Maps a source permanent to the latest card whose permission it granted. */
    public final Map<UUID, UUID> exilePlayPermissionSourceCards = new ConcurrentHashMap<>();
    /** Source permanents whose "until end of turn, you may play cards exiled with it" permission is active
     *  (Urianger Augurelt), mapped to the permission holder and the generic cost reduction (stored as a negative
     *  amount). Cards exiled with the source later that turn receive the permission too (CR 611.2c). Cleared during cleanup. */
    public final Map<UUID, ExilePlayCostModifier> exilePlayUntilEndOfTurnGrantsBySource = new ConcurrentHashMap<>();
    /** Maps a card with a source-controlled exile-play permission to its source permanent. */
    public final Map<UUID, UUID> exilePlayPermissionSourcePermanents = new ConcurrentHashMap<>();
    /** Maps a card whose play permission comes from a static ability of a source permanent to that permanent.
     *  The permission lasts only while the source keeps the ability (Uba Mask), not while its controller changes. */
    public final Map<UUID, UUID> exilePlayPermissionStaticSourcePermanents = new ConcurrentHashMap<>();
    /** Maps a card exiled by a Saga's chapter to that Saga: the exiling player may play it during any turn
     *  they put a lore counter on the Saga (Summon: Brynhildr). The permission is renewed each such turn. */
    public final Map<UUID, UUID> exilePlayPermissionLoreCounterSagas = new ConcurrentHashMap<>();
    /** Cost modifiers attached to cards that may be played from exile. */
    public final Map<UUID, ExilePlayCostModifier> exilePlayCostModifiers = new ConcurrentHashMap<>();
    /** Card UUIDs whose exile-play permission expires at end of turn (impulse draw, e.g. Vance's Blasting Cannons).
     *  Cleared during cleanup step — matching entries are also removed from {@link #exilePlayPermissions}. */
    public final Set<UUID> exilePlayPermissionsExpireEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Card UUIDs whose exile-play permission expires at end of the turn number stored as the value
     *  (e.g. Archaic's Agony: until end of your next turn). */
    public final Map<UUID, Integer> exilePlayPermissionsExpireAtTurnEnd = new ConcurrentHashMap<>();
    /** Permissions expiring as the designated turn's end step begins rather than at cleanup. */
    public final Set<UUID> exilePlayPermissionsExpireAtEndStep = ConcurrentHashMap.newKeySet();
    /** Permissions waiting for the named player's next actual turn before their expiry is fixed. */
    public final Map<UUID, UUID> exilePlayPermissionsAwaitNextTurnOfPlayer = new ConcurrentHashMap<>();
    /** Permissions from "during your next turn" wording, unusable until the owner's next turn begins. */
    public final Set<UUID> exilePlayPermissionsDuringOwnersNextTurnOnly = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> exilePlayPermissionsExpireAtTurnBeginning = new ConcurrentHashMap<>();
    /** Exiled card UUIDs that may be cast spending mana of any type (e.g. Nita, Forum Conciliator's
     *  activated ability). Complements the battlefield-permanent any-mana grant used by Hostage Taker.
     *  Current-turn grants are cleared during cleanup; grants tied to a later play-permission
     *  expiry remain until that permission expires. */
    public final Set<UUID> exilePlayAnyManaType = ConcurrentHashMap.newKeySet();
    /** Exiled card UUIDs that may be cast spending mana of any type for as long as they remain exiled. */
    public final Set<UUID> exilePlayAnyManaTypeWhileExiled = ConcurrentHashMap.newKeySet();
    /** Maps a Discord-created exiled copy to the Discord permanent whose ability created it. */
    public final Map<UUID, UUID> discordCopySourcePermanents = new ConcurrentHashMap<>();
    /** Exiled card UUIDs that have stash counters. */
    public final Set<UUID> stashCounterCardIds = ConcurrentHashMap.newKeySet();
    /** Card UUIDs that may be played from exile without paying their mana cost (e.g. Oracle's Vault's
     *  second ability). Complements {@link #exilePlayPermissions} — the card must also hold a play
     *  permission. Temporary entries are listed in {@link #exilePlayPermissionsExpireEndOfTurn};
     *  entries without an expiry remain valid for as long as the card remains exiled. */
    public final Set<UUID> exilePlayWithoutPayingManaCost = ConcurrentHashMap.newKeySet();
    /** Exiled card UUIDs whose permission makes a land played from exile enter tapped. */
    public final Set<UUID> exileCardsEnterTapped = ConcurrentHashMap.newKeySet();
    /** Card UUIDs that are exiled instead of being put into a graveyard (e.g. a spell cast via
     *  Nita, Forum Conciliator: "If that spell would be put into a graveyard, exile it instead").
     *  Cleared during cleanup step. */
    public final Set<UUID> exileInsteadOfGraveyard = ConcurrentHashMap.newKeySet();
    /** Maps graveyard card UUID → player UUID who may play it this turn (e.g. Ark of Hunger).
     *  Cleared during cleanup step for entries in {@link #graveyardPlayPermissionsExpireEndOfTurn}. */
    public final Map<UUID, UUID> graveyardPlayPermissions = new ConcurrentHashMap<>();
    /** Graveyard card UUIDs whose play permission expires at end of turn. */
    public final Set<UUID> graveyardPlayPermissionsExpireEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Graveyard card UUIDs granted a play permission that makes the permanent enter tapped. */
    public final Set<UUID> graveyardCardsEnterTapped = ConcurrentHashMap.newKeySet();
    /** Turn-scoped blanket "you may cast [filtered] spells from your graveyard this turn" grants
     *  (Liliana, Untouched by Death's −3). Unlike {@link #graveyardPlayPermissions} these are not
     *  keyed by card, so they also cover cards that reach the graveyard later in the turn.
     *  Cleared at turn cleanup. */
    public final List<GraveyardCastFilterPermission> graveyardCastFilterPermissionsThisTurn =
            new CopyOnWriteArrayList<>();
    /** Source-linked exile cast grants that expire at end of turn. */
    public final List<ExileCastPermission> exileCastPermissionsUntilEndOfTurn =
            new CopyOnWriteArrayList<>();
    /** Source-linked exile cast grants that also cover cards exiled with the source later this turn. */
    public final List<ExileCastWildcardPermission> exileCastWildcardPermissionsUntilEndOfTurn =
            new CopyOnWriteArrayList<>();
    /** Turn-scoped grants to cast one foretold card owned by the granting player for free. */
    public final List<ForetoldCardCastPermission> foretoldCardCastPermissionsThisTurn =
            new CopyOnWriteArrayList<>();
    /** Players whose cards are exiled instead of entering their graveyards for the rest of the turn. */
    public final Set<UUID> playersExilingCardsInsteadOfGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who may play face-up cards they own from exile until end of turn. */
    public final Set<UUID> playersMayPlayFaceUpCardsFromExileThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose cards go to the bottom of their owner's library instead of graveyard or exile this turn. */
    public final Set<UUID> playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn =
            ConcurrentHashMap.newKeySet();
    /** Players who can't play lands or cast spells from graveyards this turn. */
    public final Set<UUID> playersCantPlayFromGraveyardsThisTurn = ConcurrentHashMap.newKeySet();
    /** Turn-scoped blanket permissions to play matching cards from graveyards. */
    public final List<GraveyardPlayFilterPermission> graveyardPlayFilterPermissionsThisTurn =
            new CopyOnWriteArrayList<>();
    /** Depth counter for batching "cards leave graveyard" triggers (one trigger per batch). */
    public int graveyardLeaveNotificationDepth = 0;
    /** Owners whose graveyards had cards leave during a suppressed batch; triggers fire when depth returns to 0. */
    public final Set<UUID> graveyardLeaveNotificationPendingOwners = ConcurrentHashMap.newKeySet();
    /** Cards captured for the corresponding general graveyard-leave batch. */
    public final Map<UUID, List<Card>> graveyardLeaveNotificationPendingCards = new ConcurrentHashMap<>();
    /** Instant or sorcery cards leaving each owner's graveyard during a suppressed batch. */
    public final Map<UUID, List<Card>> graveyardLeaveNotificationPendingInstantOrSorceryCards =
            new ConcurrentHashMap<>();
    /** Owners whose graveyards had creature cards leave during a suppressed batch. */
    public final Set<UUID> graveyardLeaveNotificationPendingCreatureOwners = ConcurrentHashMap.newKeySet();
    /** Counts creature cards leaving each owner's graveyard during a suppressed batch. */
    public final Map<UUID, Integer> graveyardLeaveNotificationPendingCreatureCardCounts = new ConcurrentHashMap<>();
    /** Creature cards that left each owner's graveyard during a suppressed batch; these become new objects
     *  and so never see the event that moved them. */
    public final Map<UUID, List<Card>> graveyardLeaveNotificationPendingCreatureCards = new ConcurrentHashMap<>();
    /** Owners whose graveyards had artifact or creature cards leave during a suppressed batch. */
    public final Set<UUID> graveyardLeaveNotificationPendingArtifactOrCreatureOwners = ConcurrentHashMap.newKeySet();
    /** Owners whose graveyards had artifact cards leave during a suppressed batch. */
    public final Set<UUID> graveyardLeaveNotificationPendingArtifactOwners = ConcurrentHashMap.newKeySet();
    /** Artifact and creature cards captured for the corresponding suppressed leave batch. */
    public final Map<UUID, List<Card>> graveyardLeaveNotificationPendingArtifactOrCreatureCards =
            new ConcurrentHashMap<>();
    /** Number of cards exiled from each owner's graveyard during a suppressed batch. */
    public final Map<UUID, Integer> graveyardExileNotificationPendingCounts = new ConcurrentHashMap<>();
    /** Creature cards captured for Kaya's exile trigger during a suppressed batch. */
    public final Map<UUID, List<Card>> kayaExileNotificationPendingCreatureCards = new ConcurrentHashMap<>();
    /** Creature-exile events captured for Kaya during a suppressed batch. */
    public final Map<UUID, Integer> kayaExileNotificationPendingCounts = new ConcurrentHashMap<>();
    /** Whether a suppressed batch exiled one or more cards from a graveyard or the battlefield. */
    public boolean graveyardOrBattlefieldExileNotificationPending;
    /** Depth counter for tap events that are paid or performed as one batch. */
    public int permanentTapTriggerBatchDepth;
    /** Tap-trigger registrations that have already fired in the current tap batch, by source permanent. */
    public final Map<UUID, Set<CardEffect>> permanentTapTriggerBatchFiredEffects = new ConcurrentHashMap<>();
    /** Players who had one or more cards leave their graveyard this turn (cleared at turn cleanup). Used by Wilt in the Heat cost reduction. */
    public final Set<UUID> playersWhoseCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Players whose graveyards had one or more creature cards leave this turn. */
    public final Set<UUID> playersWhoseCreatureCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Number of cards that left each player's graveyard this turn. */
    public final Map<UUID, Integer> cardsLeftGraveyardCountThisTurn = new ConcurrentHashMap<>();
    /** Depth counter for batching non-dying battlefield departures into one trigger event. */
    public int permanentLeaveNotificationDepth;
    /** Last-known permanents present when a non-dying battlefield-leave batch began. */
    public final Map<UUID, Permanent> permanentLeaveBatchWatchers = new ConcurrentHashMap<>();
    /** Controllers of the last-known permanents in a non-dying battlefield-leave batch. */
    public final Map<UUID, UUID> permanentLeaveBatchWatcherControllers = new ConcurrentHashMap<>();
    /** Creature permanents that left during a non-dying battlefield-leave batch. */
    public final Map<UUID, UUID> permanentLeaveBatchPendingCreatures = new ConcurrentHashMap<>();
    /** Permanents that left during a battlefield-leave batch, keyed by permanent and controller. */
    public final Map<UUID, UUID> permanentLeaveBatchPendingPermanents = new ConcurrentHashMap<>();
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
     * {@code true} while the resolving effect's handler has not flipped a coin yet and has changed nothing, so a
     * coin flip Krark's Thumb replaces can stop it while the flipping player chooses which results to ignore. The
     * handler's first flip consumes it.
     */
    public boolean coinFlipChoicesCanRerunEffect;
    /**
     * The flips of the coin-flip event that stopped the resolving effect, in order, while their player chooses
     * which Krark's Thumb results to keep; the effect resolved again reads them back instead of flipping.
     */
    public final List<RecordedCoinFlip> recordedCoinFlips = new ArrayList<>();
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
                    PileChoiceReplay, ActivationCostReplay, SacrificeAbilityReplay, SpellCastReplay,
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
     * A "sacrifice this permanent" ability (an {@code ON_SACRIFICE} ability) stopped before its sacrifice so that its
     * death replacement could be chosen first (CR 616.1): who activated it, the permanent and the ability's target.
     */
    public record SacrificeAbilityReplay(Player player, UUID permanentId, UUID targetId)
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
            implements DeathReplacementChoiceReplay {
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
            implements DeathReplacementChoiceReplay {
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

    /** Revealed hand cards waiting to be stamped onto the spell whose additional cost they paid. */
    public final Map<UUID, Card> pendingRevealedPowerCostCards = new ConcurrentHashMap<>();


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
    public final Set<UUID> cardsGrantedFlashbackWithoutPayingManaCostUntilEndOfTurn =
            ConcurrentHashMap.newKeySet();

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

    /** Tracks which source permanents or graveyard cards (by UUID) have already fired a
     *  {@code OncePerTurnTriggerEffect} this turn (e.g. Ghoulish Process). Cleared at start of
     *  new turn; graveyard-card entries are removed when those cards leave the graveyard. */
    public final Set<UUID> oncePerTurnTriggersFiredThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks every permanent that has had counters put on it this turn, regardless of which
     *  abilities it had at the time. Used for "for the first time each turn" counter triggers that
     *  may be granted after an earlier placement. Cleared at start of new turn. */
    public final Set<UUID> permanentsWithCountersPutThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which controller has used each permanent's first-card-cycled-free permission this turn. */
    public final Map<UUID, Set<UUID>> firstCardCycledFreeUsesThisTurn = new ConcurrentHashMap<>();

    /** Tracks keyed once-per-turn trigger uses independently for each source permanent. */
    public final Map<UUID, Set<String>> keyedOncePerTurnTriggersFiredThisTurn = new ConcurrentHashMap<>();
    /** Tracks source permanent IDs to opponents whose first life loss during their turn has fired. */
    public final Map<UUID, Set<UUID>> firstOpponentLifeLossTriggersFiredThisTurn = new ConcurrentHashMap<>();
    /** Tracks source permanent object IDs whose Survival ability has been evaluated. A new object
     *  created when the card leaves and returns can evaluate again. */
    public final Set<UUID> survivalTriggersEvaluated = ConcurrentHashMap.newKeySet();

    /** Tracks source permanent object IDs whose one-shot triggered ability has fired. */
    public final Set<UUID> onceOnlyTriggersFired = ConcurrentHashMap.newKeySet();

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
    /**
     * Players whose Soul Echo replacement ("for each 1 damage that would be dealt to you until your next
     * upkeep, you remove an echo counter instead") was chosen. The effect is independent of its source, so it
     * keeps replacing damage with nothing after Soul Echo has left the battlefield; it ends at that player's
     * next upkeep.
     */
    public final Set<UUID> soulEchoReplacementPlayers = ConcurrentHashMap.newKeySet();
    /** Token creation event paused for a once-per-turn replacement choice. */
    public PendingTokenCreationReplacement pendingTokenCreationReplacement;

    /** Tracks which permanents (by UUID) have already fired a {@code OncePerTurnTriggerEffect} in the
     *  {@code ON_ATTACK} slot this turn — "attacks for the first time each turn" (Aurelia, the
     *  Warleader). Kept separate from {@link #oncePerTurnTriggersFiredThisTurn} so a permanent with
     *  both a once-each-turn death watcher and a once-each-turn attack trigger doesn't gate one on the
     *  other. Cleared at start of new turn. */
    public final Set<UUID> onceEachTurnAttackTriggersFiredThisTurn = ConcurrentHashMap.newKeySet();

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
    /**
     * Cards chosen as the damage source of a waiting redirect or next-damage prevention shield, keyed by
     * the permanent id the shield stores, so the source can be chosen again after it has left the
     * battlefield (CR 609.7a). Cleared at cleanup.
     */
    public final Map<UUID, Card> waitingRedirectDamageSourceCards = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> simultaneousDyingPermanentControllers = new ConcurrentHashMap<>();
    /** Last-known effective powers of creatures in the current simultaneous death event. */
    public final Map<UUID, Integer> simultaneousDyingPowers = new ConcurrentHashMap<>();
    /** Last-known persist / undying instance counts of creatures in the current simultaneous death event. */
    public final Map<UUID, Map<Keyword, Integer>> simultaneousDyingTriggerKeywordInstances =
            new ConcurrentHashMap<>();
    /** Last-known continuously granted ON_ANY_CREATURE_DIES effects for the current death event. */
    public final Map<UUID, List<CardEffect>> simultaneousDyingGrantedCreatureDeathEffects =
            new ConcurrentHashMap<>();
    /** Continuously granted ON_DEATH abilities before any permanent leaves a simultaneous death event. */
    public final Map<UUID, List<CardEffect>> simultaneousDyingGrantedSelfDeathEffects =
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

    // Mindslaver — turn control
    /** Delayed effect: targetPlayerId -> controllerId, consumed when target player's turn begins. */
    public final Map<UUID, UUID> pendingTurnControl = new ConcurrentHashMap<>();
    /**
     * Players whose pending turn-control effect also grants them an extra turn after the controlled
     * turn (Emrakul, the Promised End). Consumed with {@link #pendingTurnControl} when that turn begins.
     */
    public final List<UUID> pendingTurnControlExtraTurn = new CopyOnWriteArrayList<>();
    /** Delayed effect: targetPlayerId -> controllerId, consumed when target player's next combat begins. */
    public final Map<UUID, UUID> pendingCombatControl = new ConcurrentHashMap<>();
    /** Non-null when a player is being controlled this turn (the controlled player's ID). */
    public UUID mindControlledPlayerId;
    /** Non-null when a player is being controlled this turn (the controlling player's ID). */
    public UUID mindControllerPlayerId;
    /** Whether the active mind control ends when the current combat phase ends. */
    public boolean mindControlUntilEndOfCombat;

    /** Word of Command's short-lived player-control window and its parked resolution. */
    public UUID wordOfCommandControllerPlayerId;
    public UUID wordOfCommandControlledPlayerId;
    public UUID wordOfCommandCardId;
    public StackEntry wordOfCommandPendingResolutionEntry;
    public int wordOfCommandPendingResolutionIndex;
    public boolean wordOfCommandCastingCard;
    public boolean wordOfCommandAwaitingCardResolution;

    // Taunt — "creatures that player controls attack you if able" during their next turn
    /** Delayed effect: affectedPlayerId -> controllerId to attack, consumed when the affected player's turn begins. */
    public final Map<UUID, UUID> tauntedNextTurn = new ConcurrentHashMap<>();
    /** Active this turn: affectedPlayerId -> controllerId all their creatures must attack if able. */
    public final Map<UUID, UUID> tauntedThisTurn = new ConcurrentHashMap<>();

    /**
     * Delayed single-creature taunt: creaturePermanentId -&gt; the permanent it must attack, consumed
     * when the creature's controller's next turn begins (Gideon, Battle-Forged's +2). Unlike
     * {@link #tauntedNextTurn} this binds one creature rather than every creature a player controls,
     * and it is promoted onto the creature's own transient {@code mustAttackThisTurn} /
     * {@code mustAttackTargetId} pair.
     */
    public final Map<UUID, UUID> creatureMustAttackPermanentNextTurn = new ConcurrentHashMap<>();

    // Oracle en-Vec — "the chosen creatures attack if able, and other creatures can't attack"
    /** Delayed: affectedPlayerId -> the creature IDs that player chose, consumed when their turn begins. */
    public final Map<UUID, Set<UUID>> chosenAttackersNextTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<DestroyChosenCreaturesIfDidNotAttackAtEndStep>> chosenAttackersToDestroyNextTurn =
            new ConcurrentHashMap<>();

    public void restrictAttackersNextTurn(UUID playerId, Set<UUID> chosen, UUID controllerId, Card sourceCard) {
        chosenAttackersNextTurn.merge(playerId, Set.copyOf(chosen), (previous, current) -> {
            Set<UUID> allowed = new java.util.HashSet<>(previous);
            allowed.retainAll(current);
            return Set.copyOf(allowed);
        });
        if (!chosen.isEmpty()) {
            chosenAttackersToDestroyNextTurn.computeIfAbsent(playerId, ignored -> new ArrayList<>())
                    .add(new DestroyChosenCreaturesIfDidNotAttackAtEndStep(List.copyOf(chosen), controllerId, sourceCard));
        }
    }
    /** Active this turn: affectedPlayerId -> the only creatures allowed to attack (all others can't). */
    public final Map<UUID, Set<UUID>> chosenAttackersThisTurn = new ConcurrentHashMap<>();

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
    public final List<UUID> otherCreaturesCantAttackExemptCreatureIds =
            Collections.synchronizedList(new ArrayList<>());

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

    /** Controller of the triggered ability currently resolving, or {@code null} otherwise. */
    public UUID currentlyResolvingTriggeredAbilityControllerId;

    /** Damage assignments provided at cast time for an ETB divided-damage effect (e.g. Kuldotha Flamefiend). */
    public Map<UUID, Integer> pendingETBDamageAssignments = Map.of();

    // Combat damage assignment state
    public final Map<Integer, Map<UUID, Integer>> combatDamagePlayerAssignments = new HashMap<>();
    /** Damage source, prevention source, recipient, and the allocated prevention amount. */
    public final Map<UUID, Map<UUID, Map<UUID, Integer>>> combatSharedDamagePreventionAllocations = new HashMap<>();
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
        }
    }

    private static boolean preservedDuringTurnRestart(String fieldName) {
        return switch (fieldName) {
            case "session", "subgameRequested", "waitingForSubgame", "pendingAncestorTransfers",
                    "subgameCards", "suspendedZoneTriggers", "turnStartSnapshot", "gameLog", "playersWhoLostGameThisMatch", "matchCreatureTypeErrata", "timestampCounter",
                    "nextExtraTurnSequence", "graveyardEntryVersion",
                    "domainActionSequence", "domainEventSequence", "domainStateVersion",
                    "currentlyResolvingControllerId", "pendingEffectResolutionEntry",
                    "pendingEffectResolutionIndex", "resolvingMayEffectFromStack",
                    "resolvedMayAccepted", "resolvedMayTargetingEntry",
                    "rerunCurrentEffectAfterInteraction", "deathReplacementsCanRerunEffect", "stepDepartureEffect",
                    "coinFlipChoicesCanRerunEffect", "recordedCoinFlips",
                    "chosenDeathReplacementsForRerun", "rerunMayLibraryChoices", "pendingDeathReplacementChoiceReplay",
                    "deferPlayerLossCheck",
                    "effectResolutionDepth", "restartTurnRequested", "simulation",
                    "alwaysOfferPriorityWindows" -> true;
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
                || value.getClass().getPackageName().startsWith("java.");
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
        return version;
    }

    /**
     * Ends a card's continuous stay in a graveyard. The card is a new object once it has left, so
     * anything remembering the old stay's identity no longer refers to it.
     */
    public synchronized void markGraveyardExit(UUID cardId) {
        graveyardEntryVersions.put(cardId, ++graveyardEntryVersion);
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
     *  engine (see {@code agent-docs/LAYER_SYSTEM.md}). Stamped via {@link #addFloatingEffect}
     *  and expired by duration: {@code UNTIL_END_OF_TURN} at the cleanup step,
     *  {@code UNTIL_END_OF_COMBAT} when combat state is cleared,
     *  {@code WHILE_SOURCE_ON_BATTLEFIELD}/{@code WHILE_ATTACHED} when the source permanent
     *  leaves the battlefield or becomes unattached, {@code UNTIL_YOUR_NEXT_TURN} at the start
     *  of the controller's next turn; next-turn cleanup and next-upkeep durations expire
     *  at their respective cleanup step and end of upkeep. */
    public final List<FloatingContinuousEffect> floatingEffects = Collections.synchronizedList(new ArrayList<>());

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

    private transient volatile Consumer<GameData> cardsExiledListener;
    private transient volatile Consumer<GameData> opponentOwnedCardExiledListener;

    public void setCardsExiledListener(Consumer<GameData> cardsExiledListener) {
        this.cardsExiledListener = cardsExiledListener;
    }

    public void setOpponentOwnedCardExiledListener(Consumer<GameData> opponentOwnedCardExiledListener) {
        this.opponentOwnedCardExiledListener = opponentOwnedCardExiledListener;
    }

    private transient int cardExileBatchDepth;
    private transient boolean cardExileBatchPending;

    /**
     * Runs {@code exiling} as one batch of card exiles, so that "one or more cards are put into
     * exile" triggers fire once for the whole batch rather than once per card. Per-card bookkeeping
     * (exile versions, turn counters) still happens for every card.
     */
    public void runAsSingleCardExileBatch(Runnable exiling) {
        cardExileBatchDepth++;
        try {
            exiling.run();
        } finally {
            cardExileBatchDepth--;
        }
        if (cardExileBatchDepth == 0 && cardExileBatchPending) {
            cardExileBatchPending = false;
            fireCardsExiledListeners();
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
                .filter(permanent -> permanent.getCard().getEffects(EffectSlot.ON_ANY_CARD_EXILED).stream()
                        .anyMatch(CardEffect::notesManaValueOfExiledCards))
                .forEach(permanent -> greatestManaValueNotedForPermanentThisTurn.merge(
                        permanent.getId(), manaValue, Math::max));
        cardsExiledThisTurn++;
        cardPutIntoExileThisTurn = true;
        if (cardExileBatchDepth > 0) {
            cardExileBatchPending = true;
            return;
        }
        fireCardsExiledListeners();
    }

    private void fireCardsExiledListeners() {
        Consumer<GameData> listener = cardsExiledListener;
        if (listener != null) {
            listener.accept(this);
        }
        Consumer<GameData> opponentListener = opponentOwnedCardExiledListener;
        if (opponentListener != null) {
            opponentListener.accept(this);
        }
    }

    /**
     * Stamps the given floating effect with the next CR 613.7 timestamp, stores it, and returns
     * the stamped instance (the passed-in effect's own timestamp is ignored).
     */
    public FloatingContinuousEffect addFloatingEffect(FloatingContinuousEffect effect) {
        FloatingContinuousEffect stamped = effect.withTimestamp(nextTimestamp());
        floatingEffects.add(stamped);
        if (stamped.id() != null && activePlayerId != null) {
            floatingEffectCreatingActivePlayerIds.put(stamped.id(), activePlayerId);
        }
        return stamped;
    }

    /**
     * Returns {@code true} if the permanent with the given id is prevented from dealing damage,
     * whether via the turn-scoped {@link #permanentsPreventedFromDealingDamage} set (Soul Parry)
     * or the until-your-next-turn {@link #permanentsPreventedFromDealingDamageUntilNextTurn} map
     * (Gideon of the Trials +1).
     */
    public boolean isPreventedFromDealingDamage(UUID permanentId) {
        return permanentsPreventedFromDealingDamage.contains(permanentId)
                || permanentsPreventedFromDealingDamageUntilNextTurn.containsKey(permanentId);
    }

    /** Returns whether all damage to the permanent is prevented until a player's next turn. */
    public boolean isProtectedFromDamageUntilNextTurn(UUID permanentId) {
        return permanentsProtectedFromDamageUntilNextTurn.containsKey(permanentId);
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

    /** Removes and returns all floating effects with end-of-turn duration (cleanup step). */
    public List<FloatingContinuousEffect> expireEndOfTurnFloatingEffects() {
        return expireFloatingEffects(this::endsAtCleanup);
    }

    /**
     * Removes and returns the floating effects with end-of-turn duration that were created while
     * the given player was the active player, plus any whose creating active player is unknown.
     * Used at a cleanup step under Runed Terror's sequential phases, where such an effect ends at
     * the cleanup step of the player who was active when it began (Runed Terror ruling, CR 514.2).
     */
    public List<FloatingContinuousEffect> expireEndOfTurnFloatingEffectsCreatedDuring(UUID playerId) {
        return expireFloatingEffects(fe -> endsAtCleanup(fe)
                && (fe.id() == null || playerId.equals(
                floatingEffectCreatingActivePlayerIds.getOrDefault(fe.id(), playerId))));
    }

    private boolean endsAtCleanup(FloatingContinuousEffect fe) {
        return fe.duration() == EffectDuration.UNTIL_END_OF_TURN
                || (fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST
                && !(fe.effect() instanceof ReduceCastCostForNextMatchingSpellEffect reduction
                && !reduction.expiresAtCleanup()));
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
     * ability this turn" copy triggers with their sources, and Sen Triplets' hand permission), which the cleanup step clears
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
            case GAME_DAMAGE_PREVENTION_SHIELD -> globalDamagePreventionShield;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE -> preventAllCombatDamage;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE_BY_ATTACKING_CREATURES -> preventAllCombatDamageByAttackingCreatures;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE_TO_PLAYERS -> preventAllCombatDamageToPlayers;
            case GAME_PREVENT_ALL_DAMAGE_TO_ALL_CREATURES -> preventAllDamageToAllCreatures;
            case GAME_PREVENT_ALL_DAMAGE_BY_CREATURES -> preventAllDamageByCreatures;
            case GAME_DAMAGE_BY_CREATURES_PREVENTION_LIFE_GAIN_PLAYERS -> {
                synchronized (damageByCreaturesPreventionLifeGainPlayers) {
                    yield new ArrayList<>(damageByCreaturesPreventionLifeGainPlayers);
                }
            }
            case GAME_PREVENT_ALL_DAMAGE_FROM_NON_HUMAN_SOURCES -> preventAllDamageFromNonHumanSources;
            case GAME_COMBAT_DAMAGE_EXEMPT_PREDICATES_BY_CONTROLLER -> mapEntries(combatDamageExemptPredicatesByController);
            case GAME_PREVENT_DAMAGE_FROM_COLORS -> preventDamageFromColors;
            case GAME_COLOR_DAMAGE_PREVENTION -> mapEntries(colorDamagePreventionUntilEndOfTurn);
            case GAME_COLOR_DAMAGE_PREVENTION_FOR_CONTROLLED_CREATURES ->
                    mapEntries(colorDamagePreventionForControlledCreaturesUntilEndOfTurn);
            case GAME_COLORLESS_DAMAGE_PREVENTION -> colorlessDamagePreventionUntilEndOfTurn;
            case GAME_PERMANENTS_PREVENTED_FROM_DEALING_DAMAGE -> permanentsPreventedFromDealingDamage;
            case GAME_CREATURES_PROTECTED_FROM_TARGETING_DAMAGE -> creaturesProtectedFromTargetingDamage;
            case GAME_PLAYERS_WITH_ALL_DAMAGE_PREVENTED -> playersWithAllDamagePrevented;
            case GAME_PLAYERS_WITH_ALL_PERMANENT_DAMAGE_PREVENTED -> playersWithAllPermanentDamagePrevented;
            case GAME_PLAYERS_WITH_ALL_CREATURE_DAMAGE_PREVENTED -> playersWithAllCreatureDamagePrevented;
            case GAME_PLAYERS_WITH_ALL_PLAYER_DAMAGE_PREVENTED -> playersWithAllPlayerDamagePrevented;
            case GAME_PLAYERS_WITH_ALL_COMBAT_DAMAGE_TO_PLAYER_AND_PLANESWALKERS_PREVENTED ->
                    playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented;
            case GAME_PLAYERS_WITH_DAMAGE_FROM_ATTACKERS_PREVENTED -> playersWithDamageFromAttackersPrevented;
            case GAME_PLAYERS_WITH_DAMAGE_FROM_OPPONENT_CREATURES_PREVENTED -> playersWithDamageFromOpponentCreaturesPrevented;
            case GAME_PLAYERS_WITH_COMBAT_DAMAGE_FROM_TARGET_OPPONENT_CREATURES_PREVENTED ->
                    playersWithCombatDamageFromTargetOpponentCreaturesPrevented;
            case GAME_PLAYERS_WITH_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED ->
                    mapEntries(playersWithDamageFromMatchingSourcesPrevented);
            case GAME_PLAYERS_WITH_DAMAGE_TO_CONTROLLED_CREATURES_FROM_MATCHING_SOURCES_PREVENTED ->
                    mapEntries(playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented);
            case GAME_CREATURES_WITH_ALL_DAMAGE_PREVENTED -> creaturesWithAllDamagePrevented;
            case GAME_PLAYERS_REDIRECTING_ALL_CREATURE_DAMAGE -> playersRedirectingAllCreatureDamage;
            case GAME_ALL_DAMAGE_PREVENTION_PREDICATES -> allDamagePreventionPredicates;
            case GAME_ALL_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER -> mapEntries(allDamagePreventionPredicatesByController);
            case GAME_COMBAT_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER ->
                    mapEntries(combatDamagePreventionPredicatesByController);
            case GAME_CREATURES_WITH_COMBAT_DAMAGE_PREVENTED -> creaturesWithCombatDamagePrevented;
            case GAME_CREATURES_PREVENTED_FROM_DEALING_COMBAT_DAMAGE -> creaturesPreventedFromDealingCombatDamage;
            case GAME_CREATURES_ASSIGNING_NO_COMBAT_DAMAGE -> creaturesAssigningNoCombatDamageThisTurn;
            case GAME_DAMAGE_CANT_BE_PREVENTED -> damageCantBePreventedThisTurn;
            case GAME_PLAYERS_CANT_GAIN_LIFE -> playersCantGainLifeThisTurn;
            case GAME_PLAYERS_CANT_LOSE_LIFE -> playersCantLoseLifeThisTurn;
            case GAME_PLAYERS_CANT_SEARCH_LIBRARIES -> playersCantSearchLibrariesThisTurn;
            case GAME_CREATURES_CANT_ATTACK -> creaturesCantAttackThisTurn;
            case GAME_PLAYERS_WHO_CANT_GAIN_LIFE -> playersWhoCantGainLifeThisTurn;
            case GAME_PLAYERS_WHO_GAIN_NO_LIFE -> playersWhoGainNoLifeThisTurn;
            case GAME_PLAYERS_WHO_CANT_LOSE_LIFE -> playersWhoCantLoseLifeThisTurn;
            case GAME_PLAYERS_WITH_HEXPROOF -> playersWithHexproofThisTurn;
            case GAME_PLAYERS_WITH_SHROUD -> playersWithShroudThisTurn;
            case GAME_PLAYER_HEXPROOF_FROM_COLORS -> mapEntries(playerHexproofFromColorsThisTurn);
            case GAME_PERMANENT_HEXPROOF_FROM_COLORS -> mapEntries(permanentHexproofFromColorsThisTurn);
            case GAME_PLAYER_PROTECTION_FROM_COLORS -> mapEntries(playerProtectionFromColorsUntilEndOfTurn);
            case GAME_PLAYER_PROTECTION_FROM_PLAYERS -> mapEntries(playerProtectionFromPlayerIdsUntilEndOfTurn);
            case GAME_PLAYER_KEYWORDS -> mapEntries(playerKeywordsUntilEndOfTurn);
            case GAME_PLAYER_CREATURES_CANT_BE_TARGETED_BY_COLORS -> mapEntries(playerCreaturesCantBeTargetedByColorsThisTurn);
            case GAME_PLAYER_SPELLS_CANT_BE_COUNTERED_BY_COLORS -> mapEntries(playerSpellsCantBeCounteredByColorsThisTurn);
            case GAME_PLAYERS_SPELLS_CANT_BE_COUNTERED -> playersSpellsCantBeCounteredThisTurn;
            case GAME_PLAYERS_CREATURE_SPELLS_CANT_BE_COUNTERED -> playersCreatureSpellsCantBeCounteredThisTurn;
            case GAME_PLAYERS_SILENCED -> playersSilencedThisTurn;
            case GAME_PLAYERS_CANT_PLAY_LANDS -> playersCantPlayLandsThisTurn;
            case GAME_PLAYERS_CANT_PLAY_FROM_GRAVEYARDS -> playersCantPlayFromGraveyardsThisTurn;
            case GAME_PLAYERS_CANT_CAST_SPELL_TYPES -> mapEntries(playersCantCastSpellTypesThisTurn);
            case GAME_PLAYERS_CANT_CAST_NONCREATURE_SPELLS -> playersCantCastNoncreatureSpellsThisTurn;
            case GAME_PLAYERS_CANT_ACTIVATE_ABILITIES -> playersCantActivateAbilitiesThisTurn;
            case GAME_PLAYERS_CANT_ACTIVATE_NON_MANA_ABILITIES -> playersCantActivateNonManaAbilitiesThisTurn;
            case GAME_PLAYERS_CANT_ACTIVATE_LOYALTY_ABILITIES -> playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn;
            case GAME_PLAYER_CREATURES_CANT_ATTACK -> listMapEntries(playerCreaturesCantAttackThisTurn);
            case GAME_PLAYERS_GATHERING_SPECIMENS -> new ArrayList<>(playersGatheringSpecimensThisTurn);
            case GAME_PLAYERS_GATHERING_TOKENS -> new ArrayList<>(playersGatheringTokensThisTurn);
            case GAME_PLAYERS_EXILING_UNCAST_ENTERING_CREATURES -> playersExilingUncastEnteringCreaturesThisTurn;
            case GAME_PLAYERS_EXILING_UNCAST_ENTERING_NONTOKEN_CREATURES ->
                    playersExilingUncastEnteringNontokenCreaturesThisTurn;
            case GAME_PLAYERS_SHUFFLING_CREATURES_ENTERING_FROM_EXILE -> playersShufflingCreaturesEnteringFromExileThisTurn;
            case GAME_PLAYERS_EXILING_CREATURES_INSTEAD_OF_DYING -> playersExilingCreaturesInsteadOfDyingThisTurn;
            case GAME_PLAYERS_EXILING_OPPONENT_CREATURES_INSTEAD_OF_DYING ->
                    playersExilingOpponentCreaturesInsteadOfDyingThisTurn;
            case GAME_PLAYERS_EXILING_CONTROLLED_PERMANENTS_INSTEAD_OF_DYING ->
                    playersExilingControlledPermanentsInsteadOfDyingThisTurn;
            case GAME_PLAYERS_EXILING_CARDS_INSTEAD_OF_GRAVEYARD -> playersExilingCardsInsteadOfGraveyardThisTurn;
            case GAME_PLAYERS_PUTTING_CARDS_ON_BOTTOM_OF_LIBRARY ->
                    playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn;
            case GAME_PLAYERS_WITH_FLASH -> playersWithFlashUntilEndOfTurn;
            case GAME_PLAYERS_WITH_FREE_HAND_CAST -> playersWithFreeHandCastUntilEndOfTurn;
            case GAME_PLAYERS_WHO_MAY_LOOK_AT_FACE_DOWN_CREATURES -> playersWhoMayLookAtFaceDownCreaturesThisTurn;
            case GAME_PLAYERS_MAY_PLAY_FACE_UP_CARDS_FROM_EXILE -> playersMayPlayFaceUpCardsFromExileThisTurn;
            case GAME_CARD_TYPE_FLASH_GRANTS -> mapEntries(cardTypeFlashGrantsThisTurn);
            case GAME_PLAYERS_ALLOWED_TWICE_LOYALTY_ACTIVATIONS -> playersAllowedTwiceLoyaltyActivationsThisTurn;
            case GAME_GRAVEYARD_CARD_ANIMATIONS -> Set.copyOf(temporaryGraveyardCardAnimationsUntilEndOfTurn.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), entry.getValue()))
                    .toList());
            case GAME_PLAYER_COLOR_DAMAGE_PREVENTION_COUNTS -> nestedCounts(playerColorDamagePreventionCount);
            case GAME_COMBAT_DAMAGE_TO_CREATURES_DOUBLINGS -> combatDamageToCreaturesDoublingsThisTurn;
            case GAME_CONTROLLER_DAMAGE_DOUBLINGS -> new HashMap<>(controllerDamageDoublingsThisTurn);
            case GAME_PERMANENT_DAMAGE_DOUBLINGS -> new HashMap<>(permanentDamageDoublingsThisTurn);
            case GAME_COLOR_SOURCE_DAMAGE_BONUSES -> listMapEntries(colorSourceDamageBonusThisTurn);
            case GAME_CONTROLLER_DAMAGE_BONUSES -> listMapEntries(controllerDamageBonusThisTurn);
            case GAME_DAMAGE_REPLACEMENTS -> {
                synchronized (damageReplacementsThisTurn) {
                    yield new ArrayList<>(damageReplacementsThisTurn);
                }
            }
            case GAME_ALL_PERMANENTS_ENTER_TAPPED -> allPermanentsEnterTappedThisTurn;
            case GAME_PERMANENT_ENTER_TAPPED_FILTERS -> mapEntries(permanentEnterTappedFiltersThisTurn);
            case GAME_ADDITIONAL_ENTER_COUNTERS -> new HashMap<>(additionalEnterCountersThisTurn);
            case GAME_MATCHING_CREATURE_BLOCK_RESTRICTIONS -> listMapEntries(matchingCreatureBlockRestrictionsThisTurn);
            case GAME_COUNTER_HOLDER_MUST_BLOCK_REQUIREMENTS -> mapEntries(counterHolderMustBlockRequirementsThisTurn);
            case GAME_PLAYER_EXTRA_LOYALTY_ACTIVATIONS -> new HashMap<>(playerExtraLoyaltyActivationsThisTurn);
            case GAME_EXTRA_MANA_ON_LAND_SUBTYPE_TAP -> listMapEntries(extraManaOnLandSubtypeTapThisTurn);
            case GAME_LAND_SUBTYPE_FIXED_MANA_COLOR -> Set.copyOf(plainMapEntries(landSubtypeFixedManaColorThisTurn));
            case GAME_NONBASIC_LANDS_FIXED_MANA_COLOR -> nonbasicLandsFixedManaColorThisTurn;
            case GAME_ALL_LANDS_FIXED_MANA_COLOR -> allLandsFixedManaColorThisTurn;
            case GAME_PLAYERS_WITH_COLORED_MANA_REPLACEMENT -> playersWithColoredManaReplacementThisTurn;
            case GAME_PLAYERS_WITH_WHITE_MANA_AS_ANY_COLOR -> playersWithWhiteManaAsAnyColorThisTurn;
            case GAME_PLAYERS_WITH_LAND_MANA_CHOICE_REPLACEMENT -> playersWithLandManaChoiceReplacementThisTurn;
            case GAME_LAND_MANA_FIXED_COLOR -> Set.copyOf(plainMapEntries(landManaFixedColorThisTurn));
            case GAME_PLAYER_STATIC_EFFECTS -> listMapEntries(playerStaticEffectsUntilEndOfTurn);
            case GAME_DAMAGE_LIFE_FLOORS -> List.copyOf(damageLifeFloorContributionsUntilEndOfTurn);
            case GAME_SPELL_LIMITS -> List.copyOf(playersMaxSpellsContributionsThisTurn);
            case GAME_CHANNEL_HARM_SHIELDS -> synchronizedCopy(channelHarmShields);
            case GAME_COMEUPPANCE_SHIELDS -> synchronizedCopy(comeuppanceDamagePreventionShields);
            case GAME_JUDGMENT_OF_ALEXANDER_SHIELDS -> synchronizedCopy(judgmentOfAlexanderDamagePreventionShields);
            case GAME_EYE_FOR_AN_EYE_SHIELDS -> synchronizedCopy(eyeForAnEyeShields);
            case GAME_REFLECT_DAMAGE_TO_SOURCE_CONTROLLER_SHIELDS -> synchronizedCopy(reflectDamageToSourceControllerShields);
            case GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_CONTROLLER_SHIELDS ->
                    synchronizedCopy(sourceNextCombatDamageToControllerShields);
            case GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_OPPONENT_REDIRECT_SHIELDS ->
                    synchronizedCopy(sourceNextCombatDamageToOpponentRedirectShields);
            case GAME_SOURCE_NEXT_DAMAGE_REDIRECT_TO_PERMANENT_SHIELDS ->
                    synchronizedCopy(sourceNextDamageRedirectToPermanentShields);
            case GAME_PLAYER_NEXT_INSTANT_OR_SORCERY_DAMAGE_REDIRECT_SHIELDS ->
                    synchronizedCopy(playerNextInstantOrSorceryDamageRedirectShields);
            case GAME_PLAYER_SOURCE_NEXT_DAMAGE_REDIRECT_SHIELDS -> synchronizedCopy(playerSourceNextDamageRedirectShields);
            case GAME_TURN_SOURCE_DAMAGE_REDIRECT_TO_CONTROLLER_SHIELDS ->
                    synchronizedCopy(turnSourceDamageRedirectToControllerShields);
            case GAME_TURN_DAMAGE_REDIRECT_TO_CREATURE_SHIELDS -> synchronizedCopy(turnDamageRedirectToCreatureShields);
            case GAME_TARGET_SORCERY_DAMAGE_REDIRECT_SHIELDS -> synchronizedCopy(targetSorceryDamageRedirectShields);
            case GAME_COMBAT_DAMAGE_PREVENTION_TOKEN_SHIELDS -> Set.copyOf(plainMapEntries(combatDamagePreventionTokenShields));
            case GAME_NEXT_SPELL_FLASH_GRANTS -> synchronizedListMapEntries(nextSpellFlashGrantsThisTurn);
            case GAME_NEXT_SPELL_CHOSEN_SUBTYPE_FLASH_GRANTS ->
                    synchronizedListMapEntries(nextSpellChosenSubtypeFlashGrantsThisTurn);
            case GAME_NEXT_SPELL_CONVOKE_GRANTS -> new HashMap<>(nextSpellConvokeGrantsThisTurn);
            case GAME_NEXT_SPELL_COST_REDUCTIONS -> synchronizedListMapEntries(nextSpellCostReductionsThisTurn);
            case GAME_NEXT_SPELL_PAY_LIFE_EQUAL_TO_MANA_VALUE -> new HashMap<>(nextSpellPayLifeEqualToManaValueThisTurn);
            case GAME_NEXT_SPELL_FREE_CAST_PERMISSIONS -> synchronizedListMapEntries(nextSpellFreeCastPermissionsThisTurn);
            case GAME_NEXT_CREATURE_SPELL_EMPOWERMENTS -> synchronizedListMapEntries(nextCreatureSpellEmpowermentsThisTurn);
            case GAME_NEXT_CREATURE_SPELL_DELAYED_TRIGGERS ->
                    synchronizedListMapEntries(nextCreatureSpellDelayedTriggersThisTurn);
            case GAME_NEXT_CREATURE_SPELL_CASCADE -> new HashMap<>(nextCreatureSpellCascadeThisTurn);
            case GAME_SPELL_COLOR_OVERRIDES -> Set.copyOf(plainMapEntries(spellColorOverridesUntilEndOfTurn));
            case GAME_CARDS_GRANTED_FLASHBACK -> cardsGrantedFlashbackUntilEndOfTurn;
            case GAME_CARDS_GRANTED_FLASHBACK_DESPITE_LOST_ABILITIES -> cardsGrantedFlashbackDespiteLostAbilitiesUntilEndOfTurn;
            case GAME_CARDS_GRANTED_FLASHBACK_COSTS -> Set.copyOf(plainMapEntries(cardsGrantedFlashbackCostsUntilEndOfTurn));
            case GAME_CARDS_GRANTED_WARP -> cardsGrantedWarpUntilEndOfTurn;
            case GAME_CARDS_GRANTED_HARMONIZE -> cardsGrantedHarmonizeUntilEndOfTurn;
            case GAME_CARDS_GRANTED_JUMP_START -> cardsGrantedJumpStartUntilEndOfTurn;
            case GAME_CARDS_GRANTED_EMBALM -> cardsGrantedEmbalmUntilEndOfTurn;
            case GAME_CARDS_GRANTED_UNEARTH -> Set.copyOf(plainMapEntries(cardsGrantedUnearthUntilEndOfTurn));
            case GAME_CARDS_GRANTED_UNEARTH_GRAVEYARD_VERSIONS ->
                    Set.copyOf(plainMapEntries(cardsGrantedUnearthGraveyardVersions));
            case GAME_LIBRARY_TOP_CARD_FREE_PLAY_PERMISSIONS ->
                    Set.copyOf(plainMapEntries(libraryTopCardFreePlayPermissionsUntilEndOfTurn));
            case GAME_LIBRARY_TOP_CARD_PERMISSIONS -> libraryTopCardPermissionsUntilEndOfTurn;
            case GAME_PLAYERS_ALLOWED_TO_PLAY_FROM_LIBRARY_TOP -> playersAllowedToPlayFromLibraryTopUntilEndOfTurn;
            case GAME_LIBRARY_TOP_CARD_LIFE_PLAY_PERMISSIONS -> libraryTopCardLifePlayPermissionsUntilEndOfTurn;
            case GAME_MAY_TAP_LANDS_FOR_SPELLS -> mayTapLandsForSpellsUntilEndOfTurn;
            case GAME_MAY_PAY_LIFE_FOR_COLORLESS_MANA -> mayPayLifeForColorlessManaUntilEndOfTurn;
            case GAME_GUARDIAN_ANGEL_TARGETS -> mapEntries(guardianAngelTargetsUntilEndOfTurn);
            case GAME_MAY_CAST_TOP_INSTANT_OR_SORCERY_FROM_GRAVEYARD -> mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn;
            case GAME_GRAVEYARD_CARD_CAST_PERMISSIONS ->
                    Set.copyOf(plainMapEntries(graveyardCardCastPermissionsUntilEndOfTurn));
            case GAME_GRAVEYARD_CARD_CAST_PERMISSION_GRANTS -> listMapEntries(graveyardCardCastPermissionGrantsUntilEndOfTurn);
            case GAME_GRAVEYARD_CAST_FILTER_PERMISSIONS -> new ArrayList<>(graveyardCastFilterPermissionsThisTurn);
            case GAME_GRAVEYARD_PLAY_FILTER_PERMISSIONS -> new ArrayList<>(graveyardPlayFilterPermissionsThisTurn);
            case GAME_SPELL_COPY_GRANTS -> new HashMap<>(spellCopyGrantsUntilEndOfTurn);
            case GAME_NEXT_INSTANT_SORCERY_STORM -> new HashMap<>(pendingNextInstantSorceryStormThisTurnCount);
            case GAME_NEXT_INSTANT_SORCERY_CAST_FROM_HAND_TO_HAND ->
                    new HashMap<>(pendingNextInstantSorceryCastFromHandToHandThisTurnCount);
            case GAME_NEXT_SPELL_COPIES -> new HashMap<>(pendingNextSpellCopyThisTurnCount);
            case GAME_NEXT_FILTERED_SPELL_COPIES -> listMapEntries(pendingNextFilteredSpellCopiesThisTurn);
            case GAME_NEXT_X_ACTIVATED_ABILITY_COPIES -> new HashMap<>(pendingNextXActivatedAbilityCopyThisTurnCount);
            case GAME_NEXT_SPELL_UNCOUNTERABLE -> new HashMap<>(pendingNextSpellUncounterableThisTurnCount);
            case GAME_NEXT_INSTANT_SORCERY_UNCOUNTERABLE ->
                    new HashMap<>(pendingNextInstantSorceryUncounterableThisTurnCount);
            case GAME_ANY_MANA_TYPE_FOR_NEXT_SPELL -> new HashMap<>(pendingAnyManaTypeForNextSpellThisTurnCount);
            case GAME_CHOSEN_SUBTYPE_ACTIVATION_COPIES -> new ArrayList<>(chosenSubtypeActivationCopiesThisTurn);
            case GAME_CREATURE_ENTERS_DRAW_SOURCES -> synchronizedListMapEntries(creatureEntersDrawSourcesThisTurn);
            case GAME_UNTAP_ATTACKED_CREATURES_EACH_COMBAT_SOURCES ->
                    synchronizedListMapEntries(untapAttackedCreaturesEachCombatThisTurnSources);
            case GAME_EXILE_CAST_PERMISSIONS -> new ArrayList<>(exileCastPermissionsUntilEndOfTurn);
            case GAME_EXILE_CAST_WILDCARD_PERMISSIONS -> new ArrayList<>(exileCastWildcardPermissionsUntilEndOfTurn);
            case GAME_FORETOLD_CARD_CAST_PERMISSIONS -> new ArrayList<>(foretoldCardCastPermissionsThisTurn);
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_IDS -> mapEntries(playerSourceDamagePreventionIds);
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_REQUIRED_COLORS -> {
                Set<Map.Entry<Map.Entry<UUID, UUID>, Set<CardColor>>> entries = new HashSet<>();
                playerSourceDamagePreventionRequiredColors.forEach((playerId, bySource) -> bySource.forEach(
                        (sourceId, colors) -> entries.add(Map.entry(Map.entry(playerId, sourceId), Set.copyOf(colors)))));
                yield entries;
            }
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_LIFE_GAIN_IDS -> mapEntries(playerSourceDamagePreventionLifeGainIds);
            case GAME_PLAYER_NEXT_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED ->
                    listMapEntries(playerNextDamageFromMatchingSourcesPrevented);
            case GAME_NEXT_ENCHANTMENT_CREATURE_ENTRY_ADDITIONAL_COUNTERS ->
                    new HashMap<>(pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn);
            case GAME_NEXT_INSTANT_SORCERY_COPIES -> new HashMap<>(pendingNextInstantSorceryCopyThisTurnCount);
            case GAME_NEXT_INSTANT_SORCERY_COPY_SINGLE_TRIGGERS ->
                    new HashMap<>(pendingNextInstantSorceryCopyThisTurnSingleTriggerCount);
            case GAME_NEXT_INSTANT_SORCERY_COPY_SOURCES ->
                    Set.copyOf(plainMapEntries(pendingNextInstantSorceryCopyThisTurnCountSource));
            case GAME_NEXT_INSTANT_SORCERY_COPY_MAX_MANA_VALUES ->
                    listMapEntries(pendingNextInstantSorceryCopyThisTurnMaxManaValues);
            case GAME_NEXT_INSTANT_SORCERY_DYNAMIC_COPIES -> {
                List<Map.Entry<UUID, PendingDynamicCopy>> entries = new ArrayList<>();
                pendingNextInstantSorceryCopyThisTurnDynamicCounts.forEach((playerId, counts) -> {
                    List<Card> sources = pendingNextInstantSorceryCopyThisTurnDynamicSources
                            .getOrDefault(playerId, List.of());
                    for (int i = 0; i < counts.size(); i++) {
                        entries.add(Map.entry(playerId,
                                new PendingDynamicCopy(counts.get(i), i < sources.size() ? sources.get(i) : null)));
                    }
                });
                yield entries;
            }
            case GAME_NEXT_LOYALTY_ABILITY_COPIES -> new HashMap<>(pendingNextLoyaltyAbilityCopyThisTurnCount);
            case GAME_NEXT_LOYALTY_ABILITY_COPY_SOURCES -> listMapEntries(pendingNextLoyaltyAbilityCopySourcesThisTurn);
            case GAME_NEXT_SPELL_OR_LOYALTY_COPY_LINKED -> pendingNextSpellOrLoyaltyCopyLinkedThisTurn;
            case GAME_NEXT_EXHAUST_ABILITY_COPIES -> new HashMap<>(pendingNextExhaustAbilityCopyThisTurnCount);
            case GAME_NEXT_EXHAUST_ABILITY_COPY_SOURCES -> listMapEntries(pendingNextExhaustAbilityCopySourcesThisTurn);
            case GAME_SEN_TRIPLETS_HAND_PERMISSION -> senControllerPlayerId == null || senControlledPlayerId == null
                    ? null : Map.entry(senControllerPlayerId, senControlledPlayerId);
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private void addGameEndOfTurnStateValue(EndOfTurnStateField field, Object value) {
        switch (field) {
            case GAME_DAMAGE_PREVENTION_SHIELD -> globalDamagePreventionShield += (Integer) value;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE -> preventAllCombatDamage = true;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE_BY_ATTACKING_CREATURES -> preventAllCombatDamageByAttackingCreatures = true;
            case GAME_PREVENT_ALL_COMBAT_DAMAGE_TO_PLAYERS -> preventAllCombatDamageToPlayers = true;
            case GAME_PREVENT_ALL_DAMAGE_TO_ALL_CREATURES -> preventAllDamageToAllCreatures = true;
            case GAME_PREVENT_ALL_DAMAGE_BY_CREATURES -> preventAllDamageByCreatures = true;
            case GAME_DAMAGE_BY_CREATURES_PREVENTION_LIFE_GAIN_PLAYERS ->
                    damageByCreaturesPreventionLifeGainPlayers.addAll((List<UUID>) value);
            case GAME_PREVENT_ALL_DAMAGE_FROM_NON_HUMAN_SOURCES -> preventAllDamageFromNonHumanSources = true;
            case GAME_COMBAT_DAMAGE_EXEMPT_PREDICATES_BY_CONTROLLER ->
                    addMapEntries(combatDamageExemptPredicatesByController, (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_PREVENT_DAMAGE_FROM_COLORS -> preventDamageFromColors.addAll((Set<CardColor>) value);
            case GAME_COLOR_DAMAGE_PREVENTION ->
                    addMapEntries(colorDamagePreventionUntilEndOfTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_COLOR_DAMAGE_PREVENTION_FOR_CONTROLLED_CREATURES ->
                    addMapEntries(colorDamagePreventionForControlledCreaturesUntilEndOfTurn,
                            (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_COLORLESS_DAMAGE_PREVENTION -> colorlessDamagePreventionUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_PERMANENTS_PREVENTED_FROM_DEALING_DAMAGE -> permanentsPreventedFromDealingDamage.addAll((Set<UUID>) value);
            case GAME_CREATURES_PROTECTED_FROM_TARGETING_DAMAGE -> creaturesProtectedFromTargetingDamage.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_ALL_DAMAGE_PREVENTED -> playersWithAllDamagePrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_ALL_PERMANENT_DAMAGE_PREVENTED ->
                    playersWithAllPermanentDamagePrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_ALL_CREATURE_DAMAGE_PREVENTED ->
                    playersWithAllCreatureDamagePrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_ALL_PLAYER_DAMAGE_PREVENTED -> playersWithAllPlayerDamagePrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_ALL_COMBAT_DAMAGE_TO_PLAYER_AND_PLANESWALKERS_PREVENTED ->
                    playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_DAMAGE_FROM_ATTACKERS_PREVENTED ->
                    playersWithDamageFromAttackersPrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_DAMAGE_FROM_OPPONENT_CREATURES_PREVENTED ->
                    playersWithDamageFromOpponentCreaturesPrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_COMBAT_DAMAGE_FROM_TARGET_OPPONENT_CREATURES_PREVENTED ->
                    playersWithCombatDamageFromTargetOpponentCreaturesPrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED ->
                    addMapEntries(playersWithDamageFromMatchingSourcesPrevented,
                            (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_PLAYERS_WITH_DAMAGE_TO_CONTROLLED_CREATURES_FROM_MATCHING_SOURCES_PREVENTED ->
                    addMapEntries(playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented,
                            (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_CREATURES_WITH_ALL_DAMAGE_PREVENTED -> creaturesWithAllDamagePrevented.addAll((Set<UUID>) value);
            case GAME_PLAYERS_REDIRECTING_ALL_CREATURE_DAMAGE -> playersRedirectingAllCreatureDamage.addAll((Set<UUID>) value);
            case GAME_ALL_DAMAGE_PREVENTION_PREDICATES ->
                    allDamagePreventionPredicates.addAll((Set<PermanentPredicate>) value);
            case GAME_ALL_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER ->
                    addMapEntries(allDamagePreventionPredicatesByController,
                            (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_COMBAT_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER ->
                    addMapEntries(combatDamagePreventionPredicatesByController,
                            (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_CREATURES_WITH_COMBAT_DAMAGE_PREVENTED -> creaturesWithCombatDamagePrevented.addAll((Set<UUID>) value);
            case GAME_CREATURES_PREVENTED_FROM_DEALING_COMBAT_DAMAGE ->
                    creaturesPreventedFromDealingCombatDamage.addAll((Set<UUID>) value);
            case GAME_CREATURES_ASSIGNING_NO_COMBAT_DAMAGE -> creaturesAssigningNoCombatDamageThisTurn.addAll((Set<UUID>) value);
            case GAME_DAMAGE_CANT_BE_PREVENTED -> damageCantBePreventedThisTurn = true;
            case GAME_PLAYERS_CANT_GAIN_LIFE -> playersCantGainLifeThisTurn = true;
            case GAME_PLAYERS_CANT_LOSE_LIFE -> playersCantLoseLifeThisTurn = true;
            case GAME_PLAYERS_CANT_SEARCH_LIBRARIES -> playersCantSearchLibrariesThisTurn = true;
            case GAME_CREATURES_CANT_ATTACK -> creaturesCantAttackThisTurn = true;
            case GAME_PLAYERS_WHO_CANT_GAIN_LIFE -> playersWhoCantGainLifeThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_GAIN_NO_LIFE -> playersWhoGainNoLifeThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_CANT_LOSE_LIFE -> playersWhoCantLoseLifeThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_HEXPROOF -> playersWithHexproofThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_SHROUD -> playersWithShroudThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYER_HEXPROOF_FROM_COLORS ->
                    addMapEntries(playerHexproofFromColorsThisTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_PERMANENT_HEXPROOF_FROM_COLORS ->
                    addMapEntries(permanentHexproofFromColorsThisTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_PLAYER_PROTECTION_FROM_COLORS ->
                    addMapEntries(playerProtectionFromColorsUntilEndOfTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_PLAYER_PROTECTION_FROM_PLAYERS ->
                    addMapEntries(playerProtectionFromPlayerIdsUntilEndOfTurn, (Set<Map.Entry<UUID, UUID>>) value);
            case GAME_PLAYER_KEYWORDS -> addMapEntries(playerKeywordsUntilEndOfTurn, (Set<Map.Entry<UUID, Keyword>>) value);
            case GAME_PLAYER_CREATURES_CANT_BE_TARGETED_BY_COLORS ->
                    addMapEntries(playerCreaturesCantBeTargetedByColorsThisTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_PLAYER_SPELLS_CANT_BE_COUNTERED_BY_COLORS ->
                    addMapEntries(playerSpellsCantBeCounteredByColorsThisTurn, (Set<Map.Entry<UUID, CardColor>>) value);
            case GAME_PLAYERS_SPELLS_CANT_BE_COUNTERED -> playersSpellsCantBeCounteredThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CREATURE_SPELLS_CANT_BE_COUNTERED ->
                    playersCreatureSpellsCantBeCounteredThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_SILENCED -> playersSilencedThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_PLAY_LANDS -> playersCantPlayLandsThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_PLAY_FROM_GRAVEYARDS -> playersCantPlayFromGraveyardsThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_CAST_SPELL_TYPES ->
                    addMapEntries(playersCantCastSpellTypesThisTurn, (Set<Map.Entry<UUID, CardType>>) value);
            case GAME_PLAYERS_CANT_CAST_NONCREATURE_SPELLS ->
                    playersCantCastNoncreatureSpellsThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_ACTIVATE_ABILITIES -> playersCantActivateAbilitiesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_ACTIVATE_NON_MANA_ABILITIES ->
                    playersCantActivateNonManaAbilitiesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_CANT_ACTIVATE_LOYALTY_ABILITIES ->
                    playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYER_CREATURES_CANT_ATTACK ->
                    addListMapEntries(playerCreaturesCantAttackThisTurn, (List<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_PLAYERS_GATHERING_SPECIMENS -> playersGatheringSpecimensThisTurn.addAll((List<UUID>) value);
            case GAME_PLAYERS_GATHERING_TOKENS -> playersGatheringTokensThisTurn.addAll((List<UUID>) value);
            case GAME_PLAYERS_EXILING_UNCAST_ENTERING_CREATURES ->
                    playersExilingUncastEnteringCreaturesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_EXILING_UNCAST_ENTERING_NONTOKEN_CREATURES ->
                    playersExilingUncastEnteringNontokenCreaturesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_SHUFFLING_CREATURES_ENTERING_FROM_EXILE ->
                    playersShufflingCreaturesEnteringFromExileThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_EXILING_CREATURES_INSTEAD_OF_DYING ->
                    playersExilingCreaturesInsteadOfDyingThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_EXILING_OPPONENT_CREATURES_INSTEAD_OF_DYING ->
                    playersExilingOpponentCreaturesInsteadOfDyingThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_EXILING_CONTROLLED_PERMANENTS_INSTEAD_OF_DYING ->
                    playersExilingControlledPermanentsInsteadOfDyingThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_EXILING_CARDS_INSTEAD_OF_GRAVEYARD ->
                    playersExilingCardsInsteadOfGraveyardThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_PUTTING_CARDS_ON_BOTTOM_OF_LIBRARY ->
                    playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_FLASH -> playersWithFlashUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_FREE_HAND_CAST -> playersWithFreeHandCastUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WHO_MAY_LOOK_AT_FACE_DOWN_CREATURES ->
                    playersWhoMayLookAtFaceDownCreaturesThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_MAY_PLAY_FACE_UP_CARDS_FROM_EXILE ->
                    playersMayPlayFaceUpCardsFromExileThisTurn.addAll((Set<UUID>) value);
            case GAME_CARD_TYPE_FLASH_GRANTS ->
                    addMapEntries(cardTypeFlashGrantsThisTurn, (Set<Map.Entry<UUID, CardPredicate>>) value);
            case GAME_PLAYERS_ALLOWED_TWICE_LOYALTY_ACTIVATIONS ->
                    playersAllowedTwiceLoyaltyActivationsThisTurn.addAll((Set<UUID>) value);
            case GAME_GRAVEYARD_CARD_ANIMATIONS -> ((Set<Map.Entry<UUID, GraveyardCardAnimation>>) value)
                    .forEach(entry -> temporaryGraveyardCardAnimationsUntilEndOfTurn.put(entry.getKey(), entry.getValue()));
            case GAME_PLAYER_COLOR_DAMAGE_PREVENTION_COUNTS -> ((Map<Map.Entry<UUID, CardColor>, Integer>) value)
                    .forEach((key, count) -> playerColorDamagePreventionCount
                            .computeIfAbsent(key.getKey(), id -> new ConcurrentHashMap<>())
                            .merge(key.getValue(), count, Integer::sum));
            case GAME_COMBAT_DAMAGE_TO_CREATURES_DOUBLINGS -> combatDamageToCreaturesDoublingsThisTurn += (Integer) value;
            case GAME_CONTROLLER_DAMAGE_DOUBLINGS -> addCounts(controllerDamageDoublingsThisTurn, (Map<UUID, Integer>) value);
            case GAME_PERMANENT_DAMAGE_DOUBLINGS -> addCounts(permanentDamageDoublingsThisTurn, (Map<UUID, Integer>) value);
            case GAME_COLOR_SOURCE_DAMAGE_BONUSES -> ((List<Map.Entry<UUID, DamageBonusGrant>>) value)
                    .forEach(entry -> addDamageBonusGrant(colorSourceDamageBonusThisTurn, entry.getKey(), entry.getValue()));
            case GAME_CONTROLLER_DAMAGE_BONUSES -> ((List<Map.Entry<UUID, DamageBonusGrant>>) value)
                    .forEach(entry -> addDamageBonusGrant(controllerDamageBonusThisTurn, entry.getKey(), entry.getValue()));
            case GAME_DAMAGE_REPLACEMENTS ->
                    damageReplacementsThisTurn.addAll((List<ReplaceDamageAboveThresholdThisTurnEffect>) value);
            case GAME_ALL_PERMANENTS_ENTER_TAPPED -> allPermanentsEnterTappedThisTurn = true;
            case GAME_PERMANENT_ENTER_TAPPED_FILTERS ->
                    addMapEntries(permanentEnterTappedFiltersThisTurn, (Set<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_ADDITIONAL_ENTER_COUNTERS -> addCounts(additionalEnterCountersThisTurn, (Map<UUID, Integer>) value);
            case GAME_MATCHING_CREATURE_BLOCK_RESTRICTIONS -> addListMapEntries(matchingCreatureBlockRestrictionsThisTurn,
                    (List<Map.Entry<UUID, GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect>>) value);
            case GAME_COUNTER_HOLDER_MUST_BLOCK_REQUIREMENTS ->
                    addMapEntries(counterHolderMustBlockRequirementsThisTurn, (Set<Map.Entry<UUID, CounterType>>) value);
            case GAME_PLAYER_EXTRA_LOYALTY_ACTIVATIONS ->
                    addCounts(playerExtraLoyaltyActivationsThisTurn, (Map<UUID, Integer>) value);
            case GAME_EXTRA_MANA_ON_LAND_SUBTYPE_TAP -> addListMapEntries(extraManaOnLandSubtypeTapThisTurn,
                    (List<Map.Entry<CardSubtype, TemporaryLandTapMana>>) value);
            case GAME_LAND_SUBTYPE_FIXED_MANA_COLOR -> ((Set<Map.Entry<CardSubtype, ManaColor>>) value)
                    .forEach(entry -> landSubtypeFixedManaColorThisTurn.put(entry.getKey(), entry.getValue()));
            case GAME_NONBASIC_LANDS_FIXED_MANA_COLOR -> nonbasicLandsFixedManaColorThisTurn = (ManaColor) value;
            case GAME_ALL_LANDS_FIXED_MANA_COLOR -> allLandsFixedManaColorThisTurn = (ManaColor) value;
            case GAME_PLAYERS_WITH_COLORED_MANA_REPLACEMENT ->
                    playersWithColoredManaReplacementThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_WHITE_MANA_AS_ANY_COLOR ->
                    playersWithWhiteManaAsAnyColorThisTurn.addAll((Set<UUID>) value);
            case GAME_PLAYERS_WITH_LAND_MANA_CHOICE_REPLACEMENT ->
                    playersWithLandManaChoiceReplacementThisTurn.addAll((Set<UUID>) value);
            case GAME_LAND_MANA_FIXED_COLOR -> ((Set<Map.Entry<UUID, ManaColor>>) value)
                    .forEach(entry -> landManaFixedColorThisTurn.put(entry.getKey(), entry.getValue()));
            case GAME_PLAYER_STATIC_EFFECTS -> ((List<Map.Entry<UUID, CardEffect>>) value).forEach(entry ->
                    playerStaticEffectsUntilEndOfTurn.computeIfAbsent(entry.getKey(), key -> new ArrayList<>())
                            .add(entry.getValue()));
            case GAME_DAMAGE_LIFE_FLOORS -> ((List<Map.Entry<UUID, Integer>>) value)
                    .forEach(entry -> addDamageLifeFloorUntilEndOfTurn(entry.getKey(), entry.getValue()));
            case GAME_SPELL_LIMITS -> ((List<Map.Entry<UUID, Integer>>) value)
                    .forEach(entry -> limitSpellsThisTurn(entry.getKey(), entry.getValue()));
            case GAME_CHANNEL_HARM_SHIELDS -> channelHarmShields.addAll((List<ChannelHarmShield>) value);
            case GAME_COMEUPPANCE_SHIELDS ->
                    comeuppanceDamagePreventionShields.addAll((List<ComeuppanceDamagePreventionShield>) value);
            case GAME_JUDGMENT_OF_ALEXANDER_SHIELDS -> judgmentOfAlexanderDamagePreventionShields
                    .addAll((List<JudgmentOfAlexanderDamagePreventionShield>) value);
            case GAME_EYE_FOR_AN_EYE_SHIELDS -> eyeForAnEyeShields.addAll((List<EyeForAnEyeShield>) value);
            case GAME_REFLECT_DAMAGE_TO_SOURCE_CONTROLLER_SHIELDS ->
                    reflectDamageToSourceControllerShields.addAll((List<UUID>) value);
            case GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_CONTROLLER_SHIELDS -> sourceNextCombatDamageToControllerShields
                    .addAll((List<SourceNextCombatDamageToControllerShield>) value);
            case GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_OPPONENT_REDIRECT_SHIELDS -> sourceNextCombatDamageToOpponentRedirectShields
                    .addAll((List<SourceNextCombatDamageToOpponentRedirectShield>) value);
            case GAME_SOURCE_NEXT_DAMAGE_REDIRECT_TO_PERMANENT_SHIELDS -> sourceNextDamageRedirectToPermanentShields
                    .addAll((List<SourceNextDamageRedirectToPermanentShield>) value);
            case GAME_PLAYER_NEXT_INSTANT_OR_SORCERY_DAMAGE_REDIRECT_SHIELDS ->
                    playerNextInstantOrSorceryDamageRedirectShields.addAll((List<UUID>) value);
            case GAME_PLAYER_SOURCE_NEXT_DAMAGE_REDIRECT_SHIELDS ->
                    playerSourceNextDamageRedirectShields.addAll((List<PlayerSourceNextDamageRedirectShield>) value);
            case GAME_TURN_SOURCE_DAMAGE_REDIRECT_TO_CONTROLLER_SHIELDS -> turnSourceDamageRedirectToControllerShields
                    .addAll((List<TurnSourceDamageRedirectToControllerShield>) value);
            case GAME_TURN_DAMAGE_REDIRECT_TO_CREATURE_SHIELDS ->
                    turnDamageRedirectToCreatureShields.addAll((List<TurnDamageRedirectToCreatureShield>) value);
            case GAME_TARGET_SORCERY_DAMAGE_REDIRECT_SHIELDS ->
                    targetSorceryDamageRedirectShields.addAll((List<TargetSorceryDamageRedirectShield>) value);
            case GAME_COMBAT_DAMAGE_PREVENTION_TOKEN_SHIELDS -> ((Set<Map.Entry<UUID, CombatDamagePreventionTokenShield>>) value)
                    .forEach(entry -> combatDamagePreventionTokenShields.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_NEXT_SPELL_FLASH_GRANTS ->
                    addSynchronizedListMapEntries(nextSpellFlashGrantsThisTurn, (List<Map.Entry<UUID, CardType>>) value);
            case GAME_NEXT_SPELL_CHOSEN_SUBTYPE_FLASH_GRANTS -> addSynchronizedListMapEntries(
                    nextSpellChosenSubtypeFlashGrantsThisTurn, (List<Map.Entry<UUID, CardSubtype>>) value);
            case GAME_NEXT_SPELL_CONVOKE_GRANTS -> addCounts(nextSpellConvokeGrantsThisTurn, (Map<UUID, Integer>) value);
            case GAME_NEXT_SPELL_COST_REDUCTIONS -> addSynchronizedListMapEntries(
                    nextSpellCostReductionsThisTurn, (List<Map.Entry<UUID, NextSpellCostReduction>>) value);
            case GAME_NEXT_SPELL_PAY_LIFE_EQUAL_TO_MANA_VALUE ->
                    addCounts(nextSpellPayLifeEqualToManaValueThisTurn, (Map<UUID, Integer>) value);
            case GAME_NEXT_SPELL_FREE_CAST_PERMISSIONS -> addSynchronizedListMapEntries(
                    nextSpellFreeCastPermissionsThisTurn, (List<Map.Entry<UUID, CardPredicate>>) value);
            case GAME_NEXT_CREATURE_SPELL_EMPOWERMENTS -> addSynchronizedListMapEntries(
                    nextCreatureSpellEmpowermentsThisTurn, (List<Map.Entry<UUID, CreatureSpellEmpowerment>>) value);
            case GAME_NEXT_CREATURE_SPELL_DELAYED_TRIGGERS -> addSynchronizedListMapEntries(
                    nextCreatureSpellDelayedTriggersThisTurn, (List<Map.Entry<UUID, DelayedSpellCastTrigger>>) value);
            case GAME_NEXT_CREATURE_SPELL_CASCADE -> addCounts(nextCreatureSpellCascadeThisTurn, (Map<UUID, Integer>) value);
            case GAME_SPELL_COLOR_OVERRIDES -> ((Set<Map.Entry<UUID, Set<CardColor>>>) value)
                    .forEach(entry -> spellColorOverridesUntilEndOfTurn.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_CARDS_GRANTED_FLASHBACK -> cardsGrantedFlashbackUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_FLASHBACK_DESPITE_LOST_ABILITIES ->
                    cardsGrantedFlashbackDespiteLostAbilitiesUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_FLASHBACK_COSTS -> ((Set<Map.Entry<UUID, String>>) value)
                    .forEach(entry -> cardsGrantedFlashbackCostsUntilEndOfTurn.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_CARDS_GRANTED_WARP -> cardsGrantedWarpUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_HARMONIZE -> cardsGrantedHarmonizeUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_JUMP_START -> cardsGrantedJumpStartUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_EMBALM -> cardsGrantedEmbalmUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_CARDS_GRANTED_UNEARTH -> ((Set<Map.Entry<UUID, String>>) value)
                    .forEach(entry -> cardsGrantedUnearthUntilEndOfTurn.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_CARDS_GRANTED_UNEARTH_GRAVEYARD_VERSIONS -> ((Set<Map.Entry<UUID, Long>>) value)
                    .forEach(entry -> cardsGrantedUnearthGraveyardVersions.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_LIBRARY_TOP_CARD_FREE_PLAY_PERMISSIONS -> ((Set<Map.Entry<UUID, UUID>>) value).forEach(entry ->
                    libraryTopCardFreePlayPermissionsUntilEndOfTurn.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_LIBRARY_TOP_CARD_PERMISSIONS ->
                    libraryTopCardPermissionsUntilEndOfTurn.addAll((Set<LibraryTopCardPermission>) value);
            case GAME_PLAYERS_ALLOWED_TO_PLAY_FROM_LIBRARY_TOP ->
                    playersAllowedToPlayFromLibraryTopUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_LIBRARY_TOP_CARD_LIFE_PLAY_PERMISSIONS ->
                    libraryTopCardLifePlayPermissionsUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_MAY_TAP_LANDS_FOR_SPELLS -> mayTapLandsForSpellsUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_MAY_PAY_LIFE_FOR_COLORLESS_MANA -> mayPayLifeForColorlessManaUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_GUARDIAN_ANGEL_TARGETS ->
                    addMapEntries(guardianAngelTargetsUntilEndOfTurn, (Set<Map.Entry<UUID, UUID>>) value);
            case GAME_MAY_CAST_TOP_INSTANT_OR_SORCERY_FROM_GRAVEYARD ->
                    mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn.addAll((Set<UUID>) value);
            case GAME_GRAVEYARD_CARD_CAST_PERMISSIONS -> ((Set<Map.Entry<UUID, GraveyardCardCastPermission>>) value)
                    .forEach(entry -> graveyardCardCastPermissionsUntilEndOfTurn.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_GRAVEYARD_CARD_CAST_PERMISSION_GRANTS -> addListMapEntries(graveyardCardCastPermissionGrantsUntilEndOfTurn,
                    (List<Map.Entry<UUID, GraveyardCardCastPermission>>) value);
            case GAME_GRAVEYARD_CAST_FILTER_PERMISSIONS ->
                    graveyardCastFilterPermissionsThisTurn.addAll((List<GraveyardCastFilterPermission>) value);
            case GAME_GRAVEYARD_PLAY_FILTER_PERMISSIONS ->
                    graveyardPlayFilterPermissionsThisTurn.addAll((List<GraveyardPlayFilterPermission>) value);
            case GAME_SPELL_COPY_GRANTS -> addCounts(spellCopyGrantsUntilEndOfTurn, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_STORM ->
                    addCounts(pendingNextInstantSorceryStormThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_CAST_FROM_HAND_TO_HAND ->
                    addCounts(pendingNextInstantSorceryCastFromHandToHandThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_SPELL_COPIES -> addCounts(pendingNextSpellCopyThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_FILTERED_SPELL_COPIES -> addListMapEntries(pendingNextFilteredSpellCopiesThisTurn,
                    (List<Map.Entry<UUID, CopyNextSpellCastThisTurnEffect>>) value);
            case GAME_NEXT_X_ACTIVATED_ABILITY_COPIES ->
                    addCounts(pendingNextXActivatedAbilityCopyThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_SPELL_UNCOUNTERABLE ->
                    addCounts(pendingNextSpellUncounterableThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_UNCOUNTERABLE ->
                    addCounts(pendingNextInstantSorceryUncounterableThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_ANY_MANA_TYPE_FOR_NEXT_SPELL ->
                    addCounts(pendingAnyManaTypeForNextSpellThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_CHOSEN_SUBTYPE_ACTIVATION_COPIES ->
                    chosenSubtypeActivationCopiesThisTurn.addAll((List<ChosenSubtypeActivationCopy>) value);
            case GAME_CREATURE_ENTERS_DRAW_SOURCES -> addSynchronizedListMapEntries(
                    creatureEntersDrawSourcesThisTurn, (List<Map.Entry<UUID, Card>>) value);
            case GAME_UNTAP_ATTACKED_CREATURES_EACH_COMBAT_SOURCES -> addSynchronizedListMapEntries(
                    untapAttackedCreaturesEachCombatThisTurnSources, (List<Map.Entry<UUID, Card>>) value);
            case GAME_EXILE_CAST_PERMISSIONS ->
                    exileCastPermissionsUntilEndOfTurn.addAll((List<ExileCastPermission>) value);
            case GAME_EXILE_CAST_WILDCARD_PERMISSIONS ->
                    exileCastWildcardPermissionsUntilEndOfTurn.addAll((List<ExileCastWildcardPermission>) value);
            case GAME_FORETOLD_CARD_CAST_PERMISSIONS ->
                    foretoldCardCastPermissionsThisTurn.addAll((List<ForetoldCardCastPermission>) value);
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_IDS ->
                    addMapEntries(playerSourceDamagePreventionIds, (Set<Map.Entry<UUID, UUID>>) value);
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_REQUIRED_COLORS ->
                    ((Set<Map.Entry<Map.Entry<UUID, UUID>, Set<CardColor>>>) value).forEach(entry ->
                            playerSourceDamagePreventionRequiredColors
                                    .computeIfAbsent(entry.getKey().getKey(), key -> new ConcurrentHashMap<>())
                                    .putIfAbsent(entry.getKey().getValue(), entry.getValue()));
            case GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_LIFE_GAIN_IDS ->
                    addMapEntries(playerSourceDamagePreventionLifeGainIds, (Set<Map.Entry<UUID, UUID>>) value);
            case GAME_PLAYER_NEXT_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED -> addListMapEntries(
                    playerNextDamageFromMatchingSourcesPrevented, (List<Map.Entry<UUID, PermanentPredicate>>) value);
            case GAME_NEXT_ENCHANTMENT_CREATURE_ENTRY_ADDITIONAL_COUNTERS ->
                    addCounts(pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_COPIES ->
                    addCounts(pendingNextInstantSorceryCopyThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_COPY_SINGLE_TRIGGERS ->
                    addCounts(pendingNextInstantSorceryCopyThisTurnSingleTriggerCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_INSTANT_SORCERY_COPY_SOURCES -> ((Set<Map.Entry<UUID, Card>>) value).forEach(entry ->
                    pendingNextInstantSorceryCopyThisTurnCountSource.putIfAbsent(entry.getKey(), entry.getValue()));
            case GAME_NEXT_INSTANT_SORCERY_COPY_MAX_MANA_VALUES -> ((List<Map.Entry<UUID, Integer>>) value)
                    .forEach(entry -> pendingNextInstantSorceryCopyThisTurnMaxManaValues
                            .computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
            case GAME_NEXT_INSTANT_SORCERY_DYNAMIC_COPIES -> ((List<Map.Entry<UUID, PendingDynamicCopy>>) value)
                    .forEach(entry -> {
                        pendingNextInstantSorceryCopyThisTurnDynamicCounts
                                .computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue().count());
                        pendingNextInstantSorceryCopyThisTurnDynamicSources
                                .computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue().source());
                    });
            case GAME_NEXT_LOYALTY_ABILITY_COPIES ->
                    addCounts(pendingNextLoyaltyAbilityCopyThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_LOYALTY_ABILITY_COPY_SOURCES -> ((List<Map.Entry<UUID, PendingCopySource>>) value)
                    .forEach(entry -> pendingNextLoyaltyAbilityCopySourcesThisTurn
                            .computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
            case GAME_NEXT_SPELL_OR_LOYALTY_COPY_LINKED ->
                    pendingNextSpellOrLoyaltyCopyLinkedThisTurn.addAll((Set<UUID>) value);
            case GAME_NEXT_EXHAUST_ABILITY_COPIES ->
                    addCounts(pendingNextExhaustAbilityCopyThisTurnCount, (Map<UUID, Integer>) value);
            case GAME_NEXT_EXHAUST_ABILITY_COPY_SOURCES -> ((List<Map.Entry<UUID, PendingCopySource>>) value)
                    .forEach(entry -> pendingNextExhaustAbilityCopySourcesThisTurn
                            .computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
            case GAME_SEN_TRIPLETS_HAND_PERMISSION -> {
                Map.Entry<UUID, UUID> permission = (Map.Entry<UUID, UUID>) value;
                senControllerPlayerId = permission.getKey();
                senControlledPlayerId = permission.getValue();
            }
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

    /** Removes and returns all floating effects that expire at the beginning of the next end step. */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtNextEndStep() {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_NEXT_END_STEP);
    }

    /** Removes and returns all floating effects with {@code UNTIL_END_OF_COMBAT} duration. */
    public List<FloatingContinuousEffect> expireEndOfCombatFloatingEffects() {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_END_OF_COMBAT);
    }

    /** Removes floating effects that last until any player casts a creature spell. */
    public List<FloatingContinuousEffect> expireFloatingEffectsOnCreatureSpellCast() {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_CREATURE_SPELL_CAST);
    }

    /**
     * Removes and returns all floating effects that depended on the given source permanent still
     * being on the battlefield ({@code WHILE_SOURCE_ON_BATTLEFIELD}, {@code WHILE_SOURCE_TAPPED},
     * {@code WHILE_SOURCE_REMAINS_TAPPED}, and {@code WHILE_ATTACHED}).
     * Called whenever a permanent leaves any battlefield.
     */
    public List<FloatingContinuousEffect> expireFloatingEffectsForDepartedSource(UUID sourcePermanentId) {
        return expireFloatingEffects(fe ->
                (fe.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                        || fe.duration() == EffectDuration.WHILE_SOURCE_CONTROLLED
                        || fe.duration() == EffectDuration.WHILE_SOURCE_REMAINS
                        || fe.duration() == EffectDuration.WHILE_SOURCE_TAPPED
                        || fe.duration() == EffectDuration.WHILE_SOURCE_REMAINS_TAPPED
                        || fe.duration() == EffectDuration.WHILE_ATTACHED)
                        && sourcePermanentId.equals(fe.sourcePermanentId()));
    }

    /**
     * Removes and returns all NON-control source-tapped floating effects sourced from the given
     * permanent. Called when that permanent becomes untapped. Control source-tapped effects are
     * handled separately by the control reconciliation in {@code CreatureControlService}.
     */
    public List<FloatingContinuousEffect> expireTappedSourceFloatingEffects(UUID sourcePermanentId) {
        return expireFloatingEffects(fe -> (fe.duration() == EffectDuration.WHILE_SOURCE_TAPPED
                || fe.duration() == EffectDuration.WHILE_SOURCE_REMAINS_TAPPED)
                && !fe.isControlEffect()
                && sourcePermanentId.equals(fe.sourcePermanentId()));
    }

    /**
     * Removes and returns all {@code WHILE_ATTACHED} floating effects sourced from the given
     * Aura/Equipment. Called whenever the attachment becomes unattached or attaches to a new
     * permanent (the old attachment's effects end either way).
     */
    public List<FloatingContinuousEffect> expireFloatingEffectsForUnattachedSource(UUID sourcePermanentId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.WHILE_ATTACHED
                && sourcePermanentId.equals(fe.sourcePermanentId()));
    }

    /**
     * Removes and returns all {@code UNTIL_YOUR_NEXT_TURN} floating effects controlled by the
     * given player. Called when that player's turn begins.
     */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtTurnStart(UUID playerId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN
                && playerId.equals(fe.controllerId()));
    }

    /** Removes floating effects controlled by the given player after that player's next turn. */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtTurnEnd(UUID playerId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_END_OF_YOUR_NEXT_TURN
                && playerId != null
                && playerId.equals(fe.controllerId())
                && turnStartTimestamp > 0
                && fe.timestamp() < turnStartTimestamp);
    }

    /**
     * Removes and returns all {@code UNTIL_CONTROLLERS_NEXT_UPKEEP} floating effects controlled by
     * the given player. Called at the beginning of that player's upkeep step (Cycle of Life).
     */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtUpkeep(UUID playerId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP
                && playerId.equals(fe.controllerId()));
    }

    /** Marks the beginning of an upkeep for effects that last through the end of that upkeep. */
    public void markUpkeepStart() {
        upkeepStartTimestamp = timestampCounter + 1;
    }

    /** Removes effects that lasted through the end of the current upkeep. */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtUpkeepEnd(UUID playerId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP
                && playerId.equals(fe.controllerId())
                && upkeepStartTimestamp > 0
                && fe.timestamp() < upkeepStartTimestamp);
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
        if (fe.duration() == EffectDuration.WHILE_ATTACHED && fe.sourcePermanentId() != null) {
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
        if (newest == null || newest.duration() != EffectDuration.UNTIL_END_OF_TURN) {
            return false;
        }
        UUID withEffect = resolveControlEffectController(newest);
        FloatingContinuousEffect newestSurviving = null;
        for (FloatingContinuousEffect fe : controlEffectsFor(permanentId)) {
            if (fe.duration() == EffectDuration.UNTIL_END_OF_TURN) continue;
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
     * (priority grant, auto-pass, {@code SpellCastingService.finishSpellCast}).
     */
    public void enqueueTrigger(StackEntry entry) {
        if (waitingForSubgame && session.active() != this) {
            suspendedZoneTriggers.add(entry);
            for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
                suspendedZoneTriggers.add(new StackEntry(entry));
            }
            return;
        }
        if (manaAbilityResolutionDepth > 0) {
            pendingManaAbilityTriggers.add(entry);
            for (int i = 1; i < activeTriggeredAbilityCopies; i++) {
                pendingManaAbilityTriggers.add(new StackEntry(entry));
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

    private static final class TriggerAwareStackList extends ArrayList<StackEntry> {

        private final GameData gameData;

        private TriggerAwareStackList(GameData gameData) {
            this.gameData = gameData;
        }

        @Override
        public boolean add(StackEntry entry) {
            gameData.stampSpellEnteringStack(entry);
            boolean added = super.add(entry);
            if (added) {
                gameData.recordStackEntryForCrime(entry);
                gameData.recordGraveyardTargetVersions(entry);
            }
            if (added && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                for (int i = 1; i < gameData.activeTriggeredAbilityCopies(); i++) {
                    StackEntry copy = new StackEntry(entry);
                    super.add(copy);
                    gameData.recordStackEntryForCrime(copy);
                }
            }
            if (added) {
                gameData.recordCurrentStackSourceCount();
            }
            return added;
        }

        @Override
        public void add(int index, StackEntry entry) {
            gameData.stampSpellEnteringStack(entry);
            super.add(index, entry);
            gameData.recordStackEntryForCrime(entry);
            gameData.recordGraveyardTargetVersions(entry);
            gameData.recordCurrentStackSourceCount();
        }

        @Override
        public boolean addAll(java.util.Collection<? extends StackEntry> entries) {
            entries.forEach(gameData::stampSpellEnteringStack);
            boolean added = super.addAll(entries);
            if (added) {
                entries.forEach(gameData::recordStackEntryForCrime);
                entries.forEach(gameData::recordGraveyardTargetVersions);
                gameData.recordCurrentStackSourceCount();
            }
            return added;
        }

        @Override
        public boolean addAll(int index, java.util.Collection<? extends StackEntry> entries) {
            entries.forEach(gameData::stampSpellEnteringStack);
            boolean added = super.addAll(index, entries);
            if (added) {
                entries.forEach(gameData::recordStackEntryForCrime);
                entries.forEach(gameData::recordGraveyardTargetVersions);
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
                return type.cast(interaction);
            }
        }
        return null;
    }

    /**
     * Removes every queued interaction of the given kind (e.g. Karn restart wiping trigger state).
     */
    public void clearPendingInteractions(Class<? extends PendingInteraction> type) {
        pendingInteractions.removeIf(type::isInstance);
    }

    // ===== Delayed-action queue helpers (mirror the pendingInteractions helpers above) =====

    /**
     * Appends a scheduled {@link DelayedAction} to the tail of the unified delayed-action queue.
     */
    public void queueDelayedAction(DelayedAction action) {
        if (action instanceof PendingExileReturn pending && findExiledCard(pending.card().getId()) != null) {
            pendingExileReturnExileVersions.put(pending,
                    exileEntryVersions.getOrDefault(pending.card().getId(), -1L));
        }
        delayedActions.add(action);
    }

    /** Marks a land as already cleaned by the corresponding Cyclopean Tomb delayed trigger. */
    public void markCyclopeanTombLandRemoved(UUID actionId, UUID landId) {
        synchronized (delayedActions) {
            for (int i = 0; i < delayedActions.size(); i++) {
                DelayedAction action = delayedActions.get(i);
                if (action instanceof CyclopeanTombUpkeepCleanup cleanup
                        && cleanup.actionId().equals(actionId)) {
                    delayedActions.set(i, cleanup.withLandRemoved(landId));
                    return;
                }
            }
        }
    }

    /** Updates the last-known source snapshot of delayed spell-cast triggers before a permanent leaves. */
    public void updateDelayedControllerSpellCastTriggerSourceSnapshot(Permanent permanent) {
        updateDelayedControllerSpellCastTriggerSourceSnapshot(permanent, null);
    }

    /** Updates a delayed spell-cast trigger's last-known source state before a permanent leaves. */
    public void updateDelayedControllerSpellCastTriggerSourceSnapshot(
            Permanent permanent, Integer powerAtLastKnown) {
        synchronized (delayedActions) {
            for (int i = 0; i < delayedActions.size(); i++) {
                DelayedAction action = delayedActions.get(i);
                if (action instanceof DelayedControllerSpellCastTrigger delayed
                        && permanent.getId().equals(delayed.sourcePermanentId())) {
                    delayedActions.set(i, delayed.withSourcePermanentSnapshot(
                            new Permanent(permanent), powerAtLastKnown));
                }
            }
        }
    }

    /**
     * Returns {@code true} if the queue holds at least one delayed action of the given kind.
     */
    public boolean hasDelayedAction(Class<? extends DelayedAction> type) {
        for (DelayedAction action : delayedActions) {
            if (type.isInstance(action)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the queue holds at least one delayed action of the given kind that
     * matches {@code filter} (e.g. one timing point's {@link DelayedPermanentAction} kinds).
     */
    public <T extends DelayedAction> boolean hasDelayedAction(Class<T> type, Predicate<T> filter) {
        for (DelayedAction action : delayedActions) {
            if (type.isInstance(action) && filter.test(type.cast(action))) return true;
        }
        return false;
    }

    /**
     * Returns an unmodifiable snapshot of all queued delayed actions of the given kind in insertion
     * order, WITHOUT removing them (for read-only consumers such as the per-combat-step loot check).
     */
    public <T extends DelayedAction> List<T> getDelayedActions(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (DelayedAction action : delayedActions) {
            if (type.isInstance(action)) result.add(type.cast(action));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Removes and returns all queued delayed actions of the given kind, preserving insertion order.
     */
    public <T extends DelayedAction> List<T> drainDelayedActions(Class<T> type) {
        List<T> drained = new ArrayList<>();
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (type.isInstance(action)) {
                drained.add(type.cast(action));
                it.remove();
            }
        }
        return drained;
    }

    /** Removes and returns the first queued delayed action of the given kind, if one exists. */
    public <T extends DelayedAction> T drainFirstDelayedAction(Class<T> type) {
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (type.isInstance(action)) {
                it.remove();
                return type.cast(action);
            }
        }
        return null;
    }

    /** Returns the first queued delayed action of the given kind without removing it. */
    public <T extends DelayedAction> T peekFirstDelayedAction(Class<T> type) {
        for (DelayedAction action : delayedActions) {
            if (type.isInstance(action)) {
                return type.cast(action);
            }
        }
        return null;
    }

    /**
     * Removes and returns the queued delayed actions of the given kind that match {@code filter},
     * preserving insertion order and leaving non-matching entries in place (used by the per-step
     * exile-return drain, which fires only entries whose scheduled step is the current one).
     */
    public <T extends DelayedAction> List<T> drainDelayedActions(Class<T> type, Predicate<T> filter) {
        List<T> drained = new ArrayList<>();
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (type.isInstance(action)) {
                T typed = type.cast(action);
                if (filter.test(typed)) {
                    drained.add(typed);
                    it.remove();
                }
            }
        }
        return drained;
    }

    /**
     * Removes every queued delayed action of the given kind (e.g. Karn restart wiping scheduled state,
     * or turn cleanup clearing the delayed combat-damage loot triggers).
     */
    public void clearDelayedActions(Class<? extends DelayedAction> type) {
        delayedActions.removeIf(type::isInstance);
    }

    /**
     * Removes every queued delayed action of the given kind that matches {@code filter} (e.g. only
     * some {@link DelayedPermanentAction} kinds), leaving non-matching entries in place.
     */
    public <T extends DelayedAction> void clearDelayedActions(Class<T> type, Predicate<T> filter) {
        delayedActions.removeIf(action -> type.isInstance(action) && filter.test(type.cast(action)));
    }

    /**
     * Accumulates {@code delta} pending +1/+1 counters for {@code permanentId} at the next end step,
     * preserving the legacy keyed-map semantics (at most one {@link DelayedPlusOneCounters} per
     * permanent, holding the running total).
     */
    public void addDelayedPlusOneCounters(UUID permanentId, int delta) {
        addDelayedPlusOneCounters(permanentId, null, delta);
    }

    /** As {@link #addDelayedPlusOneCounters(UUID, int)}, with the controller of the delayed trigger fixed. */
    public void addDelayedPlusOneCounters(UUID permanentId, UUID controllerId, int delta) {
        int total = delta;
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (action instanceof DelayedPlusOneCounters existing && existing.permanentId().equals(permanentId)
                    && java.util.Objects.equals(existing.controllerId(), controllerId)) {
                total += existing.totalCounters();
                it.remove();
            }
        }
        delayedActions.add(new DelayedPlusOneCounters(permanentId, total, controllerId));
    }

    /**
     * Accumulates {@code delta} pending +0/+1 counters for {@code permanentId} at the next end step
     * (Sacred Boon), preserving keyed-map semantics (at most one {@link DelayedPlusZeroPlusOneCounters}
     * per permanent, holding the running total).
     */
    public void addDelayedPlusZeroPlusOneCounters(UUID permanentId, int delta) {
        int total = delta;
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (action instanceof DelayedPlusZeroPlusOneCounters existing && existing.permanentId().equals(permanentId)) {
                total += existing.totalCounters();
                it.remove();
            }
        }
        delayedActions.add(new DelayedPlusZeroPlusOneCounters(permanentId, total));
    }

    /**
     * Removes and returns the Sacred Boon entry for {@code shieldId}, or {@code null} if it is gone.
     */
    public DelayedPlusZeroPlusOneCounters takeSacredBoonEntry(UUID shieldId) {
        var drained = drainDelayedActions(DelayedPlusZeroPlusOneCounters.class,
                entry -> entry.shieldId().equals(shieldId));
        return drained.isEmpty() ? null : drained.getFirst();
    }

    /**
     * Re-queues a triggered Sacred Boon entry whose prevention is still active.
     */
    public void requeueSacredBoonEntry(DelayedPlusZeroPlusOneCounters entry) {
        delayedActions.add(entry);
    }

    public void registerSacredBoonShield(UUID permanentId, UUID controllerId, Card sourceCard,
                                         int amount) {
        delayedActions.add(new DelayedPlusZeroPlusOneCounters(
                permanentId, 0, controllerId, sourceCard, UUID.randomUUID(), amount));
    }

    public int consumeSacredBoonShields(UUID permanentId, int damage) {
        int remainingDamage = damage;
        List<DelayedPlusZeroPlusOneCounters> replacements = new ArrayList<>();
        var it = delayedActions.iterator();
        while (it.hasNext() && remainingDamage > 0) {
            DelayedAction action = it.next();
            if (!(action instanceof DelayedPlusZeroPlusOneCounters shield)
                    || !shield.permanentId().equals(permanentId)
                    || shield.remainingShield() <= 0) {
                continue;
            }
            int prevented = Math.min(shield.remainingShield(), remainingDamage);
            remainingDamage -= prevented;
            it.remove();
            replacements.add(new DelayedPlusZeroPlusOneCounters(
                    shield.permanentId(), shield.totalCounters() + prevented,
                    shield.controllerId(), shield.sourceCard(), shield.shieldId(),
                    shield.remainingShield() - prevented, shield.triggered()));
        }
        delayedActions.addAll(replacements);
        return damage - remainingDamage;
    }

    /**
     * Ends every Sacred Boon prevention remainder at cleanup. The prevention lapses with "this turn",
     * but the +0/+1 counters already owed for damage prevented stay queued for the next end step.
     */
    public void endSacredBoonShieldsAtCleanup() {
        List<DelayedPlusZeroPlusOneCounters> replacements = new ArrayList<>();
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (action instanceof DelayedPlusZeroPlusOneCounters shield && shield.triggered()) {
                it.remove();
            } else if (action instanceof DelayedPlusZeroPlusOneCounters shield && shield.remainingShield() > 0) {
                it.remove();
                replacements.add(new DelayedPlusZeroPlusOneCounters(
                        shield.permanentId(), shield.totalCounters(), shield.controllerId(),
                        shield.sourceCard(), shield.shieldId(), 0, false));
            }
        }
        delayedActions.addAll(replacements);
    }

    /**
     * Returns the pending +1/+1 counter total scheduled for {@code permanentId} (0 if none).
     */
    public int getDelayedPlusOneCounters(UUID permanentId) {
        int total = 0;
        for (DelayedAction action : delayedActions) {
            if (action instanceof DelayedPlusOneCounters existing && existing.permanentId().equals(permanentId)) {
                total += existing.totalCounters();
            }
        }
        return total;
    }

    /**
     * Records a spell cast by the given player this turn.
     */
    public void recordSpellCast(UUID playerId, Card card) {
        // Per-spell entry grants belong to this cast object. Clear a stale grant when a countered
        // card is cast again before end-of-turn cleanup, then let current grants be recorded below.
        spellAdditionalEnterCounters.remove(card.getId());
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
        consumeNextSpellFlashGrant(playerId, card);
        consumeNextSpellChosenSubtypeFlashGrant(playerId, card);
        if (hasNextSpellConvokeGrant(playerId)) {
            spellsCastWithConvokeGrantThisTurn.add(card.getId());
        }
        consumeNextSpellConvokeGrant(playerId);
        consumeNextSpellCostReductions(playerId, card);
        consumeNextSpellPayLifeEqualToManaValue(playerId);
        consumeNextCreatureSpellEmpowerments(playerId, card);
        consumePersistentNextCreatureSpellEmpowerments(playerId, card);
        consumeNextSpellUncounterableGrant(playerId, card);
        consumeNextInstantSorceryUncounterableGrant(playerId, card);
    }

    /** Records a card cycled by the given player during this game. */
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
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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

    /**
     * Adds a pending "the next creature spell you cast this turn ..." grant for the player
     * (Savage Summoning).
     */
    public void addNextCreatureSpellEmpowerment(UUID playerId, CreatureSpellEmpowerment empowerment) {
        nextCreatureSpellEmpowermentsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(empowerment);
    }

    /** Registers a delayed trigger for the player's next creature spell this turn. */
    public void addNextCreatureSpellDelayedTrigger(UUID playerId, DelayedSpellCastTrigger trigger) {
        nextCreatureSpellDelayedTriggersThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(trigger);
    }

    /** Removes and returns every delayed trigger waiting for the next creature spell the player casts. */
    public List<DelayedSpellCastTrigger> consumeNextCreatureSpellDelayedTriggers(UUID playerId, Card card) {
        if (card == null || !card.hasType(CardType.CREATURE)) return List.of();
        List<DelayedSpellCastTrigger> triggers = nextCreatureSpellDelayedTriggersThisTurn.remove(playerId);
        return triggers == null ? List.of() : new ArrayList<>(triggers);
    }

    /** Adds one pending cascade grant for the next creature spell this turn. */
    public void addNextCreatureSpellCascade(UUID playerId) {
        nextCreatureSpellCascadeThisTurn.merge(playerId, 1, Integer::sum);
    }

    /** Consumes every pending cascade grant when the next creature spell is cast. */
    public int consumeNextCreatureSpellCascade(UUID playerId, Card card) {
        if (card == null || !card.hasType(CardType.CREATURE)) return 0;
        Integer grants = nextCreatureSpellCascadeThisTurn.remove(playerId);
        return grants == null ? 0 : grants;
    }

    /** Adds a persistent one-time boon for the player's next creature spell. */
    public void addPersistentNextCreatureSpellEmpowerment(UUID playerId, CreatureSpellEmpowerment empowerment) {
        nextCreatureSpellEmpowerments
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(empowerment);
    }

    /**
     * Applies every pending creature-spell empowerment to the creature spell just cast. All pending
     * grants refer to the same "next creature spell", so they all apply and are all consumed.
     */
    private void consumeNextCreatureSpellEmpowerments(UUID playerId, Card card) {
        consumeCreatureSpellEmpowerments(nextCreatureSpellEmpowermentsThisTurn, playerId, card);
    }

    private void consumePersistentNextCreatureSpellEmpowerments(UUID playerId, Card card) {
        consumeCreatureSpellEmpowerments(nextCreatureSpellEmpowerments, playerId, card);
    }

    private void consumeCreatureSpellEmpowerments(
            Map<UUID, List<CreatureSpellEmpowerment>> empowerments, UUID playerId, Card card) {
        if (!card.hasType(CardType.CREATURE)) return;
        List<CreatureSpellEmpowerment> grants = empowerments.get(playerId);
        if (grants == null) return;
        List<CreatureSpellEmpowerment> consumed;
        synchronized (grants) {
            if (grants.isEmpty()) return;
            consumed = new ArrayList<>(grants);
            grants.clear();
        }
        for (CreatureSpellEmpowerment grant : consumed) {
            if (grant.uncounterable()) {
                spellsMadeUncounterable.add(card.getId());
            }
            if (grant.additionalPlusOneCounters() > 0) {
                spellAdditionalEnterCounters.merge(card.getId(), grant.additionalPlusOneCounters(), Integer::sum);
            }
        }
    }

    /** Makes the next spell cast by the player uncounterable and consumes all pending grants. */
    private void consumeNextSpellUncounterableGrant(UUID playerId, Card card) {
        if (pendingNextSpellUncounterableThisTurnCount.remove(playerId) != null) {
            spellsMadeUncounterable.add(card.getId());
        }
    }

    public void addPendingAnyManaTypeForNextSpellThisTurn(UUID playerId) {
        pendingAnyManaTypeForNextSpellThisTurnCount.merge(playerId, 1, Integer::sum);
    }

    public boolean hasPendingAnyManaTypeForNextSpellThisTurn(UUID playerId) {
        return pendingAnyManaTypeForNextSpellThisTurnCount.getOrDefault(playerId, 0) > 0;
    }

    public void markSpellPaidUsingPendingAnyManaType(UUID cardId) {
        if (cardId != null) {
            spellsPaidUsingPendingAnyManaTypeThisTurn.add(cardId);
        }
    }

    private void consumePendingAnyManaTypePermission(UUID playerId, Card card) {
        if (!spellsPaidUsingPendingAnyManaTypeThisTurn.remove(card.getId())) {
            return;
        }
        pendingAnyManaTypeForNextSpellThisTurnCount.computeIfPresent(playerId, (id, count) ->
                count <= 1 ? null : count - 1);
    }

    private void consumeNextInstantSorceryUncounterableGrant(UUID playerId, Card card) {
        if (!card.hasType(CardType.INSTANT) && !card.hasType(CardType.SORCERY)) return;
        if (pendingNextInstantSorceryUncounterableThisTurnCount.remove(playerId) != null) {
            spellsMadeUncounterable.add(card.getId());
        }
    }

    /**
     * Adds a pending "the next spell of this type you cast this turn can be cast as though it had
     * flash" grant for the player (Quicken).
     */
    public void addNextSpellFlashGrant(UUID playerId, CardType cardType) {
        nextSpellFlashGrantsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(cardType);
    }

    public void addNextSpellFlashGrantForChosenSubtype(UUID playerId, CardSubtype subtype) {
        nextSpellChosenSubtypeFlashGrantsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(subtype);
    }

    public void addNextSpellConvokeGrant(UUID playerId) {
        nextSpellConvokeGrantsThisTurn.merge(playerId, 1, Integer::sum);
    }

    public boolean hasNextSpellConvokeGrant(UUID playerId) {
        return nextSpellConvokeGrantsThisTurn.getOrDefault(playerId, 0) > 0;
    }

    public boolean spellWasCastWithConvokeGrant(UUID spellCardId) {
        return spellsCastWithConvokeGrantThisTurn.contains(spellCardId);
    }

    public void addNextSpellCostReduction(UUID playerId, NextSpellCostReduction reduction) {
        nextSpellCostReductionsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reduction);
    }

    public void addNextSpellPayLifeEqualToManaValue(UUID playerId) {
        nextSpellPayLifeEqualToManaValueThisTurn.merge(playerId, 1, Integer::sum);
    }

    public boolean hasNextSpellPayLifeEqualToManaValue(UUID playerId) {
        return nextSpellPayLifeEqualToManaValueThisTurn.getOrDefault(playerId, 0) > 0;
    }

    public void addNextSpellFreeCastPermission(UUID playerId, CardPredicate predicate) {
        nextSpellFreeCastPermissionsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(predicate);
    }

    private void consumeNextSpellCostReductions(UUID playerId, Card card) {
        List<NextSpellCostReduction> reductions = nextSpellCostReductionsThisTurn.get(playerId);
        if (reductions == null) return;
        synchronized (reductions) {
            reductions.removeIf(reduction -> reduction.cardTypes().stream().anyMatch(card::hasType));
        }
    }

    private void consumeNextSpellPayLifeEqualToManaValue(UUID playerId) {
        nextSpellPayLifeEqualToManaValueThisTurn.remove(playerId);
    }

    /** Adds an unlimited flash permission for spells of the given type until end of turn. */
    public void addCardTypeFlashGrant(UUID playerId, CardType cardType) {
        addCardPredicateFlashGrant(playerId, new CardTypePredicate(cardType));
    }

    /** Adds an unlimited flash permission for spells matching the predicate until end of turn. */
    public void addCardPredicateFlashGrant(UUID playerId, CardPredicate predicate) {
        cardTypeFlashGrantsThisTurn
                .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                .add(predicate);
    }

    /** Adds an unlimited flash permission for spells matching the predicate until the player's next turn. */
    public void addCardPredicateFlashGrantUntilNextTurn(UUID playerId, CardPredicate predicate) {
        cardTypeFlashGrantsUntilNextTurn
                .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                .add(predicate);
    }

    /** Returns true if the player may cast spells of the card's type as though they had flash. */
    public boolean hasCardTypeFlashGrant(UUID playerId, Card card) {
        Set<CardPredicate> grants = cardTypeFlashGrantsThisTurn.get(playerId);
        return grants != null && grants.stream()
                .filter(CardTypePredicate.class::isInstance)
                .map(CardTypePredicate.class::cast)
                .anyMatch(grant -> card.hasType(grant.cardType()));
    }

    /** Returns true if a until-next-turn flash grant names one of the card's types. */
    public boolean hasCardTypeFlashGrantUntilNextTurn(UUID playerId, Card card) {
        Set<CardPredicate> grants = cardTypeFlashGrantsUntilNextTurn.get(playerId);
        return grants != null && grants.stream()
                .filter(CardTypePredicate.class::isInstance)
                .map(CardTypePredicate.class::cast)
                .anyMatch(grant -> card.hasType(grant.cardType()));
    }

    /**
     * Returns true if the player holds an unconsumed next-spell flash grant matching this card's types.
     */
    public boolean hasNextSpellFlashGrant(UUID playerId, Card card) {
        List<CardType> grants = nextSpellFlashGrantsThisTurn.get(playerId);
        if (grants == null) return false;
        synchronized (grants) {
            return grants.stream().anyMatch(type -> type == null || card.hasType(type));
        }
    }

    /** Returns true if the player holds a pending grant matching one of this card's creature
     * subtypes. Changeling spells match every creature subtype. */
    public boolean hasNextSpellChosenSubtypeFlashGrant(UUID playerId, Card card) {
        List<CardSubtype> grants = nextSpellChosenSubtypeFlashGrantsThisTurn.get(playerId);
        if (grants == null) return false;
        synchronized (grants) {
            return grants.stream().anyMatch(subtype -> card.getSubtypes().contains(subtype)
                    || card.hasKeyword(Keyword.CHANGELING));
        }
    }

    private void consumeNextSpellFlashGrant(UUID playerId, Card card) {
        List<CardType> grants = nextSpellFlashGrantsThisTurn.get(playerId);
        if (grants == null) return;
        synchronized (grants) {
            // Every pending grant names the same "next spell of this type", so one matching spell
            // consumes them all rather than only the oldest.
            grants.removeIf(type -> type == null || card.hasType(type));
        }
    }

    private void consumeNextSpellChosenSubtypeFlashGrant(UUID playerId, Card card) {
        List<CardSubtype> grants = nextSpellChosenSubtypeFlashGrantsThisTurn.get(playerId);
        if (grants == null) return;
        synchronized (grants) {
            grants.removeIf(subtype -> card.getSubtypes().contains(subtype)
                    || card.hasKeyword(Keyword.CHANGELING));
        }
    }

    private void consumeNextSpellConvokeGrant(UUID playerId) {
        nextSpellConvokeGrantsThisTurn.remove(playerId);
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

    public void addSpellCastPathOfAncestryManaSpent(UUID spellCardId, int manaSpent) {
        if (manaSpent > 0) {
            spellCastPathOfAncestryManaSpent.merge(spellCardId, manaSpent, Integer::sum);
        }
    }

    public int getSpellCastPathOfAncestryManaSpent(UUID spellCardId) {
        return spellCastPathOfAncestryManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastPathOfAncestryManaSpent(UUID spellCardId) {
        spellCastPathOfAncestryManaSpent.remove(spellCardId);
    }

    public void markPendingPathOfAncestryManaChoice(UUID sourcePermanentId, int amount) {
        if (sourcePermanentId != null && amount > 0) {
            pendingPathOfAncestryManaChoices.merge(sourcePermanentId, amount, Integer::sum);
        }
    }

    public int consumePendingPathOfAncestryManaChoice(UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) {
            return 0;
        }
        int[] consumed = {0};
        pendingPathOfAncestryManaChoices.computeIfPresent(sourcePermanentId, (ignored, pending) -> {
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

    /** Returns the number of spells cast with Treasure-produced mana by the player this turn. */
    public long getSpellsCastUsingTreasureManaThisTurnCount(UUID playerId) {
        return spellsCastThisTurn.getOrDefault(playerId, List.of()).stream()
                .filter(card -> spellsCastUsingTreasureManaThisTurn.contains(card.getId()))
                .count();
    }

    /** Applies an absolute spell limit for the rest of this turn, keeping the most restrictive one. */
    public void limitSpellsThisTurn(UUID playerId, int maxSpells) {
        if (playerId == null) return;
        playersMaxSpellsThisTurn.merge(playerId, maxSpells, Math::min);
        playersMaxSpellsContributionsThisTurn.add(Map.entry(playerId, maxSpells));
    }

    /** Clears every "this turn" spell limit set by {@link #limitSpellsThisTurn}. */
    public void clearSpellLimitsThisTurn() {
        playersMaxSpellsThisTurn.clear();
        playersMaxSpellsContributionsThisTurn.clear();
    }

    /**
     * Until end of turn, damage can't reduce {@code playerId}'s life total below {@code floor} (Angel of Grace); the
     * highest floor applies.
     */
    public void addDamageLifeFloorUntilEndOfTurn(UUID playerId, int floor) {
        if (playerId == null) return;
        damageLifeFloorsUntilEndOfTurn.merge(playerId, floor, Math::max);
        damageLifeFloorContributionsUntilEndOfTurn.add(Map.entry(playerId, floor));
    }

    /** Clears every floor set by {@link #addDamageLifeFloorUntilEndOfTurn}. */
    public void clearDamageLifeFloorsUntilEndOfTurn() {
        damageLifeFloorsUntilEndOfTurn.clear();
        damageLifeFloorContributionsUntilEndOfTurn.clear();
    }

    /** Prevents {@code playerId} from casting any further spells this turn. */
    public void preventAdditionalSpellCastsThisTurn(UUID playerId) {
        if (playerId == null) return;

        EnumSet<CardType> allSpellTypes = EnumSet.allOf(CardType.class);
        allSpellTypes.remove(CardType.LAND);
        playersCantCastSpellTypesThisTurn.merge(playerId, allSpellTypes, (existing, added) -> {
            EnumSet<CardType> merged = EnumSet.copyOf(existing);
            merged.addAll(added);
            return merged;
        });
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
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
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
                for (CardEffect effect : planar.getCard().getEffects(EffectSlot.STATIC)) {
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
        }

        private void recordCardPutIntoHand(Card card) {
            cardsPutIntoHandThisTurn.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
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
        CommanderGraveyardList(List<Card> cards) { super(cards); }
        @Override public boolean add(Card card) { commanderEnteredReturnZone(card); return super.add(card); }
        @Override public void add(int index, Card card) { commanderEnteredReturnZone(card); super.add(index, card); }
        @Override public boolean addAll(java.util.Collection<? extends Card> cards) { cards.forEach(GameData.this::commanderEnteredReturnZone); return super.addAll(cards); }
        @Override public boolean addAll(int index, java.util.Collection<? extends Card> cards) { cards.forEach(GameData.this::commanderEnteredReturnZone); return super.addAll(index, cards); }
    }
    public final Map<UUID, UUID> pendingCommandCasts = new ConcurrentHashMap<>();
    public UUID commandCastPlayerId;
    public UUID commandCastCardId;
    /** While set, {@link #foreignHandPlayPlayerId} is playing a card from {@link #foreignHandOwnerId}'s hand
     *  (Sen Triplets): the hand cast and land-play paths read that hand instead of the player's own. */
    public UUID foreignHandPlayPlayerId;
    public UUID foreignHandOwnerId;
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
        return playerMaximumLifeTotals.getOrDefault(playerId, Integer.MAX_VALUE);
    }

    /** Applies the player's maximum life total to a proposed new life total. */
    public int capLifeTotal(UUID playerId, int lifeTotal) {
        return Math.min(lifeTotal, getMaximumLifeTotal(playerId));
    }

    /**
     * Discards copy-rider mana (Sunken Palace) spent during an action without a spell cast or an
     * ability activated to claim it: mana spent on anything else, such as a tax or a special action,
     * never triggers the rider (CR 106.6).
     */
    public void discardUnclaimedSpentManaRiders() {
        playerManaPools.values().forEach(ManaPool::takeSpentCopyGrantingMana);
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
                        || permanent.isAnimatedUntilEndOfTurn()
                        || permanent.isAnimatedUntilEndOfCombat()
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
        cardsRevealedInHandUntilOwnerNextTurn.remove(card.getId());
        cardsRevealedInHandUntilUpkeepEnd.remove(card.getId());
        cardsCantBePlayedInHandUntilOwnerNextTurn.remove(card.getId());
        countersPreservedAcrossZoneChanges.remove(card.getId());
        // Returning from the graveyard makes the card a new object (CR 400.7), so graveyard permissions
        // and the exile-instead-of-graveyard replacement granted to the old object no longer apply.
        graveyardPlayPermissions.remove(card.getId());
        graveyardPlayPermissionsExpireEndOfTurn.remove(card.getId());
        graveyardCardsEnterTapped.remove(card.getId());
        exileInsteadOfGraveyard.remove(card.getId());
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

    private List<Card> asLibrary(List<Card> cards) {
        if (cards instanceof LibraryCardList) {
            return cards;
        }
        LibraryCardList wrapped = new LibraryCardList();
        wrapped.addAllWithoutClearing(cards);
        return wrapped;
    }

    private final class LibraryMap extends ConcurrentHashMap<UUID, List<Card>> {

        @Override
        public List<Card> put(UUID key, List<Card> value) {
            return super.put(key, asLibrary(value));
        }

        @Override
        public List<Card> putIfAbsent(UUID key, List<Card> value) {
            return super.putIfAbsent(key, asLibrary(value));
        }

        @Override
        public List<Card> computeIfAbsent(UUID key,
                                          Function<? super UUID, ? extends List<Card>> mappingFunction) {
            return super.computeIfAbsent(key, ignored -> asLibrary(mappingFunction.apply(key)));
        }

        @Override
        public void putAll(Map<? extends UUID, ? extends List<Card>> map) {
            map.forEach(this::put);
        }
    }

    private final class LibraryCardList extends ArrayList<Card> {

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
            return super.add(card);
        }

        @Override
        public void add(int index, Card card) {
            if (deferCommanderZoneMove(card, Zone.LIBRARY, index)) return;
            forgetPreservedCounters(card);
            super.add(index, card);
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
            entry.convertToBombardmentMissileSpell(missile.getEffects(EffectSlot.SPELL));
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

    /** Adds a card to the ante zone, represented by an untracked exile entry. */
    public void addToAnte(UUID ownerId, Card card) {
        exiledCards.add(new ExiledCardEntry(card, ownerId, null, false, turnNumber));
        markCardAsAnted(card);
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
                || !playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn.contains(ownerId)) {
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
            delayedActions.replaceAll(action -> action instanceof DelayedStillExiledCardsEndStepTrigger still
                    && still.cardIds().contains(cardId) ? still.withoutCard(cardId) : action);
            delayedActions.removeIf(action -> action instanceof com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep back
                    && back.cardId().equals(cardId));
            exiledVoyageCounters.remove(cardId);
            exiledVoyageControllerIds.remove(cardId);
            hauntingCardToPermanentId.remove(cardId);
            foretoldCardIds.remove(cardId);
            foretoldCardCosts.remove(cardId);
            foretoldCardTurnsTaken.remove(cardId);
            exilePlayPermissionSourceCards.entrySet().removeIf(entry -> cardId.equals(entry.getValue()));
            exilePlayPermissionSourcePermanents.remove(cardId);
            exilePlayPermissionStaticSourcePermanents.remove(cardId);
            exileCastPermissionsUntilEndOfTurn.removeIf(permission -> permission.cardId().equals(cardId));
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
            exilePlayAnyManaTypeWhileExiled.remove(cardId);
            plottedCardIds.remove(cardId);
            exilePlayPermissions.remove(cardId);
            exileLookPermissions.remove(cardId);
            additionalExileLookPermissions.remove(cardId);
            clearExilePlayPermissionGroup(cardId);
            exilePlayPermissionConditions.remove(cardId);
            exilePlayPermissionLoreCounterSagas.remove(cardId);
            exilePlayPermissionSpellFilters.remove(cardId);
            exilePlayForLifeEqualToManaValue.remove(cardId);
            exilePlayCostModifiers.remove(cardId);
            exilePlayPermissionsExpireEndOfTurn.remove(cardId);
            exilePlayPermissionsExpireAtTurnEnd.remove(cardId);
            exilePlayPermissionsExpireAtEndStep.remove(cardId);
            exilePlayPermissionsAwaitNextTurnOfPlayer.remove(cardId);
            exilePlayPermissionsExpireAtTurnBeginning.remove(cardId);
            exilePlayAnyManaType.remove(cardId);
            exilePlayWithoutPayingManaCost.remove(cardId);
            exileCardsEnterTapped.remove(cardId);
            exileInsteadOfGraveyard.remove(cardId);
            cardsExiledFromGraveyardThisTurn.remove(cardId);
            exiledCardTimeCounters.remove(cardId);
            exiledCardsWithNonSuspendTimeCounters.remove(cardId);
            exiledCardsWithAlaundoCastAbility.remove(cardId);
            exiledCardHitCounters.remove(cardId);
            lukkaExileCastPermissions.remove(cardId);
            suspendedSpellExiles.removeIf(pending -> cardId.equals(pending.cardId()));
        }
        return removed;
    }

    /** Registers a shared quota for a group of exiled cards that may be played. */
    public synchronized void registerExilePlayPermissionGroup(UUID groupId, int maxUses,
                                                               Collection<UUID> cardIds) {
        if (maxUses < 0) {
            throw new IllegalArgumentException("maximum exile plays must not be negative");
        }
        exilePlayPermissionGroupUsesRemaining.put(groupId, maxUses);
        for (UUID cardId : cardIds) {
            exilePlayPermissionGroups.put(cardId, groupId);
        }
    }

    /** Returns whether a card's shared limited exile-play permission still has a use available. */
    public synchronized boolean hasExilePlayPermissionRemaining(UUID cardId) {
        UUID groupId = exilePlayPermissionGroups.get(cardId);
        return groupId == null
                || exilePlayPermissionGroupUsesRemaining.getOrDefault(groupId, 0) > 0;
    }

    /** Adds a one-spell-per-card-type restriction to an existing exile permission group. */
    public synchronized void registerExileSpellTypePermissionGroup(UUID groupId) {
        exileSpellTypePermissionHistory.put(groupId, new ArrayList<>());
    }

    /** Checks whether the spell and earlier spells can each use a distinct permitted card type. */
    public synchronized boolean hasExileSpellTypePermission(UUID cardId, Card spell) {
        UUID groupId = exilePlayPermissionGroups.get(cardId);
        List<Set<CardType>> history = groupId == null ? null : exileSpellTypePermissionHistory.get(groupId);
        if (history == null) return true;
        List<Set<CardType>> choices = new ArrayList<>(history);
        choices.add(exilePermissionSpellTypes(spell));
        return assignExilePermissionSpellTypes(choices, 0, EnumSet.noneOf(CardType.class));
    }

    /** Records a spell cast using a one-spell-per-card-type exile permission. */
    public synchronized boolean consumeExileSpellTypePermission(UUID cardId, Card spell) {
        if (!hasExileSpellTypePermission(cardId, spell)) return false;
        UUID groupId = exilePlayPermissionGroups.get(cardId);
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
        UUID groupId = exilePlayPermissionGroups.get(cardId);
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

    /** Removes a card from its shared limited exile-play permission group. */
    public synchronized void clearExilePlayPermissionGroup(UUID cardId) {
        UUID groupId = exilePlayPermissionGroups.remove(cardId);
        if (groupId != null && !exilePlayPermissionGroups.containsValue(groupId)) {
            exilePlayPermissionGroupUsesRemaining.remove(groupId);
            exileSpellTypePermissionHistory.remove(groupId);
        }
    }

    /** Ends exile-play permissions granted while a source permanent was controlled. */
    public void expireExilePlayPermissionsForSource(UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return;
        }
        for (Map.Entry<UUID, UUID> entry : exilePlayPermissionSourcePermanents.entrySet()) {
            UUID cardId = entry.getKey();
            if (sourcePermanentId.equals(entry.getValue())
                    && exilePlayPermissionSourcePermanents.remove(cardId, sourcePermanentId)) {
                exilePlayPermissions.remove(cardId);
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
        removedIds.forEach(lukkaExileCastPermissions::remove);
        removedIds.forEach(antedCardIds::remove);
        removedIds.forEach(cardId -> {
            exilePlayPermissionSourcePermanents.remove(cardId);
            exilePlayPermissions.remove(cardId);
            exileLookPermissions.remove(cardId);
            additionalExileLookPermissions.remove(cardId);
        });
    }

    /** Removes source tracking from exile entries (sets sourcePermanentId to null). Used by Karn restart. */
    public void clearAllSourceTracking() {
        exilePlayPermissionSourcePermanents.keySet().forEach(exilePlayPermissions::remove);
        exilePlayPermissionSourcePermanents.clear();
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

    /**
     * CR 603.5 — Puts a "you may" triggered ability directly onto the stack with the
     * MayEffect wrapper intact.  The may choice happens at resolution time, not trigger time.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may) {
        stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may))
        ));
    }

    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID triggeringCardId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may))
        );
        entry.setTriggeringCardId(triggeringCardId);
        if (triggeringCardId != null) {
            entry.setTriggeringCardGraveyardEntryVersion(graveyardEntryVersion(triggeringCardId));
        }
        stack.add(entry);
    }

    /**
     * CR 603.5 — Puts a "you may" triggered ability with source permanent and target context
     * directly onto the stack.  The may choice happens at resolution time, not trigger time.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId, UUID sourcePermanentId) {
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, 0, false);
    }

    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, boolean markSourceOncePerTurnOnAcceptance) {
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, 0,
                markSourceOncePerTurnOnAcceptance);
    }

    /** Queues an enter-triggered may ability with last-known toughness context. */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, boolean markSourceOncePerTurnOnAcceptance,
                                Integer triggeringPermanentToughnessAtTrigger) {
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, 0,
                markSourceOncePerTurnOnAcceptance, triggeringPermanentToughnessAtTrigger);
    }

    /** Queues a resolution-time may ability while preserving its active-player context. */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, UUID activePlayerId, Permanent sourcePermanentSnapshot) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                targetCardId,
                sourcePermanentId
        );
        entry.setActivePlayerId(activePlayerId);
        entry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        stack.add(entry);
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

    /** Queues a may ability while retaining the controller of the permanent that caused the trigger. */
    public void queueMayAbilityForPlayer(Card sourceCard, UUID controllerId, MayEffect may,
                                         UUID targetCardId, UUID sourcePermanentId, UUID choicePlayerId,
                                         Permanent sourcePermanentSnapshot, UUID sourceControllerId,
                                         UUID triggeringPermanentId) {
        queueMayAbilityForPlayer(sourceCard, controllerId, may, targetCardId, sourcePermanentId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringPermanentId,
                null);
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
     * Puts a resolution-time May ability on the stack while preserving the combat target that
     * defines a non-targeting defending-player effect.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, UUID attackedTargetId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                targetCardId,
                sourcePermanentId
        );
        entry.setAttackedTargetId(attackedTargetId);
        if (may.wrapped() instanceof OtherAttackingCreatureReferenceEffect) {
            entry.setNonTargeting(true);
        }
        stack.add(entry);
    }

    /**
     * Same as {@link #queueMayAbility(Card, UUID, MayEffect, UUID, UUID)} but also snapshots an
     * {@code eventValue} (e.g. combat damage dealt) onto the stack entry so that the wrapped effect
     * can reference "that many" via an {@code EventValue} amount at resolution time (e.g.
     * Cold-Eyed Selkie's "you may draw that many cards").
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId, UUID sourcePermanentId, int eventValue) {
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, eventValue, false);
    }

    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, int eventValue,
                                boolean markSourceOncePerTurnOnAcceptance) {
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, eventValue,
                markSourceOncePerTurnOnAcceptance, null);
    }

    /** Queues a may ability while preserving last-known toughness for an entering permanent. */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId,
                                UUID sourcePermanentId, int eventValue,
                                boolean markSourceOncePerTurnOnAcceptance,
                                Integer triggeringPermanentToughnessAtTrigger) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                targetCardId,
                sourcePermanentId
        );
        entry.setEventValue(eventValue);
        entry.setMarkSourceOncePerTurnOnAcceptance(markSourceOncePerTurnOnAcceptance);
        entry.setTriggeringPermanentToughnessAtTrigger(triggeringPermanentToughnessAtTrigger);
        if (may.targetSpec().declaredTarget() == null) {
            entry.setNonTargeting(true);
        }
        if (triggeringPermanentToughnessAtTrigger != null && targetCardId != null) {
            entry.setTriggeringPermanentId(targetCardId);
        }
        stack.add(entry);
    }

    /**
     * CR 603.5 — Puts a "you may pay" triggered ability directly onto the stack with the
     * MayPayManaEffect wrapper intact.  The may choice happens at resolution time.
     * The targetCardId (e.g. the entering permanent for Mirrorworks) is preserved on the
     * stack entry so that the wrapped effect can reference it at resolution time.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayPayManaEffect mayPay, UUID targetCardId) {
        queueMayAbility(sourceCard, controllerId, mayPay, targetCardId, null);
    }

    public void queueMayAbility(Card sourceCard, UUID controllerId, MayPayManaEffect mayPay,
                                UUID targetCardId, UUID sourcePermanentId) {
        queueMayAbility(sourceCard, controllerId, mayPay, targetCardId, sourcePermanentId, 0);
    }

    public void queueMayAbility(Card sourceCard, UUID controllerId, MayPayManaEffect mayPay,
                                UUID targetCardId, UUID sourcePermanentId, int eventValue) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)),
                targetCardId,
                sourcePermanentId
        );
        entry.setEventValue(eventValue);
        stack.add(entry);
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
        return Math.max(card.getStartingIntensity(), card.getEffects(EffectSlot.SPELL).stream()
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
        copy.reflectDamageSourceIds = new ArrayList<>(source.reflectDamageSourceIds);
        copy.pendingOptionalDamageChoices = source.pendingOptionalDamageChoices;
        copy.awaitingOptionalDamageChoices = source.awaitingOptionalDamageChoices;
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
        source.selfDealsCombatDamageEffects.forEach((key, value) -> copy.selfDealsCombatDamageEffects.put(permanentCopy.apply(key), new ArrayList<>(value)));
        source.selfDealsCombatDamageToPlayerOrPlaneswalkerEffects.forEach((key, value) -> copy.selfDealsCombatDamageToPlayerOrPlaneswalkerEffects.put(permanentCopy.apply(key), new ArrayList<>(value)));
        source.selfDealsCombatDamageToPlayerOrBattleEffects.forEach((key, value) -> copy.selfDealsCombatDamageToPlayerOrBattleEffects.put(permanentCopy.apply(key), new ArrayList<>(value)));
        source.selfDealsDamageEffects.forEach((key, value) -> copy.selfDealsDamageEffects.put(permanentCopy.apply(key), new ArrayList<>(value)));
        source.enchantedCreatureDealsDamageTriggers.forEach(value -> copy.enchantedCreatureDealsDamageTriggers.add(new StackEntry(value)));
        source.allyCreatureDealsDamageToPlaneswalkerTriggers.forEach(value -> copy.allyCreatureDealsDamageToPlaneswalkerTriggers.add(new StackEntry(value)));
        source.delayedCombatDamageDrawQualifications.forEach(value -> copy.delayedCombatDamageDrawQualifications.add(new CombatDamageState.DelayedCombatDamageDrawQualification(value.delayedAction(), Set.copyOf(value.sourceIds()))));
        copy.delayedCombatDamageLookAtHandAndDrawQualifications.addAll(source.delayedCombatDamageLookAtHandAndDrawQualifications);
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
        copy.subgameRequested = subgameRequested;
        copy.waitingForSubgame = waitingForSubgame;
        copy.pendingAncestorTransfers.addAll(pendingAncestorTransfers);
        copy.subgameCards.putAll(subgameCards);
        suspendedZoneTriggers.forEach(entry -> copy.suspendedZoneTriggers.add(new StackEntry(entry)));
        copy.cardsExiledListener = this.cardsExiledListener;
        copy.opponentOwnedCardExiledListener = this.opponentOwnedCardExiledListener;

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
        copy.globalDamagePreventionShield = this.globalDamagePreventionShield;
        copy.preventAllCombatDamage = this.preventAllCombatDamage;
        copy.preventAllCombatDamageByAttackingCreatures = this.preventAllCombatDamageByAttackingCreatures;
        copy.preventAllCombatDamageToPlayers = this.preventAllCombatDamageToPlayers;
        copy.collectingSimultaneousCreatureEntryTriggers = this.collectingSimultaneousCreatureEntryTriggers;
        this.simultaneousCreatureEntryTriggers.forEach((id, effects) ->
                copy.simultaneousCreatureEntryTriggers.put(id, new HashSet<>(effects)));
        copy.preventAllDamageToAllCreatures = this.preventAllDamageToAllCreatures;
        copy.preventAllDamageByCreatures = this.preventAllDamageByCreatures;
        copy.preventAllDamageFromNonHumanSources = this.preventAllDamageFromNonHumanSources;
        copy.combatDamageExemptPredicate = this.combatDamageExemptPredicate;
        this.combatDamageExemptPredicatesByController.forEach((controllerId, predicates) ->
                copy.combatDamageExemptPredicatesByController.put(controllerId, new HashSet<>(predicates)));
        this.pendingAttackingCopyOpponents.forEach((batchId, opponents) ->
                copy.pendingAttackingCopyOpponents.put(batchId, new ArrayList<>(opponents)));
        copy.pendingAttackingCopyChoices.putAll(this.pendingAttackingCopyChoices);
        copy.combatDamageExemptControllerId = this.combatDamageExemptControllerId;
        copy.allPermanentsEnterTappedThisTurn = this.allPermanentsEnterTappedThisTurn;
        this.permanentEnterTappedFiltersThisTurn.forEach((playerId, filters) -> {
            Set<PermanentPredicate> copiedFilters = ConcurrentHashMap.newKeySet();
            copiedFilters.addAll(filters);
            copy.permanentEnterTappedFiltersThisTurn.put(playerId, copiedFilters);
        });
        copy.additionalEnterCountersThisTurn.putAll(this.additionalEnterCountersThisTurn);
        copy.additionalEnterCountersUntilNextTurn.putAll(this.additionalEnterCountersUntilNextTurn);
        copy.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn.putAll(
                this.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn);
        copy.activeAdditionalCountersForEnchantmentCreatureEntryBatch.putAll(
                this.activeAdditionalCountersForEnchantmentCreatureEntryBatch);
        copyDamageBonusGrants(this.colorSourceDamageBonusThisTurn, copy.colorSourceDamageBonusThisTurn);
        copyDamageBonusGrants(this.controllerDamageBonusThisTurn, copy.controllerDamageBonusThisTurn);
        copy.combatDamageRedirectTarget = this.combatDamageRedirectTarget;
        copy.combatDamageRedirectPlayer = this.combatDamageRedirectPlayer;
        copy.pendingEffectResolutionEntry = this.pendingEffectResolutionEntry != null
                ? new StackEntry(this.pendingEffectResolutionEntry) : null;
        copy.pendingEffectResolutionIndex = this.pendingEffectResolutionIndex;
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
        // resolvedMayTargetingEntry usually aliases pendingEffectResolutionEntry (the CR 603.5
        // resolution-time targeting flow sets the chosen target through the alias and resumes
        // through pendingEffectResolutionEntry) — preserve the shared identity in the copy,
        // otherwise the simulated choice answer would set the target on a dead copy.
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
        copy.onlyLandCreaturesCanAttackThisCombat = this.onlyLandCreaturesCanAttackThisCombat;
        copy.onlyAggressiveCreaturesCanAttackThisCombat = this.onlyAggressiveCreaturesCanAttackThisCombat;
        copy.creaturesCantAttackThisCombat = this.creaturesCantAttackThisCombat;
        copy.onlyPermanentCanAttackThisCombatId = this.onlyPermanentCanAttackThisCombatId;
        copy.onlyPermanentsCanAttackThisCombatIds = this.onlyPermanentsCanAttackThisCombatIds == null
                ? null : new HashSet<>(this.onlyPermanentsCanAttackThisCombatIds);
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
        copy.untapLockActivatorBySource.putAll(this.untapLockActivatorBySource);
        copy.exileEntryVersions.putAll(this.exileEntryVersions);
        copy.handEntryVersions.putAll(this.handEntryVersions);
        copy.pendingExileReturnExileVersions.putAll(this.pendingExileReturnExileVersions);
        copy.pendingExileReturnEarliestTurns.putAll(this.pendingExileReturnEarliestTurns);
        this.permanentsCantAttackUntilNextTurn.forEach((permanentId, controllers) ->
                copy.permanentsCantAttackUntilNextTurn.put(permanentId, new java.util.HashSet<>(controllers)));
        copy.combatDamageFirstStrikeStepComplete = this.combatDamageFirstStrikeStepComplete;
        copy.combatDamageFirstestStrikeStepComplete = this.combatDamageFirstestStrikeStepComplete;
        copy.creaturesInFirstestStrikeDamageStepThisCombat.addAll(this.creaturesInFirstestStrikeDamageStepThisCombat);
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.combatDamageFirstStrikeAssignmentPhase = this.combatDamageFirstStrikeAssignmentPhase;
        copy.combatDamageFirstestStrikeAssignmentPhase = this.combatDamageFirstestStrikeAssignmentPhase;
        copy.pendingGraveyardReturnQueue.addAll(this.pendingGraveyardReturnQueue);
        copy.deferredGraveyardCastCopies.addAll(this.deferredGraveyardCastCopies);
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
        copy.preventDamageFromColors.addAll(this.preventDamageFromColors);
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);
        copy.playersWithAllDamagePrevented.addAll(this.playersWithAllDamagePrevented);
        copy.playersWithAllPermanentDamagePrevented.addAll(this.playersWithAllPermanentDamagePrevented);
        copy.playersWithAllCreatureDamagePrevented.addAll(this.playersWithAllCreatureDamagePrevented);
        copy.playersRedirectingAllCreatureDamage.addAll(this.playersRedirectingAllCreatureDamage);
        copy.resolvingDeclinedAllCreatureDamageRedirect = this.resolvingDeclinedAllCreatureDamageRedirect;
        copy.playersWithAllPlayerDamagePrevented.addAll(this.playersWithAllPlayerDamagePrevented);
        copy.playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented
                .addAll(this.playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented);
        copy.playersWithAllPlayerDamagePreventedUntilNextTurn
                .addAll(this.playersWithAllPlayerDamagePreventedUntilNextTurn);
        copy.combatDamagePreventionTokenShields.putAll(this.combatDamagePreventionTokenShields);
        copy.playersWithColoredManaReplacementThisTurn
                .addAll(this.playersWithColoredManaReplacementThisTurn);
        copy.playersWithWhiteManaAsAnyColorThisTurn
                .addAll(this.playersWithWhiteManaAsAnyColorThisTurn);
        copy.playersWithProtectionFromEverythingUntilNextTurn
                .addAll(this.playersWithProtectionFromEverythingUntilNextTurn);
        copy.playersWithLifeTotalCantChangeUntilNextTurn
                .addAll(this.playersWithLifeTotalCantChangeUntilNextTurn);
        copy.playersWithDamageFromAttackersPrevented.addAll(this.playersWithDamageFromAttackersPrevented);
        copy.playersWithDamageFromOpponentCreaturesPrevented.addAll(this.playersWithDamageFromOpponentCreaturesPrevented);
        copy.playersWithCombatDamageFromTargetOpponentCreaturesPrevented
                .addAll(this.playersWithCombatDamageFromTargetOpponentCreaturesPrevented);
        this.playersWithDamageFromMatchingSourcesPrevented.forEach((k, v) ->
                copy.playersWithDamageFromMatchingSourcesPrevented.put(k, new HashSet<>(v)));
        this.playerNextDamageFromMatchingSourcesPrevented.forEach((k, v) ->
                copy.playerNextDamageFromMatchingSourcesPrevented.put(k, new CopyOnWriteArrayList<>(v)));
        this.playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented.forEach((k, v) ->
                copy.playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented.put(k, new HashSet<>(v)));
        this.playersWithDamageFromNamedPlanesPrevented.forEach((k, v) ->
                copy.playersWithDamageFromNamedPlanesPrevented.put(k, new HashSet<>(v)));
        copy.playersGatheringSpecimensThisTurn.addAll(this.playersGatheringSpecimensThisTurn);
        copy.playersGatheringTokensThisTurn.addAll(this.playersGatheringTokensThisTurn);
        copy.playersExilingUncastEnteringCreaturesThisTurn.addAll(this.playersExilingUncastEnteringCreaturesThisTurn);
        copy.playersExilingUncastEnteringNontokenCreaturesThisTurn
                .addAll(this.playersExilingUncastEnteringNontokenCreaturesThisTurn);
        copy.playersShufflingCreaturesEnteringFromExileThisTurn
                .addAll(this.playersShufflingCreaturesEnteringFromExileThisTurn);
        copy.playersExilingCreaturesInsteadOfDyingThisTurn
                .addAll(this.playersExilingCreaturesInsteadOfDyingThisTurn);
        copy.playersExilingOpponentCreaturesInsteadOfDyingThisTurn
                .addAll(this.playersExilingOpponentCreaturesInsteadOfDyingThisTurn);
        copy.playersExilingControlledPermanentsInsteadOfDyingThisTurn
                .addAll(this.playersExilingControlledPermanentsInsteadOfDyingThisTurn);
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
        copy.playersWithHexproofThisTurn.addAll(this.playersWithHexproofThisTurn);
        copy.playersWithShroudThisTurn.addAll(this.playersWithShroudThisTurn);
        copy.playersDealtCombatDamageSinceTheirLastTurn.addAll(this.playersDealtCombatDamageSinceTheirLastTurn);
        copy.playersDealtCombatDamageLastTurn.addAll(this.playersDealtCombatDamageLastTurn);
        copy.playersWhoActivatedExhaustAbilityThisTurn.addAll(this.playersWhoActivatedExhaustAbilityThisTurn);
        copy.playersWhoActivatedAbilityOfGraveyardCardThisTurn
                .addAll(this.playersWhoActivatedAbilityOfGraveyardCardThisTurn);
        copy.playersWhoActivatedEquipAbilityThisTurn.addAll(this.playersWhoActivatedEquipAbilityThisTurn);
        copy.playersWhoActivatedArtifactAbilityThisTurn.addAll(this.playersWhoActivatedArtifactAbilityThisTurn);
        copy.playersWhoActivatedPowerUpAbilityThisTurn.addAll(this.playersWhoActivatedPowerUpAbilityThisTurn);
        copy.creaturesWithAllDamagePrevented.addAll(this.creaturesWithAllDamagePrevented);
        copy.permanentsPreventedFromDealingDamageUntilNextTurn.putAll(this.permanentsPreventedFromDealingDamageUntilNextTurn);
        copy.permanentsProtectedFromDamageUntilNextTurn.putAll(this.permanentsProtectedFromDamageUntilNextTurn);
        copy.allDamagePreventionPredicates.addAll(this.allDamagePreventionPredicates);
        this.allDamagePreventionPredicatesByController.forEach((controllerId, predicates) ->
                copy.allDamagePreventionPredicatesByController
                        .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                        .addAll(predicates));
        this.combatDamagePreventionPredicatesByController.forEach((controllerId, predicates) ->
                copy.combatDamagePreventionPredicatesByController
                        .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                        .addAll(predicates));
        copy.creaturesWithCombatDamagePrevented.addAll(this.creaturesWithCombatDamagePrevented);
        copy.creaturesPreventedFromDealingCombatDamage.addAll(this.creaturesPreventedFromDealingCombatDamage);
        copy.creaturesAssigningNoCombatDamageThisTurn.addAll(this.creaturesAssigningNoCombatDamageThisTurn);
        copy.creaturesAssigningNoCombatDamageThisCombat.addAll(this.creaturesAssigningNoCombatDamageThisCombat);
        copy.creaturesWithCombatDamagePreventedThisCombat.addAll(
                this.creaturesWithCombatDamagePreventedThisCombat);
        copy.creaturesPreventedFromDealingCombatDamageThisCombat.addAll(
                this.creaturesPreventedFromDealingCombatDamageThisCombat);
        this.colorDamagePreventionUntilEndOfTurn.forEach((targetId, colors) -> {
            Set<CardColor> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(colors);
            copy.colorDamagePreventionUntilEndOfTurn.put(targetId, copied);
        });
        this.colorDamagePreventionForControlledCreaturesUntilEndOfTurn.forEach((controllerId, colors) -> {
            Set<CardColor> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(colors);
            copy.colorDamagePreventionForControlledCreaturesUntilEndOfTurn.put(controllerId, copied);
        });
        copy.colorlessDamagePreventionUntilEndOfTurn.addAll(this.colorlessDamagePreventionUntilEndOfTurn);
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
        copy.damageCantBePreventedThisTurn = this.damageCantBePreventedThisTurn;
        copy.damageLifeFloorsUntilEndOfTurn.putAll(this.damageLifeFloorsUntilEndOfTurn);
        copy.damageLifeFloorContributionsUntilEndOfTurn.addAll(this.damageLifeFloorContributionsUntilEndOfTurn);
        copy.damageReplacementsThisTurn.addAll(this.damageReplacementsThisTurn);
        copy.playersCantGainLifeThisTurn = this.playersCantGainLifeThisTurn;
        copy.playersCantLoseLifeThisTurn = this.playersCantLoseLifeThisTurn;
        copy.playersCantSearchLibrariesThisTurn = this.playersCantSearchLibrariesThisTurn;
        copy.creaturesCantAttackThisTurn = this.creaturesCantAttackThisTurn;
        this.playerCreaturesCantAttackThisTurn.forEach((playerId, filters) ->
                copy.playerCreaturesCantAttackThisTurn.put(playerId, new ArrayList<>(filters)));
        copy.combatDamageToCreaturesDoublingsThisTurn = this.combatDamageToCreaturesDoublingsThisTurn;
        copy.controllerDamageDoublingsThisTurn.putAll(this.controllerDamageDoublingsThisTurn);
        copy.permanentDamageDoublingsThisTurn.putAll(this.permanentDamageDoublingsThisTurn);
        copy.opponentGraveyardLifeLossWatchers.addAll(this.opponentGraveyardLifeLossWatchers);
        copy.lifeGainOpponentLifeLossWatchers.addAll(this.lifeGainOpponentLifeLossWatchers);
        copy.temporaryGlobalTriggeredAbilities.addAll(this.temporaryGlobalTriggeredAbilities);
        copy.sacrificeBoonWatchers.addAll(this.sacrificeBoonWatchers);
        copy.creatureDeathTriggerWatchers.addAll(this.creatureDeathTriggerWatchers);
        copy.targetedCreatureDeathTriggerWatchers.addAll(this.targetedCreatureDeathTriggerWatchers);
        copy.damagedCreatureDeathTriggerWatchers.addAll(this.damagedCreatureDeathTriggerWatchers);
        copy.allyCreatureEntersTriggerWatchers.addAll(this.allyCreatureEntersTriggerWatchers);
        copy.damageRedirectShields.addAll(this.damageRedirectShields);
        copy.comeuppanceDamagePreventionShields.addAll(this.comeuppanceDamagePreventionShields);
        copy.judgmentOfAlexanderDamagePreventionShields.addAll(this.judgmentOfAlexanderDamagePreventionShields);
        copy.channelHarmShields.addAll(this.channelHarmShields);
        copy.sourceDamageRedirectShields.addAll(this.sourceDamageRedirectShields);
        copy.creatureDamageRedirectShields.addAll(this.creatureDamageRedirectShields);
        copy.turnDamageRedirectToCreatureShields.addAll(this.turnDamageRedirectToCreatureShields);
        copy.creatureControllerDamageRedirectShields.addAll(this.creatureControllerDamageRedirectShields);
        copy.turnSourceDamageRedirectToControllerShields.addAll(this.turnSourceDamageRedirectToControllerShields);
        copy.playerNextDamageRedirectShields.addAll(this.playerNextDamageRedirectShields);
        copy.sourcePermanentAndControllerNextDamageRedirectShields.addAll(
                this.sourcePermanentAndControllerNextDamageRedirectShields);
        copy.playerSourceNextDamageRedirectShields.addAll(this.playerSourceNextDamageRedirectShields);
        copy.playerNextInstantOrSorceryDamageRedirectShields.addAll(this.playerNextInstantOrSorceryDamageRedirectShields);
        copy.sourceNextCombatDamageToOpponentRedirectShields.addAll(this.sourceNextCombatDamageToOpponentRedirectShields);
        copy.targetSourceDamagePreventionShields.addAll(this.targetSourceDamagePreventionShields);
        copy.damagePreventionLifeGainShields.addAll(this.damagePreventionLifeGainShields);
        copy.damageByCreaturesPreventionLifeGainPlayers.addAll(this.damageByCreaturesPreventionLifeGainPlayers);
        copy.playerSourceNextDamageShields.addAll(this.playerSourceNextDamageShields);
        copy.sourceNextDamageToAnyTargetShields.addAll(this.sourceNextDamageToAnyTargetShields);
        copy.eyeForAnEyeShields.addAll(this.eyeForAnEyeShields);
        copy.reflectDamageToSourceControllerShields.addAll(this.reflectDamageToSourceControllerShields);
        copy.sourceNextCombatDamageToControllerShields.addAll(this.sourceNextCombatDamageToControllerShields);
        copy.sourceNextDamageRedirectToPermanentShields.addAll(this.sourceNextDamageRedirectToPermanentShields);
        copy.sourceRedirectShieldsUsedInOpenEvent.addAll(this.sourceRedirectShieldsUsedInOpenEvent);
        copy.playerSourceShieldsUsedInOpenEvent.addAll(this.playerSourceShieldsUsedInOpenEvent);
        copy.pendingEyeForAnEyeReflections.addAll(this.pendingEyeForAnEyeReflections);
        this.pendingSourceDamageForReflection.forEach((sourceId, pending) ->
                copy.pendingSourceDamageForReflection.put(sourceId, pending.copy()));
        copy.pendingLifelinkGains.addAll(this.pendingLifelinkGains);
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
        this.perpetualTriggeredAbilityGrants.forEach((cardId, effectsBySlot) -> {
            Map<EffectSlot, List<CardEffect>> copied = new EnumMap<>(EffectSlot.class);
            effectsBySlot.forEach((slot, effects) -> copied.put(slot, List.copyOf(effects)));
            copy.perpetualTriggeredAbilityGrants.put(cardId, Map.copyOf(copied));
        });
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
        this.pendingNextInstantSorceryCopyThisTurnDynamicSources.forEach((playerId, sources) ->
                copy.pendingNextInstantSorceryCopyThisTurnDynamicSources.put(playerId, new ArrayList<>(sources)));
        this.pendingNextInstantSorceryCopyThisTurnMaxManaValues.forEach((playerId, maxManaValues) ->
                copy.pendingNextInstantSorceryCopyThisTurnMaxManaValues.put(
                        playerId, new ArrayList<>(maxManaValues)));
        this.pendingNextInstantSorceryCopyThisTurnDynamicCounts.forEach((playerId, counts) ->
                copy.pendingNextInstantSorceryCopyThisTurnDynamicCounts.put(
                        playerId, new ArrayList<>(counts)));
        copy.pendingNextSpellCopyThisTurnCount.putAll(this.pendingNextSpellCopyThisTurnCount);
        this.pendingNextFilteredSpellCopiesThisTurn.forEach((playerId, effects) ->
                copy.pendingNextFilteredSpellCopiesThisTurn.put(playerId, new ArrayList<>(effects)));
        copy.pendingNextXActivatedAbilityCopyThisTurnCount
                .putAll(this.pendingNextXActivatedAbilityCopyThisTurnCount);
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
        this.ragingRiverBlockRestrictionsThisCombat.forEach((attackerId, restrictions) ->
                copy.ragingRiverBlockRestrictionsThisCombat.put(attackerId,
                        restrictions.stream().map(restriction -> Set.copyOf(restriction)).toList()));
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
        copy.playerMaximumLifeTotals.putAll(this.playerMaximumLifeTotals);
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
        copy.playerDamagePreventionShields.putAll(this.playerDamagePreventionShields);
        copy.playerCombatDamagePreventionShields.putAll(this.playerCombatDamagePreventionShields);
        copy.stolenCreatures.putAll(this.stolenCreatures);
        copy.cardOwnerOverrides.putAll(this.cardOwnerOverrides);
        copy.sacrificeForbiddenThisTurn.putAll(this.sacrificeForbiddenThisTurn);
        this.controlLossUnattachTriggers.forEach((equipmentId, triggers) ->
                copy.controlLossUnattachTriggers.put(equipmentId,
                        Collections.synchronizedList(new ArrayList<>(triggers))));
        this.controlLossTapTriggers.forEach((permanentId, triggers) ->
                copy.controlLossTapTriggers.put(permanentId,
                        Collections.synchronizedList(new ArrayList<>(triggers))));
        copy.drawReplacementTargetToController.putAll(this.drawReplacementTargetToController);
        copy.drawRedirectsAppliedToCurrentDraw.addAll(this.drawRedirectsAppliedToCurrentDraw);
        copy.chainsDrawReplacementsApplied.putAll(this.chainsDrawReplacementsApplied);
        copy.drawStepFirstDrawTaken.addAll(this.drawStepFirstDrawTaken);
        copy.firstNonDrawStepDrawReplacementsUsedThisTurn
                .addAll(this.firstNonDrawStepDrawReplacementsUsedThisTurn);
        this.pendingNextDrawLookAtTop.forEach((playerId, xValues) ->
                copy.pendingNextDrawLookAtTop.put(playerId,
                        Collections.synchronizedList(new ArrayList<>(xValues))));
        copy.pendingNextDrawGainLife.putAll(this.pendingNextDrawGainLife);
        this.pendingNextDrawCreateBears.forEach((playerId, sourceSetCodes) ->
                copy.pendingNextDrawCreateBears.put(playerId,
                        Collections.synchronizedList(new ArrayList<>(sourceSetCodes))));
        this.pendingNextDrawDamage.forEach((playerId, replacements) ->
                copy.pendingNextDrawDamage.put(playerId, Collections.synchronizedList(new ArrayList<>(replacements))));
        copy.pendingNextDrawReturnPermanents.putAll(this.pendingNextDrawReturnPermanents);
        copy.pendingNextDrawDiscardOpponents.putAll(this.pendingNextDrawDiscardOpponents);
        this.pendingNextDrawFromExiledPile.forEach((k, v) ->
                copy.pendingNextDrawFromExiledPile.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.pendingNextDrawFromOutsideGame.putAll(this.pendingNextDrawFromOutsideGame);
        copy.pendingNextDrawExileTopCard.putAll(this.pendingNextDrawExileTopCard);
        copy.pendingNextSchemeSetInMotionReplacements.putAll(this.pendingNextSchemeSetInMotionReplacements);
        copy.pendingMysticReflections.addAll(this.pendingMysticReflections);
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
        copy.pendingRevealedPowerCostCards.putAll(this.pendingRevealedPowerCostCards);
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
        copy.oncePerTurnTriggersFiredThisTurn.addAll(this.oncePerTurnTriggersFiredThisTurn);
        copy.permanentsWithCountersPutThisTurn.addAll(this.permanentsWithCountersPutThisTurn);
        copy.onceOnlyTriggersFired.addAll(this.onceOnlyTriggersFired);
        this.firstCardCycledFreeUsesThisTurn.forEach((k, v) ->
                copy.firstCardCycledFreeUsesThisTurn.put(k, new HashSet<>(v)));
        this.keyedOncePerTurnTriggersFiredThisTurn.forEach((k, v) -> {
            Set<String> keys = ConcurrentHashMap.newKeySet();
            keys.addAll(v);
            copy.keyedOncePerTurnTriggersFiredThisTurn.put(k, keys);
        });
        this.firstOpponentLifeLossTriggersFiredThisTurn.forEach((k, v) ->
                copy.firstOpponentLifeLossTriggersFiredThisTurn.put(k, new HashSet<>(v)));
        copy.survivalTriggersEvaluated.addAll(this.survivalTriggersEvaluated);
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
        copy.onceEachTurnAttackTriggersFiredThisTurn.addAll(this.onceEachTurnAttackTriggersFiredThisTurn);
        copy.permanentsExertedThisTurn.addAll(this.permanentsExertedThisTurn);
        this.bendingTypesCompletedThisTurn.forEach((k, v) ->
                copy.bendingTypesCompletedThisTurn.put(k, new java.util.HashSet<>(v)));
        copy.tokenCreationReplacementUsedThisTurn.addAll(this.tokenCreationReplacementUsedThisTurn);
        copy.soulEchoReplacementPlayers.addAll(this.soulEchoReplacementPlayers);
        copy.pendingTokenCreationReplacement = this.pendingTokenCreationReplacement;
        copy.simultaneousDyingCreatures.putAll(this.simultaneousDyingCreatures);
        copy.simultaneousDyingControllers.putAll(this.simultaneousDyingControllers);
        copy.simultaneousDyingPermanents.putAll(this.simultaneousDyingPermanents);
        this.departedPermanentSnapshots.forEach((cardId, permanent) ->
                copy.departedPermanentSnapshots.put(cardId, new Permanent(permanent)));
        copy.departedPermanentControllerIds.putAll(this.departedPermanentControllerIds);
        copy.waitingRedirectDamageSourceCards.putAll(this.waitingRedirectDamageSourceCards);
        copy.simultaneousDyingPermanentControllers.putAll(this.simultaneousDyingPermanentControllers);
        copy.simultaneousDyingPowers.putAll(this.simultaneousDyingPowers);
        copy.simultaneousDyingTriggerKeywordInstances.putAll(this.simultaneousDyingTriggerKeywordInstances);
        this.simultaneousDyingGrantedCreatureDeathEffects.forEach((permanentId, effects) ->
                copy.simultaneousDyingGrantedCreatureDeathEffects.put(
                        permanentId, List.copyOf(effects)));
        this.simultaneousDyingGrantedSelfDeathEffects.forEach((permanentId, effects) ->
                copy.simultaneousDyingGrantedSelfDeathEffects.put(permanentId, List.copyOf(effects)));
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
        copy.lukkaExileCastPermissions.putAll(this.lukkaExileCastPermissions);
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
        this.phasedOutUntilSourceLeaves.forEach((k, v) -> {
            Set<UUID> targetIds = ConcurrentHashMap.newKeySet();
            targetIds.addAll(v);
            copy.phasedOutUntilSourceLeaves.put(k, targetIds);
        });
        copy.tappedWhenSourceLeavesPhasesIn.addAll(this.tappedWhenSourceLeavesPhasesIn);
        copy.phasedOutUntilPlaneswalk.addAll(this.phasedOutUntilPlaneswalk);
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
        copy.damagedCreaturesCantRegenerateSourceIdsThisTurn.addAll(this.damagedCreaturesCantRegenerateSourceIdsThisTurn);
        copy.exileDamagedCreaturesSourceIdsThisTurn.addAll(this.exileDamagedCreaturesSourceIdsThisTurn);
        this.creatureCardsDamagedThisTurnBySource.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnBySource.put(k, new HashSet<>(v)));
        this.creatureCardsDamagedThisTurnByController.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnByController.put(k, new HashSet<>(v)));
        copy.sourcesThatDealtDamageToCreaturesThisTurn.addAll(this.sourcesThatDealtDamageToCreaturesThisTurn);
        copy.sourcesWhoseDamagedCreaturesDiedThisTurn.addAll(this.sourcesWhoseDamagedCreaturesDiedThisTurn);
        this.creatureCardsDamagedBySourceThatDiedThisTurn.forEach((k, v) ->
                copy.creatureCardsDamagedBySourceThatDiedThisTurn.put(k, new HashSet<>(v)));
        copy.creatureGivingControllerPoisonOnDeathThisTurn.putAll(this.creatureGivingControllerPoisonOnDeathThisTurn);
        this.creaturesReturnedToBattlefieldOnDeathThisTurn.forEach((k, v) ->
                copy.creaturesReturnedToBattlefieldOnDeathThisTurn.put(k, new ArrayList<>(v)));
        this.exploitedPermanentIdsBySource.forEach((k, v) ->
                copy.exploitedPermanentIdsBySource.put(k, new HashSet<>(v)));
        this.creatureTriggeringEffectOnDeathThisTurn.forEach((k, v) ->
                copy.creatureTriggeringEffectOnDeathThisTurn.put(k, new ArrayList<>(v)));
        this.permanentTriggeringEffectOnDeathThisTurn.forEach((k, v) ->
                copy.permanentTriggeringEffectOnDeathThisTurn.put(k, new ArrayList<>(v)));
        this.seraphReturnedCreatures.forEach((k, v) ->
                copy.seraphReturnedCreatures.put(k, new HashSet<>(v)));
        copy.seraphControlWatch.putAll(this.seraphControlWatch);
        copy.seraphLinkedController.putAll(this.seraphLinkedController);
        copy.exiledCardsToGraveyardOnControlLossWatch.putAll(this.exiledCardsToGraveyardOnControlLossWatch);
        this.sourceCreatedTokens.forEach((k, v) ->
                copy.sourceCreatedTokens.put(k, new HashSet<>(v)));

        // --- Map<UUID, Map<CardColor, Integer>> ---
        this.playerColorDamagePreventionCount.forEach((k, v) ->
                copy.playerColorDamagePreventionCount.put(k, new HashMap<>(v)));

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
        this.delayedActions.forEach(action -> {
            if (action instanceof DelayedControllerSpellCastTrigger delayed
                    && delayed.sourcePermanentSnapshot() != null) {
                copy.delayedActions.add(delayed.withSourcePermanentSnapshot(
                        new Permanent(delayed.sourcePermanentSnapshot()), delayed.sourcePowerAtLastKnown()));
            } else {
                copy.delayedActions.add(action);
            }
        });

        // --- Exile-until-source-leaves map (O-ring style) ---
        this.exileReturnOnPermanentLeave.forEach((k, v) ->
                copy.exileReturnOnPermanentLeave.put(k, new ArrayList<>(v)));
        copy.exiledCardReturnToHandOnPermanentLeave.putAll(this.exiledCardReturnToHandOnPermanentLeave);
        this.exileReturnOnOpponentBecomesMonarch.forEach((k, v) ->
                copy.exileReturnOnOpponentBecomesMonarch.put(k, new ArrayList<>(v)));

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---
        this.playerSourceDamagePreventionIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionIds.put(k, new HashSet<>(v)));
        this.playerSourceDamagePreventionLifeGainIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionLifeGainIds.put(k, new HashSet<>(v)));
        this.playerSourceDamagePreventionRequiredColors.forEach((k, v) -> {
            Map<UUID, Set<CardColor>> perSource = new ConcurrentHashMap<>();
            v.forEach((sourceId, colors) -> perSource.put(sourceId, new HashSet<>(colors)));
            copy.playerSourceDamagePreventionRequiredColors.put(k, perSource);
        });

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
        copy.graveyardTargetOperation.triggeringPermanentId =
                this.graveyardTargetOperation.triggeringPermanentId;
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
        copy.cloneOperation.additionalSlotEffects = this.cloneOperation.additionalSlotEffects;
        copy.cloneOperation.reflexiveEffects = this.cloneOperation.reflexiveEffects;
        copy.cloneOperation.shieldCounterIfControllerControlsCopiedPermanent =
                this.cloneOperation.shieldCounterIfControllerControlsCopiedPermanent;
        copy.cloneOperation.xValue = this.cloneOperation.xValue;
        copy.cloneOperation.graveyardCopyChoicePending = this.cloneOperation.graveyardCopyChoicePending;
        copy.cloneOperation.graveyardCopyEffect = this.cloneOperation.graveyardCopyEffect;
        copy.cloneOperation.exileCopiedGraveyardCardAfterEntry =
                this.cloneOperation.exileCopiedGraveyardCardAfterEntry;
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
        copy.permanentAbilityResolutionsThisTurn.putAll(this.permanentAbilityResolutionsThisTurn);

        // --- Deques ---
        this.pendingInteractions.forEach(pending -> copy.pendingInteractions.add(
                pending instanceof PendingInteraction.LandManaDrainContinuation drain
                        ? new PendingInteraction.LandManaDrainContinuation(drain.entry(), drain.remainingLandIds(),
                                drain.delayedColorless())
                        : pending instanceof PendingInteraction.ColorChoice choice
                        ? choice.copyCardTypeOnEnterPermanent()
                        : pending instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget trigger
                        ? trigger.copyPlanarSnapshot()
                        : pending instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger trigger
                        ? trigger.copyPlanarSnapshot() : pending));
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
            preventionSources.forEach((preventionId, amounts) -> copiedSources.put(preventionId, new HashMap<>(amounts)));
            copy.combatSharedDamagePreventionAllocations.put(sourceId, copiedSources);
        });
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
        copy.floatingEffectCreatingActivePlayerIds.putAll(this.floatingEffectCreatingActivePlayerIds);
        this.phaseSequenceEndOfTurnShares.forEach((objectId, shares) ->
                copy.phaseSequenceEndOfTurnShares.put(objectId, new LinkedHashMap<>(shares)));
        copy.phaseSequenceAttributedEndOfTurnState.putAll(this.phaseSequenceAttributedEndOfTurnState);
        copy.temporaryGraveyardCardAnimationsUntilEndOfTurn
                .putAll(this.temporaryGraveyardCardAnimationsUntilEndOfTurn);
        copy.perpetualCardPowerToughnessModifiers.putAll(this.perpetualCardPowerToughnessModifiers);
        copy.perpetualCardBasePowerToughness.putAll(this.perpetualCardBasePowerToughness);
        this.perpetualCardKeywords.forEach((cardId, keywords) ->
                copy.perpetualCardKeywords.put(cardId, keywords.isEmpty()
                        ? EnumSet.noneOf(Keyword.class) : EnumSet.copyOf(keywords)));
        this.perpetualCardSpellCastingAbilityGrants.forEach((cardId, grants) ->
                copy.perpetualCardSpellCastingAbilityGrants.put(cardId,
                        Collections.synchronizedList(new ArrayList<>(grants))));

        // --- Permanent no-max-hand-size grants ---
        copy.playersWithNoMaximumHandSize.addAll(this.playersWithNoMaximumHandSize);
        copy.playersWithNoMaximumHandSizeUntilNextTurn.addAll(this.playersWithNoMaximumHandSizeUntilNextTurn);
        copy.noMaximumHandSizeGrantTimestamps.putAll(this.noMaximumHandSizeGrantTimestamps);

        // --- Permanent "can't gain life" grants (Stigma Lasher) ---
        copy.playersWhoCantGainLifeRestOfGame.addAll(this.playersWhoCantGainLifeRestOfGame);
        copy.playersWithHandRevealed.addAll(this.playersWithHandRevealed);
        copy.playersWhoCantGainLifeThisTurn.addAll(this.playersWhoCantGainLifeThisTurn);
        copy.playersWhoGainNoLifeThisTurn.addAll(this.playersWhoGainNoLifeThisTurn);
        copy.playersWhoCantLoseLifeThisTurn.addAll(this.playersWhoCantLoseLifeThisTurn);

        // --- Source-linked animations (Awakener Druid-style) ---
        copy.sourceLinkedAnimations.putAll(this.sourceLinkedAnimations);

        // --- Per-player spell/creature color protection (Autumn's Veil style) ---
        this.playerSpellsCantBeCounteredByColorsThisTurn.forEach((k, v) ->
                copy.playerSpellsCantBeCounteredByColorsThisTurn.put(k, new HashSet<>(v)));
        copy.playersSpellsCantBeCounteredThisTurn.addAll(this.playersSpellsCantBeCounteredThisTurn);
        copy.playersCreatureSpellsCantBeCounteredThisTurn.addAll(this.playersCreatureSpellsCantBeCounteredThisTurn);
        this.playerCreaturesCantBeTargetedByColorsThisTurn.forEach((k, v) ->
                copy.playerCreaturesCantBeTargetedByColorsThisTurn.put(k, new HashSet<>(v)));
        this.playerHexproofFromColorsThisTurn.forEach((k, v) ->
                copy.playerHexproofFromColorsThisTurn.put(k, new HashSet<>(v)));
        this.permanentHexproofFromColorsThisTurn.forEach((k, v) ->
                copy.permanentHexproofFromColorsThisTurn.put(k, new HashSet<>(v)));
        copy.spellsMadeUncounterable.addAll(this.spellsMadeUncounterable);
        this.spellTextReplacements.forEach((k, v) -> copy.spellTextReplacements.put(k, new ArrayList<>(v)));
        this.spellColorOverrides.forEach((k, v) -> copy.spellColorOverrides.put(k, new HashSet<>(v)));
        this.spellColorOverridesUntilEndOfTurn.forEach((k, v) ->
                copy.spellColorOverridesUntilEndOfTurn.put(k, new HashSet<>(v)));
        this.playerProtectionFromColorsUntilEndOfTurn.forEach((k, v) ->
                copy.playerProtectionFromColorsUntilEndOfTurn.put(k, new HashSet<>(v)));
        this.playerProtectionFromPlayerIdsUntilEndOfTurn.forEach((k, v) ->
                copy.playerProtectionFromPlayerIdsUntilEndOfTurn.put(k, new HashSet<>(v)));
        this.playerProtectionFromPlayerIdsUntilNextTurn.forEach((k, v) ->
                copy.playerProtectionFromPlayerIdsUntilNextTurn.put(k, new HashSet<>(v)));
        this.playerKeywordsUntilEndOfTurn.forEach((k, v) ->
                copy.playerKeywordsUntilEndOfTurn.put(k, new HashSet<>(v)));
        this.playerKeywordsUntilNextTurn.forEach((k, v) ->
                copy.playerKeywordsUntilNextTurn.put(k, new HashSet<>(v)));
        this.playerStaticEffectsUntilEndOfTurn.forEach((k, v) ->
                copy.playerStaticEffectsUntilEndOfTurn.put(k, new ArrayList<>(v)));

        // --- Silence-style "opponents can't cast" flag ---
        copy.playersSilencedThisTurn.addAll(this.playersSilencedThisTurn);
        copy.playersMaxSpellsThisTurn.putAll(this.playersMaxSpellsThisTurn);
        copy.playersMaxSpellsContributionsThisTurn.addAll(this.playersMaxSpellsContributionsThisTurn);
        copy.playersCantCastSpellsForRestOfGame.addAll(this.playersCantCastSpellsForRestOfGame);
        this.opponentsCantCastNamedSpellsUntilControllerNextTurn.forEach((k, v) ->
                copy.opponentsCantCastNamedSpellsUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.opponentsCantCastSpellsWithManaValueUntilControllerNextTurn.forEach((k, v) ->
                copy.opponentsCantCastSpellsWithManaValueUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantCastNamedSpellsUntilControllerNextTurn.forEach((controllerId, restrictions) -> {
            Map<UUID, Set<String>> copiedRestrictions = new ConcurrentHashMap<>();
            restrictions.forEach((playerId, names) -> copiedRestrictions.put(playerId, new HashSet<>(names)));
            copy.playersCantCastNamedSpellsUntilControllerNextTurn.put(controllerId, copiedRestrictions);
        });
        this.spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn.forEach((k, v) ->
                copy.spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantCastNoncreatureSpellsUntilControllerNextTurn.forEach((k, v) ->
                copy.playersCantCastNoncreatureSpellsUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantCastSpellsFromOutsideHandUntilControllerNextTurn.forEach((k, v) ->
                copy.playersCantCastSpellsFromOutsideHandUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantPlayCardsFromHandUntilControllerNextTurn.forEach((k, v) ->
                copy.playersCantPlayCardsFromHandUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantCastSpellTypesUntilEndOfControllerNextTurn.forEach((k, v) ->
                copy.playersCantCastSpellTypesUntilEndOfControllerNextTurn.put(k, new HashMap<>(v)));
        copy.cardsRevealedInHandUntilOwnerNextTurn.putAll(this.cardsRevealedInHandUntilOwnerNextTurn);
        copy.cardsRevealedInHandUntilUpkeepEnd.putAll(this.cardsRevealedInHandUntilUpkeepEnd);
        copy.cardsCantBePlayedInHandUntilOwnerNextTurn.putAll(this.cardsCantBePlayedInHandUntilOwnerNextTurn);
        this.extraManaOnLandSubtypeTapThisTurn.forEach((subtype, bonuses) ->
                copy.extraManaOnLandSubtypeTapThisTurn.put(subtype, new ArrayList<>(bonuses)));
        copy.landSubtypeFixedManaColorThisTurn.putAll(this.landSubtypeFixedManaColorThisTurn);
        copy.nonbasicLandsFixedManaColorThisTurn = this.nonbasicLandsFixedManaColorThisTurn;
        copy.allLandsFixedManaColorThisTurn = this.allLandsFixedManaColorThisTurn;
        copy.playersWithLandManaChoiceReplacementThisTurn.addAll(this.playersWithLandManaChoiceReplacementThisTurn);
        copy.landManaFixedColorThisTurn.putAll(this.landManaFixedColorThisTurn);
        copy.playersWhoTappedLandForManaThisTurn.addAll(this.playersWhoTappedLandForManaThisTurn);
        copy.playersCantPlayLandsThisTurn.addAll(this.playersCantPlayLandsThisTurn);
        copy.playersCantCastSpellTypesThisTurn.putAll(this.playersCantCastSpellTypesThisTurn);
        this.playersCantCastSpellTypesNextTurn.forEach((k, v) ->
                copy.playersCantCastSpellTypesNextTurn.put(k, new HashSet<>(v)));
        copy.playersCantCastNoncreatureSpellsThisTurn.addAll(this.playersCantCastNoncreatureSpellsThisTurn);
        copy.playersCantActivateAbilitiesThisTurn.addAll(this.playersCantActivateAbilitiesThisTurn);
        copy.playersCantActivateNonManaAbilitiesThisTurn.addAll(this.playersCantActivateNonManaAbilitiesThisTurn);
        copy.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn.addAll(
                this.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn);
        copy.playerExtraLoyaltyActivationsThisTurn.putAll(this.playerExtraLoyaltyActivationsThisTurn);
        copy.playersAllowedTwiceLoyaltyActivationsThisTurn.addAll(this.playersAllowedTwiceLoyaltyActivationsThisTurn);
        copy.counteredSpellManaCosts.putAll(this.counteredSpellManaCosts);
        copy.senControllerPlayerId = this.senControllerPlayerId;
        copy.senControlledPlayerId = this.senControlledPlayerId;

        // --- Spell copy until end of turn (The Mirari Conjecture chapter III) ---
        copy.spellCopyGrantsUntilEndOfTurn.putAll(this.spellCopyGrantsUntilEndOfTurn);
        copy.conspiredSpellCopyCounts.putAll(this.conspiredSpellCopyCounts);
        copy.casualtySpellCopyCounts.putAll(this.casualtySpellCopyCounts);

        // --- Pending one-shot spell copy triggers (Primal Wellspring) ---
        copy.pendingNextInstantSorceryCopyCount.putAll(this.pendingNextInstantSorceryCopyCount);
        copy.pendingPathOfAncestryManaChoices.putAll(this.pendingPathOfAncestryManaChoices);
        copy.pendingNextRedInstantSorceryCopyCount.putAll(this.pendingNextRedInstantSorceryCopyCount);
        copy.pendingNextInstantSorceryCopyThisTurnCount.putAll(this.pendingNextInstantSorceryCopyThisTurnCount);
        copy.pendingNextInstantSorceryCopyThisTurnCountSource.putAll(this.pendingNextInstantSorceryCopyThisTurnCountSource);
        copy.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.putAll(this.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount);
        copy.pendingNextInstantSorceryStormThisTurnCount.putAll(this.pendingNextInstantSorceryStormThisTurnCount);
        copy.pendingNextInstantSorceryCastFromHandToHandThisTurnCount
                .putAll(this.pendingNextInstantSorceryCastFromHandToHandThisTurnCount);
        copy.pendingNextSpellUncounterableThisTurnCount.putAll(this.pendingNextSpellUncounterableThisTurnCount);
        copy.pendingAnyManaTypeForNextSpellThisTurnCount.putAll(this.pendingAnyManaTypeForNextSpellThisTurnCount);
        copy.spellsPaidUsingPendingAnyManaTypeThisTurn.addAll(this.spellsPaidUsingPendingAnyManaTypeThisTurn);
        copy.pendingNextInstantSorceryUncounterableThisTurnCount.putAll(
                this.pendingNextInstantSorceryUncounterableThisTurnCount);
        copy.pendingNextLoyaltyAbilityCopyThisTurnCount.putAll(this.pendingNextLoyaltyAbilityCopyThisTurnCount);
        copy.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.addAll(this.pendingNextSpellOrLoyaltyCopyLinkedThisTurn);
        this.pendingNextLoyaltyAbilityCopySourcesThisTurn.forEach((playerId, sources) ->
                copy.pendingNextLoyaltyAbilityCopySourcesThisTurn.put(playerId, new ArrayList<>(sources)));
        copy.pendingNextXActivatedAbilityCopyThisTurnCount
                .putAll(this.pendingNextXActivatedAbilityCopyThisTurnCount);
        copy.temporaryChosenSubtypePermanentIds.addAll(this.temporaryChosenSubtypePermanentIds);
        copy.chosenSubtypeActivationCopiesThisTurn.addAll(this.chosenSubtypeActivationCopiesThisTurn);
        copy.pendingNextExhaustAbilityCopyThisTurnCount.putAll(this.pendingNextExhaustAbilityCopyThisTurnCount);
        this.pendingNextExhaustAbilityCopySourcesThisTurn.forEach((playerId, sources) ->
                copy.pendingNextExhaustAbilityCopySourcesThisTurn.put(playerId, new ArrayList<>(sources)));
        this.creatureEntersDrawSourcesThisTurn.forEach((playerId, cards) ->
                copy.creatureEntersDrawSourcesThisTurn.put(playerId, new ArrayList<>(cards)));
        this.creatureEntersDrawSources.forEach((playerId, cards) ->
                copy.creatureEntersDrawSources.put(playerId, new ArrayList<>(cards)));
        this.untapAttackedCreaturesEachCombatThisTurnSources.forEach((playerId, cards) ->
                copy.untapAttackedCreaturesEachCombatThisTurnSources.put(playerId, new ArrayList<>(cards)));

        copy.libraryTopCardFreePlayPermissionsUntilEndOfTurn.putAll(this.libraryTopCardFreePlayPermissionsUntilEndOfTurn);
        copy.libraryTopCardPermissionsUntilEndOfTurn.addAll(this.libraryTopCardPermissionsUntilEndOfTurn);
        copy.exilePlayPermissions.putAll(this.exilePlayPermissions);
        copy.exileLookPermissions.putAll(this.exileLookPermissions);
        this.additionalExileLookPermissions.forEach((cardId, viewers) ->
                copy.additionalExileLookPermissions.put(cardId, new HashSet<>(viewers)));
        copy.outsideGamePlayPermissions.copyFrom(this.outsideGamePlayPermissions);
        copy.outsideGameAdditionalModalModePermissions.addAll(this.outsideGameAdditionalModalModePermissions);
        copy.playersAllowedToPlayFromLibraryTopUntilEndOfTurn
                .addAll(this.playersAllowedToPlayFromLibraryTopUntilEndOfTurn);
        copy.libraryTopCardLifePlayPermissionsUntilEndOfTurn.addAll(this.libraryTopCardLifePlayPermissionsUntilEndOfTurn);
        copy.exilePlayPermissionGroups.putAll(this.exilePlayPermissionGroups);
        this.exileSpellTypePermissionHistory.forEach((groupId, history) ->
                copy.exileSpellTypePermissionHistory.put(groupId, history.stream()
                        .map(types -> Set.copyOf(types)).collect(java.util.stream.Collectors.toCollection(ArrayList::new))));
        copy.exilePlayPermissionGroupUsesRemaining.putAll(this.exilePlayPermissionGroupUsesRemaining);
        copy.exilePlayPermissionConditions.putAll(this.exilePlayPermissionConditions);
        copy.exilePlayPermissionSpellFilters.putAll(this.exilePlayPermissionSpellFilters);
        copy.exilePlayForLifeEqualToManaValue.addAll(this.exilePlayForLifeEqualToManaValue);
        copy.exilePlayPermissionSourceCards.putAll(this.exilePlayPermissionSourceCards);
        copy.exilePlayPermissionSourcePermanents.putAll(this.exilePlayPermissionSourcePermanents);
        copy.exilePlayPermissionStaticSourcePermanents.putAll(this.exilePlayPermissionStaticSourcePermanents);
        copy.exilePlayPermissionLoreCounterSagas.putAll(this.exilePlayPermissionLoreCounterSagas);
        copy.exilePlayCostModifiers.putAll(this.exilePlayCostModifiers);
        copy.exilePlayUntilEndOfTurnGrantsBySource.putAll(this.exilePlayUntilEndOfTurnGrantsBySource);
        copy.exilePlayPermissionsExpireEndOfTurn.addAll(this.exilePlayPermissionsExpireEndOfTurn);
        copy.exilePlayPermissionsExpireAtTurnEnd.putAll(this.exilePlayPermissionsExpireAtTurnEnd);
        copy.exilePlayPermissionsExpireAtEndStep.addAll(this.exilePlayPermissionsExpireAtEndStep);
        copy.exilePlayPermissionsAwaitNextTurnOfPlayer.putAll(this.exilePlayPermissionsAwaitNextTurnOfPlayer);
        copy.exilePlayPermissionsDuringOwnersNextTurnOnly.addAll(this.exilePlayPermissionsDuringOwnersNextTurnOnly);
        copy.exilePlayPermissionsExpireAtTurnBeginning.putAll(this.exilePlayPermissionsExpireAtTurnBeginning);
        copy.exilePlayAnyManaType.addAll(this.exilePlayAnyManaType);
        copy.exilePlayAnyManaTypeWhileExiled.addAll(this.exilePlayAnyManaTypeWhileExiled);
        copy.discordCopySourcePermanents.putAll(this.discordCopySourcePermanents);
        copy.stashCounterCardIds.addAll(this.stashCounterCardIds);
        copy.exilePlayWithoutPayingManaCost.addAll(this.exilePlayWithoutPayingManaCost);
        copy.exileCardsEnterTapped.addAll(this.exileCardsEnterTapped);
        copy.exileInsteadOfGraveyard.addAll(this.exileInsteadOfGraveyard);
        copy.graveyardPlayPermissions.putAll(this.graveyardPlayPermissions);
        copy.graveyardPlayPermissionsExpireEndOfTurn.addAll(this.graveyardPlayPermissionsExpireEndOfTurn);
        copy.graveyardCardsEnterTapped.addAll(this.graveyardCardsEnterTapped);
        copy.graveyardCastFilterPermissionsThisTurn.addAll(this.graveyardCastFilterPermissionsThisTurn);
        copy.playersCantPlayFromGraveyardsThisTurn.addAll(this.playersCantPlayFromGraveyardsThisTurn);
        copy.graveyardPlayFilterPermissionsThisTurn.addAll(this.graveyardPlayFilterPermissionsThisTurn);
        copy.exileCastPermissionsUntilEndOfTurn.addAll(this.exileCastPermissionsUntilEndOfTurn);
        copy.exileCastWildcardPermissionsUntilEndOfTurn.addAll(this.exileCastWildcardPermissionsUntilEndOfTurn);
        copy.foretoldCardCastPermissionsThisTurn.addAll(this.foretoldCardCastPermissionsThisTurn);
        copy.playersWhoForetoldThisTurn.addAll(this.playersWhoForetoldThisTurn);
        copy.playersExilingCardsInsteadOfGraveyardThisTurn.addAll(this.playersExilingCardsInsteadOfGraveyardThisTurn);
        copy.playersMayPlayFaceUpCardsFromExileThisTurn.addAll(this.playersMayPlayFaceUpCardsFromExileThisTurn);
        copy.playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn
                .addAll(this.playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn);
        copy.graveyardLeaveNotificationDepth = this.graveyardLeaveNotificationDepth;
        copy.graveyardLeaveNotificationPendingOwners.addAll(this.graveyardLeaveNotificationPendingOwners);
        this.graveyardLeaveNotificationPendingCards.forEach((playerId, cards) ->
                copy.graveyardLeaveNotificationPendingCards.put(playerId, new ArrayList<>(cards)));
        this.graveyardLeaveNotificationPendingInstantOrSorceryCards.forEach((playerId, cards) ->
                copy.graveyardLeaveNotificationPendingInstantOrSorceryCards.put(playerId, new ArrayList<>(cards)));
        copy.graveyardLeaveNotificationPendingCreatureOwners.addAll(this.graveyardLeaveNotificationPendingCreatureOwners);
        copy.graveyardLeaveNotificationPendingCreatureCardCounts.putAll(this.graveyardLeaveNotificationPendingCreatureCardCounts);
        this.graveyardLeaveNotificationPendingCreatureCards.forEach((playerId, cards) ->
                copy.graveyardLeaveNotificationPendingCreatureCards.put(playerId, new ArrayList<>(cards)));
        copy.graveyardLeaveNotificationPendingArtifactOrCreatureOwners.addAll(this.graveyardLeaveNotificationPendingArtifactOrCreatureOwners);
        copy.graveyardLeaveNotificationPendingArtifactOwners.addAll(this.graveyardLeaveNotificationPendingArtifactOwners);
        this.graveyardLeaveNotificationPendingArtifactOrCreatureCards.forEach((playerId, cards) ->
                copy.graveyardLeaveNotificationPendingArtifactOrCreatureCards.put(playerId, new ArrayList<>(cards)));
        copy.graveyardExileNotificationPendingCounts.putAll(this.graveyardExileNotificationPendingCounts);
        this.kayaExileNotificationPendingCreatureCards.forEach((playerId, cards) ->
                copy.kayaExileNotificationPendingCreatureCards.put(playerId, new ArrayList<>(cards)));
        copy.kayaExileNotificationPendingCounts.putAll(this.kayaExileNotificationPendingCounts);
        copy.graveyardOrBattlefieldExileNotificationPending = this.graveyardOrBattlefieldExileNotificationPending;
        copy.permanentTapTriggerBatchDepth = this.permanentTapTriggerBatchDepth;
        this.permanentTapTriggerBatchFiredEffects.forEach((sourceId, effects) -> {
            Set<CardEffect> copiedEffects = ConcurrentHashMap.newKeySet();
            copiedEffects.addAll(effects);
            copy.permanentTapTriggerBatchFiredEffects.put(sourceId, copiedEffects);
        });
        copy.playersWhoseCardsLeftGraveyardThisTurn.addAll(this.playersWhoseCardsLeftGraveyardThisTurn);
        copy.playersWhoseCreatureCardsLeftGraveyardThisTurn
                .addAll(this.playersWhoseCreatureCardsLeftGraveyardThisTurn);
        copy.cardsLeftGraveyardCountThisTurn.putAll(this.cardsLeftGraveyardCountThisTurn);
        copy.permanentLeaveNotificationDepth = this.permanentLeaveNotificationDepth;
        copy.permanentLeaveBatchWatchers.putAll(this.permanentLeaveBatchWatchers);
        copy.permanentLeaveBatchWatcherControllers.putAll(this.permanentLeaveBatchWatcherControllers);
        copy.permanentLeaveBatchPendingCreatures.putAll(this.permanentLeaveBatchPendingCreatures);
        copy.permanentLeaveBatchPendingPermanents.putAll(this.permanentLeaveBatchPendingPermanents);
        copy.deathReplacementChoiceBatchDepth = this.deathReplacementChoiceBatchDepth;
        copy.deferredDeathReplacementChoices.addAll(this.deferredDeathReplacementChoices);
        copy.deathReplacementsCanRerunEffect = this.deathReplacementsCanRerunEffect;
        copy.coinFlipChoicesCanRerunEffect = this.coinFlipChoicesCanRerunEffect;
        copy.recordedCoinFlips.addAll(this.recordedCoinFlips);
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
        copy.pendingTurnControl.putAll(this.pendingTurnControl);
        copy.pendingTurnControlExtraTurn.addAll(this.pendingTurnControlExtraTurn);
        copy.pendingCombatControl.putAll(this.pendingCombatControl);
        copy.mindControlledPlayerId = this.mindControlledPlayerId;
        copy.mindControllerPlayerId = this.mindControllerPlayerId;
        copy.mindControlUntilEndOfCombat = this.mindControlUntilEndOfCombat;
        copy.wordOfCommandControllerPlayerId = this.wordOfCommandControllerPlayerId;
        copy.wordOfCommandControlledPlayerId = this.wordOfCommandControlledPlayerId;
        copy.wordOfCommandCardId = this.wordOfCommandCardId;
        copy.wordOfCommandPendingResolutionIndex = this.wordOfCommandPendingResolutionIndex;
        copy.wordOfCommandCastingCard = this.wordOfCommandCastingCard;
        copy.wordOfCommandAwaitingCardResolution = this.wordOfCommandAwaitingCardResolution;
        copy.tauntedNextTurn.putAll(this.tauntedNextTurn);
        copy.tauntedThisTurn.putAll(this.tauntedThisTurn);
        copy.creatureMustAttackPermanentNextTurn.putAll(this.creatureMustAttackPermanentNextTurn);
        this.chosenAttackersNextTurn.forEach((playerId, ids) -> copy.chosenAttackersNextTurn.put(playerId, Set.copyOf(ids)));
        this.chosenAttackersToDestroyNextTurn.forEach((playerId, ids) ->
                copy.chosenAttackersToDestroyNextTurn.put(playerId, new ArrayList<>(ids)));
        this.chosenAttackersThisTurn.forEach((playerId, ids) -> copy.chosenAttackersThisTurn.put(playerId, Set.copyOf(ids)));
        this.attackableCreaturesThisTurn.forEach((playerId, ids) -> copy.attackableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        this.creaturesAbleToAttackAtDeclareAttackersThisTurn.forEach((playerId, ids) ->
                copy.creaturesAbleToAttackAtDeclareAttackersThisTurn.put(playerId, Set.copyOf(ids)));
        this.blockableCreaturesThisTurn.forEach((playerId, ids) -> copy.blockableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        copy.otherCreaturesCantAttackExemptCreatureIds.addAll(this.otherCreaturesCantAttackExemptCreatureIds);
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
        copy.skipNextCombatPhaseCount.putAll(this.skipNextCombatPhaseCount);
        copy.skipCombatPhaseExpirationsThisTurn.putAll(this.skipCombatPhaseExpirationsThisTurn);
        copy.skipCombatPhasesNextTurn.addAll(this.skipCombatPhasesNextTurn);
        this.matchingCreatureBlockRestrictionsThisTurn.forEach((id, restrictions) ->
                copy.matchingCreatureBlockRestrictionsThisTurn.put(id, new ArrayList<>(restrictions)));
        this.counterHolderMustBlockRequirementsThisTurn.forEach((id, counterTypes) ->
                copy.counterHolderMustBlockRequirementsThisTurn.put(id, new HashSet<>(counterTypes)));
        this.matchingPermanentUntapRestrictions.forEach((id, restrictions) ->
                copy.matchingPermanentUntapRestrictions.put(id, new ArrayList<>(restrictions)));
        copy.skipNextDrawStepCount.putAll(this.skipNextDrawStepCount);
        copy.skipDrawStepThisTurn.putAll(this.skipDrawStepThisTurn);
        copy.skipNextTurnCount.putAll(this.skipNextTurnCount);
        copy.skipNextUntapStepCount.putAll(this.skipNextUntapStepCount);
        this.skippedStepOrPhasesThisTurn.forEach((playerId, kinds) -> {
            Set<SkipStepOrPhaseKind> copiedKinds = ConcurrentHashMap.newKeySet();
            copiedKinds.addAll(kinds);
            copy.skippedStepOrPhasesThisTurn.put(playerId, copiedKinds);
        });
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
        copy.spellCastPathOfAncestryManaSpent.putAll(this.spellCastPathOfAncestryManaSpent);
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
        copy.cardsGrantedFlashbackUntilEndOfTurn.addAll(this.cardsGrantedFlashbackUntilEndOfTurn);
        copy.cardsGrantedFlashbackDespiteLostAbilitiesUntilEndOfTurn.addAll(
                this.cardsGrantedFlashbackDespiteLostAbilitiesUntilEndOfTurn);
        copy.cardsGrantedFlashbackCostsUntilEndOfTurn.putAll(this.cardsGrantedFlashbackCostsUntilEndOfTurn);
        copy.cardsGrantedWarpUntilEndOfTurn.addAll(this.cardsGrantedWarpUntilEndOfTurn);
        copy.warpExileEntryVersions.putAll(this.warpExileEntryVersions);
        copy.cardsGrantedHarmonizeUntilEndOfTurn.addAll(this.cardsGrantedHarmonizeUntilEndOfTurn);
        copy.cardsGrantedJumpStartUntilEndOfTurn.addAll(this.cardsGrantedJumpStartUntilEndOfTurn);
        copy.cardsGrantedEmbalmUntilEndOfTurn.addAll(this.cardsGrantedEmbalmUntilEndOfTurn);
        copy.cardsGrantedUnearthUntilEndOfTurn.putAll(this.cardsGrantedUnearthUntilEndOfTurn);
        copy.cardsGrantedUnearthGraveyardVersions.putAll(this.cardsGrantedUnearthGraveyardVersions);
        copy.playersWithFlashUntilEndOfTurn.addAll(this.playersWithFlashUntilEndOfTurn);
        copy.playersWithFreeHandCastUntilEndOfTurn.addAll(this.playersWithFreeHandCastUntilEndOfTurn);
        copy.playersWhoMayLookAtFaceDownCreaturesThisTurn
                .addAll(this.playersWhoMayLookAtFaceDownCreaturesThisTurn);
        this.cardTypeFlashGrantsThisTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsThisTurn.put(k, ConcurrentHashMap.newKeySet()));
        this.cardTypeFlashGrantsThisTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsThisTurn.get(k).addAll(v));
        this.cardTypeFlashGrantsUntilNextTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsUntilNextTurn.put(k, ConcurrentHashMap.newKeySet()));
        this.cardTypeFlashGrantsUntilNextTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsUntilNextTurn.get(k).addAll(v));
        this.nextSpellFlashGrantsThisTurn.forEach((k, v) ->
                copy.nextSpellFlashGrantsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.nextSpellChosenSubtypeFlashGrantsThisTurn.forEach((k, v) ->
                copy.nextSpellChosenSubtypeFlashGrantsThisTurn.put(k,
                        Collections.synchronizedList(new ArrayList<>(v))));
        copy.nextSpellConvokeGrantsThisTurn.putAll(this.nextSpellConvokeGrantsThisTurn);
        this.nextSpellCostReductionsThisTurn.forEach((k, v) ->
                copy.nextSpellCostReductionsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.nextSpellPayLifeEqualToManaValueThisTurn.putAll(this.nextSpellPayLifeEqualToManaValueThisTurn);
        this.nextSpellFreeCastPermissionsThisTurn.forEach((k, v) ->
                copy.nextSpellFreeCastPermissionsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.nextCreatureSpellEmpowermentsThisTurn.forEach((k, v) ->
                copy.nextCreatureSpellEmpowermentsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.nextCreatureSpellCascadeThisTurn.putAll(this.nextCreatureSpellCascadeThisTurn);
        this.nextCreatureSpellDelayedTriggersThisTurn.forEach((k, v) ->
                copy.nextCreatureSpellDelayedTriggersThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.nextCreatureSpellEmpowerments.forEach((k, v) ->
                copy.nextCreatureSpellEmpowerments.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.spellAdditionalEnterCounters.putAll(this.spellAdditionalEnterCounters);
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
        copy.mayTapLandsForSpellsUntilEndOfTurn.addAll(this.mayTapLandsForSpellsUntilEndOfTurn);
        copy.mayPayLifeForColorlessManaUntilEndOfTurn.addAll(this.mayPayLifeForColorlessManaUntilEndOfTurn);
        this.guardianAngelTargetsUntilEndOfTurn.forEach((controllerId, targetIds) -> {
            Set<UUID> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(targetIds);
            copy.guardianAngelTargetsUntilEndOfTurn.put(controllerId, copied);
        });
        copy.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn
                .addAll(this.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn);
        copy.graveyardCardCastPermissionsUntilEndOfTurn.putAll(this.graveyardCardCastPermissionsUntilEndOfTurn);
        this.graveyardCardCastPermissionGrantsUntilEndOfTurn.forEach((cardId, grants) ->
                copy.graveyardCardCastPermissionGrantsUntilEndOfTurn.put(cardId, new ArrayList<>(grants)));
        copy.graveyardAdventureCastPermissions.putAll(this.graveyardAdventureCastPermissions);

        // --- Damage prevention / redirection still pending ---
        copy.permanentsPreventedFromDealingDamage.addAll(this.permanentsPreventedFromDealingDamage);
        copy.creaturesProtectedFromTargetingDamage.addAll(this.creaturesProtectedFromTargetingDamage);
        copy.targetSpellDamagePreventionShields.addAll(this.targetSpellDamagePreventionShields);
        copy.targetSorceryDamageRedirectShields.addAll(this.targetSorceryDamageRedirectShields);
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

        copyDamageBonusGrants(this.controllerNoncombatDamageBonusThisTurn, copy.controllerNoncombatDamageBonusThisTurn);
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
        copy.cardsGrantedFlashbackWithoutPayingManaCostUntilEndOfTurn.addAll(
                this.cardsGrantedFlashbackWithoutPayingManaCostUntilEndOfTurn);

        Map<UUID, Permanent> copiedCombatPermanents = new HashMap<>();
        copy.playerBattlefields.values().forEach(permanents -> permanents.forEach(
                permanent -> copiedCombatPermanents.put(permanent.getId(), permanent)));
        copy.phasedOutPermanents.values().forEach(permanents -> permanents.forEach(
                permanent -> copiedCombatPermanents.put(permanent.getId(), permanent)));
        java.util.function.Function<Permanent, Permanent> copyCombatPermanent = permanent ->
                permanent == null ? null : copiedCombatPermanents.computeIfAbsent(
                        permanent.getId(), ignored -> new Permanent(permanent));
        copy.pendingCombatDamageState = copyCombatDamageState(this.pendingCombatDamageState, copyCombatPermanent);
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
        }
    }

    /** Records one damage bonus grant under {@code key} in a grant map such as {@link #controllerDamageBonusThisTurn}. */
    public static void addDamageBonusGrant(Map<UUID, List<DamageBonusGrant>> grants, UUID key, DamageBonusGrant grant) {
        grants.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(grant);
    }

    private static void copyDamageBonusGrants(Map<UUID, List<DamageBonusGrant>> from,
                                              Map<UUID, List<DamageBonusGrant>> to) {
        from.forEach((key, grants) -> to.put(key, new CopyOnWriteArrayList<>(grants)));
    }
}
