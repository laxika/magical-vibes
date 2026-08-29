package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandsThenDestroyRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Urza's Sylex-style land choices before destroying the remaining permanents. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesLandsThenDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesLandsThenDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerChoosesLandsThenDestroyRestEffect choiceEffect =
                (EachPlayerChoosesLandsThenDestroyRestEffect) effect;
        step(gameData, apnapPlayers(gameData), 0, List.of(), choiceEffect.landsToKeep(),
                entry.getCard().getName());
    }

    /** Continues after the current player chooses the lands to keep. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesLandsThenDestroyRestChoice context) {
        List<UUID> keptIds = new ArrayList<>(context.keptIds());
        keptIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, keptIds,
                context.requiredCount(), context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
                      List<UUID> keptIds, int requiredCount, String sourceName) {
        List<UUID> allKeptIds = new ArrayList<>(keptIds);

        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = landIds(gameData, playerId);

            if (requiredCount > 0 && candidates.size() > requiredCount) {
                playerInputService.beginMultiPermanentChoice(
                        gameData, playerId, candidates, requiredCount,
                        new MultiPermanentChoiceContext.EachPlayerChoosesLandsThenDestroyRestChoice(
                                playerIds, currentPlayerIndex, requiredCount, allKeptIds, sourceName),
                        sourceName + " — choose " + requiredCount + " lands to keep.");
                return;
            }

            allKeptIds.addAll(candidates);
        }

        destroyRest(gameData, new HashSet<>(allKeptIds), sourceName);
    }

    private List<UUID> landIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> gameQueryService.isLand(gameData, permanent));
    }

    private void destroyRest(GameData gameData, Set<UUID> keptIds, String sourceName) {
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!keptIds.contains(permanent.getId())) {
                    toDestroy.add(permanent);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but destroys no permanents."));
            return;
        }

        destructionSupport.destroyBatchCollecting(gameData, toDestroy, sourceName, false);
        log.info("Game {} - {} destroys {} permanents", gameData.id, sourceName, toDestroy.size());
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
