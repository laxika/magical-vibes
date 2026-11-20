package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole discard family via {@link DiscardEffect}: {@link SubjectResolutionService} names who discards
 * and {@code random} chooses between chosen and random discard. One player discards directly; several target players
 * each choose and discard in turn, in APNAP order; {@code eachPlayer()} / {@code eachOpponent()} (and any other
 * subject naming several players) make every player choose in APNAP order and then discard the chosen cards at once
 * (CR 101.4), while random discards run inline in APNAP order. The {@code discardCausedByOpponent} flag, read by
 * discard-punisher triggers (e.g. Raider's Wake), is {@code true} for each discarding player other than the
 * controller. A card {@code filter} limits the cards each player may choose to those matching it.
 */
@Component
@RequiredArgsConstructor
public class DiscardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final com.github.laxika.magicalvibes.service.input.PlayerInputService playerInputService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardEffect) effect;
        if (!gameData.eachPlayerRummage.active) {
            // A new discard step (not a rerun after one player's choice): every card discarded from here on, as this
            // resolves or as its discard prompts are answered, is added to the step's result (GameData.discarded).
            entry.recordStep(StepKind.DISCARD, StepOutcome.NONE);
        }

        // Clear the last-discarded snapshot so a trailing LastDiscardedCardManaValue on the same
        // spell reads this discard only, and stays 0 when nothing is discarded (empty hand).
        gameData.lastDiscardedCardManaValue = 0;
        gameData.greatestDiscardedCardManaValue = 0;
        gameData.lastDiscardedCardTypes = Set.of();

        // Source-relative amounts (e.g. CountersOnSource for Shrine of Limitless Power) use the
        // live source permanent when still on the battlefield, else the last-known snapshot
        // (sacrificed as an activation cost). X-based amounts (Mind Shatter) read xValue.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        PlayerSubject subject = e.player();
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, subject);
        if (subject instanceof PlayerSubject.TargetPlayer) {
            if (players.size() > 1) {
                resolveTargetPlayers(gameData, entry, e, amount, players);
            } else if (!players.isEmpty()) {
                resolveSinglePlayer(gameData, entry, e, amount, players.getFirst());
            }
        } else if (subject instanceof PlayerSubject.Each || players.size() > 1) {
            resolveEachPlayer(gameData, entry, e, amount, players);
        } else if (!players.isEmpty()) {
            resolveSinglePlayer(gameData, entry, e, amount, players.getFirst());
        }
    }

    /** Several target players choose and discard one after another, in APNAP order. */
    private void resolveTargetPlayers(GameData gameData, StackEntry entry, DiscardEffect e, int amount,
                                      List<UUID> targetPlayers) {
        if (e.random()) {
            for (UUID playerId : targetPlayers) {
                gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
                playerInteractionSupport.resolveRandomDiscardCards(
                        gameData, playerId, entry.getCard().getName(), amount);
            }
            return;
        }
        playerInteractionSupport.startNextEachPlayerDiscard(
                gameData, DiscardFollowUp.eachPlayer(targetPlayers, entry.getControllerId(), amount));
    }

    private void resolveSinglePlayer(GameData gameData, StackEntry entry, DiscardEffect e, int amount,
                                     UUID playerId) {
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());

        if (amount <= 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " discards 0 cards."));
            return;
        }

        if (e.random()) {
            playerInteractionSupport.resolveRandomDiscardCards(gameData, playerId,
                    entry.getCard().getName(), amount);
        } else if (e.filter() != null) {
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount,
                    matchingIndices(gameData, entry, e, playerId));
        } else if (e.onlyCardsDrawnThisResolution()) {
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            List<Integer> validIndices = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                if (entry.getDrawnCardIdsThisResolution().contains(hand.get(i).getId())) {
                    validIndices.add(i);
                }
            }
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount, validIndices);
        } else if (e.stopAfterDiscardingPredicate() != null) {
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount,
                    DiscardFollowUp.NONE, e.stopAfterDiscardingPredicate());
        } else {
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount,
                    DiscardFollowUp.NONE, e.stopAfterDiscardingType());
        }
    }

    /**
     * Several players discard: random discards run inline in APNAP order; chosen discards are chosen by every player in
     * APNAP order first and then discarded together (CR 101.4). The handler is rerun after each choice, so the players
     * are fixed on the first run.
     */
    private void resolveEachPlayer(GameData gameData, StackEntry entry, DiscardEffect e, int amount,
                                   List<UUID> players) {
        UUID controllerId = entry.getControllerId();

        if (e.random()) {
            String sourceName = entry.getCard().getName();
            for (UUID playerId : players) {
                gameData.discardCausedByOpponent = !playerId.equals(controllerId);
                playerInteractionSupport.resolveRandomDiscardCards(gameData, playerId, sourceName, amount);
            }
            return;
        }

        var state = gameData.eachPlayerRummage;
        if (!state.active) {
            state.active = true;
            state.deferDiscards = true;
            state.remaining.addAll(players);
        }
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            List<Integer> choosable = e.filter() == null
                    ? java.util.stream.IntStream.range(0, hand.size()).boxed().toList()
                    : matchingIndices(gameData, entry, e, playerId);
            if (choosable.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " has no cards to discard."));
                continue;
            }
            if (!playerId.equals(controllerId) && gameQueryService.isDiscardPrevented(gameData, playerId)) continue;
            int count = Math.min(amount, choosable.size());
            if (count <= 0) continue;
            state.currentPlayerId = playerId;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginDiscardChoice(gameData, playerId, choosable,
                    "Choose cards to discard.", count, DiscardFollowUp.NONE);
            return;
        }
        var selected = List.copyOf(state.selectedDiscards);
        state.reset();
        cardChoiceHandlerService.discardCollectedCards(gameData, selected, controllerId);
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    /** The indices of the cards in {@code playerId}'s hand that match the discard's card filter. */
    private List<Integer> matchingIndices(GameData gameData, StackEntry entry, DiscardEffect e, UUID playerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(hand.get(i), e.filter(), sourceCardId,
                    gameData, entry.getControllerId())) {
                indices.add(i);
            }
        }
        return indices;
    }
}
