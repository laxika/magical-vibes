package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "204")
public class GadgetTechnician extends Card {

    public GadgetTechnician() {
        CreateTokenEffect thopter = new CreateTokenEffect(
                "Thopter", 1, 1, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), thopter);
        addMorph("{U/R}{U/R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), thopter);
    }
}
