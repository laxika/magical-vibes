package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class EnergyCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;

    public EnergyCountersEffectHandler(GameLogService gameLogService,
                                       @Lazy TriggerCollectionService triggerCollectionService,
                                       AmountEvaluationService amountEvaluationService) {
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnergyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EnergyCountersEffect energy = (EnergyCountersEffect) effect;
        int current = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        int amount = amountEvaluationService.evaluate(gameData, energy.amount(),
                AmountContext.forStackEntry(entry, null));
        int updated = Math.max(0, current + amount);
        int changed = updated - current;
        if (changed == 0) {
            return;
        }

        gameData.playerEnergyCounters.put(entry.getControllerId(), updated);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        String action = changed > 0 ? "gets " + changed : "pays " + -changed;
        gameLogService.append(gameData, GameLog.text(playerName + " " + action + " energy counter(s)."));

        if (changed > 0) {
            triggerCollectionService.checkEnergyGainTriggers(gameData, entry.getControllerId(), changed);
        }
    }
}
