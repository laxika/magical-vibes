package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "8")
public class DescendantOfStorms extends Card {

    public DescendantOfStorms() {
        addTrigger(Triggers.attacks(), new MayPayManaEffect("{1}{W}",
                new EndureEffect(1), "Pay {1}{W} to endure 1?"));
    }
}
