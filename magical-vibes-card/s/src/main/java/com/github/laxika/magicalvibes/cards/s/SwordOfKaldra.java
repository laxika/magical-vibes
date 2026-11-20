package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "251")
public class SwordOfKaldra extends Card {

    public SwordOfKaldra() {
        addStaticEffect(new StaticBoostEffect(5, 5, Subjects.attachedCreature()));
        addTrigger(Triggers.equippedCreatureDealsDamageToCreature(),
                ExileTargetPermanentEffect.ofTriggeringPermanent());
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
