package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GaladrielElvenQueenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnRingBearerEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.VotingFinishedSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Galadriel, Elven-Queen's dominion-or-guidance vote. */
@Component
@RequiredArgsConstructor
public class GaladrielElvenQueenEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final VotingFinishedSupport votingFinishedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GaladrielElvenQueenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.GaladrielElvenQueenChoice context) {
        if (!ChoiceContext.GaladrielElvenQueenChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Galadriel, Elven-Queen vote: " + choice);
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
            votingFinishedSupport.finishVoting(gameData, effectControllerId);
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("Galadriel, Elven-Queen resolution is not resumable");
            }

            CardEffect result = votes.getOrDefault(ChoiceContext.GaladrielElvenQueenChoice.DOMINION, 0)
                    > votes.getOrDefault(ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE, 0)
                    ? SequenceEffect.of(
                            new RingTemptsYouEffect(),
                            new PutCounterOnRingBearerEffect(CounterType.PLUS_ONE_PLUS_ONE))
                    : new DrawCardEffect(1);
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(result));
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.GaladrielElvenQueenChoice(
                        effectControllerId, remaining, votes, sourceName),
                ChoiceContext.GaladrielElvenQueenChoice.OPTIONS,
                sourceName + " — vote for dominion or guidance."));
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
