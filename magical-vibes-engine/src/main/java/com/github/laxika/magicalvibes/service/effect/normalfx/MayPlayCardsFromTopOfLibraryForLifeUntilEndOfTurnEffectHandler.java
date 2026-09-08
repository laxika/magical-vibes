package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect;
import org.springframework.stereotype.Component;

@Component
public class MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.libraryTopCardLifePlayPermissionsUntilEndOfTurn.add(entry.getControllerId());
    }
}
