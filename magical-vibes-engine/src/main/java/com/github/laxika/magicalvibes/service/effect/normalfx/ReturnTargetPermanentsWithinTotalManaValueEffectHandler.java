package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentsWithinTotalManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentsWithinTotalManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnTargetPermanentsWithinTotalManaValueEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        int totalManaValue = 0;
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || (returnEffect.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, target, returnEffect.filter()))) {
                continue;
            }
            int manaValue = target.getCard().getManaValue();
            if (totalManaValue + manaValue > returnEffect.maxTotalManaValue()) {
                continue;
            }
            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                totalManaValue += manaValue;
                gameLogService.append(gameData,
                        GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
