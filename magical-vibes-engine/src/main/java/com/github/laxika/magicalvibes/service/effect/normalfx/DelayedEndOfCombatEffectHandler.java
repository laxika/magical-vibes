package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Schedules a triggered ability for the beginning of the end-of-combat step. */
@Component
@RequiredArgsConstructor
public class DelayedEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DelayedEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DelayedEndOfCombatEffect delayedEffect = (DelayedEndOfCombatEffect) effect;
        gameData.queueDelayedAction(new DelayedEndOfCombatTrigger(
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), delayedEffect.effect()));
    }
}
