package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GainActivatedAndTriggeredAbilitiesOfExiledCardsSelfEffectHandler
        implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.sourceId());
        for (Card card : exiledCards) {
            for (ActivatedAbility ability : ManaAbilities.withIntrinsic(card)) {
                accumulator.addActivatedAbility(ability);
            }

            for (EffectSlot slot : EffectSlot.values()) {
                for (CardEffect triggeredEffect : card.getEffects(slot)) {
                    accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(
                            slot, triggeredEffect, GrantScope.SELF));
                }
            }
        }
    }
}
