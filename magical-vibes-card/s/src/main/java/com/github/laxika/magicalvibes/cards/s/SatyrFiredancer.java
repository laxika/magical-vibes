package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "108")
public class SatyrFiredancer extends Card {

    public SatyrFiredancer() {
        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(),
                new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue()));
    }
}
