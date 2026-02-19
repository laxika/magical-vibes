package com.github.laxika.magicalvibes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DraftData {

    // Identity
    public final UUID id;
    public final String draftName;
    public final UUID createdByUserId;
    public final String createdByUsername;
    public final String setCode;
    public final LocalDateTime createdAt;

    // Status
    public volatile DraftStatus status;

    // Players (8 total)
    public final List<UUID> playerIds = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, String> playerNames = new ConcurrentHashMap<>();
    public final Set<UUID> aiPlayerIds = ConcurrentHashMap.newKeySet();

    // Draft picking
    public int currentPackNumber;
    public int currentPickNumber;
    public final Map<UUID, List<Card>> currentPacks = new ConcurrentHashMap<>();
    public final Map<UUID, List<Card>> draftPools = new ConcurrentHashMap<>();
    public final Set<UUID> playersPickedThisRound = ConcurrentHashMap.newKeySet();

    // Deck building
    public final Map<UUID, List<Card>> builtDecks = new ConcurrentHashMap<>();
    public final Set<UUID> deckSubmitted = ConcurrentHashMap.newKeySet();
    public long deckBuildingDeadline;

    // Tournament
    public final List<List<List<UUID>>> tournamentRounds = Collections.synchronizedList(new ArrayList<>());
    public int currentRound;
    public final Set<UUID> roundWinners = ConcurrentHashMap.newKeySet();
    public final Map<UUID, UUID> activeGameForPlayer = new ConcurrentHashMap<>();
    public final Set<UUID> tournamentGameIds = ConcurrentHashMap.newKeySet();
    public UUID tournamentWinnerId;

    public DraftData(UUID id, String draftName, UUID createdByUserId, String createdByUsername, String setCode) {
        this.id = id;
        this.draftName = draftName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.setCode = setCode;
        this.createdAt = LocalDateTime.now();
        this.status = DraftStatus.WAITING;
    }
}
