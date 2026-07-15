package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CardViewFactory cardViewFactory;
    private final SessionManager sessionManager;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardOfTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((LookAtTopCardOfTargetLibraryEffect) effect).count();
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + ": " + targetName + "'s library is empty."));
            return;
        }

        if (count == 1) {
            resolveSingleCardLook(gameData, entry, controllerId, targetPlayerId, deck, targetName, controllerName);
            return;
        }

        resolveMultiCardLook(gameData, count, controllerId, deck, targetName, controllerName);
    }

    /**
     * Single-card look (Dewdrop Spy): surfaced through a blocking library search so the controller
     * acknowledges the card. It always stays on top — declining leaves it in place, "selecting" it
     * puts it back on top (reorderRemainingToTop + TOP_OF_LIBRARY).
     */
    private void resolveSingleCardLook(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, String targetName, String controllerName) {
        // Public log records only that a look happened, never the card's identity (private look).
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " looks at the top card of " + targetName + "'s library."));

        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, 1);
        String prompt = "The top card of " + targetName + "'s library. It will remain on top.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(true)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                true));

        log.info("Game {} - {} looks at the top card of {}'s library ({})",
                gameData.id, controllerName, targetName, entry.getCard().getName());
    }

    /**
     * Multi-card look (Orcish Spy): a pure informational look with no rearranging permitted. The
     * cards must stay on top in their original order, so this never routes through the pick-one
     * search flow (which would let the controller reorder). Instead the library is left untouched
     * and the top cards are surfaced to the controller with a non-blocking private reveal.
     */
    private void resolveMultiCardLook(GameData gameData, int count, UUID controllerId,
            List<Card> deck, String targetName, String controllerName) {
        int actual = Math.min(count, deck.size());
        List<Card> topCards = new ArrayList<>(deck.subList(0, actual));

        // Public log records only that a look happened, never the cards' identity (private look).
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " looks at the top "
                + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new RevealLibraryTopMessage(cardViews, targetName));

        log.info("Game {} - {} looks at the top {} cards of {}'s library",
                gameData.id, controllerName, actual, targetName);
    }
}
