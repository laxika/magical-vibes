package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Gives the named permanents the activated abilities of the cards of that type exiled with the source. */
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilitiesOfCardsExiledWithSourceEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilitiesOfCardsExiledWithSourceEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilitiesOfCardsExiledWithSourceEffect) effect;
        if (!support.names(context, grant.subject(), grant.filter())) {
            return;
        }
        for (Card card : context.gameData().getCardsExiledByPermanent(context.sourceId())) {
            if (!card.hasType(grant.cardType())) {
                continue;
            }
            for (ActivatedAbility ability : ManaAbilities.withIntrinsic(card)) {
                accumulator.addActivatedAbility(ability);
            }
        }
    }
}
