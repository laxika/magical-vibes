package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MRD", collectorNumber = "243")
@CardRegistration(set = "TD2", collectorNumber = "21")
public class SlagwurmArmor extends Card {

    public SlagwurmArmor() {
        addStaticEffect(new StaticBoostEffect(0, 6, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
