package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.cast.CostModificationSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffectHandler implements CostModificationHandlerBean {

    private final CostModificationSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect) effect;
        return support.anyOpponentControlsAtLeastNMoreCreatures(
                context.gameData(), context.castingPlayerId(), reduce.minimumCreatureDifference())
                ? -reduce.amount() : 0;
    }
}
