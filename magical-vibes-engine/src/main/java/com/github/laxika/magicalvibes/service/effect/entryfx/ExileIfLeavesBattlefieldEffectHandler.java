package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileIfLeavesBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExileIfLeavesBattlefieldEffectHandler implements EntryReplacementHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileIfLeavesBattlefieldEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        enteringPermanent.setExileIfLeavesBattlefield(true);
    }
}
