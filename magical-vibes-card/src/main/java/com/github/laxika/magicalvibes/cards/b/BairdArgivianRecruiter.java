package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "195")
public class BairdArgivianRecruiter extends Card {

    public BairdArgivianRecruiter() {
        // At the beginning of your end step, if you control a creature with power greater than
        // its base power, create a 1/1 white Soldier creature token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerGreaterThanBasePowerPredicate()))),
                CreateTokenEffect.whiteSoldier(1)));
    }
}
