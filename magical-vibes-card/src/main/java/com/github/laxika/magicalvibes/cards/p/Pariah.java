package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "33")
@CardRegistration(set = "7ED", collectorNumber = "30")
@CardRegistration(set = "USG", collectorNumber = "28")
public class Pariah extends Card {

    public Pariah() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToEnchantedCreatureEffect());
    }
}
