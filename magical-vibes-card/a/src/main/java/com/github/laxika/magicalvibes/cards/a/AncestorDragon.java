package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "489")
@CardRegistration(set = "GS1", collectorNumber = "12")
public class AncestorDragon extends Card {

    public AncestorDragon() {
        // Whenever one or more creatures you control attack, you gain 1 life for each attacking creature.
        addTrigger(Triggers.youAttack(),
                new GainLifeEffect(new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER)));
    }
}
