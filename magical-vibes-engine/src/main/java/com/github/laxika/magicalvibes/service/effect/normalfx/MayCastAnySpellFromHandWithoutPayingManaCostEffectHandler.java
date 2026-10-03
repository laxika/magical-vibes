package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 *
 * <p>Reuses the Counterlash may-cast-from-hand routing: one {@link PendingMayAbility} is queued per
 * eligible nonland hand card, and accepting one casts it for free while clearing the remaining
 * offers. A null filter (Maelstrom Archangel) matches every nonland; Wildfire Eternal passes an
 * instant-or-sorcery filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastAnySpellFromHandWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    private final com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastAnySpellFromHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastAnySpellFromHandWithoutPayingManaCostEffect e =
                (MayCastAnySpellFromHandWithoutPayingManaCostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;
        if (e.revealDrawnCards()) {
            cardRevealService.revealToAllPlayers(gameData, controllerId,
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND,
                    hand.stream().filter(card -> entry.getDrawnCardIdsThisResolution().contains(card.getId())).toList());
        }
        int maxManaValue = e.maxManaValue() == null
                ? Integer.MAX_VALUE
                : amountEvaluationService.evaluate(gameData, e.maxManaValue(),
                        AmountContext.forStackEntry(entry, entry.getSourcePermanentId() == null
                                ? null
                                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())));

        List<Card> eligible = hand.stream()
                .filter(c -> !e.drawnCardsOnly() || entry.getDrawnCardIdsThisResolution().contains(c.getId()))
                .filter(c -> !c.hasType(CardType.LAND))
                .filter(c -> castingCostService.canPayAdditionalSpellCosts(gameData, controllerId, c))
                .filter(c -> c.getManaValue() <= maxManaValue)
                .filter(c -> predicateEvaluationService.matchesCardPredicate(
                        c, e.spellFilter(), null, gameData, controllerId))
                .toList();

        UUID choiceGroupId = UUID.randomUUID();
        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card c = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(
                            e.revealDrawnCards(), choiceGroupId, null, false, e.afterSuccessfulCastEffect())),
                    "Cast " + c.getName() + " without paying its mana cost?",
                    entry.getSourcePermanentId(), (Integer) null
            ));
        }
    }
}
