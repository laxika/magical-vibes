package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ODY", collectorNumber = "102")
@CardRegistration(set = "TLE", collectorNumber = "19")
@CardRegistration(set = "MB2", collectorNumber = "172")
public class Standstill extends Card {

    public Standstill() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect(3, Subjects.opponentsOf(Subjects.triggeringPlayer())))));
    }
}
