package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "37")
public class ScrapworkCohort extends Card {

    public ScrapworkCohort() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Soldier", 1, 1, null,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT)));

        addUnearth("{2}{W}");
    }
}
