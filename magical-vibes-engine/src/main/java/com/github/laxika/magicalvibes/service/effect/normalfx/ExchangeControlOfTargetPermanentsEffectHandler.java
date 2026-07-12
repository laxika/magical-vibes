package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExchangeControlOfTargetPermanentsEffect} (Puca's Mischief). Reads the two targets
 * from {@code targetIds}: {@code [0]} the permanent the ability's controller controls, {@code [1]}
 * the opponent's permanent. Re-checks target legality at resolution (CR 701.10 / Gatherer ruling: if
 * either target has become illegal the exchange doesn't happen) and, if both are still legal, swaps
 * their controllers permanently by creating two layer-2 control effects via {@link CreatureControlService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfTargetPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfTargetPermanentsEffect.class;
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

        // Re-check legality (CR 701.10): the first target must still be a nonland permanent the
        // controller controls, the second a nonland permanent an opponent controls with equal or
        // lesser mana value. If any condition no longer holds, the exchange doesn't happen.
        boolean stillLegal = ownController.equals(controllerId)
                && !opponentController.equals(controllerId)
                && !ownTarget.getCard().hasType(CardType.LAND)
                && !opponentTarget.getCard().hasType(CardType.LAND)
                && opponentTarget.getCard().getManaValue() <= ownTarget.getCard().getManaValue();
        if (!stillLegal) {
            logFizzle(gameData, entry);
            return;
        }

        // Exchange: give the controller's permanent to the opponent, and the opponent's to the controller.
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        creatureControlService.applyControlEffect(gameData, opponentController, ownTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
        creatureControlService.applyControlEffect(gameData, ownController, opponentTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());

        String logEntry = entry.getCard().getName() + ": " + ownTarget.getCard().getName() + " and "
                + opponentTarget.getCard().getName() + " exchange controllers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id, entry.getCard().getName(),
                ownTarget.getCard().getName(), opponentTarget.getCard().getName());
    }

    private void logFizzle(GameData gameData, StackEntry entry) {
        String logEntry = entry.getCard().getName() + "'s exchange has no effect (a target is no longer legal).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exchange fizzles (illegal target)", gameData.id, entry.getCard().getName());
    }
}
