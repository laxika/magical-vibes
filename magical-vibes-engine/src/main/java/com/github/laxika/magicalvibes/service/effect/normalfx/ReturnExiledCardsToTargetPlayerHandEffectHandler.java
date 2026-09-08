package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardsToTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnExiledCardsToTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardsToTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnExiledCardsToTargetPlayerHandEffect) effect;
        int returned = 0;
        for (var cardId : returnEffect.cardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || !returnEffect.playerId().equals(exiled.ownerId())
                    || !gameData.removeFromExile(cardId)) {
                continue;
            }
            gameData.addCardToHand(returnEffect.playerId(), exiled.card());
            returned++;
        }

        if (returned > 0) {
            String playerName = gameData.playerIdToName.get(returnEffect.playerId());
            gameLogService.append(gameData, GameLog.text(playerName + " returns " + returned
                    + " card" + (returned != 1 ? "s" : "") + " from exile to their hand."));
        }
        log.info("Game {} - {} returns {} cards from Suppress's delayed exile",
                gameData.id, gameData.playerIdToName.get(returnEffect.playerId()), returned);
    }
}
