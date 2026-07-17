package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
 * Resolves {@link GainControlOfTargetEffect} for every {@code ControlDuration}. Each
 * resolution creates a floating layer-2 control effect (CR 613.2/613.7) whose duration maps via
 * {@code ControlDuration.toEffectDuration()}; expiry and fallback to the next most recent
 * still-active control effect are handled by {@code CreatureControlService}.
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
            case END_OF_TURN -> resolveEndOfTurn(gameData, entry, e);
            case WHILE_SOURCE_ON_BATTLEFIELD, WHILE_SOURCE_TAPPED -> resolveWhileSource(gameData, entry, e);
        }
    }

    private void resolvePermanent(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                    e, e.duration().toEffectDuration(), null, entry.getCard().getName());

            if (e.grantedSubtype() != null && !target.getGrantedSubtypes().contains(e.grantedSubtype())) {
                target.getGrantedSubtypes().add(e.grantedSubtype());
                String subtypeLog = target.getCard().getName() + " becomes a " + e.grantedSubtype().getDisplayName() + " in addition to its other types.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(subtypeLog));
            }
        }
    }

    private void resolveEndOfTurn(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                e, e.duration().toEffectDuration(), null, entry.getCard().getName());

        // Magus of the Unseen: "When you lose control of the artifact, tap it." The stolen
        // permanent is tapped when this until-end-of-turn control effect expires (cleanup step).
        if (e.tapWhenControlLost()) {
            gameData.permanentsToTapWhenControlLost.add(target.getId());
        }
    }

    private void resolveWhileSource(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        // Per ruling: if you lose control of the source permanent before this resolves,
        // the ability resolves with no effect.
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (source left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }
        UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (sourceController == null || !sourceController.equals(entry.getControllerId())) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (controller no longer controls " + source.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                e, e.duration().toEffectDuration(), sourcePermanentId, entry.getCard().getName());
    }
}
