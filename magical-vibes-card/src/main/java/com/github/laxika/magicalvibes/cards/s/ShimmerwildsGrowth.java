package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "194")
public class ShimmerwildsGrowth extends Card {

    public ShimmerwildsGrowth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesChosenColorEffect())
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                        new AddManaOnEnchantedLandTapEffect(new AwardChosenColorManaEffect()));
    }
}
