package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "220")
public class WarlordsAxe extends Card {

    public WarlordsAxe() {
        addStaticEffect(new StaticBoostEffect(3, 1, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
