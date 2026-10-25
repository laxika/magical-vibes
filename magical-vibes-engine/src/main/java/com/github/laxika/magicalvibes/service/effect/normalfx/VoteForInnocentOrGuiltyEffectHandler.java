package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteForInnocentOrGuiltyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Trial of a Time Lord's innocent-or-guilty vote. */
@Component
@RequiredArgsConstructor
public class VoteForInnocentOrGuiltyEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingSupport votingSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VoteForInnocentOrGuiltyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, votingSupport.voterOrder(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.VoteForInnocentOrGuiltyChoice context) {
        if (!ChoiceContext.VoteForInnocentOrGuiltyChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid innocent-or-guilty vote: " + choice);
        }

        Map<String, Integer> votes = new HashMap<>(context.votes());
        votes.merge(choice, 1, Integer::sum);
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, Map<String, Integer> votes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            if (votes.getOrDefault(ChoiceContext.VoteForInnocentOrGuiltyChoice.GUILTY, 0)
                    > votes.getOrDefault(ChoiceContext.VoteForInnocentOrGuiltyChoice.INNOCENT, 0)) {
                putCardsExiledWithSourceOnOwnersLibraries(gameData, sourceName);
            }
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.VoteForInnocentOrGuiltyChoice(
                        effectControllerId, remaining, votes, sourceName),
                ChoiceContext.VoteForInnocentOrGuiltyChoice.OPTIONS,
                sourceName + " — vote for innocent or guilty."));
    }

    private void putCardsExiledWithSourceOnOwnersLibraries(GameData gameData, String sourceName) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        UUID sourcePermanentId = pendingEntry == null ? null : pendingEntry.getSourcePermanentId();
        if (sourcePermanentId == null && pendingEntry != null && pendingEntry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = pendingEntry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        UUID sourceId = sourcePermanentId;
        List<ExiledCardEntry> exiledCards = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .toList();
        Map<UUID, List<Card>> cardsByOwner = new LinkedHashMap<>();
        for (ExiledCardEntry exiled : exiledCards) {
            Card card = exiled.card();
            if (gameData.playerDecks.get(exiled.ownerId()) == null || !gameData.removeFromExile(card.getId())) {
                continue;
            }
            cardsByOwner.computeIfAbsent(exiled.ownerId(), ignored -> new ArrayList<>()).add(card);
        }
        cardsByOwner.forEach((ownerId, cards) -> {
            if (cards.size() == 1) {
                gameData.playerDecks.get(ownerId).addLast(cards.getFirst());
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(ownerId) + " puts ", cards.getFirst(),
                        " on the bottom of their library (" + sourceName + ")."));
                return;
            }
            // CR 401.4: the owner of two or more cards put on the same library position arranges them.
            PendingInteraction.LibraryReorder reorder = new PendingInteraction.LibraryReorder(
                    ownerId, List.copyOf(cards), true, ownerId,
                    sourceName + " — put these cards on the bottom of your library in any order.",
                    0, false);
            if (gameData.interaction.isAwaitingInput()) {
                gameData.pendingInteractions.addLast(reorder);
            } else {
                interactionHandlerRegistry.begin(gameData, reorder);
            }
        });
        gameData.exileReturnOnPermanentLeave.remove(sourceId);
    }
}
