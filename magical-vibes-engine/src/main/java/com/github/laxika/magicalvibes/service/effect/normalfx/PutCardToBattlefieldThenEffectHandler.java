package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardToBattlefieldThenEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldThenEffect) effect;
        PutCardToBattlefieldEffect putEffect = new PutCardToBattlefieldEffect(
                e.predicate(), e.label(), e.enterTapped());
        playerInteractionSupport.applyPutCardToBattlefield(gameData, entry.getControllerId(), putEffect,
                entry.getXValue(), null, entry.getCard() == null ? null : entry.getCard().getId(),
                e.thenEffect(), e.thenCondition());
    }
}
