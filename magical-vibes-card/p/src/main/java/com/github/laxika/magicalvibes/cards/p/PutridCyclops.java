package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TopCardOfLibraryManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "75")
public class PutridCyclops extends Card {

    public PutridCyclops() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostSelfEffect(
                new Scaled(new TopCardOfLibraryManaValue(), -1),
                new Scaled(new TopCardOfLibraryManaValue(), -1)));
    }
}
