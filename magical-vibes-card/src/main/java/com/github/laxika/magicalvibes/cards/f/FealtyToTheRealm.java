package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LTC", collectorNumber = "21")
@CardRegistration(set = "LTC", collectorNumber = "104")
public class FealtyToTheRealm extends Card {

    public FealtyToTheRealm() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect(true))
                .addEffect(EffectSlot.STATIC, new MustAttackEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackControllerEffect());
    }
}
