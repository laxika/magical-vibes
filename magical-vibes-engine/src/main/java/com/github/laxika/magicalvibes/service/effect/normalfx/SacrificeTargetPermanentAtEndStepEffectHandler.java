package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCoinFlipSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        SacrificeTargetPermanentAtEndStepEffect e = (SacrificeTargetPermanentAtEndStepEffect) effect;
        if (e.flipBeforeSacrificing()) {
            gameData.queueDelayedAction(new DelayedCoinFlipSacrificeTargetPermanentAtEndStep(
                    target.getId(), entry.getControllerId(), entry.getCard()));
        } else {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    target.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }

        String timingText = e.flipBeforeSacrificing()
                ? " will be subject to a coin flip at the beginning of the next end step."
                : " will be sacrificed at the beginning of the next end step.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), timingText));
        log.info("Game {} - {} scheduled for {} at end step", gameData.id, target.getCard().getName(),
                e.flipBeforeSacrificing() ? "coin flip and possible sacrifice" : "sacrifice");
    }
}
