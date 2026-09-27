package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCreatureCardsToBattlefieldAndShuffleRestEffect;
import com.github.laxika.magicalvibes.model.effect.SelvalasStampedeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Selvala's Stampede's council's-dilemma vote. */
@Component
@RequiredArgsConstructor
public class SelvalasStampedeEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SelvalasStampedeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.SelvalasStampedeChoice context) {
        if (!ChoiceContext.SelvalasStampedeChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Selvala's Stampede vote: " + choice);
        }

        Map<String, Integer> votes = new HashMap<>(context.votes());
        votes.merge(choice, 1, Integer::sum);
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, Map<String, Integer> votes,
                               String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("Selvala's Stampede resolution is not resumable");
            }

            int wildVotes = votes.getOrDefault(ChoiceContext.SelvalasStampedeChoice.WILD, 0);
            int freeVotes = votes.getOrDefault(ChoiceContext.SelvalasStampedeChoice.FREE, 0);
            List<CardEffect> results = new ArrayList<>(2);
            if (wildVotes > 0) {
                results.add(new RevealCreatureCardsToBattlefieldAndShuffleRestEffect(wildVotes));
            }
            if (freeVotes > 0) {
                results.add(new PutUpToCardsFromHandOntoBattlefieldEffect(
                        new CardIsPermanentPredicate(), "permanent", freeVotes));
            }
            if (!results.isEmpty()) {
                pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, results);
            }
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.SelvalasStampedeChoice(
                        effectControllerId, remaining, votes, sourceName),
                ChoiceContext.SelvalasStampedeChoice.OPTIONS,
                sourceName + " — vote for wild or free."));
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
