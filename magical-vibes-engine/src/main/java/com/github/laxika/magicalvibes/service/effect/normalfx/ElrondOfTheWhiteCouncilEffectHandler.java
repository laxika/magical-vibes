package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantAttackCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ElrondOfTheWhiteCouncilEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Elrond's secret fellowship-or-aid vote. */
@Component
@RequiredArgsConstructor
public class ElrondOfTheWhiteCouncilEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final VotingFinishedSupport votingFinishedSupport;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GrantStaticEffectToTargetEffectHandler grantStaticEffectToTargetEffectHandler;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ElrondOfTheWhiteCouncilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), List.of(), 0, entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice, UUID voterId,
                             ChoiceContext.ElrondOfTheWhiteCouncilChoice context) {
        if (!ChoiceContext.ElrondOfTheWhiteCouncilChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Elrond of the White Council vote: " + choice);
        }

        List<UUID> fellowshipVoterIds = new ArrayList<>(context.fellowshipVoterIds());
        int aidVotes = context.aidVotes();
        if (ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP.equals(choice)) {
            fellowshipVoterIds.add(voterId);
        } else {
            aidVotes++;
        }

        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                fellowshipVoterIds, aidVotes, context.sourceName());
    }

    public void completeFellowshipChoice(GameData gameData, List<UUID> selectedIds,
                                         MultiPermanentChoiceContext.ElrondFellowshipChoice context) {
        List<UUID> chosenCreatureIds = new ArrayList<>(context.chosenCreatureIds());
        if (!selectedIds.isEmpty()) {
            chosenCreatureIds.add(selectedIds.getFirst());
        }
        beginNextFellowshipChoice(gameData, context.remainingVoterIds(), chosenCreatureIds,
                context.effectControllerId(), context.aidVotes(), context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, List<UUID> fellowshipVoterIds,
                               int aidVotes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            beginNextFellowshipChoice(gameData, fellowshipVoterIds, List.of(), effectControllerId,
                    aidVotes, sourceName);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.ElrondOfTheWhiteCouncilChoice(
                        effectControllerId, remaining, fellowshipVoterIds, aidVotes, sourceName),
                ChoiceContext.ElrondOfTheWhiteCouncilChoice.OPTIONS,
                sourceName + " — vote for fellowship or aid."));
    }

    private void beginNextFellowshipChoice(GameData gameData, List<UUID> remainingVoterIds,
                                           List<UUID> chosenCreatureIds, UUID effectControllerId,
                                           int aidVotes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingVoterIds);
        while (!remaining.isEmpty()) {
            UUID voterId = remaining.removeFirst();
            List<UUID> candidates = controlledCreatureIds(gameData, voterId);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                List<UUID> chosen = new ArrayList<>(chosenCreatureIds);
                chosen.add(candidates.getFirst());
                chosenCreatureIds = chosen;
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, voterId, candidates, 1,
                    new MultiPermanentChoiceContext.ElrondFellowshipChoice(
                            effectControllerId, remaining, chosenCreatureIds, aidVotes, sourceName),
                    sourceName + " — choose a creature you control for fellowship.");
            return;
        }

        resolveResults(gameData, effectControllerId, chosenCreatureIds, aidVotes, sourceName);
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private void resolveResults(GameData gameData, UUID effectControllerId,
                                List<UUID> chosenCreatureIds, int aidVotes, String sourceName) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Elrond of the White Council resolution is not resumable");
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        GrantStaticEffectToTargetEffect grantRestriction =
                new GrantStaticEffectToTargetEffect(new CantAttackCardOwnerEffect());
        Set<UUID> uniqueChosenIds = new LinkedHashSet<>(chosenCreatureIds);
        for (UUID chosenCreatureId : uniqueChosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, chosenCreatureId);
            if (permanent == null) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, effectControllerId, permanent,
                    controlEffect, EffectDuration.PERMANENT, null, sourceName);

            StackEntry grantEntry = new StackEntry(entry.getEntryType(), entry.getCard(), effectControllerId,
                    entry.getDescription(), List.of(grantRestriction), permanent.getId(),
                    entry.getSourcePermanentId());
            grantEntry.setNonTargeting(true);
            grantStaticEffectToTargetEffectHandler.resolve(gameData, grantEntry, grantRestriction);
        }

        if (aidVotes == 0) {
            return;
        }
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(effectControllerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, permanent, CounterType.PLUS_ONE_PLUS_ONE, aidVotes);
            }
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
