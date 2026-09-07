package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetOpponentsDiscardThenDrawState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentsDiscardThenDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Hollow Marauder's hidden discard choices and conditional draws. */
@Component
@RequiredArgsConstructor
public class TargetOpponentsDiscardThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentsDiscardThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetOpponentsDiscardThenDrawEffect discardEffect =
                (TargetOpponentsDiscardThenDrawEffect) effect;
        TargetOpponentsDiscardThenDrawState state = gameData.targetOpponentsDiscardThenDraw;

        if (state.completed) {
            complete(gameData, discardEffect, state);
            return;
        }

        if (!state.active) {
            state.reset();
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.remainingTargetIds.addAll(
                    targetsInTurnOrder(gameData, entry.targetsForEffect(effect)));
        }

        while (!state.remainingTargetIds.isEmpty()) {
            UUID targetId = state.remainingTargetIds.removeFirst();
            if (gameData.playerHands.getOrDefault(targetId, List.of()).isEmpty()) {
                state.noDiscardPlayers.add(targetId);
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginDiscardChoice(gameData, targetId, 1,
                    DiscardFollowUp.targetOpponentsDiscardThenDraw(List.copyOf(state.remainingTargetIds)));
            return;
        }

        state.completed = true;
        complete(gameData, discardEffect, state);
    }

    private void complete(GameData gameData, TargetOpponentsDiscardThenDrawEffect effect,
                          TargetOpponentsDiscardThenDrawState state) {
        for (TargetOpponentsDiscardThenDrawState.SelectedDiscard selected : state.selectedDiscards) {
            if (!selected.wasDiscarded() || selected.manaValue() < effect.minimumManaValue()) {
                drawService.resolveDrawCard(gameData, state.controllerId);
            }
        }
        for (UUID ignored : state.noDiscardPlayers) {
            drawService.resolveDrawCard(gameData, state.controllerId);
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private List<UUID> targetsInTurnOrder(GameData gameData, List<UUID> targetIds) {
        List<UUID> orderedPlayers = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
            orderedPlayers.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameData.playerIds.contains(playerId) && !orderedPlayers.contains(playerId)) {
                orderedPlayers.add(playerId);
            }
        }

        List<UUID> orderedTargets = new ArrayList<>();
        for (UUID playerId : orderedPlayers) {
            if (targetIds.contains(playerId)) {
                orderedTargets.add(playerId);
            }
        }
        return orderedTargets;
    }
}
