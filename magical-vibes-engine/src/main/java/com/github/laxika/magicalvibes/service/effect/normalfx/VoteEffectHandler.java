package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves {@link VoteEffect}: each player in voting order casts a vote, an additional vote offered through
 * {@link VotingSupport} (CR 701.38d). A vote among words is a list prompt ({@link ChoiceContext.VoteChoice}); a vote for
 * permanents, cards or players is a one-of selection among the candidates found as the vote began
 * ({@link MultiPermanentChoiceContext.ObjectVoteChoice}), those that can't be chosen left out, with no prompt for a voter
 * with no candidate and none for a voter with one candidate and no additional vote, who votes for it (CR 608.2d). A
 * vote step that pauses at its end: after the last vote it records the VOTE step, finishes the vote ("whenever players
 * finish voting") and the resolution goes on with the steps that read it.
 *
 * <p>An open vote is logged as it is cast, so each player knows the votes before theirs (Plea for Power's and Council's
 * Judgment's rulings); a secret vote is logged only once the last one is cast, all of them together, once (Trap the
 * Trespassers' ruling). A player who has left the game doesn't vote.</p>
 */
@Component
@RequiredArgsConstructor
public class VoteEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
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
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        if (vote.candidates() instanceof VoteEffect.Words words) {
            next(gameData, entry, new ChoiceContext.VoteChoice(entry.getControllerId(), voters, List.of(),
                    words.options(), vote.secret(), sourceName));
            return;
        }
        boolean otherThanVoter = vote.candidates() instanceof VoteEffect.Players players && players.otherThanVoter();
        nextObjectVote(gameData, entry, new MultiPermanentChoiceContext.ObjectVoteChoice(entry.getControllerId(),
                voters, List.of(), kindPrefix(vote.candidates()), candidates(gameData, entry, vote), otherThanVoter,
                vote.upToOne(), vote.secret(), sourceName));
    }

    /** The answer of {@code voterId}, the player the prompt asked: one more vote, then the next voter or the end. */
    public void completeVote(GameData gameData, UUID voterId, String choice, ChoiceContext.VoteChoice context) {
        if (!context.options().contains(choice)) {
            throw new IllegalArgumentException("Invalid vote for " + context.sourceName() + ": " + choice);
        }
        StackEntry entry = resumableEntry(gameData, context.sourceName());
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

    /**
     * The answer of {@code voterId} to a vote for objects or players: the candidate chosen, or nothing for a voter who
     * may vote for up to one and cast no vote.
     */
    public void completeObjectVote(GameData gameData, UUID voterId, List<UUID> selectedIds,
                                   MultiPermanentChoiceContext.ObjectVoteChoice context) {
        StackEntry entry = resumableEntry(gameData, context.sourceName());
        if (selectedIds.isEmpty()) {
            if (!context.upToOne()) {
                throw new IllegalArgumentException("A vote for " + context.sourceName() + " must be cast");
            }
            nextObjectVote(gameData, entry, context);
            return;
        }
        UUID candidateId = selectedIds.getFirst();
        if (selectedIds.size() != 1 || !context.candidatesOf(voterId).contains(candidateId)) {
            throw new IllegalArgumentException("Invalid vote for " + context.sourceName() + ": " + selectedIds);
        }
        nextObjectVote(gameData, entry, cast(gameData, voterId, candidateId, context));
    }

    /** The key {@code VotingFinishedSupport} records for a vote for {@code candidateId}. */
    public static String voteKey(MultiPermanentChoiceContext.ObjectVoteChoice context, UUID candidateId) {
        return context.kindPrefix() + candidateId;
    }

    private StackEntry resumableEntry(GameData gameData, String sourceName) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException(sourceName + "'s vote is not resumable");
        }
        return entry;
    }

    private void next(GameData gameData, StackEntry entry, ChoiceContext.VoteChoice context) {
        List<UUID> remaining = new ArrayList<>(context.remainingVoterIds());
        if (remaining.isEmpty()) {
            finish(gameData, entry, context.effectControllerId(), context.votes(), context.secret(),
                    context.sourceName(), null);
            return;
        }
        UUID voterId = remaining.removeFirst();
        String options = context.options().stream().map(String::toLowerCase).collect(Collectors.joining(" or "));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                voterId, null, null, context.withRemainingVoterIds(remaining), context.options(),
                context.sourceName() + " — " + (context.secret() ? "secretly vote" : "vote") + " for " + options
                        + "."));
    }

    private void nextObjectVote(GameData gameData, StackEntry entry,
                                MultiPermanentChoiceContext.ObjectVoteChoice context) {
        MultiPermanentChoiceContext.ObjectVoteChoice current = context;
        List<UUID> remaining = new ArrayList<>(current.remainingVoterIds());
        while (!remaining.isEmpty()) {
            UUID voterId = remaining.removeFirst();
            current = current.withRemainingVoterIds(remaining);
            List<UUID> candidates = current.candidatesOf(voterId);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1 && !current.upToOne() && !votingSupport.mayVoteAgain(gameData, voterId)) {
                current = cast(gameData, voterId, candidates.getFirst(), current);
                continue;
            }
            String prompt = current.sourceName() + " — " + (current.secret() ? "secretly vote" : "vote") + " for "
                    + candidateNoun(current) + ".";
            switch (current.kindPrefix()) {
                case StepOutcome.Vote.PLAYER -> playerInputService.beginMultiPermanentOrPlayerChoice(
                        gameData, voterId, List.of(), candidates, 1, current, prompt);
                case StepOutcome.Vote.CARD -> playerInputService.beginMultiPermanentChoice(
                        gameData, voterId, List.of(), candidates, 1, current, prompt);
                default -> playerInputService.beginMultiPermanentChoice(
                        gameData, voterId, candidates, 1, current, prompt);
            }
            return;
        }
        finish(gameData, entry, current.effectControllerId(), current.votes(), current.secret(),
                current.sourceName(), current.kindPrefix());
    }

    /** {@code voterId}'s vote for {@code candidateId}, logged as it is cast when the vote is open. */
    private MultiPermanentChoiceContext.ObjectVoteChoice cast(GameData gameData, UUID voterId, UUID candidateId,
                                                             MultiPermanentChoiceContext.ObjectVoteChoice context) {
        String key = voteKey(context, candidateId);
        votingFinishedSupport.recordVote(gameData, context.effectControllerId(), voterId, key);
        if (!context.secret()) {
            gameLogService.append(gameData, voteLine(gameData, GameLog.builder(), voterId, candidateId,
                    context.kindPrefix()).text(" (" + context.sourceName() + ").").build());
        }
        return context.withVote(new StepOutcome.Vote(voterId, key, candidateId));
    }

    private void finish(GameData gameData, StackEntry entry, UUID effectControllerId, List<StepOutcome.Vote> votes,
                        boolean secret, String sourceName, String kindPrefix) {
        if (secret && !votes.isEmpty()) {
            if (kindPrefix == null) {
                String revealed = votes.stream()
                        .map(cast -> gameData.playerIdToName.get(cast.voterId()) + " votes for " + cast.choice())
                        .collect(Collectors.joining(", "));
                gameLogService.append(gameData, GameLog.text(
                        "The votes for " + sourceName + " are revealed: " + revealed + "."));
            } else {
                GameLog.Builder line = GameLog.builder().text("The votes for " + sourceName + " are revealed: ");
                for (int i = 0; i < votes.size(); i++) {
                    voteLine(gameData, line.text(i == 0 ? "" : ", "), votes.get(i).voterId(),
                            votes.get(i).candidateId(), kindPrefix);
                }
                gameLogService.append(gameData, line.text(".").build());
            }
        }
        entry.recordStep(StepKind.VOTE, StepOutcome.ofVotes(votes));
        votingFinishedSupport.finishVoting(gameData, effectControllerId);
    }

    /** Appends "[voter] votes for [candidate]" to {@code line}, a card candidate as a card segment. */
    private GameLog.Builder voteLine(GameData gameData, GameLog.Builder line, UUID voterId,
                                          UUID candidateId, String kindPrefix) {
        String voter = gameData.playerIdToName.get(voterId) + " votes for ";
        if (StepOutcome.Vote.PLAYER.equals(kindPrefix)) {
            return line.text(voter + gameData.playerIdToName.get(candidateId));
        }
        Card card = StepOutcome.Vote.CARD.equals(kindPrefix)
                ? gameQueryService.findCardInGraveyardById(gameData, candidateId)
                : cardOf(gameData, candidateId);
        if (card == null && StepOutcome.Vote.CARD.equals(kindPrefix) && gameData.findExiledCard(candidateId) != null) {
            card = gameData.findExiledCard(candidateId).card();
        }
        return card == null ? line.text(voter + "a card that is gone") : line.text(voter).card(card);
    }

    private Card cardOf(GameData gameData, UUID permanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        return permanent == null ? null : permanent.getCard();
    }

    private List<UUID> candidates(GameData gameData, StackEntry entry, VoteEffect vote) {
        return switch (vote.candidates()) {
            case VoteEffect.Permanents permanents -> subjectResolutionService
                    .permanents(gameData, entry, vote, permanents.permanents()).stream()
                    .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                    .map(Permanent::getId)
                    .toList();
            case VoteEffect.Cards cards -> subjectResolutionService.cards(gameData, entry, vote, cards.cards()).stream()
                    .map(Card::getId)
                    .toList();
            case VoteEffect.Players players -> subjectResolutionService
                    .players(gameData, entry, vote, players.players()).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case VoteEffect.Words ignored -> List.of();
        };
    }

    private static String kindPrefix(VoteEffect.Candidates candidates) {
        return switch (candidates) {
            case VoteEffect.Permanents ignored -> StepOutcome.Vote.PERMANENT;
            case VoteEffect.Cards ignored -> StepOutcome.Vote.CARD;
            case VoteEffect.Players ignored -> StepOutcome.Vote.PLAYER;
            case VoteEffect.Words ignored -> throw new IllegalArgumentException("A word vote has no candidates");
        };
    }

    private static String candidateNoun(MultiPermanentChoiceContext.ObjectVoteChoice context) {
        String noun = switch (context.kindPrefix()) {
            case StepOutcome.Vote.PLAYER -> context.otherThanVoter() ? "other player" : "player";
            case StepOutcome.Vote.CARD -> "card";
            default -> "permanent";
        };
        return (context.upToOne() ? "up to one " : context.otherThanVoter() ? "an" : "a ") + noun;
    }
}
