package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves {@link VoteEffect}: each player in voting order chooses one of the options at a list prompt
 * ({@link ChoiceContext.VoteChoice}), an additional vote offered through {@link VotingSupport} (CR 701.38d). A vote
 * step that pauses at its end: after the last vote it records the VOTE step, finishes the vote ("whenever players
 * finish voting") and the resolution goes on with the steps that read it.
 *
 * <p>An open vote is logged as it is cast, so each player knows the votes before theirs (Plea for Power's ruling); a
 * secret vote is logged only once the last one is cast, all of them together (Trap the Trespassers' ruling). A player
 * who has left the game doesn't vote.</p>
 */
@Component
@RequiredArgsConstructor
public class VoteEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingSupport votingSupport;
    private final VotingFinishedSupport votingFinishedSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VoteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var vote = (VoteEffect) effect;
        entry.recordStep(StepKind.VOTE, StepOutcome.NONE);
        List<UUID> first = subjectResolutionService.players(gameData, entry, effect, vote.firstVoter());
        UUID firstVoterId = first.isEmpty() ? entry.getControllerId() : first.getFirst();
        List<UUID> voters = new ArrayList<>(votingSupport.voterOrder(gameData, firstVoterId));
        voters.removeIf(playerId -> !gameData.playerIds.contains(playerId));
        ChoiceContext.VoteChoice start = new ChoiceContext.VoteChoice(entry.getControllerId(), voters, List.of(),
                vote.options(), vote.secret(), FaceDownVisibility.sourceName(gameData, entry));
        next(gameData, entry, start);
    }

    /** The answer of {@code voterId}, the player the prompt asked: one more vote, then the next voter or the end. */
    public void completeVote(GameData gameData, UUID voterId, String choice, ChoiceContext.VoteChoice context) {
        if (!context.options().contains(choice)) {
            throw new IllegalArgumentException("Invalid vote for " + context.sourceName() + ": " + choice);
        }
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException(context.sourceName() + "'s vote is not resumable");
        }
        List<StepOutcome.Vote> votes = new ArrayList<>(context.votes());
        votes.add(new StepOutcome.Vote(voterId, choice));
        votingFinishedSupport.recordVote(gameData, context.effectControllerId(), voterId, choice);
        if (!context.secret()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(voterId) + " votes for "
                    + choice + " (" + context.sourceName() + ")."));
        }
        next(gameData, entry, new ChoiceContext.VoteChoice(context.effectControllerId(), context.remainingVoterIds(),
                votes, context.options(), context.secret(), context.sourceName()));
    }

    private void next(GameData gameData, StackEntry entry, ChoiceContext.VoteChoice context) {
        List<UUID> remaining = new ArrayList<>(context.remainingVoterIds());
        if (remaining.isEmpty()) {
            finish(gameData, entry, context);
            return;
        }
        UUID voterId = remaining.removeFirst();
        String options = context.options().stream().map(String::toLowerCase).collect(Collectors.joining(" or "));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                voterId, null, null, context.withRemainingVoterIds(remaining), context.options(),
                context.sourceName() + " — " + (context.secret() ? "secretly vote" : "vote") + " for " + options
                        + "."));
    }

    private void finish(GameData gameData, StackEntry entry, ChoiceContext.VoteChoice context) {
        if (context.secret() && !context.votes().isEmpty()) {
            String revealed = context.votes().stream()
                    .map(cast -> gameData.playerIdToName.get(cast.voterId()) + " votes for " + cast.choice())
                    .collect(Collectors.joining(", "));
            gameLogService.append(gameData, GameLog.text(
                    "The votes for " + context.sourceName() + " are revealed: " + revealed + "."));
        }
        entry.recordStep(StepKind.VOTE, StepOutcome.ofVotes(context.votes()));
        votingFinishedSupport.finishVoting(gameData, context.effectControllerId());
    }
}
