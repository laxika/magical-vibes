package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardFromTargetHandForValkiEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentRevealsHandAndExilesCreatureCardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EachOpponentRevealsHandAndExilesCreatureCardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentRevealsHandAndExilesCreatureCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> choices = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .map(ChooseCreatureCardFromTargetHandForValkiEffect::new)
                .map(effectToResolve -> (CardEffect) effectToResolve)
                .toList();
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (!choices.isEmpty() && effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, choices);
        }
    }
}
