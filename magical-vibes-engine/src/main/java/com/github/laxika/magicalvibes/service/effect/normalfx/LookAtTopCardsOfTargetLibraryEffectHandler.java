package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Look at the top N cards of target player's library, then apply the record's
 * {@code TargetLibraryAction}. Handles {@link LookAtTopCardsOfTargetLibraryEffect}, the collapsed
 * target-library look family; each action branch keeps its original flow verbatim:
 *
 * <ul>
 *   <li>{@code LOOK_ONLY} — single-card look is a blocking acknowledge via
 *       {@link PendingInteraction.LibrarySearch} that puts the card back on top (Dewdrop Spy);
 *       multi-card look leaves the library untouched and surfaces the cards with a non-blocking
 *       private {@link RevealLibraryTopMessage} (Orcish Spy — no reordering permitted).</li>
 *   <li>{@code MAY_EXILE_ONE} — optional exile of one looked-at card, rest back on top
 *       (Psychic Surgery, Puresight Merrow).</li>
 *   <li>{@code MAY_SHUFFLE} — the looked-at names go into a may-ability prompt wrapping
 *       {@link ShuffleLibraryEffect} (Visions; the cards stay on top, no reordering).</li>
 *   <li>{@code PUT_ONE_INTO_GRAVEYARD} — mandatory pick of one card for that player's graveyard,
 *       rest back on top in any order (Cruel Fate, Wu Spy).</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CardViewFactory cardViewFactory;
    private final SessionManager sessionManager;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsOfTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsOfTargetLibraryEffect e = (LookAtTopCardsOfTargetLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId() != null ? entry.getTargetId() : controllerId;
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int actual = deck != null ? Math.min(e.count(), deck.size()) : 0;
        if (actual == 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(": " + targetName + "'s library is empty.").build());
            return;
        }

        switch (e.action()) {
            case LOOK_ONLY -> resolveLookOnly(gameData, entry, e, controllerId, targetPlayerId, deck,
                    actual, controllerName, targetName);
            case MAY_EXILE_ONE -> resolveMayExileOne(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName);
            case MAY_SHUFFLE -> resolveMayShuffle(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName);
            case PUT_ONE_INTO_GRAVEYARD -> resolvePutOneIntoGraveyard(gameData, entry, controllerId,
                    targetPlayerId, deck, actual, controllerName, targetName);
        }
    }

    /**
     * Single-card look (Dewdrop Spy): surfaced through a blocking library search so the controller
     * acknowledges the card. It always stays on top — declining leaves it in place, "selecting" it
     * puts it back on top (reorderRemainingToTop + TOP_OF_LIBRARY). Multi-card look (Orcish Spy):
     * a pure informational look with no rearranging permitted — the library is left untouched and
     * the top cards are surfaced to the controller with a non-blocking private reveal.
     */
    private void resolveLookOnly(GameData gameData, StackEntry entry, LookAtTopCardsOfTargetLibraryEffect e,
            UUID controllerId, UUID targetPlayerId, List<Card> deck, int actual,
            String controllerName, String targetName) {
        if (e.count() == 1) {
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
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, actual));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " looks at the top "
                + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new RevealLibraryTopMessage(cardViews, targetName));
        log.info("Game {} - {} looks at the top {} cards of {}'s library",
                gameData.id, controllerName, actual, targetName);
    }

    private void resolveMayExileOne(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        List<Card> sourceCards = new ArrayList<>(topCards);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(true)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(sourceCards)
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt("You may exile one of these cards. The rest will be put on top of the library.")
                        .destination(LibrarySearchDestination.EXILE)
                        .build(),
                "You may exile one of these cards. The rest will be put on top of the library.",
                true));
        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    }

    private void resolveMayShuffle(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        String sourceName = entry.getCard().getName();
        String names = deck.subList(0, actual).stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, sourceName);
        String prompt = sourceName + " — Top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName
                + "'s library: " + names + ". Have that player shuffle their library?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new ShuffleLibraryEffect(true)),
                prompt,
                targetPlayerId));
    }

    private void resolvePutOneIntoGraveyard(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = "Put one of these cards into that player's graveyard. The rest will be put on top of the library in any order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(false)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(sourceCards)
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.GRAVEYARD)
                        .build(),
                prompt,
                false));
        log.info("Game {} - {} looks at top {} of {}'s library to mill one ({})", gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    }
}
