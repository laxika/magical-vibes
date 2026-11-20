package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MRD", collectorNumber = "146")
@CardRegistration(set = "TD2", collectorNumber = "20")
@CardRegistration(set = "MMA", collectorNumber = "202")
@CardRegistration(set = "HA4", collectorNumber = "21")
public class Bonesplitter extends Card {

    public Bonesplitter() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
