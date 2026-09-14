package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledAurasAndEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Heavenly Blademaster's optional mixed Aura and Equipment attachment choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachAnyNumberOfControlledAurasAndEquipmentToSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final AuraAttachmentService auraAttachmentService;
    private final EquipSupport equipSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAnyNumberOfControlledAurasAndEquipmentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        List<UUID> legalAttachmentIds = controlledLegalAttachmentIds(gameData, entry.getControllerId(), source);
        if (legalAttachmentIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                legalAttachmentIds,
                legalAttachmentIds.size(),
                new MultiPermanentChoiceContext.AttachAnyNumberOfControlledAurasAndEquipmentToSource(
                        source.getId()),
                "Choose any number of Auras and Equipment to attach to " + source.getCard().getName() + ".");
    }

    private List<UUID> controlledLegalAttachmentIds(GameData gameData, UUID controllerId, Permanent source) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (permanent.getId().equals(source.getId())
                    || source.getId().equals(permanent.getAttachedTo())) {
                continue;
            }
            if (permanent.getCard().isAura()
                    && auraAttachmentService.canEnchant(gameData, permanent.getCard(), controllerId, source)) {
                ids.add(permanent.getId());
            } else if (GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, permanent, source)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    public void completeChoice(GameData gameData, UUID controllerId, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.AttachAnyNumberOfControlledAurasAndEquipmentToSource context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        for (UUID permanentId : permanentIds) {
            Permanent attachment = gameQueryService.findPermanentById(gameData, permanentId);
            if (attachment == null
                    || !controllerId.equals(gameQueryService.findPermanentController(gameData, attachment.getId()))
                    || source.getId().equals(attachment.getAttachedTo())) {
                continue;
            }

            if (attachment.getCard().isAura()) {
                attachAura(gameData, controllerId, attachment, source);
            } else if (GameQueryService.permanentHasSubtype(attachment, CardSubtype.EQUIPMENT)
                    && equipSupport.attachEquipment(gameData, attachment, source)) {
                logAttachment(gameData, attachment, source);
            }
        }
    }

    private void attachAura(GameData gameData, UUID controllerId, Permanent aura, Permanent source) {
        if (!auraAttachmentService.canEnchant(gameData, aura.getCard(), controllerId, source)) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(source.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, source.getId());
        logAttachment(gameData, aura, source);
    }

    private void logAttachment(GameData gameData, Permanent attachment, Permanent source) {
        gameLogService.append(gameData,
                GameLog.cardTextCard(attachment.getCard(), " is now attached to ", source.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                attachment.getCard().getName(), source.getCard().getName());
    }
}
