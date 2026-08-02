package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect}: reveal the top X
 * cards, offer the eligible ones (mana value &le; X) as an optional capped battlefield pick, and
 * shuffle whatever is left back into the library.
 *
 * <p>The pick rides the shared {@code LibraryRevealChoice} flow; leaving {@code remainingToGraveyard},
 * {@code randomRemainingToBottom} and {@code selectedToHand} all false selects the shuffle-the-rest
 * branch in {@code LibraryChoiceHandlerService}. When nothing is eligible the reveal still happens
 * and the cards are shuffled back without a prompt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect e =
                (RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int xValue = entry.getXValue();

        int count = Math.min(xValue, deck.size());
        if (count <= 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    ": " + playerName + " reveals no cards."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals the top " + count + (count == 1 ? " card" : " cards") + " of their library."));

        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (card.getManaValue() <= xValue
                    && predicateEvaluationService.matchesCardPredicate(card, e.eligiblePredicate(), null, gameData, controllerId)) {
                eligibleCards.add(card);
            }
        }

        if (eligibleCards.isEmpty()) {
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " finds no eligible cards. The revealed cards are shuffled into their library."));
            return;
        }

        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        int maxCount = Math.min(e.maxSelections(), eligibleCards.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, cardIds, false, false, false, false, false, 0, null,
                maxCount,
                "You may put up to " + maxCount + " revealed nonland permanent card"
                        + (maxCount == 1 ? "" : "s") + " with mana value " + xValue
                        + " or less onto the battlefield. The rest are shuffled into your library."));

        log.info("Game {} - {} resolving {} with X={}, {} revealed, {} eligible",
                gameData.id, playerName, entry.getCard().getName(), xValue, count, eligibleCards.size());
    }
}
