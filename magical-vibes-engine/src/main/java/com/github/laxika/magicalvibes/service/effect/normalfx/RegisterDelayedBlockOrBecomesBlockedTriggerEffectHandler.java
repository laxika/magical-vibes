package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedBlockOrBecomesBlockedTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockOrBecomesBlockedTriggerEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterDelayedBlockOrBecomesBlockedTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedBlockOrBecomesBlockedTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        var e = (RegisterDelayedBlockOrBecomesBlockedTriggerEffect) effect;
        gameData.queueDelayedAction(new DelayedBlockOrBecomesBlockedTrigger(
                entry.getSourcePermanentId(), entry.getControllerId(), entry.getCard(), e.effect()));
        log.info("Game {} - {} registers a block-or-blocked delayed trigger for this combat",
                gameData.id, entry.getCard().getName());
    }
}
