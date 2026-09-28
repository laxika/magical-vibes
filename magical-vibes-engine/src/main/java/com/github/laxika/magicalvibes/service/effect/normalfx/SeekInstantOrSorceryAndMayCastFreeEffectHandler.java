package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekInstantOrSorceryAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Karlach's blue face by seeking one eligible card into hand and offering a free cast. */
@Component
@RequiredArgsConstructor
public class SeekInstantOrSorceryAndMayCastFreeEffectHandler implements NormalEffectHandlerBean {

    private static final CardPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));
    private static final CardPredicate ELIGIBLE = new CardAllOfPredicate(List.of(
            INSTANT_OR_SORCERY, new CardMaxManaValuePredicate(3)));

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekInstantOrSorceryAndMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> eligible = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, ELIGIBLE, null, gameData, controllerId))
                .toList();
        if (eligible.isEmpty()) {
            return;
        }

        Card sought = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));
        gameData.addCardToHand(controllerId, sought);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sought,
                controllerId,
                List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                "Cast " + sought.getName() + " without paying its mana cost?"));
    }
}
