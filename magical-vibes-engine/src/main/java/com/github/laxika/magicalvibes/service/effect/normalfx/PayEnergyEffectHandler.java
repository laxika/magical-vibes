package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Pay {E}{E}" ({@link PayEnergyEffect}): the controller loses that many energy counters when they have them, through
 * {@link GameData#setPlayerEnergyCounters} (the payment every energy cost makes, counted as paid this turn), recorded
 * as the {@link StepKind#PAY_ENERGY} step; without enough nothing is paid (CR 118.3) and the step records
 * {@link StepOutcome#NONE}.
 */
@Component
@RequiredArgsConstructor
public class PayEnergyEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayEnergyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var pay = (PayEnergyEffect) effect;
        UUID payerId = entry.getControllerId();
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, pay.amount(),
                AmountContext.forStackEntry(entry, null)));
        int energy = gameData.playerEnergyCounters.getOrDefault(payerId, 0);
        if (amount > energy) {
            entry.recordStep(StepKind.PAY_ENERGY, StepOutcome.NONE);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(payerId)
                    + " can't pay " + amount + " energy counter(s)."));
            return;
        }
        gameData.setPlayerEnergyCounters(payerId, energy - amount);
        entry.recordStep(StepKind.PAY_ENERGY, StepOutcome.ofAmount(amount, List.of(payerId)));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(payerId)
                + " pays " + amount + " energy counter(s)."));
    }
}
