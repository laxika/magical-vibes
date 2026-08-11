package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DismantleEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DismantleEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DismantleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int counterCount = totalCounters(target);
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);

        if (counterCount > 0) {
            playerInputService.beginDismantleCounterTypeChoice(gameData, entry.getControllerId(), counterCount,
                    entry.getCard().getName());
        }
    }

    private int totalCounters(Permanent permanent) {
        int total = 0;
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER) {
                total += permanent.getCounterCount(counterType);
            }
        }
        return total;
    }
}
