package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForSourceControllerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenForSourceControllerEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenForSourceControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenForSourceControllerEffect) effect;
        if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
            log.info("Game {} - CreateTokenForSourceControllerEffect fizzles (no valid target player)", gameData.id);
            return;
        }
        createTokenEffectHandler.resolveForController(
                gameData, entry, e.tokenEffect(), entry.getControllerId());
    }
}
