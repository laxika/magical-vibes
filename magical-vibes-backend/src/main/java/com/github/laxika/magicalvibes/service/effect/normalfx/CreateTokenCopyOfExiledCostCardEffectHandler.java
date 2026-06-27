package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCostCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfExiledCostCardEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfImprintedCardEffectHandler createTokenCopyOfImprintedCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfExiledCostCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        createTokenCopyOfImprintedCardEffectHandler.resolve(gameData, entry, new CreateTokenCopyOfImprintedCardEffect(false, false));
    }
}
