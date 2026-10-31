package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PregameChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.SecretMissionEffect;
import com.github.laxika.magicalvibes.model.effect.SerumPowderEffect;
import com.github.laxika.magicalvibes.model.PendingGemstoneCavernsChoice;
import com.github.laxika.magicalvibes.model.PendingKarnRestart;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseLegacyWordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Runs the pregame mulligan process (CR 103.5).
 *
 * <p><b>Deliberate deviation from CR 103.5.</b> The rule has each mulligan draw a new seven-card hand and
 * then put cards on the bottom of the library equal to the number of mulligans taken. Like Arena and
 * MTGO, this service instead redraws seven and defers bottoming until the player keeps
 * ({@link #keepHand}). For an ordinary mulligan the outcome is identical, and the player chooses which
 * cards to bottom once, from the hand they actually keep.
 *
 * <p>The order only matters for a mulligan action that acts on the whole hand before keeping. Serum
 * Powder ("you may exile all the cards from your hand, then draw that many cards") must exile the
 * post-bottom hand, so when a mulligan leaves Serum Powder in the new hand the cards are bottomed right
 * away, as CR 103.5 orders it ({@link #requestBottomBeforeKeeping}), and keeping that hand later bottoms
 * nothing more.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MulliganService {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.planar.PlanechaseService planechaseService;


    private final Random random = new Random();

    private final GameLogService gameLogService;
    private final TurnProgressionService turnProgressionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PlayerInputService playerInputService;
    private final GameMutationCoordinator mutationCoordinator;

    public void keepHand(GameData gameData, Player player) {
        ensureNoPendingMulliganAction(gameData);
        ensureNoPendingBottomChoice(gameData, player);
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }

        gameData.playerKeptHand.add(player.getId());
        gameData.playerMulliganDecisionIds.remove(player.getId());
        int mulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
        List<Card> hand = gameData.playerHands.get(player.getId());

        mutationCoordinator.emit(gameData,
                new GameEventFact.MulliganResolved(player.getId(), true, mulliganCount),
                GameEventAudience.allPlayers());

        // A hand that was already bottomed right after its mulligan (Serum Powder) owes nothing more.
        boolean alreadyBottomed = gameData.playerBottomedBeforeKeeping.contains(player.getId());
        if (mulliganCount > 0 && !hand.isEmpty() && !alreadyBottomed) {
            int cardsToBottom = Math.min(mulliganCount, hand.size());
            String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                    " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
            gameLogService.append(gameData, GameLog.text(logEntry));

            log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);

            requestCardsToBottom(gameData, player, cardsToBottom);
        } else {
            String logEntry = player.getUsername() + " keeps their hand.";
            gameLogService.append(gameData, GameLog.text(logEntry));

            log.info("Game {} - {} kept hand (no mulligans)", gameData.id, player.getUsername());

            checkStartGame(gameData);
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
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
        gameData.playerBottomDecisionIds.remove(player.getId());

        String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

        if (!gameData.playerKeptHand.contains(player.getId())) {
            // Bottomed right after a mulligan (Serum Powder in hand): the player still decides to keep,
            // mulligan again, or use Serum Powder on the post-bottom hand.
            continueMulliganDecision(gameData, player);
            return;
        }
        invalidateForAllPlayers(gameData);
        checkStartGame(gameData);
    }

    public void mulligan(GameData gameData, Player player) {
        ensureNoPendingMulliganAction(gameData);
        ensureNoPendingBottomChoice(gameData, player);
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }

        if (queueMulliganActionChoice(gameData, player, null)) {
            return;
        }

        performMulligan(gameData, player);
    }

    public void resolveSerumPowderChoice(GameData gameData, Player player, boolean accepted,
                                         PendingMayAbility ability) {
        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to use ", ability.sourceCard(), "."));
            if (!queueMulliganActionChoice(gameData, player, ability.sourceCard().getId())) {
                performMulligan(gameData, player);
            }
            return;
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Card> deck = gameData.playerDecks.get(player.getId());
        int cardsToDraw = hand.size();
        List<Card> cardsToExile = new ArrayList<>(hand);

        hand.clear();
        for (Card card : cardsToExile) {
            gameData.addToExile(player.getId(), card);
        }

        int drawn = 0;
        while (drawn < cardsToDraw) {
            if (deck.isEmpty()) {
                gameData.playersAttemptedDrawFromEmptyLibrary.add(player.getId());
                break;
            }
            hand.add(deck.removeFirst());
            drawn++;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles their hand and draws " + drawn + " card"
                        + (drawn == 1 ? "" : "s") + " with ", ability.sourceCard(), "."));
        log.info("Game {} - {} used Serum Powder to exile {} card(s) and draw {} card(s)",
                gameData.id, player.getUsername(), cardsToExile.size(), drawn);
        continueMulliganDecision(gameData, player);
    }

    public void declineMulliganAction(GameData gameData, Player player) {
        performMulligan(gameData, player);
    }

    public void continueMulliganDecision(GameData gameData, Player player) {
        if (gameData.status != GameStatus.FINISHED) {
            invalidateForAllPlayers(gameData);
            requestMulliganDecision(gameData, player);
        }
    }

    private boolean queueMulliganActionChoice(GameData gameData, Player player, UUID declinedCardId) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (hand == null) {
            return false;
        }
        for (Card card : hand) {
            List<CardEffect> effects = card.getEffects(EffectSlot.MULLIGAN_ACTION);
            if (effects.isEmpty() || card.getId().equals(declinedCardId)) {
                continue;
            }
            CardEffect effect = effects.getFirst();
            String description = effect.mulliganActionDescription();
            if (description == null) {
                description = "Use this mulligan action?";
            }
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    card, player.getId(), List.of(effect), card.getName() + " — " + description));
            invalidateForAllPlayers(gameData);
            playerInputService.processNextMayAbility(gameData);
            return true;
        }
        return false;
    }

    private void performMulligan(GameData gameData, Player player) {
        int currentMulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
        if (currentMulliganCount >= 7) {
            throw new IllegalStateException("Maximum mulligans reached");
        }
        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Card> deck = gameData.playerDecks.get(player.getId());

        deck.addAll(hand);
        hand.clear();
        Collections.shuffle(deck, random);
        gameData.playerBottomedBeforeKeeping.remove(player.getId());

        int count = Math.min(7, deck.size());
        if (count < 7) gameData.playersAttemptedDrawFromEmptyLibrary.add(player.getId());
        List<Card> newHand = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        gameData.playerHands.put(player.getId(), newHand);

        int newMulliganCount = currentMulliganCount + 1;
        gameData.mulliganCounts.put(player.getId(), newMulliganCount);
        mutationCoordinator.emit(gameData,
                new GameEventFact.MulliganResolved(player.getId(), false, newMulliganCount),
                GameEventAudience.allPlayers());

        String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
        if (holdsSerumPowder(newHand)) {
            requestBottomBeforeKeeping(gameData, player, newMulliganCount);
            return;
        }
        invalidateForAllPlayers(gameData);
        requestMulliganDecision(gameData, player);
    }

    private static boolean holdsSerumPowder(List<Card> hand) {
        return hand.stream().anyMatch(card -> card.getEffects(EffectSlot.MULLIGAN_ACTION).stream()
                .anyMatch(SerumPowderEffect.class::isInstance));
    }

    /**
     * CR 103.5 order for a hand holding Serum Powder: the cards for this mulligan go on the bottom now,
     * before the player can use Powder, so Powder exiles and redraws the post-bottom hand. Bottoming at
     * keep (the usual flow, see the class docs) would let Powder exile and redraw the full seven instead.
     */
    private void requestBottomBeforeKeeping(GameData gameData, Player player, int mulliganCount) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        int cardsToBottom = Math.min(mulliganCount, hand.size());
        gameData.playerBottomedBeforeKeeping.add(player.getId());
        if (cardsToBottom == 0) {
            continueMulliganDecision(gameData, player);
            return;
        }
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " must put " + cardsToBottom
                + " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library."));
        log.info("Game {} - {} holds Serum Powder, bottoms {} card(s) before keeping",
                gameData.id, player.getUsername(), cardsToBottom);
        requestCardsToBottom(gameData, player, cardsToBottom);
    }

    private void requestCardsToBottom(GameData gameData, Player player, int cardsToBottom) {
        gameData.playerNeedsToBottom.put(player.getId(), cardsToBottom);
        UUID decisionId = UUID.randomUUID();
        gameData.playerBottomDecisionIds.put(player.getId(), decisionId);
        invalidateForAllPlayers(gameData);
        mutationCoordinator.emit(gameData,
                new GameEventFact.DecisionRequested(
                        decisionId,
                        player.getId(),
                        GameEventFact.DecisionKind.CARDS_TO_BOTTOM),
                GameEventAudience.player(player.getId()));
    }

    private void ensureNoPendingMulliganAction(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()) {
            throw new IllegalStateException("You must answer the pending mulligan action first");
        }
    }

    private void ensureNoPendingBottomChoice(GameData gameData, Player player) {
        if (gameData.playerNeedsToBottom.containsKey(player.getId())) {
            throw new IllegalStateException("You must put cards on the bottom of your library first");
        }
    }

    private void checkStartGame(GameData gameData) {
        if (gameData.playerKeptHand.size() >= 2 && gameData.playerNeedsToBottom.isEmpty()) {
            startGame(gameData);
        }
    }

    private void requestMulliganDecision(GameData gameData, Player player) {
        UUID decisionId = UUID.randomUUID();
        gameData.playerMulliganDecisionIds.put(player.getId(), decisionId);
        mutationCoordinator.emit(gameData,
                new GameEventFact.DecisionRequested(
                        decisionId,
                        player.getId(),
                        GameEventFact.DecisionKind.MULLIGAN),
                GameEventAudience.player(player.getId()));
    }

    private void startGame(GameData gameData) {
        // Leyline mechanic (CR 103.6): if a card with the leyline ability is in a player's
        // opening hand, the player may begin the game with it on the battlefield.
        // Per CR 103.6, the starting player takes all such actions first, then each other player.
        List<UUID> pregameActionOrder = new ArrayList<>();
        if (gameData.startingPlayerId != null && gameData.orderedPlayerIds.contains(gameData.startingPlayerId)) {
            pregameActionOrder.add(gameData.startingPlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!pregameActionOrder.contains(playerId)) {
                pregameActionOrder.add(playerId);
            }
        }
        for (UUID playerId : pregameActionOrder) {
            // A Secret Mission's owner secretly chooses its mission before the game.
            for (Card card : gameData.playerCommandZones.getOrDefault(playerId, List.of())) {
                for (CardEffect effect : card.getStaticEffects(Zone.COMMAND)) {
                    if (effect instanceof SecretMissionEffect mission
                            && !gameData.secretMissionChoices.containsKey(card.getId())) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                card, playerId, List.of(mission),
                                card.getName() + " — Secretly choose a mission."));
                    }
                }
            }
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;
            List<Card> pregameCards = new ArrayList<>(hand);
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library != null) {
                // Legacy cards are chosen before the game starts wherever they are in the deck.
                library.stream()
                        .filter(card -> card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).stream()
                                .anyMatch(effect -> effect instanceof MayEffect may
                                        && may.wrapped() instanceof ChooseLegacyWordEffect))
                        .forEach(pregameCards::add);
            }
            for (Card card : pregameCards) {
                for (CardEffect effect : card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)) {
                    if (effect instanceof MayEffect may
                            && may.wrapped() instanceof PregameChoiceEffect pregame
                            && (!pregame.onlyForNonStartingPlayer()
                            || !playerId.equals(gameData.startingPlayerId))) {
                        // These are pregame actions, not triggered abilities —
                        // bypasses the stack, so add directly to pendingMayAbilities.
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                card, playerId,
                                List.of(may.wrapped()),
                                card.getName() + " — " + may.prompt()
                        ));
                    }
                }
            }
        }
        if (!gameData.pendingMayAbilities.isEmpty()) {
            invalidateForAllPlayers(gameData);
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        continueStartGame(gameData);
    }

    public void completeGemstoneCavernsExile(GameData gameData, UUID playerId) {
        PendingGemstoneCavernsChoice pending = gameData.pendingGemstoneCavernsChoice;
        if (pending == null || !pending.controllerId().equals(playerId)) {
            return;
        }
        gameData.pendingGemstoneCavernsChoice = null;
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            continueStartGame(gameData);
        }
    }

    /**
     * Called after all leyline may-choices have been resolved to finish the game start
     * (Karn restart, set game to RUNNING, begin turn 1).
     */
    public void continueStartGame(GameData gameData) {
        // Karn Liberated restart: put exiled cards onto battlefield before starting
        // Per ruling: "After the pregame procedure is complete but before the new game's
        // first turn, Karn's ability finishes resolving and the cards left in exile are
        // put onto the battlefield."
        PendingKarnRestart karnRestart = gameData.peekPendingInteraction(PendingKarnRestart.class);
        if (karnRestart != null && !karnRestart.cards().isEmpty()) {
            UUID controllerId = karnRestart.controllerId();
            String controllerName = gameData.playerIdToName.get(controllerId);
            for (Card card : karnRestart.cards()) {
                // The card changes zones: it must leave exile as it hits the battlefield,
                // or it would exist in both zones at once
                gameData.removeFromExile(card.getId());
                Permanent perm = new Permanent(card);
                perm.setSummoningSick(false);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

                if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                    perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                }

                boolean isCreature = card.hasType(CardType.CREATURE);
                if (isCreature) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
                } else {
                    battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, null, false);
                }

                gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts " , card, " onto the battlefield (Karn Liberated)."));
                log.info("Game {} - {} puts {} onto the battlefield from Karn's exile",
                        gameData.id, controllerName, card.getName());
            }
        }
        gameData.clearPendingInteractions(PendingKarnRestart.class);

        gameData.permanentsEnteredBattlefieldThisTurn.clear();
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        if (gameData.planechase != null) planechaseService.start(gameData);
        gameData.turnNumber = 1;
        gameData.turnStartTimestamp = gameData.timestampCounter + 1;
        gameData.turnsTakenByPlayer.clear();
        gameData.turnsTakenByPlayer.put(gameData.activePlayerId, 1);
        gameData.currentStep = TurnStep.first();
        gameData.handSizeAtTurnStart.clear();
        List<Card> handAtTurnStart = gameData.playerHands.get(gameData.activePlayerId);
        gameData.handSizeAtTurnStart.put(gameData.activePlayerId,
                handAtTurnStart == null ? 0 : handAtTurnStart.size());
        gameData.turnUntapStepSkipped = false;
        gameData.captureTurnStartSnapshot();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        gameLogService.append(gameData, GameLog.text(logEntry1));
        gameLogService.append(gameData, GameLog.text(logEntry2));

        invalidateForAllPlayers(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void invalidateForAllPlayers(GameData gameData) {
        mutationCoordinator.emit(gameData,
                new GameEventFact.StateInvalidated(
                        GameEventFact.StateSection.PRIVATE_PLAYER_VIEW),
                GameEventAudience.allPlayers());
    }

}
