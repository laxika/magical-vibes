package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a choice to put a specific card from exile onto the battlefield under the controller. */
@Component
@RequiredArgsConstructor
public class PutExiledCardOntoBattlefieldUnderControllerEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutExiledCardOntoBattlefieldUnderControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutExiledCardOntoBattlefieldUnderControllerEffect putEffect =
                (PutExiledCardOntoBattlefieldUnderControllerEffect) effect;
        ExiledCardEntry exiled = gameData.findExiledCard(putEffect.exiledCardId());
        if (exiled == null || !gameData.removeFromExile(putEffect.exiledCardId())) {
            return;
        }

        Card card = exiled.card();
        graveyardReturnSupport.putCardOntoBattlefieldFromExile(gameData, entry.getControllerId(), card);
    }
}
