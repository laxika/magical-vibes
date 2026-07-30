package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExchangeControlOfTargetPermanentsEffect} (Puca's Mischief). Reads the two targets
 * from {@code targetIds}: for cards that pin the first target to the ability's controller, {@code [0]}
 * is that controller's permanent and {@code [1]} the opponent's; for "two target creatures" wordings
 * either slot may belong to anyone. Re-checks target legality at resolution (CR 701.12a: if either
 * target has become illegal the exchange doesn't happen) and, if both are still legal and controlled
 * by different players (CR 701.12b), swaps
 * their controllers permanently by creating two layer-2 control effects via {@link CreatureControlService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfTargetPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

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

        // Re-check legality (CR 701.12a): both targets must still be matching permanents and must be
        // controlled by different players (CR 701.12b - if the same player controls both, the
        // exchange does nothing). Cards whose wording pins the first target to the ability's
        // controller ("target land you control and target land an opponent controls") additionally
        // require that split; plus, for Puca's Mischief, equal or lesser mana value.
        ExchangeControlOfTargetPermanentsEffect exchange = (ExchangeControlOfTargetPermanentsEffect) effect;
        boolean controllersDiffer = !ownController.equals(opponentController);
        boolean ownershipSplitOk = !exchange.requireFirstTargetControlledByController()
                || (ownController.equals(controllerId) && !opponentController.equals(controllerId));
        boolean stillLegal = controllersDiffer
                && ownershipSplitOk
                && predicateEvaluationService.matchesPermanentPredicate(gameData, ownTarget, exchange.targetPredicate())
                && predicateEvaluationService.matchesPermanentPredicate(gameData, opponentTarget, exchange.targetPredicate())
                && (!exchange.requireOpponentManaValueNotGreater()
                        || opponentTarget.getCard().getManaValue() <= ownTarget.getCard().getManaValue());
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

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(": ").card(ownTarget.getCard()).text(" and ").card(opponentTarget.getCard()).text(" exchange controllers.").build());
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id, entry.getCard().getName(),
                ownTarget.getCard().getName(), opponentTarget.getCard().getName());
    }

    private void logFizzle(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s exchange has no effect (a target is no longer legal)."));
        log.info("Game {} - {} exchange fizzles (illegal target)", gameData.id, entry.getCard().getName());
    }
}
