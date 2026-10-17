package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillAsEntersEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MillAsEntersEffectHandler implements EntryReplacementHandlerBean {

    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillAsEntersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect, int xValue, boolean kicked) {
        MillAsEntersEffect mill = (MillAsEntersEffect) effect;
        if (mill.onlyIfKicked() && !kicked) {
            return;
        }
        graveyardService.resolveMillPlayer(gameData, controllerId, mill.count());
    }
}
