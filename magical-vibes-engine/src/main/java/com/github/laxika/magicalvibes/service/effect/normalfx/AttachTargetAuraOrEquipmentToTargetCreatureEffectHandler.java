package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraOrEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachTargetAuraOrEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraOrEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            return;
        }

        Permanent attachment = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (attachment == null || destination == null || !attachment.isAttached()
                || !gameQueryService.isCreature(gameData, destination)
                || !entry.getControllerId().equals(gameData.findControllerOf(destination))) {
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, attachment.getAttachedTo());
        if (host == null || !gameQueryService.isCreature(gameData, host)
                || !entry.getControllerId().equals(gameData.findControllerOf(host))) {
            return;
        }

        if (attachment.getCard().isAura()) {
            UUID auraControllerId = gameQueryService.findPermanentController(gameData, attachment.getId());
            if (auraControllerId == null
                    || !auraAttachmentService.canEnchant(gameData, attachment.getCard(), auraControllerId, destination)
                    || gameQueryService.hasProtectionFromSource(gameData, destination, attachment)) {
                return;
            }

            gameData.expireFloatingEffectsForUnattachedSource(attachment.getId());
            attachment.setAttachedTo(destination.getId());
            attachment.setTimestamp(gameData.nextTimestamp());
            gameLogService.append(gameData, GameLog.cardTextCard(attachment.getCard(), " is now attached to ",
                    destination.getCard(), "."));
            triggerCollectionService.checkAuraAttachedTriggers(gameData, attachment.getCard(), destination.getId());
        } else if (attachment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                && equipSupport.canAttachEquipment(gameData, attachment, destination)) {
            UUID oldAttachedTo = attachment.getAttachedTo();
            gameData.expireFloatingEffectsForUnattachedSource(attachment.getId());
            attachment.setAttachedTo(destination.getId());
            attachment.setTimestamp(gameData.nextTimestamp());
            gameLogService.append(gameData, GameLog.cardTextCard(attachment.getCard(), " is now attached to ",
                    destination.getCard(), "."));
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, attachment, oldAttachedTo, destination.getId());
        }
    }
}
