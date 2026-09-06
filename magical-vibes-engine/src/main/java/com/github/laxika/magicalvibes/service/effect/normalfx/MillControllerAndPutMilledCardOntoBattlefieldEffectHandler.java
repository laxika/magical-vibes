package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Resolves a mill followed by a mandatory choice of one matching milled card to put onto the battlefield. */
@Component
@RequiredArgsConstructor
public class MillControllerAndPutMilledCardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndPutMilledCardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndPutMilledCardOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, controllerId, millEffect.count());

        Set<UUID> matchingMilledIds = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.filter(), entry.getCard().getId(), gameData, controllerId))
                .map(Card::getId)
                .collect(Collectors.toSet());
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Integer> validIndices = graveyard == null ? List.of() : IntStream.range(0, graveyard.size())
                .filter(index -> matchingMilledIds.contains(graveyard.get(index).getId()))
                .boxed()
                .toList();

        if (validIndices.isEmpty()) {
            return;
        }

        String filterLabel = CardPredicateUtils.describeFilter(millEffect.filter());
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, validIndices, GraveyardChoiceDestination.BATTLEFIELD,
                        "Choose a " + filterLabel + " milled this way to put onto the battlefield.")
                .mandatory(true)
                .build());
    }
}
