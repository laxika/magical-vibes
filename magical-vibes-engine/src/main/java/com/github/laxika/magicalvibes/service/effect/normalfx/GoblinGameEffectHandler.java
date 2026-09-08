package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GoblinGameState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves hidden-number choices and the resulting life loss for Goblin Game and Menacing Ogre. */
@Component
@RequiredArgsConstructor
public class GoblinGameEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoblinGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GoblinGameEffect goblinGameEffect = (GoblinGameEffect) effect;
        GoblinGameState state = gameData.goblinGame;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.order.addAll(apnapPlayers(gameData));
            promptNextPlayer(gameData, cardName, goblinGameEffect);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        state.itemCounts.put(state.currentPlayerId, gameData.chosenXValue);
        gameData.chosenXValue = null;
        state.index++;

        if (state.index < state.order.size()) {
            promptNextPlayer(gameData, cardName, goblinGameEffect);
            return;
        }

        finish(gameData, state, cardName, entry, goblinGameEffect);
    }

    private void promptNextPlayer(GameData gameData, String cardName, GoblinGameEffect effect) {
        GoblinGameState state = gameData.goblinGame;
        UUID playerId = state.order.get(state.index);
        state.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, effect.highestNumberWins() ? 0 : 1, Integer.MAX_VALUE,
                effect.highestNumberWins()
                        ? "Choose a number for " + cardName + "."
                        : "Choose how many items to hide for " + cardName + ".",
                cardName));
    }

    private void finish(GameData gameData, GoblinGameState state, String cardName,
                        StackEntry entry, GoblinGameEffect effect) {
        int winningNumber = effect.highestNumberWins()
                ? state.itemCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0)
                : state.itemCounts.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        List<UUID> winningPlayers = state.order.stream()
                .filter(playerId -> state.itemCounts.get(playerId) == winningNumber)
                .toList();

        StringBuilder reveal = new StringBuilder("Players reveal their hidden numbers: ");
        for (int i = 0; i < state.order.size(); i++) {
            if (i > 0) {
                reveal.append(", ");
            }
            UUID playerId = state.order.get(i);
            reveal.append(gameData.playerIdToName.get(playerId))
                    .append(" reveals ")
                    .append(state.itemCounts.get(playerId));
        }
        gameLogService.append(gameData, GameLog.text(reveal + "."));

        if (effect.highestNumberWins()) {
            for (UUID playerId : winningPlayers) {
                lifeSupport.applyLifeLoss(gameData, playerId, winningNumber, cardName);
            }

            if (effect.countersOnSourceIfControllerWins() > 0
                    && winningPlayers.contains(entry.getControllerId())) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source != null) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, entry, source,
                            CounterType.PLUS_ONE_PLUS_ONE,
                            effect.countersOnSourceIfControllerWins());
                }
            }
        } else {
            for (UUID playerId : state.order) {
                lifeSupport.applyLifeLoss(gameData, playerId, state.itemCounts.get(playerId), cardName);
            }

            for (UUID playerId : winningPlayers) {
                int currentLife = gameData.getLife(playerId);
                int halfLife = currentLife > 0 ? (currentLife + 1) / 2 : 0;
                if (halfLife > 0) {
                    lifeSupport.applyLifeLoss(gameData, playerId, halfLife, cardName);
                }
            }
        }

        state.reset();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }
}
