package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumSpellCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class MinimumSpellCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MinimumSpellCostEffect.class;
    }

    @Override
    public boolean appliesAfterOtherCostModifiers() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyCostAfterOtherModifiers(CostModificationContext context, CardEffect effect,
                                              CostModificationSource source, int accumulatedModifier) {
        if (source.sourcePermanent() == null || source.sourcePermanent().isTapped()) {
            return 0;
        }
        ManaCost manaCost = context.spell().getParsedManaCost();
        if (manaCost == null) {
            return 0;
        }
        int manaComponent = manaCost.getManaValue();
        if (manaCost.hasX()) {
            manaComponent += context.xValue() * manaCost.getXSymbolCount();
        }
        int minimumMana = ((MinimumSpellCostEffect) effect).minimumMana();
        return Math.max(0, minimumMana - manaComponent - accumulatedModifier);
    }
}
