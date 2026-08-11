package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ZEN", collectorNumber = "122")
public class Electropotence extends Card {

    public Electropotence() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{2}{R}",
                new DealDamageToAnyTargetEffect(new SourcePower()),
                "Pay {2}{R} to have that creature deal damage equal to its power to any target?",
                true));
    }
}
