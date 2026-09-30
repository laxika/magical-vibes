package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffectHandler
        implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect,
                          CostModificationSource source) {
        return 0;
    }

    @Override
    public ManaCost coloredManaCostReduction(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId())
                || !context.castingPlayerId().equals(context.gameData().activePlayerId)
                || context.targetIds().stream().noneMatch(targetId -> isCreaturePermanent(context, targetId))) {
            return null;
        }
        if (context.gameData().hasSpellCastTargetingCreatureThisTurn(context.castingPlayerId())) {
            return null;
        }
        return ((ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffect) effect).reduction();
    }

    private boolean isCreaturePermanent(CostModificationContext context, UUID targetId) {
        return targetId != null && context.gameData().playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(permanent -> permanent.getId().equals(targetId)
                        && permanent.getCard().hasType(CardType.CREATURE));
    }
}
