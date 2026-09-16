package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.LegacyWordSupport;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenLegacyWordSpellsEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

/** Applies Inspirational Antelope's chosen-word reduction to spells cast by its controller. */
@Component
public class ReduceCastCostForChosenLegacyWordSpellsEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForChosenLegacyWordSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId()) || source.sourcePermanent() == null) {
            return 0;
        }
        String chosenWord = context.gameData().legacyChosenWordsByCardId
                .get(source.sourcePermanent().getCard().getId());
        if (!LegacyWordSupport.cardHasWord(context.spell(), chosenWord)) {
            return 0;
        }
        return -((ReduceCastCostForChosenLegacyWordSpellsEffect) effect).amount();
    }
}
