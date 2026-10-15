package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOK", collectorNumber = "86")
public class RavingOniSlave extends Card {

    public RavingOniSlave() {
        ConditionalEffect lifeLoss = new ConditionalEffect(
                new NotCondition(new ControlsPermanentCount(1,
                        new PermanentHasSubtypePredicate(CardSubtype.DEMON))),
                new LoseLifeEffect(3)
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, lifeLoss);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, lifeLoss);
    }
}
