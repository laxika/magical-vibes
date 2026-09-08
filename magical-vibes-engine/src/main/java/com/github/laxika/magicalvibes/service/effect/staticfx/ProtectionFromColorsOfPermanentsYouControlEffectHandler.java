package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ProtectionFromColorsOfPermanentsYouControlEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public ProtectionFromColorsOfPermanentsYouControlEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsOfPermanentsYouControlEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsOfPermanentsYouControlEffect) effect;
        if (protection.scope() != GrantScope.ENCHANTED_CREATURE
                || !context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(context.sourceControllerId());
        if (battlefield == null) return;

        Set<CardColor> colors = new HashSet<>();
        for (Permanent permanent : battlefield) {
            colors.addAll(gameQueryService.colorsForStaticEvaluation(permanent));
        }
        accumulator.addProtectionColors(colors);
    }
}
