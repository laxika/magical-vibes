package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffect;
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
public class PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} tuck-on-death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        gameData.playerDecks.get(ownerId).add(0, sourceCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String logEntry = sourceCard.getName() + " is put on top of " + ownerName + "'s library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(sourceCard).text(" is put on top of " + ownerName + "'s library.").build());
        log.info("Game {} - {} put on top of {}'s library from graveyard", gameData.id, sourceCard.getName(), ownerName);
    }
}
