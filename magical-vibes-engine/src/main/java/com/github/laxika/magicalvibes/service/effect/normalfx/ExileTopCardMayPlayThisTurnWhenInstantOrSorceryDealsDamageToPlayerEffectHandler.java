package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Syr Carah's instant-or-sorcery damage trigger. */
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileTopCardMayPlayThisTurnEffectHandler delegate;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        delegate.resolve(gameData, entry, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
