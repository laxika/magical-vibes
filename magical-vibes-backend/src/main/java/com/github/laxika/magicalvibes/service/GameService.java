package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.dto.AutoStopsUpdatedMessage;
import com.github.laxika.magicalvibes.dto.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.dto.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.dto.BattlefieldUpdatedMessage;
import com.github.laxika.magicalvibes.dto.DeckSizesUpdatedMessage;
import com.github.laxika.magicalvibes.dto.GameLogEntryMessage;
import com.github.laxika.magicalvibes.dto.GameOverMessage;
import com.github.laxika.magicalvibes.dto.GameStartedMessage;
import com.github.laxika.magicalvibes.dto.HandDrawnMessage;
import com.github.laxika.magicalvibes.dto.JoinGame;
import com.github.laxika.magicalvibes.dto.LifeUpdatedMessage;
import com.github.laxika.magicalvibes.dto.LobbyGame;
import com.github.laxika.magicalvibes.dto.ManaUpdatedMessage;
import com.github.laxika.magicalvibes.dto.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.dto.PlayableCardsMessage;
import com.github.laxika.magicalvibes.dto.PriorityUpdatedMessage;
import com.github.laxika.magicalvibes.dto.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.dto.StepAdvancedMessage;
import com.github.laxika.magicalvibes.dto.TurnChangedMessage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
        Card forest = new Forest();
        Card llanowarElves = new LlanowarElves();
        Card grizzlyBears = new GrizzlyBears();

        for (Long playerId : gameData.playerIds) {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                deck.add(forest);
            }
            for (int i = 0; i < 4; i++) {
                deck.add(llanowarElves);
            }
            for (int i = 0; i < 32; i++) {
                deck.add(grizzlyBears);
            }
            Collections.shuffle(deck, random);
            gameData.playerDecks.put(playerId, deck);
            gameData.mulliganCounts.put(playerId, 0);
            gameData.playerBattlefields.put(playerId, new ArrayList<>());
            gameData.playerManaPools.put(playerId, new ManaPool());
            gameData.playerLifeTotals.put(playerId, 20);

            List<Card> hand = new ArrayList<>(deck.subList(0, 7));
            deck.subList(0, 7).clear();
            gameData.playerHands.put(playerId, hand);

            Set<TurnStep> defaultStops = ConcurrentHashMap.newKeySet();
            defaultStops.add(TurnStep.PRECOMBAT_MAIN);
            defaultStops.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(playerId, defaultStops);
        }

        gameData.status = GameStatus.MULLIGAN;

        gameData.gameLog.add("Game started!");
        gameData.gameLog.add("Each player receives a deck of 24 Forests, 4 Llanowar Elves, and 32 Grizzly Bears.");

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

            resolveAutoPass(gameData);
        }
    }

    private void advanceStep(GameData gameData) {
        gameData.priorityPassedBy.clear();
        TurnStep next = gameData.currentStep.next();

        drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameData.gameLog.add(logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);

            broadcastLogEntry(gameData, logEntry);
            broadcastToGame(gameData, new StepAdvancedMessage(getPriorityPlayerId(gameData), next));

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.DRAW) {
                handleDrawStep(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                handleDeclareAttackersStep(gameData);
                return;
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleDeclareBlockersStep(gameData);
                return;
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                resolveCombatDamage(gameData);
                return;
            } else if (next == TurnStep.END_OF_COMBAT) {
                clearCombatState(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    private void handleDrawStep(GameData gameData) {
        Long activeId = gameData.activePlayerId;

        // The starting player skips their draw on turn 1
        if (gameData.turnNumber == 1 && activeId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activeId) + " skips the draw (first turn).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activeId));
            return;
        }

        List<Card> deck = gameData.playerDecks.get(activeId);
        List<Card> hand = gameData.playerHands.get(activeId);

        if (deck == null || deck.isEmpty()) {
            log.warn("Game {} - {} has no cards to draw", gameData.id, gameData.playerIdToName.get(activeId));
            return;
        }

        Card drawn = deck.remove(0);
        hand.add(drawn);

        sendToPlayer(activeId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(activeId, 0)));
        broadcastDeckSizes(gameData);

        String playerName = gameData.playerIdToName.get(activeId);
        String logEntry = playerName + " draws a card.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);

        log.info("Game {} - {} draws a card (hand: {}, deck: {})", gameData.id, playerName, hand.size(), deck.size());
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
        gameData.landsPlayedThisTurn.clear();

        drainManaPools(gameData);

        // Untap all permanents for the new active player
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(Permanent::untap);
            battlefield.forEach(p -> p.setSummoningSick(false));
        }
        broadcastBattlefields(gameData);

        String untapLog = nextActiveName + " untaps their permanents.";
        gameData.gameLog.add(untapLog);
        broadcastLogEntry(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

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

        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            if (gameData.playerKeptHand.contains(player.getId())) {
                throw new IllegalStateException("You have already kept your hand");
            }

            gameData.playerKeptHand.add(player.getId());
            int mulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
            List<Card> hand = gameData.playerHands.get(player.getId());

            broadcastToGame(gameData, new MulliganResolvedMessage(player.getUsername(), true, mulliganCount));

            if (mulliganCount > 0 && !hand.isEmpty()) {
                int cardsToBottom = Math.min(mulliganCount, hand.size());
                gameData.playerNeedsToBottom.put(player.getId(), cardsToBottom);
                sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));

                String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                        " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameId, player.getUsername(), cardsToBottom, mulliganCount);
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} kept hand (no mulligans)", gameId, player.getUsername());

                checkStartGame(gameData);
            }
        }
    }

    public void bottomCards(Long gameId, Player player, List<Integer> cardIndices) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            Integer neededCount = gameData.playerNeedsToBottom.get(player.getId());
            if (neededCount == null) {
                throw new IllegalStateException("You don't need to put cards on the bottom");
            }
            if (cardIndices.size() != neededCount) {
                throw new IllegalStateException("You must select exactly " + neededCount + " card(s) to put on the bottom");
            }

            List<Card> hand = gameData.playerHands.get(player.getId());
            List<Card> deck = gameData.playerDecks.get(player.getId());

            Set<Integer> uniqueIndices = new HashSet<>(cardIndices);
            if (uniqueIndices.size() != cardIndices.size()) {
                throw new IllegalStateException("Duplicate card indices are not allowed");
            }
            for (int idx : cardIndices) {
                if (idx < 0 || idx >= hand.size()) {
                    throw new IllegalStateException("Invalid card index: " + idx);
                }
            }

            // Sort indices descending so removal doesn't shift earlier indices
            List<Integer> sorted = new ArrayList<>(cardIndices);
            sorted.sort(Collections.reverseOrder());
            List<Card> bottomCards = new ArrayList<>();
            for (int idx : sorted) {
                bottomCards.add(hand.remove(idx));
            }
            deck.addAll(bottomCards);

            gameData.playerNeedsToBottom.remove(player.getId());

            sendToPlayer(player.getId(), new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(player.getId(), 0)));

            String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                    " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} bottomed {} cards, hand size now {}", gameId, player.getUsername(), bottomCards.size(), hand.size());

            broadcastDeckSizes(gameData);
            checkStartGame(gameData);
        }
    }

    private void checkStartGame(GameData gameData) {
        if (gameData.playerKeptHand.size() >= 2 && gameData.playerNeedsToBottom.isEmpty()) {
            startGame(gameData);
        }
    }

    public void mulligan(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        synchronized (gameData) {
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

        broadcastLifeTotals(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        resolveAutoPass(gameData);
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
        Map<String, Integer> manaPool = getManaPool(data, playerId);
        List<TurnStep> autoStopSteps = playerId != null && data.playerAutoStopSteps.containsKey(playerId)
                ? new ArrayList<>(data.playerAutoStopSteps.get(playerId))
                : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
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
                mulliganCount,
                getDeckSizes(data),
                getBattlefields(data),
                manaPool,
                autoStopSteps,
                getLifeTotals(data)
        );
    }

    private List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (Long pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    private void broadcastDeckSizes(GameData data) {
        broadcastToGame(data, new DeckSizesUpdatedMessage(getDeckSizes(data)));
    }

    private List<List<Permanent>> getBattlefields(GameData data) {
        List<List<Permanent>> battlefields = new ArrayList<>();
        for (Long pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            battlefields.add(bf != null ? new ArrayList<>(bf) : new ArrayList<>());
        }
        return battlefields;
    }

    private void broadcastBattlefields(GameData data) {
        broadcastToGame(data, new BattlefieldUpdatedMessage(getBattlefields(data)));
    }

    public void playCard(Long gameId, Player player, int cardIndex) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Long playerId = player.getId();
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
            if (!playable.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable");
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);
            gameData.playerBattlefields.get(playerId).add(new Permanent(card));

            if (card.getType() == CardType.BASIC_LAND) {
                gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
            } else if (card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                cost.pay(pool);
                sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
            }

            sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
            broadcastBattlefields(gameData);

            String logEntry = player.getUsername() + " plays " + card.getName() + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} plays {}", gameId, player.getUsername(), card.getName());

            resolveAutoPass(gameData);
        }
    }

    public void tapPermanent(Long gameId, Player player, int permanentIndex) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Long playerId = player.getId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                throw new IllegalStateException("Invalid permanent index");
            }

            Permanent permanent = battlefield.get(permanentIndex);
            if (permanent.isTapped()) {
                throw new IllegalStateException("Permanent is already tapped");
            }
            if (permanent.getCard().getOnTapEffects() == null || permanent.getCard().getOnTapEffects().isEmpty()) {
                throw new IllegalStateException("Permanent has no tap effects");
            }
            if (permanent.isSummoningSick() && permanent.getCard().getType() == CardType.CREATURE) {
                throw new IllegalStateException("Creature has summoning sickness");
            }

            permanent.tap();

            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            for (CardEffect effect : permanent.getCard().getOnTapEffects()) {
                if (effect instanceof AwardManaEffect awardMana) {
                    manaPool.add(awardMana.color());
                }
            }

            broadcastBattlefields(gameData);
            sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));

            String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} taps {}", gameId, player.getUsername(), permanent.getCard().getName());

            broadcastPlayableCards(gameData);
        }
    }

    public void setAutoStops(Long gameId, Player player, List<TurnStep> stops) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            sendToPlayer(player.getId(), new AutoStopsUpdatedMessage(new ArrayList<>(stopSet)));
        }
    }

    // ===== Combat methods =====

    private List<Integer> getAttackableCreatureIndices(GameData gameData, Long playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (p.getCard().getType() == CardType.CREATURE && !p.isTapped() && !p.isSummoningSick()) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getBlockableCreatureIndices(GameData gameData, Long playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (p.getCard().getType() == CardType.CREATURE && !p.isTapped()) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getAttackingCreatureIndices(GameData gameData, Long playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).isAttacking()) {
                indices.add(i);
            }
        }
        return indices;
    }

    private Long getOpponentId(GameData gameData, Long playerId) {
        List<Long> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    private List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (Long pid : gameData.orderedPlayerIds) {
            totals.add(gameData.playerLifeTotals.getOrDefault(pid, 20));
        }
        return totals;
    }

    private void broadcastLifeTotals(GameData gameData) {
        broadcastToGame(gameData, new LifeUpdatedMessage(getLifeTotals(gameData)));
    }

    private void handleDeclareAttackersStep(GameData gameData) {
        Long activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            skipToEndOfCombat(gameData);
            return;
        }

        gameData.awaitingAttackerDeclaration = true;
        sendToPlayer(activeId, new AvailableAttackersMessage(attackable));
    }

    private void skipToEndOfCombat(GameData gameData) {
        gameData.currentStep = TurnStep.END_OF_COMBAT;
        clearCombatState(gameData);

        String logEntry = "Step: " + TurnStep.END_OF_COMBAT.getDisplayName();
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastToGame(gameData, new StepAdvancedMessage(getPriorityPlayerId(gameData), TurnStep.END_OF_COMBAT));
    }

    public void declareAttackers(Long gameId, Player player, List<Integer> attackerIndices) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        synchronized (gameData) {
            if (!gameData.awaitingAttackerDeclaration) {
                throw new IllegalStateException("Not awaiting attacker declaration");
            }
            if (!player.getId().equals(gameData.activePlayerId)) {
                throw new IllegalStateException("Only the active player can declare attackers");
            }

            Long playerId = player.getId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Integer> attackable = getAttackableCreatureIndices(gameData, playerId);

            // Validate indices
            Set<Integer> uniqueIndices = new HashSet<>(attackerIndices);
            if (uniqueIndices.size() != attackerIndices.size()) {
                throw new IllegalStateException("Duplicate attacker indices");
            }
            for (int idx : attackerIndices) {
                if (!attackable.contains(idx)) {
                    throw new IllegalStateException("Invalid attacker index: " + idx);
                }
            }

            gameData.awaitingAttackerDeclaration = false;

            if (attackerIndices.isEmpty()) {
                log.info("Game {} - {} declares no attackers", gameData.id, player.getUsername());
                skipToEndOfCombat(gameData);
                resolveAutoPass(gameData);
                return;
            }

            // Mark creatures as attacking and tap them
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                attacker.setAttacking(true);
                attacker.tap();
            }

            String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                    " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            broadcastBattlefields(gameData);

            log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());

            advanceStep(gameData);
            resolveAutoPass(gameData);
        }
    }

    private void handleDeclareBlockersStep(GameData gameData) {
        Long activeId = gameData.activePlayerId;
        Long defenderId = getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = getAttackingCreatureIndices(gameData, activeId);

        if (blockable.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block", gameData.id);
            advanceStep(gameData);
            return;
        }

        gameData.awaitingBlockerDeclaration = true;
        sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
    }

    public void declareBlockers(Long gameId, Player player, List<int[]> blockerAssignments) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        synchronized (gameData) {
            if (!gameData.awaitingBlockerDeclaration) {
                throw new IllegalStateException("Not awaiting blocker declaration");
            }

            Long activeId = gameData.activePlayerId;
            Long defenderId = getOpponentId(gameData, activeId);

            if (!player.getId().equals(defenderId)) {
                throw new IllegalStateException("Only the defending player can declare blockers");
            }

            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
            List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
            List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

            // Validate assignments
            Set<Integer> usedBlockers = new HashSet<>();
            for (int[] assignment : blockerAssignments) {
                int blockerIdx = assignment[0];
                int attackerIdx = assignment[1];

                if (!blockable.contains(blockerIdx)) {
                    throw new IllegalStateException("Invalid blocker index: " + blockerIdx);
                }
                if (!usedBlockers.add(blockerIdx)) {
                    throw new IllegalStateException("Duplicate blocker index: " + blockerIdx);
                }
                if (attackerIdx < 0 || attackerIdx >= attackerBattlefield.size() || !attackerBattlefield.get(attackerIdx).isAttacking()) {
                    throw new IllegalStateException("Invalid attacker index: " + attackerIdx);
                }
            }

            gameData.awaitingBlockerDeclaration = false;

            // Mark creatures as blocking
            for (int[] assignment : blockerAssignments) {
                Permanent blocker = defenderBattlefield.get(assignment[0]);
                blocker.setBlocking(true);
                blocker.setBlockingTarget(assignment[1]);
            }

            if (!blockerAssignments.isEmpty()) {
                String logEntry = player.getUsername() + " declares " + blockerAssignments.size() +
                        " blocker" + (blockerAssignments.size() > 1 ? "s" : "") + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
            }

            broadcastBattlefields(gameData);

            log.info("Game {} - {} declares {} blockers", gameData.id, player.getUsername(), blockerAssignments.size());

            advanceStep(gameData);
            resolveAutoPass(gameData);
        }
    }

    private void resolveCombatDamage(GameData gameData) {
        Long activeId = gameData.activePlayerId;
        Long defenderId = getOpponentId(gameData, activeId);

        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);

        int damageToDefender = 0;
        Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
        Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

        List<Integer> attackingIndices = getAttackingCreatureIndices(gameData, activeId);

        for (int atkIdx : attackingIndices) {
            Permanent attacker = attackerBattlefield.get(atkIdx);
            int attackerPower = attacker.getCard().getPower() != null ? attacker.getCard().getPower() : 0;
            int attackerToughness = attacker.getCard().getToughness() != null ? attacker.getCard().getToughness() : 0;

            // Find all blockers targeting this attacker
            List<Integer> blockerIndices = new ArrayList<>();
            for (int i = 0; i < defenderBattlefield.size(); i++) {
                Permanent p = defenderBattlefield.get(i);
                if (p.isBlocking() && p.getBlockingTarget() == atkIdx) {
                    blockerIndices.add(i);
                }
            }

            if (blockerIndices.isEmpty()) {
                // Unblocked — damage goes to defending player
                damageToDefender += attackerPower;
            } else {
                // Blocked — auto-assign attacker's damage to blockers in order
                int remainingDamage = attackerPower;
                int totalDamageToAttacker = 0;

                for (int blockerIdx : blockerIndices) {
                    Permanent blocker = defenderBattlefield.get(blockerIdx);
                    int blockerToughness = blocker.getCard().getToughness() != null ? blocker.getCard().getToughness() : 0;
                    int blockerPower = blocker.getCard().getPower() != null ? blocker.getCard().getPower() : 0;

                    // Assign lethal damage to this blocker
                    int damageToThisBlocker = Math.min(remainingDamage, blockerToughness);
                    remainingDamage -= damageToThisBlocker;

                    // Check if blocker dies
                    if (damageToThisBlocker >= blockerToughness) {
                        deadDefenderIndices.add(blockerIdx);
                    }

                    totalDamageToAttacker += blockerPower;
                }

                // Check if attacker dies
                if (totalDamageToAttacker >= attackerToughness) {
                    deadAttackerIndices.add(atkIdx);
                }
            }
        }

        // Remove dead creatures (descending order to preserve indices)
        List<String> deadCreatureNames = new ArrayList<>();
        for (int idx : deadAttackerIndices) {
            deadCreatureNames.add(gameData.playerIdToName.get(activeId) + "'s " + attackerBattlefield.get(idx).getCard().getName());
            attackerBattlefield.remove(idx);
        }
        for (int idx : deadDefenderIndices) {
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + defenderBattlefield.get(idx).getCard().getName());
            defenderBattlefield.remove(idx);
        }

        // Apply life loss
        if (damageToDefender > 0) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 20);
            gameData.playerLifeTotals.put(defenderId, currentLife - damageToDefender);

            String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + damageToDefender + " combat damage.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        }

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        }

        broadcastBattlefields(gameData);
        broadcastLifeTotals(gameData);

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, damageToDefender, deadAttackerIndices.size() + deadDefenderIndices.size());

        // Check win condition
        if (checkWinCondition(gameData)) {
            return;
        }

        advanceStep(gameData);
        resolveAutoPass(gameData);
    }

    private boolean checkWinCondition(GameData gameData) {
        for (Long playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            if (life <= 0) {
                Long winnerId = getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                broadcastToGame(gameData, new GameOverMessage(winnerId, winnerName));

                log.info("Game {} - {} wins! {} is at {} life", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life);
                return true;
            }
        }
        return false;
    }

    private void clearCombatState(GameData gameData) {
        for (Long playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                battlefield.forEach(Permanent::clearCombatState);
            }
        }
        broadcastBattlefields(gameData);
    }

    // ===== End combat methods =====

    private void drainManaPools(GameData gameData) {
        for (Long playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
                sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));
            }
        }
    }

    private Map<String, Integer> getManaPool(GameData data, Long playerId) {
        if (playerId == null) {
            return Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
    }

    private List<Integer> getPlayableCardIndices(GameData gameData, Long playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING) {
            return playable;
        }

        Long priorityHolder = getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.BASIC_LAND && isActivePlayer && isMainPhase && landsPlayed < 1) {
                playable.add(i);
            }
            if (card.getType() == CardType.CREATURE && isActivePlayer && isMainPhase && card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (cost.canPay(pool)) {
                    playable.add(i);
                }
            }
        }

        return playable;
    }

    private void broadcastPlayableCards(GameData gameData) {
        for (Long playerId : gameData.orderedPlayerIds) {
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
            sendToPlayer(playerId, new PlayableCardsMessage(playable));
        }
    }

    private void resolveAutoPass(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.awaitingAttackerDeclaration || gameData.awaitingBlockerDeclaration) {
                broadcastPlayableCards(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            Long priorityHolder = getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep(gameData);
                continue;
            }

            List<Integer> playable = getPlayableCardIndices(gameData, priorityHolder);
            if (!playable.isEmpty()) {
                // Priority holder can act — stop and let them decide
                broadcastPlayableCards(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                broadcastPlayableCards(gameData);
                return;
            }

            // Priority holder has nothing to play — auto-pass for them
            String playerName = gameData.playerIdToName.get(priorityHolder);
            log.info("Game {} - Auto-passing priority for {} on step {} (no playable cards)",
                    gameData.id, playerName, gameData.currentStep);

            gameData.priorityPassedBy.add(priorityHolder);

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep(gameData);
            } else {
                broadcastToGame(gameData, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        broadcastPlayableCards(gameData);
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
        final Map<Long, Integer> playerNeedsToBottom = new ConcurrentHashMap<>();
        final List<String> gameLog = Collections.synchronizedList(new ArrayList<>());
        Long startingPlayerId;
        TurnStep currentStep;
        Long activePlayerId;
        int turnNumber;
        final Set<Long> priorityPassedBy = ConcurrentHashMap.newKeySet();
        final Map<Long, Integer> landsPlayedThisTurn = new ConcurrentHashMap<>();
        final Map<Long, List<Permanent>> playerBattlefields = new ConcurrentHashMap<>();
        final Map<Long, ManaPool> playerManaPools = new ConcurrentHashMap<>();
        final Map<Long, Set<TurnStep>> playerAutoStopSteps = new ConcurrentHashMap<>();
        final Map<Long, Integer> playerLifeTotals = new ConcurrentHashMap<>();
        boolean awaitingAttackerDeclaration;
        boolean awaitingBlockerDeclaration;

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
