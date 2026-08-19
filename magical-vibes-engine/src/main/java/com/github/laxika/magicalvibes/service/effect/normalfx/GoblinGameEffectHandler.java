package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GoblinGameState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Goblin Game's hidden item-count choices and resulting life loss. */
@Component
@RequiredArgsConstructor
public class GoblinGameEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoblinGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GoblinGameState state = gameData.goblinGame;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.order.addAll(apnapPlayers(gameData));
            promptNextPlayer(gameData, cardName);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        state.itemCounts.put(state.currentPlayerId, gameData.chosenXValue);
        gameData.chosenXValue = null;
        state.index++;

        if (state.index < state.order.size()) {
            promptNextPlayer(gameData, cardName);
            return;
        }

        finish(gameData, state, cardName);
    }

    private void promptNextPlayer(GameData gameData, String cardName) {
        GoblinGameState state = gameData.goblinGame;
        UUID playerId = state.order.get(state.index);
        state.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, 1, Integer.MAX_VALUE,
                "Choose how many items to hide for " + cardName + ".",
                cardName));
    }

    private void finish(GameData gameData, GoblinGameState state, String cardName) {
        int fewest = state.itemCounts.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        List<UUID> fewestPlayers = state.order.stream()
                .filter(playerId -> state.itemCounts.get(playerId) == fewest)
                .toList();

        StringBuilder reveal = new StringBuilder("Players reveal their hidden item counts: ");
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

        for (UUID playerId : state.order) {
            lifeSupport.applyLifeLoss(gameData, playerId, state.itemCounts.get(playerId), cardName);
        }

        for (UUID playerId : fewestPlayers) {
            int currentLife = gameData.getLife(playerId);
            int halfLife = currentLife > 0 ? (currentLife + 1) / 2 : 0;
            if (halfLife > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, halfLife, cardName);
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
