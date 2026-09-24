package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachSelectedEquipmentToCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSelectedEquipmentToCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AttachSelectedEquipmentToCreatureEffect attachEffect =
                (AttachSelectedEquipmentToCreatureEffect) effect;
        if (attachEffect.equipmentPermanentIds().size() != 1) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID equipmentId = attachEffect.equipmentPermanentIds().getFirst();
        Permanent equipment = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getId().equals(equipmentId))
                .findFirst()
                .orElse(null);
        if (equipment == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)) {
            return;
        }

        List<UUID> creatureIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(creature -> equipSupport.canAttachEquipment(gameData, equipment, creature))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachEquipmentToCreature(equipmentId, controllerId));
            playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                    entry.getCard().getName() + " - Choose a creature to attach it to.");
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
        if (equipSupport.attachEquipment(gameData, equipment, creature)) {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
        }
    }
}
