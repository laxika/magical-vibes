package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TyrantsChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Tyrant's Choice votes and applies the winning result. */
@Component
@RequiredArgsConstructor
public class TyrantsChoiceEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TyrantsChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextVote(gameData, orderStartingWith(gameData, entry.getControllerId()),
                entry.getControllerId(), new HashMap<>(), entry.getCard().getName());
    }

    public void completeVote(GameData gameData, String choice,
                             ChoiceContext.TyrantsChoiceChoice context) {
        if (!ChoiceContext.TyrantsChoiceChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Tyrant's Choice vote: " + choice);
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
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("Tyrant's Choice resolution is not resumable");
            }

            CardEffect result = votes.getOrDefault(ChoiceContext.TyrantsChoiceChoice.DEATH, 0)
                    > votes.getOrDefault(ChoiceContext.TyrantsChoiceChoice.TORTURE, 0)
                    ? new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                            SacrificeRecipient.EACH_OPPONENT).withSimultaneousChoices()
                    : new LoseLifeEffect(4, LoseLifeRecipient.EACH_OPPONENT);
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(result));
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.TyrantsChoiceChoice(
                        effectControllerId, remaining, votes, sourceName),
                ChoiceContext.TyrantsChoiceChoice.OPTIONS,
                sourceName + " — vote for death or torture."));
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
