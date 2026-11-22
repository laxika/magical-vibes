package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerPayManaState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Asks one X-value payment prompt per player who could pay, resolving again at its own index for each answer, and
 * records the payments once the last player has answered.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        String cardName = entry.getCard().getName();
        if (!state.active) {
            state.reset();
            state.active = true;
            seedOrder(gameData, state, ((EachPlayerPaysAnyManaEffect) effect).startingWithYou()
                    ? entry.getControllerId() : gameData.activePlayerId);
            promptOrRecord(gameData, entry);
            return;
        }
        if (gameData.chosenXValue == null) {
            return;
        }
        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        UUID playerId = state.currentPlayerId;
        String playerName = gameData.playerIdToName.get(playerId);
        if (amount > 0) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getTotal() < amount) {
                gameLogService.append(gameData, GameLog.text(playerName + " can't pay {" + amount + "} for "
                        + cardName + " (tap mana sources, then choose again)."));
                prompt(gameData, playerId, cardName);
                return;
            }
            new ManaCost("{0}").pay(pool, amount);
            state.manaPaid.merge(playerId, amount, Integer::sum);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + amount + "} for " + cardName + "."));
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no mana for " + cardName + "."));
        }
        state.index++;
        promptOrRecord(gameData, entry);
    }

    private static void seedOrder(GameData gameData, EachPlayerPayManaState state, UUID firstPlayerId) {
        int size = gameData.orderedPlayerIds.size();
        int start = Math.max(0, gameData.orderedPlayerIds.indexOf(firstPlayerId));
        for (int i = 0; i < size; i++) {
            UUID playerId = gameData.orderedPlayerIds.get((start + i) % size);
            state.order.add(playerId);
            state.manaPaid.put(playerId, 0);
        }
    }

    /** A player who can't pay anything isn't asked. */
    private void promptOrRecord(GameData gameData, StackEntry entry) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotentialX(gameData, playerId) > 0) {
                prompt(gameData, playerId, entry.getCard().getName());
                return;
            }
            state.index++;
        }
        entry.recordStep(StepKind.PAY_MANA, StepOutcome.ofAmounts(state.manaPaid));
        state.reset();
    }

    private void prompt(GameData gameData, UUID playerId, String cardName) {
        gameData.eachPlayerPayMana.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(playerId,
                maxPotentialX(gameData, playerId), "Pay any amount of mana for " + cardName + ".", cardName, true));
    }

    /** The pool plus untapped sources: players may tap during the payment (CR 605.3a). */
    private int maxPotentialX(GameData gameData, UUID playerId) {
        return potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal();
    }
}
