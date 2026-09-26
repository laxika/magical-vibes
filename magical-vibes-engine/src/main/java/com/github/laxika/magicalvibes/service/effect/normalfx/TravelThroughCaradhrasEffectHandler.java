package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TravelThroughCaradhrasEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Travel Through Caradhras's vote and applies one result per vote. */
@Component
@RequiredArgsConstructor
public class TravelThroughCaradhrasEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TravelThroughCaradhrasEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), 0, 0, entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.TravelThroughCaradhrasChoice context) {
        if (!ChoiceContext.TravelThroughCaradhrasChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Travel Through Caradhras vote: " + choice);
        }

        int redhornPassVotes = context.redhornPassVotes();
        int minesOfMoriaVotes = context.minesOfMoriaVotes();
        if (ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS.equals(choice)) {
            redhornPassVotes++;
        } else {
            minesOfMoriaVotes++;
        }

        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                redhornPassVotes, minesOfMoriaVotes, context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, int redhornPassVotes,
                               int minesOfMoriaVotes, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            insertVoteResults(gameData, redhornPassVotes, minesOfMoriaVotes);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.TravelThroughCaradhrasChoice(
                        effectControllerId, remaining, redhornPassVotes, minesOfMoriaVotes, sourceName),
                ChoiceContext.TravelThroughCaradhrasChoice.OPTIONS,
                sourceName + " — vote for Redhorn Pass or Mines of Moria."));
    }

    private void insertVoteResults(GameData gameData, int redhornPassVotes, int minesOfMoriaVotes) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Travel Through Caradhras resolution is not resumable");
        }

        List<CardEffect> results = new ArrayList<>();
        for (int i = 0; i < redhornPassVotes; i++) {
            results.add(new SearchLibraryEffect(
                    CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
        }
        for (int i = 0; i < minesOfMoriaVotes; i++) {
            results.add(ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).build());
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
