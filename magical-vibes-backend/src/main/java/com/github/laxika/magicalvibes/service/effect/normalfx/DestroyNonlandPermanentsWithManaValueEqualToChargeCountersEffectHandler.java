package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        destructionSupport.destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), entry.getCard().getName(), null);
    }
}
