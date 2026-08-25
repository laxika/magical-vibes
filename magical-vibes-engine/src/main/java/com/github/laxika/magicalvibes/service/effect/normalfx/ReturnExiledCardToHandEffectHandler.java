package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Returns a card remembered by a token's leaves-the-battlefield trigger to the bound hand. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnExiledCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnExiledCardToHandEffect returnEffect = (ReturnExiledCardToHandEffect) effect;
        UUID cardId = returnEffect.exiledCardId();
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }

        Card card = exiled.card();
        UUID handPlayerId = returnEffect.handPlayerId() != null
                ? returnEffect.handPlayerId() : exiled.ownerId();
        gameData.addCardToHand(handPlayerId, card);
        String playerName = gameData.playerIdToName.get(handPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", card,
                " from exile into their hand."));
        log.info("Game {} - {} returns {} from exile to hand", gameData.id, playerName, card.getName());
    }
}
