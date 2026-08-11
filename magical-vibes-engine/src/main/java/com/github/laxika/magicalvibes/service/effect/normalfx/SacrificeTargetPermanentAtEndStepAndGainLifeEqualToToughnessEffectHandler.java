package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (!entry.getControllerId().equals(targetControllerId)) {
            return;
        }

        gameData.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStep(
                target.getId(), entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " will be sacrificed at the beginning of the next end step."));
        log.info("Game {} - {} scheduled for sacrifice and life gain at end step",
                gameData.id, target.getCard().getName());
    }
}
