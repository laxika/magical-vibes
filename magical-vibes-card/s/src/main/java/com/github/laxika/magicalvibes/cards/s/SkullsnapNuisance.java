package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "234")
public class SkullsnapNuisance extends Card {

    public SkullsnapNuisance() {
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), new SurveilEffect(1));
        addTrigger(Triggers.youFullyUnlockRoom(), new SurveilEffect(1));
    }
}
