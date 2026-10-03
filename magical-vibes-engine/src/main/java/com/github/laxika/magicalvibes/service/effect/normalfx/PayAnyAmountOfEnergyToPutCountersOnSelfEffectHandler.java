package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayAnyAmountOfEnergyToPutCountersOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PutCountersOnSelfEffectHandler putCountersOnSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfEnergyToPutCountersOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterEffect = (PayAnyAmountOfEnergyToPutCountersOnSelfEffect) effect;
        int currentEnergy = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        if (gameData.chosenXValue == null) {
            if (currentEnergy <= 0) {
                return;
            }
            beginChoice(gameData, entry, currentEnergy);
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        if (amount < 0 || amount > currentEnergy) {
            beginChoice(gameData, entry, currentEnergy);
            return;
        }

        gameData.setPlayerEnergyCounters(entry.getControllerId(), currentEnergy - amount);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        if (amount == 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no energy for "
                    + entry.getCard().getName() + "."));
            return;
        }

        gameLogService.append(gameData, GameLog.text(playerName + " pays " + amount
                + " energy counter(s) for " + entry.getCard().getName() + "."));
        entry.setEventValue(amount);
        putCountersOnSelfEffectHandler.resolve(gameData, entry,
                new PutCountersOnSelfEffect(counterEffect.counterType(), new EventValue()));
    }

    private void beginChoice(GameData gameData, StackEntry entry, int maxValue) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), maxValue,
                "You may pay one or more energy for " + entry.getCard().getName()
                        + ". If you do, put that many counters on this creature.",
                entry.getCard().getName()));
    }
}
