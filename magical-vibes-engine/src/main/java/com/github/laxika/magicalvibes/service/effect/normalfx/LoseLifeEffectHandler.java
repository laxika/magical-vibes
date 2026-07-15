package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole life-loss family via {@link LoseLifeEffect}: the {@code LoseLifeRecipient}
 * routes who loses life and {@code controllerGainsLifeLost} drains the total life lost back to the
 * controller. The {@link DynamicAmount} amount is evaluated once against the stack entry (source-
 * relative amounts use the live source permanent when present, else the last-known snapshot).
 *
 * <p>Controller / each-player / each-opponent life loss goes through {@link LifeSupport#applyLifeLoss}
 * (which fires "loses life" triggers). Target-player life loss is applied inline without firing those
 * triggers — behaviour preserved verbatim from the former {@code TargetPlayerLosesLifeEffectHandler}.
 * Life loss is never routed through damage plumbing (CR 118.2).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoseLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeEffect) effect;

        // Source-relative amounts (e.g. "for each Vampire you control") use the live source
        // permanent when present, else the last-known snapshot.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        switch (e.recipient()) {
            case CONTROLLER -> lifeSupport.applyLifeLoss(gameData, controllerId, amount, sourceName);
            case TARGET_PLAYER -> loseTargetPlayerLife(gameData, entry, amount, sourceName);
            case TARGET_PERMANENT_CONTROLLER -> loseTargetPermanentControllerLife(gameData, entry, amount, sourceName);
            case EACH_PLAYER -> eachPlayerLosesLife(gameData, e, controllerId, amount, sourceName, false);
            case EACH_OPPONENT -> eachPlayerLosesLife(gameData, e, controllerId, amount, sourceName, true);
        }
    }

    private void loseTargetPlayerLife(GameData gameData, StackEntry entry, int amount, String sourceName) {
        UUID targetPlayerId = entry.getTargetId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + "'s life total can't change."));
        } else {
            int targetCurrentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - amount);

            String lossLog = targetName + " loses " + amount + " life (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(lossLog));
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, amount, sourceName);
        }
    }

    private void loseTargetPermanentControllerLife(GameData gameData, StackEntry entry, int amount, String sourceName) {
        // targetId is the targeted permanent; the controller of that permanent loses life. Runs
        // before any accompanying destroy effect so the permanent is still on the battlefield.
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        lifeSupport.applyLifeLoss(gameData, controllerId, amount, sourceName);
    }

    private void eachPlayerLosesLife(GameData gameData, LoseLifeEffect e, UUID controllerId,
            int amount, String sourceName, boolean opponentsOnly) {
        // The X-scaled drain (Exsanguinate) short-circuits on non-positive X before touching any
        // life total — preserves the former EachOpponentLosesXLife... early-out.
        if (e.controllerGainsLifeLost() && amount <= 0) {
            return;
        }

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentsOnly && playerId.equals(controllerId)) {
                continue;
            }
            lifeSupport.applyLifeLoss(gameData, playerId, amount, sourceName);
            totalLifeLost += amount;
        }

        if (e.controllerGainsLifeLost() && totalLifeLost > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }
}
