package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToEnchantedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "71")
public class TreacherousLink extends Card {

    public TreacherousLink() {
        target(TargetFilters.creature())
                .addStaticEffect(new RedirectAllDamageToEnchantedCreatureControllerEffect());
    }
}
