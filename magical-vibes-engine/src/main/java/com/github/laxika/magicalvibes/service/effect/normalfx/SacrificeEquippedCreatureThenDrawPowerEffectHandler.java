package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEquippedCreatureThenDrawPowerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a triggered ability that may sacrifice an Equipment's equipped creature for a draw. */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeEquippedCreatureThenDrawPowerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEquippedCreatureThenDrawPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent equipmentSnapshot = entry.getSourcePermanentSnapshot();
        UUID equippedId = equipmentSnapshot != null && equipmentSnapshot.getAttachedTo() != null
                ? equipmentSnapshot.getAttachedTo()
                : equipment == null ? null : equipment.getAttachedTo();
        if (equippedId == null) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, equippedId);
        if (creature == null) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (!entry.getControllerId().equals(creatureControllerId)
                || gameQueryService.cantBeSacrificed(gameData, creature)) {
            return;
        }

        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, creature));
        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, creature)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, creatureControllerId, creature.getCard());
        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (int i = 0; i < power; i++) {
            drawService.resolveDrawCard(gameData, entry.getControllerId());
        }
        log.info("Game {} - {} sacrifices {} and draws {} cards",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()),
                creature.getCard().getName(), power);
    }
}
