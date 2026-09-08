package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedChooseOpponentGainsControlOfSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import org.springframework.stereotype.Component;

/** Registers Rainbow Vale's next-end-step control-change trigger. */
@Component
public class RegisterDelayedChooseOpponentGainsControlOfSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedChooseOpponentGainsControlOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.queueDelayedAction(new DelayedChooseOpponentGainsControlOfSource(
                entry.getControllerId(), entry.getSourcePermanentId(), entry.getCard()));
    }
}
