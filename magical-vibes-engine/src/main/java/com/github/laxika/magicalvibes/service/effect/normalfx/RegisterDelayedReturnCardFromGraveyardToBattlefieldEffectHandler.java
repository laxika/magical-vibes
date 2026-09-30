package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterDelayedReturnCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayedReturn = (RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect) effect;
        UUID cardId = delayedReturn.cardId() != null ? delayedReturn.cardId() : entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldUnderControl(
                cardId,
                entry.getControllerId(),
                entry.getSourcePermanentId(),
                false,
                null,
                0,
                null,
                null,
                false,
                false));
    }
}
