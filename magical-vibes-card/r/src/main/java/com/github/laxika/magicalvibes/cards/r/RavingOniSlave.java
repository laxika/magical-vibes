package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "86")
public class RavingOniSlave extends Card {

    public RavingOniSlave() {
        // "you lose 3 life if you don't control a Demon" is checked only on resolution, not an
        // intervening-if (CR 603.4), so the ability triggers even while you control a Demon.
        ConditionalEffect lifeLoss = ConditionalEffect.unless(
                new NotCondition(new ControlsPermanentCount(1,
                        new PermanentHasSubtypePredicate(CardSubtype.DEMON))),
                new LoseLifeEffect(3)
        );
        addTrigger(Triggers.enters(ObjectRelation.THIS), lifeLoss);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), lifeLoss);
    }
}
