package com.github.laxika.magicalvibes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameData {

    public final long id;
    public final String gameName;
    public final long createdByUserId;
    public final String createdByUsername;
    public final LocalDateTime createdAt;
    public GameStatus status;
    public final Set<Long> playerIds = ConcurrentHashMap.newKeySet();
    public final List<Long> orderedPlayerIds = Collections.synchronizedList(new ArrayList<>());
    public final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
    public final Map<Long, String> playerIdToName = new ConcurrentHashMap<>();
    public final Map<Long, List<Card>> playerDecks = new ConcurrentHashMap<>();
    public final Map<Long, List<Card>> playerHands = new ConcurrentHashMap<>();
    public final Map<Long, Integer> mulliganCounts = new ConcurrentHashMap<>();
    public final Set<Long> playerKeptHand = ConcurrentHashMap.newKeySet();
    public final Map<Long, Integer> playerNeedsToBottom = new ConcurrentHashMap<>();
    public final List<String> gameLog = Collections.synchronizedList(new ArrayList<>());
    public Long startingPlayerId;
    public TurnStep currentStep;
    public Long activePlayerId;
    public int turnNumber;
    public final Set<Long> priorityPassedBy = ConcurrentHashMap.newKeySet();
    public final Map<Long, Integer> landsPlayedThisTurn = new ConcurrentHashMap<>();
    public final Map<Long, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
    public final Map<Long, ManaPool> playerManaPools = new ConcurrentHashMap<>();
    public final Map<Long, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
    public final Map<Long, Integer> playerLifeTotals = new ConcurrentHashMap<>();
    public boolean awaitingAttackerDeclaration;
    public boolean awaitingBlockerDeclaration;
    public boolean awaitingCardChoice;
    public Long awaitingCardChoicePlayerId;
    public Set<Integer> awaitingCardChoiceValidIndices;

    public GameData(long id, String gameName, long createdByUserId, String createdByUsername) {
        this.id = id;
        this.gameName = gameName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.createdAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
    }
}
