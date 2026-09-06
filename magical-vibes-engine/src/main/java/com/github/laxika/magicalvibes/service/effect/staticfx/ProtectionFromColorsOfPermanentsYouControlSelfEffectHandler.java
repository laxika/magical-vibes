package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class ProtectionFromColorsOfPermanentsYouControlSelfEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public ProtectionFromColorsOfPermanentsYouControlSelfEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsOfPermanentsYouControlEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsOfPermanentsYouControlEffect) effect;
        if (protection.scope() != null) {
            return;
        }
        if (context.sourceControllerId() == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(context.sourceControllerId());
        if (battlefield == null) {
            return;
        }

        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (Permanent permanent : battlefield) {
            colors.addAll(gameQueryService.colorsForStaticEvaluation(permanent));
        }
        accumulator.addProtectionColors(colors);
    }
}
