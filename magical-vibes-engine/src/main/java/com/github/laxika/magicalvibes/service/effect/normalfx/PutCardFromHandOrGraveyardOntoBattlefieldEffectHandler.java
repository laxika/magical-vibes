package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PutCardFromHandOrGraveyardOntoBattlefieldSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardFromHandOrGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCardFromHandOrGraveyardOntoBattlefieldEffect putEffect =
                (PutCardFromHandOrGraveyardOntoBattlefieldEffect) effect;
        support.beginChoice(gameData, entry.getControllerId(), putEffect.predicate(), putEffect.label(),
                entry.getCard().getId(), entry.getCard().getName());
    }
}
