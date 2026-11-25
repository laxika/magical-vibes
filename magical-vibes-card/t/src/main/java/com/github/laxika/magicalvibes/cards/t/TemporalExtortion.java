package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "81")
@CardRegistration(set = "SLD", collectorNumber = "1859")
public class TemporalExtortion extends Card {

    public TemporalExtortion() {
        addTrigger(Triggers.youCastThisSpell(), UnlessPaysEffect.mayPay(Subjects.eachPlayer(), PayLifeCost.halfLife(),
                CounterSpellEffect.triggeringSpell()));
        addSpellEffect(new ControllerExtraTurnEffect(1));
    }
}
