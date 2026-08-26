package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTotalDyingCreaturesPowerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token whose base power and toughness were snapshotted from a death event. */
@Component
@RequiredArgsConstructor
public class CreateTokenWithTotalDyingCreaturesPowerEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithTotalDyingCreaturesPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createToken = (CreateTokenWithTotalDyingCreaturesPowerEffect) effect;
        createTokenEffectHandler.resolve(gameData, entry,
                createToken.tokenTemplate().withPowerToughness(entry.getEventValue(), entry.getEventValue()));
    }
}
