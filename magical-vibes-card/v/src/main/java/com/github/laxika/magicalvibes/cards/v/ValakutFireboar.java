package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "170")
public class ValakutFireboar extends Card {

    public ValakutFireboar() {
        addTrigger(Triggers.attacks(), new SwitchPowerToughnessEffect(true));
    }
}
