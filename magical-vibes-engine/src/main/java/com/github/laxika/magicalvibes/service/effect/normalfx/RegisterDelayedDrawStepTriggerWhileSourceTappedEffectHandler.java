package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedDrawStepTriggerWhileSourceTapped;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDrawStepTriggerWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates the repeating draw-step delayed trigger of {@link RegisterDelayedDrawStepTriggerWhileSourceTappedEffect}
 * for the resolving ability's target, its controller the ability's (CR 603.7e). A source that has untapped or left
 * the battlefield since the ability was activated has already ended the duration, so nothing is created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDrawStepTriggerWhileSourceTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDrawStepTriggerWhileSourceTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedDrawStepTriggerWhileSourceTappedEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || sourceSnapshot == null || target == null
                || source.getUntapSequence() != sourceSnapshot.getUntapSequence()) {
            return;
        }
        gameData.queueDelayedAction(new DelayedDrawStepTriggerWhileSourceTapped(entry.getControllerId(),
                entry.getCard(), source.getId(), source.getUntapSequence(), target.getId(), e.triggerEffect()));
        log.info("Game {} - {} creates a draw-step delayed trigger for {}", gameData.id,
                entry.getCard().getName(), target.getCard().getName());
    }
}
