package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "240")
@CardRegistration(set = "TLE", collectorNumber = "282")
public class IrohFirebendingInstructor extends Card {

    public IrohFirebendingInstructor() {
        addTrigger(Triggers.attacks(), new BoostAllOwnCreaturesEffect(
                1, 1, new PermanentIsAttackingPredicate()));
    }
}
