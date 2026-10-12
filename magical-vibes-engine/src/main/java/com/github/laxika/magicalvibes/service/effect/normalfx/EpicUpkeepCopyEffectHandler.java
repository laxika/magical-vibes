package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EpicUpkeepCopyEffect;
import com.github.laxika.magicalvibes.service.epic.EpicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EpicUpkeepCopyEffectHandler implements NormalEffectHandlerBean {

    private final EpicService epicService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EpicUpkeepCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        epicService.createUpkeepCopy(gameData, entry.getControllerId(), entry.getCard(), entry.getTargetId());
    }
}
