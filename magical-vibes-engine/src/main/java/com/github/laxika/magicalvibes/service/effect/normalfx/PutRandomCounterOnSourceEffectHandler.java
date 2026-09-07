package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCounterOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PutRandomCounterOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCounterOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutRandomCounterOnSourceEffect randomEffect = (PutRandomCounterOnSourceEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<CounterType> missingCounterTypes =
                randomEffect.counterTypes().stream()
                        .filter(counterType -> source.getCounterCount(counterType) == 0)
                        .toList();
        if (missingCounterTypes.isEmpty()) {
            return;
        }

        int chosenIndex = ThreadLocalRandom.current().nextInt(missingCounterTypes.size());
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, source, missingCounterTypes.get(chosenIndex), 1);
    }
}
