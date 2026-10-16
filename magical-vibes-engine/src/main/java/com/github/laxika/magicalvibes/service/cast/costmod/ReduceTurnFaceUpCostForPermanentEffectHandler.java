package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceTurnFaceUpCostForPermanentEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies a turn-face-up cost adjustment to the permanent it targets. */
@Component
public class ReduceTurnFaceUpCostForPermanentEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceTurnFaceUpCostForPermanentEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect,
                          CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyTurnFaceUpCost(CostModificationContext context, CardEffect effect,
                                    CostModificationSource source) {
        ReduceTurnFaceUpCostForPermanentEffect reduction =
                (ReduceTurnFaceUpCostForPermanentEffect) effect;
        if (!reduction.permanentId().equals(context.turnFaceUpPermanentId())
                || losesAllAbilities(context.gameData(), reduction.permanentId())) {
            return 0;
        }
        return reduction.amount();
    }

    private static boolean losesAllAbilities(GameData gameData, UUID permanentId) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getId().equals(permanentId))
                .anyMatch(Permanent::isLosesAllAbilitiesUntilEndOfTurn);
    }
}
