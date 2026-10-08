package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GraveyardService graveyardService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect attachmentEffect =
                (ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect) effect;
        List<UUID> legalAuraCardIds = legalAuraCardIds(gameData, entry.getControllerId(), target,
                attachmentEffect);
        List<UUID> legalEquipmentIds = legalAuraCardIds.isEmpty()
                ? legalEquipmentIds(gameData, target, attachmentEffect) : List.of();
        if (!legalAuraCardIds.isEmpty() || !legalEquipmentIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(),
                    legalEquipmentIds, legalAuraCardIds,
                    legalAuraCardIds.size() + legalEquipmentIds.size(),
                    new MultiPermanentChoiceContext.ReturnAurasAndAttachEquipmentToTargetCreature(
                            target.getId(), legalAuraCardIds.isEmpty() ? List.of() : attachmentEffect.auraCardIds(),
                            attachmentEffect.equipmentPermanentIds()),
                    "Choose any number of Auras and Equipment to attach to " + target.getCard().getName() + ".");
        }
    }

    public void completeChoice(GameData gameData, UUID controllerId, List<UUID> selectedIds,
                               MultiPermanentChoiceContext.ReturnAurasAndAttachEquipmentToTargetCreature context) {
        Permanent target = gameQueryService.findPermanentById(gameData, context.targetCreatureId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Set<UUID> selected = new LinkedHashSet<>(selectedIds);
        for (UUID auraCardId : context.auraCardIds()) {
            if (selected.contains(auraCardId)) {
                returnAura(gameData, controllerId, auraCardId, target);
            }
        }
        creatureControlService.recomputeControl(gameData, target);
        if (!context.auraCardIds().isEmpty()) {
            var equipmentEffect = new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect(
                    List.of(), context.equipmentPermanentIds());
            List<UUID> legalEquipmentIds = legalEquipmentIds(gameData, target, equipmentEffect);
            if (!legalEquipmentIds.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        legalEquipmentIds, List.of(), legalEquipmentIds.size(),
                        new MultiPermanentChoiceContext.ReturnAurasAndAttachEquipmentToTargetCreature(
                                target.getId(), List.of(), context.equipmentPermanentIds()),
                        "Choose any number of Equipment to attach to " + target.getCard().getName() + ".");
            }
            return;
        }
        for (UUID equipmentPermanentId : context.equipmentPermanentIds()) {
            if (selected.contains(equipmentPermanentId)) {
                attachEquipment(gameData, equipmentPermanentId, target);
            }
        }
        creatureControlService.recomputeControl(gameData, target);
    }

    private List<UUID> legalAuraCardIds(GameData gameData, UUID controllerId, Permanent target,
                                        ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect effect) {
        List<UUID> ids = new ArrayList<>();
        for (UUID auraCardId : effect.auraCardIds()) {
            Card aura = findCard(gameData, controllerId, auraCardId);
            if (aura != null && auraAttachmentService.canEnchant(gameData, aura, controllerId, target)) {
                ids.add(auraCardId);
            }
        }
        return ids;
    }

    private List<UUID> legalEquipmentIds(GameData gameData, Permanent target,
                                          ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect effect) {
        List<UUID> ids = new ArrayList<>();
        for (UUID equipmentPermanentId : effect.equipmentPermanentIds()) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
            if (equipment != null
                    && GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, equipment, target)) {
                ids.add(equipmentPermanentId);
            }
        }
        return ids;
    }

    private void returnAura(GameData gameData, UUID controllerId, UUID auraCardId, Permanent target) {
        Card aura = findCard(gameData, controllerId, auraCardId);
        if (aura == null || !auraAttachmentService.canEnchant(gameData, aura, controllerId, target)) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            gameData.playerGraveyards.get(controllerId).remove(aura);
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, aura);
            Permanent enteringAura = new Permanent(aura, Zone.GRAVEYARD);
            enteringAura.setAttachedTo(target.getId());
            enteringAura.setEnteredFromGraveyardOwnerId(controllerId);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, enteringAura);
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(aura)
                .text(" returns from the graveyard attached to ")
                .card(target.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} returns from the graveyard attached to {}", gameData.id,
                aura.getName(), target.getCard().getName());
    }

    private void attachEquipment(GameData gameData, UUID equipmentPermanentId, Permanent target) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
        if (equipment == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || !equipSupport.attachEquipment(gameData, equipment, target)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", target.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                equipment.getCard().getName(), target.getCard().getName());
    }

    private Card findCard(GameData gameData, UUID controllerId, UUID cardId) {
        return gameData.playerGraveyards.getOrDefault(controllerId, java.util.List.of()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
