package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndOrGraveyardForCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;

    private static final CardPredicate PERMANENT_CARD = new CardIsPermanentPredicate();

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryAndOrGraveyardForCardToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchLibraryAndOrGraveyardForCardToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        int xValue = entry.getXValue();
        ManaValueBound manaValueBound = effect.manaValueBound();
        Integer boundValue = manaValueBound == null ? null
                : amountEvaluationService.evaluate(gameData, manaValueBound.amount(),
                        AmountContext.forStackEntry(entry, null)) + manaValueBound.offset();
        boolean searchesLibrary = effect.zones().contains(Zone.LIBRARY);
        boolean librarySearchAllowed = searchesLibrary
                && !librarySearchSupport.isSearchPrevented(gameData, controllerId, false);

        List<Card> graveyardMatches = effect.zones().contains(Zone.GRAVEYARD)
                ? gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                        .filter(card -> matches(card, effect, sourceCardId, gameData, controllerId, xValue,
                                boundValue, manaValueBound))
                        .toList()
                : List.of();

        List<Card> handMatches = effect.zones().contains(Zone.HAND)
                ? gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                        .filter(card -> matches(card, effect, sourceCardId, gameData, controllerId, xValue,
                                boundValue, manaValueBound))
                        .toList()
                : List.of();

        List<Card> libraryMatches = List.of();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
            libraryMatches = deck.stream()
                    .limit(Math.min(topLimit, deck.size()))
                    .filter(card -> matches(card, effect, sourceCardId, gameData, controllerId, xValue,
                            boundValue, manaValueBound))
                    .toList();
        }

        String description = CardPredicateUtils.describeFilter(effect.filter());
        if (boundValue != null) {
            description += manaValueBound.exact()
                    ? " with mana value " + boundValue
                    : " with mana value " + boundValue + " or less";
        }
        if (libraryMatches.isEmpty() && graveyardMatches.isEmpty() && handMatches.isEmpty()) {
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
                if (deck != null) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
                }
                gameLogService.append(gameData, GameLog.text(playerName + " searches their " + searched(effect.zones(), "and")
                        + " but finds no " + description + "." + (deck == null ? "" : " Library is shuffled.")));
            }
            return;
        }

        List<Card> pool = new ArrayList<>(libraryMatches);
        pool.addAll(graveyardMatches);
        pool.addAll(handMatches);
        UUID attachToPermanentId = effect.attachToSource() ? entry.getSourcePermanentId() : null;
        CounterType enterWithCounterType = effect.enterWithCounters() == null
                ? null : effect.enterWithCounters().type();
        int enterWithCounterCount = effect.enterWithCounters() == null ? 0
                : amountEvaluationService.evaluate(gameData, effect.enterWithCounters().count(),
                AmountContext.forStackEntry(entry, null));
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                controllerId, pool, new HashSet<>(libraryMatches.stream().map(Card::getId).toList()),
                new HashSet<>(handMatches.stream().map(Card::getId).toList()), new HashSet<>(),
                librarySearchAllowed, description, LibrarySearchDestination.BATTLEFIELD,
                attachToPermanentId, enterWithCounterType, enterWithCounterCount, 1,
                effect.attachAuraOrEquipment(), searchesLibrary));
        gameLogService.append(gameData, GameLog.text(playerName + " searches their " + searched(effect.zones(), "and/or")
                + "."));
        log.info("Game {} - {} searches library and/or graveyard for a card to battlefield", gameData.id, playerName);
    }

    /** "Library and/or graveyard", "hand and/or library", "graveyard, hand, and/or library". */
    private static String searched(Set<Zone> zones, String conjunction) {
        List<String> names = new ArrayList<>();
        for (Zone zone : List.of(Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY)) {
            if (zones.contains(zone)) {
                names.add(zone == Zone.GRAVEYARD ? "graveyard" : zone == Zone.HAND ? "hand" : "library");
            }
        }
        if (names.equals(List.of("graveyard", "library"))) {
            return "library " + conjunction + " graveyard";
        }
        if (names.size() <= 2) {
            return String.join(" " + conjunction + " ", names);
        }
        return String.join(", ", names.subList(0, names.size() - 1)) + ", " + conjunction + " " + names.getLast();
    }

    /** Only a permanent card can be put onto the battlefield (CR 400.4a). */
    private boolean matches(Card card, SearchLibraryAndOrGraveyardForCardToBattlefieldEffect effect,
                            UUID sourceCardId, GameData gameData,
                            UUID controllerId, int xValue, Integer boundValue, ManaValueBound bound) {
        return predicateEvaluationService.matchesCardPredicate(card, PERMANENT_CARD, sourceCardId, gameData,
                controllerId, null, null, xValue)
                && predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), sourceCardId, gameData, controllerId, null, null, xValue)
                && (bound == null || (bound.exact()
                ? card.getManaValue() == boundValue
                : card.getManaValue() <= boundValue));
    }
}
