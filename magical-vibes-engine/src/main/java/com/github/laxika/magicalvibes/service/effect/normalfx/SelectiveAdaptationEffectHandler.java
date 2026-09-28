package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SelectiveAdaptationEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Selective Adaptation's one-card-per-keyword selection over a revealed pile. */
@Component
@RequiredArgsConstructor
public class SelectiveAdaptationEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SelectiveAdaptationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SelectiveAdaptationEffect adaptation = (SelectiveAdaptationEffect) effect;
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, adaptation.count(), false);
        if (result == null) {
            return;
        }

        List<Card> topCards = result.topCards();
        GameLog.Builder revealLog = GameLog.builder().text(result.playerName() + " reveals ");
        appendCards(revealLog, topCards);
        revealLog.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, revealLog.build());

        List<KeywordPick> picks = adaptation.keywords().stream()
                .map(keyword -> new KeywordPick(new CardKeywordPredicate(keyword),
                        "a card with " + keyword.name().toLowerCase().replace('_', ' ')))
                .toList();
        if (!beginFirstAvailablePick(gameData, entry, topCards, picks)) {
            putIntoGraveyard(gameData, entry.getControllerId(), topCards);
        }
    }

    private boolean beginFirstAvailablePick(GameData gameData, StackEntry entry,
                                            List<Card> topCards, List<KeywordPick> picks) {
        for (int i = 0; i < picks.size(); i++) {
            KeywordPick pick = picks.get(i);
            List<Card> eligible = topCards.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, pick.predicate(), entry.getCard().getId(), gameData, entry.getControllerId()))
                    .toList();
            if (eligible.isEmpty()) {
                continue;
            }

            List<LibrarySearchFollowUp.SecondBoundedPick.PredicatePick> remaining =
                    picks.subList(i + 1, picks.size()).stream()
                            .map(next -> new LibrarySearchFollowUp.SecondBoundedPick.PredicatePick(
                                    next.predicate(), next.prompt()))
                            .toList();
            LibrarySearchFollowUp followUp = remaining.isEmpty()
                    ? LibrarySearchFollowUp.NONE
                    : LibrarySearchFollowUp.forBoundedPick(
                            LibrarySearchFollowUp.SecondBoundedPick.predicate(
                                    remaining.getFirst().predicate(), remaining.getFirst().prompt(),
                                    false, LibrarySearchDestination.HOLD_OUT,
                                    remaining.subList(1, remaining.size())));
            String prompt = "Choose " + pick.prompt() + " from among them.";
            LibrarySearchParams params = LibrarySearchParams.builder(
                            entry.getControllerId(), new ArrayList<>(eligible))
                    .reveals(true)
                    .canFailToFind(false)
                    .destination(LibrarySearchDestination.HOLD_OUT)
                    .sourceCards(new ArrayList<>(topCards))
                    .accumulatedCards(List.of())
                    .restToGraveyard(true)
                    .shuffleAfterSelection(false)
                    .followUp(followUp)
                    .prompt(prompt)
                    .build();
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.LibrarySearch(params, prompt, false));
            return true;
        }
        return false;
    }

    private void putIntoGraveyard(GameData gameData, java.util.UUID controllerId, List<Card> cards) {
        for (Card card : cards) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
        }
    }

    private static void appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }

    private record KeywordPick(CardKeywordPredicate predicate, String prompt) {
    }
}
