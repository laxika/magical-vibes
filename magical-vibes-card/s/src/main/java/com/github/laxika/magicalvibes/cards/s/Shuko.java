package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BOK", collectorNumber = "159")
public class Shuko extends Card {

    public Shuko() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
