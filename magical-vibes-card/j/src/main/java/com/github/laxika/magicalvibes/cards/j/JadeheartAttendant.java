package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class JadeheartAttendant extends Card {

    public JadeheartAttendant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new TotalManaValueOfCardsExiledWithSource()));
    }
}
