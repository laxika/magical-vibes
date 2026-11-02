package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostDrawnDragonEffect;
import com.github.laxika.magicalvibes.model.effect.SeekDragonAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "8")
public class DarigaazsWhelp extends Card {

    public DarigaazsWhelp() {
        addStaticEffect(new KickerEffect("{1}{R}"));
        addTrigger(Triggers.youDrawCard(), new PerpetuallyBoostDrawnDragonEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new Kicked(), new SeekDragonAndPerpetuallyBoostEffect()));
    }
}
