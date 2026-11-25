package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "261")
public class LionHeart extends Card {

    public LionHeart() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(2));
        addStaticEffect(new StaticBoostEffect(2, 1, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
