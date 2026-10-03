package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandsThenSacrificeRestEffect;
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

/** Resolves Planetary Annihilation-style land choices before sacrificing the remaining lands. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesLandsThenSacrificeRestEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesLandsThenSacrificeRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerChoosesLandsThenSacrificeRestEffect choiceEffect =
                (EachPlayerChoosesLandsThenSacrificeRestEffect) effect;
        step(gameData, apnapPlayers(gameData), 0, List.of(), choiceEffect.landsToKeep(),
                entry.getCard().getName());
    }

    /** Continues after the current player chooses the lands to keep. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesLandsThenSacrificeRestChoice context) {
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
                        new MultiPermanentChoiceContext.EachPlayerChoosesLandsThenSacrificeRestChoice(
                                playerIds, currentPlayerIndex, requiredCount, allKeptIds, sourceName),
                        sourceName + " — choose " + requiredCount + " lands to keep.");
                return;
            }

            allKeptIds.addAll(candidates);
        }

        sacrificeRest(gameData, new HashSet<>(allKeptIds), sourceName);
    }

    private List<UUID> landIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> gameQueryService.isLand(gameData, permanent));
    }

    private void sacrificeRest(GameData gameData, Set<UUID> keptIds, String sourceName) {
        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isLand(gameData, permanent) && !keptIds.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        });

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but nobody sacrifices a land."));
            return;
        }

        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} lands", gameData.id, sourceName, toSacrifice.size());
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
