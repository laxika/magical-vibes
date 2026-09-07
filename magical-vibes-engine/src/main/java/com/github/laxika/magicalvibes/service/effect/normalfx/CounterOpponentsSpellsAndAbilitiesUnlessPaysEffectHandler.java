package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterOpponentsSpellsAndAbilitiesUnlessPaysEffectHandler
        implements NormalEffectHandlerBean {

    private final WhirlwindDenialSupport whirlwindDenialSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        whirlwindDenialSupport.begin(
                gameData, entry, (CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect) effect);
    }
}
