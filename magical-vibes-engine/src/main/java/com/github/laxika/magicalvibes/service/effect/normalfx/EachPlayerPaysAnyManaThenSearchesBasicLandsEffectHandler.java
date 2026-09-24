package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerPayManaState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenSearchesBasicLandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the join-forces payment and then starts the shared per-player basic-land search queue.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaThenSearchesBasicLandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaThenSearchesBasicLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            seedControllerFirstOrder(gameData, state, entry.getControllerId());
            promptOrFinish(gameData, cardName);
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
            if (payableFromPool(pool) < amount) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + amount + "} for " + cardName
                                + " (tap mana sources, then choose again)."));
                promptPlayer(gameData, state, playerId, cardName);
                return;
            }
            new ManaCost("{0}").pay(pool, amount);
            state.manaPaid.merge(playerId, amount, Integer::sum);
            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + amount + "} for " + cardName + "."));
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no mana for " + cardName + "."));
        }

        state.index++;
        promptOrFinish(gameData, cardName);
    }

    private static void seedControllerFirstOrder(GameData gameData, EachPlayerPayManaState state,
                                                   UUID controllerId) {
        if (controllerId != null && gameData.orderedPlayerIds.contains(controllerId)) {
            state.order.add(controllerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                state.order.add(playerId);
            }
            state.manaPaid.put(playerId, 0);
        }
    }

    private void promptOrFinish(GameData gameData, String cardName) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotentialX(gameData, playerId) <= 0) {
                state.index++;
                continue;
            }
            promptPlayer(gameData, state, playerId, cardName);
            return;
        }

        finish(gameData, state);
    }

    private void promptPlayer(GameData gameData, EachPlayerPayManaState state, UUID playerId,
                              String cardName) {
        state.currentPlayerId = playerId;
        String prompt = "Pay any amount of mana for " + cardName
                + ". Each player may search for up to the total amount paid in basic lands.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, maxPotentialX(gameData, playerId), prompt, cardName, true));
    }

    private void finish(GameData gameData, EachPlayerPayManaState state) {
        int totalPaid = state.manaPaid.values().stream().mapToInt(Integer::intValue).sum();
        List<LibrarySearchFollowUp.BasicLandsPick> picks = state.order.stream()
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(playerId, totalPaid, true))
                .toList();
        state.reset();
        basicLandSearchQueueSupport.advance(gameData,
                LibrarySearchFollowUp.basicLandSearches(picks, List.of()));
    }

    private int maxPotentialX(GameData gameData, UUID playerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal()
                - gameData.playerManaPools.get(playerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(playerId)) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool == null ? 0 : pool.getTotal();
    }
}
