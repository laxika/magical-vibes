package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "132")
public class LonghornSharpshooter extends Card {

    public LonghornSharpshooter() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{R}"))));
        addTrigger(Triggers.becomesPlotted(), new DealDamageToAnyTargetEffect(2));
    }
}
