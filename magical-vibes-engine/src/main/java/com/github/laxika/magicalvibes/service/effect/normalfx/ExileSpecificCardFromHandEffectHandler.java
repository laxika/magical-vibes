package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpecificCardFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a delayed exile of a specific card only while it remains in hand. */
@Component
@RequiredArgsConstructor
public class ExileSpecificCardFromHandEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSpecificCardFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return;
        }

        UUID cardId = ((ExileSpecificCardFromHandEffect) effect).cardId();
        Card card = hand.stream()
                .filter(candidate -> candidate.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (card == null) {
            return;
        }

        hand.remove(card);
        exileService.exileCard(gameData, controllerId, card);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s delayed trigger exiles " + card.getName() + " from hand."));
    }
}
