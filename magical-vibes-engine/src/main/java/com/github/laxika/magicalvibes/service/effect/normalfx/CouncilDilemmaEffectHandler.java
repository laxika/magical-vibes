package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CouncilDilemmaEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Tivit's council's dilemma vote. */
@Component
@RequiredArgsConstructor
public class CouncilDilemmaEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CouncilDilemmaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> votingOrder = orderStartingWith(gameData, entry.getControllerId());
        beginNextVote(gameData, votingOrder, entry.getControllerId(), 0, 0,
                entry.getCard().getName(), true);
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.CouncilDilemmaChoice context) {
        if (!ChoiceContext.CouncilDilemmaChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid council's dilemma vote: " + choice);
        }

        int evidenceVotes = context.evidenceVotes();
        int briberyVotes = context.briberyVotes();
        if (ChoiceContext.CouncilDilemmaChoice.EVIDENCE.equals(choice)) {
            evidenceVotes++;
        } else {
            briberyVotes++;
        }

        if (context.offerAdditionalVote()) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    context.effectControllerId(), null, null,
                    new ChoiceContext.CouncilDilemmaAdditionalVoteChoice(
                            context.effectControllerId(), context.remainingPlayerIds(),
                            evidenceVotes, briberyVotes, context.sourceName()),
                    ChoiceContext.CouncilDilemmaAdditionalVoteChoice.OPTIONS,
                    context.sourceName() + " - vote an additional time?"));
            return;
        }

        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                evidenceVotes, briberyVotes, context.sourceName(), false);
    }

    public void completeAdditionalVote(GameData gameData, String choice,
                                       ChoiceContext.CouncilDilemmaAdditionalVoteChoice context) {
        if (!ChoiceContext.CouncilDilemmaAdditionalVoteChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid additional council's dilemma vote choice: " + choice);
        }

        List<UUID> remaining = new ArrayList<>(context.remainingPlayerIds());
        if (ChoiceContext.CouncilDilemmaAdditionalVoteChoice.VOTE_AGAIN.equals(choice)) {
            remaining.add(0, context.effectControllerId());
        }
        beginNextVote(gameData, remaining, context.effectControllerId(),
                context.evidenceVotes(), context.briberyVotes(), context.sourceName(), false);
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, int evidenceVotes, int briberyVotes,
                               String sourceName, boolean offerAdditionalVote) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (remaining.isEmpty()) {
            finishVote(gameData, evidenceVotes, briberyVotes);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.CouncilDilemmaChoice(
                        effectControllerId, remaining, evidenceVotes, briberyVotes, sourceName,
                        offerAdditionalVote),
                ChoiceContext.CouncilDilemmaChoice.OPTIONS,
                sourceName + " - vote for evidence or bribery."));
    }

    private void finishVote(GameData gameData, int evidenceVotes, int briberyVotes) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Council's dilemma resolution is not resumable");
        }

        List<CardEffect> results = new ArrayList<>(2);
        if (evidenceVotes > 0) {
            results.add(CreateTokenEffect.ofClueToken(evidenceVotes));
        }
        if (briberyVotes > 0) {
            results.add(CreateTokenEffect.ofTreasureToken(briberyVotes));
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, results);
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
