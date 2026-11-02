package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayXEnergyCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "75")
@CardRegistration(set = "M3C", collectorNumber = "127")
public class SphinxOfTheRevelation extends Card {

    public SphinxOfTheRevelation() {
        addTrigger(Triggers.youGainLife(), new EnergyCountersEffect(new EventValue()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}{U}",
                List.of(new PayXEnergyCost(), new DrawCardEffect(new XValue())),
                "{W}{U}{U}, {T}, Pay X {E}: Draw X cards."
        ).withXValue());
    }
}
