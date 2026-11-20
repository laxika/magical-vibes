package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "218")
public class HugsGrislyGuardian extends Card {

    public HugsGrislyGuardian() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardMayPlayThisTurnEffect(new XValue(), false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));
    }
}
