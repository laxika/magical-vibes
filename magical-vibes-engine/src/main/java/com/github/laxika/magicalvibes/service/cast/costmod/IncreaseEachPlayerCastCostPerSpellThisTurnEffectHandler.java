package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseEachPlayerCastCostPerSpellThisTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var tax = (IncreaseEachPlayerCastCostPerSpellThisTurnEffect) effect;
        return tax.amountPerSpell()
                * context.gameData().getSpellsCastThisTurnCount(context.castingPlayerId());
    }
}
