package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BLB", collectorNumber = "206")
public class BurrowguardMentor extends Card {

    public BurrowguardMentor() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(creaturesYouControl, creaturesYouControl));
    }
}
