package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The single spell-self cost-reduction handler: evaluates the effect's {@link ReduceOwnCastCostEffect#amount()}
 * against a cast-time {@link AmountContext} and returns it as a negative generic-mana delta.
 * Handles both fixed reductions ({@code Fixed}) and "for each …" reductions (counting amounts),
 * so it also serves as the delegate target for {@code ConditionalCostModificationHandler}.
 */
@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostEffectHandler implements CostModificationHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostEffect) effect;
        int amount = amountEvaluationService.evaluate(context.gameData(), reduce.amount(),
                AmountContext.forCasting(context.castingPlayerId()));
        return -amount;
    }
}
