package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceGraveyardSpellCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

/**
 * Applies {@link ReduceGraveyardSpellCastCostEffect} only while the spell is being cast from a
 * graveyard by the source's controller ("Spells you cast from your graveyard cost {1} less").
 */
@Component
public class ReduceGraveyardSpellCastCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceGraveyardSpellCastCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!context.fromGraveyard() || !source.controlledBy(context.castingPlayerId())) {
            return 0;
        }
        return -((ReduceGraveyardSpellCastCostEffect) effect).amount();
    }
}
