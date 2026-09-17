package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null) {
            targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        }
        if (targetCardId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int maxManaValue = amountEvaluationService.evaluate(
                gameData, e.maxManaValue(), AmountContext.forStackEntry(entry, source));

        var returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        var targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard != null && targetCard.getManaValue() > maxManaValue) {
            returnEffect = returnEffect.toBuilder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .build();
        }

        graveyardReturnSupport.resolvePreTargetedById(
                gameData, entry, returnEffect, entry.getControllerId(), entry.getCard().getId(), targetCardId);
    }
}
