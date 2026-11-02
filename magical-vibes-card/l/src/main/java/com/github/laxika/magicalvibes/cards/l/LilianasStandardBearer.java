package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "110")
@CardRegistration(set = "M21", collectorNumber = "299")
@CardRegistration(set = "SLD", collectorNumber = "845")
@CardRegistration(set = "MOC", collectorNumber = "255")
@CardRegistration(set = "SCD", collectorNumber = "87")
public class LilianasStandardBearer extends Card {

    public LilianasStandardBearer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(new CreatureDeathsThisTurn(CountScope.CONTROLLER)));
    }
}
