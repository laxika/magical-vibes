package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TurnsTakenByController;
import com.github.laxika.magicalvibes.model.effect.BecomeDayEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueGreaterThanEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "1")
public class AngelOfEternalDawn extends Card {

    public AngelOfEternalDawn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeDayEffect());
        addStaticEffect(new CantBecomeNightEffect());
        addStaticEffect(new OpponentsCantCastSpellsWithManaValueGreaterThanEffect(
                new TurnsTakenByController()));
    }
}
