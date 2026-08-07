package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DistributeCountersAmongCreaturesOnDeathEffect} from the
 * {@code pendingETBDamageAssignments} buffer. Skips player ids and permanents that are no longer
 * creatures; when the effect is controller-scoped, also skips permanents the trigger's controller
 * does not control.
 */
@Component
@RequiredArgsConstructor
public class DistributeCountersAmongCreaturesOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeCountersAmongCreaturesOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeCountersAmongCreaturesOnDeathEffect) effect;
        Map<UUID, Integer> assignments = gameData.pendingETBDamageAssignments;
        gameData.pendingETBDamageAssignments = Map.of();
        if (e.count() <= 0 || assignments.isEmpty()) {
            return;
        }

        List<Permanent> eligible = eligiblePermanents(gameData, entry, e.anyCreature());
        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Integer amount = assignment.getValue();
            if (amount == null || amount <= 0) {
                continue;
            }
            Permanent target = null;
            for (Permanent p : eligible) {
                if (p.getId().equals(assignment.getKey())) {
                    target = p;
                    break;
                }
            }
            if (target == null) {
                continue;
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, e.counterType(), amount);
        }
    }

    private List<Permanent> eligiblePermanents(GameData gameData, StackEntry entry, boolean anyCreature) {
        if (!anyCreature) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            return battlefield == null ? List.of() : battlefield;
        }
        List<Permanent> all = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            all.addAll(battlefield);
        }
        return all;
    }
}
