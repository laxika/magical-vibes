package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardFromHandOrGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putEffect = (PutCardFromHandOrGraveyardOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<UUID> validCardIds = new ArrayList<>();
        addMatchingCards(gameData.playerHands.getOrDefault(controllerId, List.of()), validCardIds,
                putEffect.predicate(), sourceCardId, gameData, controllerId);
        addMatchingCards(gameData.playerGraveyards.getOrDefault(controllerId, List.of()), validCardIds,
                putEffect.predicate(), sourceCardId, gameData, controllerId);

        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutCardFromHandOrGraveyardOntoBattlefieldChoice(
                        controllerId, validCardIds, putEffect.predicate(), putEffect.label(), sourceCardId,
                        putEffect.enterWithCounter()));
    }

    private void addMatchingCards(List<Card> cards, List<UUID> validCardIds, CardPredicate predicate,
                                  UUID sourceCardId, GameData gameData, UUID controllerId) {
        for (Card card : cards) {
            if (predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId, gameData, controllerId)) {
                validCardIds.add(card.getId());
            }
        }
    }
}
