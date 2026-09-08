package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayExileOneAndPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtTopCardsMayExileOneAndPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayExileOneAndPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LookAtTopCardsMayExileOneAndPlayThisTurnEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) return;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, count, true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> looked = result.topCards();
        List<Card> eligible = e.filter() == null
                ? looked
                : looked.stream()
                        .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                card, e.filter(), entry.getCard().getId(), gameData, controllerId))
                        .toList();
        if (eligible.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, looked);
            return;
        }
        String prompt = "You may exile one of these cards and play it this turn. "
                + "Put the rest on the bottom of your library in a random order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(looked))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .withoutPayingManaCost(e.withoutPayingManaCost())
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM)
                        .build(),
                prompt,
                true));
    }
}
