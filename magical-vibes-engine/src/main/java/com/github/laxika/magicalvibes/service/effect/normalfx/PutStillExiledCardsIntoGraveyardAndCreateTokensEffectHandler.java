package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutStillExiledCardsIntoGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutStillExiledCardsIntoGraveyardAndCreateTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutStillExiledCardsIntoGraveyardAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var graveyardEffect = (PutStillExiledCardsIntoGraveyardAndCreateTokensEffect) effect;
        List<ExiledCardEntry> movedCards = new ArrayList<>();
        for (var cardId : graveyardEffect.cardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || !gameData.removeFromExile(cardId)) {
                continue;
            }
            if (graveyardService.addCardToGraveyard(
                    gameData, exiled.ownerId(), exiled.card(), Zone.EXILE)) {
                movedCards.add(exiled);
            }
        }

        if (movedCards.isEmpty()) {
            return;
        }

        permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                graveyardEffect.tokenEffect(),
                movedCards.size(),
                entry.getCard().getSetCode());
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " creates "
                        + movedCards.size() + " Eldrazi Spawn token(s) for cards put into their graveyard."));
    }
}
