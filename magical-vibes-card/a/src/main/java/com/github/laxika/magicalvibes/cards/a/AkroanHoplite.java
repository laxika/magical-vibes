package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "185")
public class AkroanHoplite extends Card {

    public AkroanHoplite() {
        // Whenever this creature attacks, it gets +X/+0 until end of turn, where X is the number
        // of attacking creatures you control.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
