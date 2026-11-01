package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "200")
@CardRegistration(set = "SLD", collectorNumber = "878")
@CardRegistration(set = "HBG", collectorNumber = "221")
@CardRegistration(set = "LTC", collectorNumber = "256")
@CardRegistration(set = "BLC", collectorNumber = "121")
public class ProsperousInnkeeper extends Card {

    public ProsperousInnkeeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new GainLifeEffect(1));
    }
}
