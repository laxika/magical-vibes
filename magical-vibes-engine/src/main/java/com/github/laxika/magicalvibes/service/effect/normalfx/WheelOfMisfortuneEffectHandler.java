package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WheelOfMisfortuneState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WheelOfMisfortuneEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Wheel of Misfortune's private number choices and resulting damage and hand changes. */
@Component
@RequiredArgsConstructor
public class WheelOfMisfortuneEffectHandler implements NormalEffectHandlerBean {

    private static final int DRAW_COUNT = 7;

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final DrawService drawService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WheelOfMisfortuneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        WheelOfMisfortuneState state = gameData.wheelOfMisfortune;
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

        state.chosenNumbers.put(state.currentPlayerId, gameData.chosenXValue);
        gameData.chosenXValue = null;
        state.index++;

        if (state.index < state.order.size()) {
            promptNextPlayer(gameData, cardName);
            return;
        }

        finish(gameData, state, cardName, entry);
    }

    private void promptNextPlayer(GameData gameData, String cardName) {
        WheelOfMisfortuneState state = gameData.wheelOfMisfortune;
        UUID playerId = state.order.get(state.index);
        state.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, 0, Integer.MAX_VALUE,
                "Choose a number for " + cardName + ".", cardName));
    }

    private void finish(GameData gameData, WheelOfMisfortuneState state, String cardName,
                        StackEntry entry) {
        int highest = state.chosenNumbers.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int lowest = state.chosenNumbers.values().stream().mapToInt(Integer::intValue).min().orElse(0);

        StringBuilder reveal = new StringBuilder("Players reveal their numbers: ");
        for (int i = 0; i < state.order.size(); i++) {
            if (i > 0) {
                reveal.append(", ");
            }
            UUID playerId = state.order.get(i);
            reveal.append(gameData.playerIdToName.get(playerId))
                    .append(" reveals ")
                    .append(state.chosenNumbers.get(playerId));
        }
        gameLogService.append(gameData, GameLog.text(reveal + "."));

        if (highest > 0 && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            for (UUID playerId : state.order) {
                if (state.chosenNumbers.get(playerId) == highest) {
                    int damage = gameQueryService.applyDamageMultiplier(gameData, highest, entry);
                    damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
                }
            }
        }

        for (UUID playerId : state.order) {
            if (state.chosenNumbers.get(playerId) != lowest) {
                discardHandEffectHandler.discardHand(gameData, playerId, entry.getControllerId(), cardName);
                for (int i = 0; i < DRAW_COUNT; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId) + " draws " + DRAW_COUNT
                                + " cards (" + cardName + ")."));
            }
        }

        state.reset();
        gameOutcomeService.checkWinCondition(gameData);
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
