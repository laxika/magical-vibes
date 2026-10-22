package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnFlashbackCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Spell-self handler for {@link ReduceOwnFlashbackCostEffect}: returns the evaluated amount as a negative
 * generic-mana delta only when the spell is being cast for its flashback cost.
 */
@Component
@RequiredArgsConstructor
public class ReduceOwnFlashbackCostEffectHandler implements CostModificationHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnFlashbackCostEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!context.flashbackCost()) {
            return 0;
        }
        var reduce = (ReduceOwnFlashbackCostEffect) effect;
        int amount = amountEvaluationService.evaluate(context.gameData(), reduce.amount(),
                AmountContext.forCasting(context.castingPlayerId(), 0, context.spell()));
        return -amount;
    }
}
