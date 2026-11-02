package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "231")
public class PsychogenicProbe extends Card {

    public PsychogenicProbe() {
        addTrigger(Triggers.playerShufflesLibrary(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));
    }
}
