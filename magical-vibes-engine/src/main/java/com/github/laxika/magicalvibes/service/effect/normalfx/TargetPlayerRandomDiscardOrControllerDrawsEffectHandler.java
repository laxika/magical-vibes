package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerRandomDiscardOrControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerRandomDiscardOrControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = targetName + " has no cards to discard. " + gameData.playerIdToName.get(controllerId) + " draws a card.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            drawService.resolveDrawCard(gameData, controllerId);
        } else {
            gameData.discardCausedByOpponent = true;
            playerInteractionSupport.resolveRandomDiscardCards(gameData, targetPlayerId, entry.getCard().getName(), 1);
        }
    
    }
}
