package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardSpecificCardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardSpecificCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DiscardSpecificCardEffect discard = (DiscardSpecificCardEffect) effect;
        var hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        Card card = hand.stream()
                .filter(candidate -> discard.cardId().equals(candidate.getId()))
                .findFirst()
                .orElse(null);
        if (card == null) {
            return;
        }

        gameData.discardCausedByOpponent = false;
        triggerCollectionService.beginDiscardEvent(gameData, entry.getControllerId());
        hand.remove(card);
        graveyardService.discardCard(gameData, entry.getControllerId(), card);
        gameLogService.append(gameData, GameLog.cardThen(card, " is discarded."));
        triggerCollectionService.checkDiscardTriggers(gameData, entry.getControllerId(), card);
        triggerCollectionService.finishDiscardEvent(gameData);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }
}
