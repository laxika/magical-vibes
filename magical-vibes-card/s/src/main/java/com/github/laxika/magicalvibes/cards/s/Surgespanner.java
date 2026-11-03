package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "92")
@CardRegistration(set = "LCC", collectorNumber = "174")
public class Surgespanner extends Card {

    public Surgespanner() {
        // Whenever this creature becomes tapped, you may pay {1}{U}. If you do,
        // return target permanent to its owner's hand.
        addTrigger(Triggers.becomesTapped(),
                new MayPayManaEffect("{1}{U}",
                        ReturnToHandEffect.target(),
                        "Pay {1}{U} to return target permanent to its owner's hand?"));

        // Target filter for the may-pay bounce trigger: any permanent.
        setCastTimeTargetFilter(TargetFilters.permanent());
    }
}
