package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
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
    private final GameBroadcastService gameBroadcastService;

    /**
     * For each declared attacker carrying a {@link CantAttackUnlessSacrificeEffect}, sacrifices the
     * required matching permanents the controller controls. Called after all index-based combat
     * bookkeeping so removing the sacrificed permanents from the battlefield cannot shift indices.
     */
    public void paySacrificeAttackCosts(GameData gameData, UUID playerId, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        // Collect the costs first (by index, before any battlefield mutation), then pay them.
        List<CantAttackUnlessSacrificeEffect> costs = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantAttackUnlessSacrificeEffect sac) {
                    costs.add(sac);
                }
            }
        }

        for (CantAttackUnlessSacrificeEffect cost : costs) {
            sacrificeMatching(gameData, playerId, cost.count(), cost.filter());
        }
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
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
            log.info("Game {} - {} sacrifices {} to attack", gameData.id, playerName,
                    toSacrifice.getCard().getName());
            sacrificed++;
        }
    }
}
