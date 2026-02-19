package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.ai.AiConnection;
import com.github.laxika.magicalvibes.ai.AiDecisionEngine;
import com.github.laxika.magicalvibes.ai.AiDraftEngine;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.DraftStatus;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.DeckBuildingStateMessage;
import com.github.laxika.magicalvibes.networking.message.DraftFinishedMessage;
import com.github.laxika.magicalvibes.networking.message.DraftJoinedMessage;
import com.github.laxika.magicalvibes.networking.message.DraftPackUpdateMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.JoinGameMessage;
import com.github.laxika.magicalvibes.networking.message.TournamentGameReadyMessage;
import com.github.laxika.magicalvibes.networking.message.TournamentUpdateMessage;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DraftService {

    private static final int PLAYERS_PER_DRAFT = 8;
    private static final int CARDS_PER_PACK = 15;
    private static final int PACKS_PER_DRAFT = 3;
    private static final long DECK_BUILDING_TIMEOUT_MS = 10 * 60 * 1000; // 10 minutes

    private final DraftRegistry draftRegistry;
    private final GameRegistry gameRegistry;
    private final GameService gameService;
    private final SessionManager sessionManager;
    private final WebSocketSessionManager webSocketSessionManager;
    private final CardViewFactory cardViewFactory;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<UUID, AiDraftEngine> aiDraftEngines = new ConcurrentHashMap<>();

    public DraftService(DraftRegistry draftRegistry,
                        GameRegistry gameRegistry,
                        @Lazy GameService gameService,
                        SessionManager sessionManager,
                        WebSocketSessionManager webSocketSessionManager,
                        CardViewFactory cardViewFactory,
                        ObjectMapper objectMapper) {
        this.draftRegistry = draftRegistry;
        this.gameRegistry = gameRegistry;
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.webSocketSessionManager = webSocketSessionManager;
        this.cardViewFactory = cardViewFactory;
        this.objectMapper = objectMapper;
    }

    // ===== Draft Creation =====

    public DraftData createDraft(String draftName, UUID creatorId, String creatorName, String setCode, int aiCount) {
        UUID draftId = UUID.randomUUID();
        DraftData draftData = new DraftData(draftId, draftName, creatorId, creatorName, setCode);
        draftData.playerIds.add(creatorId);
        draftData.playerNames.put(creatorId, creatorName);
        draftData.draftPools.put(creatorId, new ArrayList<>());

        // Add AI players
        for (int i = 0; i < aiCount; i++) {
            UUID aiId = UUID.randomUUID();
            String aiName = "AI Drafter " + (i + 1);
            draftData.playerIds.add(aiId);
            draftData.playerNames.put(aiId, aiName);
            draftData.aiPlayerIds.add(aiId);
            draftData.draftPools.put(aiId, new ArrayList<>());
            aiDraftEngines.put(aiId, new AiDraftEngine());
        }

        draftRegistry.register(draftData);
        log.info("Draft created: id={}, name='{}', creator={}, aiCount={}", draftId, draftName, creatorName, aiCount);

        if (draftData.playerIds.size() >= PLAYERS_PER_DRAFT) {
            startDraft(draftData);
        }

        return draftData;
    }

    // ===== Pack Generation =====

    List<Card> generateBoosterPack(String setCode) {
        CardSet cardSet = findCardSet(setCode);

        // Partition printings by rarity (excluding basic lands)
        List<CardPrinting> commons = new ArrayList<>();
        List<CardPrinting> uncommons = new ArrayList<>();
        List<CardPrinting> rares = new ArrayList<>();
        List<CardPrinting> mythics = new ArrayList<>();

        for (CardPrinting printing : cardSet.getPrintings()) {
            Card test = printing.createCard();
            if (test.getSupertypes().contains(CardSupertype.BASIC)) continue;

            String rarity = ScryfallOracleLoader.getRarity(setCode, printing.collectorNumber());
            if (rarity == null) {
                // Fallback: treat unknown rarity as common
                commons.add(printing);
                continue;
            }
            switch (rarity) {
                case "common" -> commons.add(printing);
                case "uncommon" -> uncommons.add(printing);
                case "rare" -> rares.add(printing);
                case "mythic" -> mythics.add(printing);
                default -> commons.add(printing);
            }
        }

        List<Card> pack = new ArrayList<>();

        // 1 Rare or Mythic Rare (mythic replaces rare ~1/8 of the time)
        if (!mythics.isEmpty() && random.nextInt(8) == 0) {
            pack.add(pickRandom(mythics).createCard());
        } else if (!rares.isEmpty()) {
            pack.add(pickRandom(rares).createCard());
        } else if (!mythics.isEmpty()) {
            pack.add(pickRandom(mythics).createCard());
        }

        // 3 Uncommons
        addRandomCards(pack, uncommons, 3);

        // Fill remaining slots with Commons (typically 11)
        addRandomCards(pack, commons, CARDS_PER_PACK - pack.size());

        return pack;
    }

    private CardPrinting pickRandom(List<CardPrinting> printings) {
        return printings.get(random.nextInt(printings.size()));
    }

    private void addRandomCards(List<Card> pack, List<CardPrinting> pool, int count) {
        List<CardPrinting> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);

        Set<String> usedCollectorNumbers = ConcurrentHashMap.newKeySet();
        // Don't duplicate cards already in the pack
        for (Card existing : pack) {
            if (existing.getCollectorNumber() != null) {
                usedCollectorNumbers.add(existing.getCollectorNumber());
            }
        }

        int added = 0;
        for (CardPrinting printing : shuffled) {
            if (added >= count) break;
            if (usedCollectorNumbers.add(printing.collectorNumber())) {
                pack.add(printing.createCard());
                added++;
            }
        }
    }

    private CardSet findCardSet(String setCode) {
        for (CardSet cs : CardSet.values()) {
            if (cs.getCode().equals(setCode)) {
                return cs;
            }
        }
        throw new IllegalArgumentException("Unknown set code: " + setCode);
    }

    // ===== Draft Flow =====

    void startDraft(DraftData draftData) {
        synchronized (draftData) {
            draftData.status = DraftStatus.DRAFTING;
            draftData.currentPackNumber = 0;
            draftData.currentPickNumber = 0;

            // Generate a pack for each player
            for (UUID playerId : draftData.playerIds) {
                List<Card> pack = generateBoosterPack(draftData.setCode);
                draftData.currentPacks.put(playerId, pack);
            }

            log.info("Draft {} - Drafting started, pack 1 distributed", draftData.id);

            // Send packs to human players
            sendPackUpdatesToHumans(draftData);

            // Process AI picks
            processAiPicks(draftData);
        }
    }

    public void handlePick(DraftData draftData, UUID playerId, int cardIndex) {
        synchronized (draftData) {
            if (draftData.status != DraftStatus.DRAFTING) {
                throw new IllegalStateException("Draft is not in drafting phase");
            }

            if (draftData.playersPickedThisRound.contains(playerId)) {
                throw new IllegalStateException("You already picked this round");
            }

            List<Card> pack = draftData.currentPacks.get(playerId);
            if (pack == null || cardIndex < 0 || cardIndex >= pack.size()) {
                throw new IllegalArgumentException("Invalid card index");
            }

            // Remove card from pack, add to pool
            Card picked = pack.remove(cardIndex);
            draftData.draftPools.get(playerId).add(picked);
            draftData.playersPickedThisRound.add(playerId);

            log.info("Draft {} - {} picked {} (pack {}, pick {})", draftData.id,
                    draftData.playerNames.get(playerId), picked.getName(),
                    draftData.currentPackNumber + 1, draftData.currentPickNumber + 1);

            // Check if all players have picked
            if (draftData.playersPickedThisRound.size() >= PLAYERS_PER_DRAFT) {
                advancePick(draftData);
            }
        }
    }

    private void advancePick(DraftData draftData) {
        draftData.playersPickedThisRound.clear();
        draftData.currentPickNumber++;

        if (draftData.currentPickNumber >= CARDS_PER_PACK) {
            // Pack exhausted, move to next pack or end drafting
            draftData.currentPackNumber++;
            draftData.currentPickNumber = 0;

            if (draftData.currentPackNumber >= PACKS_PER_DRAFT) {
                // All 3 packs done - move to deck building
                log.info("Draft {} - All packs completed, moving to deck building", draftData.id);
                startDeckBuilding(draftData);
                return;
            }

            // Generate new packs
            for (UUID playerId : draftData.playerIds) {
                List<Card> pack = generateBoosterPack(draftData.setCode);
                draftData.currentPacks.put(playerId, pack);
            }
            log.info("Draft {} - Pack {} distributed", draftData.id, draftData.currentPackNumber + 1);
        } else {
            // Rotate packs
            rotatePacks(draftData);
        }

        // Send updated packs to humans
        sendPackUpdatesToHumans(draftData);

        // Process AI picks
        processAiPicks(draftData);
    }

    private void rotatePacks(DraftData draftData) {
        List<UUID> ids = new ArrayList<>(draftData.playerIds);
        Map<UUID, List<Card>> newPacks = new HashMap<>();

        boolean passLeft = (draftData.currentPackNumber % 2 == 0); // Pack 0,2 = left; Pack 1 = right

        for (int i = 0; i < ids.size(); i++) {
            UUID fromPlayer = ids.get(i);
            int toIndex;
            if (passLeft) {
                toIndex = (i + 1) % ids.size();
            } else {
                toIndex = (i - 1 + ids.size()) % ids.size();
            }
            UUID toPlayer = ids.get(toIndex);
            newPacks.put(toPlayer, draftData.currentPacks.get(fromPlayer));
        }

        draftData.currentPacks.putAll(newPacks);
    }

    private void processAiPicks(DraftData draftData) {
        for (UUID aiId : draftData.aiPlayerIds) {
            if (draftData.playersPickedThisRound.contains(aiId)) continue;

            List<Card> pack = draftData.currentPacks.get(aiId);
            if (pack == null || pack.isEmpty()) continue;

            AiDraftEngine engine = aiDraftEngines.get(aiId);
            int pickIndex = engine.pickCard(pack);

            Card picked = pack.remove(pickIndex);
            draftData.draftPools.get(aiId).add(picked);
            draftData.playersPickedThisRound.add(aiId);

            log.debug("Draft {} - AI {} picked {}", draftData.id, draftData.playerNames.get(aiId), picked.getName());
        }

        // Check if all players have now picked (human might have already picked)
        if (draftData.playersPickedThisRound.size() >= PLAYERS_PER_DRAFT) {
            advancePick(draftData);
        }
    }

    private void sendPackUpdatesToHumans(DraftData draftData) {
        for (UUID playerId : draftData.playerIds) {
            if (draftData.aiPlayerIds.contains(playerId)) continue;

            List<Card> pack = draftData.currentPacks.get(playerId);
            List<Card> pool = draftData.draftPools.get(playerId);

            List<CardView> packViews = pack.stream().map(cardViewFactory::create).toList();
            List<CardView> poolViews = pool.stream().map(cardViewFactory::create).toList();

            sessionManager.sendToPlayer(playerId, new DraftPackUpdateMessage(
                    packViews, draftData.currentPackNumber, draftData.currentPickNumber, poolViews));
        }
    }

    // ===== Deck Building =====

    void startDeckBuilding(DraftData draftData) {
        draftData.status = DraftStatus.DECK_BUILDING;
        draftData.deckBuildingDeadline = System.currentTimeMillis() + DECK_BUILDING_TIMEOUT_MS;

        // Auto-build AI decks immediately
        for (UUID aiId : draftData.aiPlayerIds) {
            AiDraftEngine engine = aiDraftEngines.get(aiId);
            List<Card> pool = draftData.draftPools.get(aiId);
            AiDraftEngine.DeckBuildResult result = engine.buildDeck(pool);

            List<Card> deck = buildDeckFromIndicesAndLands(pool, result.cardIndices(), result.basicLands(), draftData.setCode);
            draftData.builtDecks.put(aiId, deck);
            draftData.deckSubmitted.add(aiId);

            log.info("Draft {} - AI {} deck built ({} cards)", draftData.id, draftData.playerNames.get(aiId), deck.size());
        }

        // Send pool to human players
        for (UUID playerId : draftData.playerIds) {
            if (draftData.aiPlayerIds.contains(playerId)) continue;

            List<Card> pool = draftData.draftPools.get(playerId);
            List<CardView> poolViews = pool.stream().map(cardViewFactory::create).toList();

            sessionManager.sendToPlayer(playerId, new DeckBuildingStateMessage(poolViews, draftData.deckBuildingDeadline));
        }

        // Schedule timeout
        scheduler.schedule(() -> handleDeckBuildingTimeout(draftData),
                DECK_BUILDING_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    public void submitDeck(DraftData draftData, UUID playerId, List<Integer> cardIndices, Map<String, Integer> basicLands) {
        synchronized (draftData) {
            if (draftData.status != DraftStatus.DECK_BUILDING) {
                throw new IllegalStateException("Draft is not in deck building phase");
            }

            if (draftData.deckSubmitted.contains(playerId)) {
                throw new IllegalStateException("You already submitted your deck");
            }

            List<Card> pool = draftData.draftPools.get(playerId);
            List<Card> deck = buildDeckFromIndicesAndLands(pool, cardIndices, basicLands, draftData.setCode);

            if (deck.size() < 40) {
                throw new IllegalArgumentException("Deck must contain at least 40 cards (currently " + deck.size() + ")");
            }

            draftData.builtDecks.put(playerId, deck);
            draftData.deckSubmitted.add(playerId);

            log.info("Draft {} - {} submitted deck ({} cards)", draftData.id,
                    draftData.playerNames.get(playerId), deck.size());

            if (draftData.deckSubmitted.size() >= PLAYERS_PER_DRAFT) {
                startTournament(draftData);
            }
        }
    }

    private void handleDeckBuildingTimeout(DraftData draftData) {
        synchronized (draftData) {
            if (draftData.status != DraftStatus.DECK_BUILDING) return;

            // Auto-build decks for players who haven't submitted
            for (UUID playerId : draftData.playerIds) {
                if (draftData.deckSubmitted.contains(playerId)) continue;

                List<Card> pool = draftData.draftPools.get(playerId);
                AiDraftEngine autoBuilder = new AiDraftEngine();
                // Simulate picks to build affinity
                for (Card card : pool) {
                    autoBuilder.pickCard(List.of(card));
                }
                AiDraftEngine.DeckBuildResult result = autoBuilder.buildDeck(pool);
                List<Card> deck = buildDeckFromIndicesAndLands(pool, result.cardIndices(), result.basicLands(), draftData.setCode);

                draftData.builtDecks.put(playerId, deck);
                draftData.deckSubmitted.add(playerId);

                log.info("Draft {} - Auto-built deck for {} (timeout)", draftData.id, draftData.playerNames.get(playerId));
            }

            if (draftData.deckSubmitted.size() >= PLAYERS_PER_DRAFT) {
                startTournament(draftData);
            }
        }
    }

    private List<Card> buildDeckFromIndicesAndLands(List<Card> pool, List<Integer> cardIndices,
                                                     Map<String, Integer> basicLands, String setCode) {
        List<Card> deck = new ArrayList<>();

        for (int idx : cardIndices) {
            if (idx >= 0 && idx < pool.size()) {
                deck.add(pool.get(idx));
            }
        }

        // Add basic lands
        CardSet cardSet = findCardSet(setCode);
        Map<String, String> landCollectorNumbers = getBasicLandCollectorNumbers(cardSet);

        for (Map.Entry<String, Integer> entry : basicLands.entrySet()) {
            String landName = entry.getKey();
            int count = entry.getValue();
            String collectorNumber = landCollectorNumbers.get(landName);
            if (collectorNumber != null && count > 0) {
                CardPrinting printing = cardSet.findByCollectorNumber(collectorNumber);
                for (int i = 0; i < count; i++) {
                    deck.add(printing.createCard());
                }
            }
        }

        return deck;
    }

    private Map<String, String> getBasicLandCollectorNumbers(CardSet cardSet) {
        Map<String, String> result = new HashMap<>();
        for (CardPrinting printing : cardSet.getPrintings()) {
            Card test = printing.createCard();
            if (test.getSupertypes().contains(CardSupertype.BASIC) && test.getType() == CardType.LAND) {
                // Use the first collector number found for each basic land name
                result.putIfAbsent(test.getName(), printing.collectorNumber());
            }
        }
        return result;
    }

    // ===== Tournament =====

    void startTournament(DraftData draftData) {
        if (draftData.status != DraftStatus.DECK_BUILDING) {
            return; // Already transitioned (race between submit and timeout)
        }
        draftData.status = DraftStatus.TOURNAMENT;
        draftData.currentRound = 0;

        // Shuffle player order for random pairings
        List<UUID> shuffledPlayers = new ArrayList<>(draftData.playerIds);
        Collections.shuffle(shuffledPlayers, random);

        // Create quarterfinal pairings
        List<List<UUID>> pairings = new ArrayList<>();
        for (int i = 0; i < shuffledPlayers.size(); i += 2) {
            pairings.add(List.of(shuffledPlayers.get(i), shuffledPlayers.get(i + 1)));
        }
        draftData.tournamentRounds.add(pairings);

        log.info("Draft {} - Tournament started with {} pairings", draftData.id, pairings.size());

        // Broadcast bracket
        broadcastTournamentUpdate(draftData);

        // Start all games for this round
        for (List<UUID> pairing : pairings) {
            startTournamentGame(draftData, pairing.get(0), pairing.get(1));
        }
    }

    private void startTournamentGame(DraftData draftData, UUID player1Id, UUID player2Id) {
        String p1Name = draftData.playerNames.get(player1Id);
        String p2Name = draftData.playerNames.get(player2Id);
        String gameName = draftData.draftName + " - " + getRoundName(draftData.currentRound) + ": " + p1Name + " vs " + p2Name;

        List<Card> deck1 = new ArrayList<>(draftData.builtDecks.get(player1Id));
        List<Card> deck2 = new ArrayList<>(draftData.builtDecks.get(player2Id));

        GameData gameData = createDraftGame(gameName, player1Id, p1Name, player2Id, p2Name, deck1, deck2);
        gameData.draftId = draftData.id;

        draftData.activeGameForPlayer.put(player1Id, gameData.id);
        draftData.activeGameForPlayer.put(player2Id, gameData.id);
        draftData.tournamentGameIds.add(gameData.id);

        log.info("Draft {} - Tournament game {} created: {} vs {}", draftData.id, gameData.id, p1Name, p2Name);

        // For AI players, register AiConnection + AiDecisionEngine
        boolean p1IsAi = draftData.aiPlayerIds.contains(player1Id);
        boolean p2IsAi = draftData.aiPlayerIds.contains(player2Id);

        if (p1IsAi) {
            registerAiForTournamentGame(gameData, player1Id, p1Name);
        }
        if (p2IsAi) {
            registerAiForTournamentGame(gameData, player2Id, p2Name);
        }

        // Send TOURNAMENT_GAME_READY + GAME_JOINED to human players
        if (!p1IsAi) {
            sessionManager.sendToPlayer(player1Id, new TournamentGameReadyMessage(gameData.id, p2Name));
            JoinGame joinGame = gameService.getJoinGame(gameData, player1Id);
            sessionManager.sendToPlayer(player1Id, new JoinGameMessage(MessageType.GAME_JOINED, joinGame));
        }
        if (!p2IsAi) {
            sessionManager.sendToPlayer(player2Id, new TournamentGameReadyMessage(gameData.id, p1Name));
            JoinGame joinGame = gameService.getJoinGame(gameData, player2Id);
            sessionManager.sendToPlayer(player2Id, new JoinGameMessage(MessageType.GAME_JOINED, joinGame));
        }
    }

    private void registerAiForTournamentGame(GameData gameData, UUID aiPlayerId, String aiName) {
        Player aiPlayer = new Player(aiPlayerId, aiName);
        AiDecisionEngine engine = new AiDecisionEngine(gameData.id, aiPlayer, gameRegistry, gameService);
        String connectionId = "ai-draft-" + gameData.id + "-" + aiPlayerId;
        AiConnection aiConnection = new AiConnection(connectionId, engine, objectMapper);

        webSocketSessionManager.registerPlayer(aiConnection, aiPlayerId, aiName);
        webSocketSessionManager.setInGame(connectionId);

        // Schedule the AI's initial mulligan decision
        aiConnection.scheduleInitialAction(() -> engine.handleInitialMulligan(gameData));
    }

    private GameData createDraftGame(String gameName, UUID p1Id, String p1Name,
                                      UUID p2Id, String p2Name,
                                      List<Card> deck1, List<Card> deck2) {
        UUID gameId = UUID.randomUUID();
        GameData gameData = new GameData(gameId, gameName, p1Id, p1Name);

        // Add both players
        gameData.playerIds.add(p1Id);
        gameData.playerIds.add(p2Id);
        gameData.orderedPlayerIds.add(p1Id);
        gameData.orderedPlayerIds.add(p2Id);
        gameData.playerNames.add(p1Name);
        gameData.playerNames.add(p2Name);
        gameData.playerIdToName.put(p1Id, p1Name);
        gameData.playerIdToName.put(p2Id, p2Name);

        // Initialize decks
        Collections.shuffle(deck1, random);
        Collections.shuffle(deck2, random);

        initializePlayerForDraftGame(gameData, p1Id, deck1);
        initializePlayerForDraftGame(gameData, p2Id, deck2);

        gameData.status = GameStatus.MULLIGAN;

        gameData.gameLog.add("Tournament game started!");
        gameData.gameLog.add(p1Name + " vs " + p2Name);

        // Randomly pick starting player
        UUID startingPlayerId = random.nextBoolean() ? p1Id : p2Id;
        String startingPlayerName = gameData.playerIdToName.get(startingPlayerId);
        gameData.startingPlayerId = startingPlayerId;

        gameData.gameLog.add(startingPlayerName + " wins the coin toss and goes first!");
        gameData.gameLog.add("Mulligan phase — decide to keep or mulligan.");

        gameRegistry.register(gameData);

        log.info("Draft game {} created and registered", gameId);
        return gameData;
    }

    private void initializePlayerForDraftGame(GameData gameData, UUID playerId, List<Card> deck) {
        gameData.playerDecks.put(playerId, deck);
        gameData.mulliganCounts.put(playerId, 0);
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
        gameData.playerGraveyards.put(playerId, new ArrayList<>());
        gameData.playerExiledCards.put(playerId, new ArrayList<>());
        gameData.playerManaPools.put(playerId, new ManaPool());
        gameData.playerLifeTotals.put(playerId, 20);

        List<Card> hand = new ArrayList<>(deck.subList(0, Math.min(7, deck.size())));
        deck.subList(0, Math.min(7, deck.size())).clear();
        gameData.playerHands.put(playerId, hand);

        Set<TurnStep> defaultStops = ConcurrentHashMap.newKeySet();
        defaultStops.add(TurnStep.PRECOMBAT_MAIN);
        defaultStops.add(TurnStep.POSTCOMBAT_MAIN);
        gameData.playerAutoStopSteps.put(playerId, defaultStops);
    }

    // ===== Game Finished Hook =====

    public void handleGameFinished(DraftData draftData, UUID winnerId) {
        synchronized (draftData) {
            if (draftData.status != DraftStatus.TOURNAMENT) return;

            draftData.roundWinners.add(winnerId);

            if (draftData.currentRound >= draftData.tournamentRounds.size()) {
                log.warn("Draft {} - Invalid round index {}", draftData.id, draftData.currentRound);
                return;
            }

            List<List<UUID>> currentPairings = draftData.tournamentRounds.get(draftData.currentRound);
            int completedGames = 0;
            for (List<UUID> pairing : currentPairings) {
                UUID p1 = pairing.get(0);
                UUID p2 = pairing.get(1);
                if (draftData.roundWinners.contains(p1) || draftData.roundWinners.contains(p2)) {
                    completedGames++;
                }
            }

            log.info("Draft {} - Round {} game completed. Winner: {}. Completed: {}/{}",
                    draftData.id, draftData.currentRound + 1,
                    draftData.playerNames.get(winnerId), completedGames, currentPairings.size());

            // Update bracket for humans
            broadcastTournamentUpdate(draftData);

            if (completedGames >= currentPairings.size()) {
                advanceTournamentRound(draftData);
            }
        }
    }

    private void advanceTournamentRound(DraftData draftData) {
        List<UUID> winners = new ArrayList<>(draftData.roundWinners);

        if (winners.size() <= 1) {
            // Tournament finished
            draftData.status = DraftStatus.FINISHED;
            draftData.tournamentWinnerId = winners.isEmpty() ? null : winners.get(0);

            String winnerName = draftData.tournamentWinnerId != null
                    ? draftData.playerNames.get(draftData.tournamentWinnerId) : "Unknown";

            log.info("Draft {} - Tournament finished! Winner: {}", draftData.id, winnerName);

            // Broadcast final result
            for (UUID playerId : draftData.playerIds) {
                if (!draftData.aiPlayerIds.contains(playerId)) {
                    sessionManager.sendToPlayer(playerId, new DraftFinishedMessage(winnerName));
                }
            }

            // Cleanup
            aiDraftEngines.keySet().removeAll(draftData.aiPlayerIds);
            return;
        }

        // Next round
        draftData.currentRound++;
        draftData.roundWinners.clear();
        draftData.activeGameForPlayer.clear();

        // Create new pairings from winners
        List<List<UUID>> pairings = new ArrayList<>();
        for (int i = 0; i < winners.size(); i += 2) {
            if (i + 1 < winners.size()) {
                pairings.add(List.of(winners.get(i), winners.get(i + 1)));
            } else {
                // Bye (odd number, shouldn't happen in 8-player draft)
                draftData.roundWinners.add(winners.get(i));
            }
        }
        draftData.tournamentRounds.add(pairings);

        log.info("Draft {} - Round {} started with {} pairings", draftData.id,
                draftData.currentRound + 1, pairings.size());

        broadcastTournamentUpdate(draftData);

        for (List<UUID> pairing : pairings) {
            startTournamentGame(draftData, pairing.get(0), pairing.get(1));
        }
    }

    // ===== Broadcasting =====

    private void broadcastTournamentUpdate(DraftData draftData) {
        List<TournamentUpdateMessage.TournamentRound> rounds = new ArrayList<>();
        for (int r = 0; r < draftData.tournamentRounds.size(); r++) {
            List<TournamentUpdateMessage.TournamentPairing> pairingViews = new ArrayList<>();
            for (List<UUID> pairing : draftData.tournamentRounds.get(r)) {
                String p1Name = draftData.playerNames.get(pairing.get(0));
                String p2Name = draftData.playerNames.get(pairing.get(1));
                String winnerName = null;
                if (draftData.roundWinners.contains(pairing.get(0))) {
                    winnerName = p1Name;
                } else if (draftData.roundWinners.contains(pairing.get(1))) {
                    winnerName = p2Name;
                }
                pairingViews.add(new TournamentUpdateMessage.TournamentPairing(p1Name, p2Name, winnerName));
            }
            rounds.add(new TournamentUpdateMessage.TournamentRound(getRoundName(r), pairingViews));
        }

        String roundName = getRoundName(draftData.currentRound);
        TournamentUpdateMessage msg = new TournamentUpdateMessage(rounds, draftData.currentRound, roundName);

        for (UUID playerId : draftData.playerIds) {
            if (!draftData.aiPlayerIds.contains(playerId)) {
                sessionManager.sendToPlayer(playerId, msg);
            }
        }
    }

    public void sendDraftJoined(DraftData draftData, UUID playerId) {
        List<String> names = draftData.playerIds.stream()
                .map(id -> draftData.playerNames.get(id))
                .toList();
        sessionManager.sendToPlayer(playerId, new DraftJoinedMessage(
                draftData.id, draftData.draftName, draftData.setCode, names, draftData.status.name()));
    }

    // ===== Rejoin =====

    public void resendDraftState(DraftData draftData, UUID playerId) {
        synchronized (draftData) {
            // Always send DRAFT_JOINED first so the frontend knows draft info
            sendDraftJoined(draftData, playerId);

            switch (draftData.status) {
                case DRAFTING -> {
                    List<Card> pool = draftData.draftPools.get(playerId);
                    List<CardView> poolViews = pool != null
                            ? pool.stream().map(cardViewFactory::create).toList()
                            : List.of();

                    if (draftData.playersPickedThisRound.contains(playerId)) {
                        // Player already picked this round — send empty pack (waiting for others)
                        sessionManager.sendToPlayer(playerId, new DraftPackUpdateMessage(
                                List.of(), draftData.currentPackNumber, draftData.currentPickNumber, poolViews));
                    } else {
                        // Player hasn't picked yet — resend current pack
                        List<Card> pack = draftData.currentPacks.get(playerId);
                        if (pack != null) {
                            List<CardView> packViews = pack.stream().map(cardViewFactory::create).toList();
                            sessionManager.sendToPlayer(playerId, new DraftPackUpdateMessage(
                                    packViews, draftData.currentPackNumber, draftData.currentPickNumber, poolViews));
                        }
                    }
                }
                case DECK_BUILDING -> {
                    List<Card> pool = draftData.draftPools.get(playerId);
                    if (pool != null) {
                        boolean alreadySubmitted = draftData.deckSubmitted.contains(playerId);
                        List<CardView> poolViews = pool.stream().map(cardViewFactory::create).toList();
                        sessionManager.sendToPlayer(playerId, new DeckBuildingStateMessage(poolViews, draftData.deckBuildingDeadline, alreadySubmitted));
                    }
                }
                case TOURNAMENT -> {
                    // Resend bracket
                    broadcastTournamentUpdateToPlayer(draftData, playerId);

                    // If player has an active tournament game, resend game state
                    UUID activeGameId = draftData.activeGameForPlayer.get(playerId);
                    if (activeGameId != null) {
                        GameData gameData = gameRegistry.get(activeGameId);
                        if (gameData != null && gameData.status != GameStatus.FINISHED) {
                            String opponentName = getOpponentName(draftData, playerId, activeGameId);
                            sessionManager.sendToPlayer(playerId, new TournamentGameReadyMessage(gameData.id, opponentName));
                            JoinGame joinGame = gameService.getJoinGame(gameData, playerId);
                            sessionManager.sendToPlayer(playerId, new JoinGameMessage(MessageType.GAME_JOINED, joinGame));
                        }
                    }
                }
                case FINISHED -> {
                    String winnerName = draftData.tournamentWinnerId != null
                            ? draftData.playerNames.get(draftData.tournamentWinnerId) : "Unknown";
                    sessionManager.sendToPlayer(playerId, new DraftFinishedMessage(winnerName));
                }
                default -> {
                    // WAITING - just the DRAFT_JOINED is enough
                }
            }
        }
    }

    private void broadcastTournamentUpdateToPlayer(DraftData draftData, UUID playerId) {
        List<TournamentUpdateMessage.TournamentRound> rounds = new ArrayList<>();
        for (int r = 0; r < draftData.tournamentRounds.size(); r++) {
            List<TournamentUpdateMessage.TournamentPairing> pairingViews = new ArrayList<>();
            for (List<UUID> pairing : draftData.tournamentRounds.get(r)) {
                String p1Name = draftData.playerNames.get(pairing.get(0));
                String p2Name = draftData.playerNames.get(pairing.get(1));
                String winnerName = null;
                if (draftData.roundWinners.contains(pairing.get(0))) {
                    winnerName = p1Name;
                } else if (draftData.roundWinners.contains(pairing.get(1))) {
                    winnerName = p2Name;
                }
                pairingViews.add(new TournamentUpdateMessage.TournamentPairing(p1Name, p2Name, winnerName));
            }
            rounds.add(new TournamentUpdateMessage.TournamentRound(getRoundName(r), pairingViews));
        }

        String roundName = getRoundName(draftData.currentRound);
        TournamentUpdateMessage msg = new TournamentUpdateMessage(rounds, draftData.currentRound, roundName);
        sessionManager.sendToPlayer(playerId, msg);
    }

    private String getOpponentName(DraftData draftData, UUID playerId, UUID gameId) {
        for (List<UUID> pairing : draftData.tournamentRounds.get(draftData.currentRound)) {
            if (pairing.contains(playerId)) {
                UUID opponentId = pairing.get(0).equals(playerId) ? pairing.get(1) : pairing.get(0);
                return draftData.playerNames.get(opponentId);
            }
        }
        return "Unknown";
    }

    private String getRoundName(int roundIndex) {
        return switch (roundIndex) {
            case 0 -> "Quarterfinals";
            case 1 -> "Semifinals";
            case 2 -> "Finals";
            default -> "Round " + (roundIndex + 1);
        };
    }
}
