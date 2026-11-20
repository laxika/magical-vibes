package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "96")
public class EnormousEnergyBlade extends Card {

    public EnormousEnergyBlade() {
        addStaticEffect(new StaticBoostEffect(4, 0, Subjects.attachedCreature()));
        addTrigger(Triggers.becomesAttachedToCreature(),
                new TapPermanentsEffect(Subjects.attached()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
