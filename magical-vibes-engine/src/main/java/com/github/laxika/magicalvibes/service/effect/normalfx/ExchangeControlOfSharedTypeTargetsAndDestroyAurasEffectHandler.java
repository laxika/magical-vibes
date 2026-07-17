package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect} (Gauntlets of Chaos).
 * Reads the two targets from {@code targetIds}: {@code [0]} the artifact/creature/land the ability's
 * controller controls, {@code [1]} the opponent's permanent sharing one of those types. Re-checks
 * legality at resolution (CR 701.10): the first must still be controlled by the ability's controller,
 * the second by an opponent, and the two must still share an artifact/creature/land type. If both are
 * legal it swaps their controllers permanently (two layer-2 control effects) and then destroys every
 * Aura attached to either permanent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() < 2) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Permanent ownTarget = gameQueryService.findPermanentById(gameData, targetIds.get(0));
        Permanent opponentTarget = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (ownTarget == null || opponentTarget == null) {
            logFizzle(gameData, entry);
            return;
        }

        UUID ownController = gameQueryService.findPermanentController(gameData, ownTarget.getId());
        UUID opponentController = gameQueryService.findPermanentController(gameData, opponentTarget.getId());
        if (ownController == null || opponentController == null) {
            logFizzle(gameData, entry);
            return;
        }

        // Re-check legality (CR 701.10): the first target must still be controlled by the ability's
        // controller, the second by an opponent, and the two must still share an artifact/creature/land
        // type. If any condition no longer holds, the exchange doesn't happen (and no Auras are destroyed).
        boolean stillLegal = ownController.equals(controllerId)
                && !opponentController.equals(controllerId)
                && gameQueryService.sharesArtifactCreatureOrLandType(ownTarget, opponentTarget);
        if (!stillLegal) {
            logFizzle(gameData, entry);
            return;
        }

        // Snapshot the Auras attached to either permanent before the swap so we destroy exactly those.
        List<Permanent> aurasToDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().isAura() && permanent.isAttached()
                    && (ownTarget.getId().equals(permanent.getAttachedTo())
                            || opponentTarget.getId().equals(permanent.getAttachedTo()))) {
                aurasToDestroy.add(permanent);
            }
        });

        // Exchange: give the controller's permanent to the opponent, and the opponent's to the controller.
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        creatureControlService.applyControlEffect(gameData, opponentController, ownTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
        creatureControlService.applyControlEffect(gameData, ownController, opponentTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());

        String logEntry = entry.getCard().getName() + ": " + ownTarget.getCard().getName() + " and "
                + opponentTarget.getCard().getName() + " exchange controllers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id, entry.getCard().getName(),
                ownTarget.getCard().getName(), opponentTarget.getCard().getName());

        // "destroy all Auras attached to them" — only because the exchange happened.
        for (Permanent aura : aurasToDestroy) {
            destructionSupport.tryDestroyAndLog(gameData, aura, entry.getCard().getName());
        }
    }

    private void logFizzle(GameData gameData, StackEntry entry) {
        String logEntry = entry.getCard().getName() + "'s exchange has no effect (a target is no longer legal).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exchange fizzles (illegal target)", gameData.id, entry.getCard().getName());
    }
}
