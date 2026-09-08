package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Wand of Ith's random reveal and pay-or-discard choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " has no cards to reveal."));
            log.info("Game {} - {}: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        Card revealed = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
        gameLogService.append(gameData,
                GameLog.textCardText(targetName + " reveals ", revealed, " at random."));
        cardRevealService.revealToAllPlayers(
                gameData, targetPlayerId, GameEventFact.RevealZone.HAND, List.of(revealed));

        int lifeCost = lifeCost(revealed);
        boolean canPay = lifeCost == 0
                || (gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)
                && gameData.getLife(targetPlayerId) >= lifeCost);
        if (!canPay) {
            discardCard(gameData, targetPlayerId, revealed.getId(), entry.getCard(), entry.getControllerId());
            return;
        }

        String prompt = "Pay " + lifeCost + " life? If you don't, discard " + revealed.getName()
                + ". (" + sourceName + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(effect), prompt,
                revealed.getId(), entry.getControllerId()));
    }

    public int lifeCost(Card card) {
        return card.hasType(CardType.LAND) ? 1 : card.getManaValue();
    }

    public void discardCard(GameData gameData, UUID playerId, UUID cardId, Card sourceCard,
            UUID sourceControllerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return;
        }
        Card card = hand.stream().filter(candidate -> candidate.getId().equals(cardId)).findFirst().orElse(null);
        if (card == null) {
            return;
        }

        hand.remove(card);
        gameData.discardCausedByOpponent = sourceControllerId != null && !sourceControllerId.equals(playerId);
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
}
