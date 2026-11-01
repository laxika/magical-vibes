package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "209")
public class SylvokLifestaff extends Card {

    public SylvokLifestaff() {
        // Equipped creature gets +1/+0
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, you gain 3 life
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new GainLifeEffect(3));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
