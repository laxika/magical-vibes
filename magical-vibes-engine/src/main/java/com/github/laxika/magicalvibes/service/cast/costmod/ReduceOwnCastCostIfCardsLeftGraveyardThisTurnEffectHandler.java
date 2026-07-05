package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect) effect;
        return context.gameData().playersWhoseCardsLeftGraveyardThisTurn.contains(context.castingPlayerId())
                ? -reduce.amount() : 0;
    }
}
