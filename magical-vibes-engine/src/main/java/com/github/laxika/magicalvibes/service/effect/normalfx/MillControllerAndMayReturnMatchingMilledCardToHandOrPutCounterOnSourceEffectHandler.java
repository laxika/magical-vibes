package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a mill followed by grouped resolution-time offers to return one matching card to hand.
 */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect) effect;
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());

        List<Card> eligibleCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.filter(), null, gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            putCounterOnSource(gameData, entry);
            return;
        }

        UUID groupId = UUID.randomUUID();
        for (int i = eligibleCards.size() - 1; i >= 0; i--) {
            Card card = eligibleCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new ReturnMilledCardToHandOrPutCounterOnSourceEffect(groupId)),
                    "Put " + card.getName() + " into your hand?",
                    card.getId(),
                    null,
                    entry.getSourcePermanentId()));
        }
    }

    void putCounterOnSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        PutCountersOnSourceEffect counterEffect = new PutCountersOnSourceEffect(1, 1, 1);
        StackEntry counterEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(counterEffect),
                0,
                entry.getSourcePermanentId());
        putCountersOnSourceEffectHandler.resolve(gameData, counterEntry, counterEffect);
    }
}
