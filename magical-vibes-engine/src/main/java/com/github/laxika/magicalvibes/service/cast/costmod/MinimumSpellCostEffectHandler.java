package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCastingCost;
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
        if (context.flashbackCost() && context.spell().effectiveFlashbackCast().isPresent()) {
            manaCost = context.spell().effectiveFlashbackCast()
                    .flatMap(flashback -> flashback.getCost(ManaCastingCost.class))
                    .map(cost -> new ManaCost(cost.manaCost()))
                    .orElseGet(() -> new ManaCost(""));
        }
        if (manaCost == null) {
            return 0;
        }
        int manaComponent = manaCost.getManaValue();
        if (manaCost.hasX()) {
            manaComponent += context.xValue() * manaCost.getXSymbolCount();
        }
        // CR 601.2f: the total cost includes additional costs such as buyback before total-cost effects apply,
        // while life paid for Phyrexian mana is not mana (the offset is negative per symbol paid with life).
        manaComponent += context.minimumManaOffset();
        int minimumMana = ((MinimumSpellCostEffect) effect).minimumMana();
        return Math.max(0, minimumMana - manaComponent - accumulatedModifier);
    }
}
