package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MRD", collectorNumber = "272")
@CardRegistration(set = "DDI", collectorNumber = "68")
@CardRegistration(set = "CMM", collectorNumber = "418")
public class VulshokBattlegear extends Card {

    public VulshokBattlegear() {
        addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
