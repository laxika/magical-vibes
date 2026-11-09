package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSourceUntapsOrLeavesTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSourceUntapsOrLeavesTriggerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates the delayed trigger of {@link RegisterDelayedSourceUntapsOrLeavesTriggerEffect} for the resolving ability's
 * target, its controller the ability's (CR 603.7e). It is created while the source is on the battlefield, tapped or
 * not: an untapped source triggers it the next time it untaps (CR 603.7a). A source that has left the battlefield can
 * neither leave nor untap again, so nothing is created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSourceUntapsOrLeavesTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSourceUntapsOrLeavesTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedSourceUntapsOrLeavesTriggerEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }
        gameData.queueDelayedAction(new DelayedSourceUntapsOrLeavesTrigger(entry.getControllerId(),
                entry.getCard(), source.getId(), target.getId(), e.triggerEffect()));
        log.info("Game {} - {} creates an untaps-or-leaves delayed trigger for {}", gameData.id,
                entry.getCard().getName(), target.getCard().getName());
    }
}
