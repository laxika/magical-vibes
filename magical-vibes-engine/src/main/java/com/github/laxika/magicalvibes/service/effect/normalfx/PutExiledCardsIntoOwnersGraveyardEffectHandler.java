package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Moves each remembered card that is still face-up in exile to its owner's graveyard. */
@Component
@RequiredArgsConstructor
public class PutExiledCardsIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutExiledCardsIntoOwnersGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID cardId : ((PutExiledCardsIntoOwnersGraveyardEffect) effect).exiledCardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || exiled.faceDown() || !gameData.removeFromExile(cardId)) {
                continue;
            }
            graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                            " into its owner's graveyard."));
        }
    }
}
