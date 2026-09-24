package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Discards the exact duplicate tracked by Spellchain Scatter, without opening a discard choice. */
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
        DiscardSpecificCardEffect discardEffect = (DiscardSpecificCardEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        Card card = hand.stream()
                .filter(candidate -> candidate.getId().equals(discardEffect.cardId()))
                .findFirst()
                .orElse(null);
        if (card == null) {
            return;
        }

        hand.remove(card);
        gameData.discardCausedByOpponent = false;
        triggerCollectionService.beginDiscardEvent(gameData, entry.getControllerId());
        graveyardService.discardCard(gameData, entry.getControllerId(), card);
        triggerCollectionService.checkDiscardTriggers(gameData, entry.getControllerId(), card);
        triggerCollectionService.finishDiscardEvent(gameData);
        gameLogService.append(gameData, GameLog.cardThen(card, " is discarded."));
    }
}
