package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect;
import org.springframework.stereotype.Component;

@Component
public class PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn
                .add(entry.getControllerId());
    }
}
