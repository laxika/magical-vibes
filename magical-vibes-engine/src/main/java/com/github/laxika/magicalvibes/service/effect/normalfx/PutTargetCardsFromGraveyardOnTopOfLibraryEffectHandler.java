package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) effect;

        if (e.fromOtherGraveyards()) {
            graveyardReturnSupport.putTargetedCardsFromAnyGraveyardOnTopOfLibrary(gameData, entry);
            return;
        }

        graveyardReturnSupport.putTargetedGraveyardCardsOnTopInChosenOrder(gameData, entry);
    }
}
