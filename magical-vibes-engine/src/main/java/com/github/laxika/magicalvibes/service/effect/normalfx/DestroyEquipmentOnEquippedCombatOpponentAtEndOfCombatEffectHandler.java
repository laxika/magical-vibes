package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;
        var equipmentEffect = (DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect) effect;
        if (equipmentEffect.schedule()) {
            gameData.queueDelayedAction(new com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger(
                    entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), targetId,
                    new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect(false,
                            equipmentEffect.lastKnownEquipmentIds())));
            return;
        }
        java.util.List<UUID> equipmentIds = new java.util.ArrayList<>();
        Permanent creature = gameQueryService.findPermanentById(gameData, targetId);
        if (creature == null) {
            equipmentIds.addAll(equipmentEffect.lastKnownEquipmentIds());
        } else {
            gameData.forEachPermanent((controller, permanent) -> {
                if (targetId.equals(permanent.getAttachedTo())
                        && gameQueryService.hasEffectiveSubtype(gameData, permanent,
                                com.github.laxika.magicalvibes.model.CardSubtype.EQUIPMENT)) {
                    equipmentIds.add(permanent.getId());
                }
            });
        }
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (UUID equipmentId : equipmentIds) {
                Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
                if (equipment != null && permanentRemovalService.tryDestroyPermanent(gameData, equipment)) {
                    gameLogService.append(gameData, GameLog.isDestroyed(equipment.getCard()));
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
