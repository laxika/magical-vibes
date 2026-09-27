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
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the two sequential optional permanent picks over one revealed library pile. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect e =
                (LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect) effect;
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), false);
        if (result == null) {
            return;
        }

        List<Card> topCards = result.topCards();
        GameLog.Builder revealLog = GameLog.builder().text(result.playerName() + " reveals ");
        appendCards(revealLog, topCards);
        revealLog.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, revealLog.build());

        CardIsPermanentPredicate permanentPredicate = new CardIsPermanentPredicate();
        List<Card> eligibleCards = topCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, permanentPredicate, entry.getCard().getId(), gameData, entry.getControllerId()))
                .toList();
        if (eligibleCards.isEmpty()) {
            putRemainingIntoGraveyard(gameData, entry.getControllerId(), topCards);
            return;
        }

        LibrarySearchFollowUp secondPick = LibrarySearchFollowUp.forBoundedPick(
                new LibrarySearchFollowUp.SecondBoundedPick(
                        null, true, null, List.of(), false, List.of(),
                        LibrarySearchDestination.HAND, permanentPredicate, "a permanent card"));
        LibrarySearchParams params = LibrarySearchParams.builder(entry.getControllerId(),
                        new ArrayList<>(eligibleCards))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .sourceCards(new ArrayList<>(topCards))
                .restToGraveyard(true)
                .shuffleAfterSelection(false)
                .enterWithCounters(e.battlefieldEntryReplacement())
                .followUp(secondPick)
                .prompt("You may reveal a permanent card from among them and put it onto the battlefield.")
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params,
                params.prompt(), true));
    }

    private void putRemainingIntoGraveyard(GameData gameData, java.util.UUID controllerId,
                                           List<Card> cards) {
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
}
