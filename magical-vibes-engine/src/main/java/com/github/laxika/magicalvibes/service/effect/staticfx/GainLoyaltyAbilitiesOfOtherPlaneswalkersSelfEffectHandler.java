package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLoyaltyAbilitiesOfOtherPlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainLoyaltyAbilitiesOfOtherPlaneswalkersSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLoyaltyAbilitiesOfOtherPlaneswalkersEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        for (var playerId : context.gameData().orderedPlayerIds) {
            var battlefield = context.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(context.source().getId())
                        || !support.matchesStaticFilter(context, permanent, new PermanentIsPlaneswalkerPredicate())) {
                    continue;
                }
                for (var ability : permanent.getCard().getActivatedAbilities()) {
                    if (ability.getLoyaltyCost() != null) {
                        accumulator.addActivatedAbility(ability.withGrantSource(context.source().getId()));
                    }
                }
            }
        }
    }
}
