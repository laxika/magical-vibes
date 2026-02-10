package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AutoStopsUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.BattlefieldUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.DeckSizesUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.GameLogEntryMessage;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStartedMessage;
import com.github.laxika.magicalvibes.networking.message.HandDrawnMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.LifeUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.ManaUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.PlayableCardsMessage;
import com.github.laxika.magicalvibes.networking.message.PriorityUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.message.StackUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.StepAdvancedMessage;
import com.github.laxika.magicalvibes.networking.message.TurnChangedMessage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final Random random = new Random();

    private final SessionManager sessionManager;

    public void passPriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                if (!gameData.stack.isEmpty()) {
                    resolveTopOfStack(gameData);
                } else {
                    advanceStep(gameData);
                }
            } else {
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
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
            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new StepAdvancedMessage(getPriorityPlayerId(gameData), next));

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.DRAW) {
                handleDrawStep(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleDeclareBlockersStep(gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                resolveCombatDamage(gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                clearCombatState(gameData);
            } else if (next == TurnStep.CLEANUP) {
                resetEndOfTurnModifiers(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    private void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their draw on turn 1
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        List<Card> deck = gameData.playerDecks.get(activePlayerId);
        List<Card> hand = gameData.playerHands.get(activePlayerId);

        if (deck == null || deck.isEmpty()) {
            log.warn("Game {} - {} has no cards to draw", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        sessionManager.sendToPlayer(activePlayerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(activePlayerId, 0)));
        broadcastDeckSizes(gameData);

        String playerName = gameData.playerIdToName.get(activePlayerId);
        String logEntry = playerName + " draws a card.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);

        log.info("Game {} - {} draws a card (hand: {}, deck: {})", gameData.id, playerName, hand.size(), deck.size());
    }

    private void advanceTurn(GameData gameData) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID currentActive = gameData.activePlayerId;
        UUID nextActive = ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
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
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new TurnChangedMessage(
                getPriorityPlayerId(gameData), TurnStep.first(), nextActive, gameData.turnNumber
        ));
    }

    private void broadcastLogEntry(GameData gameData, String logEntry) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameLogEntryMessage(logEntry));
    }

    private void broadcastStackUpdate(GameData gameData) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new StackUpdatedMessage(new ArrayList<>(gameData.stack)));
    }

    private void resolveTopOfStack(GameData gameData) {
        if (gameData.stack.isEmpty()) return;

        StackEntry entry = gameData.stack.remove(gameData.stack.size() - 1);
        gameData.priorityPassedBy.clear();

        if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
            broadcastBattlefields(gameData);

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

            // Check for ETB effects and push triggered abilities onto the stack
            if (card.getOnEnterBattlefieldEffects() != null && !card.getOnEnterBattlefieldEffects().isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ETB ability",
                        new ArrayList<>(card.getOnEnterBattlefieldEffects())
                ));
                String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                gameData.gameLog.add(etbLog);
                broadcastLogEntry(gameData, etbLog);
                log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
            }
        } else if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            String logEntry = entry.getDescription() + " resolves.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

            for (CardEffect effect : entry.getEffectsToResolve()) {
                if (effect instanceof OpponentMayPlayCreatureEffect) {
                    resolveOpponentMayPlayCreature(gameData, entry.getControllerId());
                }
            }
        } else if (entry.getEntryType() == StackEntryType.SORCERY_SPELL) {
            String logEntry = entry.getDescription() + " resolves.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} resolves (X={})", gameData.id, entry.getDescription(), entry.getXValue());

            for (CardEffect effect : entry.getEffectsToResolve()) {
                if (effect instanceof DealDamageToFlyingAndPlayersEffect) {
                    resolveDealDamageToFlyingAndPlayers(gameData, entry.getXValue());
                }
            }
        } else if (entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
            String logEntry = entry.getDescription() + " resolves.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

            for (CardEffect effect : entry.getEffectsToResolve()) {
                if (effect instanceof BoostTargetCreatureEffect boost) {
                    resolveBoostTargetCreature(gameData, entry, boost);
                }
            }
        }

        broadcastStackUpdate(gameData);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
    }

    private UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        return toJoinGame(data, playerId);
    }

    public void keepHand(GameData gameData, Player player) {
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

            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new MulliganResolvedMessage(player.getUsername(), true, mulliganCount));

            if (mulliganCount > 0 && !hand.isEmpty()) {
                int cardsToBottom = Math.min(mulliganCount, hand.size());
                gameData.playerNeedsToBottom.put(player.getId(), cardsToBottom);
                sessionManager.sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));

                String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                        " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} kept hand (no mulligans)", gameData.id, player.getUsername());

                checkStartGame(gameData);
            }
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
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

            sessionManager.sendToPlayer(player.getId(), new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(player.getId(), 0)));

            String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                    " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

            broadcastDeckSizes(gameData);
            checkStartGame(gameData);
        }
    }

    private void checkStartGame(GameData gameData) {
        if (gameData.playerKeptHand.size() >= 2 && gameData.playerNeedsToBottom.isEmpty()) {
            startGame(gameData);
        }
    }

    public void mulligan(GameData gameData, Player player) {
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

            sessionManager.sendToPlayer(player.getId(), new HandDrawnMessage(new ArrayList<>(newHand), newMulliganCount));
            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new MulliganResolvedMessage(player.getUsername(), false, newMulliganCount));

            String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
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

        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameStartedMessage(
                gameData.activePlayerId, gameData.turnNumber, gameData.currentStep, getPriorityPlayerId(gameData)
        ));

        broadcastLifeTotals(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        resolveAutoPass(gameData);
    }

    private JoinGame toJoinGame(GameData data, UUID playerId) {
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
                getLifeTotals(data),
                new ArrayList<>(data.stack)
        );
    }

    private List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    private void broadcastDeckSizes(GameData data) {
        sessionManager.sendToPlayers(data.orderedPlayerIds, new DeckSizesUpdatedMessage(getDeckSizes(data)));
    }

    private List<List<Permanent>> getBattlefields(GameData data) {
        List<List<Permanent>> battlefields = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            battlefields.add(bf != null ? new ArrayList<>(bf) : new ArrayList<>());
        }
        return battlefields;
    }

    private void broadcastBattlefields(GameData data) {
        sessionManager.sendToPlayers(data.orderedPlayerIds, new BattlefieldUpdatedMessage(getBattlefields(data)));
    }

    public void playCard(GameData gameData, Player player, int cardIndex, int xValue, UUID targetPermanentId) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            UUID playerId = player.getId();
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
            if (!playable.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable");
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.get(cardIndex);

            // For X-cost spells, validate that player can pay colored + generic + xValue
            if (card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                if (cost.hasX()) {
                    if (xValue < 0) {
                        throw new IllegalStateException("X value cannot be negative");
                    }
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    if (!cost.canPay(pool, xValue)) {
                        throw new IllegalStateException("Not enough mana to pay for X=" + xValue);
                    }
                }
            }

            // Validate target if specified
            if (targetPermanentId != null) {
                Permanent target = findPermanentById(gameData, targetPermanentId);
                if (target == null) {
                    throw new IllegalStateException("Invalid target permanent");
                }
            }

            hand.remove(cardIndex);

            if (card.getType() == CardType.BASIC_LAND) {
                // Lands bypass the stack — go directly onto battlefield
                gameData.playerBattlefields.get(playerId).add(new Permanent(card));
                gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastBattlefields(gameData);

                String logEntry = player.getUsername() + " plays " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.CREATURE) {
                // Creatures go on the stack
                if (card.getManaCost() != null) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    cost.pay(pool);
                    sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
                }

                gameData.stack.add(new StackEntry(card, playerId));
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.SORCERY) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                cost.pay(pool, xValue);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        new ArrayList<>(card.getSpellEffects()), xValue
                ));
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + " (X=" + xValue + ").";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {} with X={}", gameData.id, player.getUsername(), card.getName(), xValue);

                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.INSTANT) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                cost.pay(pool);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

                gameData.stack.add(new StackEntry(
                        StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                        new ArrayList<>(card.getSpellEffects()), 0, targetPermanentId
                ));
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                resolveAutoPass(gameData);
            }
        }
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            UUID playerId = player.getId();
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
            sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));

            String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

            broadcastPlayableCards(gameData);
        }
    }

    public void setAutoStops(GameData gameData, Player player, List<TurnStep> stops) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            sessionManager.sendToPlayer(player.getId(), new AutoStopsUpdatedMessage(new ArrayList<>(stopSet)));
        }
    }

    // ===== ETB effect methods =====

    private void resolveOpponentMayPlayCreature(GameData gameData, UUID controllerId) {
        UUID opponentId = getOpponentId(gameData, controllerId);
        List<Card> opponentHand = gameData.playerHands.get(opponentId);

        List<Integer> creatureIndices = new ArrayList<>();
        if (opponentHand != null) {
            for (int i = 0; i < opponentHand.size(); i++) {
                if (opponentHand.get(i).getType() == CardType.CREATURE) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String opponentName = gameData.playerIdToName.get(opponentId);
            String logEntry = opponentName + " has no creature cards in hand.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for ETB effect", gameData.id, opponentName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        beginCardChoice(gameData, opponentId, creatureIndices, prompt);
    }

    private void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingCardChoice = true;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            if (!gameData.awaitingCardChoice) {
                throw new IllegalStateException("Not awaiting card choice");
            }
            if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;

            gameData.awaitingCardChoice = false;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;

            if (cardIndex == -1) {
                // Player declined
                String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());
            } else {
                if (!validIndices.contains(cardIndex)) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }

                List<Card> hand = gameData.playerHands.get(playerId);
                Card card = hand.remove(cardIndex);
                gameData.playerBattlefields.get(playerId).add(new Permanent(card));

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(new ArrayList<>(hand), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastBattlefields(gameData);

                String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

                // Check if the creature entering via ETB has its own ETB effects
                if (card.getOnEnterBattlefieldEffects() != null && !card.getOnEnterBattlefieldEffects().isEmpty()) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s ETB ability",
                            new ArrayList<>(card.getOnEnterBattlefieldEffects())
                    ));
                    String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                    gameData.gameLog.add(etbLog);
                    broadcastLogEntry(gameData, etbLog);
                    broadcastStackUpdate(gameData);
                    log.info("Game {} - {} ETB ability pushed onto stack (via Wumpus)", gameData.id, card.getName());
                }
            }

            resolveAutoPass(gameData);
        }
    }

    // ===== Instant effect methods =====

    private void resolveBoostTargetCreature(GameData gameData, StackEntry entry, BoostTargetCreatureEffect boost) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            String fizzleLog = entry.getCard().getName() + " fizzles (target no longer exists).";
            gameData.gameLog.add(fizzleLog);
            broadcastLogEntry(gameData, fizzleLog);
            log.info("Game {} - {} fizzles, target permanent {} no longer exists",
                    gameData.id, entry.getCard().getName(), entry.getTargetPermanentId());
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private Permanent findPermanentById(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    private void resetEndOfTurnModifiers(GameData gameData) {
        boolean anyReset = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0) {
                    p.resetModifiers();
                    anyReset = true;
                }
            }
        }
        if (anyReset) {
            broadcastBattlefields(gameData);
        }
    }

    // ===== Sorcery effect methods =====

    private void resolveDealDamageToFlyingAndPlayers(GameData gameData, int damage) {
        // Deal damage to creatures with flying
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (p.getCard().getKeywords().contains(Keyword.FLYING)) {
                    int toughness = p.getEffectiveToughness();
                    if (damage >= toughness) {
                        deadIndices.add(i);
                    }
                }
            }

            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                String creatureName = battlefield.get(idx).getCard().getName();
                String logEntry = playerName + "'s " + creatureName + " is destroyed by Hurricane.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                battlefield.remove(idx);
            }
        }

        broadcastBattlefields(gameData);

        // Deal damage to each player
        for (UUID playerId : gameData.orderedPlayerIds) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - damage);

            if (damage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " takes " + damage + " damage from Hurricane.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
            }
        }

        broadcastLifeTotals(gameData);
        checkWinCondition(gameData);
    }

    // ===== Combat methods =====

    private List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
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

    private List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
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

    private List<Integer> getAttackingCreatureIndices(GameData gameData, UUID playerId) {
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

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    private List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            totals.add(gameData.playerLifeTotals.getOrDefault(pid, 20));
        }
        return totals;
    }

    private void broadcastLifeTotals(GameData gameData) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new LifeUpdatedMessage(getLifeTotals(gameData)));
    }

    private void handleDeclareAttackersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            skipToEndOfCombat(gameData);
            return;
        }

        gameData.awaitingAttackerDeclaration = true;
        sessionManager.sendToPlayer(activeId, new AvailableAttackersMessage(attackable));
    }

    private void skipToEndOfCombat(GameData gameData) {
        gameData.currentStep = TurnStep.END_OF_COMBAT;
        clearCombatState(gameData);

        String logEntry = "Step: " + TurnStep.END_OF_COMBAT.getDisplayName();
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new StepAdvancedMessage(getPriorityPlayerId(gameData), TurnStep.END_OF_COMBAT));
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        synchronized (gameData) {
            if (!gameData.awaitingAttackerDeclaration) {
                throw new IllegalStateException("Not awaiting attacker declaration");
            }
            if (!player.getId().equals(gameData.activePlayerId)) {
                throw new IllegalStateException("Only the active player can declare attackers");
            }

            UUID playerId = player.getId();
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
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = getAttackingCreatureIndices(gameData, activeId);

        if (blockable.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block", gameData.id);
            advanceStep(gameData);
            return;
        }

        gameData.awaitingBlockerDeclaration = true;
        sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            if (!gameData.awaitingBlockerDeclaration) {
                throw new IllegalStateException("Not awaiting blocker declaration");
            }

            UUID activeId = gameData.activePlayerId;
            UUID defenderId = getOpponentId(gameData, activeId);

            if (!player.getId().equals(defenderId)) {
                throw new IllegalStateException("Only the defending player can declare blockers");
            }

            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
            List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
            List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

            // Validate assignments
            Set<Integer> usedBlockers = new HashSet<>();
            for (BlockerAssignment assignment : blockerAssignments) {
                int blockerIdx = assignment.blockerIndex();
                int attackerIdx = assignment.attackerIndex();

                if (!blockable.contains(blockerIdx)) {
                    throw new IllegalStateException("Invalid blocker index: " + blockerIdx);
                }
                if (!usedBlockers.add(blockerIdx)) {
                    throw new IllegalStateException("Duplicate blocker index: " + blockerIdx);
                }
                if (attackerIdx < 0 || attackerIdx >= attackerBattlefield.size() || !attackerBattlefield.get(attackerIdx).isAttacking()) {
                    throw new IllegalStateException("Invalid attacker index: " + attackerIdx);
                }

                Permanent attacker = attackerBattlefield.get(attackerIdx);
                Permanent blocker = defenderBattlefield.get(blockerIdx);
                if (attacker.getCard().getKeywords().contains(Keyword.FLYING)
                        && !blocker.getCard().getKeywords().contains(Keyword.FLYING)
                        && !blocker.getCard().getKeywords().contains(Keyword.REACH)) {
                    throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
                }
            }

            gameData.awaitingBlockerDeclaration = false;

            // Mark creatures as blocking
            for (BlockerAssignment assignment : blockerAssignments) {
                Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
                blocker.setBlocking(true);
                blocker.setBlockingTarget(assignment.attackerIndex());
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
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = getOpponentId(gameData, activeId);

        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);

        int damageToDefender = 0;
        Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
        Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

        List<Integer> attackingIndices = getAttackingCreatureIndices(gameData, activeId);

        for (int atkIdx : attackingIndices) {
            Permanent attacker = attackerBattlefield.get(atkIdx);
            int attackerPower = attacker.getEffectivePower();
            int attackerToughness = attacker.getEffectiveToughness();

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
                    int blockerToughness = blocker.getEffectiveToughness();
                    int blockerPower = blocker.getEffectivePower();

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
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            if (life <= 0) {
                UUID winnerId = getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameOverMessage(winnerId, winnerName));

                log.info("Game {} - {} wins! {} is at {} life", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life);
                return true;
            }
        }
        return false;
    }

    private void clearCombatState(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                battlefield.forEach(Permanent::clearCombatState);
            }
        }
        broadcastBattlefields(gameData);
    }

    // ===== End combat methods =====

    private void drainManaPools(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));
            }
        }
    }

    private Map<String, Integer> getManaPool(GameData data, UUID playerId) {
        if (playerId == null) {
            return Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
    }

    private List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING) {
            return playable;
        }

        UUID priorityHolder = getPriorityPlayerId(gameData);
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

        boolean stackEmpty = gameData.stack.isEmpty();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.BASIC_LAND && isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                playable.add(i);
            }
            if (card.getType() == CardType.CREATURE && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (cost.canPay(pool)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.SORCERY && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                // For X-cost spells, playable if player can pay the colored portion (X=0 minimum)
                if (cost.canPay(pool)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.INSTANT && card.getManaCost() != null) {
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
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
            sessionManager.sendToPlayer(playerId, new PlayableCardsMessage(playable));
        }
    }

    private void resolveAutoPass(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.awaitingAttackerDeclaration || gameData.awaitingBlockerDeclaration || gameData.awaitingCardChoice) {
                broadcastPlayableCards(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                broadcastPlayableCards(gameData);
                return;
            }

            UUID priorityHolder = getPriorityPlayerId(gameData);

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
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        broadcastPlayableCards(gameData);
    }

}
