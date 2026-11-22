package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CatalogEntry;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private CardCatalog cardCatalog;
    private volatile java.util.Map<String, List<OracleData>> catalogSplitCardsByHalfName;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setCardCatalog(CardCatalog cardCatalog) {
        this.cardCatalog = cardCatalog;
    }

    public record TopCardsResult(UUID controllerId, List<Card> topCards, String playerName) {}

    public TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count) {
        return takeTopCardsFromLibrary(gameData, entry, count, false);
    }

    public TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count, boolean broadcastLook) {
        return takeTopCardsFromLibrary(gameData, entry, entry.getControllerId(), count, broadcastLook);
    }

    public TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, UUID controllerId,
                                                  int count, boolean broadcastLook) {
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

    /** Validates a name outside the current game against implemented printings and type restrictions. */
    public boolean isCatalogCardNameAllowed(String name, List<CardType> excludedTypes) {
        if (cardCatalog == null) {
            return false;
        }
        String identifier = name.replaceAll("[^\\p{L}\\p{N}]", "");
        Set<String> checkedClasses = new java.util.HashSet<>();
        for (CardSet set : CardSet.values()) {
            for (var printing : cardCatalog.getPrintings(set)) {
                if (!printing.simpleCardClassName().equalsIgnoreCase(identifier)
                        || !checkedClasses.add(printing.cardClassName())) {
                    continue;
                }
                Card candidate = printing.createCard();
                if (candidate.getNames().stream().anyMatch(name::equalsIgnoreCase)
                        && excludedTypes.stream().noneMatch(candidate::hasType)) {
                    return true;
                }
            }
        }
        // A split card's name is one of its halves' names, never both at once (CR 709.4a).
        for (OracleData splitCard : catalogSplitCardsByHalfName().getOrDefault(name.toLowerCase(java.util.Locale.ROOT), List.of())) {
            if (excludedTypes.stream().noneMatch(splitCard::hasType)) {
                return true;
            }
        }
        // A double-faced card has two names, and either face's name may be chosen.
        for (CatalogEntry entry : cardCatalog.getEntries()) {
            OracleData backFace = entry.backFace();
            if (backFace != null && name.equalsIgnoreCase(backFace.name())
                    && excludedTypes.stream().noneMatch(backFace::hasType)) {
                return true;
            }
        }
        return false;
    }

    /** One catalog card's oracle data per split card, keyed by each half's lower-cased name. */
    private java.util.Map<String, List<OracleData>> catalogSplitCardsByHalfName() {
        java.util.Map<String, List<OracleData>> cached = catalogSplitCardsByHalfName;
        if (cached != null) {
            return cached;
        }
        java.util.Map<String, List<OracleData>> byHalfName = new java.util.HashMap<>();
        for (CatalogEntry entry : cardCatalog.getEntries()) {
            List<String> names = entry.oracle().names();
            if (names.size() < 2) {
                continue;
            }
            for (String half : names) {
                byHalfName.computeIfAbsent(half.toLowerCase(java.util.Locale.ROOT), key -> new ArrayList<>())
                        .add(entry.oracle());
            }
        }
        byHalfName.replaceAll((key, cards) -> List.copyOf(cards));
        cached = java.util.Map.copyOf(byHalfName);
        catalogSplitCardsByHalfName = cached;
        return cached;
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
