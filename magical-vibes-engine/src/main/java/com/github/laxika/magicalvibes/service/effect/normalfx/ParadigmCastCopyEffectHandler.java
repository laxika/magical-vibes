package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ParadigmCastCopyEffect;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParadigmCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final ParadigmService paradigmService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ParadigmCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        paradigmService.createCopyAndQueueMayCast(gameData, entry);
    }
}
