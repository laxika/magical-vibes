package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

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
        // The Equipment are destroyed together as the resolving step; the departure removes orphaned Auras.
        java.util.List<DepartureIntent> intents = equipmentIds.stream().distinct()
                .filter(equipmentId -> gameQueryService.findPermanentById(gameData, equipmentId) != null)
                .map(equipmentId -> DepartureIntent.destroy(equipmentId, false))
                .toList();
        if (!intents.isEmpty()) {
            stepDepartureSupport.depart(gameData, entry, effect, DepartureBatch.of(intents));
        }
    }
}
