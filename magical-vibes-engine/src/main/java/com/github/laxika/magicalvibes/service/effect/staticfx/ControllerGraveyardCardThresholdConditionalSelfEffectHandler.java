package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControllerGraveyardCardThresholdConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerGraveyardCardThresholdConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerGraveyardCardThresholdConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, conditional.filter(), null)) {
                    count++;
                }
            }
        }
        if (count >= conditional.threshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || support.matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }
}
