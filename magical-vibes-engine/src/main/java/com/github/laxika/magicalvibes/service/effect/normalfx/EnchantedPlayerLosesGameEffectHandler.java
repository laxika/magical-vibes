package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedPlayerLosesGameEffectHandler implements NormalEffectHandlerBean {

    private final TargetPlayerLosesGameEffectHandler targetPlayerLosesGameEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPlayerLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            targetPlayerLosesGameEffectHandler.resolve(gameData, entry,
                    new TargetPlayerLosesGameEffect(entry.getTargetId()));
        }
    }
}
