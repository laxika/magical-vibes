package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventColorDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "53")
@CardRegistration(set = "ICE", collectorNumber = "47")
public class PrismaticWard extends Card {

    public PrismaticWard() {
        target(TargetFilters.creature()).addStaticEffect(new PreventColorDamageToEnchantedCreatureEffect());
        addStaticEffect(new ChooseColorOnEnterEffect());
    }
}
