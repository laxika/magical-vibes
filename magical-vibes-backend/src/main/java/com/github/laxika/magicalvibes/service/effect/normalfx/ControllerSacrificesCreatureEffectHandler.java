package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSacrificesCreatureEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerSacrificesCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
                destructionSupport.performSacrificeCreatureForPlayer(gameData, controllerId);
    }
}
