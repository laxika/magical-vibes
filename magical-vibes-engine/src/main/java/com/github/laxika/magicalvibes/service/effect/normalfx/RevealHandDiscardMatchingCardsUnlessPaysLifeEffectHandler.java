package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandDiscardMatchingCardsUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealHandDiscardMatchingCardsUnlessPaysLifeEffect}: the target player reveals
 * their hand, then gets one independent pay-or-discard decision per matching revealed card. The
 * decisions belong to the target player, so payable ones are queued as may-abilities and offered one
 * at a time (remaining card ids live in {@link GameData#revealHandDiscardUnlessPaysRemaining}); a
 * card the player can't pay for is discarded immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandDiscardMatchingCardsUnlessPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandDiscardMatchingCardsUnlessPaysLifeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<UUID> matching = new ArrayList<>();
        for (Card card : hand) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, e.cardFilter(), entry.getCard().getId(), gameData, targetPlayerId)) {
                matching.add(card.getId());
            }
        }

        gameData.revealHandDiscardUnlessPaysRemaining.clear();
        if (matching.isEmpty()) {
            return;
        }
        gameData.revealHandDiscardUnlessPaysRemaining.addAll(matching.subList(1, matching.size()));
        offerCard(gameData, e, entry.getCard(), entry.getControllerId(), targetPlayerId, matching.getFirst());
    }

    /**
     * After a decision on one card: discard it when the player declined or couldn't pay, then offer
     * the next queued card. Called from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterCardDecision(GameData gameData, PendingMayAbility ability,
            RevealHandDiscardMatchingCardsUnlessPaysLifeEffect effect, UUID playerId, boolean paid) {
        if (!paid) {
            discardCard(gameData, playerId, ability.targetCardId(), ability.sourceCard(),
                    ability.sourceControllerId());
        }
        if (gameData.revealHandDiscardUnlessPaysRemaining.isEmpty()) {
            return;
        }
        UUID next = gameData.revealHandDiscardUnlessPaysRemaining.removeFirst();
        offerCard(gameData, effect, ability.sourceCard(), ability.sourceControllerId(), playerId, next);
    }

    private void offerCard(GameData gameData, RevealHandDiscardMatchingCardsUnlessPaysLifeEffect effect,
            Card sourceCard, UUID sourceControllerId, UUID playerId, UUID cardId) {
        Card card = findInHand(gameData, playerId, cardId);
        if (card == null) {
            // Left the hand between decisions — nothing to discard; move on.
            afterCardDecision(gameData, new PendingMayAbility(sourceCard, playerId, List.of(effect),
                    "", cardId, sourceControllerId), effect, playerId, true);
            return;
        }

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= effect.lifeCost();
        if (!canPay) {
            discardCard(gameData, playerId, cardId, sourceCard, sourceControllerId);
            if (gameData.revealHandDiscardUnlessPaysRemaining.isEmpty()) {
                return;
            }
            offerCard(gameData, effect, sourceCard, sourceControllerId, playerId,
                    gameData.revealHandDiscardUnlessPaysRemaining.removeFirst());
            return;
        }

        String prompt = "Pay " + effect.lifeCost() + " life? If you don't, discard " + card.getName()
                + ". (" + sourceCard.getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, playerId, List.of(effect), prompt, cardId, sourceControllerId));
    }

    private void discardCard(GameData gameData, UUID playerId, UUID cardId, Card sourceCard,
            UUID sourceControllerId) {
        Card card = findInHand(gameData, playerId, cardId);
        if (card == null) {
            return;
        }
        gameData.discardCausedByOpponent = !playerId.equals(sourceControllerId);
        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }
        gameData.playerHands.get(playerId).remove(card);
        graveyardService.discardCard(gameData, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " discards ", card, "."));
        log.info("Game {} - {} discards {} ({})", gameData.id,
                gameData.playerIdToName.get(playerId), card.getName(), sourceCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }

    private Card findInHand(GameData gameData, UUID playerId, UUID cardId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return null;
        }
        return hand.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
    }
}
