package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteForCreatureThenDestroyMostVotedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Vault 11: Voter's Dilemma's secret creature vote. */
@Component
@RequiredArgsConstructor
public class VoteForCreatureThenDestroyMostVotedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VoteForCreatureThenDestroyMostVotedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, List<UUID> permanentIds,
                             MultiPermanentChoiceContext.VoteForCreatureThenDestroyMostVotedChoice context) {
        Map<UUID, Integer> votes = new HashMap<>(context.votes());
        if (!permanentIds.isEmpty()) {
            votes.merge(permanentIds.getFirst(), 1, Integer::sum);
        }
        beginNextVote(gameData, context.remainingPlayerIds(), votes, context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               Map<UUID, Integer> votes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            List<UUID> creatureIds = creatureIds(gameData);
            if (creatureIds.isEmpty()) {
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, choosingPlayerId, creatureIds, 1,
                    new MultiPermanentChoiceContext.VoteForCreatureThenDestroyMostVotedChoice(
                            remaining, votes, sourceName),
                    sourceName + " — vote for up to one creature.");
            return;
        }

        finishVote(gameData, votes, sourceName);
    }

    private void finishVote(GameData gameData, Map<UUID, Integer> votes, String sourceName) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Voter's Dilemma resolution is not resumable");
        }

        if (votes.isEmpty()) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(new EachPlayerDrawsCardEffect(1)));
            return;
        }

        int mostVotes = votes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Permanent> toDestroy = votes.entrySet().stream()
                .filter(entry -> entry.getValue() == mostVotes)
                .map(entry -> gameQueryService.findPermanentById(gameData, entry.getKey()))
                .filter(permanent -> permanent != null && gameQueryService.isCreature(gameData, permanent))
                .toList();
        destructionSupport.destroyBatch(gameData, toDestroy, sourceName, false);
    }

    private List<UUID> creatureIds(GameData gameData) {
        List<UUID> creatureIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }
        return creatureIds;
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
