package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Winnowing's per-player creature choices and type-sharing sacrifice sweep. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, entry.getControllerId(), apnapPlayers(gameData), 0, Map.of(),
                entry.getCard().getName());
    }

    /** Continues the per-player choices after the controller selects a creature. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.WinnowingChoice context) {
        Map<UUID, UUID> chosenByPlayer = new LinkedHashMap<>(context.chosenByPlayer());
        chosenByPlayer.put(context.playerIds().get(context.playerIndex()), chosenIds.getFirst());
        step(gameData, context.controllerId(), context.playerIds(), context.playerIndex() + 1,
                chosenByPlayer, context.sourceName());
    }

    private void step(GameData gameData, UUID controllerId, List<UUID> playerIds, int playerIndex,
            Map<UUID, UUID> chosenByPlayer, String sourceName) {
        Map<UUID, UUID> choices = new LinkedHashMap<>(chosenByPlayer);

        while (playerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(playerIndex);
            playerIndex++;

            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, controllerId)) {
                continue;
            }

            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, playerId,
                    ignored -> true);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                choices.put(playerId, candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, controllerId, candidates, 1,
                    new MultiPermanentChoiceContext.WinnowingChoice(controllerId, playerIds,
                            playerIndex - 1, choices, sourceName),
                    sourceName + " — choose a creature for " + gameData.playerIdToName.get(playerId) + ".");
            return;
        }

        sacrificeNonsharingCreatures(gameData, playerIds, choices, sourceName);
    }

    private void sacrificeNonsharingCreatures(GameData gameData, List<UUID> playerIds,
            Map<UUID, UUID> chosenByPlayer, String sourceName) {
        List<UUID> toSacrifice = new ArrayList<>();
        for (UUID playerId : playerIds) {
            UUID chosenId = chosenByPlayer.get(playerId);
            Permanent chosen = chosenId == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, chosenId);
            if (chosen == null) {
                continue;
            }

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!permanent.getId().equals(chosenId)
                        && gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.shareCreatureType(gameData, permanent, chosen)) {
                    toSacrifice.add(permanent.getId());
                }
            }
        }

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no creatures are sacrificed."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} nonsharing creatures", gameData.id, sourceName,
                toSacrifice.size());
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
