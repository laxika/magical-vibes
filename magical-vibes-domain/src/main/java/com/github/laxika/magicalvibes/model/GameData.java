package com.github.laxika.magicalvibes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    public final Map<UUID, Integer> spellsCastThisTurn = new ConcurrentHashMap<>();
    public final Map<UUID, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
    public final Map<UUID, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<UUID, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public AwaitingInput awaitingInput;
    public UUID awaitingCardChoicePlayerId;
    public Set<Integer> awaitingCardChoiceValidIndices;
    public final List<StackEntry> stack = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, List<Card>> playerGraveyards = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> playerDamagePreventionShields = new ConcurrentHashMap<>();
    public int globalDamagePreventionShield;
    public boolean preventAllCombatDamage;
    public final Set<CardColor> preventDamageFromColors = ConcurrentHashMap.newKeySet();
    public UUID combatDamageRedirectTarget;
    public UUID awaitingPermanentChoicePlayerId;
    public Set<UUID> awaitingPermanentChoiceValidIds;
    public Card pendingAuraCard;
    public PermanentChoiceContext permanentChoiceContext;
    public UUID pendingCardChoiceTargetPermanentId;
    public UUID awaitingGraveyardChoicePlayerId;
    public Set<Integer> awaitingGraveyardChoiceValidIndices;
    public GraveyardChoiceDestination graveyardChoiceDestination;
    public UUID awaitingColorChoicePlayerId;
    public UUID awaitingColorChoicePermanentId;
    public UUID pendingColorChoiceETBTargetId;
    public ColorChoiceContext colorChoiceContext;
    public final Map<UUID, Map<CardColor, Integer>> playerColorDamagePreventionCount = new ConcurrentHashMap<>();
    public final List<PendingMayAbility> pendingMayAbilities = new ArrayList<>();
    public UUID awaitingMayAbilityPlayerId;
    public UUID awaitingMultiPermanentChoicePlayerId;
    public Set<UUID> awaitingMultiPermanentChoiceValidIds;
    public int awaitingMultiPermanentChoiceMaxCount;
    public UUID pendingCombatDamageBounceTargetPlayerId;
    public UUID awaitingLibraryReorderPlayerId;
    public List<Card> awaitingLibraryReorderCards;
    public final Set<UUID> permanentsToSacrificeAtEndOfCombat = ConcurrentHashMap.newKeySet();
    public int awaitingDiscardRemainingCount;
    public final Map<UUID, UUID> drawReplacementTargetToController = new ConcurrentHashMap<>();
    public final Map<UUID, UUID> stolenCreatures = new ConcurrentHashMap<>();
    public final Set<UUID> enchantmentDependentStolenCreatures = ConcurrentHashMap.newKeySet();

    public GameData(UUID id, String gameName, UUID createdByUserId, String createdByUsername) {
        this.id = id;
        this.gameName = gameName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.createdAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
    }
}
