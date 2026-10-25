package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CouncilDilemmaEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Tivit's council's dilemma vote. Tivit's additional vote is its static ability, offered by
 * {@link VotingSupport} like in any other vote.
 */
@Component
@RequiredArgsConstructor
public class CouncilDilemmaEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingFinishedSupport votingFinishedSupport;
    private final VotingSupport votingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CouncilDilemmaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, votingSupport.voterOrder(gameData, entry.getControllerId()),
                entry.getControllerId(), 0, 0, entry.getCard().getName());
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
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                evidenceVotes, briberyVotes, context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, int evidenceVotes, int briberyVotes,
                               String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (remaining.isEmpty()) {
            finishVote(gameData, effectControllerId, evidenceVotes, briberyVotes);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.CouncilDilemmaChoice(
                        effectControllerId, remaining, evidenceVotes, briberyVotes, sourceName),
                ChoiceContext.CouncilDilemmaChoice.OPTIONS,
                sourceName + " - vote for evidence or bribery."));
    }

    private void finishVote(GameData gameData, UUID effectControllerId, int evidenceVotes, int briberyVotes) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Council's dilemma resolution is not resumable");
        }

        // "For each evidence vote, investigate" performs one investigate per vote, so each token enters
        // as its own event rather than as one batch of simultaneously entering tokens.
        List<CardEffect> results = new ArrayList<>(evidenceVotes + briberyVotes);
        for (int vote = 0; vote < evidenceVotes; vote++) {
            results.add(CreateTokenEffect.ofClueToken(1));
        }
        for (int vote = 0; vote < briberyVotes; vote++) {
            results.add(CreateTokenEffect.ofTreasureToken(1));
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, results);
        votingFinishedSupport.finishVoting(gameData, effectControllerId);
    }
}
