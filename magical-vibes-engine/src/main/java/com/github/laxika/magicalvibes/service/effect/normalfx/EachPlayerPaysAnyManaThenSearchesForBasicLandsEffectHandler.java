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
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenSearchesForBasicLandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the shared-mana basic-land search used by Collective Voyage. */
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaThenSearchesForBasicLandsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaThenSearchesForBasicLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            seedControllerFirstOrder(gameData, entry.getControllerId(), state);
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
        ManaPool pool = gameData.playerManaPools.get(playerId);

        if (amount > 0) {
            if (payableFromPool(pool) < amount) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + amount + "} for " + cardName
                                + " (tap mana sources, then choose again)."));
                promptPlayer(gameData, playerId, cardName);
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
        promptOrFinish(gameData, cardName);
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

    private void promptOrFinish(GameData gameData, String cardName) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotentialX(gameData, playerId) <= 0) {
                state.index++;
                continue;
            }
            promptPlayer(gameData, playerId, cardName);
            return;
        }
        finish(gameData);
    }

    private void promptPlayer(GameData gameData, UUID playerId, String cardName) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        state.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, maxPotentialX(gameData, playerId),
                "Pay any amount of mana for " + cardName
                        + ". Each player will search for basic lands equal to the total mana paid.",
                cardName, true));
    }

    private void finish(GameData gameData) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        int totalManaPaid = state.manaPaid.values().stream().mapToInt(Integer::intValue).sum();
        List<LibrarySearchFollowUp.BasicLandsPick> picks = basicLandSearchQueueSupport.apnapOrder(gameData).stream()
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(playerId, totalManaPaid, true))
                .toList();
        state.reset();
        basicLandSearchQueueSupport.advance(
                gameData, LibrarySearchFollowUp.basicLandSearches(picks, List.of()));
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
