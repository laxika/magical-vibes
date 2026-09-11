package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves Reflector Mage's bounce and temporary named-spell restriction. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        String creatureName = target.getCard().getName();
        UUID ownerId = gameData.stolenCreatures.get(target.getId());
        if (ownerId == null) {
            ownerId = target.getOriginalCard().getOwnerId();
        }
        if (ownerId == null) {
            ownerId = gameQueryService.findPermanentController(gameData, target.getId());
        }

        if (!permanentRemovalService.removePermanentToHand(gameData, target)) {
            return;
        }
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned to owner's hand by {}",
                gameData.id, creatureName, entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (entry.getControllerId() == null || ownerId == null || creatureName == null) {
            return;
        }
        gameData.playersCantCastNamedSpellsUntilControllerNextTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(creatureName);
    }
}
