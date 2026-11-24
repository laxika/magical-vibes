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
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MayCastDiscardedCardWithoutPayingManaCostEffect}: each card its subject names in exile, or in its
 * owner's graveyard when the caster (the controller, or the player {@code castBy} names) owns it, that matches the effect's filter and is not a land card, is offered
 * for a free cast as part of the resolution (a card moved to a public zone can be found there, CR 400.7j). A card in a
 * graveyard is cast from there (only from the caster's own graveyard); one in exile, whoever owns it, through the
 * exile free-cast offer (Transcendent Dragon casts the opponent's spell it countered). A mana-value cap read from an
 * amount ("if the spell's mana value is less than or equal to the life you gained this turn") and a spell-relative
 * filter ("shares a card type with it") are evaluated once, as the effect resolves (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class MayCastDiscardedCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastDiscardedCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var castEffect = (MayCastDiscardedCardWithoutPayingManaCostEffect) effect;
        UUID casterId = castEffect.caster() == null ? entry.getControllerId()
                : subjectResolutionService.player(gameData, entry, castEffect, castEffect.caster());
        if (casterId == null) {
            return;
        }
        List<SubjectResolutionService.LocatedCard> cards =
                subjectResolutionService.locatedCards(gameData, entry, castEffect, castEffect.cards());
        CardPredicate filter = amountEvaluationService.bindAmounts(gameData,
                subjectResolutionService.bindResolutionFilter(gameData, entry, castEffect, castEffect.filter()),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()));
        for (SubjectResolutionService.LocatedCard located : cards.reversed()) {
            Card card = located.card();
            if (located.zone() != Zone.EXILE && !casterId.equals(located.ownerId())
                    || card.hasType(CardType.LAND)
                    || filter != null && !predicateEvaluationService.matchesCardPredicate(
                    card, filter, entry.getCard().getId())) {
                continue;
            }
            String prompt = entry.getCard().getName() + " — Cast " + card.getName()
                    + " without paying its mana cost?";
            if (located.zone() == Zone.EXILE) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        card,
                        casterId,
                        List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                        prompt,
                        card.getId()));
                continue;
            }
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    casterId,
                    List.of(new PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(
                            filter == null ? NONLAND : filter)),
                    prompt));
        }
    }
}
