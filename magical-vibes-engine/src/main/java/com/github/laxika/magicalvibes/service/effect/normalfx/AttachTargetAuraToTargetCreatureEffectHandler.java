package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetAuraToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            fizzle(gameData, entry, "invalid targets");
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, targets.get(0));
        if (aura == null) {
            fizzle(gameData, entry, "Aura no longer on the battlefield");
            return;
        }

        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (destination == null) {
            fizzle(gameData, entry, "destination no longer on the battlefield");
            return;
        }

        if (!aura.getCard().isAura() || !aura.isAttached()) {
            return;
        }
        Permanent host = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (host == null || host.getId().equals(destination.getId())) {
            return;
        }
        boolean sameCreatureType = gameQueryService.isCreature(gameData, host)
                && gameQueryService.isCreature(gameData, destination);
        boolean sameLandType = gameQueryService.isLand(gameData, host)
                && gameQueryService.isLand(gameData, destination);
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if ((!sameCreatureType && !sameLandType)
                || auraControllerId == null
                || !auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, destination)
                || gameQueryService.hasProtectionFromSource(gameData, destination, aura)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(destination.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", destination.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, aura.getCard().getName(), destination.getCard().getName(), entry.getCard().getName());

        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura.getCard(), destination.getId());
    }

    private void fizzle(GameData gameData, StackEntry entry, String reason) {
        
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text("'s ability fizzles (" + reason + ").").build());
        log.info("Game {} - {} ability fizzles: {}", gameData.id, entry.getCard().getName(), reason);
    }
}
