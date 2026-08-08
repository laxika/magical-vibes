package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingThranTomeChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ThranTomeRevealTopThreeOpponentChoosesEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Thran Tome's reveal and opponent-choice portion. The cards stay in the library while
 * the choice is pending, so the shared library-search input handler removes only the chosen card.
 */
@Component
@RequiredArgsConstructor
public class ThranTomeRevealTopThreeOpponentChoosesEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ThranTomeRevealTopThreeOpponentChoosesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.pollPendingInteraction(PendingThranTomeChoice.class) != null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.getOrDefault(controllerId, List.of());
        int actual = Math.min(3, deck.size());
        if (actual == 0) {
            return;
        }

        if (actual == 1) {
            Card card = deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, controllerId, card);
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, actual));
        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        gameLogService.append(gameData, GameLog.text(
                controllerName + " reveals the top " + actual + " cards of their library with "
                        + entry.getCard().getName() + "."));
        cardRevealService.revealToAllPlayers(
                gameData, controllerId, GameEventFact.RevealZone.LIBRARY, topCards);

        gameData.queueInteraction(new PendingThranTomeChoice());
        String prompt = entry.getCard().getName() + " — " + opponentName
                + " chooses a card to put into " + controllerName + "'s graveyard.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(opponentId, topCards)
                        .reveals(true)
                        .canFailToFind(false)
                        .targetPlayerId(controllerId)
                        .shuffleAfterSelection(false)
                        .destination(LibrarySearchDestination.GRAVEYARD)
                        .prompt(prompt)
                        .build(),
                prompt,
                false));
    }
}
