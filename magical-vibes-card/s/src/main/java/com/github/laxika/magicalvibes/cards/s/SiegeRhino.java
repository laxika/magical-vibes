package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "200")
@CardRegistration(set = "CP3", collectorNumber = "5")
@CardRegistration(set = "EA1", collectorNumber = "16")
public class SiegeRhino extends Card {

    public SiegeRhino() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(3));
    }
}
