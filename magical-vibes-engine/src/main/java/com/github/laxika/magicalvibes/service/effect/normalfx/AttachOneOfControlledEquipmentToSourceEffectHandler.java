package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfControlledEquipmentToSourceEffect;
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
public class AttachOneOfControlledEquipmentToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachOneOfControlledEquipmentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = findSource(gameData, entry);
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        List<UUID> legalEquipmentIds = controlledEquipmentIds(gameData, entry.getControllerId(), source);
        if (legalEquipmentIds.isEmpty()) {
            return;
        }
        if (legalEquipmentIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachControlledEquipmentToTargetCreature(
                            source.getId(), entry.getControllerId(), entry.getCard(), legalEquipmentIds, false));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), legalEquipmentIds,
                    entry.getCard().getName() + " - Choose an Equipment to attach.");
            return;
        }

        attach(gameData, legalEquipmentIds.getFirst(), source);
    }

    private Permanent findSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }

        UUID sourceCardId = entry.getCard().getId();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (sourceCardId.equals(permanent.getCard().getId())
                    || (permanent.getOriginalCard() != null
                    && sourceCardId.equals(permanent.getOriginalCard().getId()))) {
                return permanent;
            }
        }
        return null;
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

    private void attach(GameData gameData, UUID equipmentId, Permanent creature) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null || !equipSupport.attachEquipment(gameData, equipment, creature)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
    }
}
