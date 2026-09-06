package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnAllCardsExiledWithSourceToOwnerGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            entry.setEventValue(0);
            return;
        }

        List<ExiledCardEntry> cardsToReturn = gameData.exiledCards.stream()
                .filter(exiled -> sourcePermanentId.equals(exiled.sourcePermanentId()))
                .toList();
        int returnedCards = 0;
        for (ExiledCardEntry exiled : cardsToReturn) {
            if (!gameData.removeFromExile(exiled.card().getId())) {
                continue;
            }
            graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
            returnedCards++;
            gameLogService.append(gameData,
                    GameLog.cardThen(exiled.card(), " returns to its owner's graveyard."));
        }
        entry.setEventValue(returnedCards);
    }
}
