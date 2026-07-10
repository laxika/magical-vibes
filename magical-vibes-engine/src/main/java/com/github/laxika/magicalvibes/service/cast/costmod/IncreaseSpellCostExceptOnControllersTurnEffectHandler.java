package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostExceptOnControllersTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class IncreaseSpellCostExceptOnControllersTurnEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseSpellCostExceptOnControllersTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var tax = (IncreaseSpellCostExceptOnControllersTurnEffect) effect;
        boolean castersTurn = context.castingPlayerId().equals(context.gameData().activePlayerId);
        return castersTurn ? 0 : tax.amount();
    }
}
