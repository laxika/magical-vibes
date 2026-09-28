package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "34")
@CardRegistration(set = "PIP", collectorNumber = "562")
public class NerdRage extends Card {

    public NerdRage() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        new NoMaximumHandSizeEffect(), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new ConditionalEffect(new CardsInHandAtLeast(10), new BoostSelfEffect(10, 10)),
                        GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2));
    }
}
