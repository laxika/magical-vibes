package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LifeLostWhenTriggeringPermanentEntered;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllButOneLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "99")
public class SoulgorgerOrgg extends Card {

    public SoulgorgerOrgg() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseAllButOneLifeEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new GainLifeEffect(new LifeLostWhenTriggeringPermanentEntered()));
    }
}
