package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "194")
public class ShimmerwildsGrowth extends Card {

    public ShimmerwildsGrowth() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        target(TargetFilters.land())
                .addStaticEffect(new EnchantedPermanentBecomesChosenColorEffect())
                .addTrigger(Triggers.enchantedLandTappedForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardChosenColorManaEffect()));
    }
}
