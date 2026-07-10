package com.github.laxika.magicalvibes.model;


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
import java.util.stream.Collectors;

import com.github.laxika.magicalvibes.model.action.DelayedAction;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
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
    /** "All Random" game mode: every player is dealt a randomly generated deck. */
    public volatile boolean allRandom;
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
    /** Extra land plays granted this turn (e.g. Summer Bloom), on top of the normal one-per-turn. */
    public final Map<UUID, Integer> additionalLandsThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> permanentsEnteredBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** All spells cast by each player this turn. Access via {@link #recordSpellCast}, {@link #getSpellsCastThisTurnCount}, etc. */
    private final Map<UUID, List<Card>> spellsCastThisTurn = new ConcurrentHashMap<>();
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
    /** Tracks which permanent types each player has cast from graveyard this turn via Muldrotha-style effects. */
    public final Map<UUID, Set<CardType>> permanentTypesCastFromGraveyardThisTurn = new ConcurrentHashMap<>();
    /** Snapshot of per-player spell counts from the previous turn. Used by werewolf transform triggers. */
    public final Map<UUID, Integer> spellsCastLastTurn = new ConcurrentHashMap<>();
    /** Tracks which players declared at least one attacker this turn (for Angelic Arbiter etc.). */
    public final Set<UUID> playersDeclaredAttackersThisTurn = ConcurrentHashMap.newKeySet();
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
    public final Map<UUID, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
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
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    /** Player IDs → number of upcoming combat phases they must skip (Blinding Angel). Decremented as each is skipped. */
    public final Map<UUID, Integer> skipNextCombatPhaseCount = new ConcurrentHashMap<>();
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
    public final Map<UUID, UUID> drawReplacementTargetToController = new ConcurrentHashMap<>();
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisTurn = new ConcurrentHashMap<>();
    /** Per-permanent count of how many times its resolution-counting activated ability has resolved
     *  this turn (the {@code NthAbilityResolutionThisTurn} condition, e.g. Ashling the Pilgrim).
     *  Keyed by source permanent id; reset at the start of each turn. */
    public final Map<UUID, Integer> permanentAbilityResolutionsThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    public final Set<UUID> untilEndOfTurnStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> enchantmentDependentStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> permanentControlStolenCreatures = ConcurrentHashMap.newKeySet();
    /** Maps stolen creature ID → source permanent ID for "gain control for as long as you control [source]" effects.
     *  When the source permanent leaves the battlefield or changes controllers, the stolen creature is returned. */
    public final Map<UUID, UUID> sourceDependentStolenCreatures = new ConcurrentHashMap<>();
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
    public final Deque<UUID> extraTurns = new ArrayDeque<>();
    public int additionalCombatMainPhasePairs;
    public int lastBroadcastedLogSize = 0;
    public UUID draftId;
    public final Deque<LibraryBottomReorderRequest> pendingLibraryBottomReorders = new ArrayDeque<>();
    public final WarpWorldOperationState warpWorldOperation = new WarpWorldOperationState();
    public boolean cleanupDiscardPending;
    /** Tracks exile-until-source-leaves connections (O-ring style).
     *  Maps source permanent UUID to the exiled card + owner info.
     *  When the source permanent leaves the battlefield, the exiled card returns. */
    public final Map<UUID, PendingExileReturn> exileReturnOnPermanentLeave = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionIds = new ConcurrentHashMap<>();
    /** One-shot shields (Circle of Protection cycle): prevent the next damage event from a chosen source to a player. */
    public final List<PlayerSourceNextDamageShield> playerSourceNextDamageShields = Collections.synchronizedList(new ArrayList<>());
    /** One-shot shields (Sanctum Guardian): prevent the next damage event from a chosen source to ANY target
     *  (player, planeswalker, or creature). Each entry is a chosen source permanent ID, consumed on first use. */
    public final List<UUID> sourceNextDamageToAnyTargetShields = Collections.synchronizedList(new ArrayList<>());
    public final Set<UUID> permanentsPreventedFromDealingDamage = ConcurrentHashMap.newKeySet();
    /** Players whose damage (to themselves and their creatures) is fully prevented this turn (Safe Passage). */
    public final Set<UUID> playersWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** Specific creatures whose damage is fully prevented this turn (Wellgabber Apothecary). */
    public final Set<UUID> creaturesWithAllDamagePrevented = ConcurrentHashMap.newKeySet();
    /** When true, damage can't be prevented this turn (Impractical Joke). Cleared at turn cleanup. */
    public boolean damageCantBePreventedThisTurn = false;
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
    /** Creature-specific damage redirect shields (e.g. Oracle's Attendants): redirect all damage a chosen source would deal to a specific creature this turn onto another permanent. */
    public final List<CreatureDamageRedirectShield> creatureDamageRedirectShields = Collections.synchronizedList(new ArrayList<>());
    /** Queue for "each player returns up to N cards from graveyard to battlefield" choices. */
    public final List<PendingGraveyardReturnChoice> pendingGraveyardReturnQueue = Collections.synchronizedList(new ArrayList<>());
    public final List<Emblem> emblems = Collections.synchronizedList(new ArrayList<>());
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

    /** Per-player: this player has protection from these colors until end of turn (e.g. Faith's Shield fateful hour). Cleared at end of turn. */
    public final Map<UUID, Set<CardColor>> playerProtectionFromColorsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Players who can't cast spells this turn (e.g. Silence). Cleared at end of turn and on new turn. */
    public final Set<UUID> playersSilencedThisTurn = ConcurrentHashMap.newKeySet();

    /** Card IDs that have been granted flashback until end of turn (e.g. Past in Flames).
     *  The flashback cost for these cards equals their mana cost. Cleared at end of turn. */
    public final Set<UUID> cardsGrantedFlashbackUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    public record GraveyardCreatureCastPermission(UUID sourcePermanentId, UUID castingPlayerId) {}

    /** Targeted creature cards that may be cast from a graveyard this turn.
     *  Maps graveyard card UUID -> source permanent and casting player (e.g. Havengul Lich).
     *  Cleared at end of turn. */
    public final Map<UUID, GraveyardCreatureCastPermission> graveyardCreatureCastPermissionsUntilEndOfTurn = new ConcurrentHashMap<>();

    /** Players whose instant/sorcery spells are automatically copied until end of turn
     *  (e.g. The Mirari Conjecture chapter III). Cleared at end of turn. */
    public final Set<UUID> playersWithSpellCopyUntilEndOfTurn = ConcurrentHashMap.newKeySet();

    /** Pending one-shot spell copy triggers from mana abilities (e.g. Primal Wellspring).
     *  Each value tracks how many copies are pending for that player.
     *  Decremented when an instant/sorcery is cast; cleared when mana pools drain. */
    public final Map<UUID, Integer> pendingNextInstantSorceryCopyCount = new ConcurrentHashMap<>();

    /**
     * Paradigm (CR 702.192): delayed triggers that fire at the beginning of each of the
     * controller's precombat main phases for the rest of the game.
     */
    public final List<ParadigmDelayedTrigger> paradigmDelayedTriggers = Collections.synchronizedList(new ArrayList<>());

    public record ParadigmDelayedTrigger(UUID controllerId, Card spellPrototype) {}

    /** Spell names a player has already resolved while controlling (for Paradigm's "first time" check). */
    public final Map<UUID, Set<String>> paradigmResolvedSpellNames = new ConcurrentHashMap<>();

    /** Remaining exiled spells to cast for an in-progress Improvisation Capstone resolution. */
    public final Deque<UUID> pendingImprovisationCapstoneCastQueue = new ArrayDeque<>();

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
    /** Card UUIDs whose exile-play permission expires at end of the turn number stored as the value
     *  (e.g. Archaic's Agony: until end of your next turn). */
    public final Map<UUID, Integer> exilePlayPermissionsExpireAtTurnEnd = new ConcurrentHashMap<>();
    /** Exiled card UUIDs that may be cast spending mana of any type (e.g. Nita, Forum Conciliator's
     *  activated ability). Complements the battlefield-permanent any-mana grant used by Hostage Taker.
     *  Cleared during cleanup step. */
    public final Set<UUID> exilePlayAnyManaType = ConcurrentHashMap.newKeySet();
    /** Card UUIDs that are exiled instead of being put into a graveyard (e.g. a spell cast via
     *  Nita, Forum Conciliator: "If that spell would be put into a graveyard, exile it instead").
     *  Cleared during cleanup step. */
    public final Set<UUID> exileInsteadOfGraveyard = ConcurrentHashMap.newKeySet();
    /** Maps graveyard card UUID → player UUID who may play it this turn (e.g. Ark of Hunger).
     *  Cleared during cleanup step for entries in {@link #graveyardPlayPermissionsExpireEndOfTurn}. */
    public final Map<UUID, UUID> graveyardPlayPermissions = new ConcurrentHashMap<>();
    /** Graveyard card UUIDs whose play permission expires at end of turn. */
    public final Set<UUID> graveyardPlayPermissionsExpireEndOfTurn = ConcurrentHashMap.newKeySet();
    /** Depth counter for batching "cards leave graveyard" triggers (one trigger per batch). */
    public int graveyardLeaveNotificationDepth = 0;
    /** Owners whose graveyards had cards leave during a suppressed batch; triggers fire when depth returns to 0. */
    public final Set<UUID> graveyardLeaveNotificationPendingOwners = ConcurrentHashMap.newKeySet();
    /** Players who had one or more cards leave their graveyard this turn (cleared at turn cleanup). Used by Wilt in the Heat cost reduction. */
    public final Set<UUID> playersWhoseCardsLeftGraveyardThisTurn = ConcurrentHashMap.newKeySet();
    /** Transient field: while a player is choosing a card to exile from hand, identifies the player who should
     *  gain permission to play that card for as long as it remains exiled (e.g. Fiend of the Shadows). Null when
     *  the exiling effect does not grant play permission to a controller. */

    /** Tracks how many cards each player has drawn this turn. */
    public final Map<UUID, Integer> cardsDrawnThisTurn = new ConcurrentHashMap<>();

    /** Tracks how much life each player has gained so far this turn (for "if you gained life this turn"
     *  conditions, e.g. Streets of New Capenna's Infusion cards). Cleared at the start of each turn. */
    public final Map<UUID, Integer> lifeGainedThisTurn = new ConcurrentHashMap<>();

    /** Tracks which permanents dealt combat damage to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. */
    public final Map<UUID, Set<UUID>> combatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

    /** Tracks which players have been dealt damage this turn (from any source — combat, spells, abilities). */
    public final Set<UUID> playersDealtDamageThisTurn = ConcurrentHashMap.newKeySet();

    /** Tracks how much damage each player has been dealt this turn (from any source — combat, spells,
     *  abilities; includes damage dealt as poison). Cleared at turn cleanup. Used by Final Punishment. */
    public final Map<UUID, Integer> damageDealtToPlayersThisTurn = new ConcurrentHashMap<>();

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

    /** Monotonic CR 613.7 timestamp source. Advanced via {@link #nextTimestamp()} whenever a
     *  permanent enters a battlefield, an Aura/Equipment becomes attached (CR 613.7e), or a
     *  resolving spell/ability creates a continuous effect. Never reset during a game. */
    public long timestampCounter;

    /** Returns the next CR 613.7 timestamp (strictly increasing, starting at 1). */
    public long nextTimestamp() {
        return ++timestampCounter;
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
        } else {
            stack.add(entry);
        }
    }

    /**
     * Appends a pending interaction to the tail of the unified queue.
     */
    public void queueInteraction(PendingInteraction interaction) {
        pendingInteractions.addLast(interaction);
    }

    /**
     * Puts a pending interaction at the head of the unified queue. Used when an
     * in-progress multi-step interaction must be serviced before anything else
     * (e.g. re-queuing an updated {@code ETBTokenMultiTargetTrigger} between target slots).
     */
    public void queueInteractionFirst(PendingInteraction interaction) {
        pendingInteractions.addFirst(interaction);
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

    /**
     * Returns the number of spells the given player has cast this turn.
     */
    public int getSpellsCastThisTurnCount(UUID playerId) {
        return spellsCastThisTurn.getOrDefault(playerId, List.of()).size();
    }

    /** Total lands the given player may play this turn: the normal one plus any additional grants. */
    public int getMaxLandsThisTurn(UUID playerId) {
        return 1 + additionalLandsThisTurn.getOrDefault(playerId, 0);
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
        copy.pendingAbilityActivation = this.pendingAbilityActivation; // immutable record
        copy.endTurnRequested = this.endTurnRequested;
        copy.discardCausedByOpponent = this.discardCausedByOpponent;
        copy.additionalCombatMainPhasePairs = this.additionalCombatMainPhasePairs;
        copy.lastBroadcastedLogSize = this.lastBroadcastedLogSize;
        copy.draftId = this.draftId;
        copy.cleanupDiscardPending = this.cleanupDiscardPending;
        copy.simulation = true;
        copy.timestampCounter = this.timestampCounter;
        copy.combatDamageFirstStrikeStepComplete = this.combatDamageFirstStrikeStepComplete;
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.pendingGraveyardReturnQueue.addAll(this.pendingGraveyardReturnQueue);

        // --- Set<UUID> (ConcurrentHashMap.newKeySet()) ---
        copy.playerIds.addAll(this.playerIds);
        copy.aiPlayerIds.addAll(this.aiPlayerIds);
        copy.playerKeptHand.addAll(this.playerKeptHand);
        copy.priorityPassedBy.addAll(this.priorityPassedBy);
        copy.preventDamageFromColors.addAll(this.preventDamageFromColors);
        copy.untilEndOfTurnStolenCreatures.addAll(this.untilEndOfTurnStolenCreatures);
        copy.enchantmentDependentStolenCreatures.addAll(this.enchantmentDependentStolenCreatures);
        copy.permanentControlStolenCreatures.addAll(this.permanentControlStolenCreatures);
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);
        copy.playersWithAllDamagePrevented.addAll(this.playersWithAllDamagePrevented);
        copy.creaturesWithAllDamagePrevented.addAll(this.creaturesWithAllDamagePrevented);
        copy.damageCantBePreventedThisTurn = this.damageCantBePreventedThisTurn;
        copy.damageRedirectShields.addAll(this.damageRedirectShields);
        copy.sourceDamageRedirectShields.addAll(this.sourceDamageRedirectShields);
        copy.creatureDamageRedirectShields.addAll(this.creatureDamageRedirectShields);
        copy.targetSourceDamagePreventionShields.addAll(this.targetSourceDamagePreventionShields);
        copy.playerSourceNextDamageShields.addAll(this.playerSourceNextDamageShields);
        copy.sourceNextDamageToAnyTargetShields.addAll(this.sourceNextDamageToAnyTargetShields);
        copy.stateTriggerOnStack.addAll(this.stateTriggerOnStack);

        // --- List<UUID> (synchronized) ---
        copy.orderedPlayerIds.addAll(this.orderedPlayerIds);
        copy.playerNames.addAll(this.playerNames);

        // --- Map<UUID, String/Integer> ---
        copy.playerIdToName.putAll(this.playerIdToName);
        copy.imprintedCards.putAll(this.imprintedCards);
        copy.playerDeckChoices.putAll(this.playerDeckChoices);
        copy.mulliganCounts.putAll(this.mulliganCounts);
        copy.playerNeedsToBottom.putAll(this.playerNeedsToBottom);
        copy.landsPlayedThisTurn.putAll(this.landsPlayedThisTurn);
        copy.additionalLandsThisTurn.putAll(this.additionalLandsThisTurn);
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
        copy.lifeGainedThisTurn.putAll(this.lifeGainedThisTurn);
        this.combatDamageToPlayersThisTurn.forEach((k, v) ->
                copy.combatDamageToPlayersThisTurn.put(k, new HashSet<>(v)));
        copy.playersDealtDamageThisTurn.addAll(this.playersDealtDamageThisTurn);
        copy.damageDealtToPlayersThisTurn.putAll(this.damageDealtToPlayersThisTurn);
        copy.permanentsDealtDamageThisTurn.addAll(this.permanentsDealtDamageThisTurn);
        this.combatDamageSourceSubtypesThisTurn.forEach((k, v) ->
                copy.combatDamageSourceSubtypesThisTurn.put(k, new HashSet<>(v)));
        copy.combatDamageSourcesWithChangelingThisTurn.addAll(this.combatDamageSourcesWithChangelingThisTurn);

        // --- Map<UUID, Set<TurnStep>> ---
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.put(k, new HashSet<>(v)));

        // --- Map<UUID, List<Card>> (shared Card refs) ---
        this.playerDecks.forEach((k, v) -> copy.playerDecks.put(k, new ArrayList<>(v)));
        this.playerHands.forEach((k, v) -> copy.playerHands.put(k, new ArrayList<>(v)));
        this.playerGraveyards.forEach((k, v) -> copy.playerGraveyards.put(k, new ArrayList<>(v)));
        copy.exiledCards.addAll(this.exiledCards);
        copy.exiledCardEggCounters.putAll(this.exiledCardEggCounters);
        copy.exiledCardsWithSilverCounters.addAll(this.exiledCardsWithSilverCounters);

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

        // --- Unified delayed-action queue (immutable records, shallow copy — shared Card refs, as the
        //     per-mechanic fields it replaced were copied) ---
        copy.delayedActions.addAll(this.delayedActions);

        // --- Exile-until-source-leaves map (O-ring style) ---
        copy.exileReturnOnPermanentLeave.putAll(this.exileReturnOnPermanentLeave);

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---
        this.playerSourceDamagePreventionIds.forEach((k, v) ->
                copy.playerSourceDamagePreventionIds.put(k, new HashSet<>(v)));

        // --- GraveyardTargetOperationState ---
        copy.graveyardTargetOperation.card = this.graveyardTargetOperation.card;
        copy.graveyardTargetOperation.controllerId = this.graveyardTargetOperation.controllerId;
        copy.graveyardTargetOperation.effects = this.graveyardTargetOperation.effects;
        copy.graveyardTargetOperation.entryType = this.graveyardTargetOperation.entryType;
        copy.graveyardTargetOperation.xValue = this.graveyardTargetOperation.xValue;

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
        copy.permanentAbilityResolutionsThisTurn.putAll(this.permanentAbilityResolutionsThisTurn);

        // --- Deques ---
        copy.pendingInteractions.addAll(this.pendingInteractions);
        copy.extraTurns.addAll(this.extraTurns);
        this.pendingLibraryBottomReorders.forEach(req ->
                copy.pendingLibraryBottomReorders.add(new LibraryBottomReorderRequest(req.playerId(), new ArrayList<>(req.cards()))));

        // --- Combat damage assignment state ---
        this.combatDamagePlayerAssignments.forEach((k, v) ->
                copy.combatDamagePlayerAssignments.put(k, new HashMap<>(v)));
        copy.combatDamagePendingIndices.addAll(this.combatDamagePendingIndices);
        copy.combatDamagePhase1State = this.combatDamagePhase1State; // read-only snapshot from phase 1

        // --- Emblems (records are immutable) ---
        copy.emblems.addAll(this.emblems);

        // --- Permanent no-max-hand-size grants ---
        copy.playersWithNoMaximumHandSize.addAll(this.playersWithNoMaximumHandSize);

        // --- Source-linked animations (Awakener Druid-style) ---
        copy.sourceLinkedAnimations.putAll(this.sourceLinkedAnimations);

        // --- Per-player spell/creature color protection (Autumn's Veil style) ---
        this.playerSpellsCantBeCounteredByColorsThisTurn.forEach((k, v) ->
                copy.playerSpellsCantBeCounteredByColorsThisTurn.put(k, new HashSet<>(v)));
        this.playerCreaturesCantBeTargetedByColorsThisTurn.forEach((k, v) ->
                copy.playerCreaturesCantBeTargetedByColorsThisTurn.put(k, new HashSet<>(v)));
        this.playerProtectionFromColorsUntilEndOfTurn.forEach((k, v) ->
                copy.playerProtectionFromColorsUntilEndOfTurn.put(k, new HashSet<>(v)));

        // --- Silence-style "opponents can't cast" flag ---
        copy.playersSilencedThisTurn.addAll(this.playersSilencedThisTurn);

        // --- Spell copy until end of turn (The Mirari Conjecture chapter III) ---
        copy.playersWithSpellCopyUntilEndOfTurn.addAll(this.playersWithSpellCopyUntilEndOfTurn);

        // --- Pending one-shot spell copy triggers (Primal Wellspring) ---
        copy.pendingNextInstantSorceryCopyCount.putAll(this.pendingNextInstantSorceryCopyCount);

        copy.exilePlayPermissions.putAll(this.exilePlayPermissions);
        copy.exilePlayPermissionsExpireEndOfTurn.addAll(this.exilePlayPermissionsExpireEndOfTurn);
        copy.exilePlayPermissionsExpireAtTurnEnd.putAll(this.exilePlayPermissionsExpireAtTurnEnd);
        copy.exilePlayAnyManaType.addAll(this.exilePlayAnyManaType);
        copy.exileInsteadOfGraveyard.addAll(this.exileInsteadOfGraveyard);
        copy.graveyardPlayPermissions.putAll(this.graveyardPlayPermissions);
        copy.graveyardPlayPermissionsExpireEndOfTurn.addAll(this.graveyardPlayPermissionsExpireEndOfTurn);
        copy.graveyardLeaveNotificationDepth = this.graveyardLeaveNotificationDepth;
        copy.graveyardLeaveNotificationPendingOwners.addAll(this.graveyardLeaveNotificationPendingOwners);
        copy.playersWhoseCardsLeftGraveyardThisTurn.addAll(this.playersWhoseCardsLeftGraveyardThisTurn);

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
        copy.pendingImprovisationCapstoneCastQueue.addAll(this.pendingImprovisationCapstoneCastQueue);

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
            target.interaction.beginInteraction(source.activeInteraction());
        }
    }
}
