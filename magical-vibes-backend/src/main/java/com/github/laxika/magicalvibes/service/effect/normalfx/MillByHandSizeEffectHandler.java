package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillByHandSizeEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillByHandSizeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int handSize = hand != null ? hand.size() : 0;

        if (handSize == 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no cards in hand — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, handSize);
    }
}
