package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "100")
@CardRegistration(set = "DDQ", collectorNumber = "57")
@CardRegistration(set = "MM3", collectorNumber = "70")
@CardRegistration(set = "SIS", collectorNumber = "29")
@CardRegistration(set = "DSC", collectorNumber = "140")
@CardRegistration(set = "LCC", collectorNumber = "196")
@CardRegistration(set = "VOC", collectorNumber = "128")
@CardRegistration(set = "C17", collectorNumber = "113")
public class FalkenrathNoble extends Card {

    public FalkenrathNoble() {
        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)));
    }
}
