package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
import java.util.EnumSet;
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
        var search = (SearchLibraryAndOrGraveyardForCardToBattlefieldEffect) effect;
        // "A card with the same name as that spell" (Mishra, Artificer Prodigy): the triggering spell's name, read now.
        CardPredicate filter = AmountEvaluationService.bindTriggeringSpellName(gameData, search.filter(), entry);
        if (filter != search.filter()) {
            search = new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(filter, search.zones(),
                    search.attachToSource(), search.attachAuraOrEquipment(), search.manaValueBound(),
                    search.enterWithCounters());
        }
        doResolve(gameData, entry, search);
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
        List<Card> pool = new ArrayList<>(libraryMatches);
        pool.addAll(graveyardMatches);
        pool.addAll(handMatches);
        UUID attachToPermanentId = effect.attachToSource() ? entry.getSourcePermanentId() : null;
        CounterType enterWithCounterType = effect.enterWithCounters() == null
                ? null : effect.enterWithCounters().type();
        int enterWithCounterCount = effect.enterWithCounters() == null ? 0
                : amountEvaluationService.evaluate(gameData, effect.enterWithCounters().count(),
                AmountContext.forStackEntry(entry, null));
        UUID oppositionAgentControllerId = librarySearchAllowed
                ? librarySearchSupport.oppositionAgentControllerId(gameData, controllerId)
                : null;
        // A card in a searched hand can be left unfound, as the player may search the hand alone (CR 701.23b).
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                        controllerId, pool, new HashSet<>(libraryMatches.stream().map(Card::getId).toList()),
                        new HashSet<>(handMatches.stream().map(Card::getId).toList()), new HashSet<>(),
                        librarySearchAllowed, description, LibrarySearchDestination.BATTLEFIELD,
                        attachToPermanentId, enterWithCounterType, enterWithCounterCount, 1,
                        effect.attachAuraOrEquipment(), searchesLibrary, oppositionAgentControllerId, Set.of(),
                        effect.zones().contains(Zone.HAND));
        Set<Zone> otherZones = EnumSet.noneOf(Zone.class);
        otherZones.addAll(effect.zones());
        otherZones.remove(Zone.LIBRARY);
        if (librarySearchAllowed && !otherZones.isEmpty()) {
            // "And/or": the player chooses whether the library is among the zones searched (CR 701.23a).
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.AndOrSearchZonesChoice(choice,
                    otherZones));
            return;
        }
        if (pool.isEmpty()) {
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
                if (deck != null) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
                }
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no "
                        + description + "." + (deck == null ? "" : " Library is shuffled.")));
            }
            return;
        }
        interactionHandlerRegistry.begin(gameData, choice);
        log.info("Game {} - {} searches for a card to put onto the battlefield", gameData.id, playerName);
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
