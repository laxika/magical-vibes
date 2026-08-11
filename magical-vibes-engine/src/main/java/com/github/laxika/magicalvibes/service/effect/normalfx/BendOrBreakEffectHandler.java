package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingBendOrBreak;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BendOrBreakEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BendOrBreakEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BendOrBreakEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        List<PendingBendOrBreak.PlayerPiles> playerPiles = new ArrayList<>();
        for (UUID playerId : apnapPlayers(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            List<UUID> landIds = battlefield.stream()
                    .filter(permanent -> !permanent.getCard().isToken()
                            && gameQueryService.isLand(gameData, permanent))
                    .map(Permanent::getId)
                    .toList();
            if (!landIds.isEmpty()) {
                playerPiles.add(new PendingBendOrBreak.PlayerPiles(
                        playerId, landIds, List.of(), List.of(), null, null));
            }
        }

        if (playerPiles.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no nontoken lands are separated."));
            return;
        }

        gameData.queueInteraction(new PendingBendOrBreak(
                entry.getControllerId(), sourceName, playerPiles, 0));
        beginNextLandSeparation(gameData);
    }

    public void completeLandSeparation(GameData gameData, List<UUID> pile1Ids) {
        PendingBendOrBreak state = gameData.pollPendingInteraction(PendingBendOrBreak.class);
        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        List<UUID> pile2Ids = current.landIds().stream()
                .filter(id -> !pile1Ids.contains(id))
                .toList();
        List<PendingBendOrBreak.PlayerPiles> updated = replaceCurrent(state,
                new PendingBendOrBreak.PlayerPiles(current.playerId(), current.landIds(),
                        pile1Ids, pile2Ids, null, null));

        int nextIndex = state.currentPlayerIndex() + 1;
        if (nextIndex >= updated.size()) {
            PendingBendOrBreak opponentChoiceState = new PendingBendOrBreak(
                    state.controllerId(), state.sourceName(), updated, 0);
            gameData.queueInteraction(opponentChoiceState);
            beginNextOpponentChoice(gameData);
        } else {
            gameData.queueInteraction(new PendingBendOrBreak(
                    state.controllerId(), state.sourceName(), updated, nextIndex));
            beginNextLandSeparation(gameData);
        }
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId) {
        PendingBendOrBreak state = gameData.pollPendingInteraction(PendingBendOrBreak.class);
        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        List<PendingBendOrBreak.PlayerPiles> updated = replaceCurrent(state,
                new PendingBendOrBreak.PlayerPiles(current.playerId(), current.landIds(),
                        current.pile1Ids(), current.pile2Ids(), opponentId, null));
        PendingBendOrBreak updatedState = new PendingBendOrBreak(
                state.controllerId(), state.sourceName(), updated, state.currentPlayerIndex());
        gameData.queueInteraction(updatedState);
        beginPileChoice(gameData, updatedState);
    }

    public void completePileChoice(GameData gameData, boolean pile1Chosen) {
        PendingBendOrBreak state = gameData.pollPendingInteraction(PendingBendOrBreak.class);
        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        List<PendingBendOrBreak.PlayerPiles> updated = replaceCurrent(state,
                new PendingBendOrBreak.PlayerPiles(current.playerId(), current.landIds(),
                        current.pile1Ids(), current.pile2Ids(), current.opponentId(), pile1Chosen));
        PendingBendOrBreak updatedState = new PendingBendOrBreak(
                state.controllerId(), state.sourceName(), updated, state.currentPlayerIndex() + 1);

        if (updatedState.currentPlayerIndex() >= updatedState.playerPiles().size()) {
            resolvePiles(gameData, updatedState);
            return;
        }

        gameData.queueInteraction(updatedState);
        beginNextOpponentChoice(gameData);
    }

    private void beginNextLandSeparation(GameData gameData) {
        PendingBendOrBreak state = gameData.peekPendingInteraction(PendingBendOrBreak.class);
        if (state.currentPlayerIndex() >= state.playerPiles().size()) {
            beginNextOpponentChoice(gameData);
            return;
        }

        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        playerInputService.beginMultiPermanentChoice(gameData, current.playerId(), current.landIds(),
                current.landIds().size(), "Separate your nontoken lands into two piles. "
                        + "Select lands for Pile 1 (unselected lands form Pile 2).");
    }

    private void beginNextOpponentChoice(GameData gameData) {
        PendingBendOrBreak state = gameData.peekPendingInteraction(PendingBendOrBreak.class);
        if (state.currentPlayerIndex() >= state.playerPiles().size()) {
            resolvePiles(gameData, state);
            return;
        }

        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(current.playerId()))
                .toList();
        if (opponentIds.isEmpty()) {
            log.info("Game {} - {} has no opponent to choose a Bend or Break pile", gameData.id,
                    gameData.playerIdToName.get(current.playerId()));
            List<PendingBendOrBreak.PlayerPiles> updated = replaceCurrent(state,
                    new PendingBendOrBreak.PlayerPiles(current.playerId(), current.landIds(),
                            current.pile1Ids(), current.pile2Ids(), null, null));
            PendingBendOrBreak next = new PendingBendOrBreak(
                    state.controllerId(), state.sourceName(), updated, state.currentPlayerIndex() + 1);
            if (next.currentPlayerIndex() >= next.playerPiles().size()) {
                resolvePiles(gameData, next);
            } else {
                gameData.queueInteraction(next);
                beginNextOpponentChoice(gameData);
            }
            return;
        }

        if (opponentIds.size() == 1) {
            completeOpponentChoice(gameData, opponentIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.BendOrBreakOpponentChoice(current.playerId()));
        playerInputService.beginAnyTargetChoice(gameData, current.playerId(), List.of(), opponentIds,
                "Choose an opponent to choose a pile for "
                        + gameData.playerIdToName.get(current.playerId()) + "'s lands.");
    }

    private void beginPileChoice(GameData gameData, PendingBendOrBreak state) {
        PendingBendOrBreak.PlayerPiles current = currentPlayer(state);
        String pile1Description = describePile(gameData, current.pile1Ids());
        String pile2Description = describePile(gameData, current.pile2Ids());
        String playerName = gameData.playerIdToName.get(current.playerId());
        String prompt = "Choose a pile for " + playerName + "'s lands. Yes = Pile 1 ("
                + pile1Description + "), No = Pile 2 (" + pile2Description + ").";
        gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                null, current.opponentId(), List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    private void resolvePiles(GameData gameData, PendingBendOrBreak state) {
        List<Permanent> toDestroy = new ArrayList<>();
        List<Permanent> toTap = new ArrayList<>();
        for (PendingBendOrBreak.PlayerPiles playerPiles : state.playerPiles()) {
            if (playerPiles.pile1Chosen() == null) {
                continue;
            }
            List<UUID> chosenIds = playerPiles.pile1Chosen() ? playerPiles.pile1Ids() : playerPiles.pile2Ids();
            List<UUID> otherIds = playerPiles.pile1Chosen() ? playerPiles.pile2Ids() : playerPiles.pile1Ids();
            addCurrentLands(gameData, chosenIds, toDestroy);
            addCurrentLands(gameData, otherIds, toTap);
        }

        destructionSupport.destroyBatch(gameData, toDestroy, state.sourceName(), false);
        for (Permanent permanent : toTap) {
            if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
                continue;
            }
            tapUntapSupport.tapPermanent(gameData, permanent);
            gameLogService.append(gameData, GameLog.text(permanent.getCard().getName() + " is tapped by "
                    + state.sourceName() + "."));
        }
        if (toDestroy.isEmpty() && toTap.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(state.sourceName() + " resolves without affecting any lands."));
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private void addCurrentLands(GameData gameData, List<UUID> ids, List<Permanent> destination) {
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null && !permanent.getCard().isToken()
                    && gameQueryService.isLand(gameData, permanent)) {
                destination.add(permanent);
            }
        }
    }

    private PendingBendOrBreak.PlayerPiles currentPlayer(PendingBendOrBreak state) {
        return state.playerPiles().get(state.currentPlayerIndex());
    }

    private List<PendingBendOrBreak.PlayerPiles> replaceCurrent(PendingBendOrBreak state,
                                                                  PendingBendOrBreak.PlayerPiles replacement) {
        List<PendingBendOrBreak.PlayerPiles> updated = new ArrayList<>(state.playerPiles());
        updated.set(state.currentPlayerIndex(), replacement);
        return updated;
    }

    private String describePile(GameData gameData, List<UUID> ids) {
        if (ids.isEmpty()) {
            return "empty";
        }
        List<String> names = ids.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .map(permanent -> permanent.getCard().getName())
                .toList();
        return names.isEmpty() ? "empty" : String.join(", ", names);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
