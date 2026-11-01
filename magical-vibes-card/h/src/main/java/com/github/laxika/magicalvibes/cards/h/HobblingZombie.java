package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "106")
@CardRegistration(set = "DBL", collectorNumber = "106")
public class HobblingZombie extends Card {

    public HobblingZombie() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
