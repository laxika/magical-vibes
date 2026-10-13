package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourcePowerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token whose base power and toughness were snapshotted from the dying creature's power. */
@Component
@RequiredArgsConstructor
public class CreateTokenWithDyingSourcePowerEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithDyingSourcePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int power = Math.max(0, entry.getEventValue());
        createTokenEffectHandler.resolve(gameData, entry,
                ((CreateTokenWithDyingSourcePowerEffect) effect).tokenTemplate().withPowerToughness(power, power));
    }
}
