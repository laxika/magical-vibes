package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.UnattachEquipmentAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfControlledEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfControlledEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachOneOfControlledEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        List<UUID> legalEquipmentIds = controlledEquipmentIds(gameData, entry.getControllerId(), creature);
        if (legalEquipmentIds.isEmpty()) {
            return;
        }
        if (legalEquipmentIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachControlledEquipmentToTargetCreature(
                            creature.getId(), entry.getControllerId(), entry.getCard(), legalEquipmentIds));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), legalEquipmentIds,
                    entry.getCard().getName() + " - Choose an Equipment to attach.");
            return;
        }

        attachAndSchedule(gameData, entry.getControllerId(), entry.getCard(),
                legalEquipmentIds.getFirst(), creature);
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

    void attachAndSchedule(GameData gameData, UUID controllerId, Card sourceCard,
                           UUID equipmentId, Permanent creature) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null || !equipSupport.attachEquipment(gameData, equipment, creature)) {
            return;
        }

        gameData.queueDelayedAction(new UnattachEquipmentAtNextEndStep(controllerId, equipmentId, sourceCard));
        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
    }
}
