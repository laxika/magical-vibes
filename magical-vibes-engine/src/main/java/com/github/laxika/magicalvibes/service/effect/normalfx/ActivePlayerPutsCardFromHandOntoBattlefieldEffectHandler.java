package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerPutsCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActivePlayerPutsCardFromHandOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ActivePlayerPutsCardFromHandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var activePlayerEffect = (ActivePlayerPutsCardFromHandOntoBattlefieldEffect) effect;
        UUID activePlayerId = entry.getActivePlayerId() != null
                ? entry.getActivePlayerId() : entry.getControllerId();
        PutCardToBattlefieldEffect putEffect = new PutCardToBattlefieldEffect(
                activePlayerEffect.predicate(), activePlayerEffect.label());
        playerInteractionSupport.applyPutCardToBattlefield(
                gameData, activePlayerId, putEffect, entry.getXValue(), null, entry.getCard().getId());
    }
}
