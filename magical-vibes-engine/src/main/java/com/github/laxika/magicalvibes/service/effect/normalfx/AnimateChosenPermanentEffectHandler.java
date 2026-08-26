package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnimateChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimateChosenPermanentEffect) effect;
        animationSupport.animateChosen(gameData, entry, e.animation());
    }
}
