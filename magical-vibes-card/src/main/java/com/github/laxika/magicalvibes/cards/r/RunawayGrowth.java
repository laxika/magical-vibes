package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YNEO", collectorNumber = "28")
public class RunawayGrowth extends Card {

    public RunawayGrowth() {
        // Starting intensity 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.INTENSITY));

        // Whenever enchanted land is tapped for mana, its controller adds green mana equal to this
        // enchantment's intensity. Then this enchantment intensifies by 1.
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, SequenceEffect.of(
                new AddManaOnEnchantedLandTapEffect(
                        new AwardManaEffect(ManaColor.GREEN, new CountersOnSource(CounterType.INTENSITY))),
                new PutCountersOnSelfEffect(CounterType.INTENSITY)));
    }
}
