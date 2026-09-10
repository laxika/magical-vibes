package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCounterPTEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token whose base power and toughness were snapshotted from a dying creature's counters. */
@Component
@RequiredArgsConstructor
public class CreateTokenWithDyingSourceCounterPTEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithDyingSourceCounterPTEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var create = (CreateTokenWithDyingSourceCounterPTEffect) effect;
        int counters = create.counters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0);
        createTokenEffectHandler.resolve(gameData, entry,
                create.tokenTemplate().withPowerToughness(counters, counters));
    }
}
