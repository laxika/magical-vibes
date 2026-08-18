package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDiscardAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterDiscardAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDiscardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDiscardAtNextEndStepEffect) effect;
        gameData.queueDelayedAction(new DiscardCardsAtNextEndStep(
                entry.getControllerId(), e.count(), entry.getCard()));
        log.info("Game {} - {} registers delayed discard of {} card(s) at next end step",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
