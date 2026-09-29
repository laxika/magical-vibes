package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MobVerdictEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
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

/** Resolves Mob Verdict's secret player vote and its vote-counted results. */
@Component
@RequiredArgsConstructor
public class MobVerdictEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MobVerdictEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new LinkedHashMap<>(), entry.getCard().getName(), entry);
    }

    public void completeVote(GameData gameData, List<UUID> selectedIds,
                             MultiPermanentChoiceContext.MobVerdictChoice context) {
        Map<UUID, Integer> votes = new LinkedHashMap<>(context.votes());
        votes.merge(selectedIds.getFirst(), 1, Integer::sum);
        beginNextVote(gameData, context.remainingVoterIds(), context.effectControllerId(), votes,
                context.sourceName(), gameData.pendingEffectResolutionEntry);
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingVoterIds,
                               UUID effectControllerId, Map<UUID, Integer> votes, String sourceName,
                               StackEntry resolutionEntry) {
        List<UUID> remaining = new ArrayList<>(remainingVoterIds);
        while (!remaining.isEmpty()) {
            UUID voterId = remaining.removeFirst();
            List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(voterId))
                    .toList();

            if (validPlayerIds.isEmpty()) {
                continue;
            }
            if (validPlayerIds.size() == 1) {
                UUID votedFor = validPlayerIds.getFirst();
                votingFinishedSupport.recordVote(gameData, effectControllerId, voterId,
                        "player:" + votedFor);
                votes.merge(votedFor, 1, Integer::sum);
                continue;
            }

            playerInputService.beginMultiPermanentOrPlayerChoice(
                    gameData, voterId, List.of(), validPlayerIds, 1,
                    new MultiPermanentChoiceContext.MobVerdictChoice(
                            effectControllerId, remaining, votes, sourceName),
                    sourceName + " - secretly vote for another player.");
            return;
        }

        votingFinishedSupport.finishVoting(gameData, effectControllerId);
        resolveVotes(gameData, effectControllerId, votes, resolutionEntry);
    }

    private void resolveVotes(GameData gameData, UUID effectControllerId, Map<UUID, Integer> votes,
                              StackEntry entry) {
        if (entry == null) {
            throw new IllegalStateException("Mob Verdict resolution is not resumable");
        }

        for (Map.Entry<UUID, Integer> vote : votes.entrySet()) {
            if (vote.getKey().equals(effectControllerId)) {
                continue;
            }
            for (int i = 0; i < vote.getValue(); i++) {
                dealDamageToPlayerAndCreatures(gameData, entry, vote.getKey());
            }
        }

        int controllerVotes = votes.getOrDefault(effectControllerId, 0);
        if (controllerVotes > 0) {
            playerInteractionSupport.applyDrawCards(gameData, effectControllerId, controllerVotes);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private void dealDamageToPlayerAndCreatures(GameData gameData, StackEntry entry, UUID playerId) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, 2, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);

        List<Permanent> battlefield = new ArrayList<>(gameData.playerBattlefields.getOrDefault(playerId, List.of()));
        damageSupport.damageFilteredCreatures(gameData, entry, damage, battlefield,
                permanent -> gameQueryService.isCreature(gameData, permanent));
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
