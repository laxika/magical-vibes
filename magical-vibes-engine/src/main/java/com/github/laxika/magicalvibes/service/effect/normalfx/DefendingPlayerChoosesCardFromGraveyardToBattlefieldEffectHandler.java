package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID defendingPlayerId = defendingPlayerId(gameData, entry.getAttackedTargetId());
        List<Card> matchingCards = matchingCards(gameData, controllerId, choiceEffect.filter(),
                entry.getCard().getId());

        UUID chosenCardId = gameData.graveyardTargetOperation
                .defendingPlayerChoosesCardFromGraveyardChosenCardId;
        gameData.graveyardTargetOperation.defendingPlayerChoosesCardFromGraveyardChosenCardId = null;
        if (chosenCardId != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            matchingCards.stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .ifPresent(card -> returnCard(gameData, controllerId, card));
            return;
        }

        if (defendingPlayerId == null || matchingCards.isEmpty()) {
            return;
        }
        if (matchingCards.size() == 1) {
            returnCard(gameData, controllerId, matchingCards.getFirst());
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeDefendingPlayerChoosesCardFromGraveyardResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(defendingPlayerId,
                        IntStream.range(0, matchingCards.size()).boxed().toList(),
                        GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName() + " — choose a "
                                + CardPredicateUtils.describeFilter(choiceEffect.filter())
                                + " from its controller's graveyard to return to the battlefield.")
                .cardPool(new ArrayList<>(matchingCards))
                .mandatory(true)
                .build());
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    private List<Card> matchingCards(GameData gameData, UUID controllerId,
                                     CardPredicate filter, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return List.of();
        }
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .toList();
    }

    private void returnCard(GameData gameData, UUID controllerId, Card card) {
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        graveyardReturnSupport.putCardOntoBattlefield(
                gameData, controllerId, card, null, CardSubtype.VAMPIRE, false, false,
                CounterType.PLUS_ONE_PLUS_ONE);
    }
}
