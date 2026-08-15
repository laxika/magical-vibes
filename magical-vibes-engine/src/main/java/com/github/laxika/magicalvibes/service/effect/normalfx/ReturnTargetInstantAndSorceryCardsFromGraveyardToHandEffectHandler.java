package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetInstantAndSorceryCardsFromGraveyardToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnTargetInstantAndSorceryCardsFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetInstantAndSorceryCardsFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, entry.targetsForEffect(effect),
                (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");
    }
}
