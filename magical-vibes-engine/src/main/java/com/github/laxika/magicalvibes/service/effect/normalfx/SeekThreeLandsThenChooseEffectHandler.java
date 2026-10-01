package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekThreeLandsThenChooseEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Song of Seasons' three-card Seek and destination choices. */
@Component
@RequiredArgsConstructor
public class SeekThreeLandsThenChooseEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardPredicate> SEEK_FILTERS = List.of(
            landWithSubtype(CardSubtype.MOUNTAIN),
            landWithSubtype(CardSubtype.FOREST),
            new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.LAND),
                    new CardNotPredicate(new CardSubtypePredicate(CardSubtype.MOUNTAIN)),
                    new CardNotPredicate(new CardSubtypePredicate(CardSubtype.FOREST)))));

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekThreeLandsThenChooseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> soughtCards = new ArrayList<>(SEEK_FILTERS.size());
        for (CardPredicate filter : SEEK_FILTERS) {
            List<Card> matchingCards = library.stream()
                    .filter(card -> !card.isToken())
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, filter, null, gameData, controllerId))
                    .toList();
            if (matchingCards.isEmpty()) {
                continue;
            }

            Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            library.removeIf(card -> card.getId().equals(chosen.getId()));
            soughtCards.add(chosen);
        }

        if (soughtCards.isEmpty()) {
            return;
        }

        for (Card soughtCard : soughtCards) {
            gameData.addCardToHand(controllerId, soughtCard);
            triggerCollectionService.checkControllerCardPutIntoHandFromLibraryTriggers(
                    gameData, controllerId, soughtCard);
        }
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, soughtCards);

        Set<UUID> soughtCardIds = soughtCards.stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
        List<Integer> validIndices = new ArrayList<>();
        List<Card> hand = gameData.playerHands.get(controllerId);
        for (int i = 0; i < hand.size(); i++) {
            if (soughtCardIds.contains(hand.get(i).getId())) {
                validIndices.add(i);
            }
        }

        playerInputService.beginCardChoice(
                gameData, controllerId, validIndices,
                "Choose one of those cards to put onto the battlefield.",
                false, false, false, null, false, false, null, null, false,
                false, 0, 0, Set.of(), null, false, false,
                new PutChosenCardFromHandIntoGraveyardEffect(soughtCardIds), null);
    }

    private static CardPredicate landWithSubtype(CardSubtype subtype) {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(subtype)));
    }
}
