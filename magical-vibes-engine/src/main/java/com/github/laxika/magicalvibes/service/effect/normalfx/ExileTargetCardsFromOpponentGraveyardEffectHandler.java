package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCardsFromOpponentGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardsFromOpponentGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCardsFromOpponentGraveyardEffect) effect;

        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(entry.getControllerId());

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " fizzles (no targets).");
            return;
        }

        List<String> exiledNames = new ArrayList<>();
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                exiledNames.add(card.getName());
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
            }
        }

        if (!exiledNames.isEmpty()) {
            String logEntry = playerName + " exiles " + String.join(", ", exiledNames)
                    + " from an opponent's graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} exiled {} cards from opponent's graveyard",
                    gameData.id, playerName, exiledNames.size());
        }
    }
}
