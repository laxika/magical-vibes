package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostByManaCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

/** Handles spell-self reductions that remove colored mana symbols. */
@Component
public class ReduceOwnCastCostByManaCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostByManaCostEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public ManaCost coloredManaCostReduction(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source) {
        return ((ReduceOwnCastCostByManaCostEffect) effect).reduction();
    }
}
