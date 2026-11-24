package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsAtCheckpoints;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link FlipUntilLoseEffect}. Flips that stop at a loss are made one at a time; each won flip ends on a
 * coin-flip checkpoint whose resume point is the number of flips won so far, so a Krark's Thumb choice on a later flip
 * resumes with the next coin. A counted set that doesn't stop is flipped at once by its first coin-flip call, before
 * anything else. A won flip fires the won event and a lost one the lost event (CR 705.2); a heads-or-tails coin fires
 * neither, unless Edgar, King of Figaro made it a won flip. Records {@link StepKind#FLIP} (the flips won, or the heads)
 * after the last flip, before the all-wins effect.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlipUntilLoseEffectHandler implements NormalEffectHandlerBean, ChoosesCoinFlipResultsAtCheckpoints {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipUntilLoseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var flipEffect = (FlipUntilLoseEffect) effect;
        UUID controllerId = entry.getControllerId();
        int coins = flipEffect.coins() == null ? Integer.MAX_VALUE
                : Math.max(0, amountEvaluationService.evaluate(gameData, flipEffect.coins(),
                        AmountContext.forStackEntry(entry, null)));

        int wins;
        boolean lostAny;
        if (flipEffect.stopAtLoss()) {
            wins = coinFlipService.resumePoint(gameData, effect);
            lostAny = false;
            while (wins < coins) {
                CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
                logContestedFlip(gameData, entry, result);
                fireFlipEvent(gameData, controllerId, flipEffect, result);
                if (!result.heads()) {
                    lostAny = true;
                    break;
                }
                wins++;
                dispatch(gameData, entry, flipEffect.perWin());
                coinFlipService.checkpoint(gameData, effect, wins);
            }
        } else {
            List<CoinFlipService.CoinFlipResult> results = coinFlipService.flipCoins(gameData, controllerId, coins);
            wins = (int) results.stream().filter(CoinFlipService.CoinFlipResult::heads).count();
            lostAny = wins < results.size();
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " flips " + coins + " coins for "
                    + entry.getCard().getName() + ": " + wins + " heads"
                    + coinFlipService.replacementDetailsForCoins(results) + "."));
            for (CoinFlipService.CoinFlipResult result : results) {
                if (flipEffect.contested()) {
                    logContestedFlip(gameData, entry, result);
                }
                fireFlipEvent(gameData, controllerId, flipEffect, result);
            }
            for (CoinFlipService.CoinFlipResult result : results) {
                dispatch(gameData, entry, result.heads() ? flipEffect.perWin() : flipEffect.perLoss());
            }
        }

        entry.recordStep(StepKind.FLIP, StepOutcome.ofAmount(wins, List.of(controllerId)));
        if (!lostAny) {
            dispatch(gameData, entry, flipEffect.allWins());
        }
    }

    private void logContestedFlip(GameData gameData, StackEntry entry, CoinFlipService.CoinFlipResult result) {
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(playerName
                + (result.heads() ? " wins" : " loses") + " the coin flip for " + entry.getCard().getName()
                + coinFlipService.replacementDetails(result) + "."));
    }

    /**
     * A won flip fires the won event and a lost one the lost event (CR 705.2); a heads-or-tails coin is neither, unless
     * Edgar, King of Figaro made it a won flip (its ruling). A Pokey d20 roll is no coin flip.
     */
    private static void fireFlipEvent(GameData gameData, UUID controllerId, FlipUntilLoseEffect flipEffect,
                                      CoinFlipService.CoinFlipResult result) {
        if (!result.isActualCoinFlip()) {
            return;
        }
        if (result.heads() && (flipEffect.contested() || result.edgarApplied())) {
            ActionEvents.coinFlipWon(gameData, controllerId);
        } else if (!result.heads() && flipEffect.contested()) {
            ActionEvents.coinFlipLost(gameData, controllerId);
        }
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect == null) {
            return;
        }
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }
        if (effect instanceof ConditionalEffect conditional) {
            if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStackEntry(entry))) {
                dispatch(gameData, entry, conditional.wrapped());
            }
            return;
        }
        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for coin-flip branch effect: {}", effect.getClass().getSimpleName());
        }
    }
}
