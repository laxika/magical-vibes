package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "229")
@CardRegistration(set = "M21", collectorNumber = "236")
@CardRegistration(set = "MB1", collectorNumber = "229")
public class ShortSword extends Card {

    public ShortSword() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
