package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemAsEntersEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateEmblemEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateEmblemAsEntersEffectHandler implements EntryReplacementHandlerBean {

    private final CreateEmblemEffectHandler createEmblemEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateEmblemAsEntersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        createEmblemEffectHandler.createEmblem(gameData, controllerId,
                enteringPermanent.getCard().getName(), ((CreateEmblemAsEntersEffect) effect).emblem());
    }
}
