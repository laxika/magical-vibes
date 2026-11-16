package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScheduleCreatedPermanentsEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;

@Component
public class ScheduleCreatedPermanentsEffectHandler implements NormalEffectHandlerBean {
    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScheduleCreatedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var schedule = (ScheduleCreatedPermanentsEffect) effect;
        var scheduledController = schedule.controllerOnly() ? entry.getControllerId() : null;
        for (var permanentId : entry.getCreatedPermanentIds()) {
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(permanentId, schedule.action(), false, null, !schedule.controllerOnly()),
                    schedule.controllerOnly() ? ActivePlayer.YOU : ActivePlayer.ANY);
        }
    }
}
