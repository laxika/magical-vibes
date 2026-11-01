package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "627")
public class LivingLiesOfLoki extends Card {

    public LivingLiesOfLoki() {
        // This creature gets +1/+0 for each other Illusion you control.
        PermanentCount otherIllusionsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ILLUSION), CountScope.CONTROLLER, true);
        addStaticEffect(new BoostSelfEffect(otherIllusionsYouControl, new Fixed(0)));

        // When this creature dies, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
