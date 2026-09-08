package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndStepDrawRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegisterDrawCardsAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDrawCardsAtNextEndStepEffect) effect;
        UUID drawerId = e.recipient() == EndStepDrawRecipient.TRIGGERING_PLAYER
                ? entry.getTargetId()
                : entry.getControllerId();
        if (drawerId == null) {
            return;
        }
        gameData.queueDelayedAction(new DrawCardsAtNextEndStep(drawerId, e.count(), entry.getCard()));
        log.info("Game {} - {} registers delayed draw of {} at next end step",
                gameData.id, gameData.playerIdToName.get(drawerId), e.count());
    }
}
