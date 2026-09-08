package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardByIdFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a non-targeted return of a known card from its owner's graveyard. */
@Component
@RequiredArgsConstructor
public class ReturnCardByIdFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardByIdFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnCardByIdFromGraveyardToBattlefieldEffect returnEffect =
                (ReturnCardByIdFromGraveyardToBattlefieldEffect) effect;
        ReturnCardFromGraveyardEffect preTargetedReturn = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .targetGraveyard(true)
                .build();
        graveyardReturnSupport.resolvePreTargetedById(gameData, entry, preTargetedReturn,
                entry.getControllerId(), entry.getCard().getId(), returnEffect.cardId());
    }
}
