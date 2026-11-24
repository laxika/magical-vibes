package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A permanent's own protection whose colours read the board ("this creature has protection from each color among
 * permanents you control", Empty-Shrine Kannushi): applied in layer 6, after the layer-5 colours it reads (CR 613.1e,
 * 613.1f). Fixed and chosen-colour self protection is seeded by the layer system instead.
 */
@Component
@RequiredArgsConstructor
public class ProtectionFromColorsSelfEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsEffect) effect;
        if (protection.subject() != null || !protection.colorSet().readsBoard() || context.sourceControllerId() == null) {
            return;
        }
        accumulator.addProtectionColors(gameQueryService.protectionColorsForStaticEvaluation(
                context.gameData(), protection, context.source(), context.sourceControllerId()));
    }
}
