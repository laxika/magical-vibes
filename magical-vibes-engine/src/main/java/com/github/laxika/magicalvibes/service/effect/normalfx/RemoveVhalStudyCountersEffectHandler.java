package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveVhalStudyCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveVhalStudyCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SpecializeVhalEffectHandler specializeVhalEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveVhalStudyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemoveVhalStudyCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int removedStudyCounters = source.getCounterCount(CounterType.STUDY);
        source.setCounterCount(CounterType.STUDY, 0);
        specializeVhalEffectHandler.enqueueSpecializedTrigger(gameData, entry, source.getCard(),
                removal.color(), removedStudyCounters);
    }
}
