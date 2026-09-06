package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves the per-controller library replacement rider used by Indomitable Creativity. */
@Component
@RequiredArgsConstructor
public class RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CardSpecificSupport cardSpecificSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LegendRuleService legendRuleService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reveal = (RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect) effect;
        Map<UUID, Integer> requiredByController = new LinkedHashMap<>();
        for (UUID controllerId : entry.getEventPlayerIds()) {
            requiredByController.merge(controllerId, 1, Integer::sum);
        }

        if (requiredByController.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> cardsToEnterByController = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> requirement : requiredByController.entrySet()) {
            List<Card> foundCards = revealCards(
                    gameData, entry, requirement.getKey(), requirement.getValue(), reveal.cardTypes());
            if (!foundCards.isEmpty()) {
                cardsToEnterByController.put(requirement.getKey(), foundCards);
            }
        }

        enterCardsSimultaneously(gameData, cardsToEnterByController);
    }

    private List<Card> revealCards(GameData gameData, StackEntry entry, UUID controllerId,
                                   int requiredCount, Set<CardType> cardTypes) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return List.of();
        }

        List<Card> revealedCards = new ArrayList<>();
        List<Card> foundCards = new ArrayList<>();
        List<Card> cardsToReturn = new ArrayList<>();
        while (!library.isEmpty() && foundCards.size() < requiredCount) {
            Card card = library.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, cardTypes)) {
                foundCards.add(card);
                gameData.addToExile(ownerId(controllerId, card), card);
            } else {
                cardsToReturn.add(card);
            }
        }

        library.addAll(cardsToReturn);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        logReveal(gameData, entry, controllerId, revealedCards);
        return foundCards;
    }

    private void enterCardsSimultaneously(GameData gameData, Map<UUID, List<Card>> cardsByController) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Set<UUID> blockedCardIds = new HashSet<>();
        cardsByController.values().stream()
                .flatMap(List::stream)
                .filter(card -> gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.EXILE))
                .map(Card::getId)
                .forEach(blockedCardIds::add);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<UUID, List<Card>> enteredCardsByController = new LinkedHashMap<>();

        for (Map.Entry<UUID, List<Card>> cards : cardsByController.entrySet()) {
            UUID controllerId = cards.getKey();
            for (Card card : cards.getValue()) {
                if (!gameData.removeFromExile(card.getId())) {
                    continue;
                }
                if (blockedCardIds.contains(card.getId())) {
                    gameData.addToExile(ownerId(controllerId, card), card);
                    continue;
                }

                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromExile(true);
                initializeStartingCounters(permanent, card);
                UUID enteringController = battlefieldEntryService.resolveEnteringController(
                        gameData, controllerId, permanent);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, enteringController, permanent, enterTappedTypes, simultaneouslyEntered);

                if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                    simultaneouslyEntered.add(permanent);
                    enteredCardsByController
                            .computeIfAbsent(enteringController, ignored -> new ArrayList<>())
                            .add(card);
                }
            }
        }

        for (Map.Entry<UUID, List<Card>> entered : enteredCardsByController.entrySet()) {
            for (Card card : entered.getValue()) {
                battlefieldEntryService.processCreatureETBEffects(
                        gameData, entered.getKey(), card, null, false);
            }
        }

        if (!gameData.interaction.isAwaitingInput()) {
            enteredCardsByController.keySet().forEach(
                    controllerId -> legendRuleService.checkLegendRule(gameData, controllerId));
        }
    }

    private void initializeStartingCounters(Permanent permanent, Card card) {
        if (card.hasType(CardType.PLANESWALKER)) {
            permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty() != null ? card.getLoyalty() : 0);
            permanent.setSummoningSick(false);
        } else if (card.hasType(CardType.BATTLE)) {
            permanent.setCounterCount(CounterType.DEFENSE, card.getDefense() != null ? card.getDefense() : 0);
            permanent.setSummoningSick(false);
        }
    }

    private UUID ownerId(UUID controllerId, Card card) {
        return card.getOwnerId() != null ? card.getOwnerId() : controllerId;
    }

    private void logReveal(GameData gameData, StackEntry entry, UUID controllerId, List<Card> revealedCards) {
        if (revealedCards.isEmpty()) {
            return;
        }
        GameLog.Builder builder = GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(revealedCards.get(i));
        }
        builder.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, builder.build());
    }
}
