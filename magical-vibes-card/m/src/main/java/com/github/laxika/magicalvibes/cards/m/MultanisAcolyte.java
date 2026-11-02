package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "108")
public class MultanisAcolyte extends Card {

    public MultanisAcolyte() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{G}{G}"));
    }
}
