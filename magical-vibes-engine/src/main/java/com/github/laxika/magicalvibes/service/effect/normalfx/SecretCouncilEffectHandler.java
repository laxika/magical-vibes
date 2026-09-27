package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SecretCouncilEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Secret council votes and applies the resulting stun counters and taps. */
@Component
@RequiredArgsConstructor
public class SecretCouncilEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final TapUntapSupport tapUntapSupport;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SecretCouncilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new LinkedHashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, List<UUID> permanentIds,
                             MultiPermanentChoiceContext.SecretCouncilChoice context) {
        Map<UUID, Integer> votes = new LinkedHashMap<>(context.votes());
        votes.merge(permanentIds.getFirst(), 1, Integer::sum);
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, Map<UUID, Integer> votes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            List<UUID> candidates = creatureIdsNotControlledBy(gameData, effectControllerId);

            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                votingFinishedSupport.recordVote(gameData, effectControllerId, choosingPlayerId,
                        "permanent:" + candidates.getFirst());
                votes.merge(candidates.getFirst(), 1, Integer::sum);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, choosingPlayerId, candidates, 1,
                    new MultiPermanentChoiceContext.SecretCouncilChoice(
                            effectControllerId, remaining, votes, sourceName),
                    sourceName + " — secretly vote for a creature you don't control.");
            return;
        }

        votingFinishedSupport.finishVoting(gameData, effectControllerId);
        resolveVotes(gameData, votes);
    }

    private List<UUID> creatureIdsNotControlledBy(GameData gameData, UUID effectControllerId) {
        List<UUID> candidates = new ArrayList<>();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(effectControllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    candidates.add(permanent.getId());
                }
            }
        }
        return candidates;
    }

    private void resolveVotes(GameData gameData, Map<UUID, Integer> votes) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Secret council resolution is not resumable");
        }

        for (Map.Entry<UUID, Integer> vote : votes.entrySet()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, vote.getKey());
            if (permanent == null) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, permanent, CounterType.STUN, vote.getValue());
            tapUntapSupport.tapPermanent(gameData, permanent);
        }
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
