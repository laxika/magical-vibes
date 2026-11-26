package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "25")
public class ElectricEel extends Card {

    public ElectricEel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.to(Subjects.you(), 1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(
                        BoostEffect.self(2, 0),
                        DealDamageEffect.to(Subjects.you(), 1)
                ),
                "{R}{R}: This creature gets +2/+0 until end of turn and deals 1 damage to you."
        ));
    }
}
