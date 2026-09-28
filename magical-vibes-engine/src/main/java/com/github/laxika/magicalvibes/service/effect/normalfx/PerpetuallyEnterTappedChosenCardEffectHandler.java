package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyEnterTappedChosenCardEffect;
import org.springframework.stereotype.Component;

/** Records the selected card identity for the battlefield-entry tapped replacement. */
@Component
public class PerpetuallyEnterTappedChosenCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyEnterTappedChosenCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card chosenCard = entry.getChosenObjectCard();
        if (chosenCard != null) {
            gameData.perpetualEnterTappedCardIds.add(chosenCard.getId());
        }
    }
}
