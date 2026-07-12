package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ShuffleCardFromControllerGraveyardIntoLibraryEffect} by prompting the controller
 * to optionally shuffle up to one matching card from their own graveyard into their library. The
 * choice is resolution-time (via {@code GraveyardChoice} with destination
 * {@link GraveyardChoiceDestination#SHUFFLE_INTO_OWNERS_LIBRARY}); the controller may decline. Does
 * nothing if the controller has no matching cards in their graveyard.
 */
@Component
@RequiredArgsConstructor
public class ShuffleCardFromControllerGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleCardFromControllerGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ShuffleCardFromControllerGraveyardIntoLibraryEffect e =
                (ShuffleCardFromControllerGraveyardIntoLibraryEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), e.filter(), null)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, GraveyardChoiceDestination.SHUFFLE_INTO_OWNERS_LIBRARY,
                        "You may shuffle a card from your graveyard into your library.")
                .build());
    }
}
