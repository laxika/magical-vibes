package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "20")
@CardRegistration(set = "INR", collectorNumber = "33")
@CardRegistration(set = "DDK", collectorNumber = "13")
public class MausoleumGuard extends Card {

    public MausoleumGuard() {
        // When Mausoleum Guard dies, create two 1/1 white Spirit creature tokens with flying.
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(2));
    }
}
