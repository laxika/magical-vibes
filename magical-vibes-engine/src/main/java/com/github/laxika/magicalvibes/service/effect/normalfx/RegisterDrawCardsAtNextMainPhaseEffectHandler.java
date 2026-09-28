package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextMainPhase;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextMainPhaseEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterDrawCardsAtNextMainPhaseEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextMainPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.queueDelayedAction(new DrawCardsAtNextMainPhase(entry.getControllerId(), entry.getCard()));
        log.info("Game {} - {} registers a delayed draw at their next main phase",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()));
    }
}
