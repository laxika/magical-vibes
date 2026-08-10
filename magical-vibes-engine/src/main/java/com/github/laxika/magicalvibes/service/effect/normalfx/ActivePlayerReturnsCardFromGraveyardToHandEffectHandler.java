package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActivePlayerReturnsCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ActivePlayerReturnsCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var activePlayerEffect = (ActivePlayerReturnsCardFromGraveyardToHandEffect) effect;
        UUID activePlayerId = entry.getActivePlayerId() != null
                ? entry.getActivePlayerId() : entry.getControllerId();
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(activePlayerEffect.filter())
                .build();
        graveyardReturnSupport.resolveFromControllersGraveyard(
                gameData, entry, returnEffect, activePlayerId, entry.getCard().getId());
    }
}
