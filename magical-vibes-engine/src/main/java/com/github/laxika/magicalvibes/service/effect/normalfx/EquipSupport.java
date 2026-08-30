package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Shared equip/attach helpers used by equip effect handlers.
 *
 * <p>Extracted verbatim from {@code EquipResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final UnattachTriggerSupport unattachTriggerSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final AuraCopyService auraCopyService;

    public void expireAttachedCopyEffects(GameData gameData, Permanent equipment) {
        auraCopyService.expireAttachedCopyEffects(gameData, equipment.getId());
    }

    public void notifyEquipmentAttached(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        triggerCollectionService.checkEquipmentAttachedTriggers(gameData, equipment, oldAttachedTo);
    }

    public Permanent findEquipmentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(cardId)) {
                    return p;
                }
            }
        }
        return null;
    }

    public boolean canAttachEquipment(GameData gameData, Permanent equipment, Permanent host) {
        if (!GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || gameQueryService.isCreature(gameData, equipment)
                || !gameQueryService.isCreature(gameData, host)
                || gameQueryService.cantBeEquipped(gameData, host)
                || gameQueryService.hasProtectionFromSource(gameData, host, equipment)) {
            return false;
        }

        var attachRestriction = equipment.getCard().getAttachRestriction();
        return attachRestriction == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, host, attachRestriction);
    }

    public boolean attachEquipment(GameData gameData, Permanent equipment, Permanent host) {
        if (!canAttachEquipment(gameData, equipment, host)) {
            return false;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        if (host.getId().equals(oldAttachedTo)) {
            return true;
        }
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(host.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
        notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
        return true;
    }

    public void applySacrificeOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                UUID oldAttachedTo, UUID newAttachedTo) {
        if (oldAttachedTo != null && !oldAttachedTo.equals(newAttachedTo)) {
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, equipment, oldAttachedTo);
        }

        boolean hasSacrificeOnUnattach = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);

        if (hasSacrificeOnUnattach && oldAttachedTo != null && !oldAttachedTo.equals(newAttachedTo)) {
            Permanent oldCreature = gameQueryService.findPermanentById(gameData, oldAttachedTo);
            if (oldCreature != null) {
                
                gameLogService.append(gameData, GameLog.cardTextCard(oldCreature.getCard(), " is sacrificed (", equipment.getCard(), " became unattached)."));
                log.info("Game {} - {} sacrificed due to {} unattach", gameData.id, oldCreature.getCard().getName(), equipment.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, oldCreature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }
}
