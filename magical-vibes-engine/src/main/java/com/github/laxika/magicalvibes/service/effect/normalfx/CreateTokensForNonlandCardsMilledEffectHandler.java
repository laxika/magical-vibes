package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForNonlandCardsMilledEffect;
import org.springframework.stereotype.Component;

/** Resolves the nonland-card count captured by the mill trigger into token creation. */
@Component
public class CreateTokensForNonlandCardsMilledEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    public CreateTokensForNonlandCardsMilledEffectHandler(
            CreateTokenEffectHandler createTokenEffectHandler) {
        this.createTokenEffectHandler = createTokenEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForNonlandCardsMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensForNonlandCardsMilledEffect e = (CreateTokensForNonlandCardsMilledEffect) effect;
        createTokenEffectHandler.resolveForController(gameData, entry,
                e.tokenTemplate().withAmount(Math.max(0, entry.getEventValue())), entry.getControllerId());
    }
}
