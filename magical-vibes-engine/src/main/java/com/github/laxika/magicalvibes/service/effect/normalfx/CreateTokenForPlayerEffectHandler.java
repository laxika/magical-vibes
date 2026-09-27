package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForPlayerEffect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateTokenForPlayerEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenForPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenForPlayerEffect createToken = (CreateTokenForPlayerEffect) effect;
        if (!gameData.playerIds.contains(createToken.playerId())) {
            return;
        }
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, createToken.playerId(), createToken.token(), 1, entry.getCard().getSetCode()));
    }
}
