package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "308")
public class Panacea extends Card {

    public Panacea() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(PreventDamageEffect.nextToTarget(new XValue())),
                "{X}{X}, {T}: Prevent the next X damage that would be dealt to any target this turn."
        ));
    }
}
