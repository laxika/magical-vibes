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
import com.github.laxika.magicalvibes.model.effect.PutTopCardOfTargetLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
 *       private library reveal message (Orcish Spy — no reordering permitted).</li>
 *   <li>{@code MAY_EXILE_ONE} — optional exile of one looked-at card, rest back on top
 *       (Psychic Surgery, Puresight Merrow).</li>
 *   <li>{@code EXILE_ONE} — mandatory exile of one looked-at card, rest back on top in any order
 *       (Sealed Fate).</li>
 *   <li>{@code MAY_EXILE_ANY_NUMBER} — the may-exile pick repeats until the controller declines,
 *       then the rest go back on top in any order (Ancestral Knowledge).</li>
 *   <li>{@code MAY_SHUFFLE} — the looked-at names go into a may-ability prompt wrapping
 *       {@link ShuffleLibraryEffect} (Visions; the cards stay on top, no reordering).</li>
 *   <li>{@code PUT_ONE_INTO_GRAVEYARD} — mandatory pick of one card for that player's graveyard,
 *       rest back on top in any order (Cruel Fate, Wu Spy).</li>
 *   <li>{@code MAY_PUT_TOP_ON_BOTTOM} — the single top card is shown in a may-ability prompt and,
 *       if accepted, moved to the bottom of that player's library (Coral Fighters).</li>
 *   <li>{@code KEEP_ONE_ON_TOP_EXILE_REST} — the target player picks one card to put back on top of
 *       their library and the rest are exiled (Ashnod's Cylix).</li>
 *   <li>{@code KEEP_ONE_ON_TOP_REST_TO_GRAVEYARD} — the controller picks one card to go back on top
 *       of the target player's library; the rest go into that player's graveyard (Dimir Charm).</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CardRevealService cardRevealService;
    private final AmountEvaluationService amountEvaluationService;

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

        int requested = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()));
        int actual = deck != null ? Math.min(requested, deck.size()) : 0;
        if (actual == 0) {
            if (requested == 0) {
                return;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(": " + targetName + "'s library is empty.").build());
            if (e.action() == com.github.laxika.magicalvibes.model.effect.TargetLibraryAction.LOOK_ONLY) {
                cardRevealService.revealToPlayer(
                        gameData,
                        targetPlayerId,
                        GameEventFact.RevealZone.LIBRARY,
                        List.of(),
                        controllerId);
            }
            return;
        }

        switch (e.action()) {
            case LOOK_ONLY -> resolveLookOnly(gameData, entry, requested, controllerId, targetPlayerId, deck,
                    actual, controllerName, targetName);
            case MAY_EXILE_ONE -> resolveExileOne(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName, true, false);
            case EXILE_ONE -> resolveExileOne(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName, false, false);
            case MAY_EXILE_ANY_NUMBER -> resolveExileOne(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName, true, true);
            case MAY_SHUFFLE -> resolveMayShuffle(gameData, entry, controllerId, targetPlayerId,
                    deck, actual, controllerName, targetName);
            case PUT_ONE_INTO_GRAVEYARD -> resolvePutOneIntoGraveyard(gameData, entry, controllerId,
                    targetPlayerId, deck, actual, controllerName, targetName);
            case MAY_PUT_TOP_ON_BOTTOM -> resolveMayPutTopOnBottom(gameData, entry, controllerId,
                    targetPlayerId, deck, controllerName, targetName);
            case KEEP_ONE_ON_TOP_EXILE_REST -> resolveKeepOneOnTopExileRest(gameData, entry,
                    targetPlayerId, deck, actual, targetName);
            case KEEP_ONE_ON_TOP_REST_TO_GRAVEYARD -> resolveKeepOneOnTopRestToGraveyard(gameData, entry,
                    controllerId, targetPlayerId, deck, actual, controllerName, targetName);
        }
    }

    /**
     * Single-card look (Dewdrop Spy): surfaced through a blocking library search so the controller
     * acknowledges the card. It always stays on top — declining leaves it in place, "selecting" it
     * puts it back on top (reorderRemainingToTop + TOP_OF_LIBRARY). Multi-card look (Orcish Spy):
     * a pure informational look with no rearranging permitted — the library is left untouched and
     * the top cards are surfaced to the controller with a non-blocking private reveal.
     */
    private void resolveLookOnly(GameData gameData, StackEntry entry, int requested,
            UUID controllerId, UUID targetPlayerId, List<Card> deck, int actual,
            String controllerName, String targetName) {
        if (requested == 1) {
            gameLogService.append(gameData, GameLog.text(controllerName + " looks at the top card of " + targetName + "'s library."));
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
        gameLogService.append(gameData, GameLog.text(controllerName + " looks at the top "
                + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        cardRevealService.revealToPlayer(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.LIBRARY,
                topCards,
                controllerId);
        log.info("Game {} - {} looks at the top {} cards of {}'s library",
                gameData.id, controllerName, actual, targetName);
    }

    /**
     * Optional ({@code optional=true}, Psychic Surgery / Puresight Merrow) or mandatory
     * ({@code optional=false}, Sealed Fate) exile of one looked-at card; the rest go back on top
     * in any order. With {@code anyNumber} the pick repeats after every exile until the controller
     * declines or the cards run out (Ancestral Knowledge).
     */
    private void resolveExileOne(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName,
            boolean optional, boolean anyNumber) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameLogService.append(gameData, GameLog.text(
                controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = (anyNumber
                ? "You may exile any number of these cards, one at a time."
                : optional ? "You may exile one of these cards." : "Exile one of these cards.")
                + " The rest will be put on top of the library.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(optional)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(sourceCards)
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .repeatUntilDecline(anyNumber)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.EXILE)
                        .build(),
                prompt,
                optional));
        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    }

    private void resolveMayShuffle(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        String sourceName = entry.getCard().getName();
        String names = deck.subList(0, actual).stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
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

    /**
     * Coral Fighters: the controller sees the single top card in the prompt (that is the "look")
     * and may send it to the bottom of that player's library; declining leaves it on top.
     */
    private void resolveMayPutTopOnBottom(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, String controllerName, String targetName) {
        String sourceName = entry.getCard().getName();
        gameLogService.append(gameData, GameLog.text(
                controllerName + " looks at the top card of " + targetName + "'s library."));
        String prompt = sourceName + " — Top card of " + targetName + "'s library: " + deck.getFirst().getName()
                + ". Put it on the bottom of that library?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new PutTopCardOfTargetLibraryOnBottomEffect()),
                prompt,
                targetPlayerId));
        log.info("Game {} - {} looks at the top card of {}'s library ({})",
                gameData.id, controllerName, targetName, sourceName);
    }

    /**
     * Ashnod's Cylix: the <em>target player</em> looks at the cards and picks the one that goes back
     * on top of their own library; everything else is exiled. The searching player is therefore the
     * target player, not the ability's controller, and the pick is mandatory.
     */
    private void resolveKeepOneOnTopExileRest(GameData gameData, StackEntry entry,
            UUID targetPlayerId, List<Card> deck, int actual, String targetName) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameLogService.append(gameData, GameLog.text(targetName + " looks at the top "
                + LibraryRevealSupport.pluralCards(actual) + " of their library."));
        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = "Put one of these cards back on top of your library. The rest will be exiled.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(targetPlayerId, topCards)
                        .canFailToFind(false)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(sourceCards)
                        .restToExile(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                false));
        log.info("Game {} - {} looks at the top {} cards of their library ({})",
                gameData.id, targetName, actual, entry.getCard().getName());
    }

    /**
     * Dimir Charm's mill mode: the controller looks at the cards, picks the one that goes back on
     * top of the <em>target player's</em> library, and everything else is put into that player's
     * graveyard. The pick is mandatory.
     */
    private void resolveKeepOneOnTopRestToGraveyard(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameLogService.append(gameData, GameLog.text(controllerName + " looks at the top "
                + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = "Put one of these cards back on top of " + targetName
                + "'s library. The rest will be put into that player's graveyard.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(false)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(sourceCards)
                        .restToGraveyard(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                false));
        log.info("Game {} - {} looks at the top {} cards of {}'s library to keep one on top ({})",
                gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    }

    private void resolvePutOneIntoGraveyard(GameData gameData, StackEntry entry, UUID controllerId,
            UUID targetPlayerId, List<Card> deck, int actual, String controllerName, String targetName) {
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);
        gameLogService.append(gameData, GameLog.text(
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
