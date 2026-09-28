package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfAttachedAurasAndEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Copies the source's attached Auras and Equipment onto the first token created by the resolution. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateTokenCopiesOfAttachedAurasAndEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final AuraAttachmentService auraAttachmentService;
    private final EquipSupport equipSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfAttachedAurasAndEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, entry.getCreatedPermanentIds().getFirst());
        if (host == null) {
            return;
        }

        List<Permanent> attached = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isAttached()
                    && source.getId().equals(permanent.getAttachedTo())
                    && (permanent.getCard().isAura()
                    || permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))) {
                attached.add(permanent);
            }
        });
        if (attached.isEmpty()) {
            return;
        }

        List<UUID> copiedIds = tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                attached.stream().map(Permanent::getCard).toList(),
                source,
                entry.getControllerId(),
                new CreateTokenCopyOfTargetPermanentEffect());

        int copyCount = Math.min(attached.size(), copiedIds.size());
        for (int i = 0; i < copyCount; i++) {
            Permanent copy = gameQueryService.findPermanentById(gameData, copiedIds.get(i));
            if (copy == null) {
                continue;
            }
            if (copy.getCard().isAura()) {
                attachAura(gameData, copy, host, entry.getControllerId());
            } else if (copy.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                if (equipSupport.attachEquipment(gameData, copy, host)) {
                    logAttachment(gameData, copy, host);
                }
            }
        }
    }

    private void attachAura(GameData gameData, Permanent aura, Permanent host, UUID controllerId) {
        if (!auraAttachmentService.canEnchant(gameData, aura.getCard(), controllerId, host)) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(host.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, host.getId());
        logAttachment(gameData, aura, host);
    }

    private void logAttachment(GameData gameData, Permanent attachment, Permanent host) {
        gameLogService.append(gameData,
                GameLog.cardTextCard(attachment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                attachment.getCard().getName(), host.getCard().getName());
    }
}
