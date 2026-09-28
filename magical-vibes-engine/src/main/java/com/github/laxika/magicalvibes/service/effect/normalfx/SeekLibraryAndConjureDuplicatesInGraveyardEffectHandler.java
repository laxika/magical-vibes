package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndConjureDuplicatesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Sarevok's seek-and-conjure specialization trigger. */
@Component
@RequiredArgsConstructor
public class SeekLibraryAndConjureDuplicatesInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryAndConjureDuplicatesInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryAndConjureDuplicatesInGraveyardEffect seek =
                (SeekLibraryAndConjureDuplicatesInGraveyardEffect) effect;
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, entry.getControllerId()))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card sought = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));
        graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), sought);

        for (int i = 0; i < 2; i++) {
            Card duplicate = sought.createConjuredCopy();
            duplicate.setOwnerId(entry.getControllerId());
            duplicate.freeze();
            graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), duplicate);
        }
    }
}
