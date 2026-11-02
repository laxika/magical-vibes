package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "93")
@CardRegistration(set = "DDK", collectorNumber = "52")
@CardRegistration(set = "MM3", collectorNumber = "98")
public class Hellrider extends Card {

    public Hellrider() {
        addTrigger(Triggers.creatureYouControlAttacks(), new DealDamageToAttackedTargetEffect(1));
    }
}
