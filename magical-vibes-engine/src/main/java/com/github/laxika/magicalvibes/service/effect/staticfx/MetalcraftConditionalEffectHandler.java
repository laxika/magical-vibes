package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetalcraftConditionalEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MetalcraftConditionalEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var metalcraft = (MetalcraftConditionalEffect) effect;
        CardEffect wrapped = metalcraft.wrapped();
        // Only handle broader-scoped effects in the non-self handler
        if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            int artifactCount = support.countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
                boolean scopeMatch = switch (grant.scope()) {
                    case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                            && support.matchesStaticFilter(context.target(), grant.filter());
                    default -> support.matchesCreatureScope(context, grant.scope(), grant.filter());
                };
                if (scopeMatch) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        } else if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            int artifactCount = support.countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
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
            }
        } else if (wrapped instanceof GrantActivatedAbilityEffect grant) {
            int artifactCount = support.countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
                boolean scopeMatch = switch (grant.scope()) {
                    case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                            && support.matchesStaticFilter(context.target(), grant.filter());
                    default -> support.matchesCreatureScope(context, grant.scope(), grant.filter());
                };
                if (scopeMatch) {
                    accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
                }
            }
        }
    }
}
