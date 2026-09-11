package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepIfAttackedEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Schedules Berserk's conditional destruction rider for the next end step. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetPermanentAtEndStepIfAttackedEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAtEndStepIfAttackedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        CardEffect destruction = ConditionalEffect.unless(
                new TargetPermanentMatches(new PermanentAttackedThisTurnPredicate()),
                new DestroyTargetPermanentEffect());
        gameData.queueDelayedAction(new DelayedEndStepTrigger(entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), target.getId(), destruction));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " will be destroyed at the beginning of the next end step if it attacked this turn."));
        log.info("Game {} - {} scheduled for conditional end-step destruction if it attacked",
                gameData.id, target.getCard().getName());
    }
}
