package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "112")
@CardRegistration(set = "FDN", collectorNumber = "523")
@CardRegistration(set = "GN3", collectorNumber = "56")
public class MaalfeldTwins extends Card {

    public MaalfeldTwins() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.blackZombie(2));
    }
}
