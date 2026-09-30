package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayAnyAmountOfEnergyToCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfEnergyToCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayAnyAmountOfEnergyToCreateTokenEffect payEffect =
                (PayAnyAmountOfEnergyToCreateTokenEffect) effect;
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
        createTokenEffectHandler.resolve(gameData, entry, payEffect.tokenEffect());
    }

    private void beginChoice(GameData gameData, StackEntry entry, int maxValue) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), maxValue,
                "You may pay one or more energy for " + entry.getCard().getName()
                        + " to create the token, where X is the amount paid.",
                entry.getCard().getName()));
    }

}
