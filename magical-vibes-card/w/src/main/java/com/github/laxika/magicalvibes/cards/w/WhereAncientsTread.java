package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "122")
@CardRegistration(set = "C13", collectorNumber = "130")
public class WhereAncientsTread extends Card {

    public WhereAncientsTread() {
        // Whenever a creature you control with power 5 or greater enters, you may have this
        // enchantment deal 5 damage to any target.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(5))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new DealDamageToAnyTargetEffect(5), "Deal 5 damage to any target?"));
    }
}
