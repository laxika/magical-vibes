package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardByPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a perpetual +X/+X modification using the triggering card's power. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostTriggeringCardByPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTriggeringCardByPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringCard == null) {
            return;
        }

        int power = triggeringCard.getPower() == null ? 0 : triggeringCard.getPower();
        CardPowerToughnessModifier existingModifier =
                gameData.perpetualCardPowerToughnessModifiers.get(triggeringCard.getId());
        if (existingModifier != null) {
            power += existingModifier.power();
        }
        power = Math.max(0, power);
        PerpetualCardPowerToughnessSupport.remember(gameData, triggeringCard, power, power);
    }
}
