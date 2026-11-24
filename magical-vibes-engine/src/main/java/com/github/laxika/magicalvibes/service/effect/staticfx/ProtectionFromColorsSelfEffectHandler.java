package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A permanent's own protection that the layer system does not seed, applied in layer 6 (CR 613.1f): protection whose
 * colours read the board ("this creature has protection from each color among permanents you control", Empty-Shrine
 * Kannushi), after the layer-5 colours it reads (CR 613.1e), and protection a set it is part of has ("All Slivers have
 * protection from the chosen color" on Ward Sliver itself), the colours judged for the permanent whose ability it is.
 * Fixed and chosen-colour protection of the permanent itself ({@code null} subject) is seeded instead, and the
 * enchanted or equipped creature's is {@link ProtectionFromColorsEffectHandler}'s.
 */
@Component
@RequiredArgsConstructor
public class ProtectionFromColorsSelfEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

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
        if (context.sourceControllerId() == null) {
            return;
        }
        boolean names = protection.subject() == null
                ? protection.colorSet().readsBoard()
                : !(protection.subject() instanceof PermanentSubject.Attached)
                        && support.names(context, protection.subject(), null);
        if (names) {
            accumulator.addProtectionColors(gameQueryService.protectionColorsForStaticEvaluation(
                    context.gameData(), protection, context.source(), context.sourceControllerId()));
        }
    }
}
