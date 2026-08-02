package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvolveTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EvolveTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTriggeringPermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int enteringPower = entering != null
                ? gameQueryService.getEffectivePower(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentPowerAtTrigger());
        int enteringToughness = entering != null
                ? gameQueryService.getEffectiveToughness(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentToughnessAtTrigger());

        if (enteringPower <= gameQueryService.getEffectivePower(gameData, source)
                && enteringToughness <= gameQueryService.getEffectiveToughness(gameData, source)) {
            return;
        }

        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, source, 1);
    }

    private static int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
