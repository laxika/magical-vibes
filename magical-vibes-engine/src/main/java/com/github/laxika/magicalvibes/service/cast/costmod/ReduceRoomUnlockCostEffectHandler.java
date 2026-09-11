package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceRoomUnlockCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles generic mana reductions for Room-door unlock actions. */
@Component
public class ReduceRoomUnlockCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceRoomUnlockCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyRoomUnlockCost(GameData gameData, UUID playerId, Card room,
                                    CardEffect effect, CostModificationSource source) {
        return source.controlledBy(playerId) ? -((ReduceRoomUnlockCostEffect) effect).amount() : 0;
    }
}
