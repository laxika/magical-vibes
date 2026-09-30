package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int currentEnergy = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        if (gameData.chosenXValue == null) {
            if (currentEnergy <= 0) {
                return;
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), currentEnergy,
                    "You may pay one or more energy for " + entry.getCard().getName()
                            + " to give your creatures indestructible based on the amount paid.",
                    entry.getCard().getName()));
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        if (amount < 0 || amount > currentEnergy) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), currentEnergy,
                    "You may pay one or more energy for " + entry.getCard().getName()
                            + " to give your creatures indestructible based on the amount paid.",
                    entry.getCard().getName()));
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
        grantIndestructibleToMatchingCreatures(gameData, entry, amount);
    }

    private void grantIndestructibleToMatchingCreatures(GameData gameData, StackEntry entry, int amount) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of());
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || gameQueryService.getEffectivePower(gameData, permanent) != amount
                    || gameQueryService.cantHaveOrGainKeyword(gameData, permanent, Keyword.INDESTRUCTIBLE)) {
                continue;
            }

            permanent.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
