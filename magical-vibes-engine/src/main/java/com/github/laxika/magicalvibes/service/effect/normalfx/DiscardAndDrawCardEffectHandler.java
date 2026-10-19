package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardAndDrawCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardAndDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardAndDrawCardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            if (e.drawRegardless()) {
                playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawAmount());
            }
            return;
        }
        if (!e.drawRegardless() && hand.size() < e.discardAmount()) {
            // An "if you do" discard that the hand can't cover in full is not paid, so nothing is
            // discarded and the draw never happens.
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " can't discard " + e.discardAmount() + " cards."));
            return;
        }
        gameData.discardCausedByOpponent = false;
        if (e.random()) {
            playerInteractionSupport.resolveRandomDiscardCards(gameData, controllerId,
                    entry.getCard().getName(), e.discardAmount());
            playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawAmount());
        } else {
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, e.discardAmount(),
                    DiscardFollowUp.rummage(e.drawAmount()));
        }
    }
}
