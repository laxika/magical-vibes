package com.github.laxika.magicalvibes.model;


import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.github.laxika.magicalvibes.model.action.DelayedAction;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

public class GameData {

    public final UUID id;
    public final String gameName;
    public final UUID createdByUserId;
    public final String createdByUsername;
    public final LocalDateTime createdAt;
    public volatile GameStatus status;
    /** "All Random" game mode: every player is dealt a randomly generated deck. */
    public volatile boolean allRandom;
    /** For an {@link #allRandom} game: the set code the random decks draw from, or {@code null} for all sets. */
    public volatile String randomSetCode;
    public final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
    public final List<UUID> orderedPlayerIds = Collections.synchronizedList(new ArrayList<>());
    public final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, String> playerIdToName = new ConcurrentHashMap<>();
    public final Map<UUID, String> playerDeckChoices = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerDecks = new ConcurrentHashMap<>();
    /** Cards owned by each player that began outside the game, such as a sideboard. */
    public final Map<UUID, List<Card>> playerSideboards = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerHands = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> mulliganCounts = new ConcurrentHashMap<>();
    public final Set<UUID> playerKeptHand = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerNeedsToBottom = new ConcurrentHashMap<>();
    /** Stable identities for the currently open pregame decisions. */
    public final Map<UUID, UUID> playerMulliganDecisionIds = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> playerBottomDecisionIds = new ConcurrentHashMap<>();
    public final List<GameLogEntry> gameLog = Collections.synchronizedList(new ArrayList<>());
    public UUID startingPlayerId;
    public TurnStep currentStep;
    public UUID activePlayerId;
    public int turnNumber;
    /** Whether the turn currently in progress was taken from the extra-turn queue. */
    public boolean currentTurnIsExtraTurn;
    /**
     * Number of turns each player has taken so far this game, including the turn currently in progress
     * and any extra turns. Used by cards that speak of "your first, second, or third turns of the game".
     */
    public final Map<UUID, Integer> turnsTakenByPlayer = new ConcurrentHashMap<>();
    /** Final authoritative runtime result; null until the game has ended. */
    public GameEventFact.GameResult gameResult;
    public UUID winnerPlayerId;
    public final Set<UUID> priorityPassedBy = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> landsPlayedThisTurn = new ConcurrentHashMap<>();
    /** Extra land plays granted this turn (e.g. Summer Bloom), on top of the normal one-per-turn. */
    public final Map<UUID, Integer> additionalLandsThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Snapshot of permanents that entered under each player's control during the immediately preceding turn. */
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldLastTurn = new ConcurrentHashMap<>();
    /** All spells cast by each player this turn. Access via {@link #recordSpellCast}, {@link #getSpellsCastThisTurnCount}, etc. */
    private final Map<UUID, List<Card>> spellsCastThisTurn = new ConcurrentHashMap<>();
    /** Players whose creature spell was countered by an opponent this turn (Summoning Trap). */
    public final Set<UUID> playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who searched their own libraries this turn, including searches that found no card. */
    public final Set<UUID> playersWhoSearchedLibraryThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have received the city's blessing for the rest of the game. */
    public final Set<UUID> playersWithCityBlessing = ConcurrentHashMap.newKeySet();
    /** Players who played at least one card from exile this turn. */
    public final Set<UUID> playersWhoPlayedCardFromExileThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who investigated this turn. */
    public final Set<UUID> playersWhoInvestigatedThisTurn = ConcurrentHashMap.newKeySet();
    /** Counts permanents sacrificed by subtype and controller this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> sacrificedPermanentSubtypeCountThisTurn = new ConcurrentHashMap<>();
    /** Players who surveilled at least once this turn. */
    public final Set<UUID> playersWhoSurveilledThisTurn = ConcurrentHashMap.newKeySet();
    /**
     * Card IDs of every spell cast this turn by any player, in cast order. Unlike
     * {@link #spellsCastThisTurn} this preserves the global ordering across players, which is what
     * "the second spell cast this turn" (Second Guess) needs. Read via
     * {@link #getSpellCastOrdinalThisTurn}.
     */
    private final List<UUID> spellCastOrderThisTurn = Collections.synchronizedList(new ArrayList<>());
    /** The spell most recently cast by any player this turn, regardless of controller. */
    private Card mostRecentSpellCastThisTurn;
    /**
     * Per-player count of spells cast this game, keyed by spell name. Unlike {@link #spellsCastThisTurn}
     * this persists for the whole game. Populated by {@link #recordSpellCast}; read via
     * {@link #getSpellsCastThisGameByNameCount} (Approach of the Second Sun's "cast another spell named ... this game").
     */
    private final Map<UUID, Map<String, Integer>> spellNameCastCountsThisGame = new ConcurrentHashMap<>();
    /**
     * Transient mana spent to cast a spell, keyed by spell card instance id.
     * Populated during spell payment and consumed when spell-cast triggers fire.
     */
    public final Map<UUID, Integer> spellCastManaSpent = new ConcurrentHashMap<>();
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
    /**
     * Names of the cards spliced onto a spell (CR 702.47), keyed by host spell card instance id.
     * Populated as splice costs are paid and read while the spell is on the stack (Minamo's Meddling).
     */
    public final Map<UUID, List<String>> spellCastSplicedNames = new ConcurrentHashMap<>();
    /** Per-color mana removed specifically to pay X (not the rest of the cost). Cleared on resolution. */
    public final Map<UUID, java.util.EnumMap<ManaColor, Integer>> spellCastManaSpentOnX = new ConcurrentHashMap<>();
    /** Tracks which permanent types each player has cast from graveyard this turn via Muldrotha-style effects. */
    public final Map<UUID, Set<CardType>> permanentTypesCastFromGraveyardThisTurn = new ConcurrentHashMap<>();
    /**
     * Permanents whose once-per-your-turn graveyard cast permission has already been used this turn
     * (Gisa and Geralf), keyed by the granting permanent's id.
     */
    public final Set<UUID> oncePerTurnGraveyardCastPermissionsUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Snapshot of per-player spell counts from the previous turn. Used by werewolf transform triggers. */
    public final Map<UUID, Integer> spellsCastLastTurn = new ConcurrentHashMap<>();
    /** Tracks which players declared at least one attacker this turn (for Angelic Arbiter etc.). */
    public final Set<UUID> playersDeclaredAttackersThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who put at least one counter on a creature this turn. */
    public final Set<UUID> playersWhoPutCountersOnCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who removed at least one oil counter from a permanent they controlled this turn. */
    public final Set<UUID> playersWhoRemovedOilCountersFromControlledPermanentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Whether a permanent carrying an oil counter was put into a graveyard this turn. */
    public boolean permanentWithOilCounterPutIntoGraveyardThisTurn;
    /** Players who controlled a permanent that received a +1/+1 counter this turn. */
    public final Set<UUID> playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who sacrificed at least one permanent this turn. */
    public final Set<UUID> playersWhoSacrificedPermanentsThisTurn = ConcurrentHashMap.newKeySet();
    /** Cumulative count of attacking creatures each player declared this turn (for Windbrisk Heights etc.). */
    public final Map<UUID, Integer> creaturesAttackedCountThisTurn = new ConcurrentHashMap<>();
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
    public final Map<UUID, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<UUID, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    /**
     * Player ids controlled by an AI opponent. Auto-pass must always hand these players a
     * priority window whenever they can act (so the AI can respond at instant speed), whereas
     * human players are auto-passed through any step outside their configured auto-stop set.
     */
    public final Set<UUID> aiPlayerIds = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerPoisonCounters = new ConcurrentHashMap<>();
    /** Players for whom Melira's poison replacement effect has already applied this turn. */
    public final Set<UUID> playersAffectedByMeliraPoisonReplacementThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerEnergyCounters = new ConcurrentHashMap<>();
    /** Persistent speed values; absent means the player has not started their engines. */
    public final Map<UUID, Integer> playerSpeeds = new ConcurrentHashMap<>();
    /** Players whose speed has already increased during the current turn. */
    public final Set<UUID> playersWhoseSpeedIncreasedThisTurn = ConcurrentHashMap.newKeySet();
    public final InteractionState interaction = new InteractionState();
    public final List<StackEntry> stack = Collections.synchronizedList(new TriggerAwareStackList(this));
    /** CR 603.3 — triggers from mana-ability sacrifices wait here until the next time a player
     *  would receive priority, so they don't block sorcery-speed spell casting. */
    public final List<StackEntry> pendingManaAbilityTriggers = Collections.synchronizedList(new ArrayList<>());
    /** Triggered abilities collected while a spell's casting costs are being paid. */
    public final List<StackEntry> pendingSpellCastCostTriggers = Collections.synchronizedList(new ArrayList<>());
    /** Triggered abilities collected while an activated ability's discard cost is being paid. */
    public final List<StackEntry> pendingActivatedAbilityCostTriggers = Collections.synchronizedList(new ArrayList<>());
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
    private int activeTriggeredAbilityCopies = 1;
    public final Map<UUID, List<Card>> playerGraveyards = new ConcurrentHashMap<>();
    /** Latest graveyard-entry identity for each card, used by effects that require continuous graveyard presence. */
    public final Map<UUID, Long> graveyardEntryVersions = new ConcurrentHashMap<>();
    private long graveyardEntryVersion;
    /** Cards in each player's command zone (CR 903.6). Used by Eminence and similar command-zone abilities. */
    public final Map<UUID, List<Card>> playerCommandZones = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Tracks all non-token card IDs (any type) put into each player's graveyard from the battlefield this turn (e.g. Twilight Shepherd). */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Tracks all non-token card IDs put into each player's graveyard from any zone this turn (e.g. Garna, the Bloodflame). */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromAnywhereThisTurn = new ConcurrentHashMap<>();
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
    /**
     * When non-null, the card with this ID is currently being put into a graveyard as the discard cost
     * of activating a cycling ability. Read by {@link com.github.laxika.magicalvibes.model.effect.OwnGraveyardExileReplacement}
     * (Abandoned Sarcophagus "and it wasn't cycled").
     */
    public UUID cardEnteringGraveyardByCycling;
    /** Counts all creature deaths (including tokens) from battlefield this turn, per controller. */
    public final Map<UUID, Integer> creatureDeathCountThisTurn = new ConcurrentHashMap<>();
    /** Counts nontoken creature deaths from the battlefield this turn, per controller. */
    public final Map<UUID, Integer> nontokenCreatureDeathCountThisTurn = new ConcurrentHashMap<>();
    /** Counts creature deaths by effective creature subtype and controller this turn. */
    public final Map<UUID, Map<CardSubtype, Integer>> creatureSubtypeDeathCountThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnBySourcePermanent = new ConcurrentHashMap<>();
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
    /** Delayed trigger: creature card ID → effect registrations to resolve if it dies this turn (Skeletonize, Initiate of Blood). */
    public final Map<UUID, List<DelayedEffectOnDeath>> creatureTriggeringEffectOnDeathThisTurn = new ConcurrentHashMap<>();
    /** Seraph: source Seraph permanent id → permanent ids of the creatures it returned under a player's control. */
    public final Map<UUID, Set<UUID>> seraphReturnedCreatures = new ConcurrentHashMap<>();
    /** Seraph: source Seraph permanent id → the player who last controlled it, watched for control-loss sacrifices. */
    public final Map<UUID, UUID> seraphControlWatch = new ConcurrentHashMap<>();
    /**
     * Gustha's Scepter: source permanent id → the player who last controlled it. When the
     * controller changes or the permanent leaves the battlefield, every card exiled with it is put
     * into its owner's graveyard ("When you lose control of this artifact, ...").
     */
    public final Map<UUID, UUID> exiledCardsToGraveyardOnControlLossWatch = new ConcurrentHashMap<>();
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
    /** Exiled Cosima card UUID → voyage counters accumulated while it remains exiled. */
    public final Map<UUID, Integer> exiledVoyageCounters = new ConcurrentHashMap<>();
    /** Exiled Cosima card UUID → controller of the ability that exiled it. */
    public final Map<UUID, UUID> exiledVoyageControllerIds = new ConcurrentHashMap<>();
    /** Cards exiled by the foretell special action and therefore castable for their foretell cost. */
    public final Set<UUID> foretoldCardIds = ConcurrentHashMap.newKeySet();
    /** Foretell costs assigned when cards were granted foretell dynamically. */
    public final Map<UUID, ManaCost> foretoldCardCosts = new ConcurrentHashMap<>();
    /** Card IDs currently represented as cards in the ante zone. Ante is modelled through exile. */
    public final Set<UUID> antedCardIds = ConcurrentHashMap.newKeySet();
    /** Maps exiled card UUID → egg counter count (for Darigaaz Reincarnated-style effects). */
    public final Map<UUID, Integer> exiledCardEggCounters = new ConcurrentHashMap<>();
    /** Maps exiled card UUID → dream counter count (Goliath Daydreamer). */
    public final Map<UUID, Integer> exiledCardDreamCounters = new ConcurrentHashMap<>();
    /** Maps exiled card UUID → hit counter count (Etrata, the Silencer). */
    public final Map<UUID, Integer> exiledCardHitCounters = new ConcurrentHashMap<>();
    /** Spells whose successful resolution is replaced by exile with a dream counter. */
    public final Set<UUID> spellsWithDreamCounterOnResolution = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have silver counters (Karn, Scion of Urza). */
    public final Set<UUID> exiledCardsWithSilverCounters = ConcurrentHashMap.newKeySet();
    /** Tracks exiled card UUIDs that have ice counters (Draugr Necromancer). */
    public final Set<UUID> exiledCardsWithIceCounters = ConcurrentHashMap.newKeySet();
    /** Spells exiled with delay counters and waiting to go back onto the stack (Ertai's Meddling). */
    public final List<DelayedSpellExile> delayedSpellExiles = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    /** Player IDs → remaining combat-damage-only prevention shields for this turn. */
    public final Map<UUID, Integer> playerCombatDamagePreventionShields = new ConcurrentHashMap<>();
    /** Player IDs → number of upcoming combat phases they must skip (Blinding Angel). Decremented as each is skipped. */
    public final Map<UUID, Integer> skipNextCombatPhaseCount = new ConcurrentHashMap<>();
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
    public boolean preventAllDamageToAllCreatures;
    /** When true, all damage that would be dealt by creatures is prevented this turn (Ethereal Haze). */
    public boolean preventAllDamageByCreatures;
    /** When true, all damage that would be dealt by non-Human sources is prevented this turn (Repel the Abominable). */
    public boolean preventAllDamageFromNonHumanSources;
    /** When non-null, creatures NOT matching this predicate are prevented from dealing combat damage this turn. */
    public PermanentPredicate combatDamageExemptPredicate;
    public boolean allPermanentsEnterTappedThisTurn;
    /**
     * Per-player count of additional +1/+1 counters that creatures entering under that player's
     * control receive for the rest of this turn (Zameck Guildmage). Turn-long replacement effect
     * (CR 614.1c) that survives the source leaving the battlefield.
     */
    public final Map<UUID, Integer> additionalEnterCountersThisTurn = new ConcurrentHashMap<>();
    /** Per-controller, per-color additive damage bonus this turn (e.g. The Flame of Keld Chapter III). */
    public final Map<UUID, Map<CardColor, Integer>> colorSourceDamageBonusThisTurn = new ConcurrentHashMap<>();
    public final Set<CardColor> preventDamageFromColors = ConcurrentHashMap.newKeySet();
    public UUID combatDamageRedirectTarget;
    public final Map<UUID, Map<CardColor, Integer>> playerColorDamagePreventionCount = new ConcurrentHashMap<>();
    /** Target IDs to colors whose damage is prevented to that target until end of turn. */
    public final Map<UUID, Set<CardColor>> colorDamagePreventionUntilEndOfTurn = new ConcurrentHashMap<>();
    public final List<PendingMayAbility> pendingMayAbilities = new ArrayList<>();
    /** Tariff: players (APNAP order) still to be processed after the one currently being resolved. */
    public final List<UUID> tariffRemainingPlayers = new ArrayList<>();
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
     * RevealHandDiscardMatchingCardsUnlessPaysLife: ids of the revealed cards still to be offered the
     * pay-or-discard prompt after the one currently being decided. Cleared when the queue drains.
     */
    public final List<UUID> revealHandDiscardUnlessPaysRemaining = new ArrayList<>();
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
    public StackEntry pendingEffectResolutionEntry;
    public int pendingEffectResolutionIndex;
    /** CR 603.5 — set when a MayEffect is encountered during stack resolution, cleared after player responds. */
    public boolean resolvingMayEffectFromStack;
    /** CR 603.5 — stores the player's response to a resolution-time MayEffect (true=accepted, false=declined, null=no pending choice). */
    public Boolean resolvedMayAccepted;
    /** CR 603.5 — stores the StackEntry for resolution-time target selection so the target can be set on it. */
    public StackEntry resolvedMayTargetingEntry;
    public Integer chosenXValue;
    /** Target awaiting a resolution-time damage-allocation answer for a divided damage effect. */
    public UUID pendingDividedDamageTargetId;
    public PendingAbilityCounterCostActivation pendingAbilityCounterCostActivation;
    /**
     * Resolution-time "choose a creature type" answer for a spell/ability that has no permanent
     * to hang the choice on (e.g. Coordinated Barrage). Set by the choice handler, read and
     * cleared by the effect handler that re-runs after the choice completes.
     */
    public CardSubtype chosenSpellSubtype;
    /** Resolution-time "choose a color" answer for a spell with no permanent to store it on. */
    public CardColor chosenSpellColor;
    /** Resolution-time "choose a number" answer for a spell with no permanent to store it on. */
    public Integer chosenSpellNumber;
    /** Resolution-time Turnabout permanent type choice. */
    public CardType chosenSpellPermanentType;
    /** Resolution-time card type choice for a spell with no permanent to store it on. */
    public CardType chosenSpellCardType;
    /** Resolution-time Turnabout action choice: true to tap, false to untap. */
    public Boolean turnaboutTap;
    /**
     * Generic re-entry signal: when set, {@code EffectResolutionService} re-runs the current
     * effect (rather than advancing to the next) after the pending interaction completes.
     * Set by handlers that drive a multi-step, self-continuing flow through an interaction whose
     * completion is not itself an X-value choice (e.g. the each-player discard-then-draw of Flux).
     */
    public boolean rerunCurrentEffectAfterInteraction;
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
    /** Progress state for each-player discard effects with an opponent life-loss fallback. */
    public final EachPlayerDiscardsOrLosesLifeState eachPlayerDiscardsOrLosesLife =
            new EachPlayerDiscardsOrLosesLifeState();
    /** Progress state for Creeping Dread's each-player discard comparison. */
    public final CreepingDreadState creepingDread = new CreepingDreadState();
    /** Progress state for Dispersal's opponent-by-opponent return-then-discard sequence. */
    public final DispersalState dispersal = new DispersalState();
    /** Progress state for Plague of Vermin's "each player may pay any amount of life" flow. */
    public final EachPlayerPayLifeState eachPlayerPayLife = new EachPlayerPayLifeState();
    /** Progress state for Liege of the Hollows' "each player may pay any amount of mana" flow. */
    public final EachPlayerPayManaState eachPlayerPayMana = new EachPlayerPayManaState();
    /** Progress state for Goblin Game's hidden item-count choices. */
    public final GoblinGameState goblinGame = new GoblinGameState();
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
    /** Progress state for Plaguecrafter's simultaneous sacrifice-then-discard effect. */
    public final PlaguecrafterState plaguecrafter = new PlaguecrafterState();
    /** Progress state for Winter's Chill's per-target "may pay {1} or {2}" flow. */
    public final WintersChillState wintersChill = new WintersChillState();
    /** Progress state for Forgotten Lore's "opponent chooses a card; you may pay {G} to repeat" flow. */
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
    /** Player IDs that have already taken their first draw of their own draw step this turn. Used by
     *  Notion Thief-style replacements that exempt "the first card they draw in each of their draw
     *  steps". Cleared at end-of-turn cleanup. */
    public final Set<UUID> drawStepFirstDrawTaken = ConcurrentHashMap.newKeySet();
    /** Aladdin's Lamp — a one-shot, turn-scoped delayed replacement of a player's next draw this
     *  turn: "instead look at the top X cards of your library, put all but one on the bottom in a
     *  random order, then draw a card." Keyed by drawing player id, value = X. Consumed on the next
     *  draw in {@code DrawService.resolveDrawCard} and cleared at end-of-turn cleanup. */
    public final Map<UUID, Integer> pendingNextDrawLookAtTop = new ConcurrentHashMap<>();
    /** Mangara's Tome — one-shot, turn-scoped delayed replacements of a player's next draws this
     *  turn: "instead put the top card of the exiled pile into its owner's hand." Keyed by drawing
     *  player id, value = a queue of source permanent ids (one entry per activation, since each
     *  replacement applies to one draw). Consumed in {@code DrawService.resolveDrawCard} and
     *  cleared at end-of-turn cleanup. */
    public final Map<UUID, List<UUID>> pendingNextDrawFromExiledPile = new ConcurrentHashMap<>();
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisTurn = new ConcurrentHashMap<>();
    /** Players who have begun activating an exhaust ability this turn. */
    public final Set<UUID> playersWhoActivatedExhaustAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who have activated a loyalty ability of a planeswalker this turn, backing the
     *  {@code DidntActivateLoyaltyAbilityThisTurn} intervening-if (The Chain Veil). Recorded when the
     *  loyalty cost is paid, so an activation whose ability is countered still counts. */
    public final Set<UUID> playersWhoActivatedLoyaltyAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Per-permanent, per-ability-index count of activations for the whole game, backing "Activate
     *  only once" ({@code ActivatedAbility.maxActivationsPerGame}, e.g. Goblin Ski Patrol). Keyed by
     *  permanent id, so a permanent that leaves and re-enters the battlefield is a new object and may
     *  activate again (CR 400.7). Never cleared at turn cleanup — only by Karn's game restart. */
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisGame = new ConcurrentHashMap<>();
    /** Per-permanent count of how many times its resolution-counting activated ability has resolved
     *  this turn (the {@code NthAbilityResolutionThisTurn} condition, e.g. Ashling the Pilgrim).
     *  Keyed by source permanent id; reset at the start of each turn. */
    public final Map<UUID, Integer> permanentAbilityResolutionsThisTurn = new ConcurrentHashMap<>();
    /** Maps a permanent that is not controlled by its owner to that owner (recorded on the first
     *  control change away from the owner, removed when control reverts to the owner or the
     *  permanent leaves the battlefield). This is the OWNERSHIP record used to route cards to the
     *  right graveyard/hand/library on leave; which player CONTROLS the permanent is derived from
     *  the floating {@code L2_CONTROL} effects (see {@link #deriveControllerOf}). */
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    public boolean endTurnRequested;
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
    public PendingTransformOnCreatureDiscard pendingTransformOnCreatureDiscard;
    public PendingBoostSourceByDiscardedManaValue pendingBoostSourceByDiscardedManaValue;
    public PendingUntapOnDiscardType pendingUntapOnDiscardType;
    public final Deque<UUID> extraTurns = new ArrayDeque<>();
    /**
     * Parallel to {@link #extraTurns}: whether the correspondingly-positioned extra turn skips its
     * untap step (e.g. Savor the Moment). Maintained in lockstep — every push/poll of {@link #extraTurns}
     * in engine code pushes/polls this deque at the same end.
     */
    public final Deque<Boolean> extraTurnSkipsUntap = new ArrayDeque<>();
    public int additionalCombatMainPhasePairs;
    /** Additional combat phases with NO additional main phase (e.g. Finest Hour), queued after the
     *  current combat phase and consumed when leaving END_OF_COMBAT. Reset at the start of each turn. */
    public int additionalCombatPhasesOnly;
    /** Additional combat phases inserted after the resolving main phase, with no main phase between them. */
    public int additionalCombatPhasesAfterMain;
    /** The normal phase to resume after {@link #additionalCombatPhasesAfterMain} is exhausted. */
    public TurnStep additionalCombatPhasesAfterMainReturnStep;
    /** How many combat phases have begun this turn (1 during the first combat phase). Reset at the
     *  start of each turn and incremented on entering BEGINNING_OF_COMBAT; read by FirstCombatPhase. */
    public int combatPhasesThisTurn;
    public UUID draftId;
    public final Deque<LibraryBottomReorderRequest> pendingLibraryBottomReorders = new ArrayDeque<>();
    public final WarpWorldOperationState warpWorldOperation = new WarpWorldOperationState();
    public boolean cleanupDiscardPending;
    /** Tracks exile-until-source-leaves connections (O-ring style).
     *  Maps source permanent UUID to the exiled cards + owner info.
     *  When the source permanent leaves the battlefield, the exiled cards return.
     *  A source may hold more than one pending return (e.g. Realm Razer exiles all lands). */
    public final Map<UUID, List<PendingExileReturn>> exileReturnOnPermanentLeave = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionIds = new ConcurrentHashMap<>();
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
    /** One-shot redirection shields (Opal-Eye, Konda's Yojimbo): the next damage event the chosen source
     *  would deal to any recipient this turn is dealt to a fixed permanent instead. */
    public final List<SourceNextDamageRedirectToPermanentShield> sourceNextDamageRedirectToPermanentShields =
            Collections.synchronizedList(new ArrayList<>());
    /** Pending Eye for an Eye reflected damage to deal after a shield matches (populated by
     *  DamagePreventionService, consumed by the damage-dealing services). */
    public final List<EyeForAnEyeReflection> pendingEyeForAnEyeReflections = Collections.synchronizedList(new ArrayList<>());
    /** Per-source damage accumulated during one non-combat damage event (a single stack-entry
     *  resolution), keyed by the damage source card's id. Flushed after the resolution completes so a
     *  global "whenever a [color] source deals damage" watcher (Justice) reflects the summed total
     *  once per source (CR ruling). Combat damage batches separately via {@code combatDamageDealt}. */
    public final Map<UUID, PendingSourceDamage> pendingSourceDamageForReflection = new LinkedHashMap<>();
    public final Set<UUID> permanentsPreventedFromDealingDamage = ConcurrentHashMap.newKeySet();
    /** Instant and sorcery spells whose damage is prevented for the rest of this turn. */
    public final List<TargetSpellDamagePreventionShield> targetSpellDamagePreventionShields =
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
    /** Players whose own damage (but not their creatures') is fully prevented this turn (Riot Control). */
    public final Set<UUID> playersWithAllPlayerDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players whose own damage is fully prevented until the beginning of their next turn (Morningtide's Light). */
    public final Set<UUID> playersWithAllPlayerDamagePreventedUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players with protection from everything until the beginning of their next turn. */
    public final Set<UUID> playersWithProtectionFromEverythingUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players for whom damage dealt by attacking creatures is prevented this turn (Deep Wood). */
    public final Set<UUID> playersWithDamageFromAttackersPrevented = ConcurrentHashMap.newKeySet();
    /** Players for whom damage from matching source permanents is prevented this turn. */
    public final Map<UUID, Set<PermanentPredicate>> playersWithDamageFromMatchingSourcesPrevented =
            new ConcurrentHashMap<>();
    /** Players who, this turn, gain control of creatures that would enter under an opponent's control (Gather Specimens). */
    public final Set<UUID> playersGatheringSpecimensThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who, this turn, gain control of tokens that would be created under an opponent's control (Crafty Cutpurse). */
    public final Set<UUID> playersGatheringTokensThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who, this turn, exile creatures that would enter without having been cast (Hallowed Moonlight). */
    public final Set<UUID> playersExilingUncastEnteringCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Players who, this turn, exile nontoken creatures that would enter without having been cast (Mistcaller). */
    public final Set<UUID> playersExilingUncastEnteringNontokenCreaturesThisTurn = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose damage is fully prevented this turn (Wellgabber Apothecary). */
    public final Set<UUID> creaturesWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Players with an active effect that redirects damage dealt to any creature to them. */
    public final Set<UUID> playersRedirectingAllCreatureDamage = ConcurrentHashMap.newKeySet();
    /**
     * Predicates whose matching permanents have all damage to them prevented this turn
     * (Ethersworn Shieldmage). Re-evaluated per damage event, so it covers permanents that
     * start/stop matching after the effect resolved (official ruling).
     */
    public final Set<PermanentPredicate> allDamagePreventionPredicates = ConcurrentHashMap.newKeySet();
    /** Predicates whose matching permanents controlled by the keyed player have combat damage prevented. */
    public final Map<UUID, Set<PermanentPredicate>> combatDamagePreventionPredicatesByController =
            new ConcurrentHashMap<>();
    /** Specific creatures whose combat damage dealt to them is prevented this turn (Foxfire). */
    public final Set<UUID> creaturesWithCombatDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose combat damage is prevented this turn (Resistance Fighter). */
    public final Set<UUID> creaturesPreventedFromDealingCombatDamage = ConcurrentHashMap.newKeySet();
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
    /** When true, no player can gain life this turn (Skullcrack). Cleared at turn cleanup. */
    public boolean playersCantGainLifeThisTurn = false;
    /** When true, no creature can attack this turn. Cleared at turn cleanup. */
    public boolean creaturesCantAttackThisTurn = false;
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
    /** Active "whenever a creature dies this turn" delayed triggers, cleared at turn cleanup. */
    public final List<CreatureDeathTriggerWatcher> creatureDeathTriggerWatchers =
            Collections.synchronizedList(new ArrayList<>());
    /** Damage redirect shields (e.g. Vengeful Archon): prevention shields that redirect prevented damage to a target player. */
    public final List<DamageRedirectShield> damageRedirectShields = Collections.synchronizedList(new ArrayList<>());
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
    /** APNAP-ordered queue of players still to choose for "each player may draw up to N" effects (Temporary Truce). Head player is the one currently prompted. */
    public final List<UUID> pendingEachPlayerDrawUpToQueue = Collections.synchronizedList(new ArrayList<>());
    /** APNAP-ordered queue of players still to choose for "each other player may draw up to N" effects. */
    public final List<UUID> pendingEachOtherPlayerDrawUpToQueue = Collections.synchronizedList(new ArrayList<>());
    public final List<Emblem> emblems = Collections.synchronizedList(new ArrayList<>());
    /** Players who have been granted "no maximum hand size" for the rest of the game. */
    public final Set<UUID> playersWithNoMaximumHandSize = ConcurrentHashMap.newKeySet();
    /** Players who have no maximum hand size until the beginning of their next turn. */
    public final Set<UUID> playersWithNoMaximumHandSizeUntilNextTurn = ConcurrentHashMap.newKeySet();
    /** Players who can't gain life for the rest of the game (e.g. Stigma Lasher). */
    public final Set<UUID> playersWhoCantGainLifeRestOfGame = ConcurrentHashMap.newKeySet();
    /** Players who can't gain life this turn (e.g. Flames of the Blood Hand). Cleared at turn cleanup. */
    public final Set<UUID> playersWhoCantGainLifeThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks source-linked animations (Awakener Druid-style).
     *  Maps animated target permanent UUID → source permanent UUID.
     *  When the source leaves the battlefield, the target's animation is cleared. */
    public final Map<UUID, UUID> sourceLinkedAnimations = new ConcurrentHashMap<>();

    /** Per-player: spells controlled by this player can't be countered by spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerSpellsCantBeCounteredByColorsThisTurn = new ConcurrentHashMap<>();

    /** Players whose spells can't be countered this turn. Cleared at end of turn. */
    public final Set<UUID> playersSpellsCantBeCounteredThisTurn = ConcurrentHashMap.newKeySet();

    /** Per-player: creatures controlled by this player can't be the targets of spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerCreaturesCantBeTargetedByColorsThisTurn = new ConcurrentHashMap<>();

    /** Per-player: this player has hexproof from these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerHexproofFromColorsThisTurn = new ConcurrentHashMap<>();

    /** Per-permanent: this permanent has hexproof from these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> permanentHexproofFromColorsThisTurn = new ConcurrentHashMap<>();

    /** Card IDs of individual spells on the stack that have been made uncounterable (e.g. Vexing Shusher's
     *  "Target spell can't be countered"). Only relevant while the spell is on the stack. */
    public final Set<UUID> spellsMadeUncounterable = ConcurrentHashMap.newKeySet();

    /** Text-change replacements applied to a spell on the stack (e.g. Glamerdye targeting a spell).
     *  Keyed by the spell's card id; carried onto the permanent it resolves into (CR 613.7). */
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

    /** Players who can't cast spells this turn (e.g. Silence). Cleared at end of turn and on new turn. */
    public final Set<UUID> playersSilencedThisTurn = ConcurrentHashMap.newKeySet();

    /** Players who can't cast spells for the rest of the game because an Epic spell resolved. */
    public final Set<UUID> playersCantCastSpellsForRestOfGame = ConcurrentHashMap.newKeySet();

    /**
     * Card names opponents of the key player can't cast spells with, until that player's next turn
     * (Comply). Keyed by the naming controller → set of forbidden names. Cleared at the start of
     * that controller's next turn (survives end-of-turn cleanup).
     */
    public final Map<UUID, Set<String>> opponentsCantCastNamedSpellsUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Players who can't cast noncreature spells until the key player's next turn. */
    public final Map<UUID, Set<UUID>> playersCantCastNoncreatureSpellsUntilControllerNextTurn =
            new ConcurrentHashMap<>();

    /** Land subtype -&gt; extra mana color added whenever a player taps a land of that subtype for mana
     *  this turn (Chaos Moon's odd branch: "whenever a player taps a Mountain for mana, that player
     *  adds an additional {R}"). Cleared at end of turn. */
    public final Map<CardSubtype, ManaColor> extraManaOnLandSubtypeTapThisTurn = new ConcurrentHashMap<>();

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

    /** Sen Triplets: while set, {@link #senControllerPlayerId} may play lands and cast spells from
     *  {@link #senControlledPlayerId}'s hand this turn (that hand is also revealed to the controller,
     *  and the controlled player is silenced + can't activate abilities). Both cleared at end of turn. */
    public UUID senControllerPlayerId;
    public UUID senControlledPlayerId;

    /** Card IDs that have been granted flashback until end of turn (e.g. Past in Flames).
     *  The flashback cost for these cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedFlashbackUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs that have been granted embalm until end of turn (e.g. Cursecloth Wrappings).
     *  The embalm cost for these cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedEmbalmUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may cast spells as though they had flash until end of turn (Alchemist's
     *  Refuge, Vedalken Orrery-style one-shot grants). Cleared at end of turn. */
    public final Set<UUID> playersWithFlashUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may cast spells of a given type as though they had flash until end of turn.
     *  Cleared at end of turn. */
    public final Map<UUID, Set<CardPredicate>> cardTypeFlashGrantsThisTurn = new ConcurrentHashMap<>();

    /** Pending "the next spell of this type you cast this turn can be cast as though it had flash"
     *  grants (Quicken), keyed by player. Each list element is one unconsumed grant; a grant is
     *  consumed by {@link #recordSpellCast} when a matching spell is cast. Cleared at end of turn. */
    public final Map<UUID, List<CardType>> nextSpellFlashGrantsThisTurn = new ConcurrentHashMap<>();

    /** Pending "the next creature spell you cast this turn ..." grants (Savage Summoning), keyed by
     *  player. Every unconsumed grant applies to the same next creature spell and is consumed by
     *  {@link #recordSpellCast}. Cleared at end of turn. */
    public final Map<UUID, List<CreatureSpellEmpowerment>> nextCreatureSpellEmpowermentsThisTurn = new ConcurrentHashMap<>();

    /** Extra +1/+1 counters a spell's permanent enters the battlefield with, keyed by card id
     *  (Savage Summoning). Consumed as an as-enters replacement by {@code BattlefieldEntryService}
     *  and cleared at end of turn. */
    public final Map<UUID, Integer> spellAdditionalEnterCounters = new ConcurrentHashMap<>();

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

    /** Player IDs that may tap lands they don't control for mana until end of turn (Piracy). The
     *  mana produced this way may only be spent to cast spells. Cleared at end of turn. */
    public final Set<UUID> mayTapLandsForSpellsUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Player IDs that may pay 1 life to add {C} any time they could activate a mana ability until end
     *  of turn (Channel). Cleared at end of turn. */
    public final Set<UUID> mayPayLifeForColorlessManaUntilEndOfTurn = ConcurrentHashMap.newKeySet();

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
                                               boolean exileInsteadOfGraveyard) {}

    /** A turn-scoped grant letting {@code playerId} cast any card matching {@code filter} from
     *  their own graveyard, paying its normal costs. */
    public record GraveyardCastFilterPermission(UUID playerId, CardPredicate filter) {}

    /** One source-linked, one-card cast grant created by a temporary activated ability. */
    public record ExileCastPermission(UUID grantId, UUID sourcePermanentId, UUID castingPlayerId,
                                      UUID cardId, boolean withoutPayingManaCost) {}

    /** Targeted cards that may be cast from a graveyard this turn.
     *  Maps graveyard card UUID -> source permanent and casting player (e.g. Havengul Lich).
     *  Cleared at end of turn. */
    public final Map<UUID, GraveyardCardCastPermission> graveyardCardCastPermissionsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Players whose instant/sorcery spells are automatically copied until end of turn
     *  (e.g. The Mirari Conjecture chapter III). Cleared at end of turn. */
    public final Set<UUID> playersWithSpellCopyUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs of spells cast with their conspire cost paid (CR 702.78). Consumed by
     *  {@code TriggerCollectionService} when the spell-cast triggers are collected, queuing a
     *  single "copy it, you may choose a new target" trigger above the spell. */
    public final Set<UUID> conspiredSpellIds = ConcurrentHashMap.newKeySet();

    /** Pending one-shot spell copy triggers from mana abilities (e.g. Primal Wellspring).
     *  Each value tracks how many copies are pending for that player.
     *  Decremented when an instant/sorcery is cast; cleared when mana pools drain. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyCount = new ConcurrentHashMap<>();

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

    /** Pending mana-value-limited one-shot spell copy triggers for the current turn. */
    public final Map<UUID, List<Integer>> pendingNextInstantSorceryCopyThisTurnMaxManaValues =
            new ConcurrentHashMap<>();

    /** Pending one-shot loyalty-ability copy triggers for the current turn. */
    public final Map<UUID, Integer> pendingNextLoyaltyAbilityCopyThisTurnCount = new ConcurrentHashMap<>();

    /** Pending one-shot non-mana exhaust-ability copy triggers for the current turn. */
    public final Map<UUID, Integer> pendingNextExhaustAbilityCopyThisTurnCount = new ConcurrentHashMap<>();

    /** "Whenever you cast a creature spell this turn, draw a card" delayed triggers (Glimpse of
     *  Nature). The value is how many cards that player draws per creature spell cast; cleared at
     *  end of turn. */
    public final Map<UUID, Integer> creatureSpellCastDrawsThisTurn = new ConcurrentHashMap<>();

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
     */
    public record DelayedSpellExile(UUID cardId, UUID controllerId, int counters, StackEntry originalEntry) {}

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

    /**
     * Cards exiled by a free-cast process that should go to their owners' graveyards when casting
     * finishes (Epic Experiment). Empty when unused. Cleared by
     * {@code ExileFreeCastQueueSupport.putRemainderIntoOwnersGraveyards}.
     */
    public final List<UUID> pendingExileFreeCastRemainderToGraveyard = new ArrayList<>();

    /** Delayed triggers from Chancellor-style opening hand reveals.
     *  Fires once per opponent when they cast their first spell of the game. */
    public final List<OpeningHandRevealTrigger> openingHandRevealTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Delayed mana triggers from Chancellor-style opening hand reveals.
     *  Fires at the beginning of the revealing player's first precombat main phase. */
    public final List<OpeningHandRevealTrigger> openingHandManaTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Tracks which players have cast their first spell of the game (for opening hand triggers). */
    public final Set<UUID> playersWhoCastFirstSpellInGame = ConcurrentHashMap.newKeySet();

    /** Player UUID to card UUID for a temporary free-play permission on the current library top card. */
    public final Map<UUID, UUID> libraryTopCardFreePlayPermissionsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Maps exiled card UUID → player UUID who has permission to play it (e.g. Praetor's Grasp). */
    public final Map<UUID, UUID> exilePlayPermissions = new ConcurrentHashMap<>();
    /** Maps a source permanent to the latest card whose permission it granted. */
    public final Map<UUID, UUID> exilePlayPermissionSourceCards = new ConcurrentHashMap<>();
    /** Cost modifiers attached to cards that may be played from exile. */
    public final Map<UUID, ExilePlayCostModifier> exilePlayCostModifiers = new ConcurrentHashMap<>();
    /** Card UUIDs whose exile-play permission expires at end of turn (impulse draw, e.g. Vance's Blasting Cannons).
     *  Cleared during cleanup step — matching entries are also removed from {@link #exilePlayPermissions}. */
    public final Set<UUID> exilePlayPermissionsExpireEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Card UUIDs whose exile-play permission expires at end of the turn number stored as the value
     *  (e.g. Archaic's Agony: until end of your next turn). */
    public final Map<UUID, Integer> exilePlayPermissionsExpireAtTurnEnd = new ConcurrentHashMap<>();
    /** Exiled card UUIDs that may be cast spending mana of any type (e.g. Nita, Forum Conciliator's
     *  activated ability). Complements the battlefield-permanent any-mana grant used by Hostage Taker.
     *  Cleared during cleanup step. */
    public final Set<UUID> exilePlayAnyManaType = ConcurrentHashMap.newKeySet();
    /** Exiled card UUIDs that may be cast spending mana of any type for as long as they remain exiled. */
    public final Set<UUID> exilePlayAnyManaTypeWhileExiled = ConcurrentHashMap.newKeySet();
    /** Exiled card UUIDs that have stash counters. */
    public final Set<UUID> stashCounterCardIds = ConcurrentHashMap.newKeySet();
    /** Card UUIDs that may be played from exile without paying their mana cost (e.g. Oracle's Vault's
     *  second ability). Complements {@link #exilePlayPermissions} — the card must also hold a play
     *  permission. Temporary entries are listed in {@link #exilePlayPermissionsExpireEndOfTurn};
     *  entries without an expiry remain valid for as long as the card remains exiled. */
    public final Set<UUID> exilePlayWithoutPayingManaCost = ConcurrentHashMap.newKeySet();
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
    /** Players whose cards are exiled instead of entering their graveyards for the rest of the turn. */
    public final Set<UUID> playersExilingCardsInsteadOfGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Depth counter for batching "cards leave graveyard" triggers (one trigger per batch). */
    public int graveyardLeaveNotificationDepth = 0;
    /** Owners whose graveyards had cards leave during a suppressed batch; triggers fire when depth returns to 0. */
    public final Set<UUID> graveyardLeaveNotificationPendingOwners = ConcurrentHashMap.newKeySet();
    /** Owners whose graveyards had creature cards leave during a suppressed batch. */
    public final Set<UUID> graveyardLeaveNotificationPendingCreatureOwners = ConcurrentHashMap.newKeySet();
    /** Owners whose graveyards had artifact or creature cards leave during a suppressed batch. */
    public final Set<UUID> graveyardLeaveNotificationPendingArtifactOrCreatureOwners = ConcurrentHashMap.newKeySet();
    /** Number of cards exiled from each owner's graveyard during a suppressed batch. */
    public final Map<UUID, Integer> graveyardExileNotificationPendingCounts = new ConcurrentHashMap<>();
    /** Whether a suppressed batch exiled one or more cards from a graveyard or the battlefield. */
    public boolean graveyardOrBattlefieldExileNotificationPending;
    /** Players who had one or more cards leave their graveyard this turn (cleared at turn cleanup). Used by Wilt in the Heat cost reduction. */
    public final Set<UUID> playersWhoseCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Transient field: while a player is choosing a card to exile from hand, identifies the player who should
     *  gain permission to play that card for as long as it remains exiled (e.g. Fiend of the Shadows). Null when
     *  the exiling effect does not grant play permission to a controller. */

    /** Tracks how many cards each player has drawn this turn. */
    public final Map<UUID, Integer> cardsDrawnThisTurn = new ConcurrentHashMap<>();

    /** Tracks the card ids each player has drawn this turn, in draw order. Used by effects that must
     *  identify the specific cards "drawn this turn" (e.g. Sylvan Library). Cleared each turn. */
    public final Map<UUID, List<UUID>> cardsDrawnThisTurnIds = new ConcurrentHashMap<>();

    /** Tracks how many cards each player has discarded this turn (any discard, any source). Used by
     *  "cards discarded this turn" effects, e.g. Dream Salvage. Cleared at the start of each turn. */
    public final Map<UUID, Integer> cardsDiscardedThisTurn = new ConcurrentHashMap<>();

    /** Player and count for a discard event whose cards are being processed one at a time. */
    public UUID discardEventPlayerId;
    public int discardEventCardCount;

    /** Mana value of the most recently discarded card (any player, any source), recorded by the
     *  central discard hook. Read by {@code LastDiscardedCardManaValue} so a later effect of the same
     *  spell can scale off the card just discarded (Blast of Genius). */
    public int lastDiscardedCardManaValue;
    /** Card types of the most recently discarded card, including additional card types. */
    public Set<CardType> lastDiscardedCardTypes = Set.of();

    /** Counts colored mana symbols on the most recently milled card. */
    public final Map<ManaColor, Integer> lastMilledCardColorSymbols = new ConcurrentHashMap<>();

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

    /** Permanent IDs of creatures that dealt combat damage to a creature this turn. */
    public final Set<UUID> combatDamageSourcesThatDealtToCreaturesThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents dealt noncombat damage (spells/abilities) to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. Combined with
     *  {@link #combatDamageToPlayersThisTurn}, gives every source that dealt any damage to a player
     *  (Giltspire Avenger). Cleared at turn cleanup. */
    public final Map<UUID, Set<UUID>> noncombatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creature permanents dealt damage to which players this turn. */
    public final Map<UUID, Set<UUID>> creatureDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creatures attacked which players this turn ("target creature that attacked you
     *  this turn", Jabari's Influence). Maps attacking permanent UUID → set of attacked player UUIDs;
     *  creatures that attacked only a planeswalker never appear, since attacking a planeswalker its
     *  controller owns is not attacking that player. Cleared at turn cleanup. */
    public final Map<UUID, Set<UUID>> playersAttackedThisTurn = new ConcurrentHashMap<>();

    /** Records that {@code attackerPermanentId} was declared as an attacker against {@code playerId}. */
    public void recordAttackAgainstPlayer(UUID attackerPermanentId, UUID playerId) {
        if (attackerPermanentId == null || playerId == null) {
            return;
        }
        playersAttackedThisTurn
                .computeIfAbsent(attackerPermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
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

    /** Records that a creature permanent dealt damage to a player this turn. */
    public void recordCreatureDamageSourceToPlayer(UUID sourcePermanentId, UUID playerId) {
        if (sourcePermanentId == null || playerId == null) {
            return;
        }
        creatureDamageToPlayersThisTurn
                .computeIfAbsent(sourcePermanentId, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
    }

    /** Tracks how much damage each source dealt this turn, to every recipient (players, planeswalkers,
     *  battles and creatures; combat and noncombat alike). Maps source permanent UUID → total damage.
     *  Used by "if [this] has dealt N or more damage this turn" (Chandra, Fire of Kaladesh).
     *  Cleared at turn cleanup. */
    public final Map<UUID, Integer> damageDealtThisTurnBySource = new ConcurrentHashMap<>();

    /** Tracks source permanent objects that have dealt damage at least once. */
    public final Set<UUID> permanentsThatHaveDealtDamage = ConcurrentHashMap.newKeySet();

    /** Tracks every player or permanent that each source permanent has dealt damage to this game. */
    public final Map<UUID, Set<UUID>> damageRecipientsBySource = new ConcurrentHashMap<>();

    /** Records that {@code sourcePermanentId} dealt {@code amount} damage this turn. No-op when the
     *  source permanent is unknown or the amount is non-positive. */
    public void recordDamageDealtBySource(UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) {
            return;
        }
        damageDealtThisTurnBySource.merge(sourcePermanentId, amount, Integer::sum);
        permanentsThatHaveDealtDamage.add(sourcePermanentId);
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

    /** Tracks which players have been dealt damage this turn (from any source — combat, spells, abilities). */
    public final Set<UUID> playersDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks how much damage each player has been dealt this turn (from any source — combat, spells,
     *  abilities; includes damage dealt as poison). Cleared at turn cleanup. Used by Final Punishment. */
    public final Map<UUID, Integer> damageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much noncombat damage each player has been dealt this turn. */
    public final Map<UUID, Integer> noncombatDamageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

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

    /** Records that {@code amount} noncombat damage was dealt to {@code playerId} this turn. */
    public void recordNoncombatDamageToPlayer(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }
        noncombatDamageDealtToPlayersThisTurn.merge(playerId, amount, Integer::sum);
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
     *  turn (recorded as their upkeep begins, after the untap step). Locked so responses that tap lands
     *  don't change it. Read via {@code UntappedLandsAtTurnStart} for Power Surge. */
    public final Map<UUID, Integer> untappedLandsAtTurnStart = new ConcurrentHashMap<>();
    /** Snapshot of each active player's hand size when their current turn began. */
    public final Map<UUID, Integer> handSizeAtTurnStart = new ConcurrentHashMap<>();

    /** Tracks which permanents (by UUID) have been dealt damage this turn (from any source — combat, spells, abilities).
     *  Survives regeneration (which removes marked damage but does not undo "was dealt damage").
     *  Cleared at start of new turn. */
    public final Set<UUID> permanentsDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks the total damage actually dealt to each permanent this turn. Prevented damage is not
     *  included, and the total survives regeneration. Cleared at start of new turn. */
    public final Map<UUID, Integer> damageDealtToPermanentsThisTurn = new ConcurrentHashMap<>();

    /** Tracks controllers whose Giant, Wizard, or spell dealt damage to each permanent this turn. */
    public final Map<UUID, Set<UUID>> qualifyingDamageControllersByPermanentThisTurn = new ConcurrentHashMap<>();

    /** Records that {@code amount} damage was dealt to {@code permanentId} this turn. */
    public void recordDamageToPermanent(UUID permanentId, int amount) {
        if (permanentId == null || amount <= 0) {
            return;
        }
        permanentsDealtDamageThisTurn.add(permanentId);
        damageDealtToPermanentsThisTurn.merge(permanentId, amount, Integer::sum);
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

    /** Tracks which permanents (by UUID) have already provided their once-each-turn "you may pay {0}"
     *  alternative cast cost this turn (As Foretold). Cleared at start of new turn. */
    public final Set<UUID> freeCastPermanentUsedThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents (by UUID) have already fired a {@code OncePerTurnTriggerEffect}
     *  this turn (e.g. Ghoulish Procession). Cleared at start of new turn. */
    public final Set<UUID> oncePerTurnTriggersFiredThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks permanents that have added mana with a source ability this turn. */
    public final Set<UUID> permanentsThatAddedManaWithAbilityThisTurn = ConcurrentHashMap.newKeySet();
    /** Tracks keyed triggered-ability resolutions that have already happened for each permanent
     *  this turn. Cleared at start of new turn. */
    public final Map<UUID, Set<String>> firstResolutionTriggerKeysThisTurn = new ConcurrentHashMap<>();

    /** Crown permanent IDs that have replaced a token creation event this turn. */
    public final Set<UUID> tokenCreationReplacementUsedThisTurn = ConcurrentHashMap.newKeySet();
    /** Token creation event paused for a Mirrormind Crown replacement choice. */
    public PendingTokenCreationReplacement pendingTokenCreationReplacement;

    /** Tracks which permanents (by UUID) have already fired a {@code OncePerTurnTriggerEffect} in the
     *  {@code ON_ATTACK} slot this turn — "attacks for the first time each turn" (Aurelia, the
     *  Warleader). Kept separate from {@link #oncePerTurnTriggersFiredThisTurn} so a permanent with
     *  both a once-each-turn death watcher and a once-each-turn attack trigger doesn't gate one on the
     *  other. Cleared at start of new turn. */
    public final Set<UUID> onceEachTurnAttackTriggersFiredThisTurn = ConcurrentHashMap.newKeySet();

    /**
     * Creatures dying together in the current simultaneous-death event (CR 700.1 / destroy batch /
     * SBA lethal pass). Maps permanent id → permanent (last-known) and controller id. Used so
     * "whenever another creature dies" watchers that are themselves dying still see the other
     * deaths (e.g. Morbid Opportunist). Cleared when the batch ends; not turn-scoped.
     */
    public final Map<UUID, Permanent> simultaneousDyingCreatures = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> simultaneousDyingControllers = new ConcurrentHashMap<>();

    /** Tracks subtypes of creatures that dealt combat damage to players this turn.
     *  Maps source permanent UUID → set of subtypes the creature had at the time of dealing damage.
     *  Used by end-step triggers that check which subtypes dealt combat damage (e.g. Admiral Beckett Brass). */
    public final Map<UUID, Set<CardSubtype>> combatDamageSourceSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creatures that dealt combat damage to players this turn had the Changeling keyword.
     *  These creatures count as having all creature subtypes for subtype-conditional triggers. */
    public final Set<UUID> combatDamageSourcesWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks, per player who controlled the source at damage time, the union of subtypes of creatures
     *  they controlled that dealt combat damage to a player this turn. Used to evaluate the prowl
     *  alternative cost ("if you dealt combat damage to a player this turn with a [subtype]"). */
    public final Map<UUID, Set<CardSubtype>> combatDamageToPlayerControllerSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Tracks which players dealt combat damage to a player this turn with a Changeling creature they
     *  controlled (which counts as every creature subtype for prowl). */
    public final Set<UUID> controllersDealtCombatDamageWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

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

    /** Tracks which Leonin Arbiter permanent IDs each player has paid {2} for this turn. */
    public final Map<UUID, Set<UUID>> paidSearchTaxPermanentIds = new ConcurrentHashMap<>();

    // Mindslaver — turn control
    /** Delayed effect: targetPlayerId -> controllerId, consumed when target player's turn begins. */
    public final Map<UUID, UUID> pendingTurnControl = new ConcurrentHashMap<>();
    /**
     * Players whose pending turn-control effect also grants them an extra turn after the controlled
     * turn (Emrakul, the Promised End). Consumed with {@link #pendingTurnControl} when that turn begins.
     */
    public final Set<UUID> pendingTurnControlExtraTurn = ConcurrentHashMap.newKeySet();
    /** Non-null when a player is being controlled this turn (the controlled player's ID). */
    public UUID mindControlledPlayerId;
    /** Non-null when a player is being controlled this turn (the controlling player's ID). */
    public UUID mindControllerPlayerId;

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
    /** Active this turn: affectedPlayerId -> the only creatures allowed to attack (all others can't). */
    public final Map<UUID, Set<UUID>> chosenAttackersThisTurn = new ConcurrentHashMap<>();

    /** Active this turn: affectedPlayerId -> creatures allowed to attack by a pile effect. */
    public final Map<UUID, Set<UUID>> attackableCreaturesThisTurn = new ConcurrentHashMap<>();

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

    /** Damage assignments provided at cast time for an ETB divided-damage effect (e.g. Kuldotha Flamefiend). */
    public Map<UUID, Integer> pendingETBDamageAssignments = Map.of();


    // Combat damage assignment state
    public final Map<Integer, Map<UUID, Integer>> combatDamagePlayerAssignments = new HashMap<>();
    public final List<Integer> combatDamagePendingIndices = new ArrayList<>();
    /** CR 510.1d — the defending player's damage division for creatures blocking 2+ attackers
     *  (defending-battlefield index → attacker permanent id → damage). */
    public final Map<Integer, Map<UUID, Integer>> combatDamageBlockerAssignments = new HashMap<>();
    public final List<Integer> combatDamagePendingBlockerIndices = new ArrayList<>();
    /** True while damage assignments for the first-strike combat damage step are being collected
     *  (the regular step uses {@link #combatDamagePhase1Complete} instead). */
    public boolean combatDamageFirstStrikeAssignmentPhase = false;
    public boolean combatDamageFirstStrikeStepComplete = false;
    public boolean combatDamagePhase1Complete = false;
    public CombatDamagePhase1State combatDamagePhase1State;

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

    /** Returns the next CR 613.7 timestamp (strictly increasing, starting at 1). */
    public long nextTimestamp() {
        return ++timestampCounter;
    }

    /** Records a new continuous stay of a card in a graveyard and returns its identity. */
    public synchronized long markGraveyardEntry(Card card) {
        long version = ++graveyardEntryVersion;
        graveyardEntryVersions.put(card.getId(), version);
        return version;
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
     *  of the controller's next turn. */
    public final List<FloatingContinuousEffect> floatingEffects = Collections.synchronizedList(new ArrayList<>());

    /** Permanents whose temporary control effect carries a "tap it when you lose control" rider
     *  (Magus of the Unseen). Tapped and cleared during the cleanup step, when the until-end-of-turn
     *  control effect expires and the permanent reverts to its owner. */
    public final Set<UUID> permanentsToTapWhenControlLost = ConcurrentHashMap.newKeySet();

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
     * Stamps the given floating effect with the next CR 613.7 timestamp, stores it, and returns
     * the stamped instance (the passed-in effect's own timestamp is ignored).
     */
    public FloatingContinuousEffect addFloatingEffect(FloatingContinuousEffect effect) {
        FloatingContinuousEffect stamped = effect.withTimestamp(nextTimestamp());
        floatingEffects.add(stamped);
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

    /** Removes and returns all floating effects with end-of-turn duration (cleanup step). */
    public List<FloatingContinuousEffect> expireEndOfTurnFloatingEffects() {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_END_OF_TURN
                || fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST);
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
                        || fe.duration() == EffectDuration.WHILE_SOURCE_REMAINS
                        || fe.duration() == EffectDuration.WHILE_SOURCE_TAPPED
                        || fe.duration() == EffectDuration.WHILE_SOURCE_REMAINS_TAPPED
                        || fe.duration() == EffectDuration.WHILE_ATTACHED)
                        && sourcePermanentId.equals(fe.sourcePermanentId()));
    }

    /**
     * Removes and returns all NON-control {@code WHILE_SOURCE_TAPPED} floating effects sourced from
     * the given permanent. Called when that permanent becomes untapped (CR 611.2b — such effects end
     * and do not resume). Control {@code WHILE_SOURCE_TAPPED} effects are handled separately by the
     * control reconciliation in {@code CreatureControlService}. Tawnos's Weaponry's +1/+1 buff.
     */
    public List<FloatingContinuousEffect> expireTappedSourceFloatingEffects(UUID sourcePermanentId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.WHILE_SOURCE_TAPPED
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

    /**
     * Removes and returns all {@code UNTIL_CONTROLLERS_NEXT_UPKEEP} floating effects controlled by
     * the given player. Called at the beginning of that player's upkeep step (Cycle of Life).
     */
    public List<FloatingContinuousEffect> expireFloatingEffectsAtUpkeep(UUID playerId) {
        return expireFloatingEffects(fe -> fe.duration() == EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP
                && playerId.equals(fe.controllerId()));
    }

    public List<FloatingContinuousEffect> expireFloatingEffects(Predicate<FloatingContinuousEffect> expired) {
        List<FloatingContinuousEffect> removed = new ArrayList<>();
        var it = floatingEffects.iterator();
        while (it.hasNext()) {
            FloatingContinuousEffect fe = it.next();
            if (expired.test(fe)) {
                removed.add(fe);
                it.remove();
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
        if (fe.duration() == EffectDuration.WHILE_ATTACHED && fe.sourcePermanentId() != null) {
            UUID auraController = findControllerOf(fe.sourcePermanentId());
            if (auraController != null) {
                return auraController;
            }
        }
        return fe.controllerId();
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
            UUID cardOwner = permanent.getCard().getOwnerId();
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

    /**
     * Routes a triggered-ability {@link StackEntry} to the main stack, or to
     * {@link #pendingManaAbilityTriggers} when a mana ability is currently resolving
     * (CR 603.2 / 603.3). Deferred triggers are flushed by the existing flush points
     * (priority grant, auto-pass, {@code SpellCastingService.finishSpellCast}).
     */
    public void enqueueTrigger(StackEntry entry) {
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
            boolean added = super.add(entry);
            if (added && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                for (int i = 1; i < gameData.activeTriggeredAbilityCopies(); i++) {
                    super.add(new StackEntry(entry));
                }
            }
            return added;
        }
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
        delayedActions.add(action);
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
        int total = delta;
        var it = delayedActions.iterator();
        while (it.hasNext()) {
            DelayedAction action = it.next();
            if (action instanceof DelayedPlusOneCounters existing && existing.permanentId().equals(permanentId)) {
                total += existing.totalCounters();
                it.remove();
            }
        }
        delayedActions.add(new DelayedPlusOneCounters(permanentId, total));
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
     * Returns the pending +1/+1 counter total scheduled for {@code permanentId} (0 if none).
     */
    public int getDelayedPlusOneCounters(UUID permanentId) {
        for (DelayedAction action : delayedActions) {
            if (action instanceof DelayedPlusOneCounters existing && existing.permanentId().equals(permanentId)) {
                return existing.totalCounters();
            }
        }
        return 0;
    }

    /**
     * Records a spell cast by the given player this turn.
     */
    public void recordSpellCast(UUID playerId, Card card) {
        spellsCastThisTurn.computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>())).add(card);
        spellCastOrderThisTurn.add(card.getId());
        mostRecentSpellCastThisTurn = card;
        spellNameCastCountsThisGame.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .merge(card.getName(), 1, Integer::sum);
        consumeNextSpellFlashGrant(playerId, card);
        consumeNextCreatureSpellEmpowerments(playerId, card);
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

    /**
     * Applies every pending creature-spell empowerment to the creature spell just cast. All pending
     * grants refer to the same "next creature spell", so they all apply and are all consumed.
     */
    private void consumeNextCreatureSpellEmpowerments(UUID playerId, Card card) {
        if (!card.hasType(CardType.CREATURE)) return;
        List<CreatureSpellEmpowerment> grants = nextCreatureSpellEmpowermentsThisTurn.get(playerId);
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

    /**
     * Adds a pending "the next spell of this type you cast this turn can be cast as though it had
     * flash" grant for the player (Quicken).
     */
    public void addNextSpellFlashGrant(UUID playerId, CardType cardType) {
        nextSpellFlashGrantsThisTurn
                .computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(cardType);
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

    /** Returns true if the player may cast spells of the card's type as though they had flash. */
    public boolean hasCardTypeFlashGrant(UUID playerId, Card card) {
        Set<CardPredicate> grants = cardTypeFlashGrantsThisTurn.get(playerId);
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
            return grants.stream().anyMatch(card::hasType);
        }
    }

    private void consumeNextSpellFlashGrant(UUID playerId, Card card) {
        List<CardType> grants = nextSpellFlashGrantsThisTurn.get(playerId);
        if (grants == null) return;
        synchronized (grants) {
            // Every pending grant names the same "next spell of this type", so one matching spell
            // consumes them all rather than only the oldest.
            grants.removeIf(card::hasType);
        }
    }

    /**
     * Returns how many spells with the given name the player has cast this game (persists across turns).
     */
    public int getSpellsCastThisGameByNameCount(UUID playerId, String name) {
        return spellNameCastCountsThisGame.getOrDefault(playerId, Map.of()).getOrDefault(name, 0);
    }

    public void addSpellCastManaSpent(UUID spellCardId, int manaSpent) {
        if (manaSpent > 0) {
            spellCastManaSpent.merge(spellCardId, manaSpent, Integer::sum);
        }
    }

    public int getSpellCastManaSpent(UUID spellCardId) {
        return spellCastManaSpent.getOrDefault(spellCardId, 0);
    }

    public void clearSpellCastManaSpent(UUID spellCardId) {
        spellCastManaSpent.remove(spellCardId);
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
     * themselves controls (The Gitrog Monster / Azusa, Lost but Seeking — controller-only).
     */
    public int getMaxLandsThisTurn(UUID playerId) {
        int extraFromStatics = 0;
        for (UUID pid : orderedPlayerIds) {
            List<Permanent> battlefield = playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof EachPlayerPlaysAdditionalLandEffect) {
                        extraFromStatics++;
                    } else if (effect instanceof PlaysAdditionalLandEachTurnEffect additional && pid.equals(playerId)) {
                        extraFromStatics += additional.amount();
                    }
                }
            }
        }
        return 1 + additionalLandsThisTurn.getOrDefault(playerId, 0) + extraFromStatics;
    }

    /**
     * Returns an unmodifiable view of the spells the given player has cast this turn.
     */
    public List<Card> getSpellsCastThisTurn(UUID playerId) {
        return Collections.unmodifiableList(spellsCastThisTurn.getOrDefault(playerId, List.of()));
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
     * that card was not cast this turn. "The second spell cast this turn" is ordinal 2.
     */
    public int getSpellCastOrdinalThisTurn(UUID cardId) {
        return spellCastOrderThisTurn.indexOf(cardId) + 1;
    }

    /**
     * Snapshots per-player spell counts into the given target map, then clears spell tracking for the new turn.
     */
    public void snapshotSpellCountsAndClear(Map<UUID, Integer> target) {
        target.clear();
        spellsCastThisTurn.forEach((id, spells) -> target.put(id, spells.size()));
        spellsCastThisTurn.clear();
        spellCastOrderThisTurn.clear();
        mostRecentSpellCastThisTurn = null;
    }

    public static final int STARTING_LIFE_TOTAL = 20;

    /**
     * Returns the current life total for the given player, defaulting to 20 if not yet set.
     */
    public int getLife(UUID playerId) {
        return playerLifeTotals.getOrDefault(playerId, STARTING_LIFE_TOTAL);
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
        playerHands.get(playerId).add(card);
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

    // ===== Exile zone helpers =====

    /** Returns cards in a player's exile zone (by owner). Never null. */
    public List<Card> getPlayerExiledCards(UUID ownerId) {
        return exiledCards.stream()
                .filter(e -> e.ownerId().equals(ownerId))
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

    /** Adds a card to exile without source tracking. */
    public void addToExile(UUID ownerId, Card card) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        exiledCards.add(new ExiledCardEntry(card, ownerId, null, false, turnNumber));
    }

    /** Adds a card to the ante zone, represented by an untracked exile entry. */
    public void addToAnte(UUID ownerId, Card card) {
        addToExile(ownerId, card);
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
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, false, turnNumber));
    }

    /** Adds a card to exile with a stash counter. Stash counters are independent of any source permanent. */
    public void addToExileWithStashCounter(UUID ownerId, Card card) {
        addToExile(ownerId, card);
        stashCounterCardIds.add(card.getId());
    }

    /** Adds a card to exile and marks it with an ice counter. */
    public void addToExileWithIceCounter(UUID ownerId, Card card) {
        addToExile(ownerId, card);
        exiledCardsWithIceCounters.add(card.getId());
    }

    /** Adds a card to exile with source permanent tracking and an explicit face-down status. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId, boolean faceDown) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, faceDown, turnNumber));
    }

    /** Adds a card to exile with source tracking, face-down status, and its exiling player. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId, boolean faceDown,
                           UUID exilerId) {
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId, faceDown, exilerId));
    }

    /** Exiles a card from hand face down as a foretell special action. */
    public void addForetoldCardToExile(UUID playerId, Card card) {
        addForetoldCardToExile(playerId, card, null);
    }

    /** Exiles a card from hand face down and remembers the foretell cost used for that action. */
    public void addForetoldCardToExile(UUID playerId, Card card, ManaCost foretellCost) {
        spellsWithDreamCounterOnResolution.remove(card.getId());
        exiledCards.add(new ExiledCardEntry(card, playerId, null, true, playerId, turnNumber));
        foretoldCardIds.add(card.getId());
        if (foretellCost != null) {
            foretoldCardCosts.put(card.getId(), foretellCost);
        }
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

    /** Removes an exiled card by card ID. Returns true if found and removed. */
    public boolean removeFromExile(UUID cardId) {
        boolean removed = exiledCards.removeIf(e -> e.card().getId().equals(cardId));
        if (removed) {
            foretoldCardIds.remove(cardId);
            foretoldCardCosts.remove(cardId);
            exilePlayPermissionSourceCards.entrySet().removeIf(entry -> cardId.equals(entry.getValue()));
            exileCastPermissionsUntilEndOfTurn.removeIf(permission -> permission.cardId().equals(cardId));
            antedCardIds.remove(cardId);
            stashCounterCardIds.remove(cardId);
            exiledCardsWithIceCounters.remove(cardId);
            exilePlayAnyManaTypeWhileExiled.remove(cardId);
            exilePlayPermissions.remove(cardId);
            exilePlayCostModifiers.remove(cardId);
            exilePlayPermissionsExpireEndOfTurn.remove(cardId);
            exilePlayPermissionsExpireAtTurnEnd.remove(cardId);
            exilePlayAnyManaType.remove(cardId);
            exilePlayWithoutPayingManaCost.remove(cardId);
            exileInsteadOfGraveyard.remove(cardId);
            exiledCardHitCounters.remove(cardId);
        }
        return removed;
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
        removedIds.forEach(antedCardIds::remove);
    }

    /** Removes source tracking from exile entries (sets sourcePermanentId to null). Used by Karn restart. */
    public void clearAllSourceTracking() {
        List<ExiledCardEntry> updated = new ArrayList<>();
        var it = exiledCards.iterator();
        while (it.hasNext()) {
            ExiledCardEntry e = it.next();
            if (e.sourcePermanentId() != null) {
                it.remove();
                updated.add(new ExiledCardEntry(e.card(), e.ownerId(), null, e.faceDown(), e.exilerId()));
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
        queueMayAbility(sourceCard, controllerId, may, targetCardId, sourcePermanentId, 0);
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
        stack.add(entry);
    }

    /**
     * Same as {@link #queueMayAbility(Card, UUID, MayEffect, UUID, UUID)} but also snapshots an
     * {@code eventValue} (e.g. combat damage dealt) onto the stack entry so that the wrapped effect
     * can reference "that many" via an {@code EventValue} amount at resolution time (e.g.
     * Cold-Eyed Selkie's "you may draw that many cards").
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId, UUID sourcePermanentId, int eventValue) {
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
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)),
                targetCardId,
                sourcePermanentId
        );
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
        GameData copy = new GameData(id, gameName, createdByUserId, createdByUsername);

        // --- Primitives, enums, UUIDs, Strings ---
        copy.status = this.status;
        copy.startingPlayerId = this.startingPlayerId;
        copy.currentStep = this.currentStep;
        copy.activePlayerId = this.activePlayerId;
        copy.turnNumber = this.turnNumber;
        copy.currentTurnIsExtraTurn = this.currentTurnIsExtraTurn;
        copy.permanentWithOilCounterPutIntoGraveyardThisTurn = this.permanentWithOilCounterPutIntoGraveyardThisTurn;
        copy.gameResult = this.gameResult;
        copy.winnerPlayerId = this.winnerPlayerId;
        copy.globalDamagePreventionShield = this.globalDamagePreventionShield;
        copy.preventAllCombatDamage = this.preventAllCombatDamage;
        copy.preventAllCombatDamageByAttackingCreatures = this.preventAllCombatDamageByAttackingCreatures;
        copy.preventAllCombatDamageToPlayers = this.preventAllCombatDamageToPlayers;
        copy.preventAllDamageToAllCreatures = this.preventAllDamageToAllCreatures;
        copy.preventAllDamageByCreatures = this.preventAllDamageByCreatures;
        copy.preventAllDamageFromNonHumanSources = this.preventAllDamageFromNonHumanSources;
        copy.combatDamageExemptPredicate = this.combatDamageExemptPredicate;
        copy.allPermanentsEnterTappedThisTurn = this.allPermanentsEnterTappedThisTurn;
        copy.additionalEnterCountersThisTurn.putAll(this.additionalEnterCountersThisTurn);
        this.colorSourceDamageBonusThisTurn.forEach((pid, colorMap) ->
                copy.colorSourceDamageBonusThisTurn.put(pid, new HashMap<>(colorMap)));
        copy.combatDamageRedirectTarget = this.combatDamageRedirectTarget;
        copy.pendingEffectResolutionEntry = this.pendingEffectResolutionEntry != null
                ? new StackEntry(this.pendingEffectResolutionEntry) : null;
        copy.pendingEffectResolutionIndex = this.pendingEffectResolutionIndex;
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
        copy.pendingDividedDamageTargetId = this.pendingDividedDamageTargetId;
        copy.pendingAbilityCounterCostActivation = this.pendingAbilityCounterCostActivation;
        copy.chosenSpellSubtype = this.chosenSpellSubtype;
        copy.chosenSpellColor = this.chosenSpellColor;
        copy.chosenSpellNumber = this.chosenSpellNumber;
        copy.chosenSpellPermanentType = this.chosenSpellPermanentType;
        copy.chosenSpellCardType = this.chosenSpellCardType;
        copy.turnaboutTap = this.turnaboutTap;
        copy.rerunCurrentEffectAfterInteraction = this.rerunCurrentEffectAfterInteraction;
        copy.deferPlayerLossCheck = this.deferPlayerLossCheck;
        copy.effectResolutionDepth = this.effectResolutionDepth;
        copy.eachPlayerRummage.active = this.eachPlayerRummage.active;
        copy.eachPlayerRummage.currentPlayerId = this.eachPlayerRummage.currentPlayerId;
        copy.eachPlayerRummage.pendingDraw = this.eachPlayerRummage.pendingDraw;
        copy.eachPlayerRummage.remaining.addAll(this.eachPlayerRummage.remaining);
        copy.eachPlayerDiscardsOrLosesLife.active = this.eachPlayerDiscardsOrLosesLife.active;
        copy.eachPlayerDiscardsOrLosesLife.currentPlayerId = this.eachPlayerDiscardsOrLosesLife.currentPlayerId;
        copy.eachPlayerDiscardsOrLosesLife.discardPending = this.eachPlayerDiscardsOrLosesLife.discardPending;
        copy.eachPlayerDiscardsOrLosesLife.remaining.addAll(this.eachPlayerDiscardsOrLosesLife.remaining);
        copy.creepingDread.active = this.creepingDread.active;
        copy.creepingDread.controllerId = this.creepingDread.controllerId;
        copy.creepingDread.currentPlayerId = this.creepingDread.currentPlayerId;
        copy.creepingDread.remaining.addAll(this.creepingDread.remaining);
        this.creepingDread.discardedCardTypes.forEach((playerId, types) ->
                copy.creepingDread.discardedCardTypes.put(playerId, Set.copyOf(types)));
        copy.dispersal.active = this.dispersal.active;
        copy.dispersal.remainingOpponentIds.addAll(this.dispersal.remainingOpponentIds);
        copy.dispersal.currentOpponentId = this.dispersal.currentOpponentId;
        copy.dispersal.selectedPermanentId = this.dispersal.selectedPermanentId;
        copy.dispersal.awaitingDiscard = this.dispersal.awaitingDiscard;
        copy.eachPlayerPayLife.active = this.eachPlayerPayLife.active;
        copy.eachPlayerPayLife.order.addAll(this.eachPlayerPayLife.order);
        copy.eachPlayerPayLife.index = this.eachPlayerPayLife.index;
        copy.eachPlayerPayLife.consecutivePasses = this.eachPlayerPayLife.consecutivePasses;
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
        copy.indulgentTormentor.active = this.indulgentTormentor.active;
        copy.indulgentTormentor.waitingForSacrifice = this.indulgentTormentor.waitingForSacrifice;
        copy.indulgentTormentor.chosenMode = this.indulgentTormentor.chosenMode;
        copy.forbiddenRitual.active = this.forbiddenRitual.active;
        copy.forbiddenRitual.controllerSacrificed = this.forbiddenRitual.controllerSacrificed;
        copy.forbiddenRitual.lifeLoss = this.forbiddenRitual.lifeLoss;
        copy.forbiddenRitual.chosenMode = this.forbiddenRitual.chosenMode;
        copy.eachPlayerSacrificeOrDiscard.active = this.eachPlayerSacrificeOrDiscard.active;
        copy.eachPlayerSacrificeOrDiscard.remaining.addAll(this.eachPlayerSacrificeOrDiscard.remaining);
        copy.eachPlayerSacrificeOrDiscard.currentPlayerId = this.eachPlayerSacrificeOrDiscard.currentPlayerId;
        copy.eachPlayerSacrificeOrDiscard.chosenMode = this.eachPlayerSacrificeOrDiscard.chosenMode;
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
        copy.forgottenLore.active = this.forgottenLore.active;
        copy.forgottenLore.chosenCardIds.addAll(this.forgottenLore.chosenCardIds);
        copy.forgottenLore.lastChosenCardId = this.forgottenLore.lastChosenCardId;
        copy.forgottenLore.pendingChosenCardId = this.forgottenLore.pendingChosenCardId;
        copy.forgottenLore.chosenMode = this.forgottenLore.chosenMode;
        copy.pendingAbilityActivation = this.pendingAbilityActivation; // immutable record
        copy.pendingGraveyardAbilityActivation = this.pendingGraveyardAbilityActivation; // immutable record
        copy.endTurnRequested = this.endTurnRequested;
        copy.discardCausedByOpponent = this.discardCausedByOpponent;
        copy.cardEnteringGraveyardByCycling = this.cardEnteringGraveyardByCycling;
        copy.additionalCombatMainPhasePairs = this.additionalCombatMainPhasePairs;
        copy.additionalCombatPhasesOnly = this.additionalCombatPhasesOnly;
        copy.additionalCombatPhasesAfterMain = this.additionalCombatPhasesAfterMain;
        copy.additionalCombatPhasesAfterMainReturnStep = this.additionalCombatPhasesAfterMainReturnStep;
        copy.combatPhasesThisTurn = this.combatPhasesThisTurn;
        copy.draftId = this.draftId;
        copy.cleanupDiscardPending = this.cleanupDiscardPending;
        copy.simulation = true;
        copy.alwaysOfferPriorityWindows = this.alwaysOfferPriorityWindows;
        copy.domainActionSequence = this.domainActionSequence;
        copy.domainEventSequence = this.domainEventSequence;
        copy.domainStateVersion = this.domainStateVersion;
        copy.timestampCounter = this.timestampCounter;
        copy.graveyardEntryVersion = this.graveyardEntryVersion;
        copy.combatDamageFirstStrikeStepComplete = this.combatDamageFirstStrikeStepComplete;
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.combatDamageFirstStrikeAssignmentPhase = this.combatDamageFirstStrikeAssignmentPhase;
        copy.pendingGraveyardReturnQueue.addAll(this.pendingGraveyardReturnQueue);
        copy.pendingEachPlayerDrawUpToQueue.addAll(this.pendingEachPlayerDrawUpToQueue);
        copy.pendingEachOtherPlayerDrawUpToQueue.addAll(this.pendingEachOtherPlayerDrawUpToQueue);
        copy.pendingRegenerationControlChanges.putAll(this.pendingRegenerationControlChanges);
        copy.unpreventableDamageInProgress = this.unpreventableDamageInProgress;

        // --- Set<UUID> (ConcurrentHashMap.newKeySet()) ---
        copy.playerIds.addAll(this.playerIds);
        copy.aiPlayerIds.addAll(this.aiPlayerIds);
        copy.playerKeptHand.addAll(this.playerKeptHand);
        copy.priorityPassedBy.addAll(this.priorityPassedBy);
        copy.preventDamageFromColors.addAll(this.preventDamageFromColors);
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);
        copy.playersWithAllDamagePrevented.addAll(this.playersWithAllDamagePrevented);
        copy.playersRedirectingAllCreatureDamage.addAll(this.playersRedirectingAllCreatureDamage);
        copy.playersWithAllPlayerDamagePrevented.addAll(this.playersWithAllPlayerDamagePrevented);
        copy.playersWithAllPlayerDamagePreventedUntilNextTurn
                .addAll(this.playersWithAllPlayerDamagePreventedUntilNextTurn);
        copy.playersWithProtectionFromEverythingUntilNextTurn
                .addAll(this.playersWithProtectionFromEverythingUntilNextTurn);
        copy.playersWithDamageFromAttackersPrevented.addAll(this.playersWithDamageFromAttackersPrevented);
        this.playersWithDamageFromMatchingSourcesPrevented.forEach((k, v) ->
                copy.playersWithDamageFromMatchingSourcesPrevented.put(k, new HashSet<>(v)));
        copy.playersGatheringSpecimensThisTurn.addAll(this.playersGatheringSpecimensThisTurn);
        copy.playersGatheringTokensThisTurn.addAll(this.playersGatheringTokensThisTurn);
        copy.playersExilingUncastEnteringCreaturesThisTurn.addAll(this.playersExilingUncastEnteringCreaturesThisTurn);
        copy.playersExilingUncastEnteringNontokenCreaturesThisTurn
                .addAll(this.playersExilingUncastEnteringNontokenCreaturesThisTurn);
        copy.playersWhoActivatedLoyaltyAbilityThisTurn.addAll(this.playersWhoActivatedLoyaltyAbilityThisTurn);
        copy.playersWhoPlayedCardFromExileThisTurn.addAll(this.playersWhoPlayedCardFromExileThisTurn);
        copy.playersWhoActivatedExhaustAbilityThisTurn.addAll(this.playersWhoActivatedExhaustAbilityThisTurn);
        copy.creaturesWithAllDamagePrevented.addAll(this.creaturesWithAllDamagePrevented);
        copy.permanentsPreventedFromDealingDamageUntilNextTurn.putAll(this.permanentsPreventedFromDealingDamageUntilNextTurn);
        copy.permanentsProtectedFromDamageUntilNextTurn.putAll(this.permanentsProtectedFromDamageUntilNextTurn);
        copy.allDamagePreventionPredicates.addAll(this.allDamagePreventionPredicates);
        this.combatDamagePreventionPredicatesByController.forEach((controllerId, predicates) ->
                copy.combatDamagePreventionPredicatesByController
                        .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                        .addAll(predicates));
        copy.creaturesWithCombatDamagePrevented.addAll(this.creaturesWithCombatDamagePrevented);
        copy.creaturesPreventedFromDealingCombatDamage.addAll(this.creaturesPreventedFromDealingCombatDamage);
        this.colorDamagePreventionUntilEndOfTurn.forEach((targetId, colors) -> {
            Set<CardColor> copied = ConcurrentHashMap.newKeySet();
            copied.addAll(colors);
            copy.colorDamagePreventionUntilEndOfTurn.put(targetId, copied);
        });
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
        copy.playersCantGainLifeThisTurn = this.playersCantGainLifeThisTurn;
        copy.creaturesCantAttackThisTurn = this.creaturesCantAttackThisTurn;
        copy.combatDamageToCreaturesDoublingsThisTurn = this.combatDamageToCreaturesDoublingsThisTurn;
        copy.controllerDamageDoublingsThisTurn.putAll(this.controllerDamageDoublingsThisTurn);
        copy.permanentDamageDoublingsThisTurn.putAll(this.permanentDamageDoublingsThisTurn);
        copy.opponentGraveyardLifeLossWatchers.addAll(this.opponentGraveyardLifeLossWatchers);
        copy.lifeGainOpponentLifeLossWatchers.addAll(this.lifeGainOpponentLifeLossWatchers);
        copy.temporaryGlobalTriggeredAbilities.addAll(this.temporaryGlobalTriggeredAbilities);
        copy.creatureDeathTriggerWatchers.addAll(this.creatureDeathTriggerWatchers);
        copy.damageRedirectShields.addAll(this.damageRedirectShields);
        copy.sourceDamageRedirectShields.addAll(this.sourceDamageRedirectShields);
        copy.creatureDamageRedirectShields.addAll(this.creatureDamageRedirectShields);
        copy.turnDamageRedirectToCreatureShields.addAll(this.turnDamageRedirectToCreatureShields);
        copy.creatureControllerDamageRedirectShields.addAll(this.creatureControllerDamageRedirectShields);
        copy.turnSourceDamageRedirectToControllerShields.addAll(this.turnSourceDamageRedirectToControllerShields);
        copy.playerNextDamageRedirectShields.addAll(this.playerNextDamageRedirectShields);
        copy.playerSourceNextDamageRedirectShields.addAll(this.playerSourceNextDamageRedirectShields);
        copy.playerNextInstantOrSorceryDamageRedirectShields.addAll(this.playerNextInstantOrSorceryDamageRedirectShields);
        copy.sourceNextCombatDamageToOpponentRedirectShields.addAll(this.sourceNextCombatDamageToOpponentRedirectShields);
        copy.targetSourceDamagePreventionShields.addAll(this.targetSourceDamagePreventionShields);
        copy.damagePreventionLifeGainShields.addAll(this.damagePreventionLifeGainShields);
        copy.playerSourceNextDamageShields.addAll(this.playerSourceNextDamageShields);
        copy.sourceNextDamageToAnyTargetShields.addAll(this.sourceNextDamageToAnyTargetShields);
        copy.eyeForAnEyeShields.addAll(this.eyeForAnEyeShields);
        copy.reflectDamageToSourceControllerShields.addAll(this.reflectDamageToSourceControllerShields);
        copy.sourceNextDamageRedirectToPermanentShields.addAll(this.sourceNextDamageRedirectToPermanentShields);
        copy.pendingEyeForAnEyeReflections.addAll(this.pendingEyeForAnEyeReflections);
        this.pendingSourceDamageForReflection.forEach((sourceId, pending) ->
                copy.pendingSourceDamageForReflection.put(sourceId, pending.copy()));
        copy.stateTriggerOnStack.addAll(this.stateTriggerOnStack);

        // --- List<UUID> (synchronized) ---
        copy.orderedPlayerIds.addAll(this.orderedPlayerIds);
        copy.playerNames.addAll(this.playerNames);

        // --- Map<UUID, String/Integer> ---
        copy.playerIdToName.putAll(this.playerIdToName);
        copy.imprintedCards.putAll(this.imprintedCards);
        this.notedMana.forEach((cardId, mana) -> copy.notedMana.put(cardId, new EnumMap<>(mana)));
        this.abilityActivationManaSpent.forEach((cardId, mana) ->
                copy.abilityActivationManaSpent.put(cardId, new EnumMap<>(mana)));
        copy.playerDeckChoices.putAll(this.playerDeckChoices);
        copy.mulliganCounts.putAll(this.mulliganCounts);
        copy.playerNeedsToBottom.putAll(this.playerNeedsToBottom);
        copy.playerMulliganDecisionIds.putAll(this.playerMulliganDecisionIds);
        copy.playerBottomDecisionIds.putAll(this.playerBottomDecisionIds);
        copy.landsPlayedThisTurn.putAll(this.landsPlayedThisTurn);
        copy.turnsTakenByPlayer.putAll(this.turnsTakenByPlayer);
        copy.additionalLandsThisTurn.putAll(this.additionalLandsThisTurn);
        this.permanentsEnteredBattlefieldThisTurn.forEach((k, v) ->
                copy.permanentsEnteredBattlefieldThisTurn.put(k, new ArrayList<>(v)));
        this.permanentsEnteredBattlefieldLastTurn.forEach((k, v) ->
                copy.permanentsEnteredBattlefieldLastTurn.put(k, new ArrayList<>(v)));
        this.spellsCastThisTurn.forEach((k, v) ->
                copy.spellsCastThisTurn.put(k, new ArrayList<>(v)));
        copy.spellCastOrderThisTurn.addAll(this.spellCastOrderThisTurn);
        copy.mostRecentSpellCastThisTurn = this.mostRecentSpellCastThisTurn;
        this.spellNameCastCountsThisGame.forEach((k, v) ->
                copy.spellNameCastCountsThisGame.put(k, new ConcurrentHashMap<>(v)));
        copy.spellsCastLastTurn.putAll(this.spellsCastLastTurn);
        copy.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn
                .addAll(this.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn);
        copy.playersWithCityBlessing.addAll(this.playersWithCityBlessing);
        copy.playersWhoSurveilledThisTurn.addAll(this.playersWhoSurveilledThisTurn);
        copy.playersDeclaredAttackersThisTurn.addAll(this.playersDeclaredAttackersThisTurn);
        copy.playersWhoPutCountersOnCreaturesThisTurn.addAll(this.playersWhoPutCountersOnCreaturesThisTurn);
        copy.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn
                .addAll(this.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn);
        copy.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn
                .addAll(this.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn);
        copy.playersWhoSacrificedPermanentsThisTurn.addAll(this.playersWhoSacrificedPermanentsThisTurn);
        copy.creaturesAttackedCountThisTurn.putAll(this.creaturesAttackedCountThisTurn);
        this.creaturesAttackedCountBySubtypeThisTurn.forEach((playerId, counts) ->
                copy.creaturesAttackedCountBySubtypeThisTurn.put(playerId, new ConcurrentHashMap<>(counts)));
        copy.playerLifeTotals.putAll(this.playerLifeTotals);
        copy.playerPoisonCounters.putAll(this.playerPoisonCounters);
        copy.playersAffectedByMeliraPoisonReplacementThisTurn
                .addAll(this.playersAffectedByMeliraPoisonReplacementThisTurn);
        copy.playerEnergyCounters.putAll(this.playerEnergyCounters);
        copy.playerSpeeds.putAll(this.playerSpeeds);
        copy.playersWhoseSpeedIncreasedThisTurn.addAll(this.playersWhoseSpeedIncreasedThisTurn);
        copy.playerDamagePreventionShields.putAll(this.playerDamagePreventionShields);
        copy.playerCombatDamagePreventionShields.putAll(this.playerCombatDamagePreventionShields);
        copy.stolenCreatures.putAll(this.stolenCreatures);
        copy.drawReplacementTargetToController.putAll(this.drawReplacementTargetToController);
        copy.drawStepFirstDrawTaken.addAll(this.drawStepFirstDrawTaken);
        copy.pendingNextDrawLookAtTop.putAll(this.pendingNextDrawLookAtTop);
        this.pendingNextDrawFromExiledPile.forEach((k, v) ->
                copy.pendingNextDrawFromExiledPile.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.pendingMysticReflections.addAll(this.pendingMysticReflections);
        copy.activeMysticReflectionsForEntryBatch.addAll(this.activeMysticReflectionsForEntryBatch);
        copy.cardsDrawnThisTurn.putAll(this.cardsDrawnThisTurn);
        this.cardsDrawnThisTurnIds.forEach((k, v) -> copy.cardsDrawnThisTurnIds.put(k, new ArrayList<>(v)));
        copy.cardsDiscardedThisTurn.putAll(this.cardsDiscardedThisTurn);
        copy.discardEventPlayerId = this.discardEventPlayerId;
        copy.discardEventCardCount = this.discardEventCardCount;
        copy.lastDiscardedCardManaValue = this.lastDiscardedCardManaValue;
        copy.lastDiscardedCardTypes = Set.copyOf(this.lastDiscardedCardTypes);
        copy.lastMilledCardColorSymbols.putAll(this.lastMilledCardColorSymbols);
        copy.lifeGainedThisTurn.putAll(this.lifeGainedThisTurn);
        this.combatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        copy.combatDamageSourcesThatDealtToCreaturesThisTurn
                .addAll(this.combatDamageSourcesThatDealtToCreaturesThisTurn);
        this.noncombatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.noncombatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.creatureDamageToPlayersThisTurn.forEach((k, v) ->
                copy.creatureDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        this.playersAttackedThisTurn.forEach((k, v) ->
                copy.playersAttackedThisTurn.put(k, new HashSet<>(v)));
        copy.damageDealtThisTurnBySource.putAll(this.damageDealtThisTurnBySource);
        copy.permanentsThatHaveDealtDamage.addAll(this.permanentsThatHaveDealtDamage);
        this.damageRecipientsBySource.forEach((k, v) -> {
            Set<UUID> recipients = ConcurrentHashMap.newKeySet();
            recipients.addAll(v);
            copy.damageRecipientsBySource.put(k, recipients);
        });
        copy.playersDealtDamageThisTurn.addAll(this.playersDealtDamageThisTurn);
        copy.damageDealtToPlayersThisTurn.putAll(this.damageDealtToPlayersThisTurn);
        copy.noncombatDamageDealtToPlayersThisTurn.putAll(this.noncombatDamageDealtToPlayersThisTurn);
        copy.lastRedSpellDamagerThisTurn.putAll(this.lastRedSpellDamagerThisTurn);
        copy.untappedLandsAtTurnStart.putAll(this.untappedLandsAtTurnStart);
        copy.handSizeAtTurnStart.putAll(this.handSizeAtTurnStart);
        copy.permanentsDealtDamageThisTurn.addAll(this.permanentsDealtDamageThisTurn);
        copy.damageDealtToPermanentsThisTurn.putAll(this.damageDealtToPermanentsThisTurn);
        this.qualifyingDamageControllersByPermanentThisTurn.forEach((k, v) -> {
            Set<UUID> controllers = ConcurrentHashMap.newKeySet();
            controllers.addAll(v);
            copy.qualifyingDamageControllersByPermanentThisTurn.put(k, controllers);
        });
        copy.freeCastPermanentUsedThisTurn.addAll(this.freeCastPermanentUsedThisTurn);
        copy.oncePerTurnTriggersFiredThisTurn.addAll(this.oncePerTurnTriggersFiredThisTurn);
        copy.permanentsThatAddedManaWithAbilityThisTurn.addAll(this.permanentsThatAddedManaWithAbilityThisTurn);
        this.firstResolutionTriggerKeysThisTurn.forEach((k, v) -> {
            Set<String> keys = ConcurrentHashMap.newKeySet();
            keys.addAll(v);
            copy.firstResolutionTriggerKeysThisTurn.put(k, keys);
        });
        copy.onceEachTurnAttackTriggersFiredThisTurn.addAll(this.onceEachTurnAttackTriggersFiredThisTurn);
        copy.tokenCreationReplacementUsedThisTurn.addAll(this.tokenCreationReplacementUsedThisTurn);
        copy.pendingTokenCreationReplacement = this.pendingTokenCreationReplacement;
        copy.simultaneousDyingCreatures.putAll(this.simultaneousDyingCreatures);
        copy.simultaneousDyingControllers.putAll(this.simultaneousDyingControllers);
        this.combatDamageSourceSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageSourceSubtypesThisTurn.put(k, new HashSet<>(v)));
        copy.combatDamageSourcesWithChangelingThisTurn.addAll(this.combatDamageSourcesWithChangelingThisTurn);
        this.combatDamageToPlayerControllerSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayerControllerSubtypesThisTurn.put(k, new HashSet<>(v)));
        copy.controllersDealtCombatDamageWithChangelingThisTurn.addAll(this.controllersDealtCombatDamageWithChangelingThisTurn);
        this.combatBlockOpponentSubtypesThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentSubtypesThisTurn.put(k, new HashSet<>(v)));
        this.combatBlockOpponentColorsThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentColorsThisTurn.put(k, new HashSet<>(v)));
        copy.creaturesInCombatWithChangelingThisTurn.addAll(this.creaturesInCombatWithChangelingThisTurn);
        copy.creaturesBlockedThisTurn.addAll(this.creaturesBlockedThisTurn);
        this.combatBlockOpponentIdsThisTurn.forEach((k, v) ->
                copy.combatBlockOpponentIdsThisTurn.put(k, new HashSet<>(v)));
        this.combatBlockOpponentIdsThisCombat.forEach((k, v) ->
                copy.combatBlockOpponentIdsThisCombat.put(k, new HashSet<>(v)));
        this.combatOpponentIdsBlockedByThisTurn.forEach((k, v) ->
                copy.combatOpponentIdsBlockedByThisTurn.put(k, new HashSet<>(v)));

        // --- Map<UUID, Set<TurnStep>> ---
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.put(k, new HashSet<>(v)));

        // --- Map<UUID, List<Card>> (shared Card refs) ---
        this.playerDecks.forEach((k, v) -> copy.playerDecks.put(k, new ArrayList<>(v)));
        this.playerSideboards.forEach((k, v) -> copy.playerSideboards.put(k, new ArrayList<>(v)));
        this.playerHands.forEach((k, v) -> copy.playerHands.put(k, new ArrayList<>(v)));
        this.playerGraveyards.forEach((k, v) -> copy.playerGraveyards.put(k, new ArrayList<>(v)));
        this.playerCommandZones.forEach((k, v) -> copy.playerCommandZones.put(k, new ArrayList<>(v)));
        copy.exiledCards.addAll(this.exiledCards);
        copy.antedCardIds.addAll(this.antedCardIds);
        copy.exiledCardEggCounters.putAll(this.exiledCardEggCounters);
        copy.exiledCardDreamCounters.putAll(this.exiledCardDreamCounters);
        copy.exiledCardHitCounters.putAll(this.exiledCardHitCounters);
        copy.spellsWithDreamCounterOnResolution.addAll(this.spellsWithDreamCounterOnResolution);
        copy.exiledCardsWithSilverCounters.addAll(this.exiledCardsWithSilverCounters);
        copy.delayedSpellExiles.addAll(this.delayedSpellExiles);

        // --- Map<UUID, List<Permanent>> (deep copy each Permanent) ---
        this.playerBattlefields.forEach((k, v) -> {
            List<Permanent> battlefieldCopy = copy.newBattlefieldList();
            v.stream().map(Permanent::new).forEach(battlefieldCopy::add);
            copy.playerBattlefields.put(k, battlefieldCopy);
        });
        this.phasedOutPermanents.forEach((k, v) -> copy.phasedOutPermanents.put(k,
                Collections.synchronizedList(v.stream().map(Permanent::new)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))));

        // --- Map<UUID, ManaPool> (deep copy each ManaPool) ---
        this.playerManaPools.forEach((k, v) -> copy.playerManaPools.put(k, new ManaPool(v)));

        // --- List<StackEntry> (deep copy each StackEntry) ---
        this.stack.forEach(se -> copy.stack.add(new StackEntry(se)));
        this.pendingManaAbilityTriggers.forEach(se -> copy.pendingManaAbilityTriggers.add(new StackEntry(se)));
        this.pendingSpellCastCostTriggers.forEach(se -> copy.pendingSpellCastCostTriggers.add(new StackEntry(se)));
        this.pendingActivatedAbilityCostTriggers.forEach(se -> copy.pendingActivatedAbilityCostTriggers.add(new StackEntry(se)));

        // --- InteractionState ---
        InteractionState copiedInteraction = this.interaction.deepCopy();
        copyInteractionInto(copy, copiedInteraction);

        // --- Map<UUID, Set<UUID>> ---
        this.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) ->
                copy.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, new HashSet<>(v)));
        copy.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn
                .addAll(this.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn);
        this.cardsPutIntoGraveyardFromAnywhereThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromAnywhereThisTurn.put(k, new HashSet<>(v)));
        this.cardsDiscardedOrCycledThisTurn.forEach((k, v) ->
                copy.cardsDiscardedOrCycledThisTurn.put(k, new HashSet<>(v)));
        copy.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn
                .addAll(this.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn);
        this.cardsDiscardedByOpponentThisTurn.forEach((k, v) ->
                copy.cardsDiscardedByOpponentThisTurn.put(k, new HashSet<>(v)));
        copy.playersWhosePermanentsLeftBattlefieldThisTurn
                .addAll(this.playersWhosePermanentsLeftBattlefieldThisTurn);
        copy.creatureDeathCountThisTurn.putAll(this.creatureDeathCountThisTurn);
        copy.nontokenCreatureDeathCountThisTurn.putAll(this.nontokenCreatureDeathCountThisTurn);
        this.creatureSubtypeDeathCountThisTurn.forEach((k, v) ->
                copy.creatureSubtypeDeathCountThisTurn.put(k, new HashMap<>(v)));
        this.sacrificedPermanentSubtypeCountThisTurn.forEach((k, v) ->
                copy.sacrificedPermanentSubtypeCountThisTurn.put(k, new HashMap<>(v)));
        this.creatureCardsDamagedThisTurnBySourcePermanent.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnBySourcePermanent.put(k, new HashSet<>(v)));
        copy.sourcesWhoseDamagedCreaturesDiedThisTurn.addAll(this.sourcesWhoseDamagedCreaturesDiedThisTurn);
        this.creatureCardsDamagedBySourceThatDiedThisTurn.forEach((k, v) ->
                copy.creatureCardsDamagedBySourceThatDiedThisTurn.put(k, new HashSet<>(v)));
        copy.creatureGivingControllerPoisonOnDeathThisTurn.putAll(this.creatureGivingControllerPoisonOnDeathThisTurn);
        this.creaturesReturnedToBattlefieldOnDeathThisTurn.forEach((k, v) ->
                copy.creaturesReturnedToBattlefieldOnDeathThisTurn.put(k, new ArrayList<>(v)));
        this.creatureTriggeringEffectOnDeathThisTurn.forEach((k, v) ->
                copy.creatureTriggeringEffectOnDeathThisTurn.put(k, new ArrayList<>(v)));
        this.seraphReturnedCreatures.forEach((k, v) ->
                copy.seraphReturnedCreatures.put(k, new HashSet<>(v)));
        copy.seraphControlWatch.putAll(this.seraphControlWatch);
        copy.exiledCardsToGraveyardOnControlLossWatch.putAll(this.exiledCardsToGraveyardOnControlLossWatch);
        this.sourceCreatedTokens.forEach((k, v) ->
                copy.sourceCreatedTokens.put(k, new HashSet<>(v)));

        // --- Map<UUID, Map<CardColor, Integer>> ---
        this.playerColorDamagePreventionCount.forEach((k, v) ->
                copy.playerColorDamagePreventionCount.put(k, new HashMap<>(v)));

        // --- PendingMayAbility list (records with shared Card refs) ---
        copy.pendingMayAbilities.addAll(this.pendingMayAbilities);
        copy.tariffRemainingPlayers.addAll(this.tariffRemainingPlayers);
        copy.forcedCostOrElseRemainingPlayers.addAll(this.forcedCostOrElseRemainingPlayers);
        copy.forcedCostOrElseSourceControllerId = this.forcedCostOrElseSourceControllerId;
        copy.anyPlayerMayPayManaRemainingPlayers.addAll(this.anyPlayerMayPayManaRemainingPlayers);
        copy.eachPlayerDamageUnlessPaysRemaining.addAll(this.eachPlayerDamageUnlessPaysRemaining);
        copy.revealHandDiscardUnlessPaysRemaining.addAll(this.revealHandDiscardUnlessPaysRemaining);
        copy.destroyDamagersUnlessPaysRemaining.addAll(this.destroyDamagersUnlessPaysRemaining);
        copy.bounceUnlessPaysRemaining.addAll(this.bounceUnlessPaysRemaining);

        // --- Unified delayed-action queue (immutable records, shallow copy — shared Card refs, as the
        //     per-mechanic fields it replaced were copied) ---
        copy.delayedActions.addAll(this.delayedActions);

        // --- Exile-until-source-leaves map (O-ring style) ---
        this.exileReturnOnPermanentLeave.forEach((k, v) ->
                copy.exileReturnOnPermanentLeave.put(k, new ArrayList<>(v)));

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---
        this.playerSourceDamagePreventionIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionIds.put(k, new HashSet<>(v)));
        this.playerSourceDamagePreventionLifeGainIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionLifeGainIds.put(k, new HashSet<>(v)));

        // --- GraveyardTargetOperationState ---
        copy.graveyardTargetOperation.card = this.graveyardTargetOperation.card;
        copy.graveyardTargetOperation.controllerId = this.graveyardTargetOperation.controllerId;
        copy.graveyardTargetOperation.effects = this.graveyardTargetOperation.effects;
        copy.graveyardTargetOperation.entryType = this.graveyardTargetOperation.entryType;
        copy.graveyardTargetOperation.xValue = this.graveyardTargetOperation.xValue;
        copy.graveyardTargetOperation.anyNumber = this.graveyardTargetOperation.anyNumber;
        copy.graveyardTargetOperation.singleGraveyard = this.graveyardTargetOperation.singleGraveyard;
        copy.graveyardTargetOperation.cumulativeUpkeepPayment = this.graveyardTargetOperation.cumulativeUpkeepPayment;
        copy.graveyardTargetOperation.targetPlayerId = this.graveyardTargetOperation.targetPlayerId;
        copy.graveyardTargetOperation.spellCounterTargetId = this.graveyardTargetOperation.spellCounterTargetId;
        copy.graveyardTargetOperation.permanentTargetIds = this.graveyardTargetOperation.permanentTargetIds == null
                ? null : new ArrayList<>(this.graveyardTargetOperation.permanentTargetIds);
        copy.graveyardTargetOperation.resolutionTimeExileResume = this.graveyardTargetOperation.resolutionTimeExileResume;
        copy.graveyardTargetOperation.resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume =
                this.graveyardTargetOperation.resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume;
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
        copy.graveyardTargetOperation.resolutionTimeForgottenLoreResume =
                this.graveyardTargetOperation.resolutionTimeForgottenLoreResume;
        copy.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume =
                this.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume;
        copy.graveyardTargetOperation.phyrexianGrimoireChosenCardId =
                this.graveyardTargetOperation.phyrexianGrimoireChosenCardId;

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
        copy.cloneOperation.controllerId = this.cloneOperation.controllerId;
        copy.cloneOperation.etbTargetId = this.cloneOperation.etbTargetId;
        copy.cloneOperation.powerOverride = this.cloneOperation.powerOverride;
        copy.cloneOperation.toughnessOverride = this.cloneOperation.toughnessOverride;
        copy.cloneOperation.copyPowerToughnessFromSource = this.cloneOperation.copyPowerToughnessFromSource;
        copy.cloneOperation.additionalTypesOverride = this.cloneOperation.additionalTypesOverride;
        copy.cloneOperation.additionalActivatedAbilities = this.cloneOperation.additionalActivatedAbilities;
        copy.cloneOperation.nameOverride = this.cloneOperation.nameOverride;
        copy.cloneOperation.additionalSupertypesOverride = this.cloneOperation.additionalSupertypesOverride;

        // --- WarpWorldOperationState ---
        copy.warpWorldOperation.pendingAuraChoices.addAll(this.warpWorldOperation.pendingAuraChoices);
        copy.warpWorldOperation.pendingEnchantmentPlacements.addAll(this.warpWorldOperation.pendingEnchantmentPlacements);
        this.warpWorldOperation.pendingCreaturesByPlayer.forEach((k, v) ->
                copy.warpWorldOperation.pendingCreaturesByPlayer.put(k, new ArrayList<>(v)));
        copy.warpWorldOperation.enterTappedTypesSnapshot.addAll(this.warpWorldOperation.enterTappedTypesSnapshot);
        copy.warpWorldOperation.needsLegendChecks = this.warpWorldOperation.needsLegendChecks;
        copy.warpWorldOperation.sourceName = this.warpWorldOperation.sourceName;

        // --- Map<UUID, Map<Integer, Integer>> (activated ability uses) ---
        this.activatedAbilityUsesThisTurn.forEach((k, v) ->
                copy.activatedAbilityUsesThisTurn.put(k, new HashMap<>(v)));
        this.activatedAbilityUsesThisGame.forEach((k, v) ->
                copy.activatedAbilityUsesThisGame.put(k, new HashMap<>(v)));
        copy.permanentAbilityResolutionsThisTurn.putAll(this.permanentAbilityResolutionsThisTurn);

        // --- Deques ---
        copy.pendingInteractions.addAll(this.pendingInteractions);
        copy.extraTurns.addAll(this.extraTurns);
        copy.extraTurnSkipsUntap.addAll(this.extraTurnSkipsUntap);
        this.pendingLibraryBottomReorders.forEach(req ->
                copy.pendingLibraryBottomReorders.add(new LibraryBottomReorderRequest(req.playerId(), new ArrayList<>(req.cards()))));

        // --- Combat damage assignment state ---
        this.combatDamagePlayerAssignments.forEach((k, v) ->
                copy.combatDamagePlayerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingIndices.addAll(this.combatDamagePendingIndices);
        this.combatDamageBlockerAssignments.forEach((k, v) ->
                copy.combatDamageBlockerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingBlockerIndices.addAll(this.combatDamagePendingBlockerIndices);
        copy.combatDamagePhase1State = this.combatDamagePhase1State; // read-only snapshot from phase 1

        // --- Emblems (records are immutable) ---
        copy.emblems.addAll(this.emblems);

        // --- Floating continuous effects (immutable records, safe to share) ---
        copy.floatingEffects.addAll(this.floatingEffects);

        // --- Permanent no-max-hand-size grants ---
        copy.playersWithNoMaximumHandSize.addAll(this.playersWithNoMaximumHandSize);
        copy.playersWithNoMaximumHandSizeUntilNextTurn.addAll(this.playersWithNoMaximumHandSizeUntilNextTurn);

        // --- Permanent "can't gain life" grants (Stigma Lasher) ---
        copy.playersWhoCantGainLifeRestOfGame.addAll(this.playersWhoCantGainLifeRestOfGame);
        copy.playersWhoCantGainLifeThisTurn.addAll(this.playersWhoCantGainLifeThisTurn);

        // --- Source-linked animations (Awakener Druid-style) ---
        copy.sourceLinkedAnimations.putAll(this.sourceLinkedAnimations);

        // --- Per-player spell/creature color protection (Autumn's Veil style) ---
        this.playerSpellsCantBeCounteredByColorsThisTurn.forEach((k, v) ->
                copy.playerSpellsCantBeCounteredByColorsThisTurn.put(k, new HashSet<>(v)));
        copy.playersSpellsCantBeCounteredThisTurn.addAll(this.playersSpellsCantBeCounteredThisTurn);
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

        // --- Silence-style "opponents can't cast" flag ---
        copy.playersSilencedThisTurn.addAll(this.playersSilencedThisTurn);
        copy.playersCantCastSpellsForRestOfGame.addAll(this.playersCantCastSpellsForRestOfGame);
        this.opponentsCantCastNamedSpellsUntilControllerNextTurn.forEach((k, v) ->
                copy.opponentsCantCastNamedSpellsUntilControllerNextTurn.put(k, new HashSet<>(v)));
        this.playersCantCastNoncreatureSpellsUntilControllerNextTurn.forEach((k, v) ->
                copy.playersCantCastNoncreatureSpellsUntilControllerNextTurn.put(k, new HashSet<>(v)));
        copy.extraManaOnLandSubtypeTapThisTurn.putAll(this.extraManaOnLandSubtypeTapThisTurn);
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
        copy.senControllerPlayerId = this.senControllerPlayerId;
        copy.senControlledPlayerId = this.senControlledPlayerId;

        // --- Spell copy until end of turn (The Mirari Conjecture chapter III) ---
        copy.playersWithSpellCopyUntilEndOfTurn.addAll(this.playersWithSpellCopyUntilEndOfTurn);
        copy.conspiredSpellIds.addAll(this.conspiredSpellIds);

        // --- Pending one-shot spell copy triggers (Primal Wellspring) ---
        copy.pendingNextInstantSorceryCopyCount.putAll(this.pendingNextInstantSorceryCopyCount);
        copy.pendingNextRedInstantSorceryCopyCount.putAll(this.pendingNextRedInstantSorceryCopyCount);
        copy.pendingNextInstantSorceryCopyThisTurnCount.putAll(this.pendingNextInstantSorceryCopyThisTurnCount);
        copy.pendingNextLoyaltyAbilityCopyThisTurnCount.putAll(this.pendingNextLoyaltyAbilityCopyThisTurnCount);
        copy.pendingNextExhaustAbilityCopyThisTurnCount.putAll(this.pendingNextExhaustAbilityCopyThisTurnCount);
        copy.creatureSpellCastDrawsThisTurn.putAll(this.creatureSpellCastDrawsThisTurn);
        this.creatureEntersDrawSourcesThisTurn.forEach((playerId, cards) ->
                copy.creatureEntersDrawSourcesThisTurn.put(playerId, new ArrayList<>(cards)));
        this.creatureEntersDrawSources.forEach((playerId, cards) ->
                copy.creatureEntersDrawSources.put(playerId, new ArrayList<>(cards)));
        this.untapAttackedCreaturesEachCombatThisTurnSources.forEach((playerId, cards) ->
                copy.untapAttackedCreaturesEachCombatThisTurnSources.put(playerId, new ArrayList<>(cards)));

        copy.libraryTopCardFreePlayPermissionsUntilEndOfTurn.putAll(this.libraryTopCardFreePlayPermissionsUntilEndOfTurn);
        copy.exilePlayPermissions.putAll(this.exilePlayPermissions);
        copy.exilePlayPermissionSourceCards.putAll(this.exilePlayPermissionSourceCards);
        copy.exilePlayCostModifiers.putAll(this.exilePlayCostModifiers);
        copy.exilePlayPermissionsExpireEndOfTurn.addAll(this.exilePlayPermissionsExpireEndOfTurn);
        copy.exilePlayPermissionsExpireAtTurnEnd.putAll(this.exilePlayPermissionsExpireAtTurnEnd);
        copy.exilePlayAnyManaType.addAll(this.exilePlayAnyManaType);
        copy.exilePlayAnyManaTypeWhileExiled.addAll(this.exilePlayAnyManaTypeWhileExiled);
        copy.stashCounterCardIds.addAll(this.stashCounterCardIds);
        copy.exilePlayWithoutPayingManaCost.addAll(this.exilePlayWithoutPayingManaCost);
        copy.exileInsteadOfGraveyard.addAll(this.exileInsteadOfGraveyard);
        copy.graveyardPlayPermissions.putAll(this.graveyardPlayPermissions);
        copy.graveyardPlayPermissionsExpireEndOfTurn.addAll(this.graveyardPlayPermissionsExpireEndOfTurn);
        copy.graveyardCardsEnterTapped.addAll(this.graveyardCardsEnterTapped);
        copy.graveyardCastFilterPermissionsThisTurn.addAll(this.graveyardCastFilterPermissionsThisTurn);
        copy.exileCastPermissionsUntilEndOfTurn.addAll(this.exileCastPermissionsUntilEndOfTurn);
        copy.playersExilingCardsInsteadOfGraveyardThisTurn.addAll(this.playersExilingCardsInsteadOfGraveyardThisTurn);
        copy.graveyardLeaveNotificationDepth = this.graveyardLeaveNotificationDepth;
        copy.graveyardLeaveNotificationPendingOwners.addAll(this.graveyardLeaveNotificationPendingOwners);
        copy.graveyardLeaveNotificationPendingCreatureOwners.addAll(this.graveyardLeaveNotificationPendingCreatureOwners);
        copy.graveyardLeaveNotificationPendingArtifactOrCreatureOwners.addAll(this.graveyardLeaveNotificationPendingArtifactOrCreatureOwners);
        copy.graveyardExileNotificationPendingCounts.putAll(this.graveyardExileNotificationPendingCounts);
        copy.graveyardOrBattlefieldExileNotificationPending = this.graveyardOrBattlefieldExileNotificationPending;
        copy.playersWhoseCardsLeftGraveyardThisTurn.addAll(this.playersWhoseCardsLeftGraveyardThisTurn);

        // --- Search tax payments (Leonin Arbiter) ---
        this.paidSearchTaxPermanentIds.forEach((k, v) ->
                copy.paidSearchTaxPermanentIds.put(k, new HashSet<>(v)));

        // --- ETB / sacrifice damage assignments ---
        copy.pendingETBDamageAssignments = this.pendingETBDamageAssignments.isEmpty()
                ? Map.of() : new HashMap<>(this.pendingETBDamageAssignments);

        // --- Mindslaver turn control ---
        copy.pendingTurnControl.putAll(this.pendingTurnControl);
        copy.pendingTurnControlExtraTurn.addAll(this.pendingTurnControlExtraTurn);
        copy.mindControlledPlayerId = this.mindControlledPlayerId;
        copy.mindControllerPlayerId = this.mindControllerPlayerId;
        copy.tauntedNextTurn.putAll(this.tauntedNextTurn);
        copy.tauntedThisTurn.putAll(this.tauntedThisTurn);
        copy.creatureMustAttackPermanentNextTurn.putAll(this.creatureMustAttackPermanentNextTurn);
        this.chosenAttackersNextTurn.forEach((playerId, ids) -> copy.chosenAttackersNextTurn.put(playerId, Set.copyOf(ids)));
        this.chosenAttackersThisTurn.forEach((playerId, ids) -> copy.chosenAttackersThisTurn.put(playerId, Set.copyOf(ids)));
        this.attackableCreaturesThisTurn.forEach((playerId, ids) -> copy.attackableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        this.blockableCreaturesThisTurn.forEach((playerId, ids) -> copy.blockableCreaturesThisTurn.put(playerId, Set.copyOf(ids)));
        copy.otherCreaturesCantAttackExemptCreatureIds.addAll(this.otherCreaturesCantAttackExemptCreatureIds);
        copy.peaceTalksTurnsRemaining = this.peaceTalksTurnsRemaining;
        copy.currentlyResolvingControllerId = this.currentlyResolvingControllerId;

        // --- Opening hand reveal triggers (Chancellor cycle) ---
        copy.openingHandRevealTriggers.addAll(this.openingHandRevealTriggers);
        copy.openingHandManaTriggers.addAll(this.openingHandManaTriggers);
        copy.playersWhoCastFirstSpellInGame.addAll(this.playersWhoCastFirstSpellInGame);
        copy.paradigmDelayedTriggers.addAll(this.paradigmDelayedTriggers);
        this.paradigmResolvedSpellNames.forEach((k, v) -> {
            Set<String> names = ConcurrentHashMap.newKeySet();
            names.addAll(v);
            copy.paradigmResolvedSpellNames.put(k, names);
        });
        copy.pendingFreeCastQueue.addAll(this.pendingFreeCastQueue);
        copy.pendingFreeCastAsCopyIds.addAll(this.pendingFreeCastAsCopyIds);
        copy.pendingExileFreeCastRemainderToGraveyard.addAll(this.pendingExileFreeCastRemainderToGraveyard);

        // --- Turn-scoped counters ---
        // Read by ConditionEvaluationService / AmountEvaluationService / TurnProgressionService and
        // only reset at cleanup, so a copy taken mid-turn must carry them.
        copy.lifeLostThisTurn.putAll(this.lifeLostThisTurn);
        copy.lifeLostLastTurn.putAll(this.lifeLostLastTurn);
        copy.skipNextCombatPhaseCount.putAll(this.skipNextCombatPhaseCount);
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
        copy.activeTriggeredAbilityCopies = this.activeTriggeredAbilityCopies;
        this.permanentTypesCastFromGraveyardThisTurn.forEach((k, v) ->
                copy.permanentTypesCastFromGraveyardThisTurn.put(k, new HashSet<>(v)));
        copy.oncePerTurnGraveyardCastPermissionsUsedThisTurn.addAll(this.oncePerTurnGraveyardCastPermissionsUsedThisTurn);

        // --- Spell-cast payment tracking (X / converge / colors spent) ---
        copy.spellCastManaSpent.putAll(this.spellCastManaSpent);
        copy.spellCastConvergeValue.putAll(this.spellCastConvergeValue);
        this.spellCastColorsSpent.forEach((k, v) ->
                copy.spellCastColorsSpent.put(k, java.util.EnumSet.copyOf(v)));
        this.spellCastManaSpentByColor.forEach((k, v) ->
                copy.spellCastManaSpentByColor.put(k, new java.util.EnumMap<>(v)));
        copy.spellCastSnowManaSpent.putAll(this.spellCastSnowManaSpent);
        this.spellCastSnowManaSpentByColor.forEach((k, v) ->
                copy.spellCastSnowManaSpentByColor.put(k, new java.util.EnumMap<>(v)));
        this.spellCastManaSpentOnX.forEach((k, v) ->
                copy.spellCastManaSpentOnX.put(k, new java.util.EnumMap<>(v)));
        copy.spellCastSplicedNames.putAll(this.spellCastSplicedNames);

        // --- Until-end-of-turn casting permissions ---
        copy.cardsGrantedFlashbackUntilEndOfTurn.addAll(this.cardsGrantedFlashbackUntilEndOfTurn);
        copy.cardsGrantedEmbalmUntilEndOfTurn.addAll(this.cardsGrantedEmbalmUntilEndOfTurn);
        copy.playersWithFlashUntilEndOfTurn.addAll(this.playersWithFlashUntilEndOfTurn);
        this.cardTypeFlashGrantsThisTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsThisTurn.put(k, ConcurrentHashMap.newKeySet()));
        this.cardTypeFlashGrantsThisTurn.forEach((k, v) ->
                copy.cardTypeFlashGrantsThisTurn.get(k).addAll(v));
        this.nextSpellFlashGrantsThisTurn.forEach((k, v) ->
                copy.nextSpellFlashGrantsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.nextCreatureSpellEmpowermentsThisTurn.forEach((k, v) ->
                copy.nextCreatureSpellEmpowermentsThisTurn.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        copy.spellAdditionalEnterCounters.putAll(this.spellAdditionalEnterCounters);
        copy.spellsGrantedHasteOnEntry.addAll(this.spellsGrantedHasteOnEntry);
        copy.mayTapLandsForSpellsUntilEndOfTurn.addAll(this.mayTapLandsForSpellsUntilEndOfTurn);
        copy.mayPayLifeForColorlessManaUntilEndOfTurn.addAll(this.mayPayLifeForColorlessManaUntilEndOfTurn);
        copy.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn
                .addAll(this.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn);
        copy.graveyardCardCastPermissionsUntilEndOfTurn.putAll(this.graveyardCardCastPermissionsUntilEndOfTurn);

        // --- Damage prevention / redirection still pending ---
        copy.permanentsPreventedFromDealingDamage.addAll(this.permanentsPreventedFromDealingDamage);
        copy.targetSpellDamagePreventionShields.addAll(this.targetSpellDamagePreventionShields);
        copy.pendingRedirectDamage.addAll(this.pendingRedirectDamage);
        copy.pendingSourceRedirectDamage.addAll(this.pendingSourceRedirectDamage);
        copy.permanentsToTapWhenControlLost.addAll(this.permanentsToTapWhenControlLost);

        // --- Pending discard / search follow-ups (immutable, so the reference is safe to share) ---
        copy.pendingReturnToHandOnDiscardType = this.pendingReturnToHandOnDiscardType;
        copy.pendingTransformOnCreatureDiscard = this.pendingTransformOnCreatureDiscard;
        copy.pendingBoostSourceByDiscardedManaValue = this.pendingBoostSourceByDiscardedManaValue;
        copy.pendingUntapOnDiscardType = this.pendingUntapOnDiscardType;
        copy.pendingSearchContext = this.pendingSearchContext;

        // --- Game-creation config ---
        copy.allRandom = this.allRandom;
        copy.randomSetCode = this.randomSetCode;

        // --- Game log (share reference for simulation — not read during MCTS) ---
        copy.gameLog.addAll(this.gameLog);

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
}
