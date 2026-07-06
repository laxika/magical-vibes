package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsChooseNToHandRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsChooseNToHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsChooseNToHandRestToGraveyardEffect e = (LookAtTopCardsChooseNToHandRestToGraveyardEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();
        int count = topCards.size();
        int toHandCount = e.toHandCount();
        CardPredicate handChoicePredicate = e.handChoicePredicate();

        if (e.reveal()) {
            String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + ".");
        }

        if (handChoicePredicate == null) {
            resolveWithoutPredicate(gameData, entry, controllerId, topCards, playerName, count, toHandCount);
            return;
        }

        List<Card> eligibleCards = filterEligibleCards(topCards, handChoicePredicate, gameData, controllerId);

        if (eligibleCards.isEmpty()) {
            for (Card card : topCards) {
                gameData.playerGraveyards.get(controllerId).add(card);
            }
            String restNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + restNames + " into their graveyard.");
            log.info("Game {} - {} resolving {} — 0 eligible, {} to graveyard",
                    gameData.id, playerName, cardName, topCards.size());
            return;
        }

        if (eligibleCards.size() <= toHandCount) {
            for (Card card : eligibleCards) {
                gameData.addCardToHand(controllerId, card);
            }
            List<Card> remainingCards = new ArrayList<>(topCards);
            remainingCards.removeAll(eligibleCards);
            for (Card card : remainingCards) {
                gameData.playerGraveyards.get(controllerId).add(card);
            }

            String handNames = eligibleCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + handNames + " into their hand.");
            if (!remainingCards.isEmpty()) {
                String restNames = remainingCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " puts " + restNames + " into their graveyard.");
            }
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        String actionVerb = e.reveal() ? "Reveal" : "Look at";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, 0, null, toHandCount,
                actionVerb + " the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard."));

        if (!e.reveal()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library.");
        }
        log.info("Game {} - {} resolving {} with {} cards, {} eligible",
                gameData.id, playerName, cardName, count, eligibleCards.size());
    }

    private void resolveWithoutPredicate(GameData gameData, StackEntry entry, UUID controllerId,
            List<Card> topCards, String playerName, int count, int toHandCount) {
        if (count <= toHandCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            String logMsg = count == 1
                    ? playerName + " looks at the top card of their library and puts it into their hand."
                    : playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count)
                            + " of their library and puts them into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, 0, null, toHandCount,
                "Look at the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard."));

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library.");
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    }

    private List<Card> filterEligibleCards(List<Card> topCards, CardPredicate predicate,
            GameData gameData, UUID controllerId) {
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : topCards) {
            if (predicateEvaluationService.matchesCardPredicate(card, predicate, null, gameData, controllerId)) {
                eligibleCards.add(card);
            }
        }
        return eligibleCards;
    }
}
