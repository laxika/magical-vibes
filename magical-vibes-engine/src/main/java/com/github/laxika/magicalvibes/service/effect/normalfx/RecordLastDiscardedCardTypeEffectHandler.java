package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RecordLastDiscardedCardTypeEffect;
import org.springframework.stereotype.Component;

/** Resolves {@link RecordLastDiscardedCardTypeEffect}. */
@Component
public class RecordLastDiscardedCardTypeEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RecordLastDiscardedCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RecordLastDiscardedCardTypeEffect recordEffect = (RecordLastDiscardedCardTypeEffect) effect;
        entry.setEventValue(gameData.lastDiscardedCardTypes.contains(recordEffect.cardType()) ? 1 : 0);
    }
}
