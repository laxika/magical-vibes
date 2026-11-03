package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "73")
public class StalkedResearcher extends Card {

    public StalkedResearcher() {
        CanAttackAsThoughNoDefenderEffect canAttack = new CanAttackAsThoughNoDefenderEffect();
        addTrigger(List.of(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                Triggers.youFullyUnlockRoom()), canAttack);
    }
}
