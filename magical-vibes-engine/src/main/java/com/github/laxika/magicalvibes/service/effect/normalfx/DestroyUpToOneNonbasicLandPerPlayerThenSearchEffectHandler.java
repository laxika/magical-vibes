package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Krenko's Buzzcrusher's non-targeting per-player land choices. */
@Component
@RequiredArgsConstructor
public class DestroyUpToOneNonbasicLandPerPlayerThenSearchEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextChoice(gameData, entry.getControllerId(), apnapPlayers(gameData), 0, List.of(),
                entry.getCard().getName());
    }

    /** Continues the controller's choices after one player's land selection. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.DestroyUpToOneNonbasicLandPerPlayerChoice context) {
        List<UUID> selectedIds = new ArrayList<>(context.selectedIds());
        selectedIds.addAll(chosenIds);
        beginNextChoice(gameData, context.controllerId(), context.playerIds(), context.playerIndex() + 1,
                selectedIds, context.sourceName());
    }

    private void beginNextChoice(GameData gameData, UUID controllerId, List<UUID> playerIds,
            int playerIndex, List<UUID> selectedIds, String sourceName) {
        while (playerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(playerIndex);
            List<UUID> candidates = nonbasicLandIds(gameData, playerId);
            if (candidates.isEmpty()) {
                playerIndex++;
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, controllerId, candidates, 1,
                    new MultiPermanentChoiceContext.DestroyUpToOneNonbasicLandPerPlayerChoice(
                            controllerId, playerIds, playerIndex, selectedIds, sourceName),
                    sourceName + " — choose up to one nonbasic land for "
                            + gameData.playerIdToName.get(playerId) + ".");
            return;
        }

        destroyAndSearch(gameData, selectedIds, sourceName);
    }

    private void destroyAndSearch(GameData gameData, List<UUID> selectedIds, String sourceName) {
        List<Permanent> toDestroy = new ArrayList<>();
        Map<UUID, UUID> controllerByPermanentId = new HashMap<>();
        for (UUID selectedId : selectedIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selectedId);
            if (permanent == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, selectedId);
            if (controllerId != null) {
                toDestroy.add(permanent);
                controllerByPermanentId.put(selectedId, controllerId);
            }
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, sourceName, false);
        List<UUID> destroyedControllers = destroyed.stream()
                .map(permanent -> controllerByPermanentId.get(permanent.getId()))
                .filter(controllerId -> controllerId != null)
                .toList();

        if (!destroyedControllers.isEmpty()) {
            librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData,
                    LibrarySearchFollowUp.eachPlayerBasicLand(destroyedControllers, true));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private List<UUID> nonbasicLandIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent)
                        && !gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC))
                .map(Permanent::getId)
                .toList();
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
