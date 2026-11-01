package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "25")
@CardRegistration(set = "MOC", collectorNumber = "210")
public class SuturePriest extends Card {

    public SuturePriest() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS),
                new MayEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), "Have that player lose 1 life?"));
    }
}
