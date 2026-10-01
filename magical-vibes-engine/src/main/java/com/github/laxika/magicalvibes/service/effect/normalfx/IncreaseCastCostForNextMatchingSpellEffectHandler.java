package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.IncreaseCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("increaseCastCostForNextMatchingSpellNormalEffectHandler")
public class IncreaseCastCostForNextMatchingSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseCastCostForNextMatchingSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var increase = (IncreaseCastCostForNextMatchingSpellEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                new IncreaseCastCostForNextMatchingSpellEffect(
                        increase.predicate(), increase.amount(), entry.getTargetId()),
                null,
                null,
                null,
                EffectDuration.UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST,
                0
        ));
    }
}
