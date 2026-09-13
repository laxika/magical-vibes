package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandAsEntersEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiscardHandEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscardHandAsEntersEffectHandler implements EntryReplacementHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardHandAsEntersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        discardHandEffectHandler.discardHand(gameData, controllerId, controllerId,
                enteringPermanent.getCard().getName());
    }
}
