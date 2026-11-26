package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrPutCounterIfExactlyThreeColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostSelfOrPutCounterIfExactlyThreeColorsEffectHandler implements NormalEffectHandlerBean {

    private final BoostEffectHandler boostEffectHandler;
    private final PutCountersEffectHandler putCountersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfOrPutCounterIfExactlyThreeColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        long colorsSpent = entry.getActivationManaSpent().entrySet().stream()
                .filter(spent -> spent.getKey() != ManaColor.COLORLESS && spent.getValue() > 0)
                .count();
        if (colorsSpent == 3) {
            putCountersEffectHandler.resolve(gameData, entry, PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        } else {
            boostEffectHandler.resolve(gameData, entry, BoostEffect.self(1, 1));
        }
    }
}
