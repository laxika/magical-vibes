package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Elvish Elegy's mill, graveyard boost, and optional milled-card return. */
@Component
@RequiredArgsConstructor
public class MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PerpetuallyBoostCreatureCardsInGraveyardEffectHandler boostHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect) effect;
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());

        boostHandler.resolve(gameData, entry, new PerpetuallyBoostCreatureCardsInGraveyardEffect(
                millEffect.powerBoost(), millEffect.toughnessBoost()));

        List<Card> returnable = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.returnFilter(), entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (returnable.isEmpty()) {
            return;
        }

        UUID groupId = UUID.randomUUID();
        for (int i = returnable.size() - 1; i >= 0; i--) {
            Card card = returnable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new ReturnMilledPermanentToHandEffect(groupId, millEffect.returnFilter())),
                    "Put " + card.getName() + " into your hand?"));
        }
    }
}
