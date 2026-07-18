package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
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
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

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

    public void applySacrificeOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                UUID oldAttachedTo, UUID newAttachedTo) {
        boolean hasSacrificeOnUnattach = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);

        if (hasSacrificeOnUnattach && oldAttachedTo != null && !oldAttachedTo.equals(newAttachedTo)) {
            Permanent oldCreature = gameQueryService.findPermanentById(gameData, oldAttachedTo);
            if (oldCreature != null) {
                String sacrificeLog = oldCreature.getCard().getName() + " is sacrificed (" + equipment.getCard().getName() + " became unattached).";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(oldCreature.getCard(), " is sacrificed (", equipment.getCard(), " became unattached)."));
                log.info("Game {} - {} sacrificed due to {} unattach", gameData.id, oldCreature.getCard().getName(), equipment.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, oldCreature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }
}
