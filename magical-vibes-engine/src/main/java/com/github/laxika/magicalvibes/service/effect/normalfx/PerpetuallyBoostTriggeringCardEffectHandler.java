package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records a perpetual power/toughness boost on the card that caused the surrounding trigger. */
@Component
public class PerpetuallyBoostTriggeringCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        var boost = (PerpetuallyBoostTriggeringCardEffect) effect;
        gameData.perpetualCardPowerToughnessModifiers.merge(
                cardId,
                new GameData.PerpetualPowerToughnessModifier(boost.powerBoost(), boost.toughnessBoost()),
                (oldValue, newValue) -> new GameData.PerpetualPowerToughnessModifier(
                        oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
    }
}
