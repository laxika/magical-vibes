package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Sparring Dummy's mill, optional land return, and Lesson tracking. */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayReturnMilledLandToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayReturnMilledLandToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), 1);
        entry.setEventValue(milled.stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardSubtypePredicate(CardSubtype.LESSON), entry.getCard().getId(),
                        gameData, entry.getControllerId())) ? 1 : 0);

        Card land = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardTypePredicate(CardType.LAND), entry.getCard().getId(),
                        gameData, entry.getControllerId()))
                .findFirst()
                .orElse(null);
        if (land == null) {
            return;
        }

        UUID groupId = UUID.randomUUID();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                land,
                entry.getControllerId(),
                List.of(new ReturnMilledPermanentToHandEffect(
                        groupId, new CardTypePredicate(CardType.LAND))),
                "Put " + land.getName() + " into your hand?"));
    }
}
