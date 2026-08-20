package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentWithManaValueConditionalEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentWithManaValueConditionalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTargetPermanentWithManaValueConditionalEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);

        if (manaValue <= destroy.maxManaValue()) {
            CardEffect bonusEffect = destroy.conditionalEffect();
            EffectHandler handler = effectHandlerRegistry.getHandler(bonusEffect);
            if (handler != null) {
                handler.resolve(gameData, entry, bonusEffect);
            }
        }
    }
}
