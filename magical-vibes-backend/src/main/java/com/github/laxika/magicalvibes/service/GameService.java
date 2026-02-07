package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.GameLogEntryMessage;
import com.github.laxika.magicalvibes.dto.GameStartedMessage;
import com.github.laxika.magicalvibes.dto.HandDrawnMessage;
import com.github.laxika.magicalvibes.dto.JoinGame;
import com.github.laxika.magicalvibes.dto.LobbyGame;
import com.github.laxika.magicalvibes.dto.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.dto.PriorityUpdatedMessage;
import com.github.laxika.magicalvibes.dto.StepAdvancedMessage;
import com.github.laxika.magicalvibes.dto.TurnChangedMessage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class GameService {

    public record GameResult(JoinGame joinGame, LobbyGame lobbyGame) {}

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, GameData> games = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public GameService(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
    }

    public GameResult createGame(String gameName, Player player) {
        long gameId = idCounter.getAndIncrement();

        GameData gameData = new GameData(gameId, gameName, player.getId(), player.getUsername());
        gameData.playerIds.add(player.getId());
        gameData.orderedPlayerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        gameData.playerIdToName.put(player.getId(), player.getUsername());
        games.put(gameId, gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, player.getUsername());
        return new GameResult(toJoinGame(gameData), toLobbyGame(gameData));
    }

    public List<LobbyGame> listRunningGames() {
        return games.values().stream()
                .filter(g -> g.status != GameStatus.FINISHED)
                .map(this::toLobbyGame)
                .toList();
    }

    public LobbyGame joinGame(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (gameData.status != GameStatus.WAITING) {
            throw new IllegalStateException("Game is not accepting players");
        }

        if (gameData.playerIds.contains(player.getId())) {
            throw new IllegalStateException("You are already in this game");
        }

        gameData.playerIds.add(player.getId());
        gameData.orderedPlayerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        gameData.playerIdToName.put(player.getId(), player.getUsername());

        if (gameData.playerIds.size() >= 2) {
            initializeGame(gameData);
        }

        log.info("User {} joined game {}, status={}", player.getUsername(), gameId, gameData.status);
        return toLobbyGame(gameData);
    }

    public Long getCreatorUserId(Long gameId) {
        GameData gameData = games.get(gameId);
        return gameData != null ? gameData.createdByUserId : null;
    }

    private void initializeGame(GameData gameData) {
        Card forest = new Card("Forest", "Basic Land", "Forest", "G");

        for (Long playerId : gameData.playerIds) {
            List<Card> deck = IntStream.range(0, 60)
                    .mapToObj(i -> forest)
                    .collect(Collectors.toList());
            Collections.shuffle(deck, random);
            gameData.playerDecks.put(playerId, deck);
            gameData.mulliganCounts.put(playerId, 0);

            List<Card> hand = new ArrayList<>(deck.subList(0, 7));
            deck.subList(0, 7).clear();
            gameData.playerHands.put(playerId, hand);
        }

        gameData.status = GameStatus.MULLIGAN;

        gameData.gameLog.add("Game started!");
        gameData.gameLog.add("Each player receives a deck of 60 Forests.");

        List<Long> ids = new ArrayList<>(gameData.orderedPlayerIds);
        Long startingPlayerId = ids.get(random.nextInt(ids.size()));
        String startingPlayerName = gameData.playerIdToName.get(startingPlayerId);
        gameData.startingPlayerId = startingPlayerId;

        gameData.gameLog.add(startingPlayerName + " wins the coin toss and goes first!");
        gameData.gameLog.add("Mulligan phase — decide to keep or mulligan.");

        log.info("Game {} - Mulligan phase begins. Starting player: {}", gameData.id, startingPlayerName);
    }

    public void passPriority(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameId, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep(gameData);
            } else {
                broadcastToGame(gameData, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }
        }
    }

    private void advanceStep(GameData gameData) {
        gameData.priorityPassedBy.clear();
        TurnStep next = gameData.currentStep.next();

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameData.gameLog.add(logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);

            broadcastLogEntry(gameData, logEntry);
            broadcastToGame(gameData, new StepAdvancedMessage(getPriorityPlayerId(gameData), next));
        } else {
            advanceTurn(gameData);
        }
    }

    private void advanceTurn(GameData gameData) {
        List<Long> ids = new ArrayList<>(gameData.orderedPlayerIds);
        Long currentActive = gameData.activePlayerId;
        Long nextActive = ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
        String nextActiveName = gameData.playerIdToName.get(nextActive);

        gameData.activePlayerId = nextActive;
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.priorityPassedBy.clear();

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        gameData.gameLog.add(logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);

        broadcastLogEntry(gameData, logEntry);
        broadcastToGame(gameData, new TurnChangedMessage(
                getPriorityPlayerId(gameData), TurnStep.first(), nextActive, gameData.turnNumber
        ));
    }

    public Long getGameIdForPlayer(Long userId) {
        return games.entrySet().stream()
                .filter(e -> e.getValue().playerIds.contains(userId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void broadcastToGame(GameData gameData, Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message", e);
            return;
        }
        for (Long playerId : gameData.orderedPlayerIds) {
            Player player = sessionManager.getPlayerByUserId(playerId);
            if (player != null && player.getSession().isOpen()) {
                try {
                    player.getSession().sendMessage(new TextMessage(json));
                } catch (Exception e) {
                    log.error("Error sending message to player {}", playerId, e);
                }
            }
        }
    }

    private void broadcastLogEntry(GameData gameData, String logEntry) {
        broadcastToGame(gameData, new GameLogEntryMessage(logEntry));
    }

    private Long getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<Long> ids = new ArrayList<>(data.orderedPlayerIds);
        Long nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    public JoinGame getJoinGame(Long gameId, Long playerId) {
        GameData data = games.get(gameId);
        if (data == null) {
            throw new IllegalArgumentException("Game not found");
        }
        return toJoinGame(data, playerId);
    }

    public void keepHand(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.MULLIGAN) {
            throw new IllegalStateException("Game is not in mulligan phase");
        }
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }

        synchronized (gameData) {
            gameData.playerKeptHand.add(player.getId());
            int mulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
            List<Card> hand = gameData.playerHands.get(player.getId());
            List<Card> deck = gameData.playerDecks.get(player.getId());

            if (mulliganCount > 0 && !hand.isEmpty()) {
                int cardsToBottom = Math.min(mulliganCount, hand.size());
                List<Card> bottomCards = new ArrayList<>(hand.subList(hand.size() - cardsToBottom, hand.size()));
                hand.subList(hand.size() - cardsToBottom, hand.size()).clear();
                deck.addAll(bottomCards);
            }

            sendToPlayer(player.getId(), new HandDrawnMessage(new ArrayList<>(hand), mulliganCount));
            broadcastToGame(gameData, new MulliganResolvedMessage(player.getUsername(), true, mulliganCount));

            String logEntry = player.getUsername() + " keeps their hand" +
                    (mulliganCount > 0 ? " (mulliganed " + mulliganCount + " time" + (mulliganCount > 1 ? "s" : "") + ", keeping " + hand.size() + " cards)." : ".");
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} kept hand (mulligan count: {}, hand size: {})", gameId, player.getUsername(), mulliganCount, hand.size());

            if (gameData.playerKeptHand.size() >= 2) {
                startGame(gameData);
            }
        }
    }

    public void mulligan(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.MULLIGAN) {
            throw new IllegalStateException("Game is not in mulligan phase");
        }
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }
        int currentMulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
        if (currentMulliganCount >= 7) {
            throw new IllegalStateException("Maximum mulligans reached");
        }

        synchronized (gameData) {
            List<Card> hand = gameData.playerHands.get(player.getId());
            List<Card> deck = gameData.playerDecks.get(player.getId());

            deck.addAll(hand);
            hand.clear();
            Collections.shuffle(deck, random);

            List<Card> newHand = new ArrayList<>(deck.subList(0, 7));
            deck.subList(0, 7).clear();
            gameData.playerHands.put(player.getId(), newHand);

            int newMulliganCount = currentMulliganCount + 1;
            gameData.mulliganCounts.put(player.getId(), newMulliganCount);

            sendToPlayer(player.getId(), new HandDrawnMessage(new ArrayList<>(newHand), newMulliganCount));
            broadcastToGame(gameData, new MulliganResolvedMessage(player.getUsername(), false, newMulliganCount));

            String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} mulliganed (count: {})", gameId, player.getUsername(), newMulliganCount);
        }
    }

    private void startGame(GameData gameData) {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        gameData.gameLog.add(logEntry1);
        gameData.gameLog.add(logEntry2);
        broadcastLogEntry(gameData, logEntry1);
        broadcastLogEntry(gameData, logEntry2);

        broadcastToGame(gameData, new GameStartedMessage(
                gameData.activePlayerId, gameData.turnNumber, gameData.currentStep, getPriorityPlayerId(gameData)
        ));

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));
    }

    private void sendToPlayer(Long playerId, Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message", e);
            return;
        }
        Player player = sessionManager.getPlayerByUserId(playerId);
        if (player != null && player.getSession().isOpen()) {
            try {
                player.getSession().sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("Error sending message to player {}", playerId, e);
            }
        }
    }

    private JoinGame toJoinGame(GameData data) {
        return toJoinGame(data, null);
    }

    private JoinGame toJoinGame(GameData data, Long playerId) {
        List<Card> hand = playerId != null ? new ArrayList<>(data.playerHands.getOrDefault(playerId, List.of())) : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        return new JoinGame(
                data.id,
                data.gameName,
                data.status,
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.orderedPlayerIds),
                new ArrayList<>(data.gameLog),
                data.currentStep,
                data.activePlayerId,
                data.turnNumber,
                getPriorityPlayerId(data),
                hand,
                mulliganCount
        );
    }

    private LobbyGame toLobbyGame(GameData data) {
        return new LobbyGame(
                data.id,
                data.gameName,
                data.createdByUsername,
                data.playerIds.size(),
                data.status
        );
    }

    private static class GameData {
        final long id;
        final String gameName;
        final long createdByUserId;
        final String createdByUsername;
        final LocalDateTime createdAt;
        GameStatus status;
        final Set<Long> playerIds = ConcurrentHashMap.newKeySet();
        final List<Long> orderedPlayerIds = Collections.synchronizedList(new ArrayList<>());
        final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
        final Map<Long, String> playerIdToName = new ConcurrentHashMap<>();
        final Map<Long, List<Card>> playerDecks = new ConcurrentHashMap<>();
        final Map<Long, List<Card>> playerHands = new ConcurrentHashMap<>();
        final Map<Long, Integer> mulliganCounts = new ConcurrentHashMap<>();
        final Set<Long> playerKeptHand = ConcurrentHashMap.newKeySet();
        final List<String> gameLog = Collections.synchronizedList(new ArrayList<>());
        Long startingPlayerId;
        TurnStep currentStep;
        Long activePlayerId;
        int turnNumber;
        final Set<Long> priorityPassedBy = ConcurrentHashMap.newKeySet();

        GameData(long id, String gameName, long createdByUserId, String createdByUsername) {
            this.id = id;
            this.gameName = gameName;
            this.createdByUserId = createdByUserId;
            this.createdByUsername = createdByUsername;
            this.createdAt = LocalDateTime.now();
            this.status = GameStatus.WAITING;
        }
    }
}
