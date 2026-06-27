package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpponentPoisonedConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentPoisonedConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (OpponentPoisonedConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        boolean opponentPoisoned = false;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && context.gameData().playerPoisonCounters.getOrDefault(playerId, 0) > 0) {
                opponentPoisoned = true;
                break;
            }
        }
        if (opponentPoisoned) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof GrantKeywordEffect grant) {
                accumulator.addKeywords(grant.keywords());
            } else if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
                accumulator.addProtectionColors(protection.colors());
            }
        }
    }
}
