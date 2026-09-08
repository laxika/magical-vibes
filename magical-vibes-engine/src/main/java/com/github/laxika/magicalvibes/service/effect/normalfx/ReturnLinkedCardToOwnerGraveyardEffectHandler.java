package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLinkedCardToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the linked leave-the-battlefield return granted to Hofri's token copies. */
@Component
@RequiredArgsConstructor
public class ReturnLinkedCardToOwnerGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnLinkedCardToOwnerGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID linkedCardId = ((ReturnLinkedCardToOwnerGraveyardEffect) effect).linkedCardId();
        if (linkedCardId == null) {
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(linkedCardId);
        if (exiled == null) {
            return;
        }

        gameData.removeFromExile(linkedCardId);
        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(), " returns to its owner's graveyard."));
    }
}
