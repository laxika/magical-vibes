package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConniveConvokeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConniveConvokeCreaturesEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConniveConvokeCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> conniveEffects = entry.getConvokeCreatureIds().stream()
                .map(id -> (CardEffect) DrawDiscardAndConniveEffect.forPermanent(id))
                .toList();
        if (!conniveEffects.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, conniveEffects);
        }
    }
}
