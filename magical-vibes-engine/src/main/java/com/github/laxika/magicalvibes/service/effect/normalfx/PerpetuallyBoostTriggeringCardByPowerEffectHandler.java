package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
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

        // The card's current power, including characteristic-defining power while it is still a spell
        // (Maro) and any perpetual modifiers it already carries.
        Integer currentPower = gameQueryService.getEffectiveCardPower(gameData, triggeringCard);
        int power = Math.max(0, currentPower == null ? 0 : currentPower);
        PerpetualCardPowerToughnessSupport.remember(gameData, triggeringCard, power, power);
    }
}
