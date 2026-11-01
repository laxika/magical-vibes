package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "110")
@CardRegistration(set = "INR", collectorNumber = "378")
@CardRegistration(set = "MID", collectorNumber = "102")
@CardRegistration(set = "DBL", collectorNumber = "102")
public class GhoulishProcession extends Card {

    public GhoulishProcession() {
        // Whenever one or more nontoken creatures die, create a 2/2 black Zombie creature token
        // with decayed. This ability triggers only once each turn.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANY),
                new OncePerTurnTriggerEffect(CreateTokenEffect.blackZombieWithDecayed(1)));
    }
}
