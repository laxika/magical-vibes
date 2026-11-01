package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "16")
public class LiegeOfTheAxe extends Card {

    public LiegeOfTheAxe() {
        addMorph("{1}{W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
