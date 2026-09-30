package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromTargetHandOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileNonlandCardFromTargetHandOrGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileNonlandCardFromTargetHandOrGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileNonlandCardFromTargetHandOrGraveyardEffect exileEffect =
                (ExileNonlandCardFromTargetHandOrGraveyardEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }

        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        List<Card> matchingHandCards = matchingCards(hand, exileEffect.handFilter(), gameData, targetPlayerId);
        if (exileEffect.revealMatchingHand()) {
            cardRevealService.revealMatchingHandCardsToAllPlayers(gameData, targetPlayerId, matchingHandCards);
        } else {
            cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        }

        List<Card> candidates = new ArrayList<>();
        candidates.addAll(matchingHandCards);
        if (!exileEffect.handOnly()) {
            candidates.addAll(matchingCards(
                    gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of()),
                    exileEffect.graveyardFilter(), gameData, targetPlayerId));
        }
        if (candidates.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice(
                        entry.getControllerId(), targetPlayerId,
                        candidates.stream().map(Card::getId).toList(),
                        exileEffect.grantPlayPermission()));
    }

    private List<Card> matchingCards(List<Card> cards, CardPredicate filter,
                                     GameData gameData, UUID cardOwnerId) {
        return cards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, cardOwnerId))
                .toList();
    }
}
