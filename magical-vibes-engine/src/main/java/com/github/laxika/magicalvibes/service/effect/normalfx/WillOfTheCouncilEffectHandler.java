package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfTheCouncilEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Will of the Council votes in the effect controller's turn order. */
@Component
@RequiredArgsConstructor
@Slf4j
public class WillOfTheCouncilEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WillOfTheCouncilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    /** Continues the vote after the current player selects a permanent. */
    public void completeVote(GameData gameData, List<UUID> permanentIds,
                             MultiPermanentChoiceContext.WillOfTheCouncilChoice context) {
        Map<UUID, Integer> votes = new HashMap<>(context.votes());
        UUID chosenId = permanentIds.getFirst();
        votes.merge(chosenId, 1, Integer::sum);
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, Map<UUID, Integer> votes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            List<UUID> candidates = nonlandPermanentIdsNotControlledBy(gameData, effectControllerId);

            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                votes.merge(candidates.getFirst(), 1, Integer::sum);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, choosingPlayerId, candidates, 1,
                    new MultiPermanentChoiceContext.WillOfTheCouncilChoice(
                            effectControllerId, remaining, votes, sourceName),
                    sourceName + " — vote for a nonland permanent you don't control.");
            return;
        }

        exileMostVoted(gameData, votes, sourceName);
    }

    private List<UUID> nonlandPermanentIdsNotControlledBy(GameData gameData, UUID effectControllerId) {
        List<UUID> candidates = new ArrayList<>();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(effectControllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (!gameQueryService.isLand(gameData, permanent)) {
                    candidates.add(permanent.getId());
                }
            }
        }
        return candidates;
    }

    private void exileMostVoted(GameData gameData, Map<UUID, Integer> votes, String sourceName) {
        if (votes.isEmpty()) {
            return;
        }

        int mostVotes = votes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Permanent> toExile = votes.entrySet().stream()
                .filter(entry -> entry.getValue() == mostVotes)
                .map(entry -> gameQueryService.findPermanentById(gameData, entry.getKey()))
                .filter(permanent -> permanent != null)
                .toList();

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, permanent);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} exiles {}", gameData.id, sourceName, permanent.getCard().getName());
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private List<UUID> orderStartingWith(GameData gameData, UUID firstPlayerId) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int firstIndex = orderedPlayerIds.indexOf(firstPlayerId);
        if (firstIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(firstIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, firstIndex));
        return rotated;
    }
}
