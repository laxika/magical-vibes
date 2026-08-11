package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "84")
@CardRegistration(set = "5ED", collectorNumber = "107")
@CardRegistration(set = "4ED", collectorNumber = "89")
@CardRegistration(set = "INV", collectorNumber = "65")
public class PhantasmalTerrain extends Card {

    public PhantasmalTerrain() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseBasicLandTypeOnEnterEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesChosenTypeEffect());
    }
}
