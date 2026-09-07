package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IncreaseSpellCostUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseSpellCostUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var increase = (IncreaseSpellCostUntilNextTurnEffect) effect;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                new IncreaseSpellCostEffect(
                        increase.predicate(), increase.amount(), CostModificationScope.OPPONENT),
                null,
                null,
                null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN,
                0
        ));
    }
}
