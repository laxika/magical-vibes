package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedControllerSpellCastTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedControllerSpellCastTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedControllerSpellCastTriggerEffect) effect;
        if (entry.getSourcePermanentId() == null && e.sourceMustRemainOnBattlefield()) {
            return;
        }
        gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                entry.getControllerId(),
                entry.getSourcePermanentId(),
                entry.getCard(),
                e.spellFilter(),
                e.resolvedEffects(),
                e.sourceMustRemainOnBattlefield()));
        log.info("Game {} - {} registers a delayed spell-cast trigger for this turn",
                gameData.id, entry.getCard().getName());
    }
}
