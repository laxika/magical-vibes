package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pays static attack costs that return controller-chosen permanents to their owners' hands.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttackReturnToHandCostService {

    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    public void validateReturnToHandAttackCosts(GameData gameData, UUID playerId,
                                                List<Integer> attackerIndices) {
        collectCosts(gameData, playerId, attackerIndices).forEach((filter, required) -> {
            if (countMatching(gameData, playerId, filter) < required) {
                throw new IllegalStateException(
                        "Not enough permanents to return to hand (" + required + " required)");
            }
        });
    }

    public void payReturnToHandAttackCosts(GameData gameData, UUID playerId,
                                           List<Integer> attackerIndices) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        List<CantAttackUnlessReturnToHandEffect> costs = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantAttackUnlessReturnToHandEffect returnCost) {
                    costs.add(returnCost);
                }
            }
        }

        for (CantAttackUnlessReturnToHandEffect cost : costs) {
            returnMatching(gameData, playerId, cost.count(), cost.filter());
        }
    }

    private Map<PermanentPredicate, Integer> collectCosts(GameData gameData, UUID playerId,
                                                           List<Integer> attackerIndices) {
        Map<PermanentPredicate, Integer> totals = new LinkedHashMap<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return totals;
        }
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantAttackUnlessReturnToHandEffect returnCost) {
                    totals.merge(returnCost.filter(), returnCost.count(), Integer::sum);
                }
            }
        }
        return totals;
    }

    private int countMatching(GameData gameData, UUID playerId, PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                count++;
            }
        }
        return count;
    }

    private void returnMatching(GameData gameData, UUID playerId, int count,
                                PermanentPredicate filter) {
        String playerName = gameData.playerIdToName.get(playerId);
        int returned = 0;
        while (returned < count) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            Permanent toReturn = null;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                        toReturn = permanent;
                        break;
                    }
                }
            }
            if (toReturn == null) {
                break;
            }
            permanentRemovalService.removePermanentToHand(gameData, toReturn);
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " returns ", toReturn.getCard(), " to hand."));
            log.info("Game {} - {} returns {} to hand to attack", gameData.id, playerName,
                    toReturn.getCard().getName());
            returned++;
        }
    }
}
