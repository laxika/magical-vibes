package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.beginInitialChoice(gameData, entry,
                (EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect) effect);
    }
}
