package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantWarpToChosenCardUntilEndOfTurnEffect;
import org.springframework.stereotype.Component;

@Component
public class GrantWarpToChosenCardUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantWarpToChosenCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card chosenCard = entry.getChosenObjectCard();
        if (chosenCard != null) {
            gameData.cardsGrantedWarpUntilEndOfTurn.add(chosenCard.getId());
        }
    }
}
