package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "60")
@CardRegistration(set = "C19", collectorNumber = "104")
@CardRegistration(set = "C16", collectorNumber = "106")
@CardRegistration(set = "CM2", collectorNumber = "58")
public class BaneOfTheLiving extends Card {

    public BaneOfTheLiving() {
        addMorph("{X}{B}{B}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new BoostAllCreaturesEffect(
                new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
