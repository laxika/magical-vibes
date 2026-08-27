package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachAnyNumberOfControlledEquipmentToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, creature.getId()))) {
            return;
        }

        List<UUID> legalEquipmentIds = controlledEquipmentIds(gameData, entry.getControllerId(), creature);
        if (legalEquipmentIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                legalEquipmentIds,
                legalEquipmentIds.size(),
                new MultiPermanentChoiceContext.AttachAnyNumberOfControlledEquipmentToTargetCreature(
                        creature.getId()),
                entry.getCard().getName() + " — Choose any number of Equipment to attach.");
    }

    private List<UUID> controlledEquipmentIds(GameData gameData, UUID controllerId, Permanent creature) {
        List<UUID> legalEquipmentIds = new ArrayList<>();
        for (Permanent equipment : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, equipment, creature)) {
                legalEquipmentIds.add(equipment.getId());
            }
        }
        return legalEquipmentIds;
    }

    public void completeChoice(GameData gameData, UUID controllerId, List<UUID> equipmentIds,
                               MultiPermanentChoiceContext.AttachAnyNumberOfControlledEquipmentToTargetCreature context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, context.targetCreatureId());
        if (creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, creature.getId()))) {
            return;
        }

        for (UUID equipmentId : equipmentIds) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
            if (equipment == null
                    || !controllerId.equals(gameQueryService.findPermanentController(gameData, equipment.getId()))
                    || creature.getId().equals(equipment.getAttachedTo())
                    || !equipSupport.attachEquipment(gameData, equipment, creature)) {
                continue;
            }

            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
            log.info("Game {} - {} attached to {}", gameData.id,
                    equipment.getCard().getName(), creature.getCard().getName());
        }
    }
}
