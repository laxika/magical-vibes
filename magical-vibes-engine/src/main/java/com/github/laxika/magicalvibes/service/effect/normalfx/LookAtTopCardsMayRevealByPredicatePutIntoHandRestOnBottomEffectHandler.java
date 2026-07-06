package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect e = (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        List<Card> matchingCards = topCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, e.predicate(), null))
                .toList();

        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        String description = CardPredicateUtils.describeFilter(e.predicate());

        if (e.anyNumber()) {
            List<UUID> cardIds = matchingCards.stream().map(Card::getId).toList();
            int max = Math.min(e.maxReveal(), matchingCards.size());
            String revealPrompt = e.maxReveal() >= Integer.MAX_VALUE
                    ? "You may reveal any number of " + description + "s and put them into your hand."
                    : "You may reveal up to " + max + " " + description + "s and put them into your hand.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    controllerId, topCards, cardIds, false, true, true, false, 0, null,
                    max, revealPrompt));
            return;
        }

        String prompt = "You may reveal a " + description + " from among them and put it into your hand.";

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                .reveals(true)
                .canFailToFind(true)
                .sourceCards(topCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .build(),
                prompt,
                true));
    
    }
}
