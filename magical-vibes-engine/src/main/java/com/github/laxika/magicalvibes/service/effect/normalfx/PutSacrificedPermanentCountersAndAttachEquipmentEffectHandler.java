package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSacrificedPermanentCountersAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutSacrificedPermanentCountersAndAttachEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final EquipSupport equipSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSacrificedPermanentCountersAndAttachEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (sacrificed != null) {
            for (CounterType counterType : CounterType.values()) {
                if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                    continue;
                }
                int count = sacrificed.getCounterCount(counterType);
                if (count > 0) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, entry, target, counterType, count);
                }
            }
        }

        List<UUID> legalEquipmentIds = legalEquipmentIds(gameData, entry, target);
        if (legalEquipmentIds.size() == 1) {
            attach(gameData, legalEquipmentIds.getFirst(), target);
        } else if (legalEquipmentIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachSacrificedEquipmentToTarget(
                            target.getId(), legalEquipmentIds));
            playerInputService.beginPermanentChoice(
                    gameData,
                    entry.getControllerId(),
                    legalEquipmentIds,
                    entry.getCard().getName() + " — Choose an Equipment to attach.");
        }
    }

    public void attachChosenEquipment(GameData gameData, UUID equipmentId,
                                      PermanentChoiceContext.AttachSacrificedEquipmentToTarget context) {
        Permanent target = gameQueryService.findPermanentById(gameData, context.targetCreatureId());
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (target != null && equipment != null
                && context.equipmentPermanentIds().contains(equipmentId)
                && equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                && equipSupport.canAttachEquipment(gameData, equipment, target)) {
            attach(gameData, equipmentId, target);
        }
    }

    private List<UUID> legalEquipmentIds(GameData gameData, StackEntry entry, Permanent target) {
        List<UUID> legalIds = new ArrayList<>();
        for (UUID equipmentId : entry.getSacrificedAttachedEquipmentIds()) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
            if (equipment != null
                    && equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, equipment, target)) {
                legalIds.add(equipmentId);
            }
        }
        return legalIds;
    }

    private void attach(GameData gameData, UUID equipmentId, Permanent target) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null || !equipSupport.canAttachEquipment(gameData, equipment, target)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(target.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, target.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
    }
}
