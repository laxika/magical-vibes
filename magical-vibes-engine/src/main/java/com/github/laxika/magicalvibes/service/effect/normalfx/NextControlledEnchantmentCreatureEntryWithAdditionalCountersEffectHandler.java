package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect counters =
                (NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect) effect;
        gameData.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn.merge(
                entry.getControllerId(), counters.count(), Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                "The next enchantment creature entering under your control this turn gets "
                        + counters.count() + " additional +1/+1 counter(s)."));
    }
}
