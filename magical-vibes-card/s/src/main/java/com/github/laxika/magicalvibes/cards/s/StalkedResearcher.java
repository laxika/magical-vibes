package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "73")
public class StalkedResearcher extends Card {

    public StalkedResearcher() {
        CanAttackAsThoughNoDefenderEffect canAttack = new CanAttackAsThoughNoDefenderEffect();
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), canAttack);
        addTrigger(Triggers.youFullyUnlockRoom(), canAttack);
    }
}
