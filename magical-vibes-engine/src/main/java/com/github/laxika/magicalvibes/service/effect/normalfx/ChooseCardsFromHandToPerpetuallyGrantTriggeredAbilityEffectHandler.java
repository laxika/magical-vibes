package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Begins a bounded multi-card choice for perpetual triggered-ability grants. */
@Component
@RequiredArgsConstructor
public class ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = (ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Card> matchingCards = hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, choice.cardFilter(), null, gameData, entry.getControllerId()))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualTriggeredAbilityCardsChoice(
                entry.getControllerId(), matchingCards.stream().map(Card::getId).toList(),
                "Choose up to " + choice.maxCount() + " matching cards in your hand.",
                choice.maxCount(), choice.slot(), choice.grantedEffects(), choice.cardFilter()));
    }
}
