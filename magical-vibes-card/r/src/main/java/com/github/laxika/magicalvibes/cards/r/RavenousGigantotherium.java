package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C20", collectorNumber = "63")
public class RavenousGigantotherium extends Card {

    public RavenousGigantotherium() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(3));

        targetUpTo(new SourcePower(), TargetFilters.creature(), 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDividedDamageEffect(new SourcePower(), null, DivisionMode.CHOSEN,
                                new PermanentIsCreaturePredicate(), 0, false, false, true))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EachTargetCreatureDealsPowerDamageToSourceEffect());
    }
}
