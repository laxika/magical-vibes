package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfTargetEffect} for all three {@code ControlDuration}s.
 * Each duration keeps its own tracking set so the corresponding revert mechanism
 * (end-of-turn cleanup / source-leaves-battlefield) stays intact.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfTargetEffect) effect;
        switch (e.duration()) {
            case PERMANENT -> resolvePermanent(gameData, entry, e);
            case END_OF_TURN -> resolveEndOfTurn(gameData, entry);
            case WHILE_SOURCE_ON_BATTLEFIELD -> resolveWhileSource(gameData, entry);
        }
    }

    private void resolvePermanent(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
            if (oldController != null && !oldController.equals(entry.getControllerId())) {
                creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
                gameData.permanentControlStolenCreatures.add(target.getId());
            }

            if (e.grantedSubtype() != null && !target.getGrantedSubtypes().contains(e.grantedSubtype())) {
                target.getGrantedSubtypes().add(e.grantedSubtype());
                String subtypeLog = target.getCard().getName() + " becomes a " + e.grantedSubtype().getDisplayName() + " in addition to its other types.";
                gameBroadcastService.logAndBroadcast(gameData, subtypeLog);
            }
        }
    }

    private void resolveEndOfTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
        if (oldController == null || oldController.equals(entry.getControllerId())) {
            return;
        }

        creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
        gameData.untilEndOfTurnStolenCreatures.add(target.getId());
    }

    private void resolveWhileSource(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        // Per ruling: if you lose control of the source permanent before this resolves,
        // the ability resolves with no effect.
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (source left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (sourceController == null || !sourceController.equals(entry.getControllerId())) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (controller no longer controls " + source.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
        if (oldController != null && !oldController.equals(entry.getControllerId())) {
            creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
            gameData.sourceDependentStolenCreatures.put(target.getId(), sourcePermanentId);
        }
    }
}
