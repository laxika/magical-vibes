package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "140")
@CardRegistration(set = "GK2", collectorNumber = "61")
@CardRegistration(set = "ARC", collectorNumber = "79")
public class AvatarOfDiscord extends Card {

    public AvatarOfDiscord() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessDiscardCardTypeEffect(null, 2));
    }
}
