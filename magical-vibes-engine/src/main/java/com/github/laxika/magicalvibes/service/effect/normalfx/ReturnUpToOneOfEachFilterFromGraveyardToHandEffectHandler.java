package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnUpToOneOfEachFilterFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> graveyardReturnSupport.addCardToHandFromGraveyard(
                        gameData, entry.getControllerId(), entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");
    }
}
