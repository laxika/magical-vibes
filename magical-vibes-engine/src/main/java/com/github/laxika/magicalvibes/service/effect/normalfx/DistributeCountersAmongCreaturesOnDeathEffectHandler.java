package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DistributeCountersAmongCreaturesOnDeathEffect} from the division announced on the
 * stack entry as the ability went on the stack; nothing is placed when none was announced. A
 * recipient that is no longer a legal target isn't affected (CR 608.2b): it must still be a creature,
 * be targetable by the ability, and, when the effect is controller-scoped, be controlled by the
 * distributing player ({@link #distributingPlayerId}).
 */
@Component
@RequiredArgsConstructor
public class DistributeCountersAmongCreaturesOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeCountersAmongCreaturesOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeCountersAmongCreaturesOnDeathEffect) effect;
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments.isEmpty()) {
            return;
        }

        UUID distributorId = distributingPlayerId(e, entry);
        List<Permanent> eligible = eligiblePermanents(gameData, distributorId, !e.controlledCreaturesOnly());
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
            if (targetLegalityService.checkTriggeredPermanentTargetableReason(
                    gameData, target, entry.getCard(), distributorId).isPresent()) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, e.counterType(), amount);
        }
    }

    /**
     * The player who chooses the targets and the division and whose creatures "you control" means:
     * the ability's controller, or for {@code CREATURES_ITS_CONTROLLER_CONTROLS} the controller of the
     * creature that died (Grand Ossuary, whose ability stays under the planar controller, CR 901.6).
     */
    public static UUID distributingPlayerId(DistributeCountersAmongCreaturesOnDeathEffect effect, StackEntry entry) {
        if (effect.recipients() == DistributeCountersAmongCreaturesOnDeathEffect.Recipients.CREATURES_ITS_CONTROLLER_CONTROLS
                && entry.getTriggeringPermanentControllerId() != null) {
            return entry.getTriggeringPermanentControllerId();
        }
        return entry.getControllerId();
    }

    private List<Permanent> eligiblePermanents(GameData gameData, UUID controllerId, boolean anyCreature) {
        if (!anyCreature) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            return battlefield == null ? List.of() : battlefield;
        }
        List<Permanent> all = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            all.addAll(battlefield);
        }
        return all;
    }
}
