package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToLifeTotalAsEntersEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoseLifeEqualToLifeTotalAsEntersEffectHandler implements EntryReplacementHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEqualToLifeTotalAsEntersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        lifeSupport.applyLifeLoss(gameData, controllerId, gameData.getLife(controllerId),
                enteringPermanent.getCard().getName());
    }
}
