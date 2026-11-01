package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "113")
public class MagneticMine extends Card {

    public MagneticMine() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
