package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BlockedByMinCreaturesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlockedByMinCreaturesConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BlockedByMinCreaturesConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (BlockedByMinCreaturesConditionalEffect) effect;
        UUID sourceId = context.source().getId();

        final int[] blockerCount = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(sourceId)) {
                blockerCount[0]++;
            }
        });

        if (blockerCount[0] < conditional.minBlockers()) return;

        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
