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
        beginNextVote(gameData, votingSupport.addAdditionalControllerVotes(
                        gameData, orderStartingWith(gameData, entry.getControllerId()), entry.getControllerId()),
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
        for (ExiledCardEntry exiled : exiledCards) {
            Card card = exiled.card();
            List<Card> library = gameData.playerDecks.get(exiled.ownerId());
            if (library == null || !gameData.removeFromExile(card.getId())) {
                continue;
            }
            library.addLast(card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(exiled.ownerId()) + " puts ", card,
                    " on the bottom of their library (" + sourceName + ")."));
        }
        gameData.exileReturnOnPermanentLeave.remove(sourceId);
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
