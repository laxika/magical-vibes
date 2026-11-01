package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "11")
@CardRegistration(set = "DDK", collectorNumber = "2")
@CardRegistration(set = "DDQ", collectorNumber = "7")
@CardRegistration(set = "IMA", collectorNumber = "16")
@CardRegistration(set = "2X2", collectorNumber = "9")
@CardRegistration(set = "SIS", collectorNumber = "5")
@CardRegistration(set = "E01", collectorNumber = "4")
public class DoomedTraveler extends Card {

    public DoomedTraveler() {
        // When Doomed Traveler dies, create a 1/1 white Spirit creature token with flying.
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(1));
    }
}
