package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BushidoEffectHandler implements NormalEffectHandlerBean {

    private final BoostEffectHandler boostEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BushidoEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boostEffectHandler.resolve(gameData, entry, ((BushidoEffect) effect).asBoost());
    }
}
