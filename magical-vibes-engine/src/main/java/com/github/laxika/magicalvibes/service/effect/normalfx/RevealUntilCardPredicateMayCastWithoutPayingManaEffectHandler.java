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
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateMayCastWithoutPayingManaEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateMayCastWithoutPayingManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCardPredicateMayCastWithoutPayingManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        if (typedEffect.exileRevealedCards()) {
            resolveExilingUntil(gameData, entry, typedEffect, controllerId, deck, playerName);
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId,
                    entry.getSourcePermanentId(), null, null, entry.getSourcePermanentSnapshot())) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));

        if (foundCard == null) {
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no matching card was found."));
            return;
        }

        if (typedEffect.castPredicate() != null
                && !predicateEvaluationService.matchesCardPredicate(
                foundCard, typedEffect.castPredicate(), entry.getCard().getId(), gameData,
                controllerId, entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot() == null
                        ? null : entry.getSourcePermanentSnapshot().getEffectivePower())) {
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " cannot cast the revealed " + foundCard.getName()
                            + " without paying its mana cost; the revealed cards go to the bottom"
                            + " of the library in a random order."));
            return;
        }

        String prompt = "You may cast " + foundCard.getName() + " without paying its mana cost.";
        if (typedEffect.exileFoundCard()) {
            revealedCards.remove(foundCard);
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
            exileService.exileCard(gameData, controllerId, foundCard);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    prompt, foundCard.getId()));
            if (typedEffect.shuffleLibrary()) {
                Collections.shuffle(deck);
            }
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(foundCard))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(revealedCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(typedEffect.shuffleLibrary()
                                ? LibrarySearchDestination.CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY
                                : LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
    }

    /**
     * Exiles cards from the top of the library until a card matching the predicate is exiled, then
     * offers that card for a free cast from exile when it is a spell the cast predicate allows. The
     * exiled cards not cast go to the bottom of the library in a random order.
     */
    private void resolveExilingUntil(GameData gameData, StackEntry entry,
                                     RevealUntilCardPredicateMayCastWithoutPayingManaEffect effect,
                                     UUID controllerId, List<Card> deck, String playerName) {
        List<Card> exiledCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            exiledCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, effect.predicate(), entry.getCard().getId(), gameData, controllerId,
                    entry.getSourcePermanentId(), null, null, entry.getSourcePermanentSnapshot())) {
                foundCard = card;
                break;
            }
        }
        gameLogService.append(gameData, GameLog.text(playerName + " exiles "
                + exiledCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                + " from the top of their library."));

        Integer powerLimit = effect.castPredicate() instanceof CardManaValueAtMostSourcePowerPredicate
                ? sourcePower(gameData, entry) : null;
        if (foundCard == null || !canBeOffered(gameData, entry, effect, controllerId, foundCard, powerLimit)) {
            returnExiledCardsToBottom(gameData, deck, exiledCards);
            gameLogService.append(gameData, GameLog.text(playerName
                    + " can't cast an exiled card; the exiled cards go to the bottom of their library"
                    + " in a random order."));
            return;
        }

        String prompt = "You may cast " + foundCard.getName() + " without paying its mana cost.";
        var params = LibrarySearchParams.builder(controllerId, List.of(foundCard))
                .reveals(true)
                .canFailToFind(true)
                .sourceCards(new ArrayList<>(exiledCards))
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING);
        if (powerLimit != null) {
            params.mayCastManaValueAtMost(powerLimit);
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.LibrarySearch(params.build(), prompt, true));
    }

    private void returnExiledCardsToBottom(GameData gameData, List<Card> deck, List<Card> exiledCards) {
        List<Card> bottom = new ArrayList<>(exiledCards);
        Collections.shuffle(bottom);
        bottom.forEach(card -> gameData.removeFromExile(card.getId()));
        deck.addAll(bottom);
    }

    /** Whether the found card is a spell the cast predicate allows, counting an Adventure half on its own. */
    private boolean canBeOffered(GameData gameData, StackEntry entry,
                                 RevealUntilCardPredicateMayCastWithoutPayingManaEffect effect,
                                 UUID controllerId, Card foundCard, Integer powerLimit) {
        if (effect.castPredicate() == null) {
            return true;
        }
        if (powerLimit != null) {
            return !foundCard.hasType(CardType.LAND) && foundCard.getManaValue() <= powerLimit
                    || foundCard.getBackFaceCard() != null
                    && foundCard.getCastingOption(AdventureCast.class).isPresent()
                    && foundCard.getBackFaceCard().getManaValue() <= powerLimit;
        }
        return predicateEvaluationService.matchesCardPredicate(
                foundCard, effect.castPredicate(), entry.getCard().getId(), gameData,
                controllerId, entry.getSourcePermanentId(), null);
    }

    /** The source creature's power as the ability resolves, falling back to its last known power. */
    private Integer sourcePower(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            return gameQueryService.getEffectivePower(gameData, source);
        }
        return entry.getSourcePermanentSnapshot() == null
                ? null : entry.getSourcePermanentSnapshot().getEffectivePower();
    }
}
