package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesByManaSymbolEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostOwnCreaturesByManaSymbolEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostOwnCreaturesByManaSymbolEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        BoostOwnCreaturesByManaSymbolEffect boost = (BoostOwnCreaturesByManaSymbolEffect) effect;
        if (!context.targetOnSameBattlefield()) {
            return;
        }
        Permanent target = context.target();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());
        if (!support.isEffectivelyCreature(context.gameData(), target, hasAnimateArtifacts)) {
            return;
        }
        ManaCost cost = target.getCard().getParsedManaCost();
        if (cost == null) {
            return;
        }
        int symbols = cost.countColorSymbols(boost.manaColor());
        if (symbols == 0) {
            return;
        }
        accumulator.addPower(boost.powerPerSymbol() * symbols);
        accumulator.addToughness(boost.toughnessPerSymbol() * symbols);
    }
}
