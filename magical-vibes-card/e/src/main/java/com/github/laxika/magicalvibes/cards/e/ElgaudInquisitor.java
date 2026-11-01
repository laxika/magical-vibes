package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "6")
public class ElgaudInquisitor extends Card {

    public ElgaudInquisitor() {
        // Lifelink is loaded automatically from Scryfall.
        // When Elgaud Inquisitor dies, create a 1/1 white Spirit creature token with flying.
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(1));
    }
}
