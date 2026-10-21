package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUnplayedExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutUnplayedExiledCardsIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutUnplayedExiledCardsIntoOwnersGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID cardId : entry.getTargetCardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || !gameData.removeFromExile(cardId)) {
                continue;
            }
            gameData.exilePlayPermissions.remove(cardId);
            gameData.exileLookPermissions.remove(cardId);
            graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                            " into its owner's graveyard."));
        }
    }
}
