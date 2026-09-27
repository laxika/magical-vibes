package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneOfEachCardTypeFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileUpToOneOfEachCardTypeFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToOneOfEachCardTypeFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> exiledCards = new ArrayList<>();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds != null) {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                    exiledCards.add(card);
                }
            }
        }

        if (!exiledCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    entry.getCard().getName() + " exiles " + exiledCards.size()
                            + " card" + (exiledCards.size() == 1 ? "" : "s") + " from a graveyard."));
            if (entry.getSourcePermanentId() != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source != null) {
                    permanentCounterSupport.applyPlusOnePlusOneCounters(
                            gameData, entry, source, exiledCards.size());
                }
            }
        }
    }
}
