package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss by the owner of the permanent that caused the trigger. */
@Component
@RequiredArgsConstructor
public class LoseLifeToOwnerOfTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeToOwnerOfTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringPermanent = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringPermanent == null || triggeringPermanent.getOwnerId() == null) {
            return;
        }

        lifeSupport.applyLifeLoss(gameData, triggeringPermanent.getOwnerId(), entry.getEventValue(),
                entry.getCard().getName());
    }
}
