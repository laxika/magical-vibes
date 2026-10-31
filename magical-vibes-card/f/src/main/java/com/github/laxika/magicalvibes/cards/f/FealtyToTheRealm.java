package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LTC", collectorNumber = "21")
@CardRegistration(set = "LTC", collectorNumber = "104")
@CardRegistration(set = "LTC", collectorNumber = "423")
public class FealtyToTheRealm extends Card {

    public FealtyToTheRealm() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect())
                .addStaticEffect(new ControlEnchantedCreatureEffect(true))
                .addStaticEffect(new MustAttackEffect())
                .addStaticEffect(new EnchantedCreatureCantAttackPlayerEffect());
    }
}
