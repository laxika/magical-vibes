package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCommanderCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles static reductions that apply to a specific controller's designated commander. */
@Component
@RequiredArgsConstructor
public class ReduceCommanderCastCostEffectHandler implements CostModificationHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCommanderCastCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (source.controllerId() == null || !isSourceControllerCommander(
                context.gameData(), source.controllerId(), context.spell())) {
            return 0;
        }

        ReduceCommanderCastCostEffect reduction = (ReduceCommanderCastCostEffect) effect;
        int amount = amountEvaluationService.evaluate(
                context.gameData(), reduction.amount(), AmountContext.forCasting(source.controllerId()));
        return -amount;
    }

    private boolean isSourceControllerCommander(GameData gameData, java.util.UUID controllerId, Card spell) {
        return gameData.playerCommanders.getOrDefault(controllerId, java.util.List.of()).stream()
                .anyMatch(commander -> commander.getId().equals(spell.getId())
                        || commander.getBackFaceCard() != null
                        && commander.getBackFaceCard().getId().equals(spell.getId()));
    }
}
