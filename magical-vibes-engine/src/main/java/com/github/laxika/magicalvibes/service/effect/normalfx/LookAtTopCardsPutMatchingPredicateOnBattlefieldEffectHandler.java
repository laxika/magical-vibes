package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPredicateOnBattlefieldEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsPutMatchingPredicateOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsPutMatchingPredicateOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsPutMatchingPredicateOnBattlefieldEffect e = (LookAtTopCardsPutMatchingPredicateOnBattlefieldEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        List<Card> matchingCards = topCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, e.predicate(), sourceCardId))
                .toList();

        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                        .canFailToFind(true)
                        .sourceCards(topCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt("You may put one of these cards onto the battlefield.")
                        .destination(LibrarySearchDestination.BATTLEFIELD)
                        .build(),
                "You may put one of these cards onto the battlefield.",
                true));
    }
}
