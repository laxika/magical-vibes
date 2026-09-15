package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantRandomKeywordEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class GrantRandomKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GrantKeywordEffectHandler grantKeywordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantRandomKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantRandomKeywordEffect randomEffect = (GrantRandomKeywordEffect) effect;
        int chosenIndex = ThreadLocalRandom.current().nextInt(randomEffect.options().size());
        grantKeywordEffectHandler.resolve(gameData, entry,
                new GrantKeywordEffect(randomEffect.options().get(chosenIndex), randomEffect.scope()));
    }
}
