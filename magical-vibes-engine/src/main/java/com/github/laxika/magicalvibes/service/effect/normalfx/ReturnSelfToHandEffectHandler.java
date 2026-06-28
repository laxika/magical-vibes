package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnSelfToHandEffectHandler implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSelfToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        bounceSupport.applyReturnSelfToHand(gameData, entry);
    }
}
