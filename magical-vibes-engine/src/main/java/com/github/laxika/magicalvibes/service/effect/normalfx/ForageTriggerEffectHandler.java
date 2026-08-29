package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForageTriggerEffect;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForageTriggerEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForageTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        triggerCollectionService.checkForageTriggers(gameData, entry.getControllerId());
    }
}
