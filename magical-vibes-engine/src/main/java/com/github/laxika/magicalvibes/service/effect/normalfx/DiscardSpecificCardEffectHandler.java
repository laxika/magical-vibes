package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a delayed discard while preserving the identity of the selected card. */
@Component
@RequiredArgsConstructor
public class DiscardSpecificCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardSpecificCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var discard = (DiscardSpecificCardEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(discard.cardId())) {
                gameData.discardCausedByOpponent = false;
                playerInteractionSupport.resolveDiscardCards(
                        gameData, entry.getControllerId(), 1, List.of(i));
                return;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId())
                        + " no longer has the card to discard."));
    }
}
