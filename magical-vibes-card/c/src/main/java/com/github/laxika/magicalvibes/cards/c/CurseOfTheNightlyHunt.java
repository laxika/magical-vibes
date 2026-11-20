package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ISD", collectorNumber = "137")
@CardRegistration(set = "MB1", collectorNumber = "90")
@CardRegistration(set = "C15", collectorNumber = "147")
@CardRegistration(set = "CM2", collectorNumber = "90")
public class CurseOfTheNightlyHunt extends Card {

    public CurseOfTheNightlyHunt() {
        addStaticEffect(new MustAttackEffect(Subjects.creaturesEnchantedPlayerControls()));
    }
}
