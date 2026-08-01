package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquipoiseEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EquipoiseEffect}: delegates the land → artifact → creature phase-out sequence to
 * {@link EquipoiseSupport}.
 */
@Component
@RequiredArgsConstructor
public class EquipoiseEffectHandler implements NormalEffectHandlerBean {

    private final EquipoiseSupport equipoiseSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquipoiseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        equipoiseSupport.begin(gameData, entry.getCard(), entry.getControllerId(), entry.getTargetId());
    }
}
