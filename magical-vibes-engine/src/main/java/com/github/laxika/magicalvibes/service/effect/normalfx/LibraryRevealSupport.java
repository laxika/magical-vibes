package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Shared library reveal/look-at/scry helpers used by every normal Library Reveal effect handler.
 *
 * <p>Extracted verbatim from the original {@code LibraryRevealResolutionService} monolith;
 * behavior (log strings, interaction ordering) is identical.
 */
@Component
@RequiredArgsConstructor
public class LibraryRevealSupport {

    private final GameLogService gameLogService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    public record TopCardsResult(UUID controllerId, List<Card> topCards, String playerName) {}

    public TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count) {
        return takeTopCardsFromLibrary(gameData, entry, count, false);
    }

    public TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count, boolean broadcastLook) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int actual = Math.min(count, deck.size());
        if (actual == 0) {
            
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(": " + playerName + "'s library is empty.").build());
            return null;
        }

        List<Card> topCards = takeTopCards(deck, actual);

        if (broadcastLook) {
            String logMsg = playerName + " looks at the top " + pluralCards(actual) + " of their library.";
            gameLogService.append(gameData, GameLog.text(logMsg));
        }

        return new TopCardsResult(controllerId, topCards, playerName);
    }

    public void reorderRemainingToBottom(GameData gameData, UUID controllerId, List<Card> topCards) {
        if (topCards.size() == 1) {
            gameData.playerDecks.get(controllerId).add(topCards.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, topCards, true, controllerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }

    /** Every distinct creature card name across all zones, sorted alphabetically (Wood Sage). */
    public List<String> collectCreatureCardNamesInGame(GameData gameData) {
        return collectCardNamesInGame(gameData, card -> matchesCardTypes(card, Set.of(CardType.CREATURE)));
    }

    public List<String> collectAllCardNamesInGame(GameData gameData) {
        return collectCardNamesInGame(gameData, card -> true);
    }

    private List<String> collectCardNamesInGame(GameData gameData, java.util.function.Predicate<Card> candidate) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of()).stream()
                    .filter(p -> candidate.test(p.getCard()))
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.getPlayerExiledCards(pid).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.stream()
                .filter(se -> candidate.test(se.getCard()))
                .forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    public static List<Card> takeTopCards(List<Card> deck, int count) {
        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        return topCards;
    }

    public static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
