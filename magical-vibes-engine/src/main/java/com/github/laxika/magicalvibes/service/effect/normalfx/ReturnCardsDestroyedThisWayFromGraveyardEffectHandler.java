package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsDestroyedThisWayFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnCardsDestroyedThisWayFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardsDestroyedThisWayFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> eventCardIds = entry.getEventCardIds();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (eventCardIds == null || eventCardIds.isEmpty() || graveyard == null || graveyard.isEmpty()) {
            return;
        }

        Set<UUID> destroyedCardIds = new HashSet<>(eventCardIds);
        List<Card> cardsToReturn = new ArrayList<>();
        for (Card card : graveyard) {
            if (destroyedCardIds.contains(card.getId())) {
                cardsToReturn.add(card);
            }
        }
        if (cardsToReturn.isEmpty()) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : cardsToReturn) {
                graveyard.remove(card);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                gameData, Map.of(controllerId, cardsToReturn), false, null);

        GameLog.Builder log = GameLog.builder().text(gameData.playerIdToName.get(controllerId) + " returns ");
        for (int i = 0; i < cardsToReturn.size(); i++) {
            if (i > 0) {
                log.text(", ");
            }
            log.card(cardsToReturn.get(i));
        }
        log.text(" from graveyard to the battlefield.");
        gameLogService.append(gameData, log.build());
    }
}
