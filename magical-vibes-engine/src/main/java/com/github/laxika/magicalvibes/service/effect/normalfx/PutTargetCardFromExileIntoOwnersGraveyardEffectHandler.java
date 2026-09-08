package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromExileIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardFromExileIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardFromExileIntoOwnersGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetId() == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (no valid exile target)."));
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(entry.getTargetId());
        if (exiled == null || exiled.faceDown()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer a face-up card in exile)."));
            return;
        }

        if (!gameData.removeFromExile(exiled.card().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in exile)."));
            return;
        }

        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                        " into its owner's graveyard."));
    }
}
