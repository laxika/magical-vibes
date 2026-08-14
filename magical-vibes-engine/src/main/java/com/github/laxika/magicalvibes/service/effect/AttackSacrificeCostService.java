package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Pays {@link CantAttackUnlessSacrificeEffect} additional attack costs (Leviathan — "can't attack
 * unless you sacrifice two Islands"). Invoked from the combat declare-attackers commit phase once the
 * declaration is final. The legality that the controller can pay is enforced upstream by the paired
 * {@code CantAttackUnlessEffect} gate, so the required matching permanents are guaranteed present.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttackSacrificeCostService {

    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    /**
     * For each declared attacker carrying a {@link CantAttackUnlessSacrificeEffect}, sacrifices the
     * required matching permanents the controller controls. The attacker objects are captured before
     * payment begins, so removing permanents from the battlefield cannot invalidate the declaration.
     */
    public void paySacrificeAttackCosts(GameData gameData, UUID playerId, List<Permanent> attackers) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        // Collect the costs from the attacker snapshot before any battlefield mutation, then pay them.
        List<CantAttackUnlessSacrificeEffect> costs = new ArrayList<>();
        for (Permanent attacker : attackers) {
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantAttackUnlessSacrificeEffect sac) {
                    costs.add(sac);
                }
            }
        }

        for (CantAttackUnlessSacrificeEffect cost : costs) {
            sacrificeMatching(gameData, playerId, cost.count(), cost.filter());
        }

        collectGlobalCosts(gameData, playerId, attackers)
                .forEach((filter, count) -> sacrificeMatching(gameData, playerId, count, filter));
    }

    /**
     * Throws if the declaration can't pay its {@link CreaturesCantAttackUnlessSacrificeEffect} costs
     * (Flooded Woodlands). The cost is charged per matching attacker, so it must be validated for the
     * declaration as a whole — the per-creature gate in {@code AttackLegalityService} only guarantees
     * a single matching attacker is payable.
     */
    public void validateGlobalSacrificeAttackCosts(GameData gameData, UUID playerId, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }
        List<Permanent> attackers = attackerIndices.stream().map(battlefield::get).toList();
        collectGlobalCosts(gameData, playerId, attackers).forEach((filter, required) -> {
            if (countMatching(gameData, playerId, filter) < required) {
                throw new IllegalStateException(
                        "Not enough permanents to sacrifice to attack (" + required + " required)");
            }
        });
    }

    /**
     * Totals the sacrifice cost each global restriction charges this declaration, keyed by the filter
     * of the permanents to sacrifice. Restrictions are gathered from every battlefield, since the
     * effect applies from any permanent to every matching creature.
     */
    private Map<PermanentPredicate, Integer> collectGlobalCosts(GameData gameData, UUID playerId,
                                                                List<Permanent> attackers) {
        Map<PermanentPredicate, Integer> totals = new LinkedHashMap<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || attackers.isEmpty()) {
            return totals;
        }
        gameData.forEachPermanent((ownerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof CreaturesCantAttackUnlessSacrificeEffect restriction)) {
                    continue;
                }
                int matching = 0;
                for (Permanent attacker : attackers) {
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            gameData, attacker, restriction.attackerPredicate())) {
                        matching++;
                    }
                }
                if (matching > 0) {
                    totals.merge(restriction.sacrificeFilter(), matching * restriction.countPerAttacker(),
                            Integer::sum);
                }
            }
        });
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

    private void sacrificeMatching(GameData gameData, UUID playerId, int count, PermanentPredicate filter) {
        String playerName = gameData.playerIdToName.get(playerId);
        int sacrificed = 0;
        while (sacrificed < count) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            Permanent toSacrifice = null;
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter)) {
                        toSacrifice = p;
                        break;
                    }
                }
            }
            if (toSacrifice == null) {
                break;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
            log.info("Game {} - {} sacrifices {} to attack", gameData.id, playerName,
                    toSacrifice.getCard().getName());
            sacrificed++;
        }
    }
}
