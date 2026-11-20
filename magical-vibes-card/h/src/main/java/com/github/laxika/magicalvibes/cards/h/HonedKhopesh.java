package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AKH", collectorNumber = "230")
public class HonedKhopesh extends Card {

    public HonedKhopesh() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
