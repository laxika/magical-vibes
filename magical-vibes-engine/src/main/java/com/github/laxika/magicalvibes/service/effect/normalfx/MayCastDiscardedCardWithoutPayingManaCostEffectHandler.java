package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MayCastDiscardedCardWithoutPayingManaCostEffect}: each card its subject names in its owner's
 * graveyard or exile that the controller owns, that matches the effect's filter and is not a land card, is offered
 * for a free cast as part of the resolution (a card moved to a public zone can be found there, CR 400.7j). A card in a
 * graveyard is cast from there; one in exile through the exile free-cast offer.
 */
@Component
@RequiredArgsConstructor
public class MayCastDiscardedCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastDiscardedCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var castEffect = (MayCastDiscardedCardWithoutPayingManaCostEffect) effect;
        List<SubjectResolutionService.LocatedCard> cards =
                subjectResolutionService.locatedCards(gameData, entry, castEffect, castEffect.cards());
        for (SubjectResolutionService.LocatedCard located : cards.reversed()) {
            Card card = located.card();
            if (!entry.getControllerId().equals(located.ownerId()) || card.hasType(CardType.LAND)
                    || castEffect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                    card, castEffect.filter(), entry.getCard().getId())) {
                continue;
            }
            String prompt = entry.getCard().getName() + " — Cast " + card.getName()
                    + " without paying its mana cost?";
            if (located.zone() == Zone.EXILE) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        card,
                        entry.getControllerId(),
                        List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                        prompt,
                        card.getId()));
                continue;
            }
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(
                            castEffect.filter() == null ? NONLAND : castEffect.filter())),
                    prompt));
        }
    }
}
