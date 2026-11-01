package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "125")
public class TransmograntsCrown extends Card {

    public TransmograntsCrown() {
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new DrawCardEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
        addActivatedAbility(new EquipActivatedAbility("{B}"));
    }
}
