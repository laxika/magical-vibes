package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyRemoveKeywordEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerpetuallyRemoveKeywordEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyRemoveKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (PerpetuallyRemoveKeywordEffect) effect;
        if (entry.getCard() == null) {
            return;
        }
        gameData.perpetualCardRemovedKeywords
                .computeIfAbsent(entry.getCard().getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(removal.keyword());
    }
}
