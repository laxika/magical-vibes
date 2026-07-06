package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect e = (LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int xValue = entry.getXValue();
        boolean toBottomRandom = e.remainingToBottomRandom();

        int count = Math.min(xValue, deck.size());
        if (count <= 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName
                    + (deck.isEmpty() ? "'s library is empty."
                    : toBottomRandom ? " reveals 0 cards (0 damage dealt)." : " looks at 0 cards (X is 0).");
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> revealedCards = LibraryRevealSupport.takeTopCards(deck, count);

        String logMsg = toBottomRandom
                ? playerName + " reveals the top " + LibraryRevealSupport.pluralCards(count) + " of their library."
                : playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        // Filter eligible cards using predicates
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (e.alwaysEligiblePredicate() != null
                    && predicateEvaluationService.matchesCardPredicate(card, e.alwaysEligiblePredicate(), null, gameData, controllerId)) {
                eligibleCards.add(card);
            } else if (e.mvCappedEligiblePredicate() != null
                    && card.getManaValue() <= xValue
                    && predicateEvaluationService.matchesCardPredicate(card, e.mvCappedEligiblePredicate(), null)) {
                eligibleCards.add(card);
            }
        }

        if (eligibleCards.isEmpty()) {
            if (toBottomRandom) {
                // No eligible cards — put all on bottom in random order
                Collections.shuffle(revealedCards);
                deck.addAll(revealedCards);
                String noEligibleLog = playerName + " finds no eligible cards. All cards are put on the bottom of their library in a random order.";
                gameBroadcastService.logAndBroadcast(gameData, noEligibleLog);
            } else {
                // No eligible cards — put all into graveyard
                for (Card card : revealedCards) {
                    gameData.playerGraveyards.get(controllerId).add(card);
                }
                String noEligibleLog = playerName + " finds no eligible cards. All cards are put into their graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, noEligibleLog);
            }
            return;
        }

        // Set up player choice for selecting cards to put onto battlefield
        String prompt = toBottomRandom
                ? "Choose any number of eligible cards to put onto the battlefield. The rest go to the bottom of your library in a random order."
                : "Choose any number of eligible cards to put onto the battlefield. The rest go to your graveyard.";
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, cardIds, !toBottomRandom, false, false, toBottomRandom, 0, null,
                eligibleCards.size(), prompt));

        log.info("Game {} - {} resolving {} with X={}, {} revealed, {} eligible",
                gameData.id, playerName, entry.getCard().getName(), xValue, count, eligibleCards.size());
    
    }
}
