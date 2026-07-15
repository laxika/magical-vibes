package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromGraveyardToOwnerHandEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromGraveyardToOwnerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (card not in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
            log.info("Game {} - {} return-to-hand trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        gameData.playerHands.get(ownerId).add(sourceCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String logEntry = sourceCard.getName() + " returns from graveyard to " + ownerName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} returns from graveyard to {}'s hand", gameData.id, sourceCard.getName(), ownerName);
    }
}
