package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "162")
@CardRegistration(set = "FIN", collectorNumber = "368")
public class SummonGFCerberus extends Card {

    public SummonGFCerberus() {
        addTrigger(Triggers.chapter(1), new SurveilEffect(1));
        addTrigger(Triggers.chapter(2), new CopyNextInstantOrSorceryCastThisTurnEffect());
        addTrigger(Triggers.chapter(3), new CopyNextInstantOrSorceryCastThisTurnEffect(new Fixed(2)));
    }
}
