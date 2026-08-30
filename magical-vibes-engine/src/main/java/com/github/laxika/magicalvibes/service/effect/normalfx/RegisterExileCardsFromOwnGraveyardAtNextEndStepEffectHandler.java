package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExileCardsFromOwnGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegisterExileCardsFromOwnGraveyardAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(new ExileCardsFromOwnGraveyardAtNextEndStep(
                controllerId, e.count(), entry.getCard()));
        log.info("Game {} - {} registers delayed graveyard exile of {} card(s) at next end step",
                gameData.id, gameData.playerIdToName.get(controllerId), e.count());
    }
}
