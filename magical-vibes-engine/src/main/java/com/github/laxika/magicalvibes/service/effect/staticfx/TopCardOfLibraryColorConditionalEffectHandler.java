package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopCardOfLibraryColorConditionalEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TopCardOfLibraryColorConditionalEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            if (!support.isTopCardOfLibraryColor(context, conditional.color())) return;
            boolean scopeMatch = switch (boost.scope()) {
                case OWN_CREATURES, ALL_OWN_CREATURES -> context.targetOnSameBattlefield();
                case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                case ALL_CREATURES -> true;
                default -> false;
            };
            if (scopeMatch && support.matchesStaticFilter(context.target(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            if (!support.isTopCardOfLibraryColor(context, conditional.color())) return;
            if (support.matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }
}
