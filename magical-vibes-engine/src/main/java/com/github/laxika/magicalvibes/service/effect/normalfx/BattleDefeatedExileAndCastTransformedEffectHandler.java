package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BattleDefeatedExileAndCastTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BattleDefeatedExileAndCastTransformedEffectHandler implements NormalEffectHandlerBean {

    private final BattleDefeatSupport battleDefeatSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BattleDefeatedExileAndCastTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        battleDefeatSupport.resolveDefeat(gameData, entry);
    }
}
