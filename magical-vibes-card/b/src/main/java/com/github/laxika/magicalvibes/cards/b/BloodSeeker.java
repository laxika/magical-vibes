package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "81")
@CardRegistration(set = "ZEN", collectorNumber = "80")
@CardRegistration(set = "DSC", collectorNumber = "77")
public class BloodSeeker extends Card {

    public BloodSeeker() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS),
                new MayEffect(new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER), "Have that player lose 1 life?"));
    }
}
