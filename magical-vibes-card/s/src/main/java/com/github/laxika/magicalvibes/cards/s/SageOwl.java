package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "95")
@CardRegistration(set = "10E", collectorNumber = "104")
@CardRegistration(set = "7ED", collectorNumber = "96")
@CardRegistration(set = "8ED", collectorNumber = "98")
@CardRegistration(set = "M10", collectorNumber = "69")
@CardRegistration(set = "WTH", collectorNumber = "52")
public class SageOwl extends Card {

    public SageOwl() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReorderTopCardsOfLibraryEffect(4));
    }
}
