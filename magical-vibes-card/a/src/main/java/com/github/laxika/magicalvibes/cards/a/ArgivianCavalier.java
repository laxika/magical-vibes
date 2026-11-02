package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "4")
public class ArgivianCavalier extends Card {

    public ArgivianCavalier() {
        // Enlist is handled by the attack-declaration engine from the Scryfall-loaded keyword.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(1));
    }
}
