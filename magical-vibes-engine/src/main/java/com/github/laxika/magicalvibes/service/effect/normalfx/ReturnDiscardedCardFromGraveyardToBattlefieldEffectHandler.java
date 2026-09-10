package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDiscardedCardFromGraveyardToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a discard follow-up that returns the exact discarded card to the battlefield. */
@Component
@RequiredArgsConstructor
public class ReturnDiscardedCardFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDiscardedCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        var discardedReturn = (ReturnDiscardedCardFromGraveyardToBattlefieldEffect) effect;
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .targetGraveyard(true)
                .enterTapped(discardedReturn.enterTapped())
                .build();
        graveyardReturnSupport.resolvePreTargetedById(gameData, entry, returnEffect,
                entry.getControllerId(), entry.getCard().getId(), discardedCardId);
    }
}
