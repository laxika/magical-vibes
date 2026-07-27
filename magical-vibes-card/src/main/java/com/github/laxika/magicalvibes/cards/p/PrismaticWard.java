package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventColorDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "53")
@CardRegistration(set = "ICE", collectorNumber = "47")
public class PrismaticWard extends Card {

    public PrismaticWard() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new PreventColorDamageToEnchantedCreatureEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
    }
}
