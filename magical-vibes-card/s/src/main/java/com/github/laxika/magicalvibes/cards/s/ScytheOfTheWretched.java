package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "239")
public class ScytheOfTheWretched extends Card {

    public ScytheOfTheWretched() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_ATTACHED),
                new ReturnDyingCreatureToBattlefieldEffect(true));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
