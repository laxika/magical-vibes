package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCageCounterOnExiledCardWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Mairsil's optional hand-or-graveyard cage choice. */
@Component
@RequiredArgsConstructor
public class ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffectHandler
        implements NormalEffectHandlerBean {

    private static final CardPredicate ARTIFACT_OR_CREATURE = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardTypePredicate(CardType.CREATURE)));

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> matchingCards = new ArrayList<>();
        addMatchingCards(gameData.playerHands.get(entry.getControllerId()), matchingCards, entry);
        addMatchingCards(gameData.playerGraveyards.get(entry.getControllerId()), matchingCards, entry);
        if (matchingCards.isEmpty()) {
            return;
        }

        playerInputService.beginMultiZoneExileChoice(
                gameData, entry.getControllerId(), matchingCards, 1, entry.getControllerId(),
                "an artifact or creature card", false, entry.getSourcePermanentId(),
                new PutCageCounterOnExiledCardWithSourceEffect());
    }

    private void addMatchingCards(List<Card> cards, List<Card> matchingCards, StackEntry entry) {
        if (cards == null) {
            return;
        }
        cards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, ARTIFACT_OR_CREATURE, entry.getCard().getId()))
                .forEach(matchingCards::add);
    }
}
