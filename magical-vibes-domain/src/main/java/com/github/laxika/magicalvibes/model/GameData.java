package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.CardColor;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public class GameData {

    public final UUID id;
    public final String gameName;
    public final UUID createdByUserId;
    public final String createdByUsername;
    public final LocalDateTime createdAt;
    public volatile GameStatus status;
    public final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
    public final List<UUID> orderedPlayerIds = Collections.synchronizedList(new ArrayList<>());
    public final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, String> playerIdToName = new ConcurrentHashMap<>();
    public final Map<UUID, String> playerDeckChoices = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerDecks = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerHands = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> mulliganCounts = new ConcurrentHashMap<>();
    public final Set<UUID> playerKeptHand = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> playerNeedsToBottom = new ConcurrentHashMap<>();
    public final List<String> gameLog = Collections.synchronizedList(new ArrayList<>());
    public UUID startingPlayerId;
    public TurnStep currentStep;
    public UUID activePlayerId;
    public int turnNumber;
    public final Set<UUID> priorityPassedBy = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Integer> landsPlayedThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** All spells cast by each player this turn. Access via {@link #recordSpellCast}, {@link #getSpellsCastThisTurnCount}, etc. */
    private final Map<UUID, List<Card>> spellsCastThisTurn = new ConcurrentHashMap<>();
    /** Tracks which permanent types each player has cast from graveyard this turn via Muldrotha-style effects. */
    public final Map<UUID, Set<CardType>> permanentTypesCastFromGraveyardThisTurn = new ConcurrentHashMap<>();
    /** Snapshot of per-player spell counts from the previous turn. Used by werewolf transform triggers. */
    public final Map<UUID, Integer> spellsCastLastTurn = new ConcurrentHashMap<>();
    /** Tracks which players declared at least one attacker this turn (for Angelic Arbiter etc.). */
    public final Set<UUID> playersDeclaredAttackersThisTurn = ConcurrentHashMap.newKeySet();
    public final Map<UUID, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
    public final Map<UUID, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<UUID, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerPoisonCounters = new ConcurrentHashMap<>();
    public final InteractionState interaction = new InteractionState();
    public final List<StackEntry> stack = Collections.synchronizedList(new ArrayList<>());
    /** CR 603.3 — triggers from mana-ability sacrifices wait here until the next time a player
     *  would receive priority, so they don't block sorcery-speed spell casting. */
    public final List<StackEntry> pendingManaAbilityTriggers = Collections.synchronizedList(new ArrayList<>());
    /** CR 603.2 / 603.3 — depth counter for nested mana-ability resolution. While > 0,
     *  triggered abilities that fire from effects resolving inside a mana ability (e.g. a life-gain
     *  effect triggering Sanguine Bond) route to {@link #pendingManaAbilityTriggers} instead of the
     *  main stack. Incremented/decremented in a try/finally pair around mana-ability resolution. */
    public int manaAbilityResolutionDepth;
    public final Map<UUID, List<Card>> playerGraveyards = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Tracks all non-token card IDs put into each player's graveyard from any zone this turn (e.g. Garna, the Bloodflame). */
    public final Map<UUID, Set<UUID>> cardsPutIntoGraveyardFromAnywhereThisTurn = new ConcurrentHashMap<>();
    /** Counts all creature deaths (including tokens) from battlefield this turn, per controller. */
    public final Map<UUID, Integer> creatureDeathCountThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnBySourcePermanent = new ConcurrentHashMap<>();
    /** Delayed trigger: creature card ID → poison counters to give its controller when it dies this turn. */
    public final Map<UUID, Integer> creatureGivingControllerPoisonOnDeathThisTurn = new ConcurrentHashMap<>();
    /** Unified exile zone: every exiled card with its owner and optional source permanent. */
    public final List<ExiledCardEntry> exiledCards = Collections.synchronizedList(new ArrayList<>());
    /** Maps exiled card UUID → egg counter count (for Darigaaz Reincarnated-style effects). */
    public final Map<UUID, Integer> exiledCardEggCounters = new ConcurrentHashMap<>();
    /** Tracks exiled card UUIDs that have silver counters (Karn, Scion of Urza). */
    public final Set<UUID> exiledCardsWithSilverCounters = ConcurrentHashMap.newKeySet();
    /** Tracks the controller ID during a pending Karn Scion +1 opponent reveal choice. */
    public UUID pendingKarnScionControllerId;
    /** Tracks whether a LIBRARY_REVEAL_CHOICE is for Karn Scion -1 (return from exile). */
    public boolean pendingKarnScionReturnFromExile;
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    public int globalDamagePreventionShield;
    public boolean preventAllCombatDamage;
    /** When true, all damage to all creatures (both players') is prevented this turn (Blinding Fog). */
    public boolean preventAllDamageToAllCreatures;
    /** When non-null, creatures NOT matching this predicate are prevented from dealing combat damage this turn. */
    public PermanentPredicate combatDamageExemptPredicate;
    public boolean allPermanentsEnterTappedThisTurn;
    /** Per-controller, per-color additive damage bonus this turn (e.g. The Flame of Keld Chapter III). */
    public final Map<UUID, Map<CardColor, Integer>> colorSourceDamageBonusThisTurn = new ConcurrentHashMap<>();
    public final Set<CardColor> preventDamageFromColors = ConcurrentHashMap.newKeySet();
    public UUID combatDamageRedirectTarget;
    public final Map<UUID, Map<CardColor, Integer>> playerColorDamagePreventionCount = new ConcurrentHashMap<>();
    public final List<PendingMayAbility> pendingMayAbilities = new ArrayList<>();
    public final GraveyardTargetOperationState graveyardTargetOperation = new GraveyardTargetOperationState();
    public final CloneOperationState cloneOperation = new CloneOperationState();
    public UUID imprintSourcePermanentId;
    public List<Card> pendingKarnRestartCards;
    public UUID karnRestartControllerId;
    public PendingOpponentExileChoice pendingOpponentExileChoice;
    public PendingSphinxAmbassadorChoice pendingSphinxAmbassadorChoice;
    public UUID pendingCombatDamageBounceTargetPlayerId;
    public UUID pendingSacrificeSelfToDestroySourceId;
    public boolean pendingExileDamagedPlayerControlsPermanent;
    public int pendingProliferateCount;
    /** Creatures that took lethal damage during effect resolution — destroyed after all effects resolve. */
    public final List<Permanent> pendingLethalDamageDestructions = new ArrayList<>();
    public StackEntry pendingEffectResolutionEntry;
    public int pendingEffectResolutionIndex;
    /** CR 603.5 — set when a MayEffect is encountered during stack resolution, cleared after player responds. */
    public boolean resolvingMayEffectFromStack;
    /** CR 603.5 — stores the player's response to a resolution-time MayEffect (true=accepted, false=declined, null=no pending choice). */
    public Boolean resolvedMayAccepted;
    /** CR 603.5 — stores the StackEntry for resolution-time target selection so the target can be set on it. */
    public StackEntry resolvedMayTargetingEntry;
    public Integer chosenXValue;
    public final Set<UUID> permanentsToSacrificeAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public final Set<UUID> pendingTokenExilesAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public final Set<UUID> creaturesWithEquipmentToDestroyAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public final Set<UUID> pendingExileAndReturnTransformedAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public PendingAbilityActivation pendingAbilityActivation;
    public final Map<UUID, UUID> drawReplacementTargetToController = new ConcurrentHashMap<>();
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    public final Set<UUID> untilEndOfTurnStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> enchantmentDependentStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> permanentControlStolenCreatures = ConcurrentHashMap.newKeySet();
    /** Maps stolen creature ID → source permanent ID for "gain control for as long as you control [source]" effects.
     *  When the source permanent leaves the battlefield or changes controllers, the stolen creature is returned. */
    public final Map<UUID, UUID> sourceDependentStolenCreatures = new ConcurrentHashMap<>();
    public boolean endTurnRequested;
    public final Deque<PermanentChoiceContext.DeathTriggerTarget> pendingDeathTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.DiscardTriggerAnyTarget> pendingDiscardSelfTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.AttackTriggerTarget> pendingAttackTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.SpellTargetTriggerAnyTarget> pendingSpellTargetTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.ETBSpellTargetTrigger> pendingETBSpellTargetTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.EmblemTriggerTarget> pendingEmblemTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.UpkeepPlayerTargetTrigger> pendingUpkeepPlayerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger> pendingUpkeepMultiPlayerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.UpkeepCopyTriggerTarget> pendingUpkeepCopyTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.CapriciousEfreetOwnTarget> pendingCapriciousEfreetTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.EndStepTriggerTarget> pendingEndStepTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.LifeGainTriggerAnyTarget> pendingLifeGainTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.SagaChapterTarget> pendingSagaChapterTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.SagaChapterGraveyardTarget> pendingSagaChapterGraveyardTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.SpellGraveyardTargetTrigger> pendingSpellGraveyardTargetTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.ExploreTriggerTarget> pendingExploreTriggerTargets = new ArrayDeque<>();
    public PendingCapriciousEfreetState pendingCapriciousEfreetState;
    public boolean discardCausedByOpponent;
    public PendingReturnToHandOnDiscardType pendingReturnToHandOnDiscardType;
    public PendingTransformOnCreatureDiscard pendingTransformOnCreatureDiscard;
    /** Number of cards to draw after a "discard up to N, then draw that many" completes. */
    public int pendingRummageDrawCount;
    /** Permanent ID to untap after a "discard a card, then untap [source]" completes. */
    public UUID pendingUntapAfterDiscardPermanentId;
    /** Queue of player IDs still needing to discard for an "each player discards" effect (APNAP order). */
    public final Deque<UUID> pendingEachPlayerDiscardQueue = new ArrayDeque<>();
    public UUID pendingEachPlayerDiscardControllerId;
    public int pendingEachPlayerDiscardAmount;
    public final Deque<UUID> extraTurns = new ArrayDeque<>();
    public int additionalCombatMainPhasePairs;
    public int lastBroadcastedLogSize = 0;
    public UUID draftId;
    public final Deque<LibraryBottomReorderRequest> pendingLibraryBottomReorders = new ArrayDeque<>();
    /** Queue of player IDs still needing to search for a basic land for an "each player searches" effect (APNAP order). */
    public final Deque<UUID> pendingEachPlayerBasicLandSearchQueue = new ArrayDeque<>();
    /** When true, lands found via pendingEachPlayerBasicLandSearchQueue enter the battlefield tapped. */
    public boolean pendingEachPlayerBasicLandSearchTapped;
    public final WarpWorldOperationState warpWorldOperation = new WarpWorldOperationState();
    public boolean cleanupDiscardPending;
    public final List<PendingExileReturn> pendingExileReturns = Collections.synchronizedList(new ArrayList<>());
    /** Tracks exile-until-source-leaves connections (O-ring style).
     *  Maps source permanent UUID to the exiled card + owner info.
     *  When the source permanent leaves the battlefield, the exiled card returns. */
    public final Map<UUID, PendingExileReturn> exileReturnOnPermanentLeave = new ConcurrentHashMap<>();
    public final Set<UUID> pendingTokenExilesAtEndStep = ConcurrentHashMap.newKeySet();
    /** Permanent IDs scheduled for destruction at the beginning of the next end step (e.g. Stone Giant). */
    public final Set<UUID> pendingDestroyAtEndStep = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionIds = new ConcurrentHashMap<>();
    public final Set<UUID> permanentsPreventedFromDealingDamage = ConcurrentHashMap.newKeySet();
    /** Players whose damage (to themselves and their creatures) is fully prevented this turn (Safe Passage). */
    public final Set<UUID> playersWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Damage redirect shields (e.g. Vengeful Archon): prevention shields that redirect prevented damage to a target player. */
    public final List<DamageRedirectShield> damageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Pending redirect damage to deal after damage prevention (populated by DamagePreventionService, consumed by callers). */
    public final List<DamageRedirectShield> pendingRedirectDamage = Collections.synchronizedList(new ArrayList<>());
    /** Source-specific damage redirect shields (e.g. Harm's Way): prevent damage from a chosen source and redirect to any target. */
    public final List<SourceDamageRedirectShield> sourceDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Target+source-specific damage prevention shields (e.g. Healing Grace): prevent next N damage from a chosen source to a specific target. */
    public final List<TargetSourceDamagePreventionShield> targetSourceDamagePreventionShields = Collections.synchronizedList(new ArrayList<>());
    /** Pending source redirect damage to deal after source-specific prevention (populated by DamagePreventionService, consumed by callers). */
    public final List<SourceDamageRedirectShield> pendingSourceRedirectDamage = Collections.synchronizedList(new ArrayList<>());
    public boolean pendingSacrificeAttackingCreature;
    public int pendingForcedSacrificeCount;
    public UUID pendingForcedSacrificePlayerId;
    public final List<PendingForcedSacrifice> pendingForcedSacrificeQueue = Collections.synchronizedList(new ArrayList<>());
    /** Permanent IDs to sacrifice simultaneously once all players have made forced sacrifice choices. */
    public final List<UUID> pendingSimultaneousSacrificeIds = Collections.synchronizedList(new ArrayList<>());
    /** When true, the forced sacrifice queue is being used for "choose creature to keep" (destroy rest) instead of "choose to sacrifice". */
    public boolean pendingDestroyRestMode;
    /** Creature IDs chosen to be kept (protected from destruction) during a destroy-rest flow. */
    public final List<UUID> pendingDestroyRestProtectedIds = Collections.synchronizedList(new ArrayList<>());
    /** Queue for "each player returns up to N cards from graveyard to battlefield" choices. */
    public final List<PendingGraveyardReturnChoice> pendingGraveyardReturnQueue = Collections.synchronizedList(new ArrayList<>());
    /** Name of the card that initiated the destroy-rest flow (for logging). */
    public String pendingDestroyRestSourceName;
    public boolean pendingAwakeningCounterPlacement;
    public boolean pendingAimCounterPlacement;
    public boolean pendingOwnPermanentCounterPlacement;
    public CounterType pendingOwnPermanentCounterType;
    public int pendingOwnPermanentCounterCount;
    public UUID pendingTapSubtypeBoostSourcePermanentId;
    /** Pile separation state: shared by permanent-pile effects (Liliana of the Veil) and card-pile effects (Boneyard Parley).
     *  When {@code pendingPileSeparationCards} is non-empty, the pile IDs refer to card UUIDs (card-pile mode);
     *  otherwise they refer to permanent UUIDs (permanent-pile mode). */
    public boolean pendingPileSeparation;
    public UUID pendingPileSeparationControllerId;
    public UUID pendingPileSeparationTargetPlayerId;
    public final List<UUID> pendingPileSeparationAllPermanentIds = Collections.synchronizedList(new ArrayList<>());
    public final List<UUID> pendingPileSeparationPile1Ids = Collections.synchronizedList(new ArrayList<>());
    public final List<UUID> pendingPileSeparationPile2Ids = Collections.synchronizedList(new ArrayList<>());
    /** Card-pile mode only: the actual Card objects held during separation (not in any zone). */
    public final List<Card> pendingPileSeparationCards = Collections.synchronizedList(new ArrayList<>());
    /** Card-pile mode only: maps card UUID → original owner UUID for returning to owners' graveyards. */
    public final Map<UUID, UUID> pendingPileSeparationCardOwners = new ConcurrentHashMap<>();
    public final List<Emblem> emblems = Collections.synchronizedList(new ArrayList<>());
    /** Delayed triggers that untap up to N permanents matching a filter at the beginning of the next end step. */
    public final List<DelayedUntapPermanents> pendingDelayedUntapPermanents = Collections.synchronizedList(new ArrayList<>());

    public record DelayedUntapPermanents(UUID controllerId, int count, PermanentPredicate filter, Card sourceCard) {}

    /** Delayed trigger: card UUID → owner UUID, return from graveyard to owner's hand at the beginning
     *  of the next end step. Used by Tiana, Ship's Caretaker. */
    public final List<DelayedGraveyardToHandReturn> pendingDelayedGraveyardToHandReturns = Collections.synchronizedList(new ArrayList<>());

    public record DelayedGraveyardToHandReturn(UUID cardId, UUID ownerId) {}
    /** Players who have been granted "no maximum hand size" for the rest of the game. */
    public final Set<UUID> playersWithNoMaximumHandSize = ConcurrentHashMap.newKeySet();

    /** Tracks source-linked animations (Awakener Druid-style).
     *  Maps animated target permanent UUID → source permanent UUID.
     *  When the source leaves the battlefield, the target's animation is cleared. */
    public final Map<UUID, UUID> sourceLinkedAnimations = new ConcurrentHashMap<>();

    /** Per-player: spells controlled by this player can't be countered by spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerSpellsCantBeCounteredByColorsThisTurn = new ConcurrentHashMap<>();

    /** Per-player: creatures controlled by this player can't be the targets of spells of these colors this turn. Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerCreaturesCantBeTargetedByColorsThisTurn = new ConcurrentHashMap<>();

    /** Players who can't cast spells this turn (e.g. Silence). Cleared at end of turn and on new turn. */
    public final Set<UUID> playersSilencedThisTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs that have been granted flashback until end of turn (e.g. Past in Flames).
     *  The flashback cost for these cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedFlashbackUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Players whose instant/sorcery spells are automatically copied until end of turn
     *  (e.g. The Mirari Conjecture chapter III). Cleared at end of turn. */
    public final Set<UUID> playersWithSpellCopyUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Pending one-shot spell copy triggers from mana abilities (e.g. Primal Wellspring).
     *  Each value tracks how many copies are pending for that player.
     *  Decremented when an instant/sorcery is cast; cleared when mana pools drain. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyCount = new ConcurrentHashMap<>();

    /** Delayed triggers from Chancellor-style opening hand reveals.
     *  Fires once per opponent when they cast their first spell of the game. */
    public final List<OpeningHandRevealTrigger> openingHandRevealTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Delayed mana triggers from Chancellor-style opening hand reveals.
     *  Fires at the beginning of the revealing player's first precombat main phase. */
    public final List<OpeningHandRevealTrigger> openingHandManaTriggers = Collections.synchronizedList(new ArrayList<>());

    /** Tracks which players have cast their first spell of the game (for opening hand triggers). */
    public final Set<UUID> playersWhoCastFirstSpellInGame = ConcurrentHashMap.newKeySet();

    /** Maps exiled card UUID → player UUID who has permission to play it (e.g. Praetor's Grasp). */
    public final Map<UUID, UUID> exilePlayPermissions = new ConcurrentHashMap<>();
    /** Card UUIDs whose exile-play permission expires at end of turn (impulse draw, e.g. Vance's Blasting Cannons).
     *  Cleared during cleanup step — matching entries are also removed from {@link #exilePlayPermissions}. */
    public final Set<UUID> exilePlayPermissionsExpireEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Transient field: tracks which Knowledge Pool permanent is currently resolving a cast choice. */
    public UUID knowledgePoolSourcePermanentId;

    /** Tracks how many cards each player has drawn this turn. */
    public final Map<UUID, Integer> cardsDrawnThisTurn = new ConcurrentHashMap<>();

    /** Delayed trigger: permanent ID → total +1/+1 counters to put on it at the beginning of the next end step.
     *  Used by Protean Hydra's regrowth ability: "Whenever a +1/+1 counter is removed from this creature,
     *  put two +1/+1 counters on it at the beginning of the next end step." */
    public final Map<UUID, Integer> pendingDelayedPlusOnePlusOneCounters = new ConcurrentHashMap<>();

    /** Delayed triggers: "Whenever one or more creatures you control deal combat damage to a player this turn,
     *  draw N, then discard N." Registered by Jace, Cunning Castaway's +1. Cleared at start of new turn. */
    public final List<DelayedCombatDamageLoot> pendingDelayedCombatDamageLoots = Collections.synchronizedList(new ArrayList<>());

    public record DelayedCombatDamageLoot(UUID controllerId, int drawAmount, int discardAmount, Card sourceCard) {}

    /** Tracks which permanents dealt combat damage to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. */
    public final Map<UUID, Set<UUID>> combatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks which players have been dealt damage this turn (from any source — combat, spells, abilities). */
    public final Set<UUID> playersDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which permanents (by UUID) have been dealt damage this turn (from any source — combat, spells, abilities).
     *  Survives regeneration (which removes marked damage but does not undo "was dealt damage").
     *  Cleared at start of new turn. */
    public final Set<UUID> permanentsDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks subtypes of creatures that dealt combat damage to players this turn.
     *  Maps source permanent UUID → set of subtypes the creature had at the time of dealing damage.
     *  Used by end-step triggers that check which subtypes dealt combat damage (e.g. Admiral Beckett Brass). */
    public final Map<UUID, Set<CardSubtype>> combatDamageSourceSubtypesThisTurn = new ConcurrentHashMap<>();

    /** Tracks which creatures that dealt combat damage to players this turn had the Changeling keyword.
     *  These creatures count as having all creature subtypes for subtype-conditional triggers. */
    public final Set<UUID> combatDamageSourcesWithChangelingThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks which Leonin Arbiter permanent IDs each player has paid {2} for this turn. */
    public final Map<UUID, Set<UUID>> paidSearchTaxPermanentIds = new ConcurrentHashMap<>();

    // Mindslaver — turn control
    /** Delayed effect: targetPlayerId -> controllerId, consumed when target player's turn begins. */
    public final Map<UUID, UUID> pendingTurnControl = new ConcurrentHashMap<>();
    /** Non-null when a player is being controlled this turn (the controlled player's ID). */
    public UUID mindControlledPlayerId;
    /** Non-null when a player is being controlled this turn (the controlling player's ID). */
    public UUID mindControllerPlayerId;

    /** Stores context for a pending Leonin Arbiter search tax MayAbility choice. */
    public PendingSearchContext pendingSearchContext;

    /** When true, a follow-up library search for a basic land to hand is pending (e.g. Cultivate second pick). */
    public boolean pendingBasicLandToHandSearch;

    /** When true, a follow-up unrestricted library search for a card to graveyard is pending (e.g. Final Parting second pick). */
    public boolean pendingCardToGraveyardSearch;

    /** Damage assignments provided at cast time for an ETB divided-damage effect (e.g. Kuldotha Flamefiend). */
    public Map<UUID, Integer> pendingETBDamageAssignments = Map.of();


    // Combat damage assignment state
    public final Map<Integer, Map<UUID, Integer>> combatDamagePlayerAssignments = new HashMap<>();
    public final List<Integer> combatDamagePendingIndices = new ArrayList<>();
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
        } else {
            stack.add(entry);
        }
    }

    /**
     * Records a spell cast by the given player this turn.
     */
    public void recordSpellCast(UUID playerId, Card card) {
        spellsCastThisTurn.computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>())).add(card);
    }

    /**
     * Returns the number of spells the given player has cast this turn.
     */
    public int getSpellsCastThisTurnCount(UUID playerId) {
        return spellsCastThisTurn.getOrDefault(playerId, List.of()).size();
    }

    /**
     * Returns an unmodifiable view of the spells the given player has cast this turn.
     */
    public List<Card> getSpellsCastThisTurn(UUID playerId) {
        return Collections.unmodifiableList(spellsCastThisTurn.getOrDefault(playerId, List.of()));
    }

    /**
     * Returns true if no spells have been cast by any player this turn.
     */
    public boolean isSpellsCastThisTurnEmpty() {
        return spellsCastThisTurn.isEmpty();
    }

    /**
     * Snapshots per-player spell counts into the given target map, then clears spell tracking for the new turn.
     */
    public void snapshotSpellCountsAndClear(Map<UUID, Integer> target) {
        target.clear();
        spellsCastThisTurn.forEach((id, spells) -> target.put(id, spells.size()));
        spellsCastThisTurn.clear();
    }

    public static final int STARTING_LIFE_TOTAL = 20;

    /**
     * Returns the current life total for the given player, defaulting to 20 if not yet set.
     */
    public int getLife(UUID playerId) {
        return playerLifeTotals.getOrDefault(playerId, STARTING_LIFE_TOTAL);
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
        exiledCards.add(new ExiledCardEntry(card, ownerId, null));
    }

    /** Adds a card to exile with source permanent tracking. */
    public void addToExile(UUID ownerId, Card card, UUID sourcePermanentId) {
        exiledCards.add(new ExiledCardEntry(card, ownerId, sourcePermanentId));
    }

    /** Removes an exiled card by card ID. Returns true if found and removed. */
    public boolean removeFromExile(UUID cardId) {
        return exiledCards.removeIf(e -> e.card().getId().equals(cardId));
    }

    /** Finds an exiled card entry by card ID, or null if not found. */
    public ExiledCardEntry findExiledCard(UUID cardId) {
        return exiledCards.stream()
                .filter(e -> e.card().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /** Removes all exile entries tracked with the given source permanent. */
    public void clearExiledByPermanent(UUID sourcePermanentId) {
        exiledCards.removeIf(e -> sourcePermanentId.equals(e.sourcePermanentId()));
    }

    /** Removes source tracking from exile entries (sets sourcePermanentId to null). Used by Karn restart. */
    public void clearAllSourceTracking() {
        List<ExiledCardEntry> updated = new ArrayList<>();
        var it = exiledCards.iterator();
        while (it.hasNext()) {
            ExiledCardEntry e = it.next();
            if (e.sourcePermanentId() != null) {
                it.remove();
                updated.add(new ExiledCardEntry(e.card(), e.ownerId(), null));
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

    /**
     * CR 603.5 — Puts a "you may" triggered ability with source permanent and target context
     * directly onto the stack.  The may choice happens at resolution time, not trigger time.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayEffect may, UUID targetCardId, UUID sourcePermanentId) {
        stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                targetCardId,
                sourcePermanentId
        ));
    }

    /**
     * CR 603.5 — Puts a "you may pay" triggered ability directly onto the stack with the
     * MayPayManaEffect wrapper intact.  The may choice happens at resolution time.
     * The targetCardId (e.g. the entering permanent for Mirrorworks) is preserved on the
     * stack entry so that the wrapped effect can reference it at resolution time.
     */
    public void queueMayAbility(Card sourceCard, UUID controllerId, MayPayManaEffect mayPay, UUID targetCardId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(mayPay))
        );
        if (targetCardId != null) {
            entry.setTargetId(targetCardId);
        }
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
        copy.globalDamagePreventionShield = this.globalDamagePreventionShield;
        copy.preventAllCombatDamage = this.preventAllCombatDamage;
        copy.preventAllDamageToAllCreatures = this.preventAllDamageToAllCreatures;
        copy.combatDamageExemptPredicate = this.combatDamageExemptPredicate;
        copy.allPermanentsEnterTappedThisTurn = this.allPermanentsEnterTappedThisTurn;
        this.colorSourceDamageBonusThisTurn.forEach((pid, colorMap) ->
                copy.colorSourceDamageBonusThisTurn.put(pid, new HashMap<>(colorMap)));
        copy.combatDamageRedirectTarget = this.combatDamageRedirectTarget;
        copy.pendingCombatDamageBounceTargetPlayerId = this.pendingCombatDamageBounceTargetPlayerId;
        copy.pendingSacrificeSelfToDestroySourceId = this.pendingSacrificeSelfToDestroySourceId;
        copy.pendingExileDamagedPlayerControlsPermanent = this.pendingExileDamagedPlayerControlsPermanent;
        copy.pendingProliferateCount = this.pendingProliferateCount;
        copy.pendingEffectResolutionEntry = this.pendingEffectResolutionEntry != null
                ? new StackEntry(this.pendingEffectResolutionEntry) : null;
        copy.pendingEffectResolutionIndex = this.pendingEffectResolutionIndex;
        copy.resolvingMayEffectFromStack = this.resolvingMayEffectFromStack;
        copy.resolvedMayAccepted = this.resolvedMayAccepted;
        copy.resolvedMayTargetingEntry = this.resolvedMayTargetingEntry != null
                ? new StackEntry(this.resolvedMayTargetingEntry) : null;
        copy.chosenXValue = this.chosenXValue;
        copy.pendingAbilityActivation = this.pendingAbilityActivation; // immutable record
        copy.endTurnRequested = this.endTurnRequested;
        copy.discardCausedByOpponent = this.discardCausedByOpponent;
        copy.additionalCombatMainPhasePairs = this.additionalCombatMainPhasePairs;
        copy.lastBroadcastedLogSize = this.lastBroadcastedLogSize;
        copy.draftId = this.draftId;
        copy.cleanupDiscardPending = this.cleanupDiscardPending;
        copy.simulation = true;
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.pendingSacrificeAttackingCreature = this.pendingSacrificeAttackingCreature;
        copy.pendingForcedSacrificeCount = this.pendingForcedSacrificeCount;
        copy.pendingForcedSacrificePlayerId = this.pendingForcedSacrificePlayerId;
        copy.pendingForcedSacrificeQueue.addAll(this.pendingForcedSacrificeQueue);
        copy.pendingSimultaneousSacrificeIds.addAll(this.pendingSimultaneousSacrificeIds);
        copy.pendingDestroyRestMode = this.pendingDestroyRestMode;
        copy.pendingDestroyRestProtectedIds.addAll(this.pendingDestroyRestProtectedIds);
        copy.pendingDestroyRestSourceName = this.pendingDestroyRestSourceName;
        copy.pendingGraveyardReturnQueue.addAll(this.pendingGraveyardReturnQueue);
        copy.pendingAwakeningCounterPlacement = this.pendingAwakeningCounterPlacement;
        copy.pendingAimCounterPlacement = this.pendingAimCounterPlacement;
        copy.pendingOwnPermanentCounterPlacement = this.pendingOwnPermanentCounterPlacement;
        copy.pendingOwnPermanentCounterType = this.pendingOwnPermanentCounterType;
        copy.pendingOwnPermanentCounterCount = this.pendingOwnPermanentCounterCount;
        copy.pendingTapSubtypeBoostSourcePermanentId = this.pendingTapSubtypeBoostSourcePermanentId;
        copy.pendingPileSeparation = this.pendingPileSeparation;
        copy.pendingPileSeparationControllerId = this.pendingPileSeparationControllerId;
        copy.pendingPileSeparationTargetPlayerId = this.pendingPileSeparationTargetPlayerId;
        copy.pendingPileSeparationAllPermanentIds.addAll(this.pendingPileSeparationAllPermanentIds);
        copy.pendingPileSeparationPile1Ids.addAll(this.pendingPileSeparationPile1Ids);
        copy.pendingPileSeparationPile2Ids.addAll(this.pendingPileSeparationPile2Ids);
        copy.pendingPileSeparationCards.addAll(this.pendingPileSeparationCards);
        copy.pendingPileSeparationCardOwners.putAll(this.pendingPileSeparationCardOwners);

        // --- Set<UUID> (ConcurrentHashMap.newKeySet()) ---
        copy.playerIds.addAll(this.playerIds);
        copy.playerKeptHand.addAll(this.playerKeptHand);
        copy.priorityPassedBy.addAll(this.priorityPassedBy);
        copy.preventDamageFromColors.addAll(this.preventDamageFromColors);
        copy.permanentsToSacrificeAtEndOfCombat.addAll(this.permanentsToSacrificeAtEndOfCombat);
        copy.pendingTokenExilesAtEndOfCombat.addAll(this.pendingTokenExilesAtEndOfCombat);
        copy.creaturesWithEquipmentToDestroyAtEndOfCombat.addAll(this.creaturesWithEquipmentToDestroyAtEndOfCombat);
        copy.pendingExileAndReturnTransformedAtEndOfCombat.addAll(this.pendingExileAndReturnTransformedAtEndOfCombat);
        copy.untilEndOfTurnStolenCreatures.addAll(this.untilEndOfTurnStolenCreatures);
        copy.enchantmentDependentStolenCreatures.addAll(this.enchantmentDependentStolenCreatures);
        copy.permanentControlStolenCreatures.addAll(this.permanentControlStolenCreatures);
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);
        copy.playersWithAllDamagePrevented.addAll(this.playersWithAllDamagePrevented);
        copy.damageRedirectShields.addAll(this.damageRedirectShields);
        copy.sourceDamageRedirectShields.addAll(this.sourceDamageRedirectShields);
        copy.targetSourceDamagePreventionShields.addAll(this.targetSourceDamagePreventionShields);
        copy.stateTriggerOnStack.addAll(this.stateTriggerOnStack);

        // --- List<UUID> (synchronized) ---
        copy.orderedPlayerIds.addAll(this.orderedPlayerIds);
        copy.playerNames.addAll(this.playerNames);

        // --- Map<UUID, String/Integer> ---
        copy.playerIdToName.putAll(this.playerIdToName);
        copy.playerDeckChoices.putAll(this.playerDeckChoices);
        copy.mulliganCounts.putAll(this.mulliganCounts);
        copy.playerNeedsToBottom.putAll(this.playerNeedsToBottom);
        copy.landsPlayedThisTurn.putAll(this.landsPlayedThisTurn);
        this.permanentsEnteredBattlefieldThisTurn.forEach((k, v) ->
                copy.permanentsEnteredBattlefieldThisTurn.put(k, new ArrayList<>(v)));
        this.spellsCastThisTurn.forEach((k, v) ->
                copy.spellsCastThisTurn.put(k, new ArrayList<>(v)));
        copy.spellsCastLastTurn.putAll(this.spellsCastLastTurn);
        copy.playersDeclaredAttackersThisTurn.addAll(this.playersDeclaredAttackersThisTurn);
        copy.playerLifeTotals.putAll(this.playerLifeTotals);
        copy.playerPoisonCounters.putAll(this.playerPoisonCounters);
        copy.playerDamagePreventionShields.putAll(this.playerDamagePreventionShields);
        copy.stolenCreatures.putAll(this.stolenCreatures);
        copy.sourceDependentStolenCreatures.putAll(this.sourceDependentStolenCreatures);
        copy.drawReplacementTargetToController.putAll(this.drawReplacementTargetToController);
        copy.cardsDrawnThisTurn.putAll(this.cardsDrawnThisTurn);
        this.combatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        copy.playersDealtDamageThisTurn.addAll(this.playersDealtDamageThisTurn);
        copy.permanentsDealtDamageThisTurn.addAll(this.permanentsDealtDamageThisTurn);
        this.combatDamageSourceSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageSourceSubtypesThisTurn.put(k, new HashSet<>(v)));
        copy.combatDamageSourcesWithChangelingThisTurn.addAll(this.combatDamageSourcesWithChangelingThisTurn);
        copy.pendingDelayedPlusOnePlusOneCounters.putAll(this.pendingDelayedPlusOnePlusOneCounters);
        copy.pendingDelayedCombatDamageLoots.addAll(this.pendingDelayedCombatDamageLoots);

        // --- Map<UUID, Set<TurnStep>> ---
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.put(k, new HashSet<>(v)));

        // --- Map<UUID, List<Card>> (shared Card refs) ---
        this.playerDecks.forEach((k, v) -> copy.playerDecks.put(k, new ArrayList<>(v)));
        this.playerHands.forEach((k, v) -> copy.playerHands.put(k, new ArrayList<>(v)));
        this.playerGraveyards.forEach((k, v) -> copy.playerGraveyards.put(k, new ArrayList<>(v)));
        copy.exiledCards.addAll(this.exiledCards);
        copy.exiledCardEggCounters.putAll(this.exiledCardEggCounters);
        copy.exiledCardsWithSilverCounters.addAll(this.exiledCardsWithSilverCounters);
        copy.pendingKarnScionControllerId = this.pendingKarnScionControllerId;
        copy.pendingKarnScionReturnFromExile = this.pendingKarnScionReturnFromExile;

        // --- Map<UUID, List<Permanent>> (deep copy each Permanent) ---
        this.playerBattlefields.forEach((k, v) ->
                copy.playerBattlefields.put(k,
                        v.stream().map(Permanent::new).collect(Collectors.toCollection(ArrayList::new))));

        // --- Map<UUID, ManaPool> (deep copy each ManaPool) ---
        this.playerManaPools.forEach((k, v) -> copy.playerManaPools.put(k, new ManaPool(v)));

        // --- List<StackEntry> (deep copy each StackEntry) ---
        this.stack.forEach(se -> copy.stack.add(new StackEntry(se)));
        this.pendingManaAbilityTriggers.forEach(se -> copy.pendingManaAbilityTriggers.add(new StackEntry(se)));

        // --- InteractionState ---
        InteractionState copiedInteraction = this.interaction.deepCopy();
        copyInteractionInto(copy, copiedInteraction);

        // --- Map<UUID, Set<UUID>> ---
        this.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) ->
                copy.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, new HashSet<>(v)));
        this.cardsPutIntoGraveyardFromAnywhereThisTurn.forEach((k, v) ->
                copy.cardsPutIntoGraveyardFromAnywhereThisTurn.put(k, new HashSet<>(v)));
        copy.creatureDeathCountThisTurn.putAll(this.creatureDeathCountThisTurn);
        this.creatureCardsDamagedThisTurnBySourcePermanent.forEach((k, v) ->
                copy.creatureCardsDamagedThisTurnBySourcePermanent.put(k, new HashSet<>(v)));
        copy.creatureGivingControllerPoisonOnDeathThisTurn.putAll(this.creatureGivingControllerPoisonOnDeathThisTurn);

        // --- Map<UUID, Map<CardColor, Integer>> ---
        this.playerColorDamagePreventionCount.forEach((k, v) ->
                copy.playerColorDamagePreventionCount.put(k, new HashMap<>(v)));

        // --- PendingMayAbility list (records with shared Card refs) ---
        copy.pendingMayAbilities.addAll(this.pendingMayAbilities);

        // --- PendingExileReturn list (records with shared Card refs) ---
        copy.pendingExileReturns.addAll(this.pendingExileReturns);

        // --- Exile-until-source-leaves map (O-ring style) ---
        copy.exileReturnOnPermanentLeave.putAll(this.exileReturnOnPermanentLeave);

        // --- Pending token exiles at end step (Mimic Vat) ---
        copy.pendingTokenExilesAtEndStep.addAll(this.pendingTokenExilesAtEndStep);

        // --- Pending destroy at end step (Stone Giant) ---
        copy.pendingDestroyAtEndStep.addAll(this.pendingDestroyAtEndStep);

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---
        this.playerSourceDamagePreventionIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionIds.put(k, new HashSet<>(v)));

        // --- GraveyardTargetOperationState ---
        copy.graveyardTargetOperation.card = this.graveyardTargetOperation.card;
        copy.graveyardTargetOperation.controllerId = this.graveyardTargetOperation.controllerId;
        copy.graveyardTargetOperation.effects = this.graveyardTargetOperation.effects;
        copy.graveyardTargetOperation.entryType = this.graveyardTargetOperation.entryType;
        copy.graveyardTargetOperation.xValue = this.graveyardTargetOperation.xValue;

        // --- Imprint ---
        copy.imprintSourcePermanentId = this.imprintSourcePermanentId;

        // --- Karn restart ---
        copy.pendingKarnRestartCards = this.pendingKarnRestartCards != null ? new ArrayList<>(this.pendingKarnRestartCards) : null;
        copy.karnRestartControllerId = this.karnRestartControllerId;

        // --- Post-exile search ---
        copy.pendingOpponentExileChoice = this.pendingOpponentExileChoice; // record — immutable
        copy.pendingSphinxAmbassadorChoice = this.pendingSphinxAmbassadorChoice; // record — immutable

        // --- CloneOperationState ---
        copy.cloneOperation.card = this.cloneOperation.card;
        copy.cloneOperation.controllerId = this.cloneOperation.controllerId;
        copy.cloneOperation.etbTargetId = this.cloneOperation.etbTargetId;
        copy.cloneOperation.powerOverride = this.cloneOperation.powerOverride;
        copy.cloneOperation.toughnessOverride = this.cloneOperation.toughnessOverride;
        copy.cloneOperation.additionalTypesOverride = this.cloneOperation.additionalTypesOverride;
        copy.cloneOperation.additionalActivatedAbilities = this.cloneOperation.additionalActivatedAbilities;

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

        // --- Deques ---
        copy.pendingDeathTriggerTargets.addAll(this.pendingDeathTriggerTargets);
        copy.pendingDiscardSelfTriggers.addAll(this.pendingDiscardSelfTriggers);
        copy.pendingAttackTriggerTargets.addAll(this.pendingAttackTriggerTargets);
        copy.pendingSpellTargetTriggers.addAll(this.pendingSpellTargetTriggers);
        copy.pendingETBSpellTargetTriggers.addAll(this.pendingETBSpellTargetTriggers);
        copy.pendingEmblemTriggerTargets.addAll(this.pendingEmblemTriggerTargets);
        copy.pendingUpkeepPlayerTargets.addAll(this.pendingUpkeepPlayerTargets);
        copy.pendingUpkeepMultiPlayerTargets.addAll(this.pendingUpkeepMultiPlayerTargets);
        copy.pendingUpkeepCopyTargets.addAll(this.pendingUpkeepCopyTargets);
        copy.pendingCapriciousEfreetTargets.addAll(this.pendingCapriciousEfreetTargets);
        copy.pendingEndStepTriggerTargets.addAll(this.pendingEndStepTriggerTargets);
        copy.pendingLifeGainTriggerTargets.addAll(this.pendingLifeGainTriggerTargets);
        copy.pendingSagaChapterTargets.addAll(this.pendingSagaChapterTargets);
        copy.pendingSagaChapterGraveyardTargets.addAll(this.pendingSagaChapterGraveyardTargets);
        copy.pendingSpellGraveyardTargetTriggers.addAll(this.pendingSpellGraveyardTargetTriggers);
        copy.pendingExploreTriggerTargets.addAll(this.pendingExploreTriggerTargets);
        copy.pendingCapriciousEfreetState = this.pendingCapriciousEfreetState;
        copy.extraTurns.addAll(this.extraTurns);
        copy.pendingEachPlayerDiscardQueue.addAll(this.pendingEachPlayerDiscardQueue);
        copy.pendingEachPlayerDiscardControllerId = this.pendingEachPlayerDiscardControllerId;
        copy.pendingEachPlayerDiscardAmount = this.pendingEachPlayerDiscardAmount;
        this.pendingLibraryBottomReorders.forEach(req ->
                copy.pendingLibraryBottomReorders.add(new LibraryBottomReorderRequest(req.playerId(), new ArrayList<>(req.cards()))));
        copy.pendingEachPlayerBasicLandSearchQueue.addAll(this.pendingEachPlayerBasicLandSearchQueue);
        copy.pendingEachPlayerBasicLandSearchTapped = this.pendingEachPlayerBasicLandSearchTapped;

        // --- Combat damage assignment state ---
        this.combatDamagePlayerAssignments.forEach((k, v) ->
                copy.combatDamagePlayerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingIndices.addAll(this.combatDamagePendingIndices);
        copy.combatDamagePhase1State = this.combatDamagePhase1State; // read-only snapshot from phase 1

        // --- Emblems (records are immutable) ---
        copy.emblems.addAll(this.emblems);

        // --- Delayed untap permanents (records are immutable) ---
        copy.pendingDelayedUntapPermanents.addAll(this.pendingDelayedUntapPermanents);

        // --- Delayed graveyard-to-hand returns (records are immutable) ---
        copy.pendingDelayedGraveyardToHandReturns.addAll(this.pendingDelayedGraveyardToHandReturns);

        // --- Permanent no-max-hand-size grants ---
        copy.playersWithNoMaximumHandSize.addAll(this.playersWithNoMaximumHandSize);

        // --- Source-linked animations (Awakener Druid-style) ---
        copy.sourceLinkedAnimations.putAll(this.sourceLinkedAnimations);

        // --- Per-player spell/creature color protection (Autumn's Veil style) ---
        this.playerSpellsCantBeCounteredByColorsThisTurn.forEach((k, v) ->
                copy.playerSpellsCantBeCounteredByColorsThisTurn.put(k, new HashSet<>(v)));
        this.playerCreaturesCantBeTargetedByColorsThisTurn.forEach((k, v) ->
                copy.playerCreaturesCantBeTargetedByColorsThisTurn.put(k, new HashSet<>(v)));

        // --- Silence-style "opponents can't cast" flag ---
        copy.playersSilencedThisTurn.addAll(this.playersSilencedThisTurn);

        // --- Spell copy until end of turn (The Mirari Conjecture chapter III) ---
        copy.playersWithSpellCopyUntilEndOfTurn.addAll(this.playersWithSpellCopyUntilEndOfTurn);

        // --- Pending one-shot spell copy triggers (Primal Wellspring) ---
        copy.pendingNextInstantSorceryCopyCount.putAll(this.pendingNextInstantSorceryCopyCount);

        copy.exilePlayPermissions.putAll(this.exilePlayPermissions);
        copy.exilePlayPermissionsExpireEndOfTurn.addAll(this.exilePlayPermissionsExpireEndOfTurn);
        copy.knowledgePoolSourcePermanentId = this.knowledgePoolSourcePermanentId;

        // --- Search tax payments (Leonin Arbiter) ---
        this.paidSearchTaxPermanentIds.forEach((k, v) ->
                copy.paidSearchTaxPermanentIds.put(k, new HashSet<>(v)));

        // --- ETB / sacrifice damage assignments ---
        copy.pendingETBDamageAssignments = this.pendingETBDamageAssignments.isEmpty()
                ? Map.of() : new HashMap<>(this.pendingETBDamageAssignments);

        // --- Mindslaver turn control ---
        copy.pendingTurnControl.putAll(this.pendingTurnControl);
        copy.mindControlledPlayerId = this.mindControlledPlayerId;
        copy.mindControllerPlayerId = this.mindControllerPlayerId;

        // --- Opening hand reveal triggers (Chancellor cycle) ---
        copy.openingHandRevealTriggers.addAll(this.openingHandRevealTriggers);
        copy.openingHandManaTriggers.addAll(this.openingHandManaTriggers);
        copy.playersWhoCastFirstSpellInGame.addAll(this.playersWhoCastFirstSpellInGame);

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
        // by clearing and re-configuring through public methods.
        // However, since InteractionState uses private fields and public begin*/clear* methods,
        // we use the context object to reconstruct the state.
        if (source.awaitingInputType() == null) {
            return; // default state, nothing to copy
        }

        // Copy context through reflection-free approach: re-read the source's context
        // and call the appropriate begin* method on the target's interaction.
        InteractionState targetInteraction = target.interaction;
        var ctx = source.currentContext();
        if (ctx == null) {
            return;
        }

        switch (ctx) {
            case InteractionContext.AttackerDeclaration ad ->
                    targetInteraction.beginAttackerDeclaration(ad.activePlayerId());
            case InteractionContext.BlockerDeclaration bd ->
                    targetInteraction.beginBlockerDeclaration(bd.defenderId());
            case InteractionContext.CardChoice cc ->
                    targetInteraction.beginCardChoice(cc.type(), cc.playerId(), cc.validIndices(), cc.targetId());
            case InteractionContext.PermanentChoice pc ->
                    targetInteraction.beginPermanentChoice(pc.playerId(), pc.validIds(), pc.context());
            case InteractionContext.GraveyardChoice gc ->
                    targetInteraction.beginGraveyardChoice(gc.playerId(), gc.validIndices(), gc.destination(), gc.cardPool());
            case InteractionContext.ColorChoice cc ->
                    targetInteraction.beginColorChoice(cc.playerId(), cc.permanentId(), cc.etbTargetId(), cc.context());
            case InteractionContext.MayAbilityChoice mc ->
                    targetInteraction.beginMayAbilityChoice(mc.playerId(), mc.description());
            case InteractionContext.MultiPermanentChoice mpc ->
                    targetInteraction.beginMultiPermanentChoice(mpc.playerId(), mpc.validIds(), mpc.maxCount());
            case InteractionContext.MultiGraveyardChoice mgc ->
                    targetInteraction.beginMultiGraveyardChoice(mgc.playerId(), mgc.validCardIds(), mgc.maxCount());
            case InteractionContext.LibraryReorder lr ->
                    targetInteraction.beginLibraryReorder(lr.playerId(), lr.cards() != null ? new ArrayList<>(lr.cards()) : null, lr.toBottom(), lr.deckOwnerId());
            case InteractionContext.LibrarySearch ls ->
                    targetInteraction.beginLibrarySearch(LibrarySearchParams.builder(ls.playerId(),
                                    ls.cards() != null ? new ArrayList<>(ls.cards()) : null)
                            .reveals(ls.reveals())
                            .canFailToFind(ls.canFailToFind())
                            .targetPlayerId(ls.targetPlayerId())
                            .remainingCount(ls.remainingCount())
                            .sourceCards(ls.sourceCards() != null ? new ArrayList<>(ls.sourceCards()) : null)
                            .reorderRemainingToBottom(ls.reorderRemainingToBottom())
                            .reorderRemainingToTop(ls.reorderRemainingToTop())
                            .shuffleAfterSelection(ls.shuffleAfterSelection())
                            .prompt(ls.prompt())
                            .destination(ls.destination())
                            .filterCardTypes(ls.filterCardTypes())
                            .build());
            case InteractionContext.LibraryRevealChoice lrc -> {
                    if (lrc.randomRemainingToBottom()) {
                        targetInteraction.beginLibraryRevealChoiceRandomBottom(lrc.playerId(),
                                lrc.allCards() != null ? new ArrayList<>(lrc.allCards()) : null,
                                lrc.validCardIds() != null ? new HashSet<>(lrc.validCardIds()) : null);
                    } else if (lrc.lifeCostPerSelection() > 0) {
                        targetInteraction.beginLibraryRevealChoice(lrc.playerId(),
                                lrc.allCards() != null ? new ArrayList<>(lrc.allCards()) : null,
                                lrc.validCardIds() != null ? new HashSet<>(lrc.validCardIds()) : null,
                                lrc.remainingToGraveyard(), lrc.selectedToHand(), lrc.reorderRemainingToBottom(),
                                lrc.lifeCostPerSelection(), lrc.beneficiaryPlayerId());
                    } else {
                        targetInteraction.beginLibraryRevealChoice(lrc.playerId(),
                                lrc.allCards() != null ? new ArrayList<>(lrc.allCards()) : null,
                                lrc.validCardIds() != null ? new HashSet<>(lrc.validCardIds()) : null,
                                lrc.remainingToGraveyard(), lrc.selectedToHand(), lrc.reorderRemainingToBottom());
                    }
                }
            case InteractionContext.HandTopBottomChoice htbc ->
                    targetInteraction.beginHandTopBottomChoice(htbc.playerId(),
                            htbc.cards() != null ? new ArrayList<>(htbc.cards()) : null);
            case InteractionContext.RevealedHandChoice rhc ->
                    targetInteraction.beginRevealedHandChoice(rhc.choosingPlayerId(), rhc.targetPlayerId(),
                            rhc.validIndices(), rhc.remainingCount(), rhc.discardMode(), rhc.exileMode(), rhc.chosenCards());
            case InteractionContext.MultiZoneExileChoice mzec ->
                    targetInteraction.beginMultiZoneExileChoice(mzec.playerId(),
                            mzec.validCardIds() != null ? new HashSet<>(mzec.validCardIds()) : null,
                            mzec.maxCount(), mzec.targetPlayerId(), mzec.controllerId(), mzec.cardName());
            case InteractionContext.CombatDamageAssignment cda ->
                    targetInteraction.beginCombatDamageAssignment(cda.playerId(), cda.attackerIndex(),
                            cda.attackerPermanentId(), cda.attackerName(), cda.totalDamage(),
                            cda.validTargets(), cda.isTrample(), cda.isDeathtouch());
            case InteractionContext.XValueChoice xvc ->
                    targetInteraction.beginXValueChoice(xvc.playerId(), xvc.maxValue(), xvc.prompt(), xvc.cardName());
            case InteractionContext.Scry s ->
                    targetInteraction.beginScry(s.playerId(),
                            s.cards() != null ? new ArrayList<>(s.cards()) : null);
            case InteractionContext.KnowledgePoolCastChoice kpc ->
                    targetInteraction.beginKnowledgePoolCastChoice(kpc.playerId(),
                            kpc.validCardIds() != null ? new HashSet<>(kpc.validCardIds()) : null, kpc.maxCount());
            case InteractionContext.MirrorOfFateChoice mfc ->
                    targetInteraction.beginMirrorOfFateChoice(mfc.playerId(),
                            mfc.validCardIds() != null ? new HashSet<>(mfc.validCardIds()) : null, mfc.maxCount());
        }

        // Copy discard remaining count (not part of context reconstruction)
        if (source.revealedHandChoice() != null && source.revealedHandChoice().discardRemainingCount() > 0) {
            targetInteraction.setDiscardRemainingCount(source.revealedHandChoice().discardRemainingCount());
        }
    }
}
