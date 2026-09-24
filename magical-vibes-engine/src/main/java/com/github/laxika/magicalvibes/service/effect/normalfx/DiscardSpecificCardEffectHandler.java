package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardSpecificCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardSpecificCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardSpecificCardEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }
        Card card = hand.stream().filter(c -> c.getId().equals(e.cardId())).findFirst().orElse(null);
        if (card == null) {
            return;
        }

        hand.remove(card);
        gameData.discardCausedByOpponent = false;
        triggerCollectionService.beginDiscardEvent(gameData, entry.getControllerId());
        try {
            graveyardService.discardCard(gameData, entry.getControllerId(), card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(entry.getControllerId()) + " discards ", card, "."));
            triggerCollectionService.checkDiscardTriggers(gameData, entry.getControllerId(), card);
        } finally {
            triggerCollectionService.finishDiscardEvent(gameData);
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }
}
