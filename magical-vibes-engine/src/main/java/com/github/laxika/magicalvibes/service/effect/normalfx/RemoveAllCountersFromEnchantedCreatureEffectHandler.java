package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || aura.getAttachedTo() == null) {
            entry.setEventValue(0);
            return;
        }
        UUID enchantedId = aura.getAttachedTo();
        Permanent target = gameQueryService.findPermanentById(gameData, enchantedId);
        if (target == null) {
            entry.setEventValue(0);
            return;
        }

        int removed = 0;
        int oilRemoved = target.getCounterCount(CounterType.OIL);
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += target.getCounterCount(counterType);
            target.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(target, oilRemoved);
        entry.setEventValue(removed);

        gameLogService.append(gameData,
                GameLog.textCardText(removed + " counter(s) removed from ", target.getCard(), "."));
    }
}
