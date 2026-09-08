package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForetellCostReductionEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Handles foretell special-action cost reductions and timing permissions. */
@Component
public class ForetellCostReductionEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForetellCostReductionEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyForetellCost(GameData gameData, UUID playerId, CardEffect effect,
                                  CostModificationSource source) {
        ForetellCostReductionEffect reduction = (ForetellCostReductionEffect) effect;
        return source.controlledBy(playerId) ? -reduction.amount() : 0;
    }

    @Override
    public boolean allowsForetellDuringAnyTurn(GameData gameData, UUID playerId, CardEffect effect,
                                               CostModificationSource source) {
        ForetellCostReductionEffect reduction = (ForetellCostReductionEffect) effect;
        return reduction.allowDuringAnyTurn() && source.controlledBy(playerId);
    }
}
