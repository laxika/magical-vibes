package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "168")
public class JewelEyedCobra extends Card {

    public JewelEyedCobra() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));
    }
}
