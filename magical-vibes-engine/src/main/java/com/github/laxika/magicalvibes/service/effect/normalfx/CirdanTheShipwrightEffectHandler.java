package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CirdanTheShipwrightEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Círdan's secret council votes and their results. */
@Component
@RequiredArgsConstructor
public class CirdanTheShipwrightEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CirdanTheShipwrightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new LinkedHashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, List<UUID> selectedIds,
                             MultiPermanentChoiceContext.CirdanVoteChoice context) {
        Map<UUID, Integer> votes = new LinkedHashMap<>(context.votes());
        votes.merge(selectedIds.getFirst(), 1, Integer::sum);
        beginNextVote(gameData, context.remainingVoterIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    public void completeHandChoice(GameData gameData, List<UUID> selectedIds,
                                   MultiPermanentChoiceContext.CirdanHandChoice context) {
        List<UUID> chosenCardIds = new ArrayList<>(context.chosenCardIds());
        if (!selectedIds.isEmpty()) {
            chosenCardIds.add(selectedIds.getFirst());
        }
        beginNextHandChoice(gameData, context.remainingPlayerIds(), chosenCardIds, context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingVoterIds,
                               UUID effectControllerId, Map<UUID, Integer> votes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingVoterIds);
        if (remaining.isEmpty()) {
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            resolveVotes(gameData, effectControllerId, votes, sourceName);
            return;
        }

        UUID voterId = remaining.removeFirst();
        playerInputService.beginMultiPermanentOrPlayerChoice(
                gameData, voterId, List.of(), gameData.orderedPlayerIds, 1,
                new MultiPermanentChoiceContext.CirdanVoteChoice(
                        effectControllerId, remaining, votes, sourceName),
                sourceName + " — secretly vote for a player.");
    }

    private void resolveVotes(GameData gameData, UUID effectControllerId,
                               Map<UUID, Integer> votes, String sourceName) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int voteCount = votes.getOrDefault(playerId, 0);
            if (voteCount > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, voteCount);
            }
        }

        List<UUID> noVotePlayers = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !votes.containsKey(playerId))
                .toList();
        beginNextHandChoice(gameData, noVotePlayers, List.of(), sourceName);
    }

    private void beginNextHandChoice(GameData gameData, List<UUID> remainingPlayerIds,
                                     List<UUID> chosenCardIds, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.removeFirst();
            List<UUID> validCardIds = permanentCardIdsInHand(gameData, playerId);
            if (validCardIds.isEmpty()) {
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, List.of(), validCardIds, 1,
                    new MultiPermanentChoiceContext.CirdanHandChoice(
                            playerId, remaining, chosenCardIds, sourceName),
                    sourceName + " — you may put a permanent card from your hand onto the battlefield.");
            return;
        }

        putChosenCardsOntoBattlefield(gameData, chosenCardIds);
    }

    private List<UUID> permanentCardIdsInHand(GameData gameData, UUID playerId) {
        return gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardIsPermanentPredicate(), card.getId(), gameData, playerId))
                .map(Card::getId)
                .toList();
    }

    private void putChosenCardsOntoBattlefield(GameData gameData, List<UUID> chosenCardIds) {
        if (chosenCardIds.isEmpty()) {
            return;
        }

        List<BattlefieldEntryCard> chosenCards = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                Card card = gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                        .filter(candidate -> candidate.getId().equals(cardId))
                        .findFirst()
                        .orElse(null);
                if (card != null) {
                    chosenCards.add(new BattlefieldEntryCard(playerId, playerId, card, Zone.HAND, null));
                    break;
                }
            }
        }
        battlefieldEntryBatchSupport.begin(gameData, chosenCards);
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
