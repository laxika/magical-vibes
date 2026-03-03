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
import java.util.stream.Collectors;

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
    public final Map<UUID, Integer> spellsCastThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
    public final Map<UUID, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<UUID, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerPoisonCounters = new ConcurrentHashMap<>();
    public final InteractionState interaction = new InteractionState();
    public final List<StackEntry> stack = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, List<Card>> playerGraveyards = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsPutIntoGraveyardFromBattlefieldThisTurn = new ConcurrentHashMap<>();
    /** Counts all creature deaths (including tokens) from battlefield this turn, per controller. */
    public final Map<UUID, Integer> creatureDeathCountThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> creatureCardsDamagedThisTurnBySourcePermanent = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> playerExiledCards = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    public int globalDamagePreventionShield;
    public boolean preventAllCombatDamage;
    public final Set<CardColor> preventDamageFromColors = ConcurrentHashMap.newKeySet();
    public UUID combatDamageRedirectTarget;
    public final Map<UUID, Map<CardColor, Integer>> playerColorDamagePreventionCount = new ConcurrentHashMap<>();
    public final List<PendingMayAbility> pendingMayAbilities = new ArrayList<>();
    public final GraveyardTargetOperationState graveyardTargetOperation = new GraveyardTargetOperationState();
    public final CloneOperationState cloneOperation = new CloneOperationState();
    public UUID imprintSourcePermanentId;
    public PendingOpponentExileChoice pendingOpponentExileChoice;
    public UUID pendingCombatDamageBounceTargetPlayerId;
    public int pendingProliferateCount;
    public StackEntry pendingEffectResolutionEntry;
    public int pendingEffectResolutionIndex;
    public Integer chosenXValue;
    public final Set<UUID> permanentsToSacrificeAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public PendingAbilityActivation pendingAbilityActivation;
    public final Map<UUID, UUID> drawReplacementTargetToController = new ConcurrentHashMap<>();
    public final Map<UUID, Map<Integer, Integer>> activatedAbilityUsesThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    public final Set<UUID> untilEndOfTurnStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> enchantmentDependentStolenCreatures = ConcurrentHashMap.newKeySet();
    public final Set<UUID> permanentControlStolenCreatures = ConcurrentHashMap.newKeySet();
    public boolean endTurnRequested;
    public final Deque<PermanentChoiceContext.DeathTriggerTarget> pendingDeathTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.DiscardTriggerAnyTarget> pendingDiscardSelfTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.AttackTriggerTarget> pendingAttackTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.SpellTargetTriggerAnyTarget> pendingSpellTargetTriggers = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.EmblemTriggerTarget> pendingEmblemTriggerTargets = new ArrayDeque<>();
    public final Deque<PermanentChoiceContext.UpkeepCopyTriggerTarget> pendingUpkeepCopyTargets = new ArrayDeque<>();
    public boolean discardCausedByOpponent;
    public PendingReturnToHandOnDiscardType pendingReturnToHandOnDiscardType;
    public final Deque<UUID> extraTurns = new ArrayDeque<>();
    public int additionalCombatMainPhasePairs;
    public int lastBroadcastedLogSize = 0;
    public UUID draftId;
    public final Deque<LibraryBottomReorderRequest> pendingLibraryBottomReorders = new ArrayDeque<>();
    public final WarpWorldOperationState warpWorldOperation = new WarpWorldOperationState();
    public boolean cleanupDiscardPending;
    public final List<PendingExileReturn> pendingExileReturns = Collections.synchronizedList(new ArrayList<>());
    /** Tracks exile-until-source-leaves connections (O-ring style).
     *  Maps source permanent UUID to the exiled card + owner info.
     *  When the source permanent leaves the battlefield, the exiled card returns. */
    public final Map<UUID, PendingExileReturn> exileReturnOnPermanentLeave = new ConcurrentHashMap<>();
    public final Set<UUID> pendingTokenExilesAtEndStep = ConcurrentHashMap.newKeySet();
    public final Map<UUID, Set<UUID>> playerSourceDamagePreventionIds = new ConcurrentHashMap<>();
    public final Set<UUID> permanentsPreventedFromDealingDamage = ConcurrentHashMap.newKeySet();
    public boolean pendingSacrificeAttackingCreature;
    public boolean pendingAwakeningCounterPlacement;
    public UUID pendingTapSubtypeBoostSourcePermanentId;
    public final List<Emblem> emblems = Collections.synchronizedList(new ArrayList<>());

    /** Tracks how many cards each player has drawn this turn. */
    public final Map<UUID, Integer> cardsDrawnThisTurn = new ConcurrentHashMap<>();

    /** Tracks which permanents dealt combat damage to which players this turn.
     *  Maps source permanent UUID → set of damaged player UUIDs. */
    public final Map<UUID, Set<UUID>> combatDamageToPlayersThisTurn = new ConcurrentHashMap<>();

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

    /** Damage assignments provided at cast time for an ETB divided-damage effect (e.g. Kuldotha Flamefiend). */
    public Map<UUID, Integer> pendingETBDamageAssignments = Map.of();


    // Combat damage assignment state
    public final Map<Integer, Map<UUID, Integer>> combatDamagePlayerAssignments = new HashMap<>();
    public final List<Integer> combatDamagePendingIndices = new ArrayList<>();
    public boolean combatDamagePhase1Complete = false;
    public CombatDamagePhase1State combatDamagePhase1State;

    // CR 704.5b — track players who attempted to draw from an empty library
    public final Set<UUID> playersAttemptedDrawFromEmptyLibrary = ConcurrentHashMap.newKeySet();

    public GameData(UUID id, String gameName, UUID createdByUserId, String createdByUsername) {
        this.id = id;
        this.gameName = gameName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.createdAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
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
     * Creates a deep copy of this game state for AI simulation (MCTS).
     * <ul>
     *   <li>Card objects are shared (immutable after construction)</li>
     *   <li>Permanent objects are deep-copied (mutable state)</li>
     *   <li>Collections are copied to new independent instances</li>
     *   <li>Primitive/enum/UUID/String fields are assigned directly</li>
     * </ul>
     */
    public GameData deepCopy() {
        GameData copy = new GameData(id, gameName, createdByUserId, createdByUsername);

        // --- Primitives, enums, UUIDs, Strings ---
        copy.status = this.status;
        copy.startingPlayerId = this.startingPlayerId;
        copy.currentStep = this.currentStep;
        copy.activePlayerId = this.activePlayerId;
        copy.turnNumber = this.turnNumber;
        copy.globalDamagePreventionShield = this.globalDamagePreventionShield;
        copy.preventAllCombatDamage = this.preventAllCombatDamage;
        copy.combatDamageRedirectTarget = this.combatDamageRedirectTarget;
        copy.pendingCombatDamageBounceTargetPlayerId = this.pendingCombatDamageBounceTargetPlayerId;
        copy.pendingProliferateCount = this.pendingProliferateCount;
        copy.pendingEffectResolutionEntry = this.pendingEffectResolutionEntry != null
                ? new StackEntry(this.pendingEffectResolutionEntry) : null;
        copy.pendingEffectResolutionIndex = this.pendingEffectResolutionIndex;
        copy.chosenXValue = this.chosenXValue;
        copy.pendingAbilityActivation = this.pendingAbilityActivation; // immutable record
        copy.endTurnRequested = this.endTurnRequested;
        copy.discardCausedByOpponent = this.discardCausedByOpponent;
        copy.additionalCombatMainPhasePairs = this.additionalCombatMainPhasePairs;
        copy.lastBroadcastedLogSize = this.lastBroadcastedLogSize;
        copy.draftId = this.draftId;
        copy.cleanupDiscardPending = this.cleanupDiscardPending;
        copy.combatDamagePhase1Complete = this.combatDamagePhase1Complete;
        copy.pendingSacrificeAttackingCreature = this.pendingSacrificeAttackingCreature;
        copy.pendingAwakeningCounterPlacement = this.pendingAwakeningCounterPlacement;
        copy.pendingTapSubtypeBoostSourcePermanentId = this.pendingTapSubtypeBoostSourcePermanentId;

        // --- Set<UUID> (ConcurrentHashMap.newKeySet()) ---
        copy.playerIds.addAll(this.playerIds);
        copy.playerKeptHand.addAll(this.playerKeptHand);
        copy.priorityPassedBy.addAll(this.priorityPassedBy);
        copy.preventDamageFromColors.addAll(this.preventDamageFromColors);
        copy.permanentsToSacrificeAtEndOfCombat.addAll(this.permanentsToSacrificeAtEndOfCombat);
        copy.untilEndOfTurnStolenCreatures.addAll(this.untilEndOfTurnStolenCreatures);
        copy.enchantmentDependentStolenCreatures.addAll(this.enchantmentDependentStolenCreatures);
        copy.permanentControlStolenCreatures.addAll(this.permanentControlStolenCreatures);
        copy.playersAttemptedDrawFromEmptyLibrary.addAll(this.playersAttemptedDrawFromEmptyLibrary);

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
        copy.spellsCastThisTurn.putAll(this.spellsCastThisTurn);
        copy.playerLifeTotals.putAll(this.playerLifeTotals);
        copy.playerPoisonCounters.putAll(this.playerPoisonCounters);
        copy.playerDamagePreventionShields.putAll(this.playerDamagePreventionShields);
        copy.stolenCreatures.putAll(this.stolenCreatures);
        copy.drawReplacementTargetToController.putAll(this.drawReplacementTargetToController);
        copy.cardsDrawnThisTurn.putAll(this.cardsDrawnThisTurn);
        this.combatDamageToPlayersThisTurn.forEach((k, v) -> {
            Set<UUID> s = ConcurrentHashMap.newKeySet();
            s.addAll(v);
            copy.combatDamageToPlayersThisTurn.put(k, s);
        });

        // --- Map<UUID, Set<TurnStep>> ---
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.put(k, ConcurrentHashMap.newKeySet()));
        this.playerAutoStopSteps.forEach((k, v) -> copy.playerAutoStopSteps.get(k).addAll(v));

        // --- Map<UUID, List<Card>> (shared Card refs) ---
        this.playerDecks.forEach((k, v) -> copy.playerDecks.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.playerHands.forEach((k, v) -> copy.playerHands.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.playerGraveyards.forEach((k, v) -> copy.playerGraveyards.put(k, Collections.synchronizedList(new ArrayList<>(v))));
        this.playerExiledCards.forEach((k, v) -> copy.playerExiledCards.put(k, Collections.synchronizedList(new ArrayList<>(v))));

        // --- Map<UUID, List<Permanent>> (deep copy each Permanent) ---
        this.playerBattlefields.forEach((k, v) ->
                copy.playerBattlefields.put(k, Collections.synchronizedList(
                        v.stream().map(Permanent::new).collect(Collectors.toCollection(ArrayList::new)))));

        // --- Map<UUID, ManaPool> (deep copy each ManaPool) ---
        this.playerManaPools.forEach((k, v) -> copy.playerManaPools.put(k, new ManaPool(v)));

        // --- List<StackEntry> (deep copy each StackEntry) ---
        this.stack.forEach(se -> copy.stack.add(new StackEntry(se)));

        // --- InteractionState ---
        InteractionState copiedInteraction = this.interaction.deepCopy();
        copyInteractionInto(copy, copiedInteraction);

        // --- Map<UUID, Set<UUID>> ---
        this.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.forEach((k, v) -> {
            Set<UUID> s = ConcurrentHashMap.newKeySet();
            s.addAll(v);
            copy.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(k, s);
        });
        copy.creatureDeathCountThisTurn.putAll(this.creatureDeathCountThisTurn);
        this.creatureCardsDamagedThisTurnBySourcePermanent.forEach((k, v) -> {
            Set<UUID> s = ConcurrentHashMap.newKeySet();
            s.addAll(v);
            copy.creatureCardsDamagedThisTurnBySourcePermanent.put(k, s);
        });

        // --- Map<UUID, Map<CardColor, Integer>> ---
        this.playerColorDamagePreventionCount.forEach((k, v) ->
                copy.playerColorDamagePreventionCount.put(k, new ConcurrentHashMap<>(v)));

        // --- PendingMayAbility list (records with shared Card refs) ---
        copy.pendingMayAbilities.addAll(this.pendingMayAbilities);

        // --- PendingExileReturn list (records with shared Card refs) ---
        copy.pendingExileReturns.addAll(this.pendingExileReturns);

        // --- Exile-until-source-leaves map (O-ring style) ---
        copy.exileReturnOnPermanentLeave.putAll(this.exileReturnOnPermanentLeave);

        // --- Pending token exiles at end step (Mimic Vat) ---
        copy.pendingTokenExilesAtEndStep.addAll(this.pendingTokenExilesAtEndStep);

        // --- Map<UUID, Set<UUID>> (source damage prevention) ---
        this.playerSourceDamagePreventionIds.forEach((k, v) -> {
            Set<UUID> s = ConcurrentHashMap.newKeySet();
            s.addAll(v);
            copy.playerSourceDamagePreventionIds.put(k, s);
        });

        // --- GraveyardTargetOperationState ---
        copy.graveyardTargetOperation.card = this.graveyardTargetOperation.card;
        copy.graveyardTargetOperation.controllerId = this.graveyardTargetOperation.controllerId;
        copy.graveyardTargetOperation.effects = this.graveyardTargetOperation.effects;
        copy.graveyardTargetOperation.entryType = this.graveyardTargetOperation.entryType;
        copy.graveyardTargetOperation.xValue = this.graveyardTargetOperation.xValue;

        // --- Imprint ---
        copy.imprintSourcePermanentId = this.imprintSourcePermanentId;

        // --- Post-exile search ---
        copy.pendingOpponentExileChoice = this.pendingOpponentExileChoice; // record — immutable

        // --- CloneOperationState ---
        copy.cloneOperation.card = this.cloneOperation.card;
        copy.cloneOperation.controllerId = this.cloneOperation.controllerId;
        copy.cloneOperation.etbTargetId = this.cloneOperation.etbTargetId;

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
                copy.activatedAbilityUsesThisTurn.put(k, new ConcurrentHashMap<>(v)));

        // --- Deques ---
        copy.pendingDeathTriggerTargets.addAll(this.pendingDeathTriggerTargets);
        copy.pendingDiscardSelfTriggers.addAll(this.pendingDiscardSelfTriggers);
        copy.pendingAttackTriggerTargets.addAll(this.pendingAttackTriggerTargets);
        copy.pendingSpellTargetTriggers.addAll(this.pendingSpellTargetTriggers);
        copy.pendingEmblemTriggerTargets.addAll(this.pendingEmblemTriggerTargets);
        copy.pendingUpkeepCopyTargets.addAll(this.pendingUpkeepCopyTargets);
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

        // --- Search tax payments (Leonin Arbiter) ---
        this.paidSearchTaxPermanentIds.forEach((k, v) -> {
            Set<UUID> s = ConcurrentHashMap.newKeySet();
            s.addAll(v);
            copy.paidSearchTaxPermanentIds.put(k, s);
        });

        // --- ETB / sacrifice damage assignments ---
        copy.pendingETBDamageAssignments = this.pendingETBDamageAssignments.isEmpty()
                ? Map.of() : new HashMap<>(this.pendingETBDamageAssignments);

        // --- Mindslaver turn control ---
        copy.pendingTurnControl.putAll(this.pendingTurnControl);
        copy.mindControlledPlayerId = this.mindControlledPlayerId;
        copy.mindControllerPlayerId = this.mindControllerPlayerId;

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
                    targetInteraction.beginCardChoice(cc.type(), cc.playerId(), cc.validIndices(), cc.targetPermanentId());
            case InteractionContext.PermanentChoice pc ->
                    targetInteraction.beginPermanentChoice(pc.playerId(), pc.validIds(), pc.context());
            case InteractionContext.GraveyardChoice gc ->
                    targetInteraction.beginGraveyardChoice(gc.playerId(), gc.validIndices(), gc.destination(), gc.cardPool());
            case InteractionContext.ColorChoice cc ->
                    targetInteraction.beginColorChoice(cc.playerId(), cc.permanentId(), cc.etbTargetPermanentId(), cc.context());
            case InteractionContext.MayAbilityChoice mc ->
                    targetInteraction.beginMayAbilityChoice(mc.playerId(), mc.description());
            case InteractionContext.MultiPermanentChoice mpc ->
                    targetInteraction.beginMultiPermanentChoice(mpc.playerId(), mpc.validIds(), mpc.maxCount());
            case InteractionContext.MultiGraveyardChoice mgc ->
                    targetInteraction.beginMultiGraveyardChoice(mgc.playerId(), mgc.validCardIds(), mgc.maxCount());
            case InteractionContext.LibraryReorder lr ->
                    targetInteraction.beginLibraryReorder(lr.playerId(), lr.cards() != null ? new ArrayList<>(lr.cards()) : null, lr.toBottom());
            case InteractionContext.LibrarySearch ls ->
                    targetInteraction.beginLibrarySearch(ls.playerId(), ls.cards() != null ? new ArrayList<>(ls.cards()) : null,
                            ls.reveals(), ls.canFailToFind(), ls.targetPlayerId(), ls.remainingCount(),
                            ls.sourceCards() != null ? new ArrayList<>(ls.sourceCards()) : null,
                            ls.reorderRemainingToBottom(), ls.shuffleAfterSelection(), ls.prompt(), ls.destination());
            case InteractionContext.LibraryRevealChoice lrc ->
                    targetInteraction.beginLibraryRevealChoice(lrc.playerId(),
                            lrc.allCards() != null ? new ArrayList<>(lrc.allCards()) : null,
                            lrc.validCardIds() != null ? new HashSet<>(lrc.validCardIds()) : null,
                            lrc.remainingToGraveyard());
            case InteractionContext.HandTopBottomChoice htbc ->
                    targetInteraction.beginHandTopBottomChoice(htbc.playerId(),
                            htbc.cards() != null ? new ArrayList<>(htbc.cards()) : null);
            case InteractionContext.RevealedHandChoice rhc ->
                    targetInteraction.beginRevealedHandChoice(rhc.choosingPlayerId(), rhc.targetPlayerId(),
                            rhc.validIndices(), rhc.remainingCount(), rhc.discardMode(), rhc.chosenCards());
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
        }

        // Copy discard remaining count (not part of context reconstruction)
        if (source.discardRemainingCount() > 0) {
            targetInteraction.setDiscardRemainingCount(source.discardRemainingCount());
        }
    }
}
