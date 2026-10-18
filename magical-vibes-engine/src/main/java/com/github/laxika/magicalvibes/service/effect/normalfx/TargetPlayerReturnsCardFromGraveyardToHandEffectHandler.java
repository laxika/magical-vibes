package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class TargetPlayerReturnsCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerReturnsCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerReturnsCardFromGraveyardToHandEffect returnEffect =
                (TargetPlayerReturnsCardFromGraveyardToHandEffect) effect;
        UUID targetPlayerId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetPlayerId == null) {
            return;
        }
        returnCardFromGraveyard(gameData, entry.getCard(), targetPlayerId, returnEffect.filter());
    }

    /** Makes the player return a card matching {@code filter} from their graveyard to their hand. */
    public void returnCardFromGraveyard(GameData gameData, Card sourceCard, UUID targetPlayerId,
                                        CardPredicate filter) {
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard == null) {
            return;
        }
        List<Card> matchingCards = graveyard.stream()
                .filter(card -> filter == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, filter, sourceCard.getId(), gameData, targetPlayerId))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }
        if (matchingCards.size() == 1) {
            Card card = matchingCards.getFirst();
            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            permanentRemovalService.addCardToHandFromGraveyard(gameData, targetPlayerId, targetPlayerId, card);
            return;
        }

        List<Integer> validIndices = IntStream.range(0, matchingCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        targetPlayerId, validIndices, GraveyardChoiceDestination.HAND,
                        sourceCard.getName() + " — choose a "
                                + CardPredicateUtils.describeFilter(filter)
                                + " from your graveyard to return to your hand.")
                .cardPool(new ArrayList<>(matchingCards))
                .mandatory(true)
                .build());
    }
}
