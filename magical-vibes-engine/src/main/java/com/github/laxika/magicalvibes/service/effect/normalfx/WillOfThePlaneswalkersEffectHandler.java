package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Path of the Ghosthunter's Will of the Planeswalkers vote. */
@Component
@RequiredArgsConstructor
public class WillOfThePlaneswalkersEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WillOfThePlaneswalkersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.WillOfThePlaneswalkersChoice context) {
        if (!ChoiceContext.WillOfThePlaneswalkersChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Will of the Planeswalkers vote: " + choice);
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
                throw new IllegalStateException("Will of the Planeswalkers resolution is not resumable");
            }

            CardEffect result = votes.getOrDefault(ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK, 0)
                    > votes.getOrDefault(ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS, 0)
                    ? new PlaneswalkEffect()
                    : new ChaosEnsuesEffect();
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(result));
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.WillOfThePlaneswalkersChoice(
                        effectControllerId, remaining, votes, sourceName),
                ChoiceContext.WillOfThePlaneswalkersChoice.OPTIONS,
                sourceName + " — vote for planeswalk or chaos."));
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
