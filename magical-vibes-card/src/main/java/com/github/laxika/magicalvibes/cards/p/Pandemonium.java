package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "EXO", collectorNumber = "93")
@CardRegistration(set = "TPR", collectorNumber = "149")
public class Pandemonium extends Card {

    public Pandemonium() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new DealDamageToAnyTargetEffect(new SourcePower()),
                        "Have it deal damage to any target?"));
    }
}
