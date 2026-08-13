package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingWhimsOfTheFates;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WhimsOfTheFatesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class WhimsOfTheFatesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WhimsOfTheFatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<PendingWhimsOfTheFates.PlayerPiles> playerPiles = new ArrayList<>();
        for (UUID playerId : playersStartingWithController(gameData, entry.getControllerId())) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<UUID> permanentIds = battlefield.stream().map(Permanent::getId).toList();
            playerPiles.add(new PendingWhimsOfTheFates.PlayerPiles(
                    playerId, permanentIds, List.of(), List.of(), List.of()));
        }

        if (playerPiles.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getCard().getName() + " resolves without affecting any permanents."));
            return;
        }

        gameData.queueInteraction(new PendingWhimsOfTheFates(
                entry.getControllerId(), entry.getCard().getName(), playerPiles, 0, 1));
        beginNextPileSelection(gameData);
    }

    public void completePileSelection(GameData gameData, List<UUID> selectedIds) {
        PendingWhimsOfTheFates state = gameData.pollPendingInteraction(PendingWhimsOfTheFates.class);
        if (state == null) {
            throw new IllegalStateException("No pending Whims of the Fates pile selection");
        }

        PendingWhimsOfTheFates.PlayerPiles current = currentPlayer(state);
        List<UUID> remainingIds = current.permanentIds().stream()
                .filter(id -> !current.pile1Ids().contains(id) && !selectedIds.contains(id))
                .toList();

        PendingWhimsOfTheFates.PlayerPiles updatedPlayer;
        if (state.currentPile() == 1) {
            updatedPlayer = new PendingWhimsOfTheFates.PlayerPiles(
                    current.playerId(), current.permanentIds(), selectedIds, List.of(), List.of());
            List<PendingWhimsOfTheFates.PlayerPiles> updated = replaceCurrent(state, updatedPlayer);
            if (remainingIds.isEmpty()) {
                advanceToNextPlayer(gameData, state, updated);
            } else {
                gameData.queueInteraction(new PendingWhimsOfTheFates(
                        state.controllerId(), state.sourceName(), updated,
                        state.currentPlayerIndex(), 2));
                beginNextPileSelection(gameData);
            }
            return;
        }

        updatedPlayer = new PendingWhimsOfTheFates.PlayerPiles(
                current.playerId(), current.permanentIds(), current.pile1Ids(), selectedIds, remainingIds);
        advanceToNextPlayer(gameData, state, replaceCurrent(state, updatedPlayer));
    }

    private void beginNextPileSelection(GameData gameData) {
        PendingWhimsOfTheFates state = gameData.peekPendingInteraction(PendingWhimsOfTheFates.class);
        if (state == null || state.currentPlayerIndex() >= state.playerPiles().size()) {
            if (state != null) {
                resolvePiles(gameData, state);
            }
            return;
        }

        PendingWhimsOfTheFates.PlayerPiles current = currentPlayer(state);
        List<UUID> selectableIds = state.currentPile() == 1
                ? current.permanentIds()
                : current.permanentIds().stream()
                        .filter(id -> !current.pile1Ids().contains(id))
                        .toList();
        String pileName = state.currentPile() == 1 ? "Pile 1" : "Pile 2";
        String remainingPileName = state.currentPile() == 1 ? "Pile 2" : "Pile 3";
        playerInputService.beginMultiPermanentChoice(gameData, current.playerId(), selectableIds,
                selectableIds.size(), "Separate your permanents into three piles. Select permanents for "
                        + pileName + " (unselected permanents form " + remainingPileName + ").");
    }

    private void advanceToNextPlayer(GameData gameData, PendingWhimsOfTheFates state,
                                     List<PendingWhimsOfTheFates.PlayerPiles> updated) {
        int nextPlayerIndex = state.currentPlayerIndex() + 1;
        if (nextPlayerIndex >= updated.size()) {
            resolvePiles(gameData, new PendingWhimsOfTheFates(
                    state.controllerId(), state.sourceName(), updated, nextPlayerIndex, 1));
            return;
        }

        gameData.queueInteraction(new PendingWhimsOfTheFates(
                state.controllerId(), state.sourceName(), updated, nextPlayerIndex, 1));
        beginNextPileSelection(gameData);
    }

    private void resolvePiles(GameData gameData, PendingWhimsOfTheFates state) {
        List<UUID> sacrificeIds = new ArrayList<>();
        for (PendingWhimsOfTheFates.PlayerPiles playerPiles : state.playerPiles()) {
            int chosenPile = ThreadLocalRandom.current().nextInt(3);
            List<UUID> chosenIds = switch (chosenPile) {
                case 0 -> playerPiles.pile1Ids();
                case 1 -> playerPiles.pile2Ids();
                default -> playerPiles.pile3Ids();
            };

            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerPiles.playerId())
                    + " randomly chooses Pile " + (chosenPile + 1) + " for " + state.sourceName() + "."));
            if (gameQueryService.canEffectCauseSacrifice(gameData,
                    playerPiles.playerId(), state.controllerId())) {
                sacrificeIds.addAll(chosenIds);
            }
        }

        if (sacrificeIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(state.sourceName() + " resolves without sacrificing any permanents."));
        } else {
            destructionSupport.performSimultaneousSacrifice(gameData, sacrificeIds);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private PendingWhimsOfTheFates.PlayerPiles currentPlayer(PendingWhimsOfTheFates state) {
        return state.playerPiles().get(state.currentPlayerIndex());
    }

    private List<PendingWhimsOfTheFates.PlayerPiles> replaceCurrent(
            PendingWhimsOfTheFates state, PendingWhimsOfTheFates.PlayerPiles replacement) {
        List<PendingWhimsOfTheFates.PlayerPiles> updated = new ArrayList<>(state.playerPiles());
        updated.set(state.currentPlayerIndex(), replacement);
        return updated;
    }

    private List<UUID> playersStartingWithController(GameData gameData, UUID controllerId) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = orderedPlayers.indexOf(controllerId);
        if (controllerIndex <= 0) {
            return orderedPlayers;
        }

        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(controllerIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, controllerIndex));
        return rotated;
    }
}
