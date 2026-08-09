package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealAnyNumberOfCardsFromHandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealAnyNumberOfCardsFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealAnyNumberOfCardsFromHandEffect revealEffect =
                (RevealAnyNumberOfCardsFromHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<UUID> validCardIds = hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, revealEffect.filter(), entry.getCard().getId(), gameData, controllerId))
                .map(Card::getId)
                .toList();

        if (validCardIds.isEmpty()) {
            entry.setEventValue(0);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                        controllerId, validCardIds, entry.getCard().getName()));
    }
}
