package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "154")
public class AkkiRockspeaker extends Card {

    public AkkiRockspeaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardManaEffect(ManaColor.RED, 1));
    }
}
