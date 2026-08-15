package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayAnyAmountOfEnergyToBoostTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfEnergyToBoostTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int currentEnergy = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        if (gameData.chosenXValue == null) {
            if (currentEnergy <= 0) {
                return;
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), currentEnergy,
                    "You may pay any amount of energy for " + entry.getCard().getName()
                            + ". The creature gets -1/-1 for each energy paid.",
                    entry.getCard().getName()));
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        if (amount > currentEnergy) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), currentEnergy,
                    "You may pay any amount of energy for " + entry.getCard().getName()
                            + ". The creature gets -1/-1 for each energy paid.",
                    entry.getCard().getName()));
            return;
        }

        gameData.playerEnergyCounters.put(entry.getControllerId(), currentEnergy - amount);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        if (amount == 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no energy for "
                    + entry.getCard().getName() + "."));
            return;
        }

        gameLogService.append(gameData, GameLog.text(playerName + " pays " + amount
                + " energy counter(s) for " + entry.getCard().getName() + "."));
        entry.setEventValue(amount);
        boostTargetCreatureEffectHandler.resolve(gameData, entry,
                new BoostTargetCreatureEffect(new Scaled(new EventValue(), -1),
                        new Scaled(new EventValue(), -1)));
    }

}
