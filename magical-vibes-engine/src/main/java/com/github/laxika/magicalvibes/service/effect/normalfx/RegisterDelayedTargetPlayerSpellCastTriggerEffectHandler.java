package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTargetPlayerSpellCastTriggerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedTargetPlayerSpellCastTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedTargetPlayerSpellCastTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedTargetPlayerSpellCastTriggerEffect) effect;
        if (entry.getTargetId() == null
                || (entry.getSourcePermanentId() == null && e.sourceMustRemainOnBattlefield())) {
            return;
        }
        gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                entry.getTargetId(),
                entry.getSourcePermanentId(),
                entry.getCard(),
                e.spellFilter(),
                null,
                e.resolvedEffects(),
                e.oneShot(),
                e.sourceMustRemainOnBattlefield(),
                null,
                entry.getSourcePermanentSnapshot(),
                null,
                false,
                e.persistsUntilConsumed(),
                gameData.turnNumber));
        log.info("Game {} - {} registers a delayed spell-cast trigger for the targeted player",
                gameData.id, entry.getCard().getName());
    }
}
