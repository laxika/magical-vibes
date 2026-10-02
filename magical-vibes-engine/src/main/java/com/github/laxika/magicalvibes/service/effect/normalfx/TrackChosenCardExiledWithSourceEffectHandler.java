package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrackChosenCardExiledWithSourceEffect;
import org.springframework.stereotype.Component;

/** Associates a card exiled by a matching-hand choice with the source permanent. */
@Component
public class TrackChosenCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrackChosenCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getChosenObjectCard() != null && entry.getSourcePermanentId() != null) {
            gameData.associateExiledCardWithSource(
                    entry.getChosenObjectCard().getId(), entry.getSourcePermanentId());
        }
    }
}
