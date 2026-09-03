package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetOpponentsDiscardThenDrawState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsThenControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves each opponent's discard and the controller's matching draws. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsThenControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsThenControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetOpponentsDiscardThenDrawState state = gameData.targetOpponentsDiscardThenDraw;

        if (state.completed) {
            complete(gameData, state);
            return;
        }

        if (!state.active) {
            state.reset();
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.remainingTargetIds.addAll(orderedOpponents(gameData, entry.getControllerId()));
        }

        while (!state.remainingTargetIds.isEmpty()) {
            UUID opponentId = state.remainingTargetIds.removeFirst();
            gameData.discardCausedByOpponent = true;
            if (gameQueryService.isDiscardPrevented(gameData, opponentId)) {
                continue;
            }
            if (gameData.playerHands.getOrDefault(opponentId, List.of()).isEmpty()) {
                state.noDiscardPlayers.add(opponentId);
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginDiscardChoice(gameData, opponentId, 1,
                    DiscardFollowUp.targetOpponentsDiscardThenDraw(
                            List.copyOf(state.remainingTargetIds)));
            return;
        }

        state.completed = true;
        complete(gameData, state);
    }

    private void complete(GameData gameData, TargetOpponentsDiscardThenDrawState state) {
        for (TargetOpponentsDiscardThenDrawState.SelectedDiscard selected : state.selectedDiscards) {
            if (selected.wasDiscarded()) {
                drawService.resolveDrawCard(gameData, state.controllerId);
            }
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private List<UUID> orderedOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
