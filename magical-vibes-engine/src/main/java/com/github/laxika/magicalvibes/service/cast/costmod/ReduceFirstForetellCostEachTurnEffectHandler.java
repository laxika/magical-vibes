package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstForetellCostEachTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Handles once-per-turn foretell cost reductions such as Ranar's. */
@Component
public class ReduceFirstForetellCostEachTurnEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceFirstForetellCostEachTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyForetellCost(GameData gameData, UUID playerId, CardEffect effect,
                                  CostModificationSource source) {
        if (!source.controlledBy(playerId)) {
            return 0;
        }
        return gameData.playersWhoForetoldThisTurn.contains(playerId)
                ? 0
                : -((ReduceFirstForetellCostEachTurnEffect) effect).amount();
    }
}
