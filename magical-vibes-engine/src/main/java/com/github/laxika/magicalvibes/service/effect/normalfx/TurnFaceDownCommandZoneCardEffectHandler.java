package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceDownCommandZoneCardEffect;
import org.springframework.stereotype.Component;

/** Resolves the face-down state of a command-zone card such as a completed secret mission. */
@Component
public class TurnFaceDownCommandZoneCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnFaceDownCommandZoneCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() != null) {
            gameData.faceDownCommandZoneCards.add(entry.getCard().getId());
        }
    }
}
