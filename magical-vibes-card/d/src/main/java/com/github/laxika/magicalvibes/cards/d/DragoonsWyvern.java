package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "49")
public class DragoonsWyvern extends Card {

    public DragoonsWyvern() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Hero", 1, 1, null,
                List.of(CardSubtype.HERO), Set.of(), Set.of()));
    }
}
