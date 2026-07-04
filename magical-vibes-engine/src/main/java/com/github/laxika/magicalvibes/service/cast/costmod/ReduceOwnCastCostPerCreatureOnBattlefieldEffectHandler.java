package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.cast.CostModificationSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostPerCreatureOnBattlefieldEffectHandler implements CostModificationHandlerBean {

    private final CostModificationSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostPerCreatureOnBattlefieldEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostPerCreatureOnBattlefieldEffect) effect;
        return -reduce.amountPerCreature() * support.countCreaturesOnAllBattlefields(context.gameData());
    }
}
