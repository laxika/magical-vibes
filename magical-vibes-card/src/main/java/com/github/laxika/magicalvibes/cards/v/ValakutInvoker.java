package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "159")
public class ValakutInvoker extends Card {

    public ValakutInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{8}", List.of(new DealDamageToAnyTargetEffect(3)),
                "{8}: Valakut Invoker deals 3 damage to any target."));
    }
}
