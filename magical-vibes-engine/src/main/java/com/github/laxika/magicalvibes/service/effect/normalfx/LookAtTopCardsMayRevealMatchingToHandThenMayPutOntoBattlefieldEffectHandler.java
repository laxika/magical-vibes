package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect typed =
                (LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, typed.lookCount(), false);
        if (result == null) {
            return;
        }

        gameLogService.append(gameData, GameLog.text(result.playerName() + " looks at the top "
                + LibraryRevealSupport.pluralCards(result.topCards().size()) + " of their library."));

        List<Card> matchingCards = result.topCards().stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, typed.predicate(), entry.getCard().getId(), gameData, result.controllerId()))
                .toList();
        if (matchingCards.isEmpty()) {
            putOnBottomRandomly(gameData, result.controllerId(), result.topCards(), result.playerName());
            return;
        }

        String prompt = "You may reveal a matching creature card from among them and put it into your hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), matchingCards)
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(result.topCards()))
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.HAND)
                        .followUp(LibrarySearchFollowUp.forSelectedCardWithRandomRest(
                                new CardMaxManaValuePredicate(typed.battlefieldManaValueAtMost()),
                                new MayPutSelectedCardOntoBattlefieldEffect(
                                        typed.battlefieldManaValueAtMost())))
                        .build(),
                prompt,
                true));
    }

    private void putOnBottomRandomly(GameData gameData, UUID controllerId,
            List<Card> cards, String playerName) {
        List<Card> remaining = new ArrayList<>(cards);
        Collections.shuffle(remaining);
        gameData.playerDecks.get(controllerId).addAll(remaining);
        gameLogService.append(gameData, GameLog.text(
                playerName + " finds no matching card. The cards are put on the bottom of their library in a random order."));
    }
}
