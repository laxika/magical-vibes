package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SailIntoTheWestEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Sail into the West's return-or-embark vote. */
@Component
@RequiredArgsConstructor
public class SailIntoTheWestEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SailIntoTheWestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.SailIntoTheWestChoice context) {
        if (!ChoiceContext.SailIntoTheWestChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Sail into the West vote: " + choice);
        }

        Map<String, Integer> votes = new HashMap<>();
        votes.put(ChoiceContext.SailIntoTheWestChoice.RETURN, context.returnVotes());
        votes.put(ChoiceContext.SailIntoTheWestChoice.EMBARK, context.embarkVotes());
        votes.merge(choice, 1, Integer::sum);
        beginNextVote(gameData, context.remainingPlayerIds(), context.effectControllerId(), votes,
                context.sourceName());
    }

    private void beginNextVote(GameData gameData, List<UUID> remainingPlayerIds,
                               UUID effectControllerId, Map<String, Integer> votes,
                               String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        if (remaining.isEmpty()) {
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("Sail into the West resolution is not resumable");
            }

            CardEffect result = votes.getOrDefault(ChoiceContext.SailIntoTheWestChoice.RETURN, 0)
                    > votes.getOrDefault(ChoiceContext.SailIntoTheWestChoice.EMBARK, 0)
                    ? new EachPlayerReturnsCardsFromGraveyardToHandEffect(2, new CardTruePredicate())
                    : new EachPlayerMayDiscardHandThenDrawEffect(7);
            if (result instanceof EachPlayerReturnsCardsFromGraveyardToHandEffect) {
                pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                        List.of(result, new ExileSpellEffect()));
            } else {
                pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                        List.of(result));
            }
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.SailIntoTheWestChoice(
                        effectControllerId, remaining,
                        votes.getOrDefault(ChoiceContext.SailIntoTheWestChoice.RETURN, 0),
                        votes.getOrDefault(ChoiceContext.SailIntoTheWestChoice.EMBARK, 0),
                        sourceName),
                ChoiceContext.SailIntoTheWestChoice.OPTIONS,
                sourceName + " — vote for Return or Embark."));
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
