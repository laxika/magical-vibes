package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetPermanentAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentAtEndStepEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                gameData.pendingDestroyAtEndStep.add(target.getId());

                String logEntry = target.getCard().getName() + " will be destroyed at the beginning of the next end step.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} scheduled for destruction at end step", gameData.id, target.getCard().getName());
    }
}
