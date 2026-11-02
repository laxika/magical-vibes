package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "182")
@CardRegistration(set = "INR", collectorNumber = "405")
@CardRegistration(set = "VOW", collectorNumber = "182")
@CardRegistration(set = "DBL", collectorNumber = "449")
public class VoldarenEpicure extends Card {

    public VoldarenEpicure() {
        // When this creature enters, it deals 1 damage to each opponent. Create a Blood token.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));
    }
}
