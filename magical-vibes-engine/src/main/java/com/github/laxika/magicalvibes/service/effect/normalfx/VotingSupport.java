package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.VoteChoiceContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared voting loop support for every vote handler: the voting order (CR 701.38a) and "While voting, you
 * may vote an additional time" (Tivit, The Valeyard). After a voter answers a vote prompt, the answer is
 * held while that voter is asked whether to vote again; voting again replays the held answer with the voter
 * queued first, so the extra vote happens at the same time as their normal vote (CR 701.38d).
 */
@Component
@RequiredArgsConstructor
public class VotingSupport {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Starts a vote: every player in turn order, starting with {@code firstVoterId}. Additional votes taken
     * in an earlier vote are forgotten.
     */
    public List<UUID> voterOrder(GameData gameData, UUID firstVoterId) {
        gameData.additionalVotesTaken.clear();
        gameData.additionalVoteAccepted = null;
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int firstIndex = orderedPlayerIds.indexOf(firstVoterId);
        if (firstIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(firstIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, firstIndex));
        return rotated;
    }

    /** Whether the voter controls a "you may vote an additional time" ability they haven't used in this vote. */
    public boolean mayVoteAgain(GameData gameData, UUID voterId) {
        return gameQueryService.countAdditionalVotes(gameData, voterId)
                > gameData.additionalVotesTaken.getOrDefault(voterId, 0);
    }

    /**
     * Called with a vote answer before it is counted. If the voter may vote an additional time, the answer
     * is held and the voter is asked whether to vote again; returns {@code true} when that question was
     * asked, in which case the caller must not count the answer yet.
     */
    public boolean offerAdditionalVote(GameData gameData, UUID voterId, PendingInteraction answeredVote,
                                       String answer, List<UUID> selectedIds) {
        VoteChoiceContext vote = voteContext(answeredVote);
        if (vote == null || !mayVoteAgain(gameData, voterId)) {
            return false;
        }
        gameData.interaction.clearAwaitingInput();
        beginOffer(gameData, new ChoiceContext.AdditionalVoteChoice(
                voterId, answeredVote, answer, selectedIds, vote.sourceName()));
        return true;
    }

    /**
     * Asks the voter whether to vote an additional time for a vote that keeps its own progress and reruns
     * its effect to read the answer from {@link GameData#additionalVoteAccepted}.
     */
    public void offerAdditionalVoteToRerunningEffect(GameData gameData, UUID voterId, String sourceName) {
        gameData.rerunCurrentEffectAfterInteraction = true;
        beginOffer(gameData, new ChoiceContext.AdditionalVoteChoice(voterId, null, null, List.of(), sourceName));
    }

    /**
     * Records the voter's answer to an additional-vote offer and returns the held vote prompt to answer
     * again: unchanged when they continue, with the voter queued to vote next when they vote again.
     * Returns {@code null} for a rerunning vote, whose effect reads the answer itself.
     */
    public PendingInteraction recordAdditionalVoteDecision(GameData gameData,
                                                           ChoiceContext.AdditionalVoteChoice offer,
                                                           boolean voteAgain) {
        UUID voterId = offer.voterId();
        if (voteAgain) {
            gameData.additionalVotesTaken.merge(voterId, 1, Integer::sum);
        } else {
            gameData.additionalVotesTaken.put(voterId, gameQueryService.countAdditionalVotes(gameData, voterId));
        }
        if (offer.answeredVote() == null) {
            gameData.additionalVoteAccepted = voteAgain;
            return null;
        }
        if (!voteAgain) {
            return offer.answeredVote();
        }
        VoteChoiceContext vote = voteContext(offer.answeredVote());
        List<UUID> remaining = new ArrayList<>(vote.remainingVoterIds());
        remaining.addFirst(voterId);
        VoteChoiceContext queued = vote.withRemainingVoterIds(remaining);
        return switch (offer.answeredVote()) {
            case PendingInteraction.ColorChoice c -> new PendingInteraction.ColorChoice(
                    c.playerId(), c.permanentId(), c.etbTargetId(), (ChoiceContext) queued, c.options(),
                    c.prompt(), c.disabledOptions());
            case PendingInteraction.MultiPermanentChoice m -> new PendingInteraction.MultiPermanentChoice(
                    m.playerId(), m.validIds(), m.validPlayerIds(), m.validCardIds(), m.maxCount(),
                    (MultiPermanentChoiceContext) queued, m.prompt());
            default -> throw new IllegalStateException("Not a vote prompt: " + offer.answeredVote());
        };
    }

    private void beginOffer(GameData gameData, ChoiceContext.AdditionalVoteChoice offer) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                offer.voterId(), null, null, offer,
                ChoiceContext.AdditionalVoteChoice.OPTIONS,
                offer.sourceName() + " - vote an additional time?"));
    }

    private static VoteChoiceContext voteContext(PendingInteraction prompt) {
        return switch (prompt) {
            case PendingInteraction.ColorChoice c when c.context() instanceof VoteChoiceContext vote -> vote;
            case PendingInteraction.MultiPermanentChoice m when m.context() instanceof VoteChoiceContext vote -> vote;
            default -> null;
        };
    }
}
