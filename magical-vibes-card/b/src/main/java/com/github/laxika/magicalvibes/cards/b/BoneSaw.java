package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CON", collectorNumber = "135")
@CardRegistration(set = "OGW", collectorNumber = "161")
@CardRegistration(set = "BRR", collectorNumber = "7")
public class BoneSaw extends Card {

    public BoneSaw() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
