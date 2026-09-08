package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect) effect;
        int manaValue = Math.max(0, entry.getEventValue());
        SacrificeSelfThenEffect materialized = new SacrificeSelfThenEffect(
                e.tokenTemplate().withAmount(manaValue));
        EffectHandler handler = effectHandlerRegistry.getHandler(materialized);
        if (handler != null) {
            handler.resolve(gameData, entry, materialized);
        }
    }
}
