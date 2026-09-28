package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.InventoryManagementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Inventory Management's per-attachment choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryManagementEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final AuraAttachmentService auraAttachmentService;
    private final EquipSupport equipSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InventoryManagementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> attachmentIds = controlledAttachmentIds(gameData, controllerId);
        if (attachmentIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                attachmentIds,
                attachmentIds.size(),
                new MultiPermanentChoiceContext.InventoryManagementAttachmentSelection(controllerId),
                entry.getCard().getName() + " — Choose Auras and Equipment to attach.");
    }

    public void completeAttachmentSelection(GameData gameData, UUID controllerId,
                                             List<UUID> attachmentIds) {
        if (attachmentIds.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        beginCreatureChoice(gameData, controllerId, attachmentIds.getFirst(),
                List.copyOf(attachmentIds.subList(1, attachmentIds.size())));
    }

    public void completeCreatureChoice(GameData gameData, UUID creatureId,
                                       PermanentChoiceContext.InventoryManagementAttachment context) {
        Permanent attachment = gameQueryService.findPermanentById(gameData, context.attachmentId());
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        UUID controllerId = context.controllerId();

        if (attachment != null && creature != null
                && controllerId.equals(gameQueryService.findPermanentController(gameData, attachment.getId()))
                && controllerId.equals(gameQueryService.findPermanentController(gameData, creature.getId()))
                && gameQueryService.isCreature(gameData, creature)) {
            attachIfLegal(gameData, attachment, creature, controllerId);
        }

        continueWithNextAttachment(gameData, controllerId, context.remainingAttachmentIds());
    }

    private void beginCreatureChoice(GameData gameData, UUID controllerId,
                                     UUID attachmentId, List<UUID> remainingAttachmentIds) {
        Permanent attachment = gameQueryService.findPermanentById(gameData, attachmentId);
        if (attachment == null) {
            continueWithNextAttachment(gameData, controllerId, remainingAttachmentIds);
            return;
        }

        List<UUID> creatureIds = controlledCreatureIdsForAttachment(gameData, controllerId, attachment);
        if (creatureIds.isEmpty()) {
            continueWithNextAttachment(gameData, controllerId, remainingAttachmentIds);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.InventoryManagementAttachment(
                        controllerId, attachmentId, remainingAttachmentIds));
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                creatureIds,
                attachment.getCard().getName() + " — Choose a creature you control to attach it to.");
    }

    private void continueWithNextAttachment(GameData gameData, UUID controllerId,
                                             List<UUID> remainingAttachmentIds) {
        if (remainingAttachmentIds.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else {
            beginCreatureChoice(gameData, controllerId, remainingAttachmentIds.getFirst(),
                    List.copyOf(remainingAttachmentIds.subList(1, remainingAttachmentIds.size())));
        }
    }

    private List<UUID> controlledAttachmentIds(GameData gameData, UUID controllerId) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent attachment : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if ((attachment.getCard().isAura()
                    || GameQueryService.permanentHasSubtype(attachment, CardSubtype.EQUIPMENT))
                    && !controlledCreatureIdsForAttachment(gameData, controllerId, attachment).isEmpty()) {
                ids.add(attachment.getId());
            }
        }
        return ids;
    }

    private List<UUID> controlledCreatureIdsForAttachment(GameData gameData, UUID controllerId,
                                                           Permanent attachment) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent creature : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (!gameQueryService.isCreature(gameData, creature)
                    || !canAttach(gameData, attachment, creature, controllerId)) {
                continue;
            }
            ids.add(creature.getId());
        }
        return ids;
    }

    private boolean canAttach(GameData gameData, Permanent attachment, Permanent creature,
                              UUID controllerId) {
        if (attachment.getCard().isAura()) {
            return auraAttachmentService.canEnchant(gameData, attachment.getCard(), controllerId, creature);
        }
        return GameQueryService.permanentHasSubtype(attachment, CardSubtype.EQUIPMENT)
                && equipSupport.canAttachEquipment(gameData, attachment, creature);
    }

    private void attachIfLegal(GameData gameData, Permanent attachment, Permanent creature,
                               UUID controllerId) {
        if (!canAttach(gameData, attachment, creature, controllerId)
                || creature.getId().equals(attachment.getAttachedTo())) {
            return;
        }

        if (attachment.getCard().isAura()) {
            gameData.expireFloatingEffectsForUnattachedSource(attachment.getId());
            attachment.setAttachedTo(creature.getId());
            attachment.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkAuraAttachedTriggers(gameData, attachment, creature.getId());
            logAttachment(gameData, attachment, creature);
        } else if (equipSupport.attachEquipment(gameData, attachment, creature)) {
            logAttachment(gameData, attachment, creature);
        }
    }

    private void logAttachment(GameData gameData, Permanent attachment, Permanent creature) {
        gameLogService.append(gameData,
                GameLog.cardTextCard(attachment.getCard(), " is now attached to ", creature.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                attachment.getCard().getName(), creature.getCard().getName());
    }
}
