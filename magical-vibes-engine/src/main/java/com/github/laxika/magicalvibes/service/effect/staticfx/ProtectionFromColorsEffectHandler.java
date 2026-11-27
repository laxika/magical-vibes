package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "[Enchanted / equipped creature] has protection from [colours]" (Sword of War and Peace, Ward of Lights, Pledge of
 * Loyalty, Commander's Plate): the colours judged for the Aura or Equipment, its controller's permanents and
 * commanders included ({@code GameQueryService.protectionColorsForStaticEvaluation}). A permanent's own fixed or
 * chosen-colour protection ({@code null} subject) is seeded by the layer system and stays its own while it is attached
 * (a bestowed or reconfigured creature's printed protection is not its host's, CR 702.16a); its own board-reading
 * protection is {@link ProtectionFromColorsSelfEffectHandler}'s.
 */
@Component
@RequiredArgsConstructor
public class ProtectionFromColorsEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsEffect) effect;
        if (protection.subject() != null && support.namesBeyondSource(context, protection.subject(), null)) {
            accumulator.addProtectionColors(gameQueryService.protectionColorsForStaticEvaluation(
                    context.gameData(), protection, context.source(), context.sourceControllerId()));
        }
    }
}
