package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeDyingSourceCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DistributeDyingSourceCountersAmongControlledCreaturesEffect} from the
 * {@code pendingETBDamageAssignments} buffer. Skips player ids and permanents the controller does
 * not control or that are no longer creatures.
 */
@Component
@RequiredArgsConstructor
public class DistributeDyingSourceCountersAmongControlledCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeDyingSourceCountersAmongControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeDyingSourceCountersAmongControlledCreaturesEffect) effect;
        Map<UUID, Integer> assignments = gameData.pendingETBDamageAssignments;
        gameData.pendingETBDamageAssignments = Map.of();
        if (e.count() <= 0 || assignments.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Integer amount = assignment.getValue();
            if (amount == null || amount <= 0) {
                continue;
            }
            Permanent target = null;
            for (Permanent p : battlefield) {
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
}
