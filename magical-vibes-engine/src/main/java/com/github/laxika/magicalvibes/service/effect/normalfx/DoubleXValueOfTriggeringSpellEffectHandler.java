package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleXValueOfTriggeringSpellEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DoubleXValueOfTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleXValueOfTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        for (StackEntry stackEntry : gameData.stack) {
            if (triggeringCardId.equals(stackEntry.getTargetableId())
                    && stackEntry.getCard() != null
                    && stackEntry.getCard().getParsedManaCost() != null
                    && stackEntry.getCard().getParsedManaCost().hasX()) {
                stackEntry.setXValue(stackEntry.getXValue() * 2);
                return;
            }
        }
    }
}
