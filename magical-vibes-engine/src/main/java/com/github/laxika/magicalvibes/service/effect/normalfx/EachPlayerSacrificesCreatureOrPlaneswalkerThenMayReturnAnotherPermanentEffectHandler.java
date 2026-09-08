package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Deadly Brew's simultaneous sacrifice and per-player return choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        continueChoices(gameData, entry.getControllerId(), apnapPlayers(gameData), 0,
                Map.of(), entry.getCard().getName());
    }

    /** Continues the APNAP sacrifice choices after one player selects a permanent. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice context) {
        Map<UUID, UUID> chosenByPlayer = new LinkedHashMap<>(context.chosenByPlayer());
        if (!chosenIds.isEmpty()) {
            chosenByPlayer.put(context.playerIds().get(context.playerIndex()), chosenIds.getFirst());
        }
        continueChoices(gameData, context.effectControllerId(), context.playerIds(),
                context.playerIndex() + 1, chosenByPlayer, context.sourceName());
    }

    private void continueChoices(GameData gameData, UUID effectControllerId, List<UUID> playerIds,
            int playerIndex, Map<UUID, UUID> chosenByPlayer, String sourceName) {
        Map<UUID, UUID> choices = new LinkedHashMap<>(chosenByPlayer);
        int currentPlayerIndex = playerIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            currentPlayerIndex++;
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, effectControllerId)) {
                continue;
            }

            List<UUID> candidates = eligiblePermanentIds(gameData, playerId);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                choices.put(playerId, candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, 1,
                    new MultiPermanentChoiceContext.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice(
                            effectControllerId, playerIds, currentPlayerIndex - 1, choices, sourceName),
                    sourceName + " — choose a creature or planeswalker to sacrifice.");
            return;
        }

        sacrificeAndQueueReturns(gameData, playerIds, choices, sourceName);
    }

    private List<UUID> eligiblePermanentIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> gameQueryService.isCreature(gameData, permanent)
                        || gameQueryService.isPlaneswalker(gameData, permanent));
    }

    private void sacrificeAndQueueReturns(GameData gameData, List<UUID> playerIds,
            Map<UUID, UUID> choices, String sourceName) {
        List<UUID> permanentIds = new ArrayList<>();
        Map<UUID, UUID> sacrificedCardIds = new LinkedHashMap<>();
        for (UUID playerId : playerIds) {
            UUID permanentId = choices.get(playerId);
            Permanent permanent = permanentId == null
                    ? null : gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }
            permanentIds.add(permanentId);
            sacrificedCardIds.put(playerId, permanent.getCard().getId());
        }

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no permanents are sacrificed."));
            return;
        }

        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        for (UUID playerId : playerIds) {
            UUID sacrificedCardId = sacrificedCardIds.get(playerId);
            if (sacrificedCardId != null) {
                gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                        playerId, 1, new CardIsPermanentPredicate(), GraveyardChoiceDestination.HAND,
                        true, false, false, false, Set.of(), Set.of(sacrificedCardId)));
            }
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
        log.info("Game {} - {} sacrifices {} eligible permanents", gameData.id, sourceName,
                permanentIds.size());
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
