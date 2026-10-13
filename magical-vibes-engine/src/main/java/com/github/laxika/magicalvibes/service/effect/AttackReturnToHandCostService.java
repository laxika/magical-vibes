package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final PlayerInputService playerInputService;

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
                                           List<Permanent> attackers) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        Map<PermanentPredicate, Integer> totals = new LinkedHashMap<>();
        for (Permanent attacker : attackers) {
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantAttackUnlessReturnToHandEffect returnCost) {
                    totals.merge(returnCost.filter(), returnCost.count(), Integer::sum);
                }
            }
        }

        // Costs with the same filter are paid as one return so that every required permanent is chosen
        // together; separate prompts would replace one another and drop the later costs.
        for (Map.Entry<PermanentPredicate, Integer> total : totals.entrySet()) {
            returnMatching(gameData, playerId, total.getValue(), total.getKey());
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
        List<Permanent> candidates = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, filter))
                .toList();
        if (candidates.size() > count) {
            playerInputService.beginMultiPermanentChoice(gameData, playerId,
                    candidates.stream().map(Permanent::getId).toList(), count,
                    new MultiPermanentChoiceContext.ReturnToHandAttackCost(count),
                    "Choose " + count + " permanent" + (count == 1 ? "" : "s")
                            + " to return to hand as an attack cost.");
            return;
        }

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
