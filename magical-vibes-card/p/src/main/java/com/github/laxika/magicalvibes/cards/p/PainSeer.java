package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "80")
public class PainSeer extends Card {

    public PainSeer() {
        addTrigger(Triggers.becomesUntapped(),
                new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
