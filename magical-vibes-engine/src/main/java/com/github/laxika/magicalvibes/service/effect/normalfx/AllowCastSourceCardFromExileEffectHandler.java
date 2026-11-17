package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import org.springframework.stereotype.Component;

@Component
public class AllowCastSourceCardFromExileEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastSourceCardFromExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null || gameData.findExiledCard(sourceCard.getId()) == null) {
            return;
        }

        gameData.grantExilePlayPermission(sourceCard.getId(), entry.getControllerId(),
                ((AllowCastSourceCardFromExileEffect) effect).thisTurnOnly()
                        ? Duration.UNTIL_END_OF_TURN : Duration.PERMANENT);
    }
}
