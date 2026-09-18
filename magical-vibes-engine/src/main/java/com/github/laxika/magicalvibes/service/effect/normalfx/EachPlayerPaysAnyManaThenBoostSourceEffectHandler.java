package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerPayManaState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenBoostSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the controller-first mana-payment and shared-boost sequence used by Mana-Charged Dragon. */
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaThenBoostSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaThenBoostSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.sourceSetCode = entry.getCard().getSetCode();
            seedControllerFirstOrder(gameData, entry.getControllerId(), state);
            promptOrFinish(gameData, entry);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        UUID playerId = state.currentPlayerId;
        String playerName = gameData.playerIdToName.get(playerId);
        ManaPool pool = gameData.playerManaPools.get(playerId);

        if (amount > 0) {
            if (payableFromPool(pool) < amount) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + amount + "} for " + cardName
                                + " (tap mana sources, then choose again)."));
                promptPlayer(gameData, entry, playerId);
                return;
            }
            new ManaCost("{0}").pay(pool, amount);
            state.manaPaid.merge(playerId, amount, Integer::sum);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + amount + "} for " + cardName + "."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays no mana for " + cardName + "."));
        }

        state.index++;
        promptOrFinish(gameData, entry);
    }

    private void seedControllerFirstOrder(GameData gameData, UUID controllerId,
                                          EachPlayerPayManaState state) {
        int controllerIndex = Math.max(0, gameData.orderedPlayerIds.indexOf(controllerId));
        for (int i = 0; i < gameData.orderedPlayerIds.size(); i++) {
            UUID playerId = gameData.orderedPlayerIds.get(
                    (controllerIndex + i) % gameData.orderedPlayerIds.size());
            state.order.add(playerId);
            state.manaPaid.put(playerId, 0);
        }
    }

    private void promptOrFinish(GameData gameData, StackEntry entry) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotentialX(gameData, playerId) <= 0) {
                state.index++;
                continue;
            }
            promptPlayer(gameData, entry, playerId);
            return;
        }
        finish(gameData, entry);
    }

    private void promptPlayer(GameData gameData, StackEntry entry, UUID playerId) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        state.currentPlayerId = playerId;
        String cardName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, maxPotentialX(gameData, playerId),
                "Pay any amount of mana for " + cardName
                        + ". This creature gets +X/+0 until end of turn, where X is the total mana paid.",
                cardName, true));
    }

    private void finish(GameData gameData, StackEntry entry) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        int totalManaPaid = state.manaPaid.values().stream().mapToInt(Integer::intValue).sum();
        if (totalManaPaid > 0) {
            boostSelfEffectHandler.resolve(gameData, entry, new BoostSelfEffect(totalManaPaid, 0));
        }
        state.reset();
    }

    private int maxPotentialX(GameData gameData, UUID playerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal()
                - gameData.playerManaPools.get(playerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(playerId)) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal();
    }
}
