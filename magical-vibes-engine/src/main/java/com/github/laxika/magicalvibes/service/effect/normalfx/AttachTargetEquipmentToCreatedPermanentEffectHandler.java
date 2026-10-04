package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToCreatedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToCreatedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        var attachment = (AttachTargetEquipmentToCreatedPermanentEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID equipmentId = attachment.useSourceEquipment() ? entry.getSourcePermanentId()
                : targetIds.isEmpty() ? null : targetIds.getFirst();
        if (equipmentId == null) {
            return;
        }

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null || !gameQueryService.hasEffectiveSubtype(gameData, equipment, CardSubtype.EQUIPMENT)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (the Equipment is no longer on the battlefield)."));
            return;
        }
        List<Permanent> hosts = entry.getCreatedPermanentIds().stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .filter(host -> gameQueryService.isCreature(gameData, host)
                        && equipSupport.canAttachEquipment(gameData, equipment, host))
                .toList();
        if (hosts.isEmpty()) {
            return;
        }
        if (hosts.size() > 1) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(),
                    hosts.stream().map(Permanent::getId).toList(),
                    new PermanentChoiceContext.AttachEquipmentToCreature(equipment.getId(), entry.getControllerId()),
                    "Choose a created creature to attach " + equipment.getCard().getName() + " to.");
            return;
        }
        Permanent host = hosts.getFirst();

        UUID oldAttachedTo = equipment.getAttachedTo();
        if (!equipSupport.canAttachEquipment(gameData, equipment, host)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(host.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id,
                equipment.getCard().getName(), host.getCard().getName(), entry.getCard().getName());
    }
}
