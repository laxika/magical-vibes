package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekThreeNonlandPermanentAndChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves Klement's three-card seek and one-card selection. */
@Component
@RequiredArgsConstructor
public class SeekThreeNonlandPermanentAndChooseOneEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekThreeNonlandPermanentAndChooseOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        CardAllOfPredicate filter = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        List<Card> matching = new ArrayList<>(library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList());
        if (matching.isEmpty()) {
            return;
        }

        Collections.shuffle(matching);
        List<Card> sought = new ArrayList<>(matching.subList(0, Math.min(3, matching.size())));
        for (Card card : sought) {
            library.removeIf(libraryCard -> libraryCard.getId().equals(card.getId()));
        }

        if (sought.size() == 1) {
            gameData.addCardToHand(controllerId, sought.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId,
                sought,
                sought.stream().map(Card::getId).toList(),
                false,
                true,
                false,
                true,
                false,
                0,
                null,
                1,
                "Choose one of the sought nonland permanent cards to put into your hand. Shuffle the rest into your library.",
                false,
                1,
                false));
    }
}
