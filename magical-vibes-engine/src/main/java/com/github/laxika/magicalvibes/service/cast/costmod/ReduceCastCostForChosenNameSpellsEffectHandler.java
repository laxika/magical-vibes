package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenNameSpellsEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class ReduceCastCostForChosenNameSpellsEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForChosenNameSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId()) || source.sourcePermanent() == null) {
            return 0;
        }
        String chosenName = source.sourcePermanent().getChosenName();
        if (chosenName == null || !chosenName.equals(context.spell().getName())) {
            return 0;
        }
        return -((ReduceCastCostForChosenNameSpellsEffect) effect).amount();
    }
}
