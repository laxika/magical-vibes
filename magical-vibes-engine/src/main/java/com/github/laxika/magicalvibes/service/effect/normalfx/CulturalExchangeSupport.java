package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
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
public class CulturalExchangeSupport {

    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    public void begin(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() != 2) {
            return;
        }

        UUID firstPlayerId = targetIds.getFirst();
        UUID secondPlayerId = targetIds.get(1);
        if (!gameData.playerIds.contains(firstPlayerId)
                || !gameData.playerIds.contains(secondPlayerId)
                || firstPlayerId.equals(secondPlayerId)) {
            return;
        }

        List<UUID> firstCreatureIds = creatureIds(gameData, firstPlayerId);
        int maxCount = Math.min(firstCreatureIds.size(), creatureIds(gameData, secondPlayerId).size());
        if (maxCount == 0) {
            return;
        }

        Card sourceCard = entry.getCard();
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), firstCreatureIds, maxCount,
                new MultiPermanentChoiceContext.CulturalExchange(
                        sourceCard, entry.getControllerId(), firstPlayerId, secondPlayerId, List.of(), true),
                sourceCard.getName() + " — choose any number of creatures controlled by "
                        + gameData.playerIdToName.get(firstPlayerId) + ".");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.CulturalExchange context) {
        if (context.firstSelection()) {
            completeFirstChoice(gameData, permanentIds, context);
        } else {
            completeSecondChoice(gameData, permanentIds, context);
        }
    }

    private void completeFirstChoice(GameData gameData, List<UUID> firstChosenIds,
                                     MultiPermanentChoiceContext.CulturalExchange context) {
        if (firstChosenIds.isEmpty() || !allControlledCreatures(gameData, firstChosenIds, context.firstPlayerId())) {
            finish(gameData);
            return;
        }

        List<UUID> secondCreatureIds = creatureIds(gameData, context.secondPlayerId());
        if (secondCreatureIds.size() < firstChosenIds.size()) {
            finish(gameData);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, context.chooserId(), secondCreatureIds,
                firstChosenIds.size(),
                new MultiPermanentChoiceContext.CulturalExchange(
                        context.sourceCard(), context.chooserId(), context.firstPlayerId(), context.secondPlayerId(),
                        firstChosenIds, false),
                context.sourceCard().getName() + " — choose " + firstChosenIds.size()
                        + " creature" + (firstChosenIds.size() == 1 ? "" : "s") + " controlled by "
                        + gameData.playerIdToName.get(context.secondPlayerId()) + ".");
    }

    private void completeSecondChoice(GameData gameData, List<UUID> secondChosenIds,
                                      MultiPermanentChoiceContext.CulturalExchange context) {
        if (secondChosenIds.size() != context.firstChosenIds().size()
                || !allControlledCreatures(gameData, context.firstChosenIds(), context.firstPlayerId())
                || !allControlledCreatures(gameData, secondChosenIds, context.secondPlayerId())) {
            finish(gameData);
            return;
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        List<Permanent> firstCreatures = findPermanents(gameData, context.firstChosenIds());
        List<Permanent> secondCreatures = findPermanents(gameData, secondChosenIds);
        for (Permanent permanent : firstCreatures) {
            creatureControlService.applyControlEffect(gameData, context.secondPlayerId(), permanent,
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null,
                    context.sourceCard().getName());
        }
        for (Permanent permanent : secondCreatures) {
            creatureControlService.applyControlEffect(gameData, context.firstPlayerId(), permanent,
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null,
                    context.sourceCard().getName());
        }

        gameLogService.append(gameData, GameLog.builder().card(context.sourceCard())
                .text(": the chosen creatures exchange controllers.").build());
        log.info("Game {} - {} exchanges {} creature(s) between {} and {}", gameData.id,
                context.sourceCard().getName(), firstCreatures.size(),
                gameData.playerIdToName.get(context.firstPlayerId()),
                gameData.playerIdToName.get(context.secondPlayerId()));
        finish(gameData);
    }

    private boolean allControlledCreatures(GameData gameData, List<UUID> permanentIds, UUID controllerId) {
        return permanentIds.stream().allMatch(id -> {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            return permanent != null
                    && controllerId.equals(gameQueryService.findPermanentController(gameData, id))
                    && gameQueryService.isCreature(gameData, permanent);
        });
    }

    private List<UUID> creatureIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UUID> creatureIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        }
        return creatureIds;
    }

    private List<Permanent> findPermanents(GameData gameData, List<UUID> permanentIds) {
        List<Permanent> permanents = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                permanents.add(permanent);
            }
        }
        return permanents;
    }

    private void finish(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
