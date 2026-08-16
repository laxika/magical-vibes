package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutUpToCardsFromHandOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PutUpToCardsFromHandOntoBattlefieldSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutUpToCardsFromHandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutUpToCardsFromHandOntoBattlefieldEffect putEffect =
                (PutUpToCardsFromHandOntoBattlefieldEffect) effect;
        support.beginChoice(gameData, entry.getControllerId(), putEffect.predicate(), putEffect.label(),
                putEffect.maxCount(), entry.getCard().getId(), entry.getCard().getName());
    }
}
