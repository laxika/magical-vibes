package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AVR", collectorNumber = "223")
public class VanguardsShield extends Card {

    public VanguardsShield() {
        addStaticEffect(new StaticBoostEffect(0, 3, Subjects.attachedCreature()));
        addStaticEffect(new GrantAdditionalBlockEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
