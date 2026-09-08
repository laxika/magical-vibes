package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "29")
public class SlumberingKeepguard extends Card {

    public SlumberingKeepguard() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, new ScryEffect(1));

        PermanentCount enchantmentsYouControl =
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new BoostSelfEffect(enchantmentsYouControl, enchantmentsYouControl)),
                "{2}{W}: This creature gets +1/+1 until end of turn for each enchantment you control."
        ));
    }
}
