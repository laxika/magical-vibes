package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExpropriateEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Expropriate's vote and money choices. */
@Component
@RequiredArgsConstructor
public class ExpropriateEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExpropriateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new ArrayList<>(), 0, entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice, UUID voterId,
                             ChoiceContext.ExpropriateChoice context) {
        if (!ChoiceContext.ExpropriateChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Expropriate vote: " + choice);
        }

        List<UUID> moneyVoterIds = new ArrayList<>(context.moneyVoterIds());
        int timeVotes = context.timeVotes();
        if (ChoiceContext.ExpropriateChoice.TIME.equals(choice)) {
            timeVotes++;
        } else {
            moneyVoterIds.add(voterId);
        }

        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                moneyVoterIds, timeVotes, context.sourceName());
    }

    public void completePermanentChoice(GameData gameData, List<UUID> permanentIds,
                                         MultiPermanentChoiceContext.ExpropriatePermanentChoice context) {
        if (!permanentIds.isEmpty()) {
            gainControlIfStillOwnedBy(gameData, context.effectControllerId(), context.voterId(),
                    permanentIds.getFirst(), context.sourceName());
        }
        beginNextMoneyChoice(gameData, context.remainingMoneyVoterIds(), context.effectControllerId(),
                context.timeVotes(), context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, List<UUID> moneyVoterIds,
                               int timeVotes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            beginNextMoneyChoice(gameData, moneyVoterIds, effectControllerId, timeVotes, sourceName);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.ExpropriateChoice(
                        effectControllerId, remaining, moneyVoterIds, timeVotes, sourceName),
                ChoiceContext.ExpropriateChoice.OPTIONS,
                sourceName + " — vote for time or money."));
    }

    private void beginNextMoneyChoice(GameData gameData, List<UUID> moneyVoterIds,
                                      UUID effectControllerId, int timeVotes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(moneyVoterIds);
        while (!remaining.isEmpty()) {
            UUID voterId = remaining.removeFirst();
            List<UUID> candidates = permanentIdsOwnedBy(gameData, voterId);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                gainControlIfStillOwnedBy(gameData, effectControllerId, voterId,
                        candidates.getFirst(), sourceName);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, effectControllerId, candidates, 1,
                    new MultiPermanentChoiceContext.ExpropriatePermanentChoice(
                            effectControllerId, voterId, remaining, timeVotes, sourceName),
                    sourceName + " — choose a permanent owned by "
                            + gameData.playerIdToName.getOrDefault(voterId, "that voter") + ".");
            return;
        }

        insertExtraTurns(gameData, timeVotes);
    }

    private void insertExtraTurns(GameData gameData, int timeVotes) {
        if (timeVotes == 0) {
            return;
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Expropriate resolution is not resumable");
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                List.of(new ControllerExtraTurnEffect(timeVotes)));
    }

    private void gainControlIfStillOwnedBy(GameData gameData, UUID effectControllerId,
                                           UUID voterId, UUID permanentId, String sourceName) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null || !voterId.equals(gameData.defaultControllerOf(permanentId))) {
            return;
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        creatureControlService.applyControlEffect(gameData, effectControllerId, permanent,
                controlEffect, EffectDuration.PERMANENT, null, sourceName);
    }

    private List<UUID> permanentIdsOwnedBy(GameData gameData, UUID ownerId) {
        List<UUID> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (ownerId.equals(gameData.defaultControllerOf(permanent.getId()))) {
                    candidates.add(permanent.getId());
                }
            }
        }
        return candidates;
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
