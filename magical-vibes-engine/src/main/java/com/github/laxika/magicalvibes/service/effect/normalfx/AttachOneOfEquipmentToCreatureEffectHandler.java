package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfEquipmentToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfEquipmentToCreatureEffectHandler implements NormalEffectHandlerBean {

    private final AttachOneOfEquipmentToCreatureSupport support;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachOneOfEquipmentToCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AttachOneOfEquipmentToCreatureEffect attachEffect =
                (AttachOneOfEquipmentToCreatureEffect) effect;
        List<UUID> creatureIds = support.legalCreatureIds(
                gameData, entry.getControllerId(), attachEffect.equipmentPermanentIds());
        if (creatureIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachOneOfEquipmentToCreature(
                            attachEffect.equipmentPermanentIds()));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds,
                    entry.getCard().getName() + " - Choose a creature to attach an Equipment to.");
            return;
        }

        resolveForCreature(gameData, entry.getControllerId(), creatureIds.getFirst(),
                attachEffect.equipmentPermanentIds(), entry.getCard().getName(), entry.getCard());
    }

    public void resolveForCreature(GameData gameData, UUID controllerId, UUID creatureId,
                                   List<UUID> equipmentPermanentIds, String sourceName, Card sourceCard) {
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null || !support.legalCreatureIds(gameData, controllerId, equipmentPermanentIds)
                .contains(creatureId)) {
            return;
        }

        List<UUID> legalEquipmentIds = support.legalEquipmentIds(
                gameData, controllerId, creature, equipmentPermanentIds);
        if (legalEquipmentIds.isEmpty()) {
            return;
        }
        if (legalEquipmentIds.size() == 1) {
            support.attach(gameData, legalEquipmentIds.getFirst(), creatureId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.AttachControlledEquipmentToTargetCreature(
                        creatureId, controllerId, sourceCard, legalEquipmentIds, false));
        playerInputService.beginPermanentChoice(gameData, controllerId, legalEquipmentIds,
                sourceName + " - Choose an Equipment to attach.");
    }
}
